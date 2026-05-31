package com.example.wavex.profileScreen.settingScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.wavex.R
import com.example.wavex.SignIn
import com.example.wavex.fonts
import com.example.wavex.googleAuthentication.GoogleSignInManager
import com.example.wavex.homeScreen.viewModel.ProfileViewModel
import com.example.wavex.homeScreen.viewModel.RecentlyPlayedViewModel
import com.example.wavex.profileScreen.settingScreen.viewmodel.SettingsViewModel
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.songData.Download
import com.example.wavex.ui.theme.WaveXTheme
import com.example.wavex.updateAppScreen.UpdateAppActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

val Context.settingsDataStore by preferencesDataStore(
    name = "settings"
)

object SettingsKeys {
    val STREAM_QUALITY = stringPreferencesKey("stream_quality")
    val DOWNLOAD_QUALITY = stringPreferencesKey("download_quality")
    val PROFILE_URL = stringPreferencesKey("profile_url")
    val USER_NAME = stringPreferencesKey("user_name")
}

enum class AudioStreamQualityPreference(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    companion object {
        fun fromLabel(value: String?): AudioStreamQualityPreference {
            return when (
                value?.trim()?.lowercase()
            ) {
                "low" -> LOW
                "high" -> HIGH
                else -> MEDIUM
            }
        }
    }
}

enum class Quality {
    LOW,
    MEDIUM,
    HIGH,
    LOSSLESS
}

object StreamQualitySelector {
    private val playbackFallbackOrder =
        mapOf(
            AudioStreamQualityPreference.LOW to listOf(
                Quality.LOW,
                Quality.MEDIUM,
                Quality.HIGH,
                Quality.LOSSLESS
            ),

            AudioStreamQualityPreference.MEDIUM to listOf(
                Quality.MEDIUM,
                Quality.HIGH,
                Quality.LOW,
                Quality.LOSSLESS
            ),

            AudioStreamQualityPreference.HIGH to listOf(
                Quality.HIGH,
                Quality.LOSSLESS,
                Quality.MEDIUM,
                Quality.LOW
            )
        )

    fun selectPlaybackStream(
        streams: List<Download>,
        preference: AudioStreamQualityPreference
    ): Download? {
        Log.d(
            "PLAY_FLOW",
            "Preferred Quality = $preference"
        )

        val usable = streams.filter { isUsable(it) }

        Log.d(
            "PLAY_FLOW",
            "Usable Streams = ${usable.size}"
        )

        usable.forEach {
            Log.d(
                "PLAY_FLOW",
                """
            Quality : ${it.quality}
            Expire  : ${it.expiresAt}
            """.trimIndent()
            )
        }

        if (usable.isEmpty()) {
            Log.e(
                "PLAY_FLOW",
                "No usable streams found"
            )
            return null
        }

        val fallbackOrder = playbackFallbackOrder[preference] ?: emptyList()

        for (quality in fallbackOrder) {
            usable.lastOrNull {
                it.quality == quality
            }?.let {
                Log.d(
                    "PLAY_FLOW",
                    """
                Selected Stream
                Quality : ${it.quality}
                """.trimIndent()
                )

                return it
            }
        }

        return usable.lastOrNull()
    }

    private fun isUsable(stream: Download): Boolean {
        val url = stream.url.trim()

        if (url.isEmpty()) {
            return false
        }

        val uri = url.toUri()
        val scheme = uri.scheme ?: return false

        if (
            scheme != "http" &&
            scheme != "https" &&
            scheme != "file"
        ) {
            return false
        }

        val expiresAt = stream.expiresAt ?: return true
        val now = System.currentTimeMillis() / 1000

        return expiresAt > now
    }
}

object DownloadQualitySelector {
    private val downloadFallbackOrder =
        mapOf(
            AudioStreamQualityPreference.LOW to listOf(
                Quality.LOW,
                Quality.MEDIUM,
                Quality.HIGH,
                Quality.LOSSLESS
            ),

            AudioStreamQualityPreference.MEDIUM to listOf(
                Quality.MEDIUM,
                Quality.HIGH,
                Quality.LOW,
                Quality.LOSSLESS
            ),

            AudioStreamQualityPreference.HIGH to listOf(
                Quality.HIGH,
                Quality.LOSSLESS,
                Quality.MEDIUM,
                Quality.LOW
            )
        )

    fun selectDownload(
        downloads: List<Download>,
        preference: AudioStreamQualityPreference
    ): Download? {

        val fallbackOrder = downloadFallbackOrder[preference] ?: emptyList()

        for (quality in fallbackOrder) {
            downloads.lastOrNull {
                it.quality == quality
            }?.let {
                return it
            }
        }

        return downloads.lastOrNull()
    }
}

@Singleton
class SettingsDataStore @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {
    val streamQualityFlow: Flow<AudioStreamQualityPreference> =
        context.settingsDataStore.data.map { prefs ->
            val stored = prefs[SettingsKeys.STREAM_QUALITY]
            AudioStreamQualityPreference.fromLabel(stored)
        }

    suspend fun setStreamQuality(preference: AudioStreamQualityPreference) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.STREAM_QUALITY] = preference.label
        }
    }

    val downloadQualityFlow =
        context.settingsDataStore.data.map { preferences ->
            AudioStreamQualityPreference.valueOf(
                preferences[SettingsKeys.DOWNLOAD_QUALITY]
                    ?: AudioStreamQualityPreference.MEDIUM.name
            )
        }

    suspend fun setDownloadQuality(
        preference: AudioStreamQualityPreference
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsKeys.DOWNLOAD_QUALITY] =
                preference.name
        }
    }

    val profileUrlFlow: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.PROFILE_URL]
        }

    val userNameFlow: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.USER_NAME]
        }

    suspend fun saveProfileUrl(url: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.PROFILE_URL] = url
        }
    }

    suspend fun saveUserName(name: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.USER_NAME] = name
        }
    }

    suspend fun clearProfile() {
        context.settingsDataStore.edit { prefs ->
            prefs.remove(SettingsKeys.PROFILE_URL)
            prefs.remove(SettingsKeys.USER_NAME)
        }
    }
}

private lateinit var googleSignInManager: GoogleSignInManager

data class UpdateInfo(
    val message: String,
    val latestVersion: String,
    val currentVersion: String,
    val downloadUrl: String,
    val expectedSizeInBytes: Long
)

@AndroidEntryPoint
class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleSignInManager = GoogleSignInManager(this)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                Setting_Activity()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Setting_Activity(
    recentlyPlayedViewModel: RecentlyPlayedViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showLogOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    val (backInteraction, backScale) = pressScale()
    val (updateInteraction, updateScale) = pressScale()
    val (streamInteraction, streamScale) = pressScale()
    val (downloadInteraction, downloadScale) = pressScale()
    val (resetInteraction, resetScale) = pressScale()
    val (deleteInteraction, deleteScale) = pressScale()
    val (logoutInteraction, logoutScale) = pressScale()
    val (githubInteraction, githubScale) = pressScale()
    val (androidInteraction, androidScale) = pressScale()

    var streamingExpanded by remember { mutableStateOf(false) }
    var downloadExpanded by remember { mutableStateOf(false) }

    val selectedStreamingQuality by settingsViewModel.streamQuality.collectAsState()
    val selectedDownloadQuality by settingsViewModel.downloadQuality.collectAsState()
    val qualities = AudioStreamQualityPreference.entries

    val versionName = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
            packageInfo.versionName
        } else {
            @Suppress("DEPRECATION")
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        }
    } catch (_: Exception) {
        "1.0"
    }

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)).
        nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                width = 1.5.dp,
                                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable(
                                interactionSource = backInteraction,
                                indication = null
                            ) {
                                activity?.finish()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_icon),
                            contentDescription = "Back Icon",
                            tint = colorResource(R.color.primary_text_color),
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer {
                                    scaleX = backScale
                                    scaleY = backScale
                                }
                        )
                    }
                },
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontFamily = fonts,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color),
                        lineHeight = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.background_color),
                    scrolledContainerColor = colorResource(R.color.background_color)
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color(0xFF2C2C2C),
                            spotColor = Color(0xFF2C2C2C)
                        ),
                    containerColor = Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(when {
                            data.visuals.message.contains("Reset") -> R.drawable.delete_icon
                            data.visuals.message.contains("Update") ||
                                 data.visuals.message.contains("update") -> R.drawable.update_icon
                            else -> {
                                R.drawable.alert_icon
                            }
                        } ), contentDescription = "Icons",
                            tint = colorResource(R.color.theme_color), modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = data.visuals.message,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 13.sp,
                            color = colorResource(R.color.off_white)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val context = LocalContext.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.background_color))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 15.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFEFEFE))
            ) {
                SettingsItem(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    icon = R.drawable.update_icon,
                    title = "Updates",
                    subtitle = "Check for New Updates",
                    scale = updateScale,
                    interactionSource = updateInteraction,
                    onClick = {
                        isCheckingUpdate = true

                        scope.launch {
                            snackBarHostState.showSnackbar(
                                message = "Checking for update",
                                duration = SnackbarDuration.Short
                            )
                        }

                        checkForUpdate(
                            context,
                            onShowMessage = { message ->
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        ) { info ->
                            isCheckingUpdate = false

                            val intent = Intent(context, UpdateAppActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                                putExtra("message", info.message)
                                putExtra("latestVersion", info.latestVersion)
                                putExtra("currentVersion", info.currentVersion)
                                putExtra("downloadUrl", info.downloadUrl)
                                putExtra("expectedSizeInBytes", info.expectedSizeInBytes)
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.music_icon),
                        contentDescription = "Music Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = streamScale
                                scaleY = streamScale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Streaming Quality",
                            fontSize = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Quality of audio files streamed from online sources",
                            fontSize = 13.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.clickable(
                                interactionSource = streamInteraction,
                                indication = null
                            ) {
                                streamingExpanded = true
                            }
                        ) {
                            Text(
                                text = selectedStreamingQuality.label,
                                fontSize = 14.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                lineHeight = 16.sp,
                                color = colorResource(R.color.theme_color)
                            )

                            DropdownMenu(
                                modifier = Modifier.wrapContentWidth(),
                                containerColor = Color(0xFF3a3a3a),
                                expanded = streamingExpanded,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                onDismissRequest = { streamingExpanded = false }
                            ) {
                                qualities.forEach { quality ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = quality.label,
                                                fontSize = 14.sp,
                                                lineHeight = 16.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color =
                                                    if (quality == selectedStreamingQuality)
                                                        colorResource(R.color.theme_color)
                                                    else
                                                        colorResource(R.color.background_color)
                                            )
                                        },

                                        onClick = {
                                            streamingExpanded = false
                                            settingsViewModel.setStreamQuality(quality)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.download_icon),
                        contentDescription = "Download Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = downloadScale
                                scaleY = downloadScale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Download Quality",
                            fontSize = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Quality of audio files saved for offline listening",
                            fontSize = 13.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.clickable(
                                interactionSource = downloadInteraction,
                                indication = null
                            ) {
                                downloadExpanded = true
                            }
                        ) {
                            Text(
                                text = selectedDownloadQuality.label,
                                fontSize = 14.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                lineHeight = 16.sp,
                                color = colorResource(R.color.theme_color)
                            )

                            DropdownMenu(
                                modifier = Modifier.wrapContentWidth(),
                                containerColor = Color(0xFF3a3a3a),
                                expanded = downloadExpanded,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                onDismissRequest = { downloadExpanded = false }
                            ) {
                                qualities.forEach { quality ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = quality.label,
                                                fontSize = 14.sp,
                                                lineHeight = 16.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color =
                                                    if (quality == selectedDownloadQuality)
                                                        colorResource(R.color.theme_color)
                                                    else
                                                        colorResource(R.color.background_color)
                                            )
                                        },

                                        onClick = {
                                            downloadExpanded = false
                                            settingsViewModel.setDownloadQuality(quality)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                SettingsItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    icon = R.drawable.delete_icon,
                    title = "Reset waveX App",
                    subtitle = "Clear all your data and reset the app to its default state",
                    scale = resetScale,
                    interactionSource = resetInteraction,
                    onClick = {
                        showResetDialog = true
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                SettingsItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    icon = R.drawable.delete_account_icon,
                    title = "Delete Account",
                    subtitle = "Permanently delete your account from the \nwaveX app",
                    scale = deleteScale,
                    interactionSource = deleteInteraction,
                    onClick = {
                        showDeleteDialog = true
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                SettingsItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    icon = R.drawable.logout_icon,
                    title = "Log Out",
                    subtitle = "Sign out of your WaveX account",
                    scale = logoutScale,
                    interactionSource = logoutInteraction,
                    onClick = {
                        showLogOutDialog = true
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                SettingsItem(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    icon = R.drawable.github_icon,
                    title = "Git Hub",
                    subtitle = "For more information about waveX app",
                    scale = githubScale,
                    interactionSource = githubInteraction,
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/harshstr14".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
            }

            when {
                showLogOutDialog -> {
                    IOSStyleBottomDialog(
                        title = "Log Out",
                        message = "Are you sure you want to log out? You will need to log in again.",
                        confirmText = "Log Out",
                        icon = R.drawable.logout_icon,
                        onConfirm = {
                            googleSignInManager.signOut {
                                Toast.makeText(context, "Signed out!", Toast.LENGTH_SHORT).show()
                            }

                            try {
                                val serviceIntent = Intent(context, MusicPlayerService::class.java)
                                context.stopService(serviceIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            val intent = Intent(context, SignIn::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            activity?.finish()
                            showLogOutDialog = false
                        },
                        onDismiss = {
                            showLogOutDialog = false
                        }
                    )
                }

                showDeleteDialog -> {
                    IOSStyleBottomDialog(
                        title = "Delete Account",
                        message = "Deleting your account will remove all your data. Do you really want to proceed?",
                        confirmText = "Delete",
                        icon = R.drawable.delete_account_icon,
                        onConfirm = {
                            deleteAccount(
                                context,
                                onCacheClear = {
                                    recentlyPlayedViewModel.clearRecentlyPlayed()
                                },
                                onProfileClear = {
                                    settingsViewModel.clearProfile()
                                },
                                onDone = {
                                    showDeleteDialog = false
                                }
                            )
                        },
                        onDismiss = {
                            showDeleteDialog = false
                        }
                    )
                }

                showResetDialog -> {
                    IOSStyleBottomDialog(
                        title = "Reset waveX App",
                        message = "Are you sure you want to clear all app data? This will reset the app completely.",
                        icon = R.drawable.delete_icon,
                        confirmText = "Reset",
                        onConfirm = {
                            clearAppCache (
                                context,
                                onDone = { showResetDialog = false },
                                onShowMessage = { message ->
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = message,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                onReloadProfile = { uid ->
                                    profileViewModel.refreshUserData(uid)
                                },
                                onProfileClear = {
                                    settingsViewModel.clearProfile()
                                },
                                onCacheClear = {
                                    recentlyPlayedViewModel.clearRecentlyPlayed()
                                }
                            )
                        },
                        onDismiss = {
                            showResetDialog = false
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .background(Color.Unspecified),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.android_icon),
                    contentDescription = "Android Icon",
                    tint = colorResource(R.color.theme_color),
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = androidScale
                            scaleY = androidScale
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "App Version $versionName",
                    fontSize = 12.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

private fun clearAppCache(
    context: Context, onDone: () -> Unit,
    onShowMessage: (String) -> Unit,
    onCacheClear: () -> Unit,
    onProfileClear: suspend () -> Unit,
    onReloadProfile: ((String) -> Unit)? = null
) {
    try {
        val cacheDir = context.cacheDir
        val userID = FirebaseAuth.getInstance().currentUser?.uid

        val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
            .child(userID!!).child("Favourites")

        if (deleteDir(cacheDir)) {
            userID.let {
                favouriteReference.removeValue()
            }
            try {
                val serviceIntent = Intent(context, MusicPlayerService::class.java)
                context.stopService(serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            CoroutineScope(Dispatchers.IO).launch {
                onCacheClear()
                onProfileClear()

                userID.let { onReloadProfile?.invoke(it) }
            }

            onShowMessage("Reset Successfully")
            onDone()
        } else {
            onShowMessage("Failed to reset,try again")
            onDone()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onShowMessage("Error occurred during reset")
        onDone()
    }
}

private fun deleteDir(dir: File?): Boolean {
    if (dir != null && dir.isDirectory) {
        val children = dir.list()
        if (children != null) {
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) return false
            }
        }
    }
    return dir?.delete() ?: false
}

private fun deleteAccount(
    context: Context,
    onDone: () -> Unit,
    onProfileClear: suspend () -> Unit,
    onCacheClear : () -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
        Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
        onDone()
        return
    }

    val userReference = FirebaseDatabase.getInstance().getReference("Users").child(uid)
    userReference.removeValue().addOnSuccessListener {

        googleSignInManager.signOut { }

        FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                task.exception?.printStackTrace()
            }
        }

        try {
            val serviceIntent = Intent(context, MusicPlayerService::class.java)
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        CoroutineScope(Dispatchers.IO).launch {
            onCacheClear()
            onProfileClear()
        }

        val intent = Intent(context, SignIn::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        if (context is Activity) context.finish()

        Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show()
        onDone()

    }.addOnFailureListener {
        Toast.makeText(context, "Failed to delete account, try again", Toast.LENGTH_SHORT).show()
        onDone()
    }
}

@Composable
fun IOSStyleBottomDialog(
    title: String,
    message: String,
    icon: Int = R.drawable.alert_icon,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(R.color.off_white),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = colorResource(R.color.theme_color).copy(alpha = 0.10f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = colorResource(R.color.theme_color)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = title,
                        fontFamily = fonts,
                        fontSize = 18.sp, lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = message,
                        fontFamily = fonts,
                        fontSize = 13.sp, lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.secondary_text_color)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
                                .clickable { onDismiss() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dismissText,
                                color = colorResource(R.color.secondary_text_color),
                                fontSize = 15.sp, lineHeight = 15.sp,
                                fontFamily = fonts, fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(colorResource(R.color.theme_color))
                                .clickable { onConfirm() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmText,
                                fontSize = 15.sp, lineHeight = 15.sp,
                                fontFamily = fonts, fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.background_color)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    modifier: Modifier = Modifier,
    icon: Int,
    title: String,
    subtitle: String,
    scale: Float,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = title,
            tint = colorResource(R.color.theme_color),
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                color = colorResource(R.color.secondary_text_color),
                lineHeight = 16.sp
            )
        }
    }
}

fun checkForUpdate(
    context: Context,
    onShowMessage: (String) -> Unit,
    onResult: (UpdateInfo) -> Unit
) {
    val remoteConfig = Firebase.remoteConfig
    val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 0
    }
    remoteConfig.setConfigSettingsAsync(configSettings)
    remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

    remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val latestVersion = remoteConfig.getString("latest_version")
            val message = remoteConfig.getString("update_message")
            val downloadUrl = remoteConfig.getString("download_url")
            val expectedSizeInBytes = remoteConfig.getLong("expected_size_in_bytes")

            val currentVersion = getCurrentVersion(context)

            if (isNewVersionAvailable(currentVersion, latestVersion)) {
                onResult(
                    UpdateInfo(
                        message = message,
                        latestVersion = latestVersion,
                        currentVersion = currentVersion,
                        downloadUrl = downloadUrl,
                        expectedSizeInBytes = expectedSizeInBytes
                    )
                )
            } else {
                onShowMessage("Update not available")
            }
        } else {
            onShowMessage("Update not available")
        }
    }.addOnFailureListener {
        onShowMessage("Something went wrong,try again")
    }
}

private fun isNewVersionAvailable(current: String, latest: String): Boolean {
    val currentParts = current.split(".")
    val latestParts = latest.split(".")

    for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
        val c = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
        val l = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
        if (l > c) return true
        if (l < c) return false
    }
    return false
}

private fun getCurrentVersion(context: Context): String {
    return try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName ?: "1.0.0"
    } catch (_: Exception) {
        "1.0.0"
    }
}

@Composable
private fun pressScale(
    pressedScale: Float = 1.15f
): Pair<MutableInteractionSource, Float> {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PressScale"
    )

    return interactionSource to scale
}

@Preview(showBackground = true)
@Composable
private fun SettingActivityPreview() {
    WaveXTheme {
        Setting_Activity()
    }
}