package com.example.wavex.homeScreen

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.wavex.R
import com.example.wavex.albumScreen.AlbumActivity
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.fonts
import com.example.wavex.homeScreen.viewModel.AlbumsViewModel
import com.example.wavex.homeScreen.viewModel.ArtistsViewModel
import com.example.wavex.homeScreen.viewModel.NewReleasesSongsViewModel
import com.example.wavex.homeScreen.viewModel.PlaylistsViewModel
import com.example.wavex.homeScreen.viewModel.TrendingSongsViewModel
import com.example.wavex.playlistScreen.PlaylistActivity
import kotlinx.coroutines.delay

@Composable
fun HomeScreen (navController: NavController) {
    val scrollState = rememberScrollState()

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (logoIcon, profileAvatar, mainContent) = createRefs()

        Icon(painter = painterResource(R.drawable.wavex_logo_dark), contentDescription = "Logo Icon",
            tint = Color.Unspecified, modifier = Modifier.constrainAs(logoIcon) {
                top.linkTo(parent.top, margin = (-40).dp)
                start.linkTo(parent.start)
            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .size(158.dp).zIndex(2f)
        )

        AsyncImage(
            model = R.drawable.logo,
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.constrainAs(profileAvatar) {
                top.linkTo(parent.top, margin = 15.dp)
                end.linkTo(parent.end, margin = 22.dp)
            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .size(52.dp).clip(CircleShape).zIndex(2f),
            placeholder = painterResource(R.drawable.logo),
            error = painterResource(R.drawable.logo)
        )

        Column(modifier = Modifier.constrainAs(mainContent) {
            top.linkTo(parent.top, margin = 5.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .verticalScroll(scrollState)) {
            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                val (topPlaylistsSection,newReleasesTitle,newReleasesSection,popularArtistsTitle,popularArtistsSection,
                    trendingSongsTitle,trendingSongsSection,topAlbumsTitle,topAlbumsSection) = createRefs()

                Playlist("Top","results",modifier = Modifier.constrainAs(topPlaylistsSection) {
                    top.linkTo(parent.top, margin = 80.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })

                Text("New Releases", modifier = Modifier.constrainAs(newReleasesTitle) {
                    top.linkTo(topPlaylistsSection.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                )

                NewReleasesSongs("6689255","songs", modifier = Modifier.constrainAs(newReleasesSection) {
                    top.linkTo(newReleasesTitle.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })

                Text("Popular Artists", modifier = Modifier.constrainAs(popularArtistsTitle) {
                    top.linkTo(newReleasesSection.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                )

                Artists("top artists","results", modifier = Modifier.constrainAs(popularArtistsSection){
                    top.linkTo(popularArtistsTitle.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })

                Text("Trending Songs", modifier = Modifier.constrainAs(trendingSongsTitle) {
                    top.linkTo(popularArtistsSection.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                )

                TrendingSongs("946682072","songs", modifier = Modifier.constrainAs(trendingSongsSection) {
                    top.linkTo(trendingSongsTitle.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })

                Text("Top Albums", modifier = Modifier.constrainAs(topAlbumsTitle) {
                    top.linkTo(trendingSongsSection.bottom, margin = 20.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                )

                TopAlbums("latest","results", modifier = Modifier.constrainAs(topAlbumsSection) {
                    top.linkTo(topAlbumsTitle.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })
            }
        }
    }
}

@Composable
fun Playlist(query: String, root: String, modifier: Modifier, viewModel: PlaylistsViewModel = viewModel()) {
    val playlists by viewModel.playlists
    val context = LocalContext.current

    LaunchedEffect(query) {
        viewModel.fetchPlayListByQuery(query,root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (playlists.isEmpty()) return

    val listSize = playlists.size
    val infiniteItems = Int.MAX_VALUE
    val startIndex = infiniteItems / 2 - (infiniteItems / 2) % listSize

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val itemWidth = 140.dp
    val itemSpacing = 18.dp
    val sidePadding = (screenWidth - itemWidth) / 2

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            listState.animateScrollToItem(
                listState.firstVisibleItemIndex + 1
            )
        }
    }

    val scaleAlphaMap by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter =
                (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f

            val viewportWidth =
                layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

            val maxDistance = viewportWidth / 2f

            layoutInfo.visibleItemsInfo.associate { itemInfo ->
                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                val distance = kotlin.math.abs(viewportCenter - itemCenter)
                val fraction = (1f - distance / maxDistance).coerceIn(0f, 1f)

                val scale = 0.75f + 0.35f * fraction
                val alpha = 0.4f + 0.6f * fraction

                itemInfo.index to (scale to alpha)
            }
        }
    }

    LazyRow(modifier = modifier, state = listState, flingBehavior = snapFlingBehavior,
        contentPadding = PaddingValues(horizontal = sidePadding),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing)) {
        items(infiniteItems) { index ->
            val realItem = playlists[index % listSize]

            val scaleAndAlpha = scaleAlphaMap[index] ?: (0.75f to 0.4f)

            Column(
                modifier = Modifier
                    .width(itemWidth)
                    .graphicsLayer {
                        scaleX = scaleAndAlpha.first
                        scaleY = scaleAndAlpha.first
                        alpha = scaleAndAlpha.second
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        val intent = Intent(context, PlaylistActivity::class.java).apply {
                            putExtra("playlist_id", realItem.id)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        context.startActivity(intent)
                    }
            ) {
                AsyncImage(
                    model = realItem.image[2].url,
                    contentDescription = realItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(itemWidth)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text( modifier = Modifier.padding(horizontal = 8.dp ),
                    text = realItem.name,
                    fontSize = 12.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NewReleasesSongs(playlistId: String, root: String, modifier: Modifier, viewModel: NewReleasesSongsViewModel = viewModel()) {
    val songs by viewModel.songs

    LaunchedEffect(playlistId) {
        viewModel.fetchPlaylistsByID(playlistId, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (songs.isEmpty()) return

    LazyRow(modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        items(songs) { song ->
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {  }
            ) {
                AsyncImage(
                    model = song.image[2].url,
                    contentDescription = song.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(110.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                    text = song.name,
                    fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val artistsName = song.artist
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") { it.name }
                    ?: "Unknown Artist"

                Text( modifier = Modifier.padding(horizontal = 2.dp ),
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
fun Artists(query: String, root: String, modifier: Modifier, viewModel: ArtistsViewModel = viewModel()) {
    val artists = viewModel.artists.value
    val context = LocalContext.current

    LaunchedEffect(query) {
        viewModel.fetchArtistsByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (artists.isEmpty()) return

    LazyRow(modifier = modifier,
        contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp)) {
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
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = artist.image.takeIf { it.isNotBlank() },
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.default_artist),
                    error = painterResource(R.drawable.default_artist),
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text( modifier = Modifier.width(78.dp),
                    text = artist.name,
                    fontSize = 13.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 2, textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TrendingSongs(playlistId: String, root: String, modifier: Modifier, viewModel: TrendingSongsViewModel = viewModel()) {
    val songs by viewModel.songs

    LaunchedEffect(playlistId) {
        viewModel.fetchPlaylistsByID(playlistId, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (songs.isEmpty()) return

    LazyRow(modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        items(songs) { song ->
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {  }
            ) {
                AsyncImage(
                    model = song.image[2].url,
                    contentDescription = song.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(110.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                    text = song.name,
                    fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val artistsName = song.artist
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") { it.name }
                    ?: "Unknown Artist"

                Text( modifier = Modifier.padding(horizontal = 2.dp ),
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
fun TopAlbums(query: String, root: String, modifier: Modifier, viewModel: AlbumsViewModel = viewModel()) {
    val albums = viewModel.albums.value
    val context = LocalContext.current

    LaunchedEffect(query) {
        viewModel.fetchAlbumByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (albums.isEmpty()) return

    LazyRow(modifier = modifier,
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        items(albums) { album ->
            Column(
                modifier = Modifier
                    .width(110.dp)
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
                        .height(110.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                    text = album.name,
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

@Composable
@Preview(showSystemUi = true)
private fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}