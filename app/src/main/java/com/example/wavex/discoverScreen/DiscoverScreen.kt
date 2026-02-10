package com.example.wavex.discoverScreen

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import com.example.wavex.discoverScreen.viewModel.ExploreAlbumsViewModel
import com.example.wavex.discoverScreen.viewModel.ExploreArtistsViewModel
import com.example.wavex.discoverScreen.viewModel.ExplorePlaylistsViewModel
import com.example.wavex.discoverScreen.viewModel.ExploreSongsViewModel
import com.example.wavex.fonts
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.playlistScreen.PlaylistActivity
import com.example.wavex.service.MusicPlayerService
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun DiscoverScreen(navController: NavController) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShareScale"
    )

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(backButton, titleText, categoryTabs, contentPager) = createRefs()

        Text("Explore", modifier = Modifier.constrainAs(titleText) {
            top.linkTo(parent.top, margin = 22.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
            fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color), lineHeight = 22.sp
        )

        Box(modifier = Modifier.constrainAs(backButton) {
            top.linkTo(titleText.top)
            bottom.linkTo(titleText.bottom)
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
                contentDescription = "add Icon",
                tint = colorResource(R.color.primary_text_color),
                modifier = Modifier.size(20.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
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

        val suggestedGridState = rememberLazyGridState()
        val songsListState = rememberLazyListState()
        val artistsGridState = rememberLazyGridState()
        val albumsGridState = rememberLazyGridState()
        val playlistsGridState = rememberLazyGridState()

        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        val interactionSource = remember { MutableInteractionSource() }

        LazyRow(
            state = listState,
            modifier = Modifier.constrainAs(categoryTabs) {
                top.linkTo(titleText.bottom, margin = 22.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(categoriesList) { index, category ->
                val isSelected = pagerState.currentPage == index

                val bgColor by animateColorAsState(
                    if (isSelected) colorResource(R.color.theme_color)
                    else colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                    label = "bg"
                )

                val textColor by animateColorAsState(
                    if (isSelected) colorResource(R.color.background_color)
                    else colorResource(R.color.primary_text_color).copy(alpha = 0.6f),
                    label = "text"
                )

                val scale by animateFloatAsState(
                    if (isSelected) 1.05f else 1f,
                    label = "tabScale"
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    modifier = Modifier
                        .height(36.dp)
                        .selectable(
                            selected = isSelected,
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        index,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                            } }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.scale(scale),
                            text = category,
                            fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal, fontSize = 13.sp, color = textColor
                        )
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.constrainAs(contentPager) {
                top.linkTo(categoryTabs.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            }
        ) { page ->
            when(categoriesList[page]) {
                "Suggested" -> {
                    ExploreGrid(modifier = Modifier.fillMaxSize(), gridState = suggestedGridState)
                }

                "Songs" -> {
                    ExploreSongs(
                        "946682072", "songs",
                        modifier = Modifier.fillMaxSize().padding(top = 3.dp), listState = songsListState
                    )
                }

                "Artists" -> {
                    ExploreArtists(
                        "top artists", "results",
                        modifier = Modifier.fillMaxSize().padding(top = 3.dp), gridState = artistsGridState
                    )
                }

                "Albums" -> {
                    ExploreAlbums(
                        "popular", "results",
                        modifier = Modifier.fillMaxSize().padding(top = 3.dp), gridState = albumsGridState
                    )
                }

                "Playlists" -> {
                    ExplorePlaylist(
                        "Top", "results",
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
}

@Composable
fun ExploreGrid(modifier: Modifier, gridState: LazyGridState) {
    val exploreList = listOf(
        BrowseItem("Made For You", colorFromTitle("Made For You"), R.drawable.logo2),
        BrowseItem("New Releases", colorFromTitle("New Releases"), R.drawable.logo2),
        BrowseItem("Hindi", colorFromTitle("Hindi"), R.drawable.logo2),
        BrowseItem("English", colorFromTitle("English"), R.drawable.logo2),
        BrowseItem("Punjabi", colorFromTitle("Punjabi"), R.drawable.logo2),
        BrowseItem("Rajasthani", colorFromTitle("Rajasthani"), R.drawable.logo2),
        BrowseItem("Haryanvi", colorFromTitle("Haryanvi"), R.drawable.logo2),
        BrowseItem("Telugu", colorFromTitle("Telugu"), R.drawable.logo2),
        BrowseItem("Marathi", colorFromTitle("Marathi"), R.drawable.logo2),
        BrowseItem("Gujarati", colorFromTitle("Gujarati"), R.drawable.logo2),
    )

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(exploreList) { item ->
            Box(
                modifier = Modifier
                    .aspectRatio(1.9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.color)
            ) {
                Text(
                    text = item.title,
                    color = colorResource(R.color.background_color),
                    fontSize = 15.sp, lineHeight = 16.sp,
                    fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    modifier = Modifier
                        .padding(12.dp).width(85.dp)
                        .align(Alignment.TopStart)
                )

                Image(
                    painter = painterResource(item.image),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 20.dp)
                        .graphicsLayer {
                            rotationZ = 22f
                            shape = RoundedCornerShape(12.dp)
                            clip = true
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
fun colorFromTitle(title: String): Color {
    val hue = (title.hashCode() % 360).absoluteValue.toFloat()
    return Color.hsl(hue, 0.65f, 0.45f)
}

@Composable
fun ExploreSongs(
    playlistId: String,
    root: String,
    modifier: Modifier,
    viewModel: ExploreSongsViewModel = viewModel(),
    listState: LazyListState
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(playlistId) {
        viewModel.fetchPlaylistsByID(playlistId, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    when {
        isLoading -> {
            LoadingEffect()
        }

        songs.isEmpty() -> {
            ErrorState("No results found")
        }

        else -> {
            LazyColumn (
                state = listState,
                modifier = modifier,
                contentPadding = PaddingValues(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                val intent = Intent(context, MusicPlayerService::class.java).apply {
                                    action = MusicPlayerService.ACTION_PLAY_NEW
                                    putParcelableArrayListExtra("playlist", ArrayList(songs))
                                    putExtra("index", index)
                                }

                                ContextCompat.startForegroundService(context, intent)

                                scope.launch {
                                    RecentlyPlayedManager.add(context, song)
                                }
                            },
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

                            IconButton(onClick = { }) {
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

    LaunchedEffect(query) {
        viewModel.fetchArtistsByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    when {
        isLoading -> {
            LoadingEffect()
        }

        artists.isEmpty() -> {
            ErrorState("No results found")
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
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, ArtistActivity()::class.java).apply {
                                putExtra("artist_id", artist.id)
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
                            placeholder = painterResource(R.drawable.default_artist),
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

    LaunchedEffect(query) {
        viewModel.fetchAlbumByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    when {
        isLoading -> {
            LoadingEffect()
        }

        albums.isEmpty() -> {
            ErrorState("No results found")
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
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                val intent = Intent(context, AlbumActivity()::class.java).apply {
                                    putExtra("album_id", album.id)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            }
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

                        val albumName = htmlToText(album.name)

                        Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
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

    LaunchedEffect(query) {
        viewModel.fetchPlayListByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    when {
        isLoading -> {
            LoadingEffect()
        }

        playlists.isEmpty() -> {
            ErrorState("No results found")
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
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                val intent = Intent(context, PlaylistActivity::class.java).apply {
                                    putExtra("playlist_id", playlist.id)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                            }
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

@Composable
fun ErrorState(message: String) {
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

@Preview(showSystemUi = true)
@Composable
private fun DiscoverScreenPreview() {
    val navController = rememberNavController()
    DiscoverScreen(navController)
}