package com.example.wavex.artistScreen.allSongsScreen

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.MiniPlayer
import com.example.wavex.R
import com.example.wavex.downloadSong.data.DownloadedSong
import com.example.wavex.downloadSong.viewmodel.DownloadViewModel
import com.example.wavex.downloadSong.viewmodel.DownloadViewModelFactory
import com.example.wavex.fonts
import com.example.wavex.homeScreen.AppContainer
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.downloadSong
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.playerScreen.PlayerActivityScreen
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ServiceLocator
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch

class AllSongsActivity : ComponentActivity() {
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

        val artistId = intent.getStringExtra("artist_id")
        val downloadViewModel: DownloadViewModel by viewModels {
            DownloadViewModelFactory(AppContainer.downloadRepository)
        }

        setContent {
            WaveXTheme {
                All_Songs_Activity(downloadViewModel, artistId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun All_Songs_Activity(
    downloadViewModel: DownloadViewModel,
    artistId: String?,
    viewModel: AllSongsViewModel = viewModel(),
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val songsListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val downloadedIds by downloadViewModel
        .downloadedSongIds
        .collectAsState(initial = emptySet())

    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()

    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(artistId) {
        artistId?.let {
            viewModel.fetchSongsByArtistID(it, "songs")
        }
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShareScale"
    )

    val musicService = ServiceLocator.musicService

    val currentIndex by musicService?.currentIndexFlow?.collectAsState(initial = -1)
        ?: remember { mutableIntStateOf(-1) }

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val progress by musicService?.progress?.collectAsState(initial = 0)
        ?: remember { mutableIntStateOf(0) }

    val duration by musicService?.duration?.collectAsState(initial = 0)
        ?: remember { mutableIntStateOf(0) }

    val quality = musicService?.qualityIndex

    var showSongSheet by remember { mutableStateOf(false) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSongSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    Box(
                        modifier = Modifier.padding(start = 20.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                width = 1.5.dp,
                                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(20.dp)
                            ).clickable(
                                interactionSource = interactionSource,
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
                                    scaleX = scale
                                    scaleY = scale
                                }
                        )
                    }
                },
                title = {
                    Text(
                        text = "All Songs",
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
                            data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
                            data.visuals.message.contains("Downloading") -> R.drawable.downloaded_icon
                            data.visuals.message.contains("downloaded") -> R.drawable.downloaded_icon
                            data.visuals.message.contains("failed") -> R.drawable.alert_icon
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
                .padding(paddingValues)
                .background(colorResource(R.color.background_color))
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && animatedBlur > 0f) {
                        renderEffect = RenderEffect
                            .createBlurEffect(
                                animatedBlur,
                                animatedBlur,
                                Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    LoadingEffect()
                }

                songs.isError -> {
                    ErrorState(
                        message = songs.errorMessage,
                        onRetry = {
                            artistId?.let { viewModel.fetchSongsByArtistID(it, "songs") }
                        }
                    )
                }

                else -> {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(songList, miniPlayer) = createRefs()

                        LazyColumn (
                            state = songsListState,
                            modifier = Modifier.constrainAs(songList){
                                top.linkTo(parent.top, margin = 5.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                height = Dimension.fillToConstraints
                            },
                            contentPadding = PaddingValues(top = 8.dp, bottom = if (currentSong != null) 95.dp else 25.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                songs.songs,
                                key = { _, song -> song.id }
                            ) { index, song ->

                                val isDownloaded = downloadedIds.contains(song.id)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AnimatedVisibility(
                                        visible = currentSong?.id == song.id,
                                        enter = fadeIn() + expandHorizontally(),
                                        exit = fadeOut() + shrinkHorizontally()
                                    ) {
                                        val composition by rememberLottieComposition(
                                            LottieCompositionSpec.RawRes(R.raw.music_spectrum)
                                        )

                                        val progress by animateLottieCompositionAsState(
                                            composition = composition,
                                            isPlaying = isPlaying && currentSong?.id == song.id,
                                            iterations = LottieConstants.IterateForever
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RectangleShape)
                                        ) {
                                            LottieAnimation(
                                                composition = composition,
                                                progress = { progress },
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .graphicsLayer {
                                                        scaleX = 2.2f
                                                        scaleY = 2.2f
                                                    }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(start = if (currentSong?.id == song.id) 0.dp else 24.dp, end = 12.dp)
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                val intent = Intent(context, MusicPlayerService::class.java).apply {
                                                    action = MusicPlayerService.ACTION_PLAY_NEW
                                                    putExtra("index", index)
                                                }

                                                PlayerManager.currentPlaylist = songs.songs
                                                PlayerManager.currentIndex = index

                                                ContextCompat.startForegroundService(context, intent)

                                                scope.launch {
                                                    RecentlyPlayedManager.add(context, song)
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = song.image.getOrNull(2)?.url,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(
                                            modifier = Modifier
                                                .weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            val songName = htmlToText(song.name)

                                            Text(
                                                text = songName,
                                                fontSize = 15.sp,
                                                lineHeight = 16.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color),
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
                                                fontSize = 13.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = formatDuration(song.duration),
                                                fontSize = 12.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = {
                                                Log.d("DOWNLOAD_TEST", "Download button clicked")

                                                scope.launch {
                                                    snackBarHostState.showSnackbar(
                                                        message = "Downloading started",
                                                        duration = SnackbarDuration.Short
                                                    )

                                                    val url = song.downloadUrl[quality ?: 4].url

                                                    Log.d("DOWNLOAD_TEST", "URL = $url")

                                                    val path = downloadSong(
                                                        url,
                                                        song.name,
                                                        context
                                                    )

                                                    Log.d("DOWNLOAD_TEST", "Download finished path = $path")

                                                    if (path != null) {
                                                        Log.d("DOWNLOAD_TEST", "Saving to database")

                                                        downloadViewModel.insertSong(
                                                            DownloadedSong(
                                                                id = song.id,
                                                                name = song.name,
                                                                artist = song.artist,
                                                                album = song.album,
                                                                image = song.image,
                                                                duration = song.duration,
                                                                playCount = song.playCount,
                                                                downloadUrl = song.downloadUrl,
                                                                localPath = path
                                                            )
                                                        )

                                                        snackBarHostState.showSnackbar(
                                                            message = "Song downloaded successfully",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    } else {
                                                        snackBarHostState.showSnackbar(
                                                            message = "Download failed",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            }) {
                                                Icon(
                                                    modifier = Modifier.size(24.dp),
                                                    painter = if (isDownloaded) painterResource(R.drawable.downloaded_icon)
                                                        else painterResource(R.drawable.download_icon),
                                                    contentDescription = "Download",
                                                    tint = if (isDownloaded) colorResource(R.color.theme_color).copy(alpha = 0.6f)
                                                        else colorResource(R.color.primary_text_color).copy(alpha = 0.6f)
                                                )
                                            }

                                            IconButton(onClick = {
                                                selectedSong = song
                                                selectedIndex = index
                                                showSongSheet = true
                                            }) {
                                                Icon(
                                                    modifier = Modifier.size(20.dp),
                                                    painter = painterResource(R.drawable.three_dots_icon),
                                                    contentDescription = "Three Dots",
                                                    tint = colorResource(R.color.primary_text_color).copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {
                                            viewModel.loadNextPage()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "See More",
                                        fontSize = 16.sp,
                                        lineHeight = 18.sp,
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.SemiBold,
                                        fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.theme_color),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (showSongSheet && selectedSong != null) {
                            val song = selectedSong!!
                            val isFavourite = likedSongs.contains(song.id)
                            val isDownloaded = downloadedIds.contains(song.id)

                            SongOptionsBottomSheet(
                                song = song,
                                onDismiss = {
                                    showSongSheet = false
                                    selectedSong = null
                                },
                                onPlayNow = {
                                    val intent = Intent(context, MusicPlayerService::class.java).apply {
                                        action = MusicPlayerService.ACTION_PLAY_NEW
                                        putExtra("index", selectedIndex)
                                    }

                                    PlayerManager.currentPlaylist = songs.songs
                                    PlayerManager.currentIndex = selectedIndex

                                    ContextCompat.startForegroundService(context, intent)
                                    showSongSheet = false
                                },
                                isFavourite = isFavourite,
                                isDownloaded = isDownloaded,
                                onToggleFavourite = {
                                    likedViewModel.toggleLike(song)
                                },
                                onToggleDownload = { song ->
                                    if (isDownloaded) {
                                        downloadViewModel.deleteSong(song.id)
                                    } else {
                                        scope.launch {
                                            val path = downloadSong(
                                                song.downloadUrl[quality ?: 4].url,
                                                song.name,
                                                context
                                            )

                                            if (path != null) {
                                                Log.d("DOWNLOAD_TEST", "Saving to database")

                                                downloadViewModel.insertSong(
                                                    DownloadedSong(
                                                        id = song.id,
                                                        name = song.name,
                                                        artist = song.artist,
                                                        album = song.album,
                                                        image = song.image,
                                                        duration = song.duration,
                                                        playCount = song.playCount,
                                                        downloadUrl = song.downloadUrl,
                                                        localPath = path
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        Box(
                            modifier = Modifier.constrainAs(miniPlayer) {
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                            }.fillMaxWidth()
                        ) {
                            currentSong?.let { song ->
                                MiniPlayer(
                                    song = song,
                                    isPlaying = isPlaying,
                                    progress = if (duration > 0)
                                        progress.toFloat() / duration.toFloat()
                                    else 0f,
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
                                        showSongSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 45.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes (R.raw.spaceman)
        )

        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(144.dp)
        )

        Spacer(modifier = Modifier.height(0.dp))

        Text(
            modifier = Modifier.offset(y = (-8).dp),
            text = message,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color), maxLines = 2
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier.offset(y = (-8).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(R.color.theme_color))
                .clickable { onRetry() }
                .padding(horizontal = 24.dp, vertical = 10.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Retry",
                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.background_color)
            )
        }
    }
}

@Composable
private fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.fillMaxWidth().padding(bottom = 45.dp).size(144.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun All_Songs_ActivityPreview() {
    WaveXTheme {

    }
}