package com.example.wavex.profileScreen.settingScreen

import android.app.Activity
import android.app.Application
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.example.wavex.R
import com.example.wavex.SignIn
import com.example.wavex.fonts
import com.example.wavex.googleAuthentication.GoogleSignInManager
import com.example.wavex.homeScreen.ProfilePrefs
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.viewModel.ProfileViewModel
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ServiceLocator
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.edit

private lateinit var googleSignInManager: GoogleSignInManager

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
fun Setting_Activity() {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val prefs = context.getSharedPreferences("player_settings", Context.MODE_PRIVATE)

    var showLogOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

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

    var selectedStreamingQuality by remember { mutableStateOf("High") }
    var selectedDownloadQuality by remember { mutableStateOf("High") }

    val qualities = listOf("Low", "Normal", "High")

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        val reference = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(uid)
            .child("streamingQuality")

        reference.get().addOnSuccessListener { snapshot ->
            val savedQuality = snapshot.getValue(String::class.java)
            if (savedQuality != null) {
                selectedStreamingQuality = savedQuality
            }
        }
    }

    LaunchedEffect(Unit) {
        val savedIndex = prefs.getInt("download_quality_index", 4)

        selectedDownloadQuality = when (savedIndex) {
            2 -> "Low"
            3 -> "Normal"
            4 -> "High"
            else -> "High"
        }
    }

    val musicService = ServiceLocator.musicService

    LaunchedEffect(musicService?.downloadQualityIndex) {
        Log.d("QualityIndex", "${musicService?.downloadQualityIndex}")
    }

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)),
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
                    .padding(horizontal = 18.dp, vertical = 25.dp)
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.background_color))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp)
                    .background(colorResource(R.color.background_color))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, end = 25.dp, top = 15.dp)
                        .clickable(
                            interactionSource = updateInteraction,
                            indication = null
                        ) {

                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.update_icon),
                        contentDescription = "Update Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = updateScale
                                scaleY = updateScale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Updates",
                            fontSize = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Check for New Updates",
                            fontSize = 12.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, top = 18.dp),
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

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Quality of audio files streamed from \nonline sources",
                            fontSize = 12.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f),
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
                                text = selectedStreamingQuality,
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
                                                text = quality,
                                                fontSize = 14.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color = if (quality == selectedStreamingQuality)
                                                    colorResource(R.color.theme_color)
                                                else colorResource(R.color.background_color)
                                            )
                                        },
                                        onClick = {
                                            selectedStreamingQuality = quality
                                            saveStreamingQualityToFirebase(quality)
                                            streamingExpanded = false

                                            val index = when (quality) {
                                                "Low" -> 2
                                                "Normal" -> 3
                                                "High" -> 4
                                                else -> 4
                                            }

                                            musicService?.setQuality(index)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, top = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.music_icon),
                        contentDescription = "Music Icon",
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

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Quality of audio files saved for offline\nlistening",
                            fontSize = 12.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f),
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
                                text = selectedDownloadQuality,
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
                                                text = quality,
                                                fontSize = 14.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color = if (quality == selectedDownloadQuality)
                                                    colorResource(R.color.theme_color)
                                                else colorResource(R.color.background_color)
                                            )
                                        },
                                        onClick = {
                                            selectedDownloadQuality = quality
                                            downloadExpanded = false

                                            val index = when (quality) {
                                                "Low" -> 2
                                                "Normal" -> 3
                                                "High" -> 4
                                                else -> 4
                                            }

                                            prefs.edit {
                                                    putInt("download_quality_index", index)
                                                }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, end = 25.dp, top = 18.dp)
                        .clickable(
                            interactionSource = resetInteraction,
                            indication = null
                        ) {
                            showResetDialog = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete_icon),
                        contentDescription = "Delete Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = resetScale
                                scaleY = resetScale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Reset waveX App",
                            fontSize = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Clear all your data and reset the app to its default state",
                            fontSize = 12.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, end = 25.dp, top = 18.dp)
                        .clickable(
                            interactionSource = deleteInteraction,
                            indication = null
                        ) {
                            showDeleteDialog = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete_account_icon),
                        contentDescription = "User Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = deleteScale
                                scaleY = deleteScale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Delete Account",
                            fontSize = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Permanently delete your account from the \nwaveX app",
                            fontSize = 12.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, end = 25.dp, top = 18.dp)
                        .clickable(
                            interactionSource = logoutInteraction,
                            indication = null
                        ) {
                            showLogOutDialog = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logout_icon),
                        contentDescription = "LogOut Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = logoutScale
                                scaleY = logoutScale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Log Out",
                            fontSize = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Sign out of your WaveX account",
                            fontSize = 12.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, end = 25.dp, top = 18.dp)
                        .clickable(
                            interactionSource = githubInteraction,
                            indication = null
                        ) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/harshstr14".toUri()
                            )
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.github_icon),
                        contentDescription = "GitHub Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = githubScale
                                scaleY = githubScale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Git Hub",
                            fontSize = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "For more information about waveX app",
                            fontSize = 12.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            when {
                showLogOutDialog -> {
                    ConfirmActionDialog(
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
                    ConfirmActionDialog(
                        title = "Delete Account",
                        message = "Deleting your account will remove all your data. Do you really want to proceed?",
                        confirmText = "Delete",
                        icon = R.drawable.delete_account_icon,
                        onConfirm = {
                            deleteAccount(context) {
                                showDeleteDialog = false
                            }
                        },
                        onDismiss = {
                            showDeleteDialog = false
                        }
                    )
                }

                showResetDialog -> {
                    ConfirmActionDialog(
                        title = "Reset waveX App",
                        message = "Are you sure you want to clear all app data? This will reset the app completely.",
                        confirmText = "Reset",
                        icon = R.drawable.delete_icon,
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
                                    val profileVM = ProfileViewModel(context.applicationContext as Application)
                                    profileVM.reloadProfileFromFirebase(uid)
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
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .background(colorResource(R.color.background_color)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.android_icon),
                    contentDescription = "Android Icon",
                    tint = colorResource(R.color.theme_color),
                    modifier = Modifier
                        .size(20.dp)
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

private fun saveStreamingQualityToFirebase(quality: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val reference = FirebaseDatabase.getInstance()
        .getReference("Users")
        .child(uid)
        .child("streamingQuality")

    reference.setValue(quality)
}

private fun clearAppCache(
    context: Context, onDone: () -> Unit,
    onShowMessage: (String) -> Unit,
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
                RecentlyPlayedManager.clear(context)
                ProfilePrefs.clear(context)

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

private fun deleteAccount(context: Context,onDone: () -> Unit) {
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
            RecentlyPlayedManager.clear(context)
            ProfilePrefs.clear(context)
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
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: Int? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorResource(R.color.off_white)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth().padding(20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = colorResource(R.color.theme_color),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = title,
                        fontFamily = fonts,
                        fontSize = 18.sp, lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                    thickness = 1.2.dp,
                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontFamily = fonts,
                    fontSize = 14.sp, lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dismissText,
                        color = colorResource(R.color.theme_color),
                        fontSize = 15.sp, lineHeight = 15.sp,
                        fontFamily = fonts, fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        modifier = Modifier.clickable { onDismiss() }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = confirmText,
                        fontSize = 15.sp, lineHeight = 15.sp,
                        fontFamily = fonts, fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.theme_color),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable { onConfirm() }
                    )
                }
            }
        }
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
fun SettingActivityPreview() {
    WaveXTheme {
        Setting_Activity()
    }
}