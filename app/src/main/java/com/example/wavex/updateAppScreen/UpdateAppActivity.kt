package com.example.wavex.updateAppScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.wavex.R
import com.example.wavex.fonts
import com.example.wavex.libraryScreen.pressScale
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch
import java.io.File

class UpdateAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        val message = intent.getStringExtra("message")
        val latestVersion = intent.getStringExtra("latestVersion")
        val currentVersion = intent.getStringExtra("currentVersion")
        val downloadUrl = intent.getStringExtra("downloadUrl")
        val expectedSizeInBytes = intent.getLongExtra("expectedSizeInBytes", 0)
        Log.d("size", "$expectedSizeInBytes")

        setContent {
            WaveXTheme {
                Update_App_Activity(
                    message, downloadUrl, currentVersion,
                    latestVersion, expectedSizeInBytes
                )
            }
        }
    }
}

fun saveDownloadedVersion(context: Context, version: String) {
    val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
    prefs.edit { putString("downloaded_version", version) }
}

fun getDownloadedVersion(context: Context): String? {
    val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
    return prefs.getString("downloaded_version", null)
}

fun clearDownloadedVersion(context: Context) {
    val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
    prefs.edit { remove("downloaded_version") }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Update_App_Activity(
    message: String? = null,
    downloadUrl: String? = null,
    currentVersion: String? = null,
    latestVersion: String? = null,
    expectedSizeInBytes: Long
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val (backInteraction, backScale) = pressScale()

    var downloadedVersion by remember { mutableStateOf<String?>(null) }
    var isApkExists by remember { mutableStateOf(false) }

    val workManager = WorkManager.getInstance(context)

    fun pauseDownload() {
        workManager.cancelUniqueWork("app_update_download")
    }

    fun resumeDownload() {
        if (downloadUrl != null) {
            val request = OneTimeWorkRequestBuilder<AppDownloadWorker>()
                .setInputData(
                    workDataOf(
                        "url" to downloadUrl,
                        "version" to latestVersion,
                        "expectedSizeInBytes" to expectedSizeInBytes.toString().toLong()
                    )
                )
                .build()

            workManager.enqueueUniqueWork(
                "app_update_download",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    fun checkApkState() {
        val file = File(context.getExternalFilesDir(null), "waveX.apk")
        isApkExists = file.exists() && file.length() > 0
    }

    val workInfo = workManager
        .getWorkInfosForUniqueWorkLiveData("app_update_download")
        .observeAsState()

    val currentWork = workInfo.value?.firstOrNull()

    val isDownloading = currentWork?.state == WorkInfo.State.RUNNING
    val isCompleted = currentWork?.state == WorkInfo.State.SUCCEEDED
    val isFailed = currentWork?.state == WorkInfo.State.FAILED

    val progress = currentWork?.progress?.getInt("progress", 0) ?: 0

    LaunchedEffect(isFailed) {
        if (isFailed) {
            scope.launch {
                snackBarHostState.showSnackbar("Download failed • Tap to retry")
            }
        }
    }

    Log.d("Version","$downloadedVersion $latestVersion")

    val file = File(
        context.getExternalFilesDir(null),
        "waveX.apk"
    )

    LaunchedEffect(downloadedVersion, latestVersion) {
        if (file.exists() && !downloadedVersion.isNullOrEmpty() && downloadedVersion != latestVersion) {
            Log.d("APK_DELETE", "Deleting APK → version=$downloadedVersion latest=$latestVersion size=${file.length()}")
            file.delete()
            clearDownloadedVersion(context)

            checkApkState()
            downloadedVersion = null
        }
    }

    var startAnimation by remember { mutableStateOf(false) }

    val shadowBlur by animateFloatAsState(
        targetValue = if (startAnimation) 50f else 20f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowBlur"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.35f else 0.4f,
        animationSpec = tween(900),
        label = "glowAlpha"
    )

    val glowScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "glowScale"
    )

    var glowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            downloadedVersion = getDownloadedVersion(context)
            checkApkState()

            if (!file.exists() || file.length() == 0L) {
                return@LaunchedEffect
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            "Allow 'Install unknown apps' to continue"
                        )
                    }

                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                    return@LaunchedEffect
                }
            }

            installApk(context)
        }
    }

    LaunchedEffect(isApkExists) {
        if (!isApkExists) {
            workManager.cancelUniqueWork("app_update_download")
        }
    }

    LaunchedEffect(Unit) {
        val file = File(context.getExternalFilesDir(null), "waveX.apk")

        val isValidFile = file.exists() && file.length() >= expectedSizeInBytes

        if (!isValidFile) {
            clearDownloadedVersion(context)
            downloadedVersion = null
        } else {
            downloadedVersion = getDownloadedVersion(context)
        }

        isApkExists = isValidFile

        startAnimation = true

        extractDominantColor(context, R.drawable.logo) {
            glowColor = it
        }
    }

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)),
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 68.dp)
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
                            data.visuals.message.contains("Update") -> R.drawable.update_icon
                            data.visuals.message.contains("Download") -> R.drawable.download_icon
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
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.background_color))
        ) {
            val(backButton, updateButton, text1, text2, text3, text4, divider, logo) = createRefs()

            Box(
                modifier = Modifier.constrainAs(backButton) {
                    top.linkTo(parent.top, margin = 15.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                }.size(36.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.5.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    ).clickable(
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
                    modifier = Modifier.size(20.dp)
                        .graphicsLayer {
                            scaleX = backScale
                            scaleY = backScale
                        }
                )
            }

            Image(
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(createRef()) {
                        top.linkTo(parent.top)
                        bottom.linkTo(logo.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .size(350.dp)
                    .graphicsLayer {
                        rotationZ = -12f
                        alpha = 0.4f
                    }
            )

            Box(
                modifier = Modifier.constrainAs(logo) {
                    bottom.linkTo(text1.top, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = glowScale
                            scaleY = glowScale
                        }
                        .drawBehind {
                            val safeBlur = shadowBlur.coerceAtLeast(0.1f)
                            val cornerRadius = 20.dp.toPx()

                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    color = glowColor.copy(alpha = glowAlpha)
                                    asFrameworkPaint().apply {
                                        isAntiAlias = true
                                        maskFilter = android.graphics.BlurMaskFilter(
                                            safeBlur,
                                            android.graphics.BlurMaskFilter.Blur.NORMAL
                                        )
                                    }
                                }

                                canvas.drawRoundRect(
                                    8f,
                                    8f,
                                    size.width - 8f,
                                    size.height - 8f,
                                    cornerRadius,
                                    cornerRadius,
                                    paint
                                )
                            }
                        }
                )

                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Text(
                modifier = Modifier.constrainAs(text1) {
                    bottom.linkTo(text2.top, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 30.dp),
                text = "NEW UPDATE IS AVAILABLE",
                fontSize = 14.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.secondary_text_color),
                lineHeight = 16.sp
            )

            Text(
                modifier = Modifier.constrainAs(text2) {
                    bottom.linkTo(text3.top, margin = 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 30.dp),
                text = "Update your application to the latest version",
                fontSize = 20.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.primary_text_color),
                lineHeight = 24.sp
            )

            Text(
                modifier = Modifier.constrainAs(text3) {
                    bottom.linkTo(updateButton.top, margin = 55.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 30.dp),
                text = message ?: "A new version of Wavex is now available! Update your app to enjoy the latest features and improvements.",
                fontSize = 13.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.secondary_text_color),
                lineHeight = 16.sp
            )

            Text(
                modifier = Modifier.constrainAs(text4) {
                    bottom.linkTo(divider.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 30.dp),
                text = "version $currentVersion - $latestVersion",
                fontSize = 12.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.secondary_text_color),
                lineHeight = 16.sp
            )

            HorizontalDivider(
                modifier = Modifier.constrainAs(divider) {
                    bottom.linkTo(updateButton.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(vertical = 8.dp, horizontal = 2.dp),
                thickness = 1.2.dp,
                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.4f)
            )

            val isApkReady =
                file.exists() &&
                        file.length() >= expectedSizeInBytes &&
                        downloadedVersion == latestVersion

            Box(
                modifier = Modifier.constrainAs(updateButton) {
                    bottom.linkTo(parent.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.fillMaxWidth()
                    .padding(horizontal = 20.dp).height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colorResource(R.color.theme_color))
                    .clickable (
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) {
                        when {
                            currentWork == null -> {
                                if (downloadUrl != null) {
                                    val request = OneTimeWorkRequestBuilder<AppDownloadWorker>()
                                        .setInputData(
                                            workDataOf(
                                                "url" to downloadUrl,
                                                "version" to latestVersion,
                                                "expectedSizeInBytes" to expectedSizeInBytes.toString().toLong()
                                            )
                                        )
                                        .build()

                                    workManager.enqueueUniqueWork(
                                        "app_update_download",
                                        ExistingWorkPolicy.KEEP,
                                        request
                                    )

                                    scope.launch {
                                        snackBarHostState.showSnackbar("Download Started")
                                    }
                                }
                            }

                            isDownloading -> {
                                pauseDownload()
                                scope.launch {
                                    snackBarHostState.showSnackbar("Download Paused")
                                }
                            }

                            isFailed -> {
                                resumeDownload()
                                scope.launch {
                                    snackBarHostState.showSnackbar("Retrying Download")
                                }
                            }

                            isApkReady -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    if (!context.packageManager.canRequestPackageInstalls()) {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "Allow 'Install unknown apps' to continue"
                                            )
                                        }

                                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                            data = "package:${context.packageName}".toUri()
                                        }
                                        context.startActivity(intent)

                                        return@clickable
                                    }
                                }

                                installApk(context)
                            }

                            else -> {
                                resumeDownload()
                                scope.launch {
                                    snackBarHostState.showSnackbar("Download Resumed")
                                }
                            }
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isApkReady -> "Install Now"
                        isDownloading -> "Downloading • $progress%"
                        isFailed -> "Retry Download"
                        currentWork != null && !isCompleted && file.exists() && file.length() < expectedSizeInBytes -> "Resume Download"
                        else -> "Update Now"
                    },
                    fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.off_white)
                )
            }
        }
    }
}

fun extractDominantColor(
    context: Context,
    @DrawableRes drawableRes: Int,
    onColorExtracted: (Color) -> Unit
) {
    val bitmap = BitmapFactory.decodeResource(context.resources, drawableRes)

    Palette.from(bitmap).generate { palette ->
        val dominant = palette?.getDominantColor(android.graphics.Color.GRAY)
        onColorExtracted(Color(dominant ?: android.graphics.Color.GRAY))
    }
}

fun installApk(context: Context) {
    val file = File(
        context.getExternalFilesDir(null),
        "waveX.apk"
    )

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    context.startActivity(intent)
}

@Preview(showSystemUi = true)
@Composable
fun UpdateAppActivityPreview() {
    WaveXTheme {

    }
}