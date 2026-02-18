package com.example.wavex.artistScreen.allAlbumsScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.albumScreen.AlbumActivity
import com.example.wavex.fonts
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.ui.theme.WaveXTheme

class AllAlbumsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        val artistId = intent.getStringExtra("artist_id")

        setContent {
            WaveXTheme {
                All_Albums_Screen(artistId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun All_Albums_Screen(artistId: String?, viewModel: AllAlbumsViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val albumsGridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(artistId) {
        artistId?.let {
            viewModel.fetchAlbumsByArtistID(it, "albums")
        }
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShareScale"
    )

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)),
        topBar = {
            CenterAlignedTopAppBar(
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
                                interactionSource = interactionSource,
                                indication = null
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
                                    scaleX = scale
                                    scaleY = scale
                                }
                        )
                    }
                },
                title = {
                    Text(
                        text = "All Albums",
                        fontSize = 20.sp,
                        fontFamily = fonts,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color),
                        lineHeight = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.background_color),
                    scrolledContainerColor = colorResource(R.color.background_color)
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 15.dp).shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color(0xFF1C1C1C),
                            spotColor = Color(0xFF1C1C1C)
                        ),
                    containerColor = Color(0xFF1C1C1C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(when {
                            data.visuals.message.contains("like") -> R.drawable.heart_outline
                            data.visuals.message.contains("email") -> R.drawable.email_icon
                            data.visuals.message.contains("Welcome") -> R.drawable.logo2
                            data.visuals.message.contains("password") -> R.drawable.password_icon
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
                            color = colorResource(R.color.background_color)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.background_color)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    LoadingEffect()
                }

                albums.isError -> {
                    ErrorState(
                        message = albums.errorMessage,
                        onRetry = {
                            artistId?.let { viewModel.fetchAlbumsByArtistID(it, "albums") }
                        }
                    )
                }

                else -> {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (albumsGrid) = createRefs()

                        LazyVerticalGrid(
                            state = albumsGridState,
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.constrainAs(albumsGrid){
                                top.linkTo(parent.top, margin = 5.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                height = Dimension.fillToConstraints
                            },
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 25.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            items(
                                items = albums.albums,
                                key = { it.id }
                            ) { album ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                                        },
                                ) {
                                    AsyncImage(
                                        model = album.image.getOrNull(2)?.url,
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

                            item(
                                span = { GridItemSpan(maxLineSpan) }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {
                                            viewModel.loadNextPage()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "See More",
                                        fontSize = 16.sp,
                                        lineHeight = 18.sp,
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.SemiBold,
                                        fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.theme_color),
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
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 45.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes (R.raw.spaceman)
        )

        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(144.dp)
        )

        Spacer(modifier = Modifier.height(0.dp))

        Text(
            modifier = Modifier.offset(y = (-8).dp),
            text = message,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color), maxLines = 2
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier.offset(y = (-8).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(R.color.theme_color))
                .clickable { onRetry() }
                .padding(horizontal = 24.dp, vertical = 10.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Retry",
                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
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

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.fillMaxWidth().padding(bottom = 45.dp).size(144.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun AllAlbumsScreenPreview() {
    WaveXTheme {
        All_Albums_Screen("")
    }
}