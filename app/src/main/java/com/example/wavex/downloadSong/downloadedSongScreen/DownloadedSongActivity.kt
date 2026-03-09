package com.example.wavex.downloadSong.downloadedSongScreen

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.albumScreen.AlbumActivity
import com.example.wavex.albumScreen.ShareBottomSheet
import com.example.wavex.albumScreen.ShareItem
import com.example.wavex.albumScreen.ShareType
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.downloadSong.data.DownloadedSong
import com.example.wavex.downloadSong.viewmodel.DownloadViewModel
import com.example.wavex.downloadSong.viewmodel.DownloadViewModelFactory
import com.example.wavex.fonts
import com.example.wavex.homeScreen.AppContainer
import com.example.wavex.homeScreen.ParallelDownloader
import com.example.wavex.homeScreen.downloadSong
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.playlistScreen.SheetOptionItem
import com.example.wavex.service.NetworkMonitor
import com.example.wavex.service.ServiceLocator
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch
import java.util.Locale

class DownloadedSongActivity : ComponentActivity() {
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

        val viewModel: DownloadViewModel by viewModels {
            DownloadViewModelFactory(AppContainer.downloadRepository)
        }

        setContent {
            WaveXTheme {
                Downloaded_Song_Activity(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Downloaded_Song_Activity(
    viewModel: DownloadViewModel
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val songsListState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val isPressed by interactionSource.collectIsPressedAsState()

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()

    var selectedSong by remember { mutableStateOf<DownloadedSong?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val downloadedIds by viewModel
        .downloadedSongIds
        .collectAsState(initial = emptySet())

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShareScale"
    )

    var showSheet by remember { mutableStateOf(false) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    val songs by viewModel.downloadedSongs.collectAsState(initial = emptyList())

    val musicService = ServiceLocator.musicService

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val quality = musicService?.qualityIndex

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
                        text = "Downloaded Songs",
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
                            data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
                            data.visuals.message.contains("downloads") ||
                                    data.visuals.message.contains("downloading") -> R.drawable.download_icon
                            data.visuals.message.contains("Downloading") ||
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
                songs.isEmpty() -> {
                    ErrorState(
                        message = "No Downloaded Songs"
                    )
                }

                else -> {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(songList) = createRefs()

                        LazyColumn (
                            state = songsListState,
                            modifier = Modifier.constrainAs(songList){
                                top.linkTo(parent.top, margin = 5.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                height = Dimension.fillToConstraints
                            },
                            contentPadding = PaddingValues(top = 8.dp, bottom = 25.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val uniqueSongs = songs.distinctBy { it.id }

                            itemsIndexed(
                                items = uniqueSongs,
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
//                                                val intent = Intent(context, MusicPlayerService::class.java).apply {
//                                                    action = MusicPlayerService.ACTION_PLAY_NEW
//                                                    putExtra("index", index)
//                                                }
//
//                                                PlayerManager.currentPlaylist = uniqueSongs
//                                                PlayerManager.currentIndex = index
//
//                                                ContextCompat.startForegroundService(context, intent)
//
//                                                scope.launch {
//                                                    RecentlyPlayedManager.add(context, song)
//                                                }
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
                                            IconButton(
                                                onClick = {
                                                    if (isDownloaded) {
                                                        scope.launch {
                                                            snackBarHostState.showSnackbar(
                                                                message = "Song already downloaded",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                        return@IconButton
                                                    }

                                                    if (ParallelDownloader.isDownloading(song.id)) {
                                                        scope.launch {
                                                            snackBarHostState.showSnackbar(
                                                                message = "Song is already downloading",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                        return@IconButton
                                                    }

                                                    Log.d("DOWNLOAD_TEST", "Download button clicked")

                                                    scope.launch {
                                                        snackBarHostState.showSnackbar(
                                                            message = "Downloading started",
                                                            duration = SnackbarDuration.Short
                                                        )

                                                        val url = song.downloadUrl[quality ?: 4].url

                                                        val path = ParallelDownloader.download(
                                                            songId = song.id,
                                                            url = url,
                                                            fileName = song.name,
                                                            context = context
                                                        )

                                                        if (path != null) {
                                                            viewModel.insertSong(
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
                                                            snackBarHostState.showSnackbar("Song downloaded successfully")
                                                        } else {
                                                            snackBarHostState.showSnackbar("Download failed")
                                                        }
                                                    }
                                                }
                                            ) {
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
                                                showSheet = true
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
                        }

                        if (showSheet && selectedSong != null) {
                            val song = selectedSong!!
                            val isFavourite = likedSongs.contains(song.id)
                            val isDownloaded = downloadedIds.contains(song.id)

                            SongOptionsBottomSheet(
                                song = song,
                                onDismiss = {
                                    showSheet = false
                                    selectedSong = null
                                },
                                onPlayNow = {
//                                    val intent = Intent(context, MusicPlayerService::class.java).apply {
//                                        action = MusicPlayerService.ACTION_PLAY_NEW
//                                        putExtra("index", selectedIndex)
//                                    }
//
//                                    PlayerManager.currentPlaylist = songs
//                                    PlayerManager.currentIndex = selectedIndex
//
//                                    ContextCompat.startForegroundService(context, intent)
//                                    showSheet = false
                                },
                                isFavourite = isFavourite,
                                isDownloaded = isDownloaded,
                                onToggleFavourite = {
//                                    likedViewModel.toggleLike(song)
                                },
                                onToggleDownload = { song ->
                                    if (isDownloaded) {
                                        viewModel.deleteSong(song.id)
                                    } else if (!ParallelDownloader.isDownloading(song.id)) {
                                        scope.launch {
                                            val path = ParallelDownloader.download(
                                                songId = song.id,
                                                url = song.downloadUrl[quality ?: 4].url,
                                                fileName = song.name,
                                                context = context
                                            )

                                            if (path != null) {
                                                viewModel.insertSong(
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
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: DownloadedSong,
    onDismiss: () -> Unit,
    onPlayNow: () -> Unit,
    isFavourite: Boolean,
    isDownloaded: Boolean,
    onToggleFavourite: (DownloadedSong) -> Unit,
    onToggleDownload: (DownloadedSong) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showArtistsDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val artistsGridState = rememberLazyGridState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val isOnline = rememberNetworkState()

    LaunchedEffect(Unit) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = colorResource(R.color.off_white),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            BottomSheetContent(
                song,
                onPlayNow,
                isFavourite,
                isDownloaded,
                isOnline,
                onToggleFavourite,
                onAddToPlaylistClick = {

                },
                onShowArtistsClick = {
                    showArtistsDialog = true
                },
                onShowShareSheet =  {
                    showShareSheet = true
                },
                onShowSnackBar = { message ->
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onToggleDownload
            )

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    containerColor = Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(
                                when {
                                    data.visuals.message.contains("Song") -> R.drawable.song_icon
                                    data.visuals.message.contains("playlist") -> R.drawable.playlist_icon
                                    data.visuals.message.contains("queue") -> R.drawable.queue_icon
                                    data.visuals.message.contains("downloads") ||
                                            data.visuals.message.contains("downloading") -> R.drawable.download_icon
                                    data.visuals.message.contains("Downloading") ||
                                            data.visuals.message.contains("downloaded") -> R.drawable.downloaded_icon
                                    data.visuals.message.contains("internet") -> R.drawable.cellular_network_icon
                                    else -> R.drawable.alert_icon
                                }
                            ),
                            contentDescription = null,
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
    }

    if (showArtistsDialog) {
        Dialog(onDismissRequest = { showArtistsDialog = false}) {
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
                        Icon(
                            painter = painterResource(R.drawable.mic_icon),
                            contentDescription = null,
                            tint = colorResource(R.color.theme_color),
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "Artists",
                            fontFamily = fonts,
                            fontSize = 18.sp, lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                        thickness = 1.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
                    )

                    LazyVerticalGrid(
                        state = artistsGridState,
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(song.artist) { artist ->
                            Column(
                                modifier = Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (isOnline) {
                                        val intent = Intent(context, ArtistActivity::class.java).apply {
                                            putExtra("artist_id", artist.id)
                                            putExtra("artist_imageUrl", artist.image)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                message = "No internet connection",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                } ,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = artist.image.takeIf { it.isNotBlank() },
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(R.drawable.default_artist),
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val artistName = htmlToText(artist.name)

                                Text(
                                    modifier = Modifier.width(78.dp),
                                    text = artistName,
                                    fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color), maxLines = 2, textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        ShareBottomSheet(
            item = ShareItem(
                title = htmlToText(song.name),
                subtitle = song.artist.joinToString(", ") { htmlToText(it.name) },
                image = song.image.getOrNull(2)?.url,
                id = song.id,
                type = ShareType.SONG
            ),
            onDismiss = { showShareSheet = false }
        )
    }
}

@Composable
private fun BottomSheetContent(
    song: DownloadedSong,
    onPlayNow: () -> Unit,
    isFavourite: Boolean,
    isDownloaded: Boolean,
    isOnline: Boolean,
    onToggleFavourite: (DownloadedSong) -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onShowArtistsClick: () -> Unit,
    onShowShareSheet: () -> Unit,
    onShowSnackBar: (String) -> Unit,
    onToggleDownload: (DownloadedSong) -> Unit
) {
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService

    val queue by musicService?.queueFlow?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val isInQueue = queue.any { it.id == song.id }

    var isScrollingDown by remember { mutableStateOf(false) }

    val shadowAlpha by animateFloatAsState(
        targetValue = if (isScrollingDown) 0f else 0.8f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "ShadowAlpha"
    )

    val shadowBlur by animateFloatAsState(
        targetValue = if (isScrollingDown) 0f else 50f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "ShadowBlur"
    )

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .width(58.dp)
                .height(4.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.image.getOrNull(2)?.url)
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                onSuccess = { result ->
                    val drawable = result.result.drawable
                    val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return@AsyncImage

                    Palette.from(bitmap).generate { palette ->
                        palette?.dominantSwatch?.rgb?.let { colorInt ->
                            shadowColor = Color(colorInt)
                        }
                    }
                },
                modifier = Modifier
                    .size(120.dp)
                    .drawBehind {
                        val safeBlur = shadowBlur.coerceAtLeast(0.1f)
                        val cornerRadius = 18.dp.toPx()

                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = shadowColor.copy(alpha = shadowAlpha)
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

                            canvas.drawRoundRect(
                                0f,
                                0f,
                                size.width,
                                size.height,
                                cornerRadius,
                                cornerRadius,
                                paint
                            )                        }
                    }
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = htmlToText(song.name), maxLines = 1,overflow = TextOverflow.Ellipsis,
                    fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Album • ${htmlToText(song.album?.name ?: "Unknown")}", overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color), lineHeight = 16.sp, maxLines = 2,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.headset_icon),
                        contentDescription = "Headset Icon",
                        tint = colorResource(R.color.primary_text_color),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "PlayCount • ${formatCount(song.playCount.toLong())}", overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.secondary_text_color), lineHeight = 14.sp, maxLines = 2,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            thickness = 1.dp,
            color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        SheetOptionItem(R.drawable.notificationplaybutton, "Play Now") {
            onPlayNow()
        }

        SheetOptionItem(
            icon = if (isFavourite) R.drawable.heart_filled else R.drawable.heart_outline,
            text = if (isFavourite) "Remove from Favourite" else "Save to Favourite",
            isAnimated = isFavourite,
            enabled = isOnline
        ) {
            if (isOnline) {
                onToggleFavourite(song)
            } else {
                onShowSnackBar("No internet connection")
            }
        }

        //SheetOptionItem(R.drawable.next_icon, "Play Next")
        SheetOptionItem(
            icon = R.drawable.add_playlist_icon,
            text = "Add to Playlist",
            enabled = isOnline
        ) {
            if (isOnline) {
                onAddToPlaylistClick()
            } else {
                onShowSnackBar("No internet connection")
            }
        }

        SheetOptionItem(
            R.drawable.queue_icon,
            when {
                isInQueue -> "Remove from queue"
                else -> "Add to queue"
            }
        ) {
//            when {
//                isInQueue -> {
//                    musicService?.removeFromQueue(song.id)
//                    onShowSnackBar("Removed from queue")
//                }
//
//                isInPlaylist -> {
//                    onShowSnackBar("Song already in playlist")
//                }
//
//                else -> {
//                    musicService?.addToQueue(song)
//                    onShowSnackBar("Added to queue")
//                }
//            }
        }

        SheetOptionItem(
            icon = when {
                isDownloaded -> R.drawable.downloaded_icon
                ParallelDownloader.isDownloading(song.id) -> R.drawable.download_icon
                else -> R.drawable.download_icon
            },
            text = when {
                isDownloaded -> "Remove From Download"
                ParallelDownloader.isDownloading(song.id) -> "Download in Progress"
                else -> "Download"
            }
        ) {
            if (ParallelDownloader.isDownloading(song.id)) {
                onShowSnackBar("Song is already downloading")
                return@SheetOptionItem
            }

            onToggleDownload(song)

            if (isDownloaded) {
                onShowSnackBar("Removed from downloads")
            } else {
                onShowSnackBar("Downloading started")
            }
        }

        SheetOptionItem(R.drawable.mic_icon, "View Artist") {
            onShowArtistsClick()
        }

        SheetOptionItem(
            icon = R.drawable.album_icon,
            text = "Go to Album",
            enabled = isOnline
        ) {
            if (isOnline) {
                val intent = Intent(context, AlbumActivity::class.java).apply {
                    putExtra("album_id", song.album?.id)
                    putExtra("album_imageUrl", "")
                }
                context.startActivity(intent)
            } else {
                onShowSnackBar("No internet connection")
            }
        }

        SheetOptionItem(R.drawable.share_icon, "Share") {
            onShowShareSheet()
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SheetOptionItem(
    icon: Int,
    text: String,
    enabled: Boolean = true,
    isAnimated: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isAnimated) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SheetHeart"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            }
            .alpha(if (enabled) 1f else 0.4f)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            tint = colorResource(R.color.theme_color)
        )

        Spacer(modifier = Modifier.width(18.dp))

        Text(
            text = text,
            fontSize = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color), lineHeight = 16.sp
        )
    }
}

@Composable
fun rememberNetworkState(): Boolean {
    val context = LocalContext.current
    val monitor = remember { NetworkMonitor(context) }

    val isOnline by monitor.isOnline.collectAsState(initial = true)

    return isOnline
}
private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000_000 -> String.format(Locale.US,"%.1fB", count / 1_000_000_000.0)
        count >= 1_000_000 -> String.format(Locale.US,"%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US,"%.1fK", count / 1_000.0)
        else -> count.toString()
    }.replace(".0", "")
}

@Composable
private fun ErrorState(message: String) {
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
    }
}

@Preview(showSystemUi = true)
@Composable
fun DownloadedSongActivityPreview() {
    WaveXTheme {

    }
}