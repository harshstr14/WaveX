package com.example.wavex.libraryScreen.likedSongsScreen

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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.fonts
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
import com.example.wavex.profileScreen.favouriteSongsScreen.FavouriteSongViewModel
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch

class LikedSongsActivity : ComponentActivity() {
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
                Liked_Songs_Activity()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Liked_Songs_Activity(
    viewModel: FavouriteSongViewModel = viewModel()
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsStateWithLifecycle()

    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val songsList by viewModel.songs.collectAsStateWithLifecycle()

    val (backInteraction, backScale) = pressScale()
    val (shareInteraction, shareScale) = pressScale()
    val interactionSource = remember { MutableInteractionSource() }

    val rawProgress by remember {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
            (offset / 600f).coerceIn(0f, 1f)
        }
    }

    val smoothProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ImageCollapseSpring"
    )

    val startSize = 160.dp
    val startOffsetX = 24.dp
    val startOffsetY = 0.dp

    val endSize = 88.dp
    val endOffsetX = 12.dp
    val endOffsetY = 0.dp

    val size = lerpDp(startSize, endSize, smoothProgress)
    val offsetX = lerpDp(startOffsetX, endOffsetX, smoothProgress)
    val offsetY = lerpDp(startOffsetY, endOffsetY, smoothProgress)

    val cornerRadius = lerpDp(16.dp, 14.dp, smoothProgress)
    val metaAlpha = (1f - smoothProgress * 1.3f).coerceIn(0f, 1f)
    val titleTopPadding = lerpDp(12.dp, 0.dp, smoothProgress)
    val titleStartPadding = lerpDp(25.dp, 8.dp, smoothProgress)
    val titleFontSize = lerpDp(20.dp, 18.dp, smoothProgress)

    val isTitleVisible = !songsList.isLoading && !songsList.isError

    var showSheet by remember { mutableStateOf(false) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)).
        nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
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
            }
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
                        Icon(painter = painterResource(when {
                            data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
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
                songsList.isLoading -> {
                    LoadingEffect()
                }

                songsList.isError -> {
                    ErrorState(
                        message = songsList.errorMessage,
                    )
                }

                else -> {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
                            .background(colorResource(R.color.background_color))
                    ) {
                        val (contentList) = createRefs()

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.constrainAs(contentList) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                height = Dimension.fillToConstraints
                            },
                            contentPadding = PaddingValues(bottom = 25.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                                ) {
                                    AsyncImage(
                                        model = R.drawable.liked,
                                        contentDescription = "Playlist Image",
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(R.drawable.default_image),
                                        modifier = Modifier
                                            .offset(x = offsetX, y = offsetY)
                                            .size(size)
                                            .clip(RoundedCornerShape(cornerRadius))
                                            .graphicsLayer {
                                                alpha = metaAlpha
                                            }.zIndex(10f)
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 15.dp)
                                            .animateContentSize()
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(
                                                top = titleTopPadding,
                                                start = titleStartPadding,
                                                end = 10.dp
                                            ),
                                            text = "Liked Songs",
                                            fontSize = titleFontSize.value.sp,
                                            lineHeight = 22.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            modifier = Modifier.padding(
                                                top = 15.dp,
                                                start = titleStartPadding
                                            )
                                                .graphicsLayer {
                                                    alpha = metaAlpha
                                                    scaleX = 1f - smoothProgress * 0.04f
                                                    scaleY = 1f - smoothProgress * 0.04f
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.headset_icon),
                                                contentDescription = "Headset Icon",
                                                tint = colorResource(R.color.secondary_text_color),
                                                modifier = Modifier.size(18.dp)
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "${likedSongs.size} Songs",
                                                fontSize = 12.sp,
                                                lineHeight = 12.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.padding(
                                                top = 4.dp,
                                                start = titleStartPadding
                                            )
                                                .graphicsLayer {
                                                    alpha = metaAlpha
                                                    scaleX = 1f - smoothProgress * 0.04f
                                                    scaleY = 1f - smoothProgress * 0.04f
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.airpods_icon),
                                                contentDescription = "Time Icon",
                                                tint = colorResource(R.color.secondary_text_color),
                                                modifier = Modifier.size(18.dp)
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = formatTotalDuration(totalDuration),
                                                fontSize = 12.sp,
                                                lineHeight = 12.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
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
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier.width(158.dp).padding(top = 25.dp)
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(colorResource(R.color.theme_color))
                                            .clickable { }
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
                                                modifier = Modifier.size(24.dp)
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "Shuffle",
                                                fontSize = 16.sp,
                                                lineHeight = 18.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.background_color)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(18.dp))

                                    Box(
                                        modifier = Modifier.width(158.dp).padding(top = 25.dp)
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(
                                                colorResource(R.color.secondary_text_color).copy(
                                                    alpha = 0.2f
                                                )
                                            )
                                            .clickable { }
                                            .padding(horizontal = 24.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.play_icon),
                                                contentDescription = "Play Icon",
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(22.dp)
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "Play",
                                                fontSize = 16.sp,
                                                lineHeight = 18.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.theme_color)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    modifier = Modifier.padding(
                                        top = 15.dp,
                                        start = 24.dp,
                                        bottom = 10.dp
                                    ),
                                    text = "Songs",
                                    fontSize = 18.sp,
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color),
                                    lineHeight = 20.sp
                                )
                            }

                            if (songsList.songs.isEmpty()) {
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
                                items = songsList.songs
                            ) { index, song ->

                                Row (
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(start = 24.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {
                                            val intent = Intent(context,MusicPlayerService::class.java
                                            ).apply {
                                                action = MusicPlayerService.ACTION_PLAY_NEW
                                                putExtra("index", index)
                                            }

                                            PlayerManager.currentPlaylist = songsList.songs
                                            PlayerManager.currentIndex = index

                                            ContextCompat.startForegroundService(context, intent)

                                            scope.launch {
                                                RecentlyPlayedManager.add(context, song)
                                            }
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = song.image.getOrNull(2)?.url,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
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
                                        IconButton(onClick = { }) {
                                            Icon(
                                                modifier = Modifier.size(22.dp),
                                                painter = painterResource(R.drawable.download_icon),
                                                contentDescription = "Download",
                                                tint = colorResource(R.color.primary_text_color).copy(alpha = 0.6f)
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

                        if (showSheet && selectedSong != null) {
                            val song = selectedSong!!
                            val isFavourite = likedSongs.contains(song.id)

                            SongOptionsBottomSheet(
                                song = song,
                                onDismiss = {
                                    showSheet = false
                                    selectedSong = null
                                },
                                onPlayNow = {
                                    val intent = Intent(context, MusicPlayerService::class.java).apply {
                                        action = MusicPlayerService.ACTION_PLAY_NEW
                                        putExtra("index", selectedIndex)
                                    }

                                    PlayerManager.currentPlaylist  = songsList.songs
                                    PlayerManager.currentIndex = selectedIndex

                                    ContextCompat.startForegroundService(context, intent)
                                    showSheet = false
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
    }
}

@Composable
private fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
    return start + (end - start) * fraction
}

private fun formatTotalDuration(totalSeconds: Int?): String {

    val safeSeconds = totalSeconds ?: 0

    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60

    return buildString {
        if (hours > 0) append("$hours h ")
        if (minutes > 0) append("$minutes min ")
        if (seconds > 0) append("$seconds s")
        if (isEmpty()) append("0 s")
    }.trim()
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

@Composable
private fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier
            .fillMaxWidth()
            .size(144.dp)
    )
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes (R.raw.spaceman)
        )

        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(134.dp)
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
fun LikedSongsActivityPreview() {
    WaveXTheme {
        Liked_Songs_Activity()
    }
}