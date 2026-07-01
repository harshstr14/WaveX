package com.example.wavex.feature.artist.presentation

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.example.wavex.MiniPlayer
import com.example.wavex.R
import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.service.MusicPlayerService
import com.example.wavex.core.service.ParallelDownloader
import com.example.wavex.core.service.ServiceLocator
import com.example.wavex.core.util.DownloadQualitySelector
import com.example.wavex.feature.album.presentation.AlbumActivity
import com.example.wavex.feature.album.presentation.ShareType
import com.example.wavex.feature.artist.allalbums.presentation.AllAlbumsActivity
import com.example.wavex.feature.artist.allsongs.presentation.AllSongsActivity
import com.example.wavex.feature.artist.model.ArtistDetailUiState
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.home.presentation.PlayerManager
import com.example.wavex.feature.home.presentation.formatDuration
import com.example.wavex.feature.home.presentation.htmlToText
import com.example.wavex.feature.library.model.LibraryUiState
import com.example.wavex.feature.player.presentation.PlayerActivityScreen
import com.example.wavex.feature.search.presentation.SearchSource
import com.example.wavex.pressScale
import com.example.wavex.uiComponent.ShareArtist
import com.example.wavex.uiComponent.ShareArtistItem
import com.example.wavex.ui.theme.WaveXTheme
import com.example.wavex.uiComponent.SongBottomSheet
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistId: String?,
    artistImageUrl: String?,
    artistSource: String?,
    onDeleteSong: (String) -> Unit,
    downloadedIds: Set<String>,
    likedSongs: Set<String>,
    onToggleLike: (SongItem) -> Unit,
    isLoading: Boolean,
    artists: ArtistDetailUiState,
    onLoadArtist: (String) -> Unit,
    onLoadYTArtist: (String) -> Unit,
    onCheckFavourite: (String) -> Unit,
    onToggleFavourite: (
        artistId: String, artistSource: String,
        artistImageUrl: String, artistName: String, onResult: (String) -> Unit
    ) -> Unit,
    isFavourite: Boolean,
    playlists: LibraryUiState,
    onAddSongToPlaylist: (String, SongItem, onResult: (Boolean, String) -> Unit) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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

    LaunchedEffect(artistId, artistSource) {
        if (artistId.isNullOrBlank()) {
            return@LaunchedEffect
        }

        when(artistSource) {
            SearchSource.JIOSAAVN.name ->
                onLoadArtist(artistId)

            SearchSource.YTMUSIC.name ->
                onLoadYTArtist(artistId)
        }
    }

    LaunchedEffect(artistId) {
        artistId?.let {
            onCheckFavourite(it)
        }
    }

    val (songSeeAllInteraction, songSeeAllScale) = pressScale()
    val (albumSeeAllInteraction, albumSeeAllScale) = pressScale()
    val (heartInteraction, heartScale) = pressScale()
    val (backInteraction, backScale) = pressScale()
    val (shareInteraction, shareScale) = pressScale()

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
                                        indication = ripple(
                                            bounded = true,
                                            color = colorResource(R.color.secondary_text_color).copy(alpha = 0.15f)
                                        )
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
                                        indication = ripple(
                                            bounded = true,
                                            color = colorResource(R.color.secondary_text_color).copy(alpha = 0.15f)
                                        )
                                    ) {
                                        onToggleFavourite(
                                            artistId ?: "", artists.name,
                                            imageToLoad, artistSource ?: ""
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

                artistSource == "unknown" -> {
                    ErrorState(
                        message = "Invalid artist source",
                        onRetry = {
                            when(artistSource) {
                                SearchSource.JIOSAAVN.name -> {
                                    onLoadArtist(artistId ?: "")
                                }

                                SearchSource.YTMUSIC.name -> {
                                    onLoadYTArtist(artistId ?: "")
                                }
                            }
                        }
                    )

                }

                artists.isError -> {
                    ErrorState(
                        message = artists.errorMessage,
                        onRetry = {
                            when(artistSource) {
                                SearchSource.JIOSAAVN.name -> {
                                    onLoadArtist(artistId ?: "")
                                }

                                SearchSource.YTMUSIC.name -> {
                                    onLoadYTArtist(artistId ?: "")
                                }
                            }
                        }
                    )
                }

                artistId.isNullOrBlank() -> {
                    ErrorState(
                        message = "Invalid artist source",
                        onRetry = {
                            when(artistSource) {
                                SearchSource.JIOSAAVN.name -> {
                                    onLoadArtist(artistId ?: "")
                                }

                                SearchSource.YTMUSIC.name -> {
                                    onLoadYTArtist(artistId ?: "")
                                }
                            }
                        }
                    )
                }

                else -> {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
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
                            contentPadding = PaddingValues(bottom = if (currentSong != null) 80.dp else 5.dp)
                        ) {
                            item {
                                if (artists.name.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, start = 24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box {
                                            AsyncImage(
                                                model = imageToLoad,
                                                contentDescription = "Artist Image",
                                                contentScale = ContentScale.Crop,
                                                modifier =  Modifier
                                                    .size(120.dp)
                                                    .clip(CircleShape)
                                            )

                                            if (artists.isVerified) {
                                                Icon(
                                                    painter = painterResource(R.drawable.verified_icon),
                                                    contentDescription = "Verified Icon",
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier
                                                        .padding(end = 6.dp, bottom = 4.dp)
                                                        .align(Alignment.BottomEnd)
                                                        .size(26.dp)
                                                )
                                            }
                                        }

                                        Column (
                                            modifier = Modifier.padding(horizontal = 15.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = htmlToText(artists.name),
                                                maxLines = 1,
                                                fontSize = 22.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color),
                                                lineHeight = 24.sp
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.followers_icon),
                                                    contentDescription = "Followers Icon",
                                                    tint = colorResource(R.color.secondary_text_color),
                                                    modifier = Modifier.size(18.dp)
                                                )

                                                Spacer(modifier = Modifier.width(4.dp))

                                                Text(
                                                    text = "Followers : ${formatCount(artists.followerCount.toLong())}",
                                                    fontSize = 13.sp,
                                                    fontFamily = fonts,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.secondary_text_color),
                                                    lineHeight = 16.sp,
                                                    maxLines = 1,
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
                                                    text = "Listeners : ${formatCount(artists.fanCount.toLongOrNull() ?: 0L)}",
                                                    fontSize = 13.sp,
                                                    fontFamily = fonts,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontStyle = FontStyle.Normal,
                                                    color = colorResource(R.color.secondary_text_color),
                                                    lineHeight = 16.sp,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            val uniqueSongs = artists.topSongs.distinctBy { it.id }

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
                                            text = "Top Songs", fontSize = 18.sp, letterSpacing = 1.sp,
                                            fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                        )

                                        Text(
                                            modifier = Modifier
                                                .clickable(
                                                    interactionSource = songSeeAllInteraction,
                                                    indication = null
                                                ) {
                                                    val intent = Intent(context, AllSongsActivity::class.java).apply {
                                                        putExtra("artist_id", artists.id)
                                                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                                    }
                                                    context.startActivity(intent)
                                                }
                                                .graphicsLayer(
                                                    scaleX = songSeeAllScale,
                                                    scaleY = songSeeAllScale
                                                ),
                                            text = "See All", fontSize = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.theme_color), lineHeight = 18.sp
                                        )
                                    }
                                }

                                itemsIndexed(
                                    uniqueSongs,
                                    key = { _, song -> song.id }
                                ) { index, song ->

                                    val songName = remember(song.name) {
                                        htmlToText(song.name)
                                    }

                                    val artistsName = remember(song.id) {
                                        htmlToText(
                                            song.artist.takeIf { it.isNotEmpty() }
                                                ?.joinToString(", ") { it.name } ?: "Unknown Artist"
                                        )
                                    }

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

                                                    PlayerManager.currentPlaylist = artists.topSongs
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
                                                        Log.d("Download_url","$downloadUrl")

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
                                            text = "Top Albums", fontSize = 18.sp, letterSpacing = 1.sp,
                                            fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                        )

                                        Text(
                                            modifier = Modifier
                                                .clickable(
                                                    interactionSource = albumSeeAllInteraction,
                                                    indication = null
                                                ) {
                                                    val intent = Intent(context, AllAlbumsActivity::class.java).apply {
                                                        putExtra("artist_id", artists.id)
                                                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                                    }
                                                    context.startActivity(intent)
                                                }
                                                .graphicsLayer(
                                                    scaleX = albumSeeAllScale,
                                                    scaleY = albumSeeAllScale
                                                ),
                                            text = "See All", fontSize = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.theme_color), lineHeight = 18.sp
                                        )
                                    }
                                }

                                item {
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp),
                                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                                    ) {
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
                                                            putExtra("album_imageUrl",
                                                                when (album.searchSource) {
                                                                    SearchSource.YTMUSIC.name ->
                                                                        album.image.getOrNull(0)?.url

                                                                    SearchSource.JIOSAAVN.name ->
                                                                        album.image.getOrNull(2)?.url
                                                                            ?: album.image.lastOrNull()?.url

                                                                    else ->
                                                                        album.image.lastOrNull()?.url
                                                                }
                                                            )
                                                            putExtra("album_source",
                                                                when(album.searchSource) {
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
                                            ) {
                                                AsyncImage(
                                                    model = when (album.searchSource) {
                                                        SearchSource.YTMUSIC.name ->
                                                            album.image.getOrNull(0)?.url

                                                        SearchSource.JIOSAAVN.name ->
                                                            album.image.getOrNull(2)?.url
                                                                ?: album.image.lastOrNull()?.url

                                                        else ->
                                                            album.image.lastOrNull()?.url
                                                    },
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
                                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp, letterSpacing = 1.sp,
                                    )
                                }

                                item {
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 15.dp),
                                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp),
                                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                                    ) {
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
                                                            putExtra("album_imageUrl",
                                                                when (album.searchSource) {
                                                                    SearchSource.YTMUSIC.name ->
                                                                        album.image.getOrNull(0)?.url

                                                                    SearchSource.JIOSAAVN.name ->
                                                                        album.image.getOrNull(2)?.url
                                                                            ?: album.image.lastOrNull()?.url

                                                                    else ->
                                                                        album.image.lastOrNull()?.url
                                                                }
                                                            )
                                                            putExtra("album_source",
                                                                when(album.searchSource) {
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
                                            ) {
                                                AsyncImage(
                                                    model = when (album.searchSource) {
                                                        SearchSource.YTMUSIC.name ->
                                                            album.image.getOrNull(0)?.url

                                                        SearchSource.JIOSAAVN.name ->
                                                            album.image.getOrNull(2)?.url
                                                                ?: album.image.lastOrNull()?.url

                                                        else ->
                                                            album.image.lastOrNull()?.url
                                                    },
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
                ShareArtist(
                    artist = ShareArtistItem(
                        id = artists.id,
                        name = artists.name,
                        followerCount = formatCount(artists.followerCount.toLong()),
                        fanCount = formatCount(artists.fanCount.toLongOrNull() ?: 0L),
                        isVerified = artists.isVerified,
                        topSongs = artists.topSongs,
                        image = imageToLoad,
                        type = ShareType.ARTIST,
                        source = artistSource.toString()
                    ),
                    onDismiss = { showShareSheet = false }
                )
            }

            if (showSongSheet && selectedSong != null) {
                val song = selectedSong!!
                val isFavourite = likedSongs.contains(song.id)
                val isDownloaded = downloadedIds.contains(song.id)
                Log.d("Song", "${song.playCount}")

                SongBottomSheet(
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

                            PlayerManager.currentPlaylist = artists.topSongs
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

        Text(
            text = message,
            fontSize = 16.sp, lineHeight = 20.sp, fontFamily = fonts,
            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
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
                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
                fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.background_color)
            )
        }
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000_000 -> String.format(Locale.US,"%.2fB", count / 1_000_000_000.0)
        count >= 1_000_000 -> String.format(Locale.US,"%.2fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US,"%.2fK", count / 1_000.0)
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

@Preview(showSystemUi = true)
@Composable
private fun ArtistScreenPreview() {
    WaveXTheme {
        ArtistScreen(
            artistId = "",
            artistImageUrl = "",
            artistSource = "",
            onDeleteSong = {},
            downloadedIds = setOf(),
            likedSongs = setOf(),
            onToggleLike = {},
            isLoading = false,
            artists = ArtistDetailUiState(),
            onLoadArtist = {},
            onLoadYTArtist = {},
            onCheckFavourite = {},
            onToggleFavourite = { _,_,_,_,_ -> },
            isFavourite = true,
            playlists = LibraryUiState(),
            onAddSongToPlaylist = { _,_,_ -> }
        )
    }
}