package com.example.wavex.feature.profile.presentation.settings.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.wavex.R
import com.example.wavex.core.service.MusicPlayerService
import com.example.wavex.feature.auth.data.GoogleSignInManager
import com.example.wavex.feature.auth.presentation.signin.SignInActivity
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.home.presentation.HomeViewModel
import com.example.wavex.feature.profile.presentation.ProfileViewModel
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    private val recentlyPlayedViewModel: HomeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val googleSignInManager = GoogleSignInManager(this)

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
                SettingsScreen(
                    onSignOut = {
                        lifecycleScope.launch {
                            googleSignInManager.signOut()
                        }
                    },
                    onRefreshUser = {
                        profileViewModel.refreshUserData()
                    },
                    onClearRecentlyPlayed = {
                        recentlyPlayedViewModel.clearRecentlyPlayed()
                    },
                    onSetStreamQuality = { quality ->
                        settingsViewModel.setStreamQuality(quality)
                    },
                    onSetDownloadQuality = { quality ->
                        settingsViewModel.setDownloadQuality(quality)
                    },
                    onClearProfile = {
                        settingsViewModel.clearProfile()
                    },
                    selectedStreamingQuality = settingsViewModel.streamQuality.collectAsStateWithLifecycle().value,
                    selectedDownloadQuality = settingsViewModel.downloadQuality.collectAsStateWithLifecycle().value
                )
            }
        }
    }
}

fun clearAppCache(
    context: Context, onDone: () -> Unit,
    onShowMessage: (String) -> Unit,
    onCacheClear: () -> Unit,
    onProfileClear: suspend () -> Unit,
    onReloadProfile: () -> Unit
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
                onReloadProfile()
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

fun deleteDir(dir: File?): Boolean {
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

fun deleteAccount(
    context: Context,
    onDone: () -> Unit,
    onProfileClear: suspend () -> Unit,
    onCacheClear : () -> Unit,
    onSignOut: () -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
        Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
        onDone()
        return
    }

    val userReference = FirebaseDatabase.getInstance().getReference("Users").child(uid)
    userReference.removeValue().addOnSuccessListener {

        onSignOut()

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

        val intent = Intent(context, SignInActivity::class.java)
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

//fun checkForUpdate(
//    context: Context,
//    onShowMessage: (String) -> Unit,
//    onResult: (UpdateInfo) -> Unit
//) {
//    val remoteConfig = Firebase.remoteConfig
//    val configSettings = remoteConfigSettings {
//        minimumFetchIntervalInSeconds = 0
//    }
//    remoteConfig.setConfigSettingsAsync(configSettings)
//    remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
//
//    remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
//        if (task.isSuccessful) {
//            val latestVersion = remoteConfig.getString("latest_version")
//            val message = remoteConfig.getString("update_message")
//            val downloadUrl = remoteConfig.getString("download_url")
//            val expectedSizeInBytes = remoteConfig.getLong("expected_size_in_bytes")
//
//            val currentVersion = getCurrentVersion(context)
//
//            if (isNewVersionAvailable(currentVersion, latestVersion)) {
//                onResult(
//                    UpdateInfo(
//                        message = message,
//                        latestVersion = latestVersion,
//                        currentVersion = currentVersion,
//                        downloadUrl = downloadUrl,
//                        expectedSizeInBytes = expectedSizeInBytes
//                    )
//                )
//            } else {
//                onShowMessage("Update not available")
//            }
//        } else {
//            onShowMessage("Update not available")
//        }
//    }.addOnFailureListener {
//        onShowMessage("Something went wrong,try again")
//    }
//}

//fun isNewVersionAvailable(current: String, latest: String): Boolean {
//    val currentParts = current.split(".")
//    val latestParts = latest.split(".")
//
//    for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
//        val c = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
//        val l = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
//        if (l > c) return true
//        if (l < c) return false
//    }
//    return false
//}
//
//fun getCurrentVersion(context: Context): String {
//    return try {
//        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            context.packageManager.getPackageInfo(
//                context.packageName,
//                PackageManager.PackageInfoFlags.of(0)
//            )
//        } else {
//            @Suppress("DEPRECATION")
//            context.packageManager.getPackageInfo(context.packageName, 0)
//        }
//        packageInfo.versionName ?: "1.0.0"
//    } catch (_: Exception) {
//        "1.0.0"
//    }
//}

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
                        fontSize = 14.sp, lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
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
                                .background(Color(0xFFe4e4e4))
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