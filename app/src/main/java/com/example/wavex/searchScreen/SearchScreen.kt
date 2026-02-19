package com.example.wavex.searchScreen

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.albumScreen.AlbumActivity
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.fonts
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.playlistScreen.PlaylistActivity
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
import com.example.wavex.searchScreen.uiState.SearchAlbumsUiState
import com.example.wavex.searchScreen.uiState.SearchArtistsUiState
import com.example.wavex.searchScreen.uiState.SearchPlaylistUiState
import com.example.wavex.searchScreen.uiState.SearchSongsUiState
import com.example.wavex.searchScreen.viewModel.SearchAlbumsViewModel
import com.example.wavex.searchScreen.viewModel.SearchArtistsViewModel
import com.example.wavex.searchScreen.viewModel.SearchPlaylistsViewModel
import com.example.wavex.searchScreen.viewModel.SearchSongsViewModel
import com.example.wavex.service.MusicPlayerService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun SearchScreen(navController: NavController) {
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var debouncedQuery by remember { mutableStateOf("") }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var showSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var currentSongs by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf(0) }

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsStateWithLifecycle()

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShareScale"
    )

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
        val(searchField, backButton, searchResults) = createRefs()

        Box(
            modifier = Modifier.constrainAs(backButton) {
            top.linkTo(searchField.top)
            bottom.linkTo(searchField.bottom)
            start.linkTo(parent.start, margin = 25.dp)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .size(36.dp).clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            ).clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                navController.popBackStack()
            }, contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_icon),
                contentDescription = "Back Icon",
                tint = colorResource(R.color.primary_text_color),
                modifier = Modifier.size(20.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }

        SearchBar(
            modifier = Modifier.constrainAs(searchField) {
                top.linkTo(parent.top, margin = 20.dp)
                start.linkTo(backButton.end)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 20.dp, end = 20.dp)
                .height(52.dp).focusRequester(focusRequester),
            query = searchText,
            onQueryChange = { newValue ->
                searchText = newValue
            }
        )

        SearchTabs(
            modifier = Modifier.constrainAs(searchResults){
            top.linkTo(searchField.bottom, margin = 12.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
            },debouncedQuery,
            onSongMoreClick = { song, songs, index ->
                selectedSong = song
                currentSongs = songs
                selectedIndex = index
                showSheet = true
            }
        )

        LaunchedEffect(Unit) {
            snapshotFlow { searchText.text }
                .debounce(450)
                .distinctUntilChanged()
                .collectLatest { text ->
                    debouncedQuery = text
                }
        }
    }

    if (showSheet && selectedSong != null) {

        val song = selectedSong!!
        val isFavourite = likedSongs.contains(song.id)

        SongOptionsBottomSheet(
            song = song,
            isFavourite = isFavourite,
            onDismiss = {
                showSheet = false
                selectedSong = null
            },
            onPlayNow = {
                val intent = Intent(context, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_PLAY_NEW
                    putExtra("index", selectedIndex)
                }

                PlayerManager.currentPlaylist = currentSongs
                PlayerManager.currentIndex = selectedIndex

                ContextCompat.startForegroundService(context, intent)

                scope.launch {
                    RecentlyPlayedManager.add(context, song)
                }

                showSheet = false
            },
            onToggleFavourite = {
                likedViewModel.toggleLike(song)
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

@Composable
private fun SearchBar(modifier: Modifier, query: TextFieldValue, onQueryChange: (TextFieldValue) -> Unit) {
    val selectionColors = TextSelectionColors(
        handleColor = Color(0xFF1C1C1C),
        backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search", fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
                fontSize = 16.sp, lineHeight = 18.sp, color = colorResource(R.color.secondary_text_color)) },
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
                        contentDescription = "Clear",
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onQueryChange(
                                TextFieldValue(
                                    text = "",
                                    selection = TextRange(0)
                                )
                            ) },
                        tint = colorResource(R.color.theme_color)
                    )
                }
            },
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                unfocusedContainerColor = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF121212)
            ),textStyle = TextStyle(
                fontFamily = fonts,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                fontSize = 16.sp, lineHeight = 18.sp,
                color = colorResource(R.color.secondary_text_color)
            )
        )
    }
}

@SuppressLint("FrequentlyChangingValue")
@Composable
private fun SearchTabs(modifier: Modifier, searchText: String,
                       onSongMoreClick: (song: SongItem, songs: List<SongItem>, index: Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()
    val tabs = listOf("Artists", "Songs", "Albums", "Playlists")

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
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
            flingBehavior = PagerDefaults.flingBehavior(pagerState)
        ) { page ->
            when(tabs[page]) {
                "Artists" -> {
                    SearchArtists(searchText, "results", modifier = Modifier, gridState = artistsGridState)
                }

                "Songs" -> {
                    SearchSongs(searchText, "results", modifier = Modifier, listState = songsListState,
                        onMoreClick = { song, songs, index ->
                            onSongMoreClick(song, songs, index)
                        }
                    )
                }

                "Albums" -> {
                    SearchAlbums(searchText, "results", modifier = Modifier, gridState = albumsGridState)
                }

                "Playlists" -> {
                    SearchPlaylists(searchText, "results", modifier = Modifier, gridState = playlistsGridState)
                }
            }
        }
    }
}

@Composable
private fun SmoothTab(text: String, selected: Boolean, onClick: () -> Unit,
              onTextMeasured: (Dp) -> Unit, modifier: Modifier = Modifier)
{
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
    root: String,
    modifier: Modifier,
    viewModel: SearchArtistsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(query) {
        when {
            query.isBlank() -> {
                viewModel.clearResults()
                viewModel.setIdle()
            }

            query.length < 2 -> {
                viewModel.clearResults()
                viewModel.setIdle()
            }

            else -> {
                viewModel.fetchArtistsByQuery(query, root)
            }
        }
    }

    when {
        query.length < 2 -> {
            EmptyState("Search for artists")
        }

        uiState is SearchArtistsUiState.Loading -> {
            LoadingEffect()
        }

        uiState is SearchArtistsUiState.Empty -> {
            ErrorState("No artists found")
        }

        uiState is SearchArtistsUiState.Error -> {
            ErrorState((uiState as SearchArtistsUiState.Error).message)
        }

        uiState is SearchArtistsUiState.Success -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(artists) { artist ->
                    Column(
                        modifier = Modifier.hideKeyboardOnClick {
                            val intent = Intent(context, ArtistActivity::class.java).apply {
                                putExtra("artist_id", artist.id)
                                putExtra("artist_imageUrl", artist.image)
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
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

                        Text( modifier = Modifier.width(78.dp),
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
private fun SearchSongs(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: SearchSongsViewModel = viewModel(),
    listState: LazyListState,
    onMoreClick: (SongItem, List<SongItem>, Int) -> Unit
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(query) {
        when {
            query.isBlank() -> {
                viewModel.clearResults()
                viewModel.setIdle()
            }

            query.length < 2 -> {
                viewModel.clearResults()
                viewModel.setIdle()
            }

            else -> {
                viewModel.fetchSongByQuery(query, root)
            }
        }
    }

    when {
        query.length < 2 -> {
            EmptyState("Search for songs")
        }

        uiState is SearchSongsUiState.Loading -> {
            LoadingEffect()
        }

        uiState is SearchSongsUiState.Empty -> {
            ErrorState("No songs found")
        }

        uiState is SearchSongsUiState.Error -> {
            ErrorState((uiState as SearchSongsUiState.Error).message)
        }

        uiState is SearchSongsUiState.Success -> {
            LazyColumn (
                state = listState,
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    Row (
                        modifier = Modifier.hideKeyboardOnClick {
                            val intent = Intent(context, MusicPlayerService::class.java).apply {
                                action = MusicPlayerService.ACTION_PLAY_NEW
                                putExtra("index", index)
                            }

                            PlayerManager.currentPlaylist = songs
                            PlayerManager.currentIndex = index

                            ContextCompat.startForegroundService(context, intent)

                            scope.launch {
                                RecentlyPlayedManager.add(context, song)
                            }
                        }.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
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

                            IconButton(onClick = { onMoreClick(song, songs, index) }) {
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

@Composable
private fun SearchAlbums(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: SearchAlbumsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(query) {
        when {
            query.isBlank() -> {
                viewModel.clearResults()
                viewModel.setIdle()
            }

            query.length < 2 -> {
                viewModel.clearResults()
                viewModel.setIdle()
            }

            else -> {
                viewModel.fetchAlbumByQuery(query, root)
            }
        }
    }

    when {
        query.length < 2 -> {
            EmptyState("Search for albums")
        }

        uiState is SearchAlbumsUiState.Loading -> {
            LoadingEffect()
        }

        uiState is SearchAlbumsUiState.Empty -> {
            ErrorState("No albums found")
        }

        uiState is SearchAlbumsUiState.Error -> {
            ErrorState((uiState as SearchAlbumsUiState.Error).message)
        }

        uiState is SearchAlbumsUiState.Success -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(albums) { album ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hideKeyboardOnClick {
                                val intent = Intent(context, AlbumActivity::class.java).apply {
                                    putExtra("album_id", album.id)
                                    putExtra("album_imageUrl", album.image[2].url)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        AsyncImage(
                            model = album.image[2].url,
                            contentDescription = album.name,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.default_image),
                            modifier = Modifier
                                .fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val albumName = htmlToText(album.name)

                        Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                            text = albumName,
                            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color), maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val artistsName = album.artist
                            .takeIf { it.isNotEmpty() }
                            ?.joinToString(", ") { it.name }
                            ?: "Unknown Artist"

                        Text( modifier = Modifier.padding(horizontal = 2.dp ),
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
private fun SearchPlaylists(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: SearchPlaylistsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(query) {
        when {
            query.isBlank() -> {
                viewModel.clearResults()
                viewModel.setIdle()
            }

            query.length < 2 -> {
                viewModel.clearResults()
                viewModel.setIdle()
            }

            else -> {
                viewModel.fetchPlayListByQuery(query, root)
            }
        }
    }

    when {
        query.length < 2 -> {
            EmptyState("Search for playlists")
        }

        uiState is SearchPlaylistUiState.Loading -> {
            LoadingEffect()
        }

        uiState is SearchPlaylistUiState.Empty -> {
            ErrorState("No playlists found")
        }

        uiState is SearchPlaylistUiState.Error -> {
            ErrorState((uiState as SearchPlaylistUiState.Error).message)
        }

        uiState is SearchPlaylistUiState.Success -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(playlists) { playlist ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hideKeyboardOnClick {
                                val intent = Intent(context, PlaylistActivity::class.java).apply {
                                    putExtra("playlist_id", playlist.id)
                                    putExtra("playlist_imageUrl", playlist.image[2].url)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        AsyncImage(
                            model = playlist.image[2].url,
                            contentDescription = playlist.name,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.default_image),
                            modifier = Modifier
                                .fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val playlistName = htmlToText(playlist.name)

                        Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
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
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth().padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color)
        )
    }
}

@Composable
private fun ErrorState(message: String) {
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

@Composable
private fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp).size(144.dp)
    )
}

@Preview(showSystemUi = true)
@Composable
private fun SearchScreenPreview() {
    val navController = rememberNavController()
    SearchScreen(navController)
}