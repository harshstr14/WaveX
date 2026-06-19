package com.example.wavex.feature.playlist.presentation

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.MiniPlayer
import com.example.wavex.R
import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.service.MusicPlayerService
import com.example.wavex.core.service.ParallelDownloader
import com.example.wavex.core.service.ServiceLocator
import com.example.wavex.core.util.DownloadQualitySelector
import com.example.wavex.feature.album.presentation.ShareType
import com.example.wavex.feature.artist.presentation.ArtistActivity
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.home.presentation.PlayerManager
import com.example.wavex.feature.home.presentation.formatDuration
import com.example.wavex.feature.home.presentation.htmlToText
import com.example.wavex.feature.playlist.model.PlaylistDetailUiState
import com.example.wavex.feature.player.presentation.PlayerActivityScreen
import com.example.wavex.pressScale
import com.example.wavex.feature.search.presentation.SearchSource
import com.example.wavex.shareComponent.ShareAlbumPlaylistItem
import com.example.wavex.shareComponent.ShareAlbum_Playlist
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: String?,
    playlistImageUrl: String?,
    playlistSource: String?,
    playlistTitle: String?,
    gradient: List<Color>,
    rectangularImage: Boolean,
    playlists: PlaylistDetailUiState,
    isLoading: Boolean,
    onLoadPlaylist: (String) -> Unit,
    onLoadYTPlaylist: (String) -> Unit,
    onDeleteSong: (String) -> Unit,
    downloadedIds: Set<String>,
    likedSongs: Set<String>,
    onToggleLike: (SongItem) -> Unit,
    onCheckFavourite: (String) -> Unit,
    onToggleFavourite: (
        playlistID: String, playlistName: String,
        imageUrl: String, source: String, onResult: (String) -> Unit
    ) -> Unit,
    isFavourite: Boolean
)  {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val imageToLoad = if (playlistImageUrl.isNullOrBlank()) {
        playlists.images.getOrNull(2)?.url
    } else {
        playlistImageUrl
    }

    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(playlistId, playlistSource) {
        if (playlistId.isNullOrBlank()) {
            return@LaunchedEffect
        }

        when(playlistSource) {
            SearchSource.JIOSAAVN.name ->
                onLoadPlaylist(playlistId)

            SearchSource.YTMUSIC.name ->
                onLoadYTPlaylist(playlistId)
        }
    }

    LaunchedEffect(playlistId) {
        playlistId?.let {
            onCheckFavourite(it)
        }
    }

    val (shuffleInteraction, shuffleScale) = pressScale()
    val (playPlaylistInteraction, playPlaylistScale) = pressScale()
    val (artistInteraction, artistScale) = pressScale()
    val (heartInteraction, heartScale) = pressScale()
    val (backInteraction, backScale) = pressScale()
    val (shareInteraction, shareScale) = pressScale()

    val isTitleVisible = !isLoading && !playlists.isError
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

    val spectrumComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.music_spectrum)
    )

    val timerComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.timer)
    )

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

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(20.dp)
                                    ).clickable(
                                        interactionSource = heartInteraction,
                                        indication = null
                                    ) {
                                        onToggleFavourite(
                                            playlistId ?: "", playlists.name,
                                            imageToLoad ?: "", playlistSource ?: ""
                                        ) { message ->
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(if (isFavourite) R.drawable.heart_filled else R.drawable.heart_outline),
                                    contentDescription = "Heart Icon",
                                    tint = if (isFavourite)
                                        colorResource(R.color.theme_color)
                                    else
                                        colorResource(R.color.primary_text_color),
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = heartScale
                                        scaleY = heartScale
                                    }.size(18.dp)
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
                isLoading -> {
                    LoadingEffect()
                }

                playlistSource == "unknown" -> {
                    ErrorState(
                        message = "Invalid playlist source",
                        onRetry = {
                            when (playlistSource) {
                                SearchSource.JIOSAAVN.name -> {
                                    onLoadPlaylist(playlistId ?: "")
                                }

                                SearchSource.YTMUSIC.name -> {
                                    onLoadYTPlaylist(playlistId ?: "")
                                }
                            }
                        }
                    )

                }

                playlists.isError -> {
                    ErrorState(
                        message = playlists.errorMessage,
                        onRetry = {
                            if (!playlistId.isNullOrBlank()) {
                                when(playlistSource) {
                                    SearchSource.JIOSAAVN.name ->
                                        onLoadPlaylist(playlistId)

                                    SearchSource.YTMUSIC.name ->
                                        onLoadYTPlaylist(playlistId)
                                }
                            }
                        }
                    )
                }

                playlistId.isNullOrBlank() ->
                    ErrorState(
                        message = "Invalid playlist source",
                        onRetry = {
                            when (playlistSource) {
                                SearchSource.JIOSAAVN.name -> {
                                    onLoadPlaylist(playlistId ?: "")
                                }

                                SearchSource.YTMUSIC.name -> {
                                    onLoadYTPlaylist(playlistId ?: "")
                                }
                            }
                        }
                    )

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
                                if (playlists.name.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, start = 24.dp, end = 24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (gradient.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size((screenWidth * 0.4f).coerceAtMost(220.dp))
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        Brush.verticalGradient(gradient)
                                                    )
                                                    .zIndex(10f)
                                            ) {
                                                Text(
                                                    text = playlistTitle ?: "",
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(16.dp),
                                                    fontSize = 20.sp,
                                                    lineHeight = 20.sp,
                                                    maxLines = 2,
                                                    fontFamily = fonts,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colorResource(R.color.off_white)
                                                )
                                            }
                                        } else {
                                            AsyncImage(
                                                model = imageToLoad,
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

                                            Text(
                                                text = if (!playlistTitle.isNullOrEmpty()) {
                                                    playlistTitle
                                                } else {
                                                    htmlToText(playlists.name)
                                                },
                                                fontSize = 25.sp,
                                                lineHeight = 26.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (playlists.description.isNotEmpty()) {
                                                Text(
                                                    modifier = Modifier
                                                        .animateContentSize(
                                                            animationSpec = spring(
                                                                stiffness = Spring.StiffnessLow
                                                            )
                                                        ),
                                                    text = htmlToText(playlists.description),
                                                    fontSize = 13.sp,
                                                    lineHeight = 16.sp,
                                                    fontFamily = fonts,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.secondary_text_color),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Spacer(modifier = Modifier.width(3.dp))

                                                Text(
                                                    text = "${playlists.songCount} Tracks",
                                                    fontSize = 12.sp,
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
                                                    text = formatTotalDuration(playlists.totalDuration),
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
                                        }
                                    }
                                }
                            }

                            item {
                                if (playlists.name.isNotEmpty()) {
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
                                                    PlayerManager.currentPlaylist = playlists.songs
                                                    ServiceLocator.musicService?.setPlaylist(playlists.songs, 0)
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
                                                    PlayerManager.currentPlaylist = playlists.songs

                                                    ServiceLocator.musicService?.let { service ->
                                                        service.setPlaylist(playlists.songs, 0)
                                                        if (!service.isShuffle.value) {
                                                            service.shuffleToggle()
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
                            }

                            if (playlists.artists.isNotEmpty()) {
                                item {
                                    Text(
                                        modifier = Modifier.padding(top = 20.dp, start = 24.dp),
                                        text = "Featured Artists", fontSize = 18.sp, fontFamily = fonts,
                                        fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        letterSpacing = 1.sp,
                                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                    )
                                }

                                val uniqueArtists = playlists.artists.distinctBy { it.id }

                                item {
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 13.dp),
                                        contentPadding = PaddingValues(horizontal = 24.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(uniqueArtists) { artist ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(30.dp))
                                                    .background(colorResource(R.color.primary_text_color).copy(alpha = 0.85f))
                                                    .clickable(
                                                        interactionSource = artistInteraction,
                                                        indication = null
                                                    ) {
                                                        val intent = Intent(context, ArtistActivity::class.java).apply {
                                                            putExtra("artist_id", artist.id)
                                                            putExtra("artist_imageUrl", artist.image)
                                                            putExtra("artist_source",
                                                                when(artist.searchSource) {
                                                                    SearchSource.YTMUSIC.name -> {
                                                                        SearchSource.YTMUSIC.name
                                                                    }
                                                                    SearchSource.JIOSAAVN.name -> {
                                                                        SearchSource.JIOSAAVN.name
                                                                    }

                                                                    else -> {
                                                                        "Unknown"
                                                                    }
                                                                }
                                                            )
                                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                        }
                                                        context.startActivity(intent)
                                                    }
                                                    .graphicsLayer(
                                                        scaleX = artistScale,
                                                        scaleY = artistScale
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AsyncImage(
                                                        model = artist.image.takeIf { it.isNotBlank() },
                                                        contentDescription = artist.name,
                                                        contentScale = ContentScale.Crop,
                                                        error = painterResource(R.drawable.default_artist),
                                                        placeholder = painterResource(R.drawable.default_artist),
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                    )

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Text(
                                                        modifier = Modifier.padding(end = 8.dp),
                                                        text = htmlToText(artist.name),
                                                        fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
                                                        fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                        color = colorResource(R.color.background_color), maxLines = 2, textAlign = TextAlign.Center,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (playlists.songs.isNotEmpty()) {
                                item {
                                    Text(
                                        modifier = Modifier.padding(top = 15.dp, start = 24.dp, bottom = 5.dp),
                                        text = "Tracks", fontSize = 18.sp, fontFamily = fonts,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                    )
                                }

                                val uniqueSongs = playlists.songs.distinctBy { it.id }

                                itemsIndexed(
                                    items = uniqueSongs,
                                    key = { _, song -> song.id }
                                ) { index, song ->

                                    val isDownloaded = downloadedIds.contains(song.id)
                                    val state = ParallelDownloader.downloadStates[song.id]

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AnimatedVisibility(
                                            visible = currentSong?.id == song.id,
                                            enter = fadeIn() + expandHorizontally(),
                                            exit = fadeOut() + shrinkHorizontally()
                                        ) {
                                            val progress by animateLottieCompositionAsState(
                                                composition = spectrumComposition,
                                                isPlaying = isPlaying && currentSong?.id == song.id,
                                                iterations = LottieConstants.IterateForever
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(RectangleShape)
                                            ) {
                                                LottieAnimation(
                                                    composition = spectrumComposition,
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

                                                    PlayerManager.currentPlaylist = uniqueSongs
                                                    PlayerManager.currentIndex = index

                                                    ContextCompat.startForegroundService(context, intent)
                                                },
                                            verticalAlignment = Alignment.CenterVertically
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
                                                modifier = Modifier
                                                    .then(
                                                        if (rectangularImage) {
                                                            Modifier
                                                                .width(100.dp)
                                                                .height(64.dp)
                                                        } else {
                                                            Modifier.size(64.dp)
                                                        }
                                                    )
                                                    .clip(RoundedCornerShape(12.dp)),
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

                                                if (song.duration > 0) {
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
                                                    val isPlayingAnimation = state == ParallelDownloader.DownloadState.DOWNLOADING

                                                    val progress by animateLottieCompositionAsState(
                                                        composition = timerComposition,
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
                                                                    composition = timerComposition,
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

            if (showShareSheet) {
                ShareAlbum_Playlist(
                    album = ShareAlbumPlaylistItem(
                        id = playlists.id,
                        title = playlists.name,
                        artists = playlists.artists.joinToString(", ") { htmlToText(it.name) },
                        songs = playlists.songs,
                        songCount = playlists.songCount,
                        totalDuration = formatTotalDuration(playlists.totalDuration),
                        image = imageToLoad,
                        type = ShareType.PLAYLIST,
                        source = playlistSource.toString()
                    ),
                    onDismiss = { showShareSheet = false }
                )
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

                            PlayerManager.currentPlaylist = playlists.songs
                            PlayerManager.currentIndex = selectedIndex
                        }

                        ContextCompat.startForegroundService(context, intent)
                    },
                    isFavourite = isFavourite,
                    isDownloaded = isDownloaded,
                    onToggleFavourite = {
                        onToggleLike(song)
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
                                onDeleteSong(song.id)
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
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
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
                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
                fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.background_color)
            )
        }
    }
}

fun formatTotalDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return buildString {
        if (hours > 0) append("$hours h ")
        if (minutes > 0) append("$minutes min")
        if (isEmpty()) append("0s") // handle 0 case
    }.trim()
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

@Preview(showSystemUi = true)
@Composable
private fun PlaylistScreenPreview() {
    WaveXTheme {
        PlaylistScreen(
            playlistId = "",
            playlistImageUrl = "",
            playlistSource = "",
            playlistTitle = "",
            gradient = listOf(),
            rectangularImage = false,
            playlists = PlaylistDetailUiState(),
            isLoading = false,
            onLoadPlaylist = {},
            onLoadYTPlaylist = {},
            onDeleteSong = {},
            downloadedIds = setOf(),
            likedSongs = setOf(),
            onToggleLike = {},
            onCheckFavourite = {},
            onToggleFavourite = { _,_,_,_,_ -> },
            isFavourite = true
        )
    }
}