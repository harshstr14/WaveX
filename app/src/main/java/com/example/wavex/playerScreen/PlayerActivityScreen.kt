package com.example.wavex.playerScreen

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.albumScreen.ShareBottomSheet
import com.example.wavex.albumScreen.ShareItem
import com.example.wavex.albumScreen.ShareType
import com.example.wavex.fonts
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ServiceLocator
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch

class PlayerActivityScreen : ComponentActivity() {
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

        setContent {
            WaveXTheme {
                Player_Activity_Screen()
            }
        }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                R.anim.fade_in,
                R.anim.slide_down
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.slide_down
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Player_Activity_Screen() {
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val (backInteraction, backScale) = pressScale()
    val (playInteraction, playScale) = pressScale()
    val (nextInteraction, nextScale) = pressScale()
    val (prevInteraction, prevScale) = pressScale()
    val (repeatInteraction, repeatScale) = pressScale()
    val (shuffleInteraction, shuffleScale) = pressScale()
    val (shareInteraction, shareScale) = pressScale()

    val context = LocalContext.current
    val activity = context as? Activity

    var showSongSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val scope = rememberCoroutineScope()
    val sheetState = scaffoldState.bottomSheetState

    BackHandler(
        enabled = sheetState.currentValue != SheetValue.PartiallyExpanded &&
                sheetState.currentValue != SheetValue.Hidden
    ) {
        scope.launch {
            sheetState.partialExpand()
        }
    }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSongSheet || showShareSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    val musicService = ServiceLocator.musicService

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val progressMs by musicService?.progress?.collectAsState(initial = 0L)
        ?: remember { mutableLongStateOf(0L) }

    val duration by musicService?.duration?.collectAsState(initial = 0)
        ?: remember { mutableIntStateOf(0) }

    val isShuffle by musicService?.isShuffle?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val repeatMode by musicService?.repeatMode?.collectAsState(initial = Player.REPEAT_MODE_OFF)
        ?: remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }

    val progressFraction =
        if (duration > 0L)
            progressMs.toFloat() / duration.toFloat()
        else 0f

    val tintColor = when (repeatMode) {
        Player.REPEAT_MODE_OFF ->
            colorResource(R.color.primary_text_color)

        Player.REPEAT_MODE_ALL ->
            colorResource(R.color.theme_color)

        Player.REPEAT_MODE_ONE ->
            colorResource(R.color.theme_color)

        else ->
            colorResource(R.color.primary_text_color)
    }

    val repeatIcon = when (repeatMode) {
        Player.REPEAT_MODE_OFF ->
            R.drawable.notificationrepeatbutton

        Player.REPEAT_MODE_ALL ->
            R.drawable.notificationrepeatbutton

        Player.REPEAT_MODE_ONE ->
            R.drawable.notificationrepeatonebutton

        else ->
            R.drawable.notificationrepeatbutton
    }

    val upNextList by musicService?.upNextFlow?.collectAsStateWithLifecycle(initialValue = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()

    val playlistViewModel: PlayerViewModel = viewModel()

    LaunchedEffect(currentSong?.id) {
        playlistViewModel.loadWaveform(currentSong?.id)
    }

    val amplitudes by playlistViewModel.amplitudes.collectAsState()

    val shadowColorButton by animateColorAsState(
        targetValue = if (isPlaying) Color(0xFF34A853) else Color(0xFF797979),
        animationSpec = tween(durationMillis = 200),
        label = "shadowColorAnimation"
    )

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }

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

    val shadowScale by animateFloatAsState(
        targetValue = if (isScrollingDown) 0.8f else 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "ShadowScale"
    )

    var isDragging by remember { mutableStateOf(false) }
    var wasPlayingBeforeDrag by remember { mutableStateOf(false) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetDragHandle = null,
        sheetPeekHeight = 85.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = Color(0xFFdbdbdb),
        sheetContent = {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val sheetHeight = maxHeight * 0.45f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(sheetHeight)
                ) {
                    UpNextSheetContent(
                        songLists = upNextList,
                        currentSongId = currentSong?.id,
                        sheetState = scaffoldState.bottomSheetState,
                        onMoreClick = { song, index, songsList ->
                            selectedSong = song
                            selectedIndex = index
                            PlayerManager.currentPlaylist = songsList
                            PlayerManager.currentIndex = index
                            showSongSheet = true
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.background(colorResource(R.color.background_color))
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets(0),
            topBar = {
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
                                        color = colorResource(R.color.secondary_text_color).copy(
                                            alpha = 0.6f
                                        ),
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
                                    modifier = Modifier.padding(end = 2.dp).size(18.dp)
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
            },
            snackbarHost = {
                SnackbarHost(
                    snackBarHostState,
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
                            Icon(
                                painter = painterResource(
                                    when {
                                        data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
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
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                    val(songImage, songName, albumName, progressBar, startTime, endTime, row) = createRefs()

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentSong?.image?.getOrNull(2)?.url)
                            .allowHardware(false)
                            .build(),
                        contentDescription = currentSong?.name,
                        onSuccess = { result ->
                            val drawable = result.result.drawable
                            val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return@AsyncImage

                            Palette.from(bitmap).generate { palette ->
                                palette?.dominantSwatch?.rgb?.let { colorInt ->
                                    shadowColor = Color(colorInt)
                                }
                            }
                        },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .constrainAs(songImage) {
                                top.linkTo(parent.top, margin = 24.dp)
                                start.linkTo(parent.start, margin = 24.dp)
                                end.linkTo(parent.end, margin = 24.dp)
                            }
                            .size(310.dp)
                            .drawBehind {
                                val safeBlur = (shadowBlur * 1.2f).coerceAtLeast(0.1f)
                                val cornerRadius = 20.dp.toPx()

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
                                    )
                                }
                            }
                            .clip(RoundedCornerShape(20.dp))
                    )

                    Text(
                        modifier = Modifier.constrainAs(songName) {
                            top.linkTo(songImage.bottom, margin = 24.dp)
                            start.linkTo(songImage.start, margin = 14.dp)
                            end.linkTo(songImage.end, margin = 14.dp)
                            width = Dimension.fillToConstraints
                        }.animateContentSize(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        text = htmlToText(currentSong?.name),
                        fontSize = 18.sp, lineHeight = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), maxLines = 2, textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        modifier = Modifier.constrainAs(albumName) {
                            top.linkTo(songName.bottom, margin = 8.dp)
                            start.linkTo(songImage.start, margin = 14.dp)
                            end.linkTo(songImage.end, margin = 14.dp)
                            width = Dimension.fillToConstraints
                        },
                        text = "Album • ${htmlToText(currentSong?.album?.name)}",
                        fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.secondary_text_color), maxLines = 1, textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis
                    )

                    Column(modifier = Modifier.constrainAs(progressBar) {
                        top.linkTo(albumName.bottom, margin = 24.dp)
                        start.linkTo(parent.start, margin = 20.dp)
                        end.linkTo(parent.end, margin = 20.dp)
                        width = Dimension.fillToConstraints
                    }) {
                        AudioWaveform(
                            amplitudes = amplitudes,
                            progress = progressFraction,
                            onDragStateChanged = { dragging ->
                                if (dragging) {
                                    wasPlayingBeforeDrag = musicService?.isPlaying() == true

                                    if (wasPlayingBeforeDrag) {
                                        musicService?.pause()
                                    }

                                    isDragging = true
                                } else {
                                    isDragging = false

                                    if (wasPlayingBeforeDrag) {
                                        musicService?.play()
                                    }
                                }
                            },
                            onProgressChanged = { newProgress ->
                                val seekPositionMs = (duration * newProgress).toLong()

                                musicService?.seekTo(seekPositionMs)
                            }                        )
                    }

                    Text(
                        modifier = Modifier.constrainAs(startTime) {
                            top.linkTo(progressBar.bottom, margin = 12.dp)
                            start.linkTo(parent.start, margin = 22.dp)
                        },
                        text = formatTime(progressMs.toLong()),
                        fontSize = 10.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), maxLines = 1
                    )

                    Text(
                        modifier = Modifier.constrainAs(endTime) {
                            top.linkTo(progressBar.bottom, margin = 12.dp)
                            end.linkTo(parent.end, margin = 22.dp)
                        },
                        text = formatTime(duration.toLong()),
                        fontSize = 10.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), maxLines = 1
                    )

                    Row(
                        modifier = Modifier.constrainAs(row) {
                            top.linkTo(endTime.bottom, margin = 28.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(24.dp))

                        Icon(
                            painter = painterResource(R.drawable.notificationshufflebutton),
                            contentDescription = "Shuffle Icon",
                            tint = if (isShuffle) colorResource(R.color.theme_color) else colorResource(R.color.primary_text_color),
                            modifier = Modifier
                                .size(26.dp)
                                .clickable(
                                    interactionSource = shuffleInteraction,
                                    indication = null
                                ) {
                                    musicService?.shuffleToggle()
                                }
                                .graphicsLayer {
                                    scaleX = shuffleScale
                                    scaleY = shuffleScale
                                }
                        )

                        Spacer(modifier = Modifier.width(38.dp))

                        Icon(
                            painter = painterResource(R.drawable.notificationprevbutton),
                            contentDescription = "Previous Icon",
                            tint = colorResource(R.color.primary_text_color),
                            modifier = Modifier
                                .size(26.dp)
                                .clickable(
                                    interactionSource = prevInteraction,
                                    indication = null
                                ) {
                                    musicService?.previous()
                                }
                                .graphicsLayer {
                                    scaleX = prevScale
                                    scaleY = prevScale
                                }
                        )

                        Spacer(modifier = Modifier.width(34.dp))

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(68.dp)
                                .drawBehind {
                                    val glowRadius = (size.minDimension / 2.5f) * shadowScale
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
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(80.dp))
                                    .background(
                                        if (isPlaying) colorResource(R.color.theme_color)
                                        else colorResource(R.color.secondary_text_color)
                                    )
                                    .clickable(
                                        interactionSource = playInteraction,
                                        indication = null
                                    ) {
                                        musicService?.togglePlayPause()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (isPlaying) R.drawable.notificationpausebutton
                                        else R.drawable.notificationplaybutton
                                    ),
                                    contentDescription = "Play Icon",
                                    tint = colorResource(R.color.background_color),
                                    modifier = Modifier
                                        .padding(start = if (isPlaying) 0.dp else 1.dp)
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX = playScale
                                            scaleY = playScale
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(34.dp))

                        Icon(
                            painter = painterResource(R.drawable.notificationnextbutton),
                            contentDescription = "Next Icon",
                            tint = colorResource(R.color.primary_text_color),
                            modifier = Modifier
                                .size(26.dp)
                                .clickable(
                                    interactionSource = nextInteraction,
                                    indication = null
                                ) {
                                    musicService?.next()
                                }
                                .graphicsLayer {
                                    scaleX = nextScale
                                    scaleY = nextScale
                                }
                        )

                        Spacer(modifier = Modifier.width(38.dp))

                        Icon(
                            painter = painterResource(repeatIcon),
                            contentDescription = "Repeat Icon",
                            tint = tintColor,
                            modifier = Modifier
                                .size(26.dp)
                                .clickable(
                                    interactionSource = repeatInteraction,
                                    indication = null
                                ) {
                                    musicService?.repeatToggle()
                                }
                                .graphicsLayer {
                                    scaleX = repeatScale
                                    scaleY = repeatScale
                                }
                        )

                        Spacer(modifier = Modifier.width(24.dp))
                    }
                }

                if (showShareSheet) {
                    ShareBottomSheet(
                        item = ShareItem(
                            title = htmlToText(currentSong?.name ?: ""),
                            subtitle = currentSong?.artist?.joinToString(", ") { htmlToText(it.name) } ?: "",
                            image = currentSong?.image?.getOrNull(2)?.url,
                            id = currentSong?.id ?: "",
                            type = ShareType.SONG
                        ),
                        onDismiss = { showShareSheet = false }
                    )
                }

                if (showSongSheet && selectedSong != null) {
                    val song = selectedSong!!
                    val isFavourite = likedSongs.contains(song.id)

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

                            PlayerManager.currentPlaylist = upNextList
                            PlayerManager.currentIndex = selectedIndex

                            ContextCompat.startForegroundService(context, intent)
                            showSongSheet = false
                        },
                        isFavourite = isFavourite,
                        onToggleFavourite = {
                            likedViewModel.toggleLike(song)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpNextSheetContent(
    songLists: List<SongItem>,
    currentSongId: String?,
    sheetState: SheetState,
    onMoreClick: (SongItem, Int, List<SongItem>) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val currentIndex = songLists.indexOfFirst { it.id == currentSongId }

    LaunchedEffect(sheetState.currentValue, currentSongId) {
        if (sheetState.currentValue == SheetValue.Expanded &&
            currentIndex != -1
        ) {
            scope.launch {
                listState.animateScrollToItem(
                    index = currentIndex,
                    scrollOffset = -(listState.layoutInfo.viewportSize.height / 3)
                )
            }
        }
    }

    val musicService = ServiceLocator.musicService
    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
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

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Up Next",
            fontSize = 16.sp,
            lineHeight = 18.sp,
            fontFamily = fonts,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
            thickness = 1.dp,
            color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 25.dp)
        ) {
            itemsIndexed(
                items = songLists,
                key = { _: Int, song: SongItem -> song.id }
            ) { index: Int, song: SongItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = currentSongId == song.id,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.music_spectrum)
                        )

                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            isPlaying = isPlaying && currentSongId == song.id,
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
                                        scaleX = 2.4f
                                        scaleY = 2.4f
                                    }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = if (currentSongId == song.id) 0.dp else 24.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
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
                                .size(60.dp)
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
                                fontSize = 14.sp,
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
                                fontSize = 12.sp,
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
                            IconButton(onClick = { }) {
                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    painter = painterResource(R.drawable.download_icon),
                                    contentDescription = "Download",
                                    tint = colorResource(R.color.primary_text_color).copy(alpha = 0.6f)
                                )
                            }

                            IconButton(onClick = {
                                onMoreClick(song, index, songLists)
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
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d : %02d".format(minutes, seconds)
}

@Composable
fun AudioWaveform(
    amplitudes: List<Float>,
    progress: Float,
    onDragStateChanged: (Boolean) -> Unit,
    onProgressChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = colorResource(R.color.theme_color),
    inactiveColor: Color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val displayProgress = if (isDragging) dragProgress else progress

    val animatedProgress by animateFloatAsState(
        targetValue = displayProgress.coerceIn(0f, 1f),
        animationSpec = if (isDragging) snap() else tween(300),
        label = ""
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        isDragging = true
                        onDragStateChanged(true)

                        val width = size.width

                        dragProgress = (down.position.x / width)
                            .coerceIn(0f, 1f)

                        drag(down.id) { change ->
                            dragProgress = (change.position.x / width)
                                .coerceIn(0f, 1f)

                            change.consume()
                        }

                        isDragging = false
                        onDragStateChanged(false)

                        onProgressChanged(dragProgress)
                    }
                }
            }
    ) {
        if (amplitudes.isEmpty()) return@Canvas

        val widthPerBar = size.width / amplitudes.size

        val centerY = size.height / 2

        amplitudes.forEachIndexed { index, amplitude ->

            val barHeight = amplitude * size.height
            val x = index * widthPerBar
            val barProgress = index.toFloat() / amplitudes.size

            val transitionWidth = 0.05f
            val diff = animatedProgress - barProgress

            val blend = when {
                diff > transitionWidth -> 1f
                diff in 0f..transitionWidth -> diff / transitionWidth
                else -> 0f
            }

            val color = lerp(inactiveColor, activeColor, blend)

            drawLine(
                color = color,
                start = Offset(x, centerY - barHeight / 2),
                end = Offset(x, centerY + barHeight / 2),
                strokeWidth = widthPerBar * 0.7f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun pressScale(
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
private fun PlayerActivityScreenPreview() {
    WaveXTheme {
        Player_Activity_Screen()
    }
}