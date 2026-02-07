package com.example.wavex

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wavex.discoverScreen.DiscoverScreen
import com.example.wavex.homeScreen.HomeScreen
import com.example.wavex.libraryScreen.LibraryScreen
import com.example.wavex.navigation.BottomItem
import com.example.wavex.navigation.BottomNavRoute
import com.example.wavex.searchScreen.SearchScreen
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
    
    Scaffold(
        containerColor = colorResource(id = R.color.background_color),
        bottomBar = {
            BottomNavBar(navController)
        },
        contentWindowInsets = WindowInsets(0)
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
                HomeScreen(navController)  // ⬅ current Home UI
            }
            composable(BottomNavRoute.Discover.route) {
                DiscoverScreen(navController)
            }
            composable(BottomNavRoute.Search.route) {
                SearchScreen(navController)
            }
            composable(BottomNavRoute.Library.route) {
                LibraryScreen(navController)
            }
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
            .navigationBarsPadding().height(68.dp).shadow( elevation = 18.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0xFF313131).copy(alpha = 0.2f),
                spotColor = Color(0xFF313131).copy(alpha = 0.4f)
            ).background(Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF313131).copy(alpha = 0.95f),
                    Color(0xFF313131).copy(alpha = 1f)
                )
            ), shape = RoundedCornerShape(18.dp)),
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
                    targetValue = if (selected) 14.dp else 20.dp,
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
                                lineHeight = 17.sp
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