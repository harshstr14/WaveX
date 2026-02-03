package com.example.wavex.searchScreen

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.fonts
import com.example.wavex.searchScreen.viewModel.SearchAlbumsViewModel
import com.example.wavex.searchScreen.viewModel.SearchArtistsViewModel
import com.example.wavex.searchScreen.viewModel.SearchPlaylistsViewModel
import com.example.wavex.searchScreen.viewModel.SearchSongsViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun SearchScreen(navController: NavController) {
    val focusRequester = remember { FocusRequester() }

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var debouncedQuery by remember { mutableStateOf("") }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(searchBar,backIcon,searchTab) = createRefs()

        Box(modifier = Modifier.constrainAs(backIcon) {
            top.linkTo(searchBar.top)
            bottom.linkTo(searchBar.bottom)
            start.linkTo(parent.start, margin = 25.dp)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .size(36.dp).clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                color = Color(0xFF797979),
                shape = RoundedCornerShape(20.dp)
            ).clickable {
                navController.popBackStack()
            }, contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_icon),
                contentDescription = "add Icon",
                tint = Color(0xFFF6F6F6),
                modifier = Modifier.size(20.dp)
            )
        }

        SearchBar(
            modifier = Modifier.constrainAs(searchBar) {
                top.linkTo(parent.top, margin = 20.dp)
                start.linkTo(backIcon.end)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 20.dp, end = 20.dp)
                .height(52.dp).focusRequester(focusRequester),
            query = searchText,
            onQueryChange = { newValue ->
                searchText = newValue
            }
        )

        SearchTabs(modifier = Modifier.constrainAs(searchTab){
            top.linkTo(searchBar.bottom, margin = 12.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
            },debouncedQuery
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
}

@Composable
fun Modifier.hideKeyboardOnClick(onClick: () -> Unit): Modifier {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    return clickable {
        focusManager.clearFocus()
        keyboardController?.hide()
        onClick()
    }
}

@Composable
private  fun SearchBar(modifier: Modifier, query: TextFieldValue, onQueryChange: (TextFieldValue) -> Unit) {
    val selectionColors = TextSelectionColors(
        handleColor = Color(0xFF1C1C1C),
        backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search", fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
                fontSize = 16.sp, lineHeight = 18.sp, color = Color(0xFFBCBCBC)) },
            singleLine = true,
            leadingIcon = {
                Box(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search_outline),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF34A853)
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
                        tint = Color(0xFF34A853)
                    )
                }
            },
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF797979).copy(alpha = 0.4f),
                unfocusedContainerColor = Color(0xFF797979).copy(alpha = 0.4f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF121212)
            ),textStyle = TextStyle(
                fontFamily = fonts,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                fontSize = 16.sp, lineHeight = 18.sp,
                color = Color(0xFFBCBCBC)
            )
        )
    }
}

@SuppressLint("FrequentlyChangingValue")
@Composable
fun SearchTabs(modifier: Modifier, searchText: String) {
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
            contentColor = Color(0xFF34A853),
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
                            color = Color(0xFF34A853),
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
                    SearchSongs(searchText, "results", modifier = Modifier, listState = songsListState)
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
fun SmoothTab(text: String, selected: Boolean, onClick: () -> Unit,
              onTextMeasured: (Dp) -> Unit, modifier: Modifier = Modifier)
{
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current

    val animatedColor by animateColorAsState(
        if (selected) Color(0xFF34A853) else Color(0xFF797979),
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
fun SearchArtists(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: SearchArtistsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val artists = viewModel.artists.value
    val isLoading by viewModel.isLoading

    LaunchedEffect(query) {
        if (query.length < 2) {
            viewModel.clearResults()
            return@LaunchedEffect
        }

        viewModel.fetchArtistsByQuery(query, root)
    }

    when {
        isLoading -> {
            LoadingEffect()
        }

        artists.isEmpty() -> {
            EmptyState("No artists found")
        }

        else -> {
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
                        modifier = Modifier.hideKeyboardOnClick {  } ,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = artist.image,
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(82.dp)
                                .fillMaxWidth()
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text( modifier = Modifier.width(78.dp),
                            text = artist.name,
                            fontSize = 13.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = Color(0xFFF6F6F6), maxLines = 2, textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSongs(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: SearchSongsViewModel = viewModel(),
    listState: LazyListState
) {
    val songs by viewModel.songs
    val isLoading by viewModel.isLoading

    LaunchedEffect(query) {
        if (query.length < 2) {
            viewModel.clearResults()
            return@LaunchedEffect
        }

        viewModel.fetchSongByQuery(query, root)
    }

    when {
        isLoading -> {
            LoadingEffect()
        }

        songs.isEmpty() -> {
            EmptyState("No songs found")
        }

        else -> {
            LazyColumn (
                state = listState,
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                items(songs) { song ->
                    Row (
                        modifier = Modifier.hideKeyboardOnClick { }
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.image[2].url,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = song.name,
                                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                color = Color(0xFFF6F6F6), maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val artistsName = song.artist
                                .takeIf { it.isNotEmpty() }
                                ?.joinToString(", ") { it.name }
                                ?: "Unknown Artist"

                            Text(
                                text = artistsName,
                                fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                color = Color(0xFF797979), maxLines = 1,
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
                                    tint = Color(0xFF9E9E9E)
                                )
                            }

                            IconButton(onClick = { }) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.three_dots_icon),
                                    contentDescription = "Three Dots",
                                    tint = Color(0xFF9E9E9E)
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
fun SearchAlbums(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: SearchAlbumsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val albums = viewModel.albums.value
    val isLoading by viewModel.isLoading

    LaunchedEffect(query) {
        if (query.length < 2) {
            viewModel.clearResults()
            return@LaunchedEffect
        }

        viewModel.fetchAlbumByQuery(query, root)
    }

    when {
        isLoading -> {
            LoadingEffect()
        }

        albums.isEmpty() -> {
            EmptyState("No albums found")
        }

        else -> {
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
                            .hideKeyboardOnClick {  }
                    ) {
                        AsyncImage(
                            model = album.image[2].url,
                            contentDescription = album.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                            text = album.name,
                            fontSize = 13.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = Color(0xFFF6F6F6), maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val artistsName = album.artist
                            .takeIf { it.isNotEmpty() }
                            ?.joinToString(", ") { it.name }
                            ?: "Unknown Artist"

                        Text( modifier = Modifier.padding(horizontal = 2.dp ),
                            text = artistsName,
                            fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = Color(0xFF797979), maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchPlaylists(
    query: String,
    root: String,
    modifier: Modifier,
    viewModel: SearchPlaylistsViewModel = viewModel(),
    gridState: LazyGridState
) {
    val playlists by viewModel.playlists
    val isLoading by viewModel.isLoading

    LaunchedEffect(query) {
        if (query.length < 2) {
            viewModel.clearResults()
            return@LaunchedEffect
        }

        viewModel.fetchPlayListByQuery(query, root)
    }

    when {
        isLoading -> {
            LoadingEffect()
        }

        playlists.isEmpty() -> {
            EmptyState("No playlists found")
        }

        else -> {
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
                            .hideKeyboardOnClick {  }
                    ) {
                        AsyncImage(
                            model = playlist.image[2].url,
                            contentDescription = playlist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                            text = playlist.name,
                            fontSize = 13.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = Color(0xFFF6F6F6), maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth().padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF797979)
        )
    }
}

@Composable
fun LoadingEffect() {
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