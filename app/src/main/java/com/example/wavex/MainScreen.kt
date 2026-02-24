package com.example.wavex

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.wavex.discoverScreen.DiscoverScreen
import com.example.wavex.homeScreen.HomeScreen
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.viewModel.LikedSongsViewModel
import com.example.wavex.libraryScreen.LibraryScreen
import com.example.wavex.libraryScreen.pressScale
import com.example.wavex.navigation.BottomItem
import com.example.wavex.navigation.BottomNavRoute
import com.example.wavex.playlistScreen.SongOptionsBottomSheet
import com.example.wavex.searchScreen.SearchScreen
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.service.ServiceLocator
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

val okHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
private lateinit var apiUrl1: String
private lateinit var apiUrl2: String
private lateinit var apiUrl3: String

suspend fun requestWithFallback(endpoint: String): String =
    withContext(Dispatchers.IO) {

        val apis = listOf(apiUrl1, apiUrl2, apiUrl3)

        for (baseUrl in apis) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .get()
                    .build()

                okHttpClient.newCall(request).execute().use { response ->

                    if (response.isSuccessful) {
                        return@withContext response.body?.string().orEmpty()
                    }

                    if (response.code in 500..599) {
                        Log.w("API", "Server error ${response.code} on $baseUrl, trying next...")
                        continue
                    }

                    if (response.code in 400..499) {
                        throw Exception("Client error ${response.code}")
                    }
                }

            } catch (e: Exception) {
                if (
                    e is java.net.SocketTimeoutException ||
                    e is java.net.ConnectException ||
                    e is java.net.UnknownHostException
                ) {
                    Log.w("API", "Network error on $baseUrl, trying next...")
                    continue
                } else {
                    throw e
                }
            }
        }

        throw Exception("All APIs timed out")
    }

class MainScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        apiUrl1 = BuildConfig.API_BASE_URL1
        apiUrl2 = BuildConfig.API_BASE_URL2
        apiUrl3 = BuildConfig.API_BASE_URL3

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                Main_Screen()
            }
        }
    }
}

@Composable
fun Main_Screen() {
    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }

    var showSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val likedViewModel: LikedSongsViewModel = viewModel()
    val likedSongs by likedViewModel.likedSongs.collectAsState()

    val context = LocalContext.current

    val musicService = ServiceLocator.musicService

    val playlist by musicService?.playlistFlow?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val currentIndex by musicService?.currentIndexFlow?.collectAsState(initial = -1)
        ?: remember { mutableIntStateOf(-1) }

    Scaffold(
        containerColor = colorResource(id = R.color.background_color),
        bottomBar = {
            Column {

                val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
                    ?: remember { mutableStateOf(false) }

                val currentSong by musicService?.currentSong?.collectAsState(initial = null)
                    ?: remember { mutableStateOf(null) }

                currentSong?.let { song ->
                    MiniPlayer(
                        song = song,
                        isPlaying = isPlaying,
                        onPlayPause = {
                            musicService?.togglePlayPause()
                        },
                        onClick = { },
                        onAddClick = {
                            selectedSong = song
                            selectedIndex = currentIndex
                            showSheet = true
                        }
                    )
                }

                BottomNavBar(navController)
            }
        },
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 15.dp).shadow(
                            elevation = 12.dp,
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
                        Icon(
                            painter = painterResource(
                                when {
                                    data.visuals.message.contains("Playlist") -> R.drawable.playlist_icon
                                    data.visuals.message.contains("name") -> R.drawable.user_icon
                                    data.visuals.message.contains("Phone") -> R.drawable.phone_icon
                                    else -> {
                                        R.drawable.alert_icon
                                    }
                                }
                            ),
                            contentDescription = "Icons",
                            tint = colorResource(R.color.theme_color),
                            modifier = Modifier.size(24.dp)
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavRoute.Home.route,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),

            enterTransition = {
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(
                        durationMillis = 320,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(220)
                )
            },

            exitTransition = {
                scaleOut(
                    targetScale = 1.05f,
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = FastOutLinearInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(160)
                )
            },

            popEnterTransition = {
                scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(300)
                ) + fadeIn()
            },

            popExitTransition = {
                scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(220)
                ) + fadeOut()
            }
        ) {
            composable(BottomNavRoute.Home.route) {
                HomeScreen(navController, showSheet = showSheet)  // ⬅ current Home UI
            }
            composable(BottomNavRoute.Discover.route) {
                DiscoverScreen(navController, showSheet = showSheet)
            }
            composable(BottomNavRoute.Search.route) {
                SearchScreen(navController, showSheet = showSheet)
            }
            composable(BottomNavRoute.Library.route) {
                LibraryScreen(navController, snackBarHostState = snackBarHostState, showSheet = showSheet)
            }
        }
    }

    if (showSheet && selectedSong != null) {
        val song = selectedSong!!
        val isFavourite = likedSongs.contains(song.id)

        SongOptionsBottomSheet(
            song = song,
            onDismiss = {
                showSheet = false
                selectedSong = null
            },
            onPlayNow = {
                val intent = Intent(context, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_PLAY_NEW
                    putExtra("index", selectedIndex)
                }

                PlayerManager.currentPlaylist = playlist.toMutableList()
                PlayerManager.currentIndex = selectedIndex

                ContextCompat.startForegroundService(context, intent)
                showSheet = false
            },
            isFavourite = isFavourite,
            onToggleFavourite = {
                likedViewModel.toggleLike(song)
            }
        )
    }
}

@Composable
fun MiniPlayer(
    song: SongItem,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val playInteractionSource = remember { MutableInteractionSource() }
    val isPressed by playInteractionSource.collectIsPressedAsState()
    val (addInteraction, addScale) = pressScale()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShareScale"
    )

    val context = LocalContext.current
    var gradientColors by remember { mutableStateOf(listOf(Color(0xFF2C2C2C))) }

    LaunchedEffect(song.image) {
        gradientColors = extractColors(
            context,
            song.image.getOrNull(2)?.url
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .padding(start = 18.dp, end = 18.dp, bottom = 4.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF2C2C2C).copy(alpha = 0.80f)  )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = song.image.getOrNull(2)?.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            colorResource(R.color.primary_text_color).copy(alpha = 0.65f),  // left strong
                            colorResource(R.color.primary_text_color).copy(alpha = 0.45f),  // middle
                            colorResource(R.color.primary_text_color).copy(alpha = 0.80f)   // right darker
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(colorResource(R.color.primary_text_color).copy(alpha = 0.65f))
        )

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = song.image.getOrNull(2)?.url,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                val songName = htmlToText(song.name)

                Text(
                    text = songName,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.off_white),
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
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(36.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorResource(R.color.theme_color))
                    .clickable(
                        interactionSource = playInteractionSource,
                        indication = null
                    ) {
                        onPlayPause()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.notificationpausebutton
                        else R.drawable.notificationplaybutton
                    ),
                    contentDescription = "Play Icon",
                    tint = colorResource(R.color.background_color),
                    modifier = Modifier
                        .padding(start = if (isPlaying) 0.dp else 1.dp)
                        .size(18.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                painter = painterResource(R.drawable.plus_icon),
                contentDescription = "Add Icon",
                tint = colorResource(R.color.off_white),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 6.dp)
                    .size(28.dp)
                    .clickable(
                        interactionSource = addInteraction,
                        indication = null
                    ) {
                        onAddClick()
                    }
                    .graphicsLayer {
                        scaleX = addScale
                        scaleY = addScale
                    }
            )
        }
    }
}

suspend fun extractColors(context: Context, imageUrl: String?): List<Color> {
    return withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as SuccessResult).drawable
            val bitmap = (result as BitmapDrawable).bitmap

            val palette = Palette.from(bitmap).generate()

            val dominant = palette.getDominantColor(0xFF2C2C2C.toInt())
            val vibrant = palette.getVibrantColor(dominant)

            listOf(Color(dominant), Color(vibrant))
        } catch (e: Exception) {
            listOf(Color(0xFF2C2C2C), Color(0xFF1E1E1E))
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavController) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        BottomItem(
            BottomNavRoute.Home.route,
            "Home",
            R.drawable.home_filled,
            R.drawable.home_outline
        ),
        BottomItem(
            BottomNavRoute.Discover.route,
            "Discover",
            R.drawable.discover_filled,
            R.drawable.discover_outline
        ),
        BottomItem(
            BottomNavRoute.Search.route,
            "Search",
            R.drawable.search_filled,
            R.drawable.search_outline
        ),
        BottomItem(
            BottomNavRoute.Library.route,
            "Library",
            R.drawable.library_filled,
            R.drawable.library_outline
        )
    )

    Box(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, bottom = 8.dp)
            .navigationBarsPadding().height(68.dp).shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFF2C2C2C).copy(alpha = 0.2f),
                spotColor = Color(0xFF2C2C2C).copy(alpha = 0.4f)
            ).background(Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2C2C2C).copy(alpha = 0.95f),
                    Color(0xFF2C2C2C).copy(alpha = 1f)
                )
            ), shape = RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            items.forEach { item ->
                val selected = currentRoute == item.route
                val animatedPadding by animateDpAsState(
                    targetValue = if (selected) 14.dp else 22.dp,
                    label = "paddingAnim"
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            brush = if (selected)
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF34A853),
                                        Color(0x2F34A853)
                                    )
                                )
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(
                            horizontal = animatedPadding,
                            vertical = 9.dp
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (selected) item.filledIcon else item.outlineIcon
                        ),
                        contentDescription = item.label,
                        tint = if (selected) Color(0xFFF6F6F6) else Color(0xFF797979),
                        modifier = Modifier.size(24.dp)
                    )

                    Box(modifier = Modifier
                            .padding(start = 6.dp)
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 420,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            .clipToBounds()
                    ) {
                        if (selected) {
                            Text(
                                text = item.label,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = 1f
                                    scaleY = 1f
                                    alpha = 1f
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                },
                                color = Color(0xFFF6F6F6),
                                fontSize = 14.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Main_ScreenPreview() {
    WaveXTheme {
        Main_Screen()
    }
}