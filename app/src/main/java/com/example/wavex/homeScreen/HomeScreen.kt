package com.example.wavex.homeScreen

import android.content.Context
import android.content.Intent
import android.text.Html
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
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
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import com.example.wavex.homeScreen.viewModel.AlbumsViewModel
import com.example.wavex.homeScreen.viewModel.ArtistsViewModel
import com.example.wavex.homeScreen.viewModel.NewReleasesSongsViewModel
import com.example.wavex.homeScreen.viewModel.PlaylistsViewModel
import com.example.wavex.homeScreen.viewModel.ProfileViewModel
import com.example.wavex.homeScreen.viewModel.TrendingSongsViewModel
import com.example.wavex.playlistScreen.PlaylistActivity
import com.example.wavex.profileScreen.ProfileActivity
import com.example.wavex.service.MusicPlayerService
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

object PlayerManager {
    var currentPlaylist: List<SongItem> = emptyList()
    var currentIndex: Int = 0
}

val Context.musicDataStore by preferencesDataStore("waveX_datastore")

object RecentlyPlayedManager {
    private val RECENTLY_PLAYED_KEY = stringPreferencesKey("recently_played")

    private val gson = Gson()
    private val type = object : TypeToken<List<SongItem>>() {}.type

    suspend fun add(context: Context, song: SongItem, maxSize: Int = 20) {
        context.musicDataStore.edit { prefs ->

            val currentList = prefs[RECENTLY_PLAYED_KEY]
                ?.let { gson.fromJson<List<SongItem>>(it, type) }
                ?.toMutableList()
                ?: mutableListOf()

            // Remove duplicate
            currentList.removeAll { it.id == song.id }

            // Add to top
            currentList.add(0, song)

            // Limit size
            if (currentList.size > maxSize) {
                currentList.subList(maxSize, currentList.size).clear()
            }

            prefs[RECENTLY_PLAYED_KEY] = gson.toJson(currentList)
        }
    }

    fun flow(context: Context) =
        context.musicDataStore.data.map { prefs ->
            prefs[RECENTLY_PLAYED_KEY]
                ?.let { gson.fromJson<List<SongItem>>(it, type) }
                ?: emptyList()
        }
}

object ProfilePrefs {
    val Context.dataStore by preferencesDataStore("profile")

    val PROFILE_URL = stringPreferencesKey("profile_url")
    val USER_NAME = stringPreferencesKey("user_name")

    suspend fun saveProfileUrl(context: Context, url: String) {
        context.dataStore.edit {
            it[PROFILE_URL] = url
        }
    }

    suspend fun saveUserName(context: Context, name: String) {
        context.dataStore.edit {
            it[USER_NAME] = name
        }
    }

    fun getProfileUrl(context: Context) =
        context.dataStore.data.map {
            it[PROFILE_URL]
        }

    fun getUserName(context: Context) =
        context.dataStore.data.map {
            it[USER_NAME]
        }
}

@Composable
fun HomeScreen (navController: NavController) {
    val isPreview = LocalInspectionMode.current

    val auth = remember(isPreview) {
        if (!isPreview) FirebaseAuth.getInstance() else null
    }
    val userID = auth?.currentUser?.uid

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val viewModel: ProfileViewModel = viewModel()

    val imageUrl by viewModel.profileImageUrl.collectAsStateWithLifecycle()

    LaunchedEffect(userID) {
        userID?.let { viewModel.silentRefresh(it) }
    }

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

    val playlistsVM: PlaylistsViewModel = viewModel()
    val newReleasesVM: NewReleasesSongsViewModel = viewModel()
    val trendingVM: TrendingSongsViewModel = viewModel()
    val albumsVM: AlbumsViewModel = viewModel()
    val artistsVM: ArtistsViewModel = viewModel()

    val playlists by playlistsVM.playlists
    val newReleases by newReleasesVM.songs
    val trending by trendingVM.songs
    val albums by albumsVM.albums
    val artists by artistsVM.artists

    val recentSongs by RecentlyPlayedManager
        .flow(context)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val isLoading by remember(playlistsVM.isLoading, newReleasesVM.isLoading,
        trendingVM.isLoading, albumsVM.isLoading, artistsVM.isLoading) {
        derivedStateOf {
            playlistsVM.isLoading || newReleasesVM.isLoading ||
                    trendingVM.isLoading || albumsVM.isLoading || artistsVM.isLoading
        }
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (isScrollingDown) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "Logo Fade"
    )

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (logoIcon, profileAvatar, mainContent, loader) = createRefs()

        Icon(painter = painterResource(R.drawable.wavex_logo_dark), contentDescription = "Logo Icon",
            tint = Color.Unspecified,
            modifier = Modifier.constrainAs(logoIcon) {
                top.linkTo(parent.top, margin = (-40).dp)
                start.linkTo(parent.start)
            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .size(158.dp)
                .graphicsLayer {
                    alpha = logoAlpha
                }.zIndex(20f)
        )

        AsyncImage(
            model = imageUrl,
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.constrainAs(profileAvatar) {
                top.linkTo(parent.top, margin = 15.dp)
                end.linkTo(parent.end, margin = 22.dp)
            }.clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                val intent = Intent(context, ProfileActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(intent)
            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .size(52.dp)
                .clip(CircleShape)
                .graphicsLayer {
                    alpha = logoAlpha
                }
                .zIndex(20f)
        )

        Column(modifier = Modifier.constrainAs(mainContent) {
            top.linkTo(parent.top, margin = 5.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .verticalScroll(scrollState))
        {
            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                val (topPlaylistsSection,recentlyPlayedTitle,recentlyPlayedSection,newReleasesTitle,newReleasesSection,popularArtistsTitle,
                    popularArtistsSection,trendingSongsTitle,trendingSongsSection,topAlbumsTitle,topAlbumsSection) = createRefs()

                Playlist("Top","results",modifier = Modifier.constrainAs(topPlaylistsSection) {
                    top.linkTo(parent.top, margin = 80.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, playlistsVM)

                if (recentSongs.isNotEmpty() && playlists.isNotEmpty()) {
                    Text("Recently Played", modifier = Modifier.constrainAs(recentlyPlayedTitle) {
                        top.linkTo(topPlaylistsSection.bottom, margin = 25.dp)
                        start.linkTo(parent.start, margin = 25.dp)
                    }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                    )

                    RecentlyPlayedSongs(recentSongs, modifier = Modifier.constrainAs(recentlyPlayedSection) {
                        top.linkTo(recentlyPlayedTitle.bottom, margin = 15.dp)
                        start.linkTo(parent.start)
                    })
                }

                if (newReleases.isNotEmpty()) {
                    Text("New Releases", modifier = Modifier.constrainAs(newReleasesTitle) {
                        top.linkTo(if (recentSongs.isNotEmpty()) recentlyPlayedSection.bottom else topPlaylistsSection.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 25.dp)
                    }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                    )
                }

                NewReleasesSongs("6689255","songs", modifier = Modifier.constrainAs(newReleasesSection) {
                    top.linkTo(newReleasesTitle.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, newReleasesVM)

                if (artists.isNotEmpty()) {
                    Text("Popular Artists", modifier = Modifier.constrainAs(popularArtistsTitle) {
                        top.linkTo(newReleasesSection.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 25.dp)
                    }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                    )
                }

                Artists("top artists","results", modifier = Modifier.constrainAs(popularArtistsSection){
                    top.linkTo(popularArtistsTitle.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, artistsVM)

                if (trending.isNotEmpty()) {
                    Text("Trending Songs", modifier = Modifier.constrainAs(trendingSongsTitle) {
                        top.linkTo(popularArtistsSection.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 25.dp)
                    }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                    )
                }

                TrendingSongs("946682072","songs", modifier = Modifier.constrainAs(trendingSongsSection) {
                    top.linkTo(trendingSongsTitle.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, trendingVM)

                if (albums.isNotEmpty()) {
                    Text("Top Albums", modifier = Modifier.constrainAs(topAlbumsTitle) {
                        top.linkTo(trendingSongsSection.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 25.dp)
                    }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                    )
                }

                TopAlbums("latest","results", modifier = Modifier.constrainAs(topAlbumsSection) {
                    top.linkTo(topAlbumsTitle.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, albumsVM)
            }
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
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                LoadingEffect()
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

    LaunchedEffect(listState, listSize) {
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
                            putExtra("playlist_imageUrl", realItem.image[2].url)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        context.startActivity(intent)
                    }
            ) {
                AsyncImage(
                    model = realItem.image[2].url,
                    contentDescription = realItem.name,
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.default_image),
                    modifier = Modifier
                        .height(itemWidth)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                val playlistName = htmlToText(realItem.name)

                Text( modifier = Modifier.padding(horizontal = 8.dp ),
                    text = playlistName,
                    fontSize = 12.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentlyPlayedSongs(recentSongs: List<SongItem>, modifier: Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val columns = remember(recentSongs) {
        recentSongs.chunked(3)
    }

    LazyRow(
        modifier = modifier.height(230.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(items = columns,
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
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                val songIndex = recentSongs.indexOfFirst { it.id == song.id }
                                if (songIndex == -1) return@clickable

                                val intent = Intent(context, MusicPlayerService::class.java).apply {
                                    action = MusicPlayerService.ACTION_PLAY_NEW
                                    putExtra("index", songIndex)
                                }

                                PlayerManager.currentPlaylist = recentSongs
                                PlayerManager.currentIndex = songIndex

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
                    }
                }
            }
        }
    }
}

@Composable
fun NewReleasesSongs(
    playlistId: String, root: String, modifier: Modifier,
    viewModel: NewReleasesSongsViewModel = viewModel())
{
    val songs by viewModel.songs
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(playlistId) {
        viewModel.fetchPlaylistsByID(playlistId, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (songs.isEmpty()) return

    LazyRow(modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            Column(
                modifier = Modifier
                    .width(110.dp)
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

                        scope.launch {
                            RecentlyPlayedManager.add(context, song)
                        }
                    }
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

                val songName = htmlToText(song.name)

                Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
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
        items(artists, key = { it.id }) { artist ->
            Column(
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    val intent = Intent(context, ArtistActivity::class.java).apply {
                        putExtra("artist_id", artist.id)
                        putExtra("artist_imageUrl", artist.image)
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
                    error = painterResource(R.drawable.default_artist),
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val artistName = htmlToText(artist.name)

                Text( modifier = Modifier.width(78.dp),
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
fun TrendingSongs(playlistId: String, root: String, modifier: Modifier, viewModel: TrendingSongsViewModel = viewModel()) {
    val songs by viewModel.songs
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(playlistId) {
        viewModel.fetchPlaylistsByID(playlistId, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (songs.isEmpty()) return

    LazyRow(modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            Column(
                modifier = Modifier
                    .width(110.dp)
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

                        scope.launch {
                            RecentlyPlayedManager.add(context, song)
                        }
                    }
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

                val songName = htmlToText(song.name)

                Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
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
                        .height(110.dp)
                        .fillMaxWidth()
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
fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.fillMaxWidth().size(144.dp)
    )
}

@Composable
@Preview(showSystemUi = true)
private fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}