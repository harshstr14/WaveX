package com.example.wavex.homeScreen

import android.content.Context
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.HttpClientProvider
import com.example.wavex.R
import com.example.wavex.albumScreen.AlbumActivity
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.downloadSong.data.DatabaseProvider
import com.example.wavex.downloadSong.repository.DownloadRepository
import com.example.wavex.fonts
import com.example.wavex.homeScreen.viewModel.AlbumsViewModel
import com.example.wavex.homeScreen.viewModel.ArtistsViewModel
import com.example.wavex.homeScreen.viewModel.NewReleasesSongsViewModel
import com.example.wavex.homeScreen.viewModel.PlaylistsViewModel
import com.example.wavex.homeScreen.viewModel.ProfileViewModel
import com.example.wavex.homeScreen.viewModel.TrendingSongsViewModel
import com.example.wavex.playlistScreen.PlaylistActivity
import com.example.wavex.profileScreen.ProfileActivity
import com.example.wavex.recommendation.MusicHistoryRepository
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ServiceLocator
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

object PlayerManager {
    var currentPlaylist: List<SongItem> = emptyList()
    var currentIndex: Int = 0
}

object AppContainer {
    lateinit var downloadRepository: DownloadRepository

    fun init(context: Context) {
        val db = DatabaseProvider.getDatabase(context)
        downloadRepository = DownloadRepository(db.downloadDao())
    }
}

object ParallelDownloader {
    private val semaphore = kotlinx.coroutines.sync.Semaphore(5)
    private val jobs = mutableMapOf<String, Job>()

    enum class DownloadState {
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        FAILED
    }

    val downloadStates = mutableStateMapOf<String, DownloadState>()

    fun getState(songId: String): DownloadState? {
        return downloadStates[songId]
    }

    fun pause(songId: String) {
        jobs[songId]?.cancel()
        jobs.remove(songId)

        downloadStates[songId] = DownloadState.PAUSED
    }

    fun resume(
        scope: CoroutineScope,
        songId: String,
        url: String,
        fileName: String,
        context: Context,
        onFinished: suspend (String?) -> Unit
    ) {
        if (downloadStates[songId] != DownloadState.PAUSED) return

        if (jobs[songId]?.isActive == true) return

        jobs[songId]?.cancel()
        jobs.remove(songId)

        downloadStates[songId] = DownloadState.DOWNLOADING

        val job = scope.launch {
            Log.d("DOWNLOAD_DEBUG", "Resume download for $songId")

            try {
                val path = semaphore.withPermit {
                    downloadSong(url, fileName, context)
                }

                if (isActive) {
                    downloadStates[songId] =
                        if (path != null) DownloadState.COMPLETED else DownloadState.FAILED

                    onFinished(path)
                }

            } catch (e: CancellationException) {
                Log.d("DOWNLOAD_DEBUG", "Cancelled $songId")
                throw e

            } finally {
                jobs.remove(songId)
            }
        }

        jobs[songId] = job
    }

    fun start(
        scope: CoroutineScope,
        songId: String,
        url: String,
        fileName: String,
        context: Context,
        onFinished: suspend (String?) -> Unit
    ) {
        val currentState = downloadStates[songId]

        if (jobs[songId]?.isActive == true && currentState == DownloadState.DOWNLOADING) return

        jobs[songId]?.cancel()
        jobs.remove(songId)

        downloadStates[songId] = DownloadState.DOWNLOADING

        val job = scope.launch {
            Log.d("DOWNLOAD_DEBUG", "Start download for $songId")

            try {
                val path = semaphore.withPermit {
                    downloadSong(url, fileName, context)
                }

                if (isActive) {
                    downloadStates[songId] =
                        if (path != null) DownloadState.COMPLETED else DownloadState.FAILED

                    onFinished(path)
                }

            } catch (e: CancellationException) {
                Log.d("DOWNLOAD_DEBUG", "Cancelled $songId")
                throw e

            } finally {
                jobs.remove(songId)
            }
        }

        jobs[songId] = job
    }
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

            currentList.removeAll { it.id == song.id }

            currentList.add(0, song)

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

    suspend fun clear(context: Context) {
        context.musicDataStore.edit { prefs ->
            prefs.remove(RECENTLY_PLAYED_KEY)
        }
    }
}

object ProfilePrefs {
    private const val DATASTORE_NAME = "profile"

    val Context.dataStore by preferencesDataStore(
        name = DATASTORE_NAME
    )

    private val PROFILE_URL =
        stringPreferencesKey("profile_url")

    private val USER_NAME =
        stringPreferencesKey("user_name")

    suspend fun saveProfileUrl(
        context: Context,
        url: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[PROFILE_URL] = url
        }
    }

    suspend fun saveUserName(
        context: Context,
        name: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME] = name
        }
    }

    fun getProfileUrl(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[PROFILE_URL]
        }
    }

    fun getUserName(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_NAME]
        }
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

@Composable
fun HomeScreen (
    showSheet: Boolean,
    onSongLongPress: (List<SongItem>, SongItem, Int) -> Unit
) {
    val isPreview = LocalInspectionMode.current

    val auth = remember(isPreview) {
        if (!isPreview) FirebaseAuth.getInstance() else null
    }
    val userID = auth?.currentUser?.uid

    val repository = MusicHistoryRepository()

    repository.getUserHistory { history ->
        Log.d(
            "HISTORY",
            history.toString()
        )
    }

    val scrollState = rememberScrollState()
    var isRefreshing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pullRefreshState = rememberPullToRefreshState()

    val viewModel: ProfileViewModel = viewModel()

    val imageUrl by viewModel.profileImageUrl.collectAsStateWithLifecycle()

    LaunchedEffect(userID) {
        userID?.let { viewModel.refreshUserData(it) }
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

            playlistsVM.fetchPlayListByQuery("Top","results")
            newReleasesVM.fetchPlaylistsByID("6689255","songs")
            trendingVM.fetchPlaylistsByID("946682072","songs")
            albumsVM.fetchAlbumByQuery("latest","results")
            artistsVM.fetchArtistsByQuery("top artists","results")

            userID?.let { viewModel.refreshUserData(it) }

            delay(1000)

            isRefreshing = false
        }
    }

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { refreshAll() },
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullRefreshState,
                isRefreshing = isLoading,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                color = colorResource(R.color.theme_color),
                containerColor = colorResource(R.color.primary_text_color)
            )
        }
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()
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
                modifier = Modifier.constrainAs(logoIcon) {
                    top.linkTo(profileAvatar.top)
                    bottom.linkTo(profileAvatar.bottom)
                    start.linkTo(parent.start, margin = 10.dp)
                }.size(width = 140.dp, height = 40.dp).zIndex(20f)
            ) {
                Icon(painter = painterResource(R.drawable.wavex_logo_dark), contentDescription = "Logo Icon",
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
                            val paint = Paint().apply {
                                color = shadowColor.copy(alpha = shadowAlpha)
                                asFrameworkPaint().apply {
                                    isAntiAlias = true

                                    maskFilter = if (shadowBlur > 0f) {
                                        android.graphics.BlurMaskFilter(
                                            safeBlur,
                                            android.graphics.BlurMaskFilter.Blur.NORMAL
                                        )
                                    } else {
                                        null
                                    }
                                }
                            }

                            canvas.drawCircle(
                                center,
                                glowRadius,
                                paint
                            )
                        }
                    }.zIndex(20f)
                , contentAlignment = Alignment.Center
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

            Column(modifier = Modifier.constrainAs(mainContent) {
                top.linkTo(parent.top, margin = 5.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            }.verticalScroll(scrollState).zIndex(0f)
            ) {
                ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                    val (topPlaylistsSection,recentlyPlayedTitle,recentlyPlayedSection,newReleasesTitle,newReleasesSection,popularArtistsTitle,
                        popularArtistsSection,trendingSongsTitle,trendingSongsSection,topAlbumsTitle,topAlbumsSection) = createRefs()

                    Playlist("Top","results",modifier = Modifier.constrainAs(topPlaylistsSection) {
                        top.linkTo(parent.top, margin = 90.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }, playlistsVM)

                    if (recentSongs.isNotEmpty() && playlists.isNotEmpty()) {
                        Text("Recently Played", modifier = Modifier.constrainAs(recentlyPlayedTitle) {
                            top.linkTo(topPlaylistsSection.bottom, margin = if (playlists.isNotEmpty()) 25.dp else 80.dp)
                            start.linkTo(parent.start, margin = 25.dp)
                        }, fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                        )

                        RecentlyPlayedSongs(
                            recentSongs, modifier = Modifier.constrainAs(recentlyPlayedSection) {
                                top.linkTo(recentlyPlayedTitle.bottom, margin = 15.dp)
                                start.linkTo(parent.start)
                            }, onSongLongPress = onSongLongPress
                        )
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
                    }, newReleasesVM, onSongLongPress = onSongLongPress)

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
                    }, trendingVM, onSongLongPress = onSongLongPress)

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
    val itemWidth = 160.dp
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
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    }
            ) {
                AsyncImage(
                    model = realItem.image.getOrNull(2)?.url,
                    contentDescription = realItem.name,
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.default_image),
                    modifier = Modifier
                        .height(itemWidth)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    modifier = Modifier.padding(horizontal = 8.dp ),
                    text = htmlToText(realItem.name),
                    fontSize = 12.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentlyPlayedSongs(
    recentSongs: List<SongItem>,
    modifier: Modifier,
    onSongLongPress: (List<SongItem>, SongItem, Int) -> Unit
) {
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
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    val songIndex = recentSongs.indexOfFirst { it.id == song.id }
                                    if (songIndex == -1) return@combinedClickable

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
                                onLongClick = {
                                    val songIndex = recentSongs.indexOfFirst { it.id == song.id }
                                    if (songIndex != -1) {
                                        onSongLongPress(recentSongs, song, songIndex)
                                    }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.image.getOrNull(2)?.url,
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
    viewModel: NewReleasesSongsViewModel = viewModel(),
    onSongLongPress: (List<SongItem>, SongItem, Int) -> Unit
) {
    val songs by viewModel.songs
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(playlistId) {
        viewModel.fetchPlaylistsByID(playlistId, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (songs.isEmpty()) return

    LazyRow(modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
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

                            scope.launch {
                                RecentlyPlayedManager.add(context, song)
                            }
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
    playlistId: String, root: String,
    modifier: Modifier,
    viewModel: TrendingSongsViewModel = viewModel(),
    onSongLongPress: (List<SongItem>, SongItem, Int) -> Unit
) {
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

                            scope.launch {
                                RecentlyPlayedManager.add(context, song)
                            }
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
fun TopAlbums(query: String, root: String, modifier: Modifier, viewModel: AlbumsViewModel = viewModel()) {
    val albums = viewModel.albums.value
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    LaunchedEffect(query) {
        viewModel.fetchAlbumByQuery(query, root)
    }

    val interactionSource = remember { MutableInteractionSource() }

    if (albums.isEmpty()) return

    LazyRow(modifier = modifier,
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = if (currentSong != null) 170.dp else 100.dp),
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

suspend fun downloadSong(
    url: String,
    fileName: String,
    context: Context
): String? = withContext(Dispatchers.IO) {

    val client = HttpClientProvider.client
    Log.d("DOWNLOAD_DEBUG", "Download started for $fileName")

    try {
        val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")

        val initialRequest = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Accept", "*/*")
            .addHeader("Referer", "https://www.jiosaavn.com/")
            .build()

        val initialResponse = client.newCall(initialRequest).execute()

        if (!initialResponse.isSuccessful) {
            Log.e("DOWNLOAD_DEBUG", "Initial request failed: ${initialResponse.code}")
            return@withContext null
        }

        val contentType = initialResponse.header("Content-Type") ?: ""

        val extension = when {
            contentType.contains("mp4") -> ".mp4"
            contentType.contains("mpeg") -> ".mp3"
            else -> ".mp3"
        }

        val file = File(context.filesDir, "$safeFileName$extension")

        val downloadedBytes = if (file.exists()) file.length() else 0
        Log.d("DOWNLOAD_DEBUG", "Resuming from byte: $downloadedBytes")

        initialResponse.close()

        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Accept", "*/*")
            .addHeader("Referer", "https://www.jiosaavn.com/")

        if (downloadedBytes > 0) {
            requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
            Log.d("DOWNLOAD_DEBUG", "Range header: bytes=$downloadedBytes-")
        }

        val request = requestBuilder.build()
        val call = client.newCall(request)

        coroutineContext.job.invokeOnCompletion {
            call.cancel()
        }

        val response = call.execute()

        Log.d("DOWNLOAD_DEBUG", "Response code: ${response.code}")
        Log.d("DOWNLOAD_DEBUG", "Accept-Ranges: ${response.header("Accept-Ranges")}")

        if (!response.isSuccessful) return@withContext null

        if (downloadedBytes > 0 && response.code != 206) {
            Log.w("DOWNLOAD_DEBUG", "Server doesn't support resume. Restarting...")
            file.delete()
            return@withContext downloadSong(url, fileName, context)
        }

        val body = response.body ?: return@withContext null

        body.byteStream().use { input ->
            FileOutputStream(file, true).buffered().use { output ->

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes = downloadedBytes

                while (input.read(buffer).also { bytesRead = it } != -1) {

                    output.write(buffer, 0, bytesRead)

                    output.flush()

                    ensureActive()

                    totalBytes += bytesRead
                    //Log.d("DOWNLOAD_DEBUG", "Downloaded: $totalBytes bytes")
                }
            }
        }

        Log.d("DOWNLOAD_DEBUG", "Saved: ${file.absolutePath}")
        file.absolutePath

    } catch (e: Exception) {

        if (e is CancellationException) {
            Log.d("DOWNLOAD_DEBUG", "Download cancelled")
            throw e
        }

        Log.e("DOWNLOAD_DEBUG", "Error", e)
        null
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
@Preview(showSystemUi = true)
private fun HomeScreenPreview() {

}