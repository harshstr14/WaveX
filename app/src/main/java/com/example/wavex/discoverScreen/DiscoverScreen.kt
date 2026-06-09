package com.example.wavex.discoverScreen

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.albumScreen.AlbumActivity
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.discoverScreen.viewModel.ExploreAlbumsViewModel
import com.example.wavex.discoverScreen.viewModel.ExploreArtistsViewModel
import com.example.wavex.discoverScreen.viewModel.ExplorePlaylistsViewModel
import com.example.wavex.discoverScreen.viewModel.ExploreSongsViewModel
import com.example.wavex.fonts
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.playlistScreen.PlaylistActivity
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
import com.example.wavex.pressScale
import com.example.wavex.profileScreen.downloadedSongScreen.DownloadViewModel
import com.example.wavex.profileScreen.settingScreen.AudioStreamQualityPreference
import com.example.wavex.profileScreen.settingScreen.DownloadQualitySelector
import com.example.wavex.searchScreen.SearchSource
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ParallelDownloader
import com.example.wavex.service.ServiceLocator
import kotlinx.coroutines.launch

data class BrowseItem(
    val title: String,
    val subtitle: String,
    val playlistId: String,
    val gradient: List<Color>
)

@Composable
fun DiscoverScreen(
    downloadViewModel: DownloadViewModel,
    qualityPreference: AudioStreamQualityPreference,
    navController: NavController,
    showSheet: Boolean,
    snackBarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()
    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val (backInteraction, backScale) = pressScale()

    var showSongSheet by remember { mutableStateOf(false) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSheet || showSongSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    val downloadedIds by downloadViewModel
        .downloadedSongIds
        .collectAsState(initial = emptySet())

    val musicService = ServiceLocator.musicService

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && animatedBlur > 0f) {
                    renderEffect = RenderEffect
                        .createBlurEffect(
                            animatedBlur,
                            animatedBlur,
                            Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                }
            }
    ) {
        val(backButton, titleText, categoryTabs, contentPager) = createRefs()

        Text(
            text = "Explore",
            modifier = Modifier
                .constrainAs(titleText) {
                    top.linkTo(parent.top, margin = 22.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            fontSize = 20.sp, fontFamily = fonts,
            fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color), lineHeight = 22.sp
        )

        Box(
            modifier = Modifier
                .constrainAs(backButton) {
                    top.linkTo(titleText.top)
                    bottom.linkTo(titleText.bottom)
                    start.linkTo(parent.start, margin = 25.dp)
                }
                .size(36.dp).clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.5.dp,
                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                        interactionSource = backInteraction,
                        indication = null
                ) {
                   navController.popBackStack()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_icon),
                contentDescription = "Back Icon",
                tint = colorResource(R.color.primary_text_color),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        scaleX = backScale
                        scaleY = backScale
                    }
            )
        }

        val categoriesList = listOf(
            "Suggested", "Songs", "Artists", "Albums", "Playlists"
        )

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { categoriesList.size }
        )

        val suggestedGridState = rememberLazyStaggeredGridState()
        val songsListState = rememberLazyListState()
        val artistsGridState = rememberLazyGridState()
        val albumsGridState = rememberLazyGridState()
        val playlistsGridState = rememberLazyGridState()

        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        val selectedBg = colorResource(R.color.theme_color).copy(alpha = 0.95f)
        val unselectedBg = colorResource(R.color.primary_text_color).copy(alpha = 0.85f)

        LazyRow(
            state = listState,
            modifier = Modifier
                .constrainAs(categoryTabs) {
                    top.linkTo(titleText.bottom, margin = 22.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(categoriesList) { index, category ->
                val isSelected by remember {
                    derivedStateOf { pagerState.currentPage == index }
                }

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) selectedBg else unselectedBg,
                    animationSpec = tween(
                        durationMillis = 450,
                        easing = FastOutSlowInEasing
                    ),
                    label = "bg"
                )

                val borderColor by animateColorAsState(
                    targetValue = if (isSelected)
                        Color(0xFF71c287)
                    else
                        colorResource(R.color.secondary_text_color),
                    animationSpec = tween(
                        durationMillis = 450,
                        easing = FastOutSlowInEasing
                    ),
                    label = "border"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected)
                        colorResource(R.color.off_white)
                    else
                        colorResource(R.color.background_color),
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = LinearOutSlowInEasing
                    ),
                    label = "text"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.96f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )

                val elevation by animateDpAsState(
                    targetValue = if (isSelected) 3.dp else 0.dp,
                    animationSpec = tween(400),
                    label = "elevation"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .shadow(
                            elevation = elevation,
                            shape = RoundedCornerShape(50),
                            clip = false
                        )
                        .height(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .border(
                            width = 1.2.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(
                                bounded = true,
                                radius = 28.dp
                            )
                        ) {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = index,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        lineHeight = 16.sp,
                        fontFamily = fonts,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal,
                        fontSize = 13.sp,
                        color = textColor
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            key = { categoriesList[it] },
            flingBehavior = PagerDefaults.flingBehavior(pagerState),
            modifier = Modifier
                .constrainAs(contentPager) {
                    top.linkTo(categoryTabs.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
        ) { page ->
            when(categoriesList[page]) {
                "Suggested" -> {
                    ExploreGrid(
                        modifier = Modifier.fillMaxSize(),
                        gridState = suggestedGridState
                    )
                }

                "Songs" -> {
                    ExploreSongs(
                        playlistId = "946682072", root = "songs",
                        modifier = Modifier.fillMaxSize().padding(top = 3.dp),
                        listState = songsListState,
                        downloadedIds = downloadedIds,
                        onMoreClick = { song, index, songsList ->
                            selectedSong = song
                            selectedIndex = index
                            PlayerManager.currentPlaylist = songsList
                            PlayerManager.currentIndex = index
                            showSongSheet = true
                        },
                        snackBarHostState = snackBarHostState
                    )
                }

                "Artists" -> {
                    ExploreArtists(
                        query = "trending artists", root = "results",
                        modifier = Modifier.fillMaxSize().padding(top = 3.dp), gridState = artistsGridState
                    )
                }

                "Albums" -> {
                    ExploreAlbums(
                        query = "popular", root = "results",
                        modifier = Modifier.fillMaxSize().padding(top = 3.dp), gridState = albumsGridState
                    )
                }

                "Playlists" -> {
                    ExplorePlaylist(
                        query = "Top", root = "results",
                        modifier = Modifier.fillMaxSize().padding(top = 3.dp), gridState = playlistsGridState
                    )
                }
            }
        }

        LaunchedEffect(pagerState.currentPage) {
            val index = pagerState.currentPage
            val visibleItems = listState.layoutInfo.visibleItemsInfo

            val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: 0

            when {
                index == 0 -> {
                    listState.animateScrollToItem(
                        index = 0,
                        scrollOffset = 0
                    )
                }
                index > lastVisibleIndex - 1 -> {
                    listState.animateScrollToItem(
                        index = index,
                        scrollOffset = -40
                    )
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
            isFavourite = isFavourite,
            isDownloaded = isDownloaded,
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
                }

                ContextCompat.startForegroundService(context, intent)
            },
            onToggleFavourite = {
                likedViewModel.toggleLike(
                    song = song
                )
            },
            onToggleDownload = { song ->
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
}

@Composable
fun ExploreGrid(
    modifier: Modifier,
    gridState: LazyStaggeredGridState
) {
    val context = LocalContext.current
    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val exploreList = listOf(
        BrowseItem(
            title = "Made\nFor You",
            subtitle = "fresh picks",
            playlistId = "RDCLAK5uy_n9Fbdw7e6ap-98_A-8JYBmPv64v-Uaq1g",
            gradient = listOf(
                Color(0xFFF9A26C),
                Color(0xFFD63384)
            )
        ),

        BrowseItem(
            title = "New\nReleases",
            subtitle = "this week",
            playlistId = "PLO7-VO1D0_6NmK47v6tpOcxurcxdW-hZa",
            gradient = listOf(
                Color(0xFFFFB347),
                Color(0xFFFF6B35)
            )
        ),

        BrowseItem(
            title = "Top\nCharts",
            subtitle = "India 100",
            playlistId = "PL4fGSI1pDJn6puJdseH2Rt9sMvt9E2M4i",
            gradient = listOf(
                Color(0xFFA18CD1),
                Color(0xFFFBC2EB)
            )
        ),

        BrowseItem(
            title = "Hindi",
            subtitle = "100 songs",
            playlistId = "PL4fGSI1pDJn5RgLW0Sb_zECecWdH_4zOX",
            gradient = listOf(
                Color(0xFFC471ED),
                Color(0xFF6A11CB)
            )
        ),

        BrowseItem(
            title = "Haryanvi",
            subtitle = "100 songs",
            playlistId = "PL4fGSI1pDJn4tiNLMZVGGt2Kghgw__2u0",
            gradient = listOf(
                Color(0xFF434343),
                Color(0xFF5B4BFF)
            )
        ),

        BrowseItem(
            title = "Punjabi",
            subtitle = "100 songs",
            playlistId = "PL4fGSI1pDJn5JXkyIohg2RstsbL2SnRew",
            gradient = listOf(
                Color(0xFFFF9966),
                Color(0xFFFF5E62)
            )
        ),

        BrowseItem(
            title = "Workout",
            subtitle = "gym vibes",
            playlistId = "PLf_ANWupyE3bhSLCyaRhSoRnj3QbOhzXn",
            gradient = listOf(
                Color(0xFFF9A26C),
                Color(0xFFD63384)
            )
        ),

        BrowseItem(
            title = "Romantic",
            subtitle = "love hits",
            playlistId = "RDCLAK5uy_miAacfMxVybbt7ketqqnPPbH9LDn1TavU",
            gradient = listOf(
                Color(0xFFFFB347),
                Color(0xFFFF6B35)
            )
        )
    )

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        verticalItemSpacing = 14.dp,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = if (currentSong != null) 168.dp else 100.dp)
    ) {
        itemsIndexed(exploreList) { index, item ->
            val cardHeight = when (index % 4) {
                0 -> 210.dp
                1 -> 100.dp
                2 -> 220.dp
                else -> 105.dp
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        val intent = Intent(context, PlaylistActivity::class.java).apply {
                            putExtra("playlist_id", item.playlistId)
                            putIntegerArrayListExtra(
                                "playlist_gradient",
                                arrayListOf(
                                    item.gradient[0].toArgb(),
                                    item.gradient[1].toArgb()
                                )
                            )
                            putExtra("playlist_title", item.title)
                            putExtra("playlist_source",SearchSource.YTMUSIC.name)
                            putExtra("rectangular_image", true)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }

                        context.startActivity(intent)
                    }
                    .background(
                        brush = Brush.verticalGradient(item.gradient)
                    )
            ) {
                if (index == 0 || index == 1) {
                    val badgeText = when (index) {
                        0 -> "DAILY"
                        1 -> "NEW"
                        else -> ""
                    }

                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(40))
                            .background(colorResource(R.color.primary_text_color).copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = colorResource(R.color.off_white),
                            fontSize = 9.sp,
                            fontFamily = fonts,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = item.title,
                        color = colorResource(R.color.off_white),
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fonts,
                        fontStyle = FontStyle.Normal
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.subtitle,
                        color = colorResource(R.color.off_white).copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontFamily = fonts,
                        lineHeight = 12.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreSongs(
    playlistId: String,
    root: String,
    modifier: Modifier,
    downloadedIds: Set<String>,
    viewModel: ExploreSongsViewModel = viewModel(),
    listState: LazyListState,
    onMoreClick: (SongItem, Int, List<SongItem>) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        viewModel.fetchPlaylistsByID(playlistId, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    when {
        isLoading -> {
            LoadingEffect()
        }

        songs.isEmpty() -> {
            ErrorState()
        }

        else -> {
            LazyColumn (
                state = listState,
                modifier = modifier,
                contentPadding = PaddingValues(top = 8.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    songs,
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
                                .padding(start = if (currentSong?.id == song.id) 0.dp else 22.dp, end = 12.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    val intent = Intent(context, MusicPlayerService::class.java).apply {
                                        action = MusicPlayerService.ACTION_PLAY_NEW
                                        putExtra("index", index)
                                    }

                                    PlayerManager.currentPlaylist = songs
                                    PlayerManager.currentIndex = index

                                    ContextCompat.startForegroundService(context, intent)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = if (song.songSource == "jiosaavn") song.image.getOrNull(2)?.url
                                else song.image.getOrNull(0)?.url,
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
                                        onMoreClick(song, index, songs)
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
    }
}

@Composable
fun ExploreArtists(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: ExploreArtistsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    LaunchedEffect(query) {
        viewModel.fetchArtistsByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    when {
        isLoading -> {
            LoadingEffect()
        }

        artists.isEmpty() -> {
            ErrorState()
        }

        else -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(artists) { artist ->
                    Column(
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, ArtistActivity::class.java).apply {
                                putExtra("artist_id", artist.id)
                                putExtra("artist_imageUrl", artist.image)
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
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
                                .size(82.dp)
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

@Composable
fun ExploreAlbums(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: ExploreAlbumsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    LaunchedEffect(query) {
        viewModel.fetchAlbumByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    when {
        isLoading -> {
            LoadingEffect()
        }

        albums.isEmpty() -> {
            ErrorState()
        }

        else -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(albums) { album ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                val intent = Intent(context, AlbumActivity::class.java).apply {
                                    putExtra("album_id", album.id)
                                    putExtra("album_imageUrl", if (album.searchSource == "ytmusic") album.image.getOrNull(0)?.url
                                                else album.image.getOrNull(2)?.url
                                    )
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        AsyncImage(
                            model = if (album.searchSource == "jiosaavn") album.image.getOrNull(2)?.url
                            else album.image.getOrNull(0)?.url,
                            contentDescription = album.name,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.default_image),
                            modifier = Modifier
                                .fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val albumName = htmlToText(album.name)

                        Text(
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                            text = albumName,
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

@Composable
fun ExplorePlaylist(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: ExplorePlaylistsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    LaunchedEffect(query) {
        viewModel.fetchPlayListByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    when {
        isLoading -> {
            LoadingEffect()
        }

        playlists.isEmpty() -> {
            ErrorState()
        }

        else -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(playlists) { playlist ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                val intent = Intent(context, PlaylistActivity::class.java).apply {
                                    putExtra("playlist_id", playlist.id)
                                    putExtra("playlist_imageUrl", if (playlist.searchSource == "jiosaavn") playlist.image[2].url
                                                else playlist.image.getOrNull(0)?.url
                                    )
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        AsyncImage(
                            model = if (playlist.searchSource == "jiosaavn") playlist.image.getOrNull(2)?.url
                            else playlist.image.getOrNull(0)?.url,
                            contentDescription = playlist.name,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.default_image),
                            modifier = Modifier
                                .fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val playlistName = htmlToText(playlist.name)

                        Text(
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                            text = playlistName,
                            fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color), maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
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
            .padding(bottom = 100.dp)
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
private fun ErrorState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
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
            text = "No results found",
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color), maxLines = 2
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DiscoverScreenPreview() {

}