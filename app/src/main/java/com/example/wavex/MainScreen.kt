package com.example.wavex

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.albumScreen.AlbumActivity
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.discoverScreen.DiscoverScreen
import com.example.wavex.downloadSong.viewmodel.DownloadViewModel
import com.example.wavex.downloadSong.viewmodel.DownloadViewModelFactory
import com.example.wavex.homeScreen.AppContainer
import com.example.wavex.homeScreen.HomeScreen
import com.example.wavex.homeScreen.ParallelDownloader
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.libraryScreen.LibraryScreen
import com.example.wavex.libraryScreen.pressScale
import com.example.wavex.navigation.BottomItem
import com.example.wavex.navigation.BottomNavRoute
import com.example.wavex.playerScreen.PlayerActivityScreen
import com.example.wavex.playlistScreen.PlaylistActivity
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
import com.example.wavex.profileScreen.settingScreen.checkForUpdate
import com.example.wavex.searchScreen.SearchScreen
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ServiceLocator
import com.example.wavex.ui.theme.WaveXTheme
import com.example.wavex.updateAppScreen.UpdateAppActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object HttpClientProvider {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}

private lateinit var apiUrl1: String
private lateinit var apiUrl2: String
private lateinit var apiUrl3: String

suspend fun requestWithFallback(endpoint: String): String =
    withContext(Dispatchers.IO) {
        val apis = listOf(apiUrl1, apiUrl2, apiUrl3)

        for (baseUrl in apis) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .get()
                    .build()

                val call = HttpClientProvider.client.newCall(request)

                coroutineContext.job.invokeOnCompletion {
                    call.cancel()
                }

                val response = call.execute()

                response.use {
                    if (it.isSuccessful) {
                        return@withContext it.body.string()
                    }

                    if (it.code in 500..599) {
                        Log.w("API", "Server error ${it.code} on $baseUrl, trying next...")
                        continue
                    }

                    if (it.code in 400..499) {
                        throw Exception("Client error ${it.code}")
                    }
                }
            } catch (e: Exception) {
                if (
                    e is SocketTimeoutException ||
                    e is ConnectException ||
                    e is UnknownHostException
                ) {
                    Log.w("API", "Network error on $baseUrl, trying next...")
                    continue
                } else {
                    throw e
                }
            }
        }

        throw Exception("All APIs failed")
    }

class MainScreen : ComponentActivity() {
    private var deepLinkType: String? = null
    private var deepLinkId: String? = null
    private var deepLinkUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleDeepLink(intent)

        apiUrl1 = BuildConfig.API_BASE_URL1
        apiUrl2 = BuildConfig.API_BASE_URL2
        apiUrl3 = BuildConfig.API_BASE_URL3

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        val downloadViewModel: DownloadViewModel by viewModels {
            DownloadViewModelFactory(AppContainer.downloadRepository)
        }

        setContent {
            WaveXTheme {
                Main_Screen(
                    downloadViewModel,
                    deepLinkType = deepLinkType,
                    deepLinkId = deepLinkId,
                    deepLinkUrl = deepLinkUrl
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return

        deepLinkUrl = uri.toString()

        val segments = uri.pathSegments
        if (segments.size >= 2) {
            deepLinkType = segments[0]
            deepLinkId = segments[1]
        }

        Log.d("DeepLink", "Type: $deepLinkType  Id: $deepLinkId")
    }
}

@Composable
fun Main_Screen(
    downloadViewModel: DownloadViewModel,
    deepLinkType: String?,
    deepLinkId: String?,
    deepLinkUrl: String?
) {
    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val downloadedIds by downloadViewModel
        .downloadedSongIds
        .collectAsState(initial = emptySet())

    var hasChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasChecked) {
            hasChecked = true

            checkForUpdate(
                context,
                onShowMessage = { message ->
                }
            ) { info ->

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
    }

    var handled by remember { mutableStateOf(false) }

    LaunchedEffect(deepLinkType, deepLinkId) {
        if (handled) return@LaunchedEffect
        if (deepLinkType == null || deepLinkId == null) return@LaunchedEffect

        handled = true

        when (deepLinkType) {

            "song" -> {
                val intent = Intent(context, PlayerActivityScreen::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

                context.startActivity(intent)
            }

            "album" -> {
                val intent = Intent(context, AlbumActivity::class.java).apply {
                    putExtra("album_id", deepLinkId)
                    putExtra("album_imageUrl", "")
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)
            }

            "playlist" -> {
                val intent = Intent(context, PlaylistActivity::class.java).apply {
                    putExtra("playlist_id", deepLinkId)
                    putExtra("playlist_imageUrl", "")
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)
            }

            "artist" -> {
                val intent = Intent(context, ArtistActivity::class.java).apply {
                    putExtra("artist_id", deepLinkId)
                    putExtra("artist_imageUrl", "")
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)
            }

            "playlists" -> {
                navController.navigate(
                    "library?openSheet=wavex&url=${deepLinkUrl}"
                ) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    var showSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var selectedPlaylist by remember { mutableStateOf<List<SongItem>>(emptyList()) }

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()

    val musicService = ServiceLocator.musicService

    val playlist by musicService?.playlistFlow?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val currentIndex by musicService?.currentIndexFlow?.collectAsState(initial = -1)
        ?: remember { mutableIntStateOf(-1) }

    val qualityIndex = musicService?.downloadQualityIndex

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    Scaffold(
        containerColor = colorResource(id = R.color.background_color),
        bottomBar = {
            Column {
                val progress by musicService?.progress?.collectAsState(initial = 0)
                    ?: remember { mutableIntStateOf(0) }

                val duration by musicService?.duration?.collectAsState(initial = 0)
                    ?: remember { mutableIntStateOf(0) }

                val isBuffering by musicService?.isBuffering?.collectAsState(initial = false)
                    ?: remember { mutableStateOf(false)}

                currentSong?.let { song ->
                    MiniPlayer(
                        song = song,
                        isPlaying = isPlaying,
                        progress = if (duration > 0)
                            progress.toFloat() / duration.toFloat()
                        else 0f,
                        isBuffering = isBuffering,
                        onPlayPause = {
                            musicService?.togglePlayPause()
                        },
                        onClick = {
                            val activity = context as? Activity

                            val intent = Intent(context, PlayerActivityScreen::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }

                            context.startActivity(intent)

                            activity?.let {
                                if (Build.VERSION.SDK_INT >= 34) {
                                    it.overrideActivityTransition(
                                        Activity.OVERRIDE_TRANSITION_OPEN,
                                        R.anim.slide_up,
                                        R.anim.fade_out
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    it.overridePendingTransition(
                                        R.anim.slide_up,
                                        R.anim.fade_out
                                    )
                                }
                            }
                        },
                        onAddClick = {
                            selectedSong = song
                            selectedIndex = currentIndex
                            selectedPlaylist = playlist
                            showSheet = true
                        }
                    )
                }

                BottomNavBar(navController = navController)
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp).shadow(
                            elevation = 12.dp,
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
                        Icon(
                            painter = painterResource(
                                when {
                                    data.visuals.message.contains("Playlist") -> R.drawable.playlist_icon
                                    data.visuals.message.contains("name") -> R.drawable.user_icon
                                    data.visuals.message.contains("Phone") -> R.drawable.phone_icon
                                    data.visuals.message.contains("downloads") ||
                                            data.visuals.message.contains("Downloading") -> R.drawable.download_icon
                                    data.visuals.message.contains("downloaded") -> R.drawable.downloaded_icon
                                    data.visuals.message.contains("failed") -> R.drawable.alert_icon
                                    else -> {
                                        R.drawable.alert_icon
                                    }
                                }
                            ),
                            contentDescription = "Icons",
                            tint = colorResource(R.color.theme_color),
                            modifier = Modifier.size(24.dp)
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
        NavHost(
            navController = navController,
            startDestination = BottomNavRoute.Home.route,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),

            enterTransition = {
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(
                        durationMillis = 320,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(220)
                )
            },

            exitTransition = {
                scaleOut(
                    targetScale = 1.05f,
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = FastOutLinearInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(160)
                )
            },

            popEnterTransition = {
                scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(300)
                ) + fadeIn()
            },

            popExitTransition = {
                scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(220)
                ) + fadeOut()
            }
        ) {
            composable(BottomNavRoute.Home.route) {
                HomeScreen(
                    showSheet = showSheet,
                    onSongLongPress = { playlist, song, index ->
                        selectedSong = song
                        selectedIndex = index
                        selectedPlaylist = playlist
                        showSheet = true
                    }
                )  // ⬅ current Home UI
            }
            composable(BottomNavRoute.Discover.route) {
                DiscoverScreen(downloadViewModel = downloadViewModel, qualityIndex = qualityIndex,
                    navController = navController, snackBarHostState = snackBarHostState, showSheet = showSheet
                )
            }
            composable(BottomNavRoute.Search.route) {
                SearchScreen(downloadViewModel = downloadViewModel, qualityIndex = qualityIndex,
                    navController = navController, snackBarHostState = snackBarHostState, showSheet = showSheet
                )
            }
            composable(
                route = "library?openSheet={openSheet}&url={url}",
                arguments = listOf(
                    navArgument("openSheet") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("url") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->

                val openSheet = backStackEntry.arguments?.getString("openSheet")
                val url = backStackEntry.arguments?.getString("url")

                LibraryScreen(
                    navController = navController,
                    snackBarHostState = snackBarHostState,
                    showSheet = showSheet,
                    openSheet = openSheet,
                    initialUrl = url
                )
            }
        }
    }

    if (showSheet && selectedSong != null) {
        val song = selectedSong!!
        val isFavourite = likedSongs.contains(song.id)
        val isDownloaded = downloadedIds.contains(song.id)

        SongOptionsBottomSheet(
            song = song,
            isPlaying = isPlaying,
            isCurrentSong = currentSong?.id == song.id,
            onDismiss = {
                showSheet = false
                selectedSong = null
            },
            onPlayNow = {
                val isSameSong = currentSong?.id == song.id

                val intent = Intent(context, MusicPlayerService::class.java)

                if (isSameSong) {
                    intent.action = if (isPlaying) {
                        MusicPlayerService.ACTION_PAUSE
                    } else {
                        MusicPlayerService.ACTION_PLAY
                    }
                } else {
                    intent.action = MusicPlayerService.ACTION_PLAY_NEW
                    intent.putExtra("index", selectedIndex)

                    PlayerManager.currentPlaylist = selectedPlaylist.toMutableList()
                    PlayerManager.currentIndex = selectedIndex
                }

                ContextCompat.startForegroundService(context, intent)
            },
            isFavourite = isFavourite,
            isDownloaded = isDownloaded,
            onToggleFavourite = {
                likedViewModel.toggleLike(song)
            },
            onToggleDownload = { song ->
                val qualityIndex = musicService?.downloadQualityIndex
                val url = song.downloadUrl[qualityIndex ?: 4].url
                val state = ParallelDownloader.downloadStates[song.id]

                when {
                    isDownloaded -> {
                        downloadViewModel.deleteSong(song.id) { success, message -> }
                    }

                    state == ParallelDownloader.DownloadState.DOWNLOADING -> {
                        scope.launch {
                            snackBarHostState.showSnackbar("Already downloading")
                        }
                    }

                    state == ParallelDownloader.DownloadState.PAUSED -> {
                        val intent = Intent(context, MusicPlayerService::class.java).apply {
                            action = MusicPlayerService.ACTION_DOWNLOAD_RESUME
                            putExtra("url", url)
                            putExtra("fileName", song.name)
                            putExtra("songId", song.id)
                            putExtra("song", song)
                        }

                        ContextCompat.startForegroundService(context, intent)
                    }

                    state == ParallelDownloader.DownloadState.FAILED -> {
                        val intent = Intent(context, MusicPlayerService::class.java).apply {
                            action = MusicPlayerService.ACTION_DOWNLOAD_START
                            putExtra("url", url)
                            putExtra("fileName", song.name)
                            putExtra("songId", song.id)
                            putExtra("song", song)
                        }

                        ContextCompat.startForegroundService(context, intent)
                    }

                    else -> {
                        val intent = Intent(context, MusicPlayerService::class.java).apply {
                            action = MusicPlayerService.ACTION_DOWNLOAD_START
                            putExtra("url", url)
                            putExtra("fileName", song.name)
                            putExtra("songId", song.id)
                            putExtra("song", song)
                        }

                        ContextCompat.startForegroundService(context, intent)
                    }
                }
            }
        )
    }
}

@Composable
fun MiniPlayer(
    song: SongItem,
    isPlaying: Boolean,
    isBuffering: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val playInteractionSource = remember { MutableInteractionSource() }
    val isPressed by playInteractionSource.collectIsPressedAsState()
    val (addInteraction, addScale) = pressScale()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShareScale"
    )

    val shadowColorButton by animateColorAsState(
        targetValue = if (isPlaying) Color(0xFF34A853) else Color(0xFF797979),
        animationSpec = tween(500),
        label = "shadowColor"
    )

    val shadowScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.2f else 1.2f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "shadowScale"
    )

    val shadowBlur by animateFloatAsState(
        targetValue = if (isPlaying) 55f else 55f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "shadowBlur"
    )

    val shadowAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.8f else 0.8f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "shadowAlpha"
    )

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading_animation)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .padding(start = 18.dp, end = 18.dp, bottom = 4.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF2C2C2C))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = song.image.getOrNull(2)?.url,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                val songName = htmlToText(song.name)

                Text(
                    text = songName,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.off_white),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val artistsList = song.artist
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") { it.name }
                    ?: "Unknown Artist"

                val artistsName = htmlToText(artistsList)

                Text(
                    text = artistsName,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.off_white).copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(46.dp)
                    .drawBehind {
                        val glowRadius = (size.minDimension / 3f) * shadowScale
                        val safeBlur = shadowBlur.coerceAtLeast(0.1f)

                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = shadowColorButton.copy(alpha = shadowAlpha)
                                asFrameworkPaint().apply {
                                    isAntiAlias = true

                                    maskFilter = if (shadowBlur > 0f) {
                                        android.graphics.BlurMaskFilter(
                                            safeBlur,
                                            android.graphics.BlurMaskFilter.Blur.NORMAL
                                        )
                                    } else {
                                        null
                                    }
                                }
                            }

                            canvas.drawCircle(
                                center,
                                glowRadius,
                                paint
                            )
                        }
                    }
                , contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(80.dp))
                        .background(
                            if (isPlaying) colorResource(R.color.theme_color)
                            else colorResource(R.color.secondary_text_color)
                        )
                        .clickable(
                            interactionSource = playInteractionSource,
                            indication = null
                        ) {
                            onPlayPause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RectangleShape)
                        ) {
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier
                                    .size(32.dp)
                                    .graphicsLayer {
                                        scaleX = 2f
                                        scaleY = 2f
                                    }
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(
                                if (isPlaying) R.drawable.notificationpausebutton
                                else R.drawable.notificationplaybutton
                            ),
                            contentDescription = "Play Icon",
                            tint = colorResource(R.color.background_color),
                            modifier = Modifier
                                .padding(start = if (isPlaying) 0.dp else 1.dp)
                                .size(18.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        )

                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                painter = painterResource(R.drawable.add_icon),
                contentDescription = "Add Icon",
                tint = colorResource(R.color.off_white),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 6.dp)
                    .size(28.dp)
                    .clickable(
                        interactionSource = addInteraction,
                        indication = null
                    ) {
                        onAddClick()
                    }
                    .graphicsLayer {
                        scaleX = addScale
                        scaleY = addScale
                    }
            )
        }

        val animatedProgress = remember { Animatable(0f) }

        LaunchedEffect(progress) {
            animatedProgress.animateTo(
                targetValue = progress.coerceIn(0f, 1f),
                animationSpec = tween(
                    durationMillis = 500,
                    easing = LinearEasing
                )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    colorResource(R.color.secondary_text_color).copy(alpha = 0.3f)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.value)
                    .background(colorResource(R.color.theme_color))
            )
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavController) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        BottomItem(
            BottomNavRoute.Home.route,
            "Home",
            R.drawable.home_filled,
            R.drawable.home_outline
        ),
        BottomItem(
            BottomNavRoute.Discover.route,
            "Discover",
            R.drawable.discover_filled,
            R.drawable.discover_outline
        ),
        BottomItem(
            BottomNavRoute.Search.route,
            "Search",
            R.drawable.search_filled,
            R.drawable.search_outline
        ),
        BottomItem(
            BottomNavRoute.Library.route,
            "Library",
            R.drawable.library_filled,
            R.drawable.library_outline
        )
    )

    Box(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, bottom = 8.dp)
            .navigationBarsPadding().height(68.dp).shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFF2C2C2C).copy(alpha = 0.2f),
                spotColor = Color(0xFF2C2C2C).copy(alpha = 0.4f)
            ).background(Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2C2C2C).copy(alpha = 0.98f),
                    Color(0xFF2C2C2C).copy(alpha = 1f)
                )
            ), shape = RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            items.forEach { item ->
                val selected = currentRoute?.startsWith(item.route) == true
                val animatedPadding by animateDpAsState(
                    targetValue = if (selected) 14.dp else 22.dp,
                    label = "paddingAnim"
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            brush = if (selected)
                                Brush.horizontalGradient(
                                    listOf(
                                        colorResource(R.color.off_white).copy(alpha = 0.2F),
                                        colorResource(R.color.off_white).copy(alpha = 0.2F)
                                    )
                                )
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(
                            horizontal = animatedPadding,
                            vertical = 9.dp
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (selected) item.filledIcon else item.outlineIcon
                        ),
                        contentDescription = item.label,
                        tint = if (selected) Color(0xFFF6F6F6) else Color(0xFF797979),
                        modifier = Modifier.size(24.dp)
                    )

                    Box(modifier = Modifier
                            .padding(start = 6.dp)
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 420,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            .clipToBounds()
                    ) {
                        if (selected) {
                            Text(
                                text = item.label,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = 1f
                                    scaleY = 1f
                                    alpha = 1f
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                },
                                color = Color(0xFFF6F6F6),
                                fontSize = 14.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Main_ScreenPreview() {
    WaveXTheme {

    }
}