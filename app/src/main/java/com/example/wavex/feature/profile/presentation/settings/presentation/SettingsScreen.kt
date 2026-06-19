package com.example.wavex.feature.profile.presentation.settings.presentation

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.core.net.toUri
import com.example.wavex.R
import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.core.service.MusicPlayerService
import com.example.wavex.feature.auth.presentation.signin.SignInActivity
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.pressScale
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onClearRecentlyPlayed: () -> Unit,
    onSetStreamQuality: (AudioStreamQualityPreference) -> Unit,
    onSetDownloadQuality: (AudioStreamQualityPreference) -> Unit,
    selectedStreamingQuality: AudioStreamQualityPreference,
    selectedDownloadQuality: AudioStreamQualityPreference,
    onClearProfile: () -> Unit,
    onSignOut: () -> Unit,
    onRefreshUser: () -> Unit
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
                        .padding(top = 10.dp)
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

//                        checkForUpdate(
//                            context,
//                            onShowMessage = { message ->
//                                scope.launch {
//                                    snackBarHostState.showSnackbar(
//                                        message = message,
//                                        duration = SnackbarDuration.Short
//                                    )
//                                }
//                            }
//                        ) { info ->
//                            isCheckingUpdate = false
//
//                            val intent = Intent(context, UpdateAppActivity::class.java).apply {
//                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//
//                                putExtra("message", info.message)
//                                putExtra("latestVersion", info.latestVersion)
//                                putExtra("currentVersion", info.currentVersion)
//                                putExtra("downloadUrl", info.downloadUrl)
//                                putExtra("expectedSizeInBytes", info.expectedSizeInBytes)
//                            }
//                            context.startActivity(intent)
//                        }
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
                                        modifier = Modifier.height(38.dp),
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
                                            onSetStreamQuality(quality)
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
                                qualities.forEachIndexed { _, quality ->
                                    DropdownMenuItem(
                                        modifier = Modifier.height(38.dp),
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
                                            onSetDownloadQuality(quality)
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
                        .padding(bottom = 10.dp)
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
                            onSignOut()
                            Toast.makeText(context, "Signed out!", Toast.LENGTH_SHORT).show()

                            try {
                                val serviceIntent = Intent(context, MusicPlayerService::class.java)
                                context.stopService(serviceIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            val intent = Intent(context, SignInActivity::class.java)
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
                                    onClearRecentlyPlayed()
                                },
                                onProfileClear = {
                                    onClearProfile()
                                },
                                onDone = {
                                    showDeleteDialog = false
                                },
                                onSignOut = {
                                    onSignOut()
                                    Toast.makeText(context, "Signed out!", Toast.LENGTH_SHORT).show()
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
                                onReloadProfile = {
                                    onRefreshUser()
                                },
                                onProfileClear = {
                                    onClearProfile()
                                },
                                onCacheClear = {
                                   onClearRecentlyPlayed()
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

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    WaveXTheme {
        SettingsScreen(
            onRefreshUser = {},
            onSignOut = {},
            onClearProfile = {},
            onSetStreamQuality = {},
            onSetDownloadQuality = {},
            onClearRecentlyPlayed = {},
            selectedStreamingQuality = AudioStreamQualityPreference.HIGH,
            selectedDownloadQuality = AudioStreamQualityPreference.HIGH,
        )
    }
}