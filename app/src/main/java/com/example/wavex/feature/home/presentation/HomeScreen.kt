package com.example.wavex.feature.home.presentation

import android.content.Intent
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.Html
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.core.database.entity.RecentlyPlayedEntity
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.service.MusicPlayerService
import com.example.wavex.core.service.ServiceLocator
import com.example.wavex.feature.album.presentation.AlbumActivity
import com.example.wavex.feature.artist.presentation.ArtistActivity
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.home.model.HomeUiState
import com.example.wavex.feature.playlist.presentation.PlaylistActivity
import com.example.wavex.feature.profile.presentation.ProfileActivity
import com.example.wavex.feature.search.presentation.SearchSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds

object PlayerManager {
    var currentPlaylist: List<SongItem> = emptyList()
    var currentIndex: Int = 0
}

@Composable
fun HomeScreen (
    showSheet: Boolean,
    onSongLongPress: (List<SongItem>, SongItem, Int) -> Unit,
    imageUrl: String?,
    uiState: HomeUiState,
    onRefresh: () -> Unit
) {
    val scrollState = rememberScrollState()
    var isRefreshing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pullRefreshState = rememberPullToRefreshState()

    var isScrollingDown by remember { mutableStateOf(false) }
    var lastScrollValue by remember { mutableIntStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { current ->
                val hideAfter = 40
                isScrollingDown = current > lastScrollValue && current > hideAfter
                lastScrollValue = current
            }
    }

    val playlists = uiState.playlists
    val newReleases = uiState.newReleases
    val trendingSongs = uiState.trendingSongs
    val albums = uiState.topAlbums
    val artists = uiState.topArtists
    val recentlyPlayedSongs = uiState.recentlyPlayed

    val isLoading = uiState.isLoading

    val logoAlpha by animateFloatAsState(
        targetValue = if (isScrollingDown) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "Logo Fade"
    )

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

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

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }

    fun refreshAll() {
        scope.launch {
            isRefreshing = true

            onRefresh()

            delay(1000.milliseconds)

            isRefreshing = false
        }
    }

//    PullToRefreshBox(
//        state = pullRefreshState,
//        isRefreshing = isRefreshing,
//        onRefresh = { refreshAll() },
//        indicator = {
//            val composition by rememberLottieComposition(
//                LottieCompositionSpec.RawRes(R.raw.astronaut_loading)
//            )
//
//            val progress by animateLottieCompositionAsState(
//                composition = composition,
//                isPlaying = isLoading,
//                iterations = LottieConstants.IterateForever
//            )
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 15.dp)
//                    .size(60.dp)
//                    .clip(RectangleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                LottieAnimation(
//                    composition = composition,
//                    progress = {progress},
//                    modifier = Modifier
//                        .size(60.dp)
//                        .graphicsLayer {
//                            scaleX = 1.2f
//                            scaleY = 1.2f
//                        }
//                )
//            }
////            PullToRefreshDefaults.Indicator(
////                state = pullRefreshState,
////                isRefreshing = isLoading,
////                modifier = Modifier
////                    .align(Alignment.TopCenter)
////                    .padding(top = 10.dp),
////                color = colorResource(R.color.theme_color),
////                containerColor = colorResource(R.color.primary_text_color)
////            )
//        }
//    )
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                            )
                            .asComposeRenderEffect()
                    }
                }
        ) {
            val (logoIcon, profileAvatar, mainContent, loader) = createRefs()

            Box(
                modifier = Modifier
                    .constrainAs(logoIcon) {
                        top.linkTo(profileAvatar.top)
                        bottom.linkTo(profileAvatar.bottom)
                        start.linkTo(parent.start, margin = 10.dp)
                    }
                    .size(width = 140.dp, height = 40.dp)
                    .zIndex(20f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.wavex_logo_dark),
                    contentDescription = "Logo Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(width = 140.dp, height = 40.dp)
                        .graphicsLayer {
                            alpha = logoAlpha
                            scaleX = 1.5f
                            scaleY = 1.5f
                        }
                )
            }

            Box(
                modifier = Modifier
                    .constrainAs(profileAvatar) {
                        top.linkTo(parent.top, margin = 10.dp)
                        end.linkTo(parent.end, margin = 22.dp)
                    }
                    .size(68.dp)
                    .drawBehind {
                        val glowRadius = (size.minDimension / 3) * shadowScale
                        val safeBlur = shadowBlur.coerceAtLeast(0.1f)

                        drawIntoCanvas { canvas ->
                            val frameworkPaint = Paint().apply {
                                isAntiAlias = true
                                color = shadowColor.copy(alpha = shadowAlpha).toArgb()

                                maskFilter = BlurMaskFilter(
                                    safeBlur,
                                    BlurMaskFilter.Blur.NORMAL
                                )
                            }

                            canvas.nativeCanvas.drawCircle(
                                center.x,
                                center.y,
                                glowRadius,
                                frameworkPaint
                            )
                        }
                    }
                    .zIndex(20f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .allowHardware(false)
                        .build(),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
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
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, ProfileActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        }
                        .graphicsLayer {
                            alpha = logoAlpha
                        }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .constrainAs(loader) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .fillMaxSize()
                        .background(colorResource(R.color.background_color))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent()
                                }
                            }
                        }
                        .zIndex(10f),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingEffect()
                }
            } else {
                Column(
                    modifier = Modifier
                        .constrainAs(mainContent) {
                            top.linkTo(parent.top, margin = 5.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        }
                        .verticalScroll(scrollState).zIndex(0f)
                ) {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val (
                            featuredPlaylistTitle, featuredPlaylistsSection, recentlyPlayedTitle,
                            recentlyPlayedSection, newReleasesTitle, newReleasesSection, popularArtistsTitle,
                            popularArtistsSection, trendingSongsTitle, trendingSongsSection, topAlbumsTitle, topAlbumsSection
                        ) = createRefs()

                        if (playlists.isNotEmpty()) {
                            Text(
                                text = "FEATURED · TODAY",
                                modifier = Modifier
                                    .constrainAs(featuredPlaylistTitle) {
                                        top.linkTo(parent.top, margin = 75.dp)
                                        start.linkTo(parent.start, margin = 25.dp)
                                    },
                                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.chalesrientta)), fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.primary_text_color),
                                lineHeight = 20.sp
                            )

                            Playlist(
                                playlists = playlists,
                                modifier = Modifier
                                    .constrainAs(featuredPlaylistsSection) {
                                        top.linkTo(featuredPlaylistTitle.bottom, margin = 15.dp)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                    }
                            )
                        }

                        if (recentlyPlayedSongs.isNotEmpty() && playlists.isNotEmpty()) {
                            Text(
                                text = "Recently Played",
                                modifier = Modifier
                                    .constrainAs(recentlyPlayedTitle) {
                                        top.linkTo(featuredPlaylistsSection.bottom, margin = if (playlists.isNotEmpty()) 25.dp else 80.dp)
                                        start.linkTo(parent.start, margin = 25.dp)
                                    },
                                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.chalesrientta)), fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.primary_text_color),
                                lineHeight = 20.sp
                            )

                            RecentlyPlayedSongs(
                                recentlyPlayed = recentlyPlayedSongs,
                                modifier = Modifier
                                    .constrainAs(recentlyPlayedSection) {
                                        top.linkTo(recentlyPlayedTitle.bottom, margin = 15.dp)
                                        start.linkTo(parent.start)
                                    },
                                onSongLongPress = onSongLongPress
                            )
                        }

                        if (newReleases.isNotEmpty()) {
                            Text(
                                text = "New Releases",
                                modifier = Modifier
                                    .constrainAs(newReleasesTitle) {
                                        top.linkTo(if (recentlyPlayedSongs.isNotEmpty()) recentlyPlayedSection.bottom else featuredPlaylistsSection.bottom, margin = 22.dp)
                                        start.linkTo(parent.start, margin = 25.dp)
                                    },
                                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.chalesrientta)), fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.primary_text_color),
                                lineHeight = 20.sp
                            )

                            NewReleasesSongs(
                                songs = newReleases,
                                modifier = Modifier
                                    .constrainAs(newReleasesSection) {
                                        top.linkTo(newReleasesTitle.bottom, margin = 15.dp)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                    },
                                onSongLongPress = onSongLongPress
                            )
                        }

                        if (artists.isNotEmpty()) {
                            Text(
                                text = "Top Artists",
                                modifier = Modifier
                                    .constrainAs(popularArtistsTitle) {
                                        top.linkTo(newReleasesSection.bottom, margin = 22.dp)
                                        start.linkTo(parent.start, margin = 25.dp)
                                    },
                                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.chalesrientta)), fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.primary_text_color),
                                lineHeight = 20.sp
                            )

                            Artists(
                                artists = artists,
                                modifier = Modifier
                                    .constrainAs(popularArtistsSection){
                                        top.linkTo(popularArtistsTitle.bottom, margin = 15.dp)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                    }
                            )
                        }

                        if (trendingSongs.isNotEmpty()) {
                            Text(
                                text = "Trending Songs",
                                modifier = Modifier
                                    .constrainAs(trendingSongsTitle) {
                                        top.linkTo(popularArtistsSection.bottom, margin = 22.dp)
                                        start.linkTo(parent.start, margin = 25.dp)
                                    },
                                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.chalesrientta)), fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.primary_text_color),
                                lineHeight = 20.sp
                            )

                            TrendingSongs(
                                songs = trendingSongs,
                                modifier = Modifier
                                    .constrainAs(trendingSongsSection) {
                                        top.linkTo(trendingSongsTitle.bottom, margin = 15.dp)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                    },
                                onSongLongPress = onSongLongPress
                            )
                        }

                        if (albums.isNotEmpty()) {
                            Text(
                                text = "Top Albums",
                                modifier = Modifier
                                    .constrainAs(topAlbumsTitle) {
                                        top.linkTo(trendingSongsSection.bottom, margin = 22.dp)
                                        start.linkTo(parent.start, margin = 25.dp)
                                    },
                                fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.chalesrientta)), fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.primary_text_color),
                                lineHeight = 20.sp
                            )

                            TopAlbums(
                                albums = albums,
                                modifier = Modifier
                                    .constrainAs(topAlbumsSection) {
                                        top.linkTo(topAlbumsTitle.bottom, margin = 15.dp)
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
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
fun Playlist(
    playlists: List<DataItem>,
    modifier: Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    if (playlists.isEmpty()) return

    val infiniteCount = Int.MAX_VALUE
    val startIndex = infiniteCount / 2

    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { infiniteCount }
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000.milliseconds)

            pagerState.animateScrollToPage(
                pagerState.currentPage + 1
            )
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 58.dp),
            pageSpacing = 2.dp
        ) { page ->
            val item = playlists[page % playlists.size]

            val pageOffset = (
                    (pagerState.currentPage - page) +
                            pagerState.currentPageOffsetFraction
                    ).absoluteValue

            val scale = lerp(
                start = 0.82f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )

            val playlistName = remember(item.name) {
                htmlToText(item.name)
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale

                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        val intent = Intent(context, PlaylistActivity::class.java).apply {
                            putExtra("playlist_id", item.id)
                            putExtra("playlist_imageUrl",
                                when (item.searchSource) {
                                    SearchSource.YTMUSIC.name ->
                                        item.image.getOrNull(0)?.url

                                    SearchSource.JIOSAAVN.name ->
                                        item.image.getOrNull(2)?.url
                                            ?: item.image.lastOrNull()?.url

                                    else ->
                                        item.image.lastOrNull()?.url
                                }
                            )
                            putExtra("playlist_source",
                                when(item.searchSource) {
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
                            putExtra("rectangular_image",
                                when(item.searchSource) {
                                    SearchSource.YTMUSIC.name -> {
                                        true
                                    }
                                    SearchSource.JIOSAAVN.name -> {
                                        false
                                    }

                                    else -> {
                                        false
                                    }
                                }
                            )
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    }
                    .fillMaxWidth()
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                AsyncImage(
                    model = item.image.getOrNull(2)?.url,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.default_image),
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                Text(
                    text = playlistName,
                    color = colorResource(R.color.off_white),
                    fontSize = 24.sp,
                    lineHeight = 26.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = 22.dp,
                            end = 22.dp,
                            bottom = 22.dp
                        )
                )
            }
        }
    }
}

@Composable
fun RecentlyPlayedSongs(
    recentlyPlayed: List<RecentlyPlayedEntity>,
    modifier: Modifier,
    onSongLongPress: (List<SongItem>, SongItem, Int) -> Unit
) {
    val uniqueSongs = recentlyPlayed.distinctBy { it.id }
    val songLists = uniqueSongs.map { it.toSongItem() }

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    val columns = remember(songLists) {
        songLists.chunked(3)
    }

    LazyRow(
        modifier = modifier.height(230.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(
            items = columns,
            key = { column -> column.firstOrNull()?.id ?: column.hashCode() }
        ) { columnSongs ->
            Column(
                modifier = Modifier.width(200.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                columnSongs.forEach { song ->
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    val songIndex = songLists.indexOfFirst { it.id == song.id }
                                    if (songIndex == -1) return@combinedClickable

                                    val intent = Intent(context, MusicPlayerService::class.java).apply {
                                        action = MusicPlayerService.ACTION_PLAY_NEW
                                        putExtra("index", songIndex)
                                    }

                                    PlayerManager.currentPlaylist = songLists
                                    PlayerManager.currentIndex = songIndex

                                    ContextCompat.startForegroundService(context, intent)
                                },
                                onLongClick = {
                                    val songIndex = songLists.indexOfFirst { it.id == song.id }
                                    if (songIndex != -1) {
                                        onSongLongPress(songLists, song, songIndex)
                                    }
                                }
                            ),
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
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
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
                    }
                }
            }
        }
    }
}

@Composable
fun NewReleasesSongs(
    songs: List<SongItem>,
    modifier: Modifier,
    onSongLongPress: (List<SongItem>, SongItem, Int) -> Unit
) {
    val context = LocalContext.current

    val interactionSource = remember { MutableInteractionSource() }

    if (songs.isEmpty()) return

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        itemsIndexed(songs, key = { _, song -> song.id }) { _, song ->
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            val songIndex = songs.indexOfFirst { it.id == song.id }
                            if (songIndex == -1) return@combinedClickable

                            val intent = Intent(context, MusicPlayerService::class.java).apply {
                                action = MusicPlayerService.ACTION_PLAY_NEW
                                putExtra("index", songIndex)
                            }

                            PlayerManager.currentPlaylist = songs
                            PlayerManager.currentIndex = songIndex

                            ContextCompat.startForegroundService(context, intent)
                        },
                        onLongClick = {
                            val songIndex = songs.indexOfFirst { it.id == song.id }
                            if (songIndex != -1) {
                                onSongLongPress(songs, song, songIndex)
                            }
                        }
                    )
            ) {
                AsyncImage(
                    model = song.image.getOrNull(2)?.url,
                    contentDescription = song.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(110.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                val songName = htmlToText(song.name)

                Text(
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                    text = songName,
                    fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val artistsList = song.artist
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") { it.name }
                    ?: "Unknown Artist"

                val artistsName = htmlToText(artistsList)

                Text(
                    modifier = Modifier.padding(horizontal = 2.dp ),
                    text = artistsName,
                    fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color), maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun Artists(
    artists: List<Artists>,
    modifier: Modifier
) {
    val context = LocalContext.current

    val interactionSource = remember { MutableInteractionSource() }

    if (artists.isEmpty()) return

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        items(artists, key = { it.id }) { artist ->
            Column(
                modifier = Modifier.clickable(
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

@Composable
fun TrendingSongs(
    songs: List<SongItem>,
    modifier: Modifier,
    onSongLongPress: (List<SongItem>, SongItem, Int) -> Unit
) {
    val context = LocalContext.current

    val interactionSource = remember { MutableInteractionSource() }

    if (songs.isEmpty()) return

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        itemsIndexed(songs, key = { _, song -> song.id }) { _, song ->
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            val songIndex = songs.indexOfFirst { it.id == song.id }
                            if (songIndex == -1) return@combinedClickable

                            val intent = Intent(context, MusicPlayerService::class.java).apply {
                                action = MusicPlayerService.ACTION_PLAY_NEW
                                putExtra("index", songIndex)
                            }

                            PlayerManager.currentPlaylist = songs
                            PlayerManager.currentIndex = songIndex

                            ContextCompat.startForegroundService(context, intent)
                        },
                        onLongClick = {
                            val songIndex = songs.indexOfFirst { it.id == song.id }
                            if (songIndex != -1) {
                                onSongLongPress(songs, song, songIndex)
                            }
                        }
                    )
            ) {
                AsyncImage(
                    model = song.image.getOrNull(2)?.url,
                    contentDescription = song.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(110.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                val songName = htmlToText(song.name)

                Text(
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                    text = songName,
                    fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val artistsList = song.artist
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") { it.name }
                    ?: "Unknown Artist"

                val artistsName = htmlToText(artistsList)

                Text(
                    modifier = Modifier.padding(horizontal = 2.dp ),
                    text = artistsName,
                    fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color), maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TopAlbums(
    albums: List<DataItem>,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val interactionSource = remember { MutableInteractionSource() }

    if (albums.isEmpty()) return

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = if (currentSong != null) 170.dp else 100.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items(albums, key = { it.id }) { album ->
            Column(
                modifier = Modifier
                    .width(110.dp)
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

fun htmlToText(html: String?): String {
    if (html.isNullOrBlank()) return ""

    return Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY).toString().trim()
}

fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.US,"%02d : %02d", minutes, remainingSeconds)
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

fun RecentlyPlayedEntity.toSongItem(): SongItem {
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

fun SongItem.toRecentlyPlayedEntity(): RecentlyPlayedEntity {
    return RecentlyPlayedEntity(
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
@Preview(showSystemUi = true)
private fun HomeScreenPreview() {
    HomeScreen(
        showSheet = false,
        onSongLongPress = { _, _, _ -> },
        imageUrl = "",
        onRefresh = {},
        uiState = HomeUiState()
    )
}