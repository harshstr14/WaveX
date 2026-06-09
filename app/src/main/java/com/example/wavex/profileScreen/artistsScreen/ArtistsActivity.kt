package com.example.wavex.profileScreen.artistsScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.fonts
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.pressScale
import com.example.wavex.profileScreen.settingScreen.IOSStyleBottomDialog
import com.example.wavex.searchScreen.SearchSource
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch

class ArtistsActivity : ComponentActivity() {
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

        setContent {
            WaveXTheme {
                Artists_Activity()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Artists_Activity(viewModel: FavouriteArtistViewModel = viewModel()) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity
    val artistsGridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val (backInteraction, backScale) = pressScale()
    val (deleteInteraction, deleteScale) = pressScale()

    val artistList by viewModel.artists.collectAsStateWithLifecycle()
    Log.d("artists", artistList.toString())

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)).
        nestedScroll(scrollBehavior.nestedScrollConnection),
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
                                interactionSource = backInteraction,
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
                                    scaleX = backScale
                                    scaleY = backScale
                                }
                        )
                    }
                },
                title = {
                    Text(
                        text = "Artists",
                        fontSize = 20.sp,
                        fontFamily = fonts,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color),
                        lineHeight = 22.sp
                    )
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(20.dp)
                                ).clickable(
                                    interactionSource = deleteInteraction,
                                    indication = null
                                ) {
                                    if (artistList.artists.isEmpty()) {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                message = "No favourite artists to remove",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        showDeleteDialog = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.delete_icon),
                                contentDescription = "Delete Icon",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier.size(18.dp)
                                    .graphicsLayer {
                                        scaleX = deleteScale
                                        scaleY = deleteScale
                                    }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.background_color),
                    scrolledContainerColor = colorResource(R.color.background_color)
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
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
                        Icon(painter = painterResource(when {
                            data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
                            data.visuals.message.contains("artists") -> R.drawable.mic_icon
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
                            color = colorResource(R.color.off_white)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.background_color))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                artistList.isLoading -> {
                    LoadingEffect()
                }

                artistList.isError -> {
                    ErrorState(
                        message = artistList.errorMessage
                    )
                }

                artistList.artists.isEmpty() -> {
                    ErrorState(
                        message = "No Favourite Artists"
                    )
                }

                else -> {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val (artistsGrid) = createRefs()

                        LazyVerticalGrid(
                            state = artistsGridState,
                            columns = GridCells.Adaptive(minSize = 110.dp),
                            modifier = Modifier
                                .constrainAs(artistsGrid){
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(parent.bottom)
                                    height = Dimension.fillToConstraints
                                },
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(artistList.artists) { artist ->
                                Column(
                                    modifier = Modifier.clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        val intent = Intent(context, ArtistActivity::class.java).apply {
                                            putExtra("artist_id", artist.artistId)
                                            putExtra("artist_imageUrl", artist.artistImageUrl)
                                            putExtra("artist_source",
                                                when(artist.source) {
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
                                    } ,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = artist.artistImageUrl.takeIf { it.isNotBlank() },
                                        contentDescription = artist.artistName,
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(R.drawable.default_artist),
                                        modifier = Modifier
                                            .size(82.dp)
                                            .clip(CircleShape)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val artistName = htmlToText(artist.artistName)

                                    Text(
                                        modifier = Modifier.width(78.dp),
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

            if (showDeleteDialog) {
                IOSStyleBottomDialog(
                    title = "Delete All Artists?",
                    message = "Do you want to delete all favourite artists.",
                    confirmText = "Delete",
                    icon = R.drawable.delete_icon,
                    onConfirm = {
                        viewModel.deleteAllArtists()
                        showDeleteDialog = false
                    },
                    onDismiss = {
                        showDeleteDialog = false
                    }
                )
            }
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
            .fillMaxWidth()
            .padding(bottom = 45.dp)
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
private fun ErrorState(message: String) {
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
            fontSize = 16.sp, lineHeight = 20.sp, fontFamily = fonts,
            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color), maxLines = 2
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ArtistsActivityPreview() {
    WaveXTheme {
        Artists_Activity()
    }
}