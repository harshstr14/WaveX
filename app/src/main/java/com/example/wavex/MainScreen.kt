package com.example.wavex

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.core.deeplink.DeepLink
import com.example.wavex.core.deeplink.DeepLinkEvent
import com.example.wavex.core.deeplink.DeepLinkManager
import com.example.wavex.core.deeplink.DeepLinkParser
import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.core.model.PlaylistData
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.service.MusicPlayerService
import com.example.wavex.core.service.ParallelDownloader
import com.example.wavex.core.service.ServiceLocator
import com.example.wavex.core.shared.LikedSongsViewModel
import com.example.wavex.core.util.DownloadQualitySelector
import com.example.wavex.feature.album.presentation.AlbumActivity
import com.example.wavex.feature.artist.presentation.ArtistActivity
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.discover.presentation.DiscoverScreen
import com.example.wavex.feature.discover.presentation.DiscoverViewModel
import com.example.wavex.feature.home.presentation.HomeScreen
import com.example.wavex.feature.home.presentation.HomeViewModel
import com.example.wavex.feature.home.presentation.PlayerManager
import com.example.wavex.feature.home.presentation.htmlToText
import com.example.wavex.feature.importplaylist.model.ImportState
import com.example.wavex.feature.importplaylist.presentation.ImportPlaylistViewModel
import com.example.wavex.feature.library.presentation.LibraryScreen
import com.example.wavex.feature.library.presentation.LibraryViewModel
import com.example.wavex.feature.library.presentation.SheetType
import com.example.wavex.feature.library.presentation.pressScale
import com.example.wavex.feature.library.sheets.model.PlaylistEditorState
import com.example.wavex.feature.library.sheets.presentation.AddSpotifyPlaylistBottomSheet
import com.example.wavex.feature.library.sheets.presentation.AddWaveXPlaylistBottomSheet
import com.example.wavex.feature.library.sheets.presentation.CreatePlaylistBottomSheet
import com.example.wavex.feature.library.sheets.presentation.PlaylistEditorViewModel
import com.example.wavex.feature.library.sheets.presentation.RenamePlaylistBottomSheet
import com.example.wavex.feature.player.presentation.PlayerActivityScreen
import com.example.wavex.feature.playlist.presentation.PlaylistActivity
import com.example.wavex.feature.profile.presentation.ProfileViewModel
import com.example.wavex.feature.profile.presentation.downloads.presentation.DownloadViewModel
import com.example.wavex.feature.search.presentation.SearchAlbumsViewModel
import com.example.wavex.feature.search.presentation.SearchArtistsViewModel
import com.example.wavex.feature.search.presentation.SearchPlaylistsViewModel
import com.example.wavex.feature.search.presentation.SearchScreen
import com.example.wavex.feature.search.presentation.SearchSongsViewModel
import com.example.wavex.feature.search.presentation.SearchSource
import com.example.wavex.navigation.BottomNavRoute
import com.example.wavex.ui.theme.WaveXTheme
import com.example.wavex.uiComponent.BottomNavBar
import com.example.wavex.uiComponent.SongBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
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
                        Timber.tag("API").w("Server error ${it.code} on $baseUrl, trying next...")
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
                    Timber.tag("API").w("Network error on $baseUrl, trying next...")
                    continue
                } else {
                    throw e
                }
            }
        }

        throw Exception("All APIs failed")
    }

private lateinit var connectivityManager: ConnectivityManager

@AndroidEntryPoint
class MainScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityManager = getSystemService()!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        Timber.tag("DeepLink").d("onCreate")
        Timber.tag("DeepLink").d("MainScreen onCreate ${hashCode()}")
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

        setContent {
            WaveXTheme {
                Main_Screen(
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Timber.tag("DeepLink").d("onNewIntent")
        Timber.tag("DeepLink").d("MainScreen onNewIntent ${hashCode()}")
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return

        val deepLink = DeepLinkParser.parse(uri)

        val event = DeepLinkEvent(
            id = System.currentTimeMillis().toString(),
            deepLink = deepLink
        )

        DeepLinkManager.events.tryEmit(event)

        Timber.tag("DeepLink").d("Emitted: $deepLink")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main_Screen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    downloadViewModel: DownloadViewModel = hiltViewModel(),
    playlistEditorViewModel: PlaylistEditorViewModel = hiltViewModel(),
    importPlaylistViewModel: ImportPlaylistViewModel = hiltViewModel(),
    likedSongsViewModel: LikedSongsViewModel = hiltViewModel(),
    discoverViewModel: DiscoverViewModel = hiltViewModel(),
    searchArtistsViewModel: SearchArtistsViewModel = viewModel(),
    searchPlaylistsViewModel: SearchPlaylistsViewModel = viewModel(),
    searchAlbumsViewModel: SearchAlbumsViewModel = viewModel(),
    searchSongsViewModel: SearchSongsViewModel = viewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val downloadedIds by downloadViewModel
        .downloadedSongIds
        .collectAsState(initial = emptySet())

    val likedSongs by likedSongsViewModel.likedSongs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        DeepLinkManager.events.collectLatest { event ->
            Timber.tag("DeepLink").d("Received: ${event.deepLink}")

            when (val deepLink = event.deepLink) {
                is DeepLink.Song -> {
                    val intent = Intent(context, PlayerActivityScreen::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }

                    context.startActivity(intent)
                    DeepLinkManager.clear()
                }

                is DeepLink.Album -> {
                    val intent = Intent(context, AlbumActivity::class.java).apply {
                        putExtra("album_id", deepLink.id)
                        putExtra("album_imageUrl", "")
                        putExtra("album_source", deepLink.source)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }

                    context.startActivity(intent)
                    DeepLinkManager.clear()
                }

                is DeepLink.Playlist -> {
                    val intent = Intent(context, PlaylistActivity::class.java).apply {
                        putExtra("playlist_id", deepLink.id)
                        putExtra("playlist_imageUrl", "")
                        putExtra("playlist_source", deepLink.source)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }

                    context.startActivity(intent)
                    DeepLinkManager.clear()
                }

                is DeepLink.Artist -> {
                    val intent = Intent(context, ArtistActivity::class.java).apply {
                        putExtra("artist_id", deepLink.id)
                        putExtra("artist_imageUrl", "")
                        putExtra("artist_source", deepLink.source)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }

                    context.startActivity(intent)
                    DeepLinkManager.clear()
                }

                is DeepLink.UserPlaylist -> {
                    navController.navigate(
                        "library?openSheet=${SheetType.ADD_WAVEX_PLAYLIST.name}&url=${deepLink.id}"
                    ) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                        restoreState = true
                    }

                    DeepLinkManager.clear()
                }

                DeepLink.Unknown -> {
                    Timber.tag("DeepLink").d("Unknown")
                }
            }

            DeepLinkManager.clear()
        }
    }

    val importState by importPlaylistViewModel.importState.collectAsStateWithLifecycle()
    val playlistEditorState by playlistEditorViewModel.state.collectAsStateWithLifecycle()
    val showBottomSheet = remember { mutableStateOf(false) }
    var librarySheetType by rememberSaveable { mutableStateOf<SheetType?>(null) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var selectedSong by rememberSaveable { mutableStateOf<SongItem?>(null) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selectedPlaylist by rememberSaveable { mutableStateOf<List<SongItem>>(emptyList()) }
    var renamePlaylist by rememberSaveable { mutableStateOf<PlaylistData?>(null) }
    var initialWaveXUrl by rememberSaveable { mutableStateOf("") }

    val musicService = ServiceLocator.musicService

    val playlist by musicService?.playlistFlow?.collectAsState(initial = emptyList())
        ?: rememberSaveable { mutableStateOf(emptyList()) }

    val currentIndex by musicService?.currentIndexFlow?.collectAsState(initial = -1)
        ?: rememberSaveable { mutableIntStateOf(-1) }

    val qualityPreference = musicService?.downloadQualityPreference
            ?: AudioStreamQualityPreference.HIGH

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: rememberSaveable { mutableStateOf(false) }

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(showBottomSheet.value) {
        if (showBottomSheet.value) {
            showBottomSheet.value = true
        }
    }

    LaunchedEffect(playlistEditorState) {
        when (val currentState = playlistEditorState) {
            is PlaylistEditorState.Success -> {
                snackBarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Short
                )

                playlistEditorViewModel.resetState()
                showBottomSheet.value = false
            }

            is PlaylistEditorState.Error -> {
                snackBarHostState.showSnackbar(
                    message = currentState.message,
                    duration = SnackbarDuration.Short
                )

                playlistEditorViewModel.resetState()
            }

            else -> Unit
        }
    }

    LaunchedEffect(importState) {
        when (val state = importState) {
            is ImportState.Success -> {
                snackBarHostState.showSnackbar(
                    message = "Playlist imported successfully",
                    duration = SnackbarDuration.Short
                )

                showBottomSheet.value = false
                importPlaylistViewModel.cancelImport()
            }

            is ImportState.Error -> {
                snackBarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )

                importPlaylistViewModel.cancelImport()
            }

            else -> Unit
        }
    }

    Scaffold(
        containerColor = colorResource(id = R.color.background_color),
        bottomBar = {
            Column {
                val progress by musicService?.progress?.collectAsState(initial = 0)
                    ?: rememberSaveable { mutableIntStateOf(0) }

                val duration by musicService?.duration?.collectAsState(initial = 0)
                    ?: rememberSaveable { mutableIntStateOf(0) }

                val isBuffering by musicService?.isBuffering?.collectAsState(initial = false)
                    ?: rememberSaveable { mutableStateOf(false)}

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
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF414141),
                    shape = RoundedCornerShape(16.dp)
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
                    imageUrl = profileViewModel.profileImageUrl.collectAsStateWithLifecycle().value,
                    showSheet = showSheet,
                    onSongLongPress = { playlist, song, index ->
                        selectedSong = song
                        selectedIndex = index
                        selectedPlaylist = playlist
                        showSheet = true
                    },
                    onRefresh = {
                        homeViewModel.refresh()
                    },
                    uiState = homeViewModel.uiState.collectAsStateWithLifecycle().value
                )
            }

            composable(BottomNavRoute.Browse.route) {
                DiscoverScreen(
                    onClickBack = {
                        navController.popBackStack()
                    },
                    onDeleteSong = { songID ->
                        downloadViewModel.deleteSong(songID) { success, message -> }
                    },
                    likedSongs = likedSongsViewModel.likedSongs.collectAsStateWithLifecycle().value,
                    onToggleLike = { song ->
                        likedSongsViewModel.toggleLike(song)
                    },
                    onLoadDiscoverData = {
                        discoverViewModel.loadDiscoverData()
                    },
                    uiSate = discoverViewModel.uiState.collectAsStateWithLifecycle().value,
                    downloadedIds = downloadedIds,
                    snackBarHostState = snackBarHostState,
                    showSheet = showSheet,
                    playlists = libraryViewModel.playlists.collectAsStateWithLifecycle().value,
                    onAddSongToPlaylist = { playlistID, song, onResult ->
                        libraryViewModel.addSongToPlaylist(
                            playlistId = playlistID,
                            song = song,
                            onResult = onResult
                        )
                    }
                )
            }

            composable(BottomNavRoute.Search.route) {
                SearchScreen(
                    onClickBack = {
                        navController.popBackStack()
                    },
                    onDeleteSong = { songID ->
                        downloadViewModel.deleteSong(songID) { success, message -> }
                    },
                    likedSongs = likedSongsViewModel.likedSongs.collectAsStateWithLifecycle().value,
                    onToggleLike = { song ->
                        likedSongsViewModel.toggleLike(song)
                    },
                    downloadedIds = downloadedIds,
                    snackBarHostState = snackBarHostState,
                    showSheet = showSheet,
                    artists = searchArtistsViewModel.artists.collectAsStateWithLifecycle().value,
                    artistsUiSate = searchArtistsViewModel.uiState.collectAsStateWithLifecycle().value,
                    onSearchArtists = { query ->
                        searchArtistsViewModel.fetchArtistsByQuery(query)
                    },
                    onSearchYTArtists = { query ->
                        searchArtistsViewModel.fetchYTMusicArtists(query)
                    },
                    onArtistsClearResult = {
                        searchArtistsViewModel.clearResults()
                    },
                    onArtistsIdeal = {
                        searchArtistsViewModel.setIdle()
                    },
                    songs = searchSongsViewModel.songs.collectAsStateWithLifecycle().value,
                    songsUiState = searchSongsViewModel.uiState.collectAsStateWithLifecycle().value,
                    onSearchSongs = { query ->
                        searchSongsViewModel.fetchSongByQuery(query)
                    },
                    onSearchYTSongs = { query ->
                        searchSongsViewModel.fetchYTMusicSongs(query)
                    },
                    onSongsClearResult = {
                        searchSongsViewModel.clearResults()
                    },
                    onSongsIdeal = {
                        searchSongsViewModel.setIdle()
                    },
                    albums = searchAlbumsViewModel.albums.collectAsStateWithLifecycle().value,
                    albumUiState = searchAlbumsViewModel.uiState.collectAsStateWithLifecycle().value,
                    onSearchAlbums = { query ->
                        searchAlbumsViewModel.fetchAlbumByQuery(query)
                    },
                    onSearchYTAlbums = { query ->
                        searchAlbumsViewModel.fetchYTMusicAlbums(query)
                    },
                    onAlbumsClearResult = {
                        searchAlbumsViewModel.clearResults()
                    },
                    onAlbumsIdeal = {
                        searchAlbumsViewModel.setIdle()
                    },
                    playlists = searchPlaylistsViewModel.playlists.collectAsStateWithLifecycle().value,
                    playlistsUiState = searchPlaylistsViewModel.uiState.collectAsStateWithLifecycle().value,
                    onSearchPlaylists = { query ->
                        searchPlaylistsViewModel.fetchPlayListByQuery(query)
                    },
                    onSearchYTPlaylists = { query ->
                        searchPlaylistsViewModel.fetchYTMusicPlaylists(query)
                    },
                    onPlaylistsClearResult = {
                        searchPlaylistsViewModel.clearResults()
                    },
                    onPlaylistsIdeal = {
                        searchPlaylistsViewModel.setIdle()
                    },
                    libraryPlaylists = libraryViewModel.playlists.collectAsStateWithLifecycle().value,
                    onAddSongToPlaylist = { playlistID, song, onResult ->
                        libraryViewModel.addSongToPlaylist(
                            playlistId = playlistID,
                            song = song,
                            onResult = onResult
                        )
                    }
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

                LaunchedEffect(url) {
                    if (!url.isNullOrBlank()) {
                        initialWaveXUrl = url
                    }
                }

                LaunchedEffect(openSheet) {
                    if (openSheet == SheetType.ADD_WAVEX_PLAYLIST.name) {
                        librarySheetType = SheetType.ADD_WAVEX_PLAYLIST
                        showBottomSheet.value = true
                    }
                }

                LibraryScreen(
                    onClickBack = {
                        navController.popBackStack()
                    },
                    onDeletePlaylist = { playlistID, onResult ->
                        libraryViewModel.deletePlaylist(playlistID) { success ->
                            onResult(success)
                        }
                    },
                    onOpenSheet = { sheetType ->
                        librarySheetType = sheetType
                        showBottomSheet.value = true
                    },
                    playlists = libraryViewModel.playlists.collectAsStateWithLifecycle().value,
                    snackBarHostState = snackBarHostState,
                    showSheet = showSheet,
                    showBottomSheet = showBottomSheet.value,
                    importState = importState,
                    onPlaylistSelect = { playlist ->
                        renamePlaylist = playlist
                    },
                    onCancelImport = {
                        importPlaylistViewModel.cancelImport()
                    },
                    likedSongs = likedSongs
                )
            }
        }
    }

    if (showBottomSheet.value) {
        Dialog(
            onDismissRequest = {
                showBottomSheet.value = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        showBottomSheet.value = false
                    }
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colorResource(R.color.off_white),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* Consume click */ }
                ) {
                    when (librarySheetType) {
                        SheetType.CREATE_PLAYLIST -> {
                            CreatePlaylistBottomSheet(
                                onClose = {
                                    showBottomSheet.value = false
                                },
                                onCreate = { title, description ->
                                    playlistEditorViewModel.createPlaylist(
                                        title = title,
                                        description = description
                                    )
                                }
                            )
                        }

                        SheetType.ADD_SPOTIFY_PLAYLIST -> {
                            AddSpotifyPlaylistBottomSheet(
                                onClose = {
                                    showBottomSheet.value = false
                                },
                                onImportPlaylist = { apiUrl, url ->
                                    importPlaylistViewModel.importSpotifyPlaylist(
                                        apiUrl = apiUrl,
                                        url = url
                                    )
                                }
                            )
                        }

                        SheetType.ADD_WAVEX_PLAYLIST -> {
                            AddWaveXPlaylistBottomSheet(
                                onClose = {
                                    showBottomSheet.value = false
                                },
                                onImportPlaylist = { apiUrl, playlistID ->
                                    importPlaylistViewModel.importWaveXPlaylist(
                                        apiUrl = apiUrl,
                                        playlistId = playlistID
                                    )
                                },
                                initialWaveXUrl = initialWaveXUrl
                            )
                        }

                        SheetType.RENAME_PLAYLIST -> {
                            RenamePlaylistBottomSheet(
                                playlist = renamePlaylist,
                                onClose = {
                                    showBottomSheet.value = false
                                    renamePlaylist = null
                                },
                                onRename = { playlistID, title, description ->
                                    playlistEditorViewModel.renamePlaylist(
                                        playlistId = playlistID,
                                        title = title,
                                        description = description
                                    )
                                }
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    if (showSheet && selectedSong != null) {
        val song = selectedSong!!
        val isFavourite = likedSongs.contains(song.id)
        val isDownloaded = downloadedIds.contains(song.id)

        SongBottomSheet(
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
                likedSongsViewModel.toggleLike(song)
            },
            onToggleDownload = { song ->
                val selectedDownload =
                    DownloadQualitySelector.selectDownload(
                        downloads = song.downloadUrl,
                        preference = qualityPreference
                    )

                val downloadUrl = selectedDownload?.url
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
                            putExtra("url", downloadUrl)
                            putExtra("fileName", song.name)
                            putExtra("songId", song.id)
                            putExtra("song", song)
                        }

                        ContextCompat.startForegroundService(context, intent)
                    }

                    state == ParallelDownloader.DownloadState.FAILED -> {
                        val intent = Intent(context, MusicPlayerService::class.java).apply {
                            action = MusicPlayerService.ACTION_DOWNLOAD_START
                            putExtra("url", downloadUrl)
                            putExtra("fileName", song.name)
                            putExtra("songId", song.id)
                            putExtra("song", song)
                        }

                        ContextCompat.startForegroundService(context, intent)
                    }

                    else -> {
                        val intent = Intent(context, MusicPlayerService::class.java).apply {
                            action = MusicPlayerService.ACTION_DOWNLOAD_START
                            putExtra("url", downloadUrl)
                            putExtra("fileName", song.name)
                            putExtra("songId", song.id)
                            putExtra("song", song)
                        }

                        ContextCompat.startForegroundService(context, intent)
                    }
                }
            },
            playlists = libraryViewModel.playlists.collectAsStateWithLifecycle().value,
            onAddSongToPlaylist = { playlistID, song, onResult ->
                libraryViewModel.addSongToPlaylist(
                    playlistId = playlistID,
                    song = song,
                    onResult = onResult
                )
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
                model = when (song.songSource) {
                    SearchSource.YTMUSIC.name ->
                        song.image.getOrNull(0)?.url

                    SearchSource.JIOSAAVN.name ->
                        song.image.getOrNull(2)?.url
                            ?: song.image.lastOrNull()?.url

                    else ->
                        song.image.lastOrNull()?.url
                },
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
                    .size(36.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(80.dp))
                    .background(
                        if (isPlaying) colorResource(R.color.theme_color)
                        else colorResource(R.color.off_white).copy(alpha = 0.08f)
                    )
                    .border(
                        width = 0.90.dp,
                        color = colorResource(R.color.off_white).copy(alpha = 0.10f),
                        shape = RoundedCornerShape(80.dp)
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
                        tint = colorResource(R.color.off_white),
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

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(80.dp))
                    .background(colorResource(R.color.off_white).copy(alpha = 0.08f))
                    .border(
                        width = 0.90.dp,
                        color = colorResource(R.color.off_white).copy(alpha = 0.10f),
                        shape = RoundedCornerShape(80.dp)
                    )
                    .clickable(
                        interactionSource = addInteraction,
                        indication = null
                    ) {
                        onAddClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_icon),
                    contentDescription = "Add Icon",
                    tint = colorResource(R.color.off_white),
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = addScale
                            scaleY = addScale
                        }
                )
            }

            Spacer(modifier = Modifier.width(5.dp))
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
fun pressScale(
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

@Preview(showSystemUi = true)
@Composable
fun Main_ScreenPreview() {
    WaveXTheme {

    }
}