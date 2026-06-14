package com.example.wavex.libraryScreen.playlistScreen

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.example.wavex.albumScreen.ShareType
import com.example.wavex.fonts
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.libraryScreen.PlaylistImageGrid
import com.example.wavex.playerScreen.PlayerActivityScreen
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
import com.example.wavex.playlistScreen.formatTotalDuration
import com.example.wavex.pressScale
import com.example.wavex.profileScreen.downloadedSongScreen.DownloadViewModel
import com.example.wavex.profileScreen.settingScreen.AudioStreamQualityPreference
import com.example.wavex.profileScreen.settingScreen.DownloadQualitySelector
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ParallelDownloader
import com.example.wavex.service.ServiceLocator
import com.example.wavex.shareComponent.ShareAlbumPlaylistItem
import com.example.wavex.shareComponent.ShareAlbum_Playlist
import com.example.wavex.ui.theme.WaveXTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class PlaylistActivity : ComponentActivity() {
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

        val playlistId = intent.getStringExtra("playlist_Id")

        setContent {
            WaveXTheme {
                Playlist_Activity(playlistId = playlistId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Playlist_Activity(
    downloadViewModel: DownloadViewModel = hiltViewModel(),
    playlistId: String?,
    viewModel: PlaylistViewModel = viewModel()
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val downloadedIds by downloadViewModel
        .downloadedSongIds
        .collectAsState(initial = emptySet())

    val playlist by viewModel.playlist.collectAsStateWithLifecycle()

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()

    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(playlistId) {
        playlistId?.let {
            viewModel.observePlaylists(it)
        }
    }

    val playlistData = playlist.playlist
    val uniqueSongs = playlistData?.songs?.distinctBy { it.id }

    val context = LocalContext.current
    val activity = context as? Activity

    val (shuffleInteraction, shuffleScale) = pressScale()
    val (playPlaylistInteraction, playPlaylistScale) = pressScale()
    val (backInteraction, backScale) = pressScale()
    val (shareInteraction, shareScale) = pressScale()
    val interactionSource = remember { MutableInteractionSource() }

    val isTitleVisible = !playlist.isLoading && !playlist.isError
    var isDeleting by remember { mutableStateOf(false) }

    var showSongSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSongSheet || showShareSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
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

    val isBuffering by musicService?.isBuffering?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false)}

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)).
        nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isTitleVisible) {
                TopAppBar(
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
                    },
                    title = {

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
                                        interactionSource = shareInteraction,
                                        indication = null
                                    ) {
                                        showShareSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.share_icon),
                                    contentDescription = "Share Icon",
                                    tint = colorResource(R.color.primary_text_color),
                                    modifier = Modifier.padding(top = 1.dp, end = 2.dp).size(18.dp)
                                        .graphicsLayer {
                                            scaleX = shareScale
                                            scaleY = shareScale
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
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
            ) { data ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Snackbar(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                data.visuals.message.contains("playlist") -> R.drawable.playlist_icon
                                data.visuals.message.contains("downloads") ||
                                        data.visuals.message.contains("Downloading") -> R.drawable.download_icon
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
                playlist.isLoading -> {
                    LoadingEffect()
                }

                playlist.isError -> {
                    ErrorState(
                        message = playlist.errorMessage,
                    )
                }

                else -> {
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(R.color.background_color))
                    ) {
                        val (contentList, miniPlayer) = createRefs()

                        LazyColumn (
                            state = listState,
                            modifier = Modifier
                                .constrainAs(contentList){
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom)
                                    height = Dimension.fillToConstraints
                                },
                            contentPadding = PaddingValues(bottom = if (currentSong != null) 80.dp else 15.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, start = 24.dp, end = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val songImages = playlistData?.songs
                                        ?.mapNotNull { it.image.lastOrNull()?.url }
                                        ?.filter { it.isNotBlank() }
                                        ?.take(4)
                                        ?: emptyList()

                                    val shouldShowGrid =
                                        playlistData?.imageUrl.isNullOrBlank() || playlistData.imageUrl.contains("default_image")

                                    if (shouldShowGrid && songImages.size >= 4) {
                                        PlaylistImageGrid(
                                            images = songImages,
                                            modifier = Modifier
                                                .size((screenWidth * 0.4f).coerceAtMost(220.dp))
                                                .clip(RoundedCornerShape(16.dp))
                                                .zIndex(10f)
                                        )
                                    } else {
                                        AsyncImage(
                                            model = playlistData?.imageUrl,
                                            contentDescription = "Playlist Image",
                                            contentScale = ContentScale.Crop,
                                            error = painterResource(R.drawable.default_image),
                                            modifier = Modifier
                                                .size((screenWidth * 0.4f).coerceAtMost(220.dp))
                                                .clip(RoundedCornerShape(16.dp))
                                                .zIndex(10f)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(start = 15.dp)
                                            .animateContentSize(),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "PLAYLIST",
                                            fontSize = 12.sp,
                                            lineHeight = 14.sp,
                                            letterSpacing = 1.5.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.theme_color),
                                            maxLines = 1
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        playlistData?.playlistName?.let {
                                            Text(
                                                modifier = Modifier
                                                    .animateContentSize(
                                                        animationSpec = spring(
                                                            stiffness = Spring.StiffnessLow
                                                        )
                                                    ),
                                                text = it,
                                                fontSize = 25.sp,
                                                lineHeight = 26.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color),
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        playlistData?.description
                                            ?.takeIf { it.isNotBlank() }
                                            ?.let { desc ->
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = desc,
                                                    fontSize = 12.sp,
                                                    lineHeight = 14.sp,
                                                    fontFamily = fonts,
                                                    fontWeight = FontWeight.Bold,
                                                    fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.secondary_text_color),
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Spacer(modifier = Modifier.width(3.dp))

                                            Text(
                                                text = "${playlistData?.totalSongs} Tracks",
                                                fontSize = 13.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Medium,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .width(1.8.dp)
                                                    .height(10.dp)
                                                    .background(
                                                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                            )

                                            Spacer(modifier = Modifier.width(3.dp))

                                            Text(
                                                text = formatTotalDuration(playlistData?.totalDuration ?: 0),
                                                fontSize = 13.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Medium,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 25.dp, start = 24.dp, end = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(60.dp)
                                            .shadow(
                                                elevation = 25.dp,
                                                shape = RoundedCornerShape(22.dp),
                                                ambientColor = colorResource(R.color.theme_color),
                                                spotColor = colorResource(R.color.theme_color)
                                            )
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(colorResource(R.color.theme_color))
                                            .clickable(
                                                interactionSource = playPlaylistInteraction,
                                                indication = null
                                            ) {
                                                if (playlistData?.songs?.isNotEmpty() == true) {
                                                    PlayerManager.currentPlaylist = playlistData.songs
                                                    ServiceLocator.musicService?.setPlaylist(playlistData.songs, 0)
                                                }
                                            }
                                            .graphicsLayer(
                                                scaleX = playPlaylistScale,
                                                scaleY = playPlaylistScale
                                            )
                                            .padding(horizontal = 24.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.notificationplaybutton),
                                                contentDescription = "Play Icon",
                                                tint = colorResource(R.color.background_color),
                                                modifier = Modifier.size(24.dp)
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = "Play playlist",
                                                fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts,
                                                fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.background_color)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .height(60.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(colorResource(R.color.primary_text_color).copy(alpha = 0.85f))
                                            .clickable(
                                                interactionSource = shuffleInteraction,
                                                indication = null
                                            ) {
                                                if (playlistData?.songs?.isNotEmpty() == true) {
                                                    PlayerManager.currentPlaylist = playlistData.songs

                                                    ServiceLocator.musicService?.let { service ->
                                                        service.setPlaylist(playlistData.songs, 0)
                                                        if (!service.isShuffle.value) {
                                                            service.shuffleToggle()
                                                        }
                                                    }
                                                }
                                            }
                                            .graphicsLayer(
                                                scaleX = shuffleScale,
                                                scaleY = shuffleScale
                                            )
                                            .padding(horizontal = 24.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.shuffle_icon),
                                                contentDescription = "Shuffle Icon",
                                                tint = colorResource(R.color.background_color),
                                                modifier = Modifier.size(22.dp)
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = "Shuffle",
                                                fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.background_color)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    modifier = Modifier.padding(top = 18.dp, start = 24.dp, bottom = 10.dp),
                                    text = "Tracks", fontSize = 18.sp, fontFamily = fonts,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                )
                            }

                            if (playlistData?.songs?.isEmpty() == true) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(top = 200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No Songs Yet", fontSize = 14.sp,
                                            fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            itemsIndexed(
                                items = uniqueSongs ?: emptyList(),
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
                                        if (offset.isNaN()) 0f else (-offset).coerceIn(0f, maxSwipePx)
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

                                                viewModel.removeSong(playlistId ?: "", song.id) { success, message ->
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
                                                .padding(start = if (currentSong?.id == song.id) 0.dp else 22.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = null
                                                ) {
                                                    val intent = Intent(context, MusicPlayerService::class.java).apply {
                                                        action = MusicPlayerService.ACTION_PLAY_NEW
                                                        putExtra("index", index)
                                                    }

                                                    PlayerManager.currentPlaylist = uniqueSongs ?: emptyList()
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
                                                        showSongSheet = true
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

                        Box(
                            modifier = Modifier
                                .constrainAs(miniPlayer) {
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom)
                                }
                                .fillMaxWidth().padding(bottom = 5.dp)
                        ) {
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
                                        showSongSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showSongSheet && selectedSong != null) {
                val song = selectedSong!!
                val isFavourite = likedSongs.contains(song.id)
                val isDownloaded = downloadedIds.contains(song.id)

                SongOptionsBottomSheet(
                    song = song,
                    isPlaying = isPlaying,
                    isCurrentSong = currentSong?.id == song.id,
                    onDismiss = {
                        showSongSheet = false
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

                            PlayerManager.currentPlaylist = playlistData?.songs ?: emptyList()
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
                    }
                )
            }

            if (showShareSheet) {
                ShareAlbum_Playlist(
                    album = ShareAlbumPlaylistItem(
                        id = playlistId.toString(),
                        title = playlistData?.playlistName.toString(),
                        artists = "Various artists",
                        songs = playlistData?.songs?.toList() ?: emptyList(),
                        songCount = playlistData?.songs?.size.toString(),
                        totalDuration = formatTotalDuration(playlistData?.totalDuration ?: 0),
                        image = playlistData?.imageUrl,
                        type = ShareType.USERPLAYLIST,
                        source = ""
                    ),
                    onDismiss = { showShareSheet = false }
                )
            }
        }
    }
}

@Composable
private fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .size(110.dp)
            .clip(RectangleShape),
        contentAlignment = Alignment.Center
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
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
            text = message,
            fontSize = 16.sp, lineHeight = 20.sp, fontFamily = fonts,
            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color), maxLines = 2
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaylistActivityPreview() {
    WaveXTheme {

    }
}