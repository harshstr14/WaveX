package com.example.wavex.artistScreen

import android.app.Activity
import android.content.Context
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
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import coil.ImageLoader
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
import com.example.wavex.albumScreen.ShareItem
import com.example.wavex.albumScreen.ShareType
import com.example.wavex.albumScreen.darkenColor
import com.example.wavex.albumScreen.generateShareLink
import com.example.wavex.albumScreen.slightlyDarken
import com.example.wavex.artistScreen.allAlbumsScreen.AllAlbumsActivity
import com.example.wavex.artistScreen.allSongsScreen.AllSongsActivity
import com.example.wavex.downloadSong.viewmodel.DownloadViewModel
import com.example.wavex.downloadSong.viewmodel.DownloadViewModelFactory
import com.example.wavex.fonts
import com.example.wavex.homeScreen.AppContainer
import com.example.wavex.homeScreen.ParallelDownloader
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.playerScreen.PlayerActivityScreen
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
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

class ArtistActivity : ComponentActivity() {
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
        val artistImageUrl = intent.getStringExtra("artist_imageUrl")

        val downloadViewModel: DownloadViewModel by viewModels {
            DownloadViewModelFactory(AppContainer.downloadRepository)
        }

        setContent {
            WaveXTheme {
                Artist_Activity(downloadViewModel, artistId, artistImageUrl)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Artist_Activity(
    downloadViewModel: DownloadViewModel,
    artistId: String?, artistImageUrl: String?,
    viewModel: ArtistViewModel = viewModel()
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val downloadedIds by downloadViewModel
        .downloadedSongIds
        .collectAsState(initial = emptySet())

    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()

    val imageToLoad = if (artistImageUrl.isNullOrBlank()) {
        artists.imageUrl
    } else {
        artistImageUrl
    }

    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity

    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("Artists").child(artistId.toString())

    LaunchedEffect(artistId) {
        artistId?.let {
            viewModel.loadArtist(it)
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

    val titleVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > 180
        }
    }

    val startSize = 120.dp
    val startOffsetX = 25.dp
    val startOffsetY = 0.dp

    val endSize = 48.dp
    val endOffsetX = 12.dp
    val endOffsetY = 0.dp

    val size = lerpDp(startSize, endSize, smoothProgress)
    val offsetX = lerpDp(startOffsetX, endOffsetX, smoothProgress)
    val offsetY = lerpDp(startOffsetY, endOffsetY, smoothProgress)

    val cornerRadius = lerpDp(60.dp, 60.dp, smoothProgress)
    val showMetaInfo = smoothProgress < 0.3f
    val metaAlpha = (1f - smoothProgress * 1.3f).coerceIn(0f, 1f)
    val titleTopPadding = lerpDp(15.dp, 0.dp, smoothProgress)
    val titleStartPadding = lerpDp(25.dp, 8.dp, smoothProgress)
    val titleFontSize = lerpDp(20.dp, 18.dp, smoothProgress)
    val iconScale = lerpDp(1.dp, 0.95.dp, smoothProgress).value
    val titleOffsetY = lerpDp((-12).dp, 0.dp, smoothProgress)

    val isTitleVisible = !isLoading && !artists.isError

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
                        AnimatedVisibility(
                            visible = titleVisible,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 220,
                                    easing = FastOutSlowInEasing
                                )
                            ) + scaleIn(
                                initialScale = 0.95f,
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = FastOutSlowInEasing
                                )
                            ),

                            exit = fadeOut(
                                animationSpec = tween(
                                    durationMillis = 160,
                                    easing = LinearOutSlowInEasing
                                )
                            ) + scaleOut(
                                targetScale = 0.92f,
                                animationSpec = tween(
                                    durationMillis = 160,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = imageToLoad,
                                    contentDescription = "Artist Image",
                                    contentScale = ContentScale.Crop,
                                    modifier =  Modifier.padding(start = 8.dp)
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(cornerRadius))
                                        .graphicsLayer {
                                            shadowElevation = 8f * (1f - smoothProgress)
                                        }
                                        .zIndex(10f)
                                )

                                Row(
                                    modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = artists.name,
                                        fontSize = 18.sp,
                                        lineHeight = 20.sp,
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.primary_text_color),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (artists.isVerified) {
                                        Icon(
                                            painter = painterResource(R.drawable.verified_icon),
                                            contentDescription = "Verified Icon",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(20.dp)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        Row(modifier = Modifier.padding(end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically) {
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
                                                    val artistData = mapOf(
                                                        "artistId" to artistId,
                                                        "artistName" to artists.name,
                                                        "artistImageUrl" to imageToLoad,
                                                        "isFavourite" to true
                                                    )

                                                    favouriteReference.setValue(artistData)
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

                artists.isError -> {
                    ErrorState(
                        message = artists.errorMessage,
                        onRetry = {
                            artistId?.let { viewModel.loadArtist(it) }
                        }
                    )
                }

                else -> {
                    ConstraintLayout(modifier = Modifier.fillMaxSize().background(colorResource(R.color.background_color))) {
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
                            contentPadding = PaddingValues(bottom = if (currentSong != null) 80.dp else 5.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = imageToLoad,
                                        contentDescription = "Artist Image",
                                        contentScale = ContentScale.Crop,
                                        modifier =  Modifier
                                            .offset(x = offsetX, y = offsetY)
                                            .size(size)
                                            .clip(RoundedCornerShape(cornerRadius))
                                            .graphicsLayer {
                                                alpha = metaAlpha
                                            }
                                            .zIndex(10f)
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                                        .offset(y = titleOffsetY).animateContentSize()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(top = titleTopPadding, start = titleStartPadding),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = htmlToText(artists.name),
                                                fontSize = titleFontSize.value.sp,
                                                lineHeight = 22.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            if (artists.isVerified) {
                                                Icon(
                                                    painter = painterResource(R.drawable.verified_icon),
                                                    contentDescription = "Verified Icon",
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(20.dp)
                                                        .graphicsLayer {
                                                            scaleX = iconScale
                                                            scaleY = iconScale
                                                        }
                                                )
                                            }
                                        }

                                        if (showMetaInfo) {
                                            Row(
                                                modifier = Modifier.padding(top = 12.dp, start = 25.dp)
                                                    .graphicsLayer {
                                                        alpha = metaAlpha
                                                        scaleX = 1f - smoothProgress * 0.04f
                                                        scaleY = 1f - smoothProgress * 0.04f
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.followers_icon),
                                                    contentDescription = "Followers Icon",
                                                    tint = colorResource(R.color.primary_text_color),
                                                    modifier = Modifier.size(16.dp)
                                                )

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Text(
                                                    text = "Followers : ${
                                                        formatCount(artists.followerCount.toLong())
                                                    }",
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
                                                modifier = Modifier.padding(top = 4.dp, start = 25.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.headset_icon),
                                                    contentDescription = "Headset Icon",
                                                    tint = colorResource(R.color.primary_text_color),
                                                    modifier = Modifier.size(16.dp)
                                                )

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Text(
                                                    text = "Listeners : ${
                                                        formatCount(artists.fanCount.toLongOrNull() ?: 0L)
                                                    }",
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
                            }

                            if (artists.topSongs.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Top Songs", fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                        )

                                        Text(
                                            modifier = Modifier.clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                val intent = Intent(context, AllSongsActivity::class.java).apply {
                                                    putExtra("artist_id", artists.id)
                                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                                }
                                                context.startActivity(intent)
                                            }, text = "See All", fontSize = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.theme_color), lineHeight = 18.sp
                                        )
                                    }
                                }

                                itemsIndexed(
                                    artists.topSongs,
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

                                                    PlayerManager.currentPlaylist = artists.topSongs
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

                            if (artists.topAlbums.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 24.dp, end = 24.dp, top = 15.dp, bottom = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Top Albums", fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                        )

                                        Text(
                                            modifier = Modifier.clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                val intent = Intent(context, AllAlbumsActivity::class.java).apply {
                                                    putExtra("artist_id", artists.id)
                                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                                }
                                                context.startActivity(intent)
                                            }, text = "See All", fontSize = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.theme_color), lineHeight = 18.sp
                                        )
                                    }
                                }

                                item {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(top = 10.dp),
                                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp),
                                        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                        items(artists.topAlbums) { album ->
                                            Column(
                                                modifier = Modifier
                                                    .width(110.dp)
                                                    .clickable(
                                                        interactionSource = interactionSource,
                                                        indication = null
                                                    ) {
                                                        val intent = Intent(context, AlbumActivity::class.java).apply {
                                                            putExtra("album_id", album.id)
                                                            putExtra("album_imageUrl", album.image[2].url)
                                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                        }
                                                        context.startActivity(intent)
                                                    }
                                            ) {
                                                AsyncImage(
                                                    model = album.image.getOrNull(2)?.url,
                                                    contentDescription = album.name,
                                                    contentScale = ContentScale.Crop,
                                                    error = painterResource(R.drawable.default_image),
                                                    modifier = Modifier
                                                        .height(110.dp)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                                                    text = album.name,
                                                    fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.primary_text_color), maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                val artistsList = album.artist
                                                    .takeIf { it.isNotEmpty() }
                                                    ?.joinToString(", ") { it.name }
                                                    ?: "Unknown Artist"

                                                val artistsName = htmlToText(artistsList)

                                                Text(
                                                    modifier = Modifier.padding(horizontal = 2.dp ),
                                                    text = artistsName,
                                                    fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.secondary_text_color), maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (artists.singles.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Singles", modifier = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 5.dp)
                                        , fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                    )
                                }

                                item {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 15.dp),
                                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp),
                                        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                        items(artists.singles) { album ->
                                            Column(
                                                modifier = Modifier
                                                    .width(110.dp)
                                                    .clickable(
                                                        interactionSource = interactionSource,
                                                        indication = null
                                                    ) {
                                                        val intent = Intent(context, AlbumActivity::class.java).apply {
                                                            putExtra("album_id", album.id)
                                                            putExtra("album_imageUrl", album.image[2].url)
                                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                        }
                                                        context.startActivity(intent)
                                                    }
                                            ) {
                                                AsyncImage(
                                                    model = album.image.getOrNull(2)?.url,
                                                    contentDescription = album.name,
                                                    contentScale = ContentScale.Crop,
                                                    error = painterResource(R.drawable.default_image),
                                                    modifier = Modifier
                                                        .height(110.dp)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                                                    text = album.name,
                                                    fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.primary_text_color), maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                val artistsList = album.artist
                                                    .takeIf { it.isNotEmpty() }
                                                    ?.joinToString(", ") { it.name }
                                                    ?: "Unknown Artist"

                                                val artistsName = htmlToText(artistsList)

                                                Text(
                                                    modifier = Modifier.padding(horizontal = 2.dp ),
                                                    text = artistsName,
                                                    fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.secondary_text_color), maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showShareSheet) {
                            ShareBottomSheetArtists(
                                item = ShareItem(
                                    title = htmlToText(artists.name),
                                    subtitle = "Artists",
                                    image = imageToLoad,
                                    id = artists.id,
                                    type = ShareType.ARTIST
                                ),
                                isVerified = artists.isVerified,
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

                                    PlayerManager.currentPlaylist = artists.topSongs
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
                                    val qualityIndex = musicService?.downloadQualityIndex
                                    val url = song.downloadUrl[qualityIndex ?: 4].url
                                    val state = ParallelDownloader.downloadStates[song.id]

                                    when {
                                        isDownloaded -> {
                                            downloadViewModel.deleteSong(song.id)
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheetArtists(
    item: ShareItem,
    isVerified: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.off_white),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ShareContent(
                item = item,
                isVerified = isVerified,
                context = context,
                onShowSnackBar = { message ->
                    scope.launch {
                        snackBarHostState.showSnackbar(message)
                    }
                }
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
                                    data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
                                    data.visuals.message.contains("Link") -> R.drawable.link_icon
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
}

@Composable
fun ShareContent(
    item: ShareItem,
    isVerified: Boolean,
    context: Context,
    onShowSnackBar: (String) -> Unit
) {
    var dominantColor by remember { mutableStateOf(Color(0xFFD32F2F)) }

    val (copyLinkInteraction, copyLinkScale) = pressScale()
    val (whatsAppInteraction, whatsAppScale) = pressScale()
    val (messageInteraction, messageScale) = pressScale()
    val (moreInteraction, moreScale) = pressScale()

    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(item.image) {
        item.image?.let { imageUrl ->
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap

            bitmap?.let {
                val palette = Palette.from(it).generate()

                val lightColor = palette.lightVibrantSwatch?.rgb
                    ?: palette.lightMutedSwatch?.rgb
                    ?: palette.vibrantSwatch?.rgb
                    ?: palette.dominantSwatch?.rgb
                    ?: "#F5F5F5".toColorInt()

                val composeColor = Color(lightColor)

                dominantColor = slightlyDarken(composeColor, 0.35f)
            }
        }
    }

    val darkColor = remember(dominantColor) {
        darkenColor(dominantColor, 0.65f)
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            dominantColor,
            darkColor
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 34.dp)
                .height(420.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorResource(R.color.primary_text_color).copy(alpha = 0.8f))
                    .padding(10.dp)
            ) {
                AsyncImage(
                    model = item.image,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(185.dp)
                        .clip(CircleShape)
                        .align(alignment = Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = htmlToText(item.title),
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontFamily = fonts,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.off_white),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isVerified) {
                        Icon(
                            painter = painterResource(R.drawable.verified_icon),
                            contentDescription = "Verified Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.subtitle,
                    color = colorResource(R.color.off_white).copy(alpha = 0.7f),
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 20.dp)
                        .offset(x = (-18).dp)
                        .clip(RectangleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.wavex_logo_light),
                        contentDescription = "Logo Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(width = 90.dp, height = 20.dp)
                            .graphicsLayer {
                                scaleX = 1.4f
                                scaleY = 1.4f
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.8f))
                        .clickable(
                            interactionSource = copyLinkInteraction,
                            indication = null
                        ) {
                            val link = generateShareLink(item)

                            clipboardManager.setText(AnnotatedString(link))

                            onShowSnackBar("Link copied")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.link_icon),
                        contentDescription = "Link Icon",
                        tint = colorResource(R.color.off_white),
                        modifier = Modifier.size(26.dp)
                            .graphicsLayer {
                                scaleX = copyLinkScale
                                scaleY = copyLinkScale
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Copy link",
                    fontSize = 12.sp, lineHeight = 14.sp,
                    fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color)
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.8f))
                        .clickable(
                            interactionSource = whatsAppInteraction,
                            indication = null
                        ) {
                            val link = generateShareLink(item)

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                this.type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, link)
                                setPackage("com.whatsapp")
                            }

                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                onShowSnackBar("WhatsApp not installed")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.whatsapp_icon),
                        contentDescription = "WhatsApp Icon",
                        tint = colorResource(R.color.off_white),
                        modifier = Modifier.size(32.dp)
                            .graphicsLayer {
                                scaleX = whatsAppScale
                                scaleY = whatsAppScale
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "WhatsApp",
                    fontSize = 12.sp, lineHeight = 14.sp,
                    fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color)
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.8f))
                        .clickable(
                            interactionSource = messageInteraction,
                            indication = null
                        ) {
                            val link = generateShareLink(item)

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "smsto:".toUri()
                                putExtra("sms_body", "Listen to this album on WaveX 🎵\n$link")
                            }

                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.message_icon),
                        contentDescription = "Message Icon",
                        tint = colorResource(R.color.off_white),
                        modifier = Modifier.size(24.dp)
                            .graphicsLayer {
                                scaleX = messageScale
                                scaleY = messageScale
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Text\nMessage",
                    fontSize = 12.sp, lineHeight = 14.sp,
                    fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.primary_text_color)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.8f))
                        .clickable(
                            interactionSource = moreInteraction,
                            indication = null
                        ) {
                            val link = generateShareLink(item)

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                this.type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, link)
                            }

                            context.startActivity(
                                Intent.createChooser(shareIntent, "Share via")
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.three_dots_icon),
                        contentDescription = "More Icon",
                        tint = colorResource(R.color.off_white),
                        modifier = Modifier.size(22.dp)
                            .rotate(90f)
                            .graphicsLayer {
                                scaleX = moreScale
                                scaleY = moreScale
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "More",
                    fontSize = 12.sp, lineHeight = 14.sp,
                    fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
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

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
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

@Preview(showSystemUi = true)
@Composable
private fun Artist_ActivityPreview() {
    WaveXTheme {

    }
}