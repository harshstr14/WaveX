package com.example.wavex.feature.search.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
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
import com.example.wavex.R
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.service.MusicPlayerService
import com.example.wavex.core.service.ParallelDownloader
import com.example.wavex.core.service.ServiceLocator
import com.example.wavex.core.util.DownloadQualitySelector
import com.example.wavex.feature.album.presentation.AlbumActivity
import com.example.wavex.feature.artist.presentation.ArtistActivity
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.home.presentation.PlayerManager
import com.example.wavex.feature.home.presentation.formatDuration
import com.example.wavex.feature.home.presentation.htmlToText
import com.example.wavex.feature.library.model.LibraryUiState
import com.example.wavex.feature.playlist.presentation.PlaylistActivity
import com.example.wavex.feature.search.model.SearchAlbumsUiState
import com.example.wavex.feature.search.model.SearchArtistsUiState
import com.example.wavex.feature.search.model.SearchPlaylistUiState
import com.example.wavex.feature.search.model.SearchSongsUiState
import com.example.wavex.pressScale
import com.example.wavex.uiComponent.SongBottomSheet
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

enum class SearchSource {
    JIOSAAVN,
    YTVIDEO,
    YTMUSIC
}

@OptIn(FlowPreview::class)
@Composable
fun SearchScreen(
    snackBarHostState: SnackbarHostState,
    showSheet: Boolean,
    onClickBack: () -> Unit,
    onToggleLike: (SongItem) -> Unit,
    likedSongs: Set<String>,
    downloadedIds: Set<String>,
    onDeleteSong: (String) -> Unit,
    artists: List<Artists>,
    artistsUiSate: SearchArtistsUiState,
    onSearchArtists: (String) -> Unit,
    onSearchYTArtists: (String) -> Unit,
    onArtistsClearResult: () -> Unit,
    onArtistsIdeal: () -> Unit,
    songs: List<SongItem>,
    songsUiState: SearchSongsUiState,
    onSearchSongs: (String) -> Unit,
    onSearchYTSongs: (String) -> Unit,
    onSongsClearResult: () -> Unit,
    onSongsIdeal: () -> Unit,
    albums: List<DataItem>,
    albumUiState: SearchAlbumsUiState,
    onSearchAlbums: (String) -> Unit,
    onSearchYTAlbums: (String) -> Unit,
    onAlbumsClearResult: () -> Unit,
    onAlbumsIdeal: () -> Unit,
    playlists: List<DataItem>,
    playlistsUiState: SearchPlaylistUiState,
    onSearchPlaylists: (String) -> Unit,
    onSearchYTPlaylists: (String) -> Unit,
    onPlaylistsClearResult: () -> Unit,
    onPlaylistsIdeal: () -> Unit,
    libraryPlaylists: LibraryUiState,
    onAddSongToPlaylist: (String, SongItem, onResult: (Boolean, String) -> Unit) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
//    val recentSearches by viewModel.recentSearches.collectAsState()
//    Log.d("History", recentSearches.toString())

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var debouncedQuery by remember { mutableStateOf("") }
    //val showHistory = searchText.text.isBlank()

    var selectedSource by remember {
        mutableStateOf(SearchSource.JIOSAAVN)
    }

    val (backInteraction, backScale) = pressScale()

    var showSongSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var currentSongs by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSongSheet || showSheet) 22f else 0f,
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
        val(searchField, backButton, searchResults, sources, history) = createRefs()

        Box(
            modifier = Modifier
                .constrainAs(backButton) {
                    top.linkTo(searchField.top)
                    bottom.linkTo(searchField.bottom)
                    start.linkTo(parent.start, margin = 25.dp)
                }
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
                    onClickBack()
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

        SearchBar(
            modifier = Modifier
                .constrainAs(searchField) {
                    top.linkTo(parent.top, margin = 20.dp)
                    start.linkTo(backButton.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .padding(start = 18.dp, end = 18.dp)
                .height(52.dp)
                .focusRequester(focusRequester),
            query = searchText,
            onQueryChange = { newValue ->
                searchText = newValue
            }
        )

        SourceSelectorUI(
            modifier = Modifier
                .constrainAs(sources) {
                    top.linkTo(searchField.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            selected = selectedSource,
            onSourceSelected = {
                selectedSource = it
            }
        )

        SearchTabs(
            modifier = Modifier
                .constrainAs(searchResults){
                    top.linkTo(sources.bottom, margin = 4.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                },
            source = selectedSource,
            searchText = debouncedQuery,
            downloadedIds = downloadedIds,
            onSongMoreClick = { song, songs, index ->
                selectedSong = song
                currentSongs = songs
                selectedIndex = index
                showSongSheet = true
            },
            snackBarHostState = snackBarHostState,
            artists = artists,
            artistsUiSate = artistsUiSate,
            onSearchArtists = { query ->
                onSearchArtists(query)
            },
            onSearchYTArtists = { query ->
                onSearchYTArtists(query)
            },
            onArtistsClearResult = onArtistsClearResult,
            onArtistsIdeal = onArtistsIdeal,
            songs = songs,
            songsUiState = songsUiState,
            onSearchSongs = { query ->
                onSearchSongs(query)
            },
            onSearchYTSongs = { query ->
                onSearchYTSongs(query)
            },
            onSongsClearResult = onSongsClearResult,
            onSongsIdeal = onSongsIdeal,
            albums = albums,
            albumUiState = albumUiState,
            onSearchAlbums = { query ->
                onSearchAlbums(query)
            },
            onSearchYTAlbums = { query ->
                onSearchYTAlbums(query)
            },
            onAlbumsClearResult = onAlbumsClearResult,
            onAlbumsIdeal = onAlbumsIdeal,
            playlists = playlists,
            playlistsUiState = playlistsUiState,
            onSearchPlaylists = { query ->
                onSearchPlaylists(query)
            },
            onSearchYTPlaylists = { query ->
                onSearchYTPlaylists(query)
            },
            onPlaylistsClearResult = onPlaylistsClearResult,
            onPlaylistsIdeal = onPlaylistsIdeal
        )

//        AnimatedVisibility(
//            visible = showHistory,
//            enter = fadeIn(),
//            exit = fadeOut(),
//            modifier = Modifier
//                .constrainAs(history) {
//                    top.linkTo(sources.bottom, margin = 52.dp)
//                    start.linkTo(parent.start)
//                    end.linkTo(parent.end)
//                }
//        ) {
//            RecentSearchSection(
//                recentSearches = recentSearches,
//                onClear = {
//                    viewModel.clearSearches()
//                },
//                onSearchClick = {
//                    searchText = TextFieldValue(it)
//                }
//            )
//        }

        LaunchedEffect(Unit) {
            snapshotFlow { searchText.text.trim() }
                .debounce(800.milliseconds)
                .distinctUntilChanged()
                .filter { it.length >= 2 || it.isEmpty() }
                .collectLatest { query ->
                    debouncedQuery = query
                }
        }
    }

    if (showSongSheet && selectedSong != null) {
        val song = selectedSong!!
        val isFavourite = likedSongs.contains(song.id)
        val isDownloaded = downloadedIds.contains(song.id)

        SongBottomSheet(
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
                    intent.putExtra("index", 0)
                    intent.putExtra("from_search", true)

                    PlayerManager.currentPlaylist = listOf(song)
                    PlayerManager.currentIndex = 0
                }

                ContextCompat.startForegroundService(context, intent)
            },
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
            playlists = libraryPlaylists,
            onAddSongToPlaylist = { playlistID, song, onResult ->
                onAddSongToPlaylist(playlistID, song, onResult)
            }
        )
    }
}

@Composable
private fun Modifier.hideKeyboardOnClick(onClick: () -> Unit): Modifier {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    return clickable(
        interactionSource = interactionSource,
        indication = null
    ) {
        focusManager.clearFocus()
        keyboardController?.hide()
        onClick()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchBar(
    modifier: Modifier, query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit
) {
    val selectionColors = TextSelectionColors(
        handleColor = colorResource(R.color.primary_text_color).copy(alpha = 0.88f),
        backgroundColor = colorResource(R.color.primary_text_color).copy(alpha = 0.3f)
    )
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible = WindowInsets.isImeVisible

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search",
                    fontFamily = fonts,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    fontSize = 16.sp, lineHeight = 18.sp,
                    color = colorResource(R.color.secondary_text_color)
                )
            },
            singleLine = true,
            leadingIcon = {
                Box(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_outline),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colorResource(R.color.theme_color)
                    )
                }
            },
            trailingIcon = {
                if (query.text.isNotEmpty()) {
                    Icon(
                        painter = painterResource(R.drawable.clear_all_icon),
                        contentDescription = "Clear Icon",
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onQueryChange(
                                TextFieldValue(
                                    text = "",
                                    selection = TextRange(0)
                                )
                            ) },
                        tint = colorResource(R.color.primary_text_color)
                    )
                }
            },
            modifier = modifier
                .border(
                    width = if (isKeyboardVisible) 1.1.dp else 0.dp,
                    color = if (isKeyboardVisible)
                        colorResource(R.color.theme_color).copy(alpha = 0.60f)
                    else
                        Color.Transparent,
                    shape = RoundedCornerShape(26.dp)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFfefefe),
                unfocusedContainerColor = Color(0xFFfefefe),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = colorResource(R.color.primary_text_color).copy(alpha = 0.88f)
            ),
            textStyle = TextStyle(
                fontFamily = fonts,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                fontSize = 16.sp, lineHeight = 18.sp,
                color = colorResource(R.color.secondary_text_color)
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            )
        )
    }
}

@Composable
fun SourceSelectorUI(
    modifier: Modifier,
    selected: SearchSource,
    onSourceSelected: (SearchSource) -> Unit
) {
    val options = listOf(
        SearchSource.JIOSAAVN to R.drawable.song_icon,
        SearchSource.YTVIDEO to R.drawable.youtube_icon,
        SearchSource.YTMUSIC to R.drawable.headset_icon
    )

    val scrollState = rememberScrollState()

    val selectedBorder = colorResource(R.color.theme_color)
    val selectedBg = colorResource(R.color.theme_color).copy(alpha = 0.10f)
    val unselectedBg = colorResource(R.color.secondary_text_color).copy(alpha = 0.10f)
    val textColor = colorResource(R.color.background_color)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(colorResource(R.color.primary_text_color).copy(alpha = 0.88f))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.scan_icon),
                    contentDescription = null,
                    tint = selectedBorder,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "SOURCES",
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.background_color), lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEach { (source, icon) ->
                    val isSelected = selected == source

                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) selectedBorder else colorResource(R.color.secondary_text_color).copy(alpha = 0.30f),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = ""
                    )

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) selectedBg else unselectedBg,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = ""
                    )

                    val textAnimatedColor by animateColorAsState(
                        targetValue = if (isSelected) colorResource(R.color.theme_color) else textColor,
                        label = ""
                    )

                    val scale by animateDpAsState(
                        targetValue = if (isSelected) 2.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = ""
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(bgColor)
                            .border(
                                width = 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable {
                                onSourceSelected(source)
                            }
                            .padding(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = null,
                                tint = textAnimatedColor,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(2.dp + scale))
                        }

                        Text(
                            text = source.name.lowercase(),
                            color = textAnimatedColor,
                            fontSize = 13.sp,
                            fontFamily = fonts,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("FrequentlyChangingValue")
@Composable
private fun SearchTabs(
    modifier: Modifier, source: SearchSource,
    searchText: String, downloadedIds: Set<String>,
    onSongMoreClick: (song: SongItem, songs: List<SongItem>, index: Int) -> Unit,
    snackBarHostState: SnackbarHostState,
    artists: List<Artists>,
    artistsUiSate: SearchArtistsUiState,
    onSearchArtists: (String) -> Unit,
    onSearchYTArtists: (String) -> Unit,
    onArtistsClearResult: () -> Unit,
    onArtistsIdeal: () -> Unit,
    songs: List<SongItem>,
    songsUiState: SearchSongsUiState,
    onSearchSongs: (String) -> Unit,
    onSearchYTSongs: (String) -> Unit,
    onSongsClearResult: () -> Unit,
    onSongsIdeal: () -> Unit,
    albums: List<DataItem>,
    albumUiState: SearchAlbumsUiState,
    onSearchAlbums: (String) -> Unit,
    onSearchYTAlbums: (String) -> Unit,
    onAlbumsClearResult: () -> Unit,
    onAlbumsIdeal: () -> Unit,
    playlists: List<DataItem>,
    playlistsUiState: SearchPlaylistUiState,
    onSearchPlaylists: (String) -> Unit,
    onSearchYTPlaylists: (String) -> Unit,
    onPlaylistsClearResult: () -> Unit,
    onPlaylistsIdeal: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()
    val tabs = remember {
        listOf("Artists", "Songs", "Albums", "Playlists")
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )

    val artistsGridState = rememberLazyGridState()
    val songsListState = rememberLazyListState()
    val albumsGridState = rememberLazyGridState()
    val playlistsGridState = rememberLazyGridState()

    Column(modifier = modifier) {
        val textWidths = remember { mutableStateMapOf<Int, Dp>() }

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            edgePadding = 20.dp,
            contentColor = colorResource(R.color.theme_color),
            indicator = { tabPositions ->
                val indicatorExtraPadding = 12.dp

                val current = pagerState.currentPage
                val offset = pagerState.currentPageOffsetFraction.coerceIn(0f, 1f)

                val currentTab = tabPositions[current]
                val nextTab = tabPositions.getOrNull(current + 1)

                val currentTextWidth = textWidths[current] ?: currentTab.width
                val nextTextWidth = textWidths[current + 1] ?: currentTextWidth

                val paddedCurrentWidth = currentTextWidth + indicatorExtraPadding * 2
                val paddedNextWidth = nextTextWidth + indicatorExtraPadding * 2

                val indicatorWidth = lerp(paddedCurrentWidth, paddedNextWidth, offset)

                val currentOffset =
                    currentTab.left +
                            (currentTab.width - paddedCurrentWidth) / 2

                val nextOffset =
                    (nextTab?.left ?: currentTab.left) +
                            ((nextTab?.width ?: currentTab.width) - paddedNextWidth) / 2

                val indicatorOffset = lerp(currentOffset, nextOffset, offset)

                Box(
                    Modifier
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = indicatorOffset)
                        .width(indicatorWidth)
                        .height(3.dp)
                        .background(
                            color = colorResource(R.color.theme_color),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                SmoothTab(
                    text = title,
                    selected = pagerState.currentPage == index,
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()

                        if (pagerState.currentPage != index) {
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
                    },
                    onTextMeasured = { width ->
                        textWidths[index] = width
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            flingBehavior = PagerDefaults.flingBehavior(pagerState)
        ) { page ->
            when(page) {
                0 -> SearchArtists(
                        query = searchText,
                        source = source,
                        modifier = Modifier,
                        gridState = artistsGridState,
                        artists = artists,
                        artistsUiSate = artistsUiSate,
                        onSearchArtists = { query ->
                            onSearchArtists(query)
                        },
                        onSearchYTArtists = { query ->
                            onSearchYTArtists(query)
                        },
                        onArtistsClearResult = onArtistsClearResult,
                        onArtistsIdeal = onArtistsIdeal
                    )

                1 -> SearchSongs(
                        query = searchText,
                        source = source,
                        modifier = Modifier,
                        downloadedIds = downloadedIds,
                        listState = songsListState,
                        onMoreClick = { song, songs, index ->
                            onSongMoreClick(song, songs, index)
                        },
                        snackBarHostState = snackBarHostState,
                        songs = songs,
                        songsUiState = songsUiState,
                        onSearchSongs = { query ->
                            onSearchSongs(query)
                        },
                        onSearchYTSongs = { query ->
                            onSearchYTSongs(query)
                        },
                        onSongsClearResult = onSongsClearResult,
                        onSongsIdeal = onSongsIdeal
                    )

                2 -> SearchAlbums(
                        query = searchText,
                        source = source,
                        modifier = Modifier,
                        gridState = albumsGridState,
                        albums = albums,
                        albumUiState = albumUiState,
                        onSearchAlbums = { query ->
                            onSearchAlbums(query)
                        },
                        onSearchYTAlbums = { query ->
                            onSearchYTAlbums(query)
                        },
                        onAlbumsClearResult = onAlbumsClearResult,
                        onAlbumsIdeal = onAlbumsIdeal
                    )

                3 -> SearchPlaylists(
                        query = searchText,
                        source = source,
                        modifier = Modifier,
                        gridState = playlistsGridState,
                        playlists = playlists,
                        playlistsUiState = playlistsUiState,
                        onSearchPlaylists = { query ->
                            onSearchPlaylists(query)
                        },
                        onSearchYTPlaylists = { query ->
                            onSearchYTPlaylists(query)
                        },
                        onPlaylistsClearResult = onPlaylistsClearResult,
                        onPlaylistsIdeal = onPlaylistsIdeal
                    )

            }
        }
    }
}

@Composable
fun RecentSearchSection(
    recentSearches: List<String>,
    onClear: () -> Unit,
    onSearchClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.recent_history),
                        contentDescription = null,
                        tint = colorResource(R.color.secondary_text_color),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "RECENT",
                        color = colorResource(R.color.secondary_text_color),
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 2.sp,
                        fontFamily = fonts,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal,
                    )
                }

                Text(
                    text = "CLEAR",
                    color = colorResource(R.color.secondary_text_color),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 2.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    modifier = Modifier.clickable {
                        onClear()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                recentSearches.forEach { item ->
                    Row(
                        modifier = Modifier
                            .clickable {
                                onSearchClick(item)
                            }
                            .clip(RoundedCornerShape(50))
                            .background(
                                colorResource(R.color.secondary_text_color).copy(alpha = 0.06f)
                            )
                            .border(
                                width = 1.dp,
                                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.10f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(
                                horizontal = 14.dp,
                                vertical = 12.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.clock_icon),
                            contentDescription = null,
                            tint = colorResource(R.color.secondary_text_color),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = item,
                            color = colorResource(R.color.primary_text_color),
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmoothTab(
    text: String, selected: Boolean, onClick: () -> Unit,
    onTextMeasured: (Dp) -> Unit, modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current

    val animatedColor by animateColorAsState(
        if (selected) colorResource(R.color.theme_color) else colorResource(R.color.secondary_text_color),
        label = "tabColor"
    )

    val scale by animateFloatAsState(
        if (selected) 1.05f else 1f,
        label = "tabScale"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.scale(scale).padding(vertical = 2.dp),
            fontSize = 14.sp, lineHeight = 16.sp,
            fontFamily = fonts,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            color = animatedColor,
            onTextLayout = {
                with(density) {
                    onTextMeasured(it.size.width.toDp())
                }
            }
        )
    }
}

@Composable
private fun SearchArtists(
    query: String,
    source: SearchSource,
    modifier: Modifier,
    gridState: LazyGridState,
    artists: List<Artists>,
    artistsUiSate: SearchArtistsUiState,
    onSearchArtists: (String) -> Unit,
    onSearchYTArtists: (String) -> Unit,
    onArtistsClearResult: () -> Unit,
    onArtistsIdeal: () -> Unit,
) {
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val primaryTextColor = colorResource(R.color.primary_text_color)

    LaunchedEffect(query, source) {
        if (query.trim().length < 2) {
            onArtistsClearResult()
            onArtistsIdeal()
            return@LaunchedEffect
        }

        when(source) {
            SearchSource.JIOSAAVN -> {
                onSearchArtists(query)
            }

            SearchSource.YTMUSIC -> {
                onSearchYTArtists(query)
            }

            else -> Unit
        }
    }

    when {
        query.length < 2 -> {
            EmptyState("Search for artists")
        }

        artistsUiSate is SearchArtistsUiState.Loading -> {
            LoadingEffect()
        }

        artistsUiSate is SearchArtistsUiState.Empty -> {
            ErrorState(
                message = "No artists found",
                onRetry = {
                    when(source) {
                        SearchSource.JIOSAAVN -> {
                            onSearchArtists(query)
                        }

                        SearchSource.YTMUSIC -> {
                            onSearchYTArtists(query)
                        }

                        else -> Unit
                    }
                }
            )
        }

        artistsUiSate is SearchArtistsUiState.Error -> {
            ErrorState(
                message = artistsUiSate.message,
                onRetry = {
                    when(source) {
                        SearchSource.JIOSAAVN -> {
                            onSearchArtists(query)
                        }

                        SearchSource.YTMUSIC -> {
                            onSearchYTArtists(query)
                        }

                        else -> Unit
                    }
                }
            )
        }

        artistsUiSate is SearchArtistsUiState.Success -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = artists,key = { it.id }
                ) { artist ->
                    val artistName = remember(artist.name) {
                        htmlToText(artist.name)
                    }

                    Column(
                        modifier = Modifier.hideKeyboardOnClick {
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
                                })
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

                        Text( modifier = Modifier.width(78.dp),
                            text = artistName,
                            fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = primaryTextColor, maxLines = 2, textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSongs(
    query: String,
    source: SearchSource,
    modifier: Modifier,
    downloadedIds: Set<String>,
    listState: LazyListState,
    onMoreClick: (SongItem, List<SongItem>, Int) -> Unit,
    snackBarHostState: SnackbarHostState,
    songs: List<SongItem>,
    songsUiState: SearchSongsUiState,
    onSearchSongs: (String) -> Unit,
    onSearchYTSongs: (String) -> Unit,
    onSongsClearResult: () -> Unit,
    onSongsIdeal: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val musicService = ServiceLocator.musicService

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val spectrumComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.music_spectrum)
    )

    val timerComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.timer)
    )

    val primaryTextColor = colorResource(R.color.primary_text_color)

    val secondaryTextColor = colorResource(R.color.secondary_text_color)

    val themeColor = colorResource(R.color.theme_color)

    LaunchedEffect(query, source) {
        if (query.trim().length < 2) {
            onSongsClearResult()
            onSongsIdeal()
            return@LaunchedEffect
        }

        when (source) {
            SearchSource.JIOSAAVN -> {
                onSearchSongs(query)
            }

            SearchSource.YTMUSIC -> {
                onSearchYTSongs(query)
            }

            else -> Unit
        }
    }

    when {
        query.length < 2 -> {
            EmptyState("Search for songs")
        }

        songsUiState is SearchSongsUiState.Loading -> {
            LoadingEffect()
        }

        songsUiState is SearchSongsUiState.Empty -> {
            ErrorState(
                message = "No songs found",
                onRetry = {
                    when (source) {
                        SearchSource.JIOSAAVN -> {
                            onSearchSongs(query)
                        }

                        SearchSource.YTMUSIC -> {
                            onSearchYTSongs(query)
                        }

                        else -> Unit
                    }
                }
            )
        }

        songsUiState is SearchSongsUiState.Error -> {
            ErrorState(
                message = songsUiState.message,
                onRetry = {
                    when (source) {
                        SearchSource.JIOSAAVN -> {
                            onSearchSongs(query)
                        }

                        SearchSource.YTMUSIC -> {
                            onSearchYTSongs(query)
                        }

                        else -> Unit
                    }
                }
            )
        }

        songsUiState is SearchSongsUiState.Success -> {
            LazyColumn (
                state = listState,
                modifier = modifier,
                contentPadding = PaddingValues(top = 8.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = songs,
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = if (currentSong?.id == song.id) 0.dp else 22.dp, end = 12.dp)
                                .hideKeyboardOnClick {
                                    scope.launch {
                                        try {
                                            PlayerManager.currentPlaylist = listOf(song)
                                            PlayerManager.currentIndex = 0

                                            val intent = Intent(
                                                context,
                                                MusicPlayerService::class.java
                                            ).apply {
                                                action = MusicPlayerService.ACTION_PLAY_NEW
                                                putExtra("index", 0)
                                                putExtra("from_search", true)
                                            }

                                            ContextCompat.startForegroundService(context, intent)
                                        } catch (e: Exception) {
                                            Log.e("PLAYER_ERROR", "Failed to play song", e)
                                        }
                                    }
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
                                    color = primaryTextColor,
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
                                    color = secondaryTextColor,
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
                                        color = secondaryTextColor,
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
                                        if (downloadUrl == null) {
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    "Download unavailable"
                                                )
                                            }
                                            return@IconButton
                                        }

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
                                                    themeColor.copy(alpha = 0.6f)
                                                else
                                                    primaryTextColor.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        onMoreClick(song, songs, index)
                                    }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(R.drawable.three_dots_icon),
                                        contentDescription = "Three Dots",
                                        tint = primaryTextColor.copy(alpha = 0.6f)
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
private fun SearchAlbums(
    query: String,
    source: SearchSource,
    modifier: Modifier,
    gridState: LazyGridState,
    albums: List<DataItem>,
    albumUiState: SearchAlbumsUiState,
    onSearchAlbums: (String) -> Unit,
    onSearchYTAlbums: (String) -> Unit,
    onAlbumsClearResult: () -> Unit,
    onAlbumsIdeal: () -> Unit
) {
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val primaryTextColor = colorResource(R.color.primary_text_color)
    val secondaryTextColor = colorResource(R.color.secondary_text_color)

    LaunchedEffect(query, source) {
        if (query.trim().length < 2) {
            onAlbumsClearResult()
            onAlbumsIdeal()
            return@LaunchedEffect
        }

        when (source) {
            SearchSource.JIOSAAVN -> {
                onSearchAlbums(query)
            }

            SearchSource.YTMUSIC -> {
                onSearchYTAlbums(query)
            }

            else -> Unit
        }
    }

    when {
        query.length < 2 -> {
            EmptyState("Search for albums")
        }

        albumUiState is SearchAlbumsUiState.Loading -> {
            LoadingEffect()
        }

        albumUiState is SearchAlbumsUiState.Empty -> {
            ErrorState(
                message = "No albums found",
                onRetry = {
                    when (source) {
                        SearchSource.JIOSAAVN -> {
                            onSearchAlbums(query)
                        }

                        SearchSource.YTMUSIC -> {
                            onSearchYTAlbums(query)
                        }

                        else -> Unit
                    }
                }
            )
        }

        albumUiState is SearchAlbumsUiState.Error -> {
            ErrorState(
                message = albumUiState.message,
                onRetry = {
                    when (source) {
                        SearchSource.JIOSAAVN -> {
                            onSearchAlbums(query)
                        }

                        SearchSource.YTMUSIC -> {
                            onSearchYTAlbums(query)
                        }

                        else -> Unit
                    }
                }
            )
        }

        albumUiState is SearchAlbumsUiState.Success -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(albums) { album ->
                    val albumName = remember(album.name) {
                        htmlToText(album.name)
                    }

                    val artistsName = remember(album.artist) {
                        htmlToText(
                            album.artist.takeIf { it.isNotEmpty() }
                                ?.joinToString(", ") { it.name }
                                ?: "Unknown Artist"
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hideKeyboardOnClick {
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
                                .fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                            text = albumName,
                            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = primaryTextColor, maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text( modifier = Modifier.padding(horizontal = 2.dp ),
                            text = artistsName,
                            fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = secondaryTextColor, maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPlaylists(
    query: String,
    source: SearchSource,
    modifier: Modifier,
    gridState: LazyGridState,
    playlists: List<DataItem>,
    playlistsUiState: SearchPlaylistUiState,
    onSearchPlaylists: (String) -> Unit,
    onSearchYTPlaylists: (String) -> Unit,
    onPlaylistsClearResult: () -> Unit,
    onPlaylistsIdeal: () -> Unit
) {
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val primaryTextColor = colorResource(R.color.primary_text_color)

    LaunchedEffect(query, source) {
        if (query.length < 2) {
            onPlaylistsClearResult()
            onPlaylistsIdeal()
            return@LaunchedEffect
        }

        when (source) {
            SearchSource.JIOSAAVN -> {
                onSearchPlaylists(query)
            }

            SearchSource.YTMUSIC -> {
               onSearchYTPlaylists(query)
            }

            else -> Unit
        }
    }

    when {
        query.length < 2 -> {
            EmptyState("Search for playlists")
        }

        playlistsUiState is SearchPlaylistUiState.Loading -> {
            LoadingEffect()
        }

        playlistsUiState is SearchPlaylistUiState.Empty -> {
            ErrorState(
                message = "No playlists found",
                onRetry = {
                    when (source) {
                        SearchSource.JIOSAAVN -> {
                            onSearchPlaylists(query)
                        }

                        SearchSource.YTMUSIC -> {
                            onSearchYTPlaylists(query)
                        }

                        else -> Unit
                    }
                }
            )
        }

        playlistsUiState is SearchPlaylistUiState.Error -> {
            ErrorState(
                message = playlistsUiState.message,
                onRetry = {
                    when (source) {
                        SearchSource.JIOSAAVN -> {
                            onSearchPlaylists(query)
                        }

                        SearchSource.YTMUSIC -> {
                            onSearchYTPlaylists(query)
                        }

                        else -> Unit
                    }
                }
            )
        }

        playlistsUiState is SearchPlaylistUiState.Success -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(playlists) { playlist ->
                    val playlistName = remember(playlist.name) {
                        htmlToText(playlist.name)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hideKeyboardOnClick {
                                val intent = Intent(context, PlaylistActivity::class.java).apply {
                                    putExtra("playlist_id", playlist.id)
                                    putExtra("playlist_imageUrl",
                                        when (playlist.searchSource) {
                                            SearchSource.YTMUSIC.name ->
                                                playlist.image.getOrNull(0)?.url

                                            SearchSource.JIOSAAVN.name ->
                                                playlist.image.getOrNull(2)?.url
                                                    ?: playlist.image.lastOrNull()?.url

                                            else ->
                                                playlist.image.lastOrNull()?.url
                                        }
                                    )
                                    putExtra("playlist_source",
                                        when(playlist.searchSource) {
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
                                        when(playlist.searchSource) {
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
                    ) {
                        AsyncImage(
                            model = when (playlist.searchSource) {
                                SearchSource.YTMUSIC.name ->
                                    playlist.image.getOrNull(0)?.url

                                SearchSource.JIOSAAVN.name ->
                                    playlist.image.getOrNull(2)?.url
                                        ?: playlist.image.lastOrNull()?.url

                                else ->
                                    playlist.image.lastOrNull()?.url
                            },
                            contentDescription = playlist.name,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.default_image),
                            modifier = Modifier
                                .fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                            text = playlistName,
                            fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = primaryTextColor, maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize().padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color)
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
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
            text = message,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
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

@Composable
private fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
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

@Preview(showSystemUi = true)
@Composable
private fun SearchScreenPreview() {
    SearchScreen(
        snackBarHostState = SnackbarHostState(),
        showSheet = false,
        onClickBack = {},
        onToggleLike = {},
        likedSongs = setOf(),
        downloadedIds = setOf(),
        onDeleteSong = {},
        artists = emptyList(),
        artistsUiSate = SearchArtistsUiState.Idle,
        onSearchArtists = {},
        onSearchYTArtists = {},
        onArtistsClearResult = {},
        onArtistsIdeal = {},
        songs = emptyList(),
        songsUiState = SearchSongsUiState.Idle,
        onSearchSongs = {},
        onSearchYTSongs = {},
        onSongsClearResult = {},
        onSongsIdeal = {},
        albums = emptyList(),
        albumUiState = SearchAlbumsUiState.Idle,
        onSearchAlbums = {},
        onSearchYTAlbums = {},
        onAlbumsClearResult = {},
        onAlbumsIdeal = {},
        playlists = emptyList(),
        playlistsUiState = SearchPlaylistUiState.Idle,
        onSearchPlaylists = {},
        onSearchYTPlaylists = {},
        onPlaylistsClearResult = {},
        onPlaylistsIdeal = {},
        libraryPlaylists = LibraryUiState(),
        onAddSongToPlaylist = { _,_,_ -> }
    )
}