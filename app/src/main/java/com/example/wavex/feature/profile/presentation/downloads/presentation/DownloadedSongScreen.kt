package com.example.wavex.feature.profile.presentation.downloads.presentation

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.core.database.entity.DownloadedSongEntity
import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.service.MusicPlayerService
import com.example.wavex.core.service.NetworkMonitor
import com.example.wavex.core.service.ParallelDownloader
import com.example.wavex.core.service.ServiceLocator
import com.example.wavex.core.util.DownloadQualitySelector
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.home.presentation.PlayerManager
import com.example.wavex.feature.home.presentation.formatDuration
import com.example.wavex.feature.home.presentation.htmlToText
import com.example.wavex.feature.library.model.LibraryUiState
import com.example.wavex.feature.profile.presentation.settings.presentation.IOSStyleBottomDialog
import com.example.wavex.pressScale
import com.example.wavex.ui.theme.WaveXTheme
import com.example.wavex.uiComponent.SongBottomSheet
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedSongScreen(
    likedSongs: Set<String>,
    downloadedIds: Set<String>,
    songs: List<DownloadedSongEntity>,
    onDeleteSong: (String, onResult: (Boolean, String) -> Unit) -> Unit,
    onDeleteAll: () -> Unit,
    playlists: LibraryUiState,
    onAddSongToPlaylist: (String, SongItem, onResult: (Boolean, String) -> Unit) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val songsListState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val (backInteraction, backScale) = pressScale()
    val (deleteInteraction, deleteScale) = pressScale()

    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    var showSheet by remember { mutableStateOf(false) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    val uniqueSongs = songs.distinctBy { it.id }
    val songLists = uniqueSongs.map { it.toSongItem() }

    val musicService = ServiceLocator.musicService

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)).
        nestedScroll(scrollBehavior.nestedScrollConnection),
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
                                interactionSource = backInteraction,
                                indication = ripple(
                                    bounded = true,
                                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.15f)
                                )
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
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(20.dp)
                                ).clickable(
                                    interactionSource = deleteInteraction,
                                    indication = ripple(
                                        bounded = true,
                                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.15f)
                                    )
                                ) {
                                    if (songs.isEmpty()) {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                message = "No downloaded songs to clear",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        showDeleteDialog = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.delete_icon),
                                contentDescription = "Delete Icon",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier.size(18.dp)
                                    .graphicsLayer {
                                        scaleX = deleteScale
                                        scaleY = deleteScale
                                    }
                            )
                        }
                    }
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
                    modifier = Modifier
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
                .background(colorResource(R.color.background_color))
                .padding(paddingValues)
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
                    ErrorState()
                }

                else -> {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val(songList) = createRefs()

                        LazyColumn (
                            state = songsListState,
                            modifier = Modifier
                                .constrainAs(songList){
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom)
                                    height = Dimension.fillToConstraints
                                },
                            contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = songLists,
                                key = { _, song -> song.id }
                            ) { index, song ->

                                val isDownloaded = downloadedIds.contains(song.id)
                                val state = ParallelDownloader.downloadStates[song.id]

                                val density = LocalDensity.current
                                val maxSwipePx = with(density) { 100.dp.toPx() }

                                val anchors = remember(maxSwipePx) {
                                    DraggableAnchors {
                                        0 at 0f
                                        1 at -maxSwipePx
                                    }
                                }

                                val swipeState = remember(song.id, anchors) {
                                    AnchoredDraggableState(
                                        initialValue = 0,
                                        anchors = anchors
                                    )
                                }

                                val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                                    state = swipeState,
                                    positionalThreshold = { it * 0.5f }
                                )

                                val revealWidth by remember {
                                    derivedStateOf {
                                        val offset = swipeState.offset
                                        if (offset.isNaN()) 0f else (-offset).coerceIn(
                                            0f,
                                            maxSwipePx
                                        )
                                    }
                                }

                                val progress by remember {
                                    derivedStateOf {
                                        (revealWidth / maxSwipePx).coerceIn(0f, 1f)
                                    }
                                }

                                LaunchedEffect(maxSwipePx) {
                                    swipeState.updateAnchors(
                                        DraggableAnchors {
                                            0 at 0f
                                            1 at -maxSwipePx
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier.fillMaxWidth().animateContentSize()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .width(with(LocalDensity.current) { revealWidth.toDp() })
                                            .height(58.dp)
                                            .padding(end = 16.dp)
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(Color(0xFFFF4F4F)),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (isDeleting) return@IconButton

                                                isDeleting = true

                                                onDeleteSong(song.id) { success, message ->
                                                    scope.launch {
                                                        isDeleting = false

                                                        snackBarHostState.showSnackbar(message)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.delete_icon),
                                                contentDescription = null,
                                                tint = colorResource(R.color.off_white),
                                                modifier = Modifier.graphicsLayer {
                                                    scaleX = 0.4f + (0.2f * progress)
                                                    scaleY = 0.4f + (0.2f * progress)
                                                    alpha = progress
                                                }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset {
                                                val offset = swipeState.offset
                                                IntOffset(
                                                    x = if (offset.isNaN()) 0 else offset.roundToInt(),
                                                    y = 0
                                                )
                                            }
                                            .anchoredDraggable(
                                                state = swipeState,
                                                orientation = Orientation.Horizontal,
                                                flingBehavior = flingBehavior
                                            ),
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
                                                .padding(start = if (currentSong?.id == song.id) 0.dp else 22.dp, end = 12.dp)
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = null
                                                ) {
                                                    val intent = Intent(context, MusicPlayerService::class.java).apply {
                                                        action = MusicPlayerService.ACTION_PLAY_NEW
                                                        putExtra("index", index)
                                                    }

                                                    PlayerManager.currentPlaylist = songLists
                                                    PlayerManager.currentIndex = index

                                                    ContextCompat.startForegroundService(context, intent)
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = song.image.getOrNull(2)?.url,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f),
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = htmlToText(song.name),
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

                                                Text(
                                                    text = htmlToText(artistsList),
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
                                                    fontWeight = FontWeight.Medium,
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
                                                        val qualityPreference = musicService?.downloadQualityPreference
                                                            ?: AudioStreamQualityPreference.HIGH
                                                        val selectedDownload =
                                                            DownloadQualitySelector.selectDownload(
                                                                downloads = song.downloadUrl,
                                                                preference = qualityPreference
                                                            )

                                                        val downloadUrl = selectedDownload?.url
                                                        when {
                                                            isDownloaded -> {
                                                                scope.launch {
                                                                    snackBarHostState.showSnackbar("Song already downloaded")
                                                                }
                                                            }

                                                            state == ParallelDownloader.DownloadState.DOWNLOADING -> {
                                                                val intent = Intent(context, MusicPlayerService::class.java).apply {
                                                                    action = MusicPlayerService.ACTION_DOWNLOAD_PAUSE
                                                                    putExtra("songId", song.id)
                                                                }

                                                                context.startService(intent)
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
                                                    }
                                                ) {
                                                    val composition by rememberLottieComposition(
                                                        LottieCompositionSpec.RawRes(R.raw.timer)
                                                    )

                                                    val isPlayingAnimation = state == ParallelDownloader.DownloadState.DOWNLOADING

                                                    val progress by animateLottieCompositionAsState(
                                                        composition = composition,
                                                        isPlaying = isPlayingAnimation,
                                                        iterations = LottieConstants.IterateForever
                                                    )

                                                    when {
                                                        state == ParallelDownloader.DownloadState.DOWNLOADING ||
                                                                state == ParallelDownloader.DownloadState.PAUSED -> {

                                                            Box(
                                                                modifier = Modifier
                                                                    .size(30.dp)
                                                                    .clip(RectangleShape)
                                                            ) {
                                                                LottieAnimation(
                                                                    composition = composition,
                                                                    progress = { progress },
                                                                    modifier = Modifier
                                                                        .size(30.dp)
                                                                        .graphicsLayer {
                                                                            scaleX = 2f
                                                                            scaleY = 2f
                                                                        }
                                                                )
                                                            }
                                                        }

                                                        else -> {
                                                            Icon(
                                                                painter = if (isDownloaded)
                                                                    painterResource(R.drawable.downloaded_icon)
                                                                else
                                                                    painterResource(R.drawable.download_icon),
                                                                contentDescription = "Download",
                                                                modifier = Modifier.size(24.dp),
                                                                tint = if (isDownloaded)
                                                                    colorResource(R.color.theme_color).copy(alpha = 0.6f)
                                                                else
                                                                    colorResource(R.color.primary_text_color).copy(alpha = 0.6f)
                                                            )
                                                        }
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        selectedSong = song
                                                        selectedIndex = index
                                                        showSheet = true
                                                    }
                                                ) {
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

                                        PlayerManager.currentPlaylist = songLists
                                        PlayerManager.currentIndex = selectedIndex
                                    }

                                    ContextCompat.startForegroundService(context, intent)
                                },
                                isFavourite = isFavourite,
                                isDownloaded = isDownloaded,
                                onToggleFavourite = {

                                },
                                onToggleDownload = { song ->
                                    val qualityPreference = musicService?.downloadQualityPreference
                                        ?: AudioStreamQualityPreference.HIGH
                                    val selectedDownload =
                                        DownloadQualitySelector.selectDownload(
                                            downloads = song.downloadUrl,
                                            preference = qualityPreference
                                        )

                                    val downloadUrl = selectedDownload?.url
                                    val state = ParallelDownloader.downloadStates[song.id]

                                    when {
                                        isDownloaded -> {
                                            onDeleteSong(song.id) { success, message ->}
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
                                playlists = playlists,
                                onAddSongToPlaylist = { playlistID, song, onResult ->
                                    onAddSongToPlaylist(playlistID, song, onResult)
                                }
                            )
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                IOSStyleBottomDialog(
                    title = "Delete All Songs?",
                    message = "Do you want to delete all downloaded songs.",
                    confirmText = "Delete",
                    icon = R.drawable.delete_icon,
                    onConfirm = {
                        onDeleteAll()
                        showDeleteDialog = false
                    },
                    onDismiss = {
                        showDeleteDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun rememberNetworkState(): Boolean {
    val context = LocalContext.current
    val monitor = remember { NetworkMonitor(context) }

    val isOnline by monitor.isOnline.collectAsState(initial = true)

    return isOnline
}

fun DownloadedSongEntity.toSongItem(): SongItem {
    return SongItem(
        id = id,
        name = name,
        artist = artist,
        album = album,
        image = image,
        duration = duration,
        playCount = playCount,
        downloadUrl = downloadUrl,
        localPath = localPath,
        songSource = songSource,
        playedAt = playedAt
    )
}

@Composable
private fun ErrorState() {
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

        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RectangleShape)
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = 1.2f
                        scaleY = 1.2f
                    }
            )
        }

        Text(
            text = "No Downloaded Songs",
            fontSize = 16.sp, lineHeight = 20.sp, fontFamily = fonts,
            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color), maxLines = 2
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun DownloadedSongScreenPreview() {
    WaveXTheme {
        DownloadedSongScreen(
            likedSongs = emptySet(),
            downloadedIds = emptySet(),
            songs = emptyList(),
            onDeleteSong = { _, _ -> },
            onDeleteAll = {},
            playlists = LibraryUiState(),
            onAddSongToPlaylist = { _,_,_ -> }
        )
    }
}