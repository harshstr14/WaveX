package com.example.wavex.playlistScreen

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.MiniPlayer
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
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.downloadSong
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.playerScreen.PlayerActivityScreen
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ServiceLocator
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.getValue

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

        val playlistId = intent.getStringExtra("playlist_id")
        val playlistImageUrl = intent.getStringExtra("playlist_imageUrl")

        val downloadViewModel: DownloadViewModel by viewModels {
            DownloadViewModelFactory(AppContainer.downloadRepository)
        }

        setContent {
            WaveXTheme {
                Playlist_Activity(downloadViewModel, playlistId, playlistImageUrl)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Playlist_Activity(
    downloadViewModel: DownloadViewModel,
    playlistId: String?, playlistImageUrl: String?,
    viewModel: PlaylistViewModel = viewModel()
)  {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val downloadedIds by downloadViewModel
        .downloadedSongIds
        .collectAsState(initial = emptySet())

    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()

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

    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("Playlists").child(playlistId.toString())

    LaunchedEffect(playlistId) {
        playlistId?.let {
            viewModel.loadPlaylist(it)
        }
    }

    var isLiked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        favouriteReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLiked = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HeartPop"
    )

    val (backInteraction, backScale) = pressScale()
    val (shareInteraction, shareScale) = pressScale()

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

    val quality = musicService?.qualityIndex

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
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        favouriteReference.addListenerForSingleValueEvent(object : ValueEventListener {

                                            override fun onDataChange(snapshot: DataSnapshot) {

                                                if (!snapshot.exists()) {
                                                    val playlistData = mapOf(
                                                        "playlistId" to playlistId,
                                                        "playlistName" to playlists.name,
                                                        "playlistImageUrl" to imageToLoad,
                                                        "isFavourite" to true
                                                    )

                                                    favouriteReference.setValue(playlistData)
                                                        .addOnSuccessListener {
                                                            isLiked = true
                                                            scope.launch {
                                                                snackBarHostState.showSnackbar(
                                                                    message = "Added To Favourite",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            scope.launch {
                                                                snackBarHostState.showSnackbar(
                                                                    message = "Failed To Add in Favourite",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }

                                                } else {

                                                    favouriteReference.removeValue()
                                                        .addOnSuccessListener {
                                                            isLiked = false
                                                            scope.launch {
                                                                snackBarHostState.showSnackbar(
                                                                    message = "Removed From Favourite",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                scope.launch {
                                                    snackBarHostState.showSnackbar("Database Error: ${error.message}")
                                                }
                                            }
                                        })
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(if (isLiked) R.drawable.heart_filled else R.drawable.heart_outline),
                                    contentDescription = "Heart Icon",
                                    tint = if (isLiked)
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

                playlists.isError -> {
                    ErrorState(
                        message = playlists.errorMessage,
                        onRetry = {
                            playlistId?.let { viewModel.loadPlaylist(it) }
                        }
                    )
                }

                else -> {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize().background(colorResource(R.color.background_color))
                    ) {
                        val (contentList, miniPlayer) = createRefs()

                        LazyColumn (
                            state = listState,
                            modifier = Modifier.constrainAs(contentList){
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                height = Dimension.fillToConstraints
                            },
                            contentPadding = PaddingValues(bottom = if (currentSong != null) 95.dp else 25.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                                ) {
                                    AsyncImage(
                                        model = imageToLoad,
                                        contentDescription = "Playlist Image",
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(R.drawable.default_image),
                                        modifier = Modifier
                                            .offset(x = offsetX, y = offsetY)
                                            .size(size)
                                            .clip(RoundedCornerShape(cornerRadius))
                                            .graphicsLayer {
                                                alpha = metaAlpha
                                            }
                                            .zIndex(10f)
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 15.dp)
                                            .animateContentSize()
                                    ) {
                                        val playlistName = htmlToText(playlists.name)

                                        Text(
                                            modifier = Modifier.padding(top = titleTopPadding,start = titleStartPadding, end = 10.dp),
                                            text = playlistName,
                                            fontSize = titleFontSize.value.sp,
                                            lineHeight = 22.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        val description = htmlToText(playlists.description)

                                        Text(
                                            modifier = Modifier.padding(top = 6.dp,start = titleStartPadding, end = 10.dp),
                                            text = description,
                                            fontSize = 12.sp,
                                            lineHeight = 14.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.secondary_text_color),
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            modifier = Modifier.padding(top = 8.dp, start = titleStartPadding)
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
                                                text = "${playlists.songCount} Songs",
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
                                            modifier = Modifier.padding(top = 4.dp, start = titleStartPadding)
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
                                                text = formatTotalDuration(playlists.totalDuration),
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
                                            .clickable(
                                                interactionSource = interactionSource,
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
                                                fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.background_color)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(18.dp))

                                    Box(
                                        modifier = Modifier.width(158.dp).padding(top = 25.dp)
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                PlayerManager.currentPlaylist = playlists.songs
                                                ServiceLocator.musicService?.setPlaylist(playlists.songs, 0)
                                            }
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
                                                fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.theme_color)
                                            )
                                        }
                                    }
                                }
                            }

                            if (playlists.artists.isNotEmpty()) {
                                item {
                                    Text(
                                        modifier = Modifier.padding(top = 15.dp, start = 24.dp),
                                        text = "Artists", fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                    )
                                }

                                val uniqueArtists = playlists.artists.distinctBy { it.id }

                                item {
                                    LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 15.dp),
                                        contentPadding = PaddingValues(horizontal = 24.dp),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        items(uniqueArtists) { artist ->
                                            Column(
                                                modifier = Modifier
                                                    .clickable(
                                                        interactionSource = interactionSource,
                                                        indication = null
                                                    ) {
                                                        val intent = Intent(context, ArtistActivity::class.java).apply {
                                                            putExtra("artist_id", artist.id)
                                                            putExtra("artist_imageUrl", artist.image)
                                                        }
                                                        context.startActivity(intent)
                                                    },
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                AsyncImage(
                                                    model = artist.image.takeIf { it.isNotBlank() },
                                                    contentDescription = artist.name,
                                                    contentScale = ContentScale.Crop,
                                                    error = painterResource(R.drawable.default_artist),
                                                    modifier = Modifier
                                                        .size(78.dp)
                                                        .clip(CircleShape)
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                val artistName = htmlToText(artist.name)

                                                Text(
                                                    modifier = Modifier.width(78.dp),
                                                    text = artistName,
                                                    fontSize = 13.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.primary_text_color), maxLines = 2, textAlign = TextAlign.Center,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (playlists.songs.isNotEmpty()) {
                                item {
                                    Text(
                                        modifier = Modifier.padding(top = 15.dp, start = 24.dp, bottom = 10.dp),
                                        text = "Songs", fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                    )
                                }

                                val uniqueSongs = playlists.songs.distinctBy { it.id }

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
                                                .padding(start = if (currentSong?.id == song.id) 0.dp else 24.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
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
                            }
                        }

                        if (showShareSheet) {
                            ShareBottomSheet(
                                item = ShareItem(
                                    title = htmlToText(playlists.name),
                                    subtitle = playlists.artists.joinToString(", ") {htmlToText(it.name)},
                                    image = imageToLoad,
                                    id = playlists.id,
                                    type = ShareType.PLAYLIST
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
                                onDismiss = {
                                    showSongSheet = false
                                    selectedSong = null
                                },
                                onPlayNow = {
                                    val intent = Intent(context, MusicPlayerService::class.java).apply {
                                        action = MusicPlayerService.ACTION_PLAY_NEW
                                        putExtra("index", selectedIndex)
                                    }

                                    PlayerManager.currentPlaylist = playlists.songs
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
                            }.fillMaxWidth().padding(bottom = 20.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: SongItem,
    onDismiss: () -> Unit,
    onPlayNow: () -> Unit,
    isFavourite: Boolean,
    isDownloaded: Boolean,
    onToggleFavourite: (SongItem) -> Unit,
    onToggleDownload: (SongItem) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showArtistsDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val artistsGridState = rememberLazyGridState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val playlistViewModel: com.example.wavex.libraryScreen.PlaylistViewModel = viewModel()
    val playlistsList by playlistViewModel.playlists.collectAsStateWithLifecycle()

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
                onToggleFavourite,
                onAddToPlaylistClick = {
                    showPlaylistDialog = true
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
                                    data.visuals.message.contains("downloads") -> R.drawable.download_icon
                                    data.visuals.message.contains("Downloading") -> R.drawable.downloaded_icon
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
                                    val intent = Intent(context, ArtistActivity::class.java).apply {
                                        putExtra("artist_id", artist.id)
                                        putExtra("artist_imageUrl", artist.image)
                                    }
                                    context.startActivity(intent)
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

    if (showPlaylistDialog) {
        Dialog(onDismissRequest = { showPlaylistDialog = false }) {
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
                            painter = painterResource(R.drawable.playlist_icon),
                            contentDescription = null,
                            tint = colorResource(R.color.theme_color),
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "Select Playlist",
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
                        thickness = 1.2.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(
                            items = playlistsList.playlists,
                            key = { it.playlistId }
                        ) { playlist ->
                            Text(
                                text = playlist.playlistName,
                                fontFamily = fonts,
                                fontSize = 14.sp, lineHeight = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.secondary_text_color),
                                maxLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showPlaylistDialog = false
                                        playlistViewModel.addSongToPlaylist(
                                            playlistId = playlist.playlistId,
                                            song = song
                                        ) { success, message ->
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSheetContent(
    song: SongItem,
    onPlayNow: () -> Unit,
    isFavourite: Boolean,
    isDownloaded: Boolean,
    onToggleFavourite: (SongItem) -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onShowArtistsClick: () -> Unit,
    onShowShareSheet: () -> Unit,
    onShowSnackBar: (String) -> Unit,
    onToggleDownload: (SongItem) -> Unit
) {
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService

    val queue by musicService?.queueFlow?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val isInQueue = queue.any { it.id == song.id }

    val playlist by musicService?.playlistFlow?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val isInPlaylist = playlist.any { it.id == song.id }

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
            isAnimated = isFavourite)
        {
            onToggleFavourite(song)
        }

        //SheetOptionItem(R.drawable.next_icon, "Play Next")
        SheetOptionItem(R.drawable.add_playlist_icon, "Add to Playlist") {
            onAddToPlaylistClick()
        }

        SheetOptionItem(
            R.drawable.queue_icon,
            when {
                isInQueue -> "Remove from queue"
                else -> "Add to queue"
            }
        ) {
            when {
                isInQueue -> {
                    musicService?.removeFromQueue(song.id)
                    onShowSnackBar("Removed from queue")
                }

                isInPlaylist -> {
                    onShowSnackBar("Song already in playlist")
                }

                else -> {
                    musicService?.addToQueue(song)
                    onShowSnackBar("Added to queue")
                }
            }
        }

        SheetOptionItem(
            icon = if (isDownloaded) R.drawable.downloaded_icon else R.drawable.download_icon,
            text = if (isDownloaded) "Remove From Download" else "Download")
        {
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

        SheetOptionItem(R.drawable.album_icon, "Go to Album") {
            val intent = Intent(context, AlbumActivity::class.java).apply {
                putExtra("album_id", song.album?.id)
                putExtra("album_imageUrl", "")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
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
private fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
    return start + (end - start) * fraction
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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

private fun formatTotalDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("$hours h ")
        if (minutes > 0) append("$minutes min  ")
        if (seconds > 0) append("$seconds s")
        if (isEmpty()) append("0s") // handle 0 case
    }.trim()
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
private fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.fillMaxWidth().size(144.dp)
    )
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

@Preview(showBackground = true)
@Composable
private fun PlaylistActivityPreview() {
    WaveXTheme {

    }
}