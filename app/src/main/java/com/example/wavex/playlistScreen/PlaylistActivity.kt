package com.example.wavex.playlistScreen

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.example.wavex.albumScreen.ShareType
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.fonts
import com.example.wavex.homeScreen.ParallelDownloader
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.toRecentlyPlayedEntity
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.homeScreen.viewModel.RecentlyPlayedViewModel
import com.example.wavex.playerScreen.PlayerActivityScreen
import com.example.wavex.profileScreen.downloadedSongScreen.DownloadViewModel
import com.example.wavex.profileScreen.downloadedSongScreen.rememberNetworkState
import com.example.wavex.searchScreen.SearchSource
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ServiceLocator
import com.example.wavex.shareComponent.ShareAlbumPlaylistItem
import com.example.wavex.shareComponent.ShareAlbum_Playlist
import com.example.wavex.shareComponent.ShareSong
import com.example.wavex.shareComponent.ShareSongItem
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

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

        val playlistId = intent.getStringExtra("playlist_id")
        val playlistImageUrl = intent.getStringExtra("playlist_imageUrl")
        val playlistSource = intent.getStringExtra("playlist_source")
        val gradientColors = intent.getIntegerArrayListExtra("playlist_gradient")
        val rectangularImage = intent.getBooleanExtra("rectangular_image", false)

        val gradient = gradientColors?.map { Color(it) } ?: emptyList()
        val playlistTitle = intent.getStringExtra("playlist_title") ?: ""

        setContent {
            WaveXTheme {
                Playlist_Activity(
                    playlistId = playlistId,
                    playlistImageUrl = playlistImageUrl,
                    playlistSource = playlistSource,
                    playlistTitle = playlistTitle,
                    rectangularImage = rectangularImage,
                    gradient = gradient
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Playlist_Activity(
    downloadViewModel: DownloadViewModel = hiltViewModel(),
    playlistId: String?, playlistImageUrl: String?,
    playlistSource: String?,
    playlistTitle: String?,
    gradient: List<Color>,
    rectangularImage: Boolean,
    viewModel: PlaylistViewModel = viewModel(),
    recentlyPlayedViewModel: RecentlyPlayedViewModel = hiltViewModel()
)  {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

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

    var invalidSource by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(playlistId, playlistSource) {
        if (playlistId.isNullOrBlank()) {
            invalidSource = true
            return@LaunchedEffect
        }

        when(playlistSource) {
            SearchSource.JIOSAAVN.name ->
                viewModel.loadPlaylist(playlistId)

            SearchSource.YTMUSIC.name ->
                viewModel.loadYTPlaylist(playlistId)

            "Unknown" ->
                invalidSource = true
        }

        invalidSource = false
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
                                                        "isFavourite" to true,
                                                        "source" to playlistSource
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

                playlists.isError -> {
                    ErrorState(
                        message = playlists.errorMessage,
                        onRetry = {
                            if (!playlistId.isNullOrBlank()) {
                                when(playlistSource) {
                                    SearchSource.JIOSAAVN.name ->
                                        viewModel.loadPlaylist(playlistId)

                                    SearchSource.YTMUSIC.name ->
                                        viewModel.loadYTPlaylist(playlistId)
                                }
                            }
                        }
                    )
                }

                playlistSource == "Unknown" ->
                    invalidSource = true

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
                            contentPadding = PaddingValues(bottom = if (currentSong != null) 80.dp else 15.dp)
                        ) {
                            item {
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
//                                            Icon(
//                                                painter = painterResource(R.drawable.headset_icon),
//                                                contentDescription = "Headset Icon",
//                                                tint = colorResource(R.color.secondary_text_color),
//                                                modifier = Modifier.size(18.dp)
//                                            )

                                            Spacer(modifier = Modifier.width(3.dp))

                                            Text(
                                                text = "${playlists.songCount} Tracks",
                                                fontSize = 13.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Normal,
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

//                                            Icon(
//                                                painter = painterResource(R.drawable.clock_icon),
//                                                contentDescription = "Clock Icon",
//                                                tint = colorResource(R.color.secondary_text_color),
//                                                modifier = Modifier.size(18.dp)
//                                            )

                                            Spacer(modifier = Modifier.width(3.dp))

                                            Text(
                                                text = formatTotalDuration(playlists.totalDuration),
                                                fontSize = 13.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Normal,
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
                                                        interactionSource = interactionSource,
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

                                                    recentlyPlayedViewModel.onSongPlayed(
                                                        song.toRecentlyPlayedEntity()
                                                    )
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
                                                        fontWeight = FontWeight.SemiBold,
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
                                                        val qualityIndex = musicService?.downloadQualityIndex
                                                        val url = song.downloadUrl[qualityIndex ?: 4].url

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

                        Box(
                            modifier = Modifier.constrainAs(miniPlayer) {
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                            }.fillMaxWidth().padding(bottom = 5.dp)
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

            if (invalidSource) {
                ErrorState(
                    message = "Invalid album source",
                    onRetry = {
                        when (playlistSource) {
                            SearchSource.JIOSAAVN.name -> {
                                viewModel.loadPlaylist(playlistId ?: "")
                                invalidSource = false
                            }

                            SearchSource.YTMUSIC.name -> {
                                viewModel.loadYTPlaylist(playlistId ?: "")
                                invalidSource = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: SongItem,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onDismiss: () -> Unit,
    onPlayNow: () -> Unit,
    isFavourite: Boolean,
    isDownloaded: Boolean,
    onToggleFavourite: (SongItem) -> Unit,
    onToggleDownload: (SongItem) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val isOnline = rememberNetworkState()
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
                isPlaying = isPlaying,
                isCurrentSong = isCurrentSong,
                onPlayNow,
                isFavourite,
                isDownloaded,
                isOnline = isOnline,
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
                                            putExtra("artist_source",
                                                when(song.songSource) {
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
        ShareSong(
            song = ShareSongItem(
                title = htmlToText(song.name),
                subtitle = song.album?.name ?: "Unknown",
                artists = song.artist.joinToString(", ") { htmlToText(it.name) },
                image = when (song.songSource) {
                    SearchSource.YTMUSIC.name ->
                        song.image.getOrNull(0)?.url

                    SearchSource.JIOSAAVN.name ->
                        song.image.getOrNull(2)?.url
                            ?: song.image.lastOrNull()?.url

                    else ->
                        song.image.lastOrNull()?.url
                },
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
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onPlayNow: () -> Unit,
    isFavourite: Boolean,
    isDownloaded: Boolean,
    isOnline: Boolean,
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

    var startAnimation by remember { mutableStateOf(false) }

    val shadowAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.8f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowAlpha"
    )

    val shadowBlur by animateFloatAsState(
        targetValue = if (startAnimation) 60f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowBlur"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }

    ParallelDownloader.downloadStates[song.id]

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
                .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(
                        when (song.songSource) {
                            SearchSource.YTMUSIC.name ->
                                song.image.getOrNull(0)?.url

                            SearchSource.JIOSAAVN.name ->
                                song.image.getOrNull(2)?.url
                                    ?: song.image.lastOrNull()?.url

                            else ->
                                song.image.lastOrNull()?.url
                        }
                    )
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
                            val frameworkPaint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = shadowColor.copy(alpha = shadowAlpha).toArgb()

                                maskFilter = if (shadowBlur > 0f) {
                                    android.graphics.BlurMaskFilter(
                                        safeBlur,
                                        android.graphics.BlurMaskFilter.Blur.NORMAL
                                    )
                                } else {
                                    null
                                }
                            }

                            canvas.nativeCanvas.drawRoundRect(
                                0f,
                                0f,
                                size.width,
                                size.height,
                                cornerRadius,
                                cornerRadius,
                                frameworkPaint
                            )
                        }
                    }
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = htmlToText(song.name), maxLines = 2,
                    fontSize = 22.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.album_icon),
                        contentDescription = "Album Icon",
                        tint = colorResource(R.color.secondary_text_color),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Album • ${htmlToText(song.album?.name ?: "Unknown")}",
                        fontSize = 13.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.secondary_text_color), lineHeight = 16.sp, maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.headset_icon),
                        contentDescription = "Headset Icon",
                        tint = colorResource(R.color.secondary_text_color),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "PlayCount • ${formatCount(song.playCount.toLong())}",
                        fontSize = 13.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.secondary_text_color), lineHeight = 16.sp, maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            thickness = 1.dp,
            color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        SheetOptionItem(
            icon = if (isCurrentSong && isPlaying)
                R.drawable.notificationpausebutton
            else
                R.drawable.notificationplaybutton,
            text = if (isCurrentSong && isPlaying)
                "Pause"
            else
                "Play Now"
        ) {
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
            icon = R.drawable.queue_icon,
            text = when {
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

        val state = ParallelDownloader.downloadStates[song.id]

        SheetOptionItem(
            icon = when {
                isDownloaded -> R.drawable.downloaded_icon
                state == ParallelDownloader.DownloadState.DOWNLOADING -> R.drawable.download_icon
                else -> R.drawable.download_icon
            },
            text = when {
                isDownloaded -> "Remove From Download"
                state == ParallelDownloader.DownloadState.DOWNLOADING -> "Download in Progress"
                state == ParallelDownloader.DownloadState.PAUSED -> "Resume Download"
                state == ParallelDownloader.DownloadState.FAILED -> "Retry Download"
                else -> "Download"
            }
        ) {
            when {
                isDownloaded -> {
                    onToggleDownload(song)
                    onShowSnackBar("Removed from downloads")
                }

                state == ParallelDownloader.DownloadState.DOWNLOADING -> {
                    onShowSnackBar("Song is already downloading")
                }

                state == ParallelDownloader.DownloadState.PAUSED -> {
                    onToggleDownload(song) // resume
                }

                state == ParallelDownloader.DownloadState.FAILED -> {
                    onToggleDownload(song) // retry
                }

                else -> {
                    onToggleDownload(song) // start
                    onShowSnackBar("Downloading started")
                }
            }
        }

        SheetOptionItem(
            icon = R.drawable.mic_icon,
            text = "View Artist"
        ) {
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
                    putExtra("album_source",
                        when(song.songSource) {
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
            } else {
                onShowSnackBar("No internet connection")
            }
        }

        SheetOptionItem(
            icon = R.drawable.share_icon,
            text = "Share"
        ) {
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

        Spacer(modifier = Modifier.height(0.dp))

        Text(
            text = message,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
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

fun formatTotalDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return buildString {
        if (hours > 0) append("$hours h ")
        if (minutes > 0) append("$minutes min")
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