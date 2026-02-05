package com.example.wavex.artistScreen

import android.app.Activity
import android.os.Bundle
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.example.wavex.fonts
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch
import java.util.Locale

class ArtistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        val artistId = intent.getStringExtra("artist_id")

        setContent {
            WaveXTheme {
                Artist_Activity(artistId)
            }
        }
    }
}

@Composable
fun Artist_Activity(artistId: String?,viewModel: ArtistViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(artistId) {
        artistId?.let {
            viewModel.loadArtist(it)
        }
    }

    Scaffold(
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

                artists.isError -> {
                    ErrorState(
                        message = artists.errorMessage,
                        onRetry = {
                            artistId?.let { viewModel.loadArtist(it) }
                        }
                    )
                }

                else -> {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (backIcon, shareIcon, likeIcon, image, text1, text2, text4, songlist) = createRefs()

                        Box(
                            modifier = Modifier.constrainAs(backIcon) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start, margin = 25.dp)
                            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                                .size(36.dp).clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = colorResource(R.color.secondary_text_color),
                                    shape = RoundedCornerShape(20.dp)
                                ).clickable {
                                    activity?.finish()
                                }, contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_icon),
                                contentDescription = "Back Icon",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier.constrainAs(shareIcon) {
                                top.linkTo(parent.top)
                                end.linkTo(likeIcon.start, margin = 15.dp)
                            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                                .size(36.dp).clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = colorResource(R.color.secondary_text_color),
                                    shape = RoundedCornerShape(20.dp)
                                ).clickable {

                                }, contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.share_icon),
                                contentDescription = "Share Icon",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier.constrainAs(likeIcon) {
                                top.linkTo(parent.top)
                                end.linkTo(parent.end, margin = 25.dp)
                            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                                .size(36.dp).clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = colorResource(R.color.secondary_text_color),
                                    shape = RoundedCornerShape(20.dp)
                                ).clickable {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Click on like",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }, contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.heart_outline),
                                contentDescription = "Like Icon",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        AsyncImage(
                            model = artists.imageUrl,
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.constrainAs(image) {
                                top.linkTo(backIcon.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                                .size(112.dp).clip(CircleShape).zIndex(2f),
                            placeholder = painterResource(R.drawable.logo),
                            error = painterResource(R.drawable.logo)
                        )

                        Row(
                            modifier = Modifier.constrainAs(text1){
                                top.linkTo(image.top, margin = 15.dp)
                                start.linkTo(image.end)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 15.dp, end = 15.dp)) {
                            Text(
                                text = artists.name,
                                fontSize = 18.sp,
                                lineHeight = 18.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.primary_text_color),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            if (artists.isVerified) {
                                Icon(
                                    painter = painterResource(R.drawable.verified_icon),
                                    contentDescription = "Verified Icon",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.constrainAs(text2){
                                top.linkTo(text1.bottom, margin = 15.dp)
                                start.linkTo(image.end)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            }.padding(start = 15.dp, end = 15.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.followers_icon),
                                contentDescription = "Followers Icon",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Followers : ${
                                    formatCount(artists.followerCount.toLong())
                                }",
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.secondary_text_color),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.constrainAs(text4){
                                top.linkTo(text2.bottom, margin = 5.dp)
                                start.linkTo(image.end)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            }.padding(start = 15.dp, end = 15.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.headset_icon),
                                contentDescription = "Followers Icon",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Listeners : ${
                                    formatCount(artists.fanCount.toLongOrNull() ?: 0L)
                                }",
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.secondary_text_color),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        LazyColumn (
                            modifier = Modifier.constrainAs(songlist){
                                top.linkTo(image.bottom, margin = 20.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                height = Dimension.fillToConstraints
                            }) {

                            item {
                                Text("Top Songs", modifier = Modifier.padding(start = 24.dp, top = 5.dp, bottom = 10.dp)
                                    , fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                )
                            }

                            items(
                                items = artists.topSongs,
                                key = { it.id }
                            ) { song ->
                                Row (
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(start = 24.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {

                                        }, verticalAlignment = Alignment.CenterVertically
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
                                            color = colorResource(R.color.primary_text_color), maxLines = 1,
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
                                            color = colorResource(R.color.secondary_text_color), maxLines = 1,
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

                            item {
                                Text("Top Albums", modifier = Modifier.padding(start = 24.dp, top = 15.dp, bottom = 8.dp)
                                    , fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                )
                            }

                            item {
                                LazyRow(modifier = Modifier.fillMaxWidth()
                                    .padding(top = 10.dp),
                                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                    items(artists.topAlbums) { album ->
                                        Column(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = null
                                                ) {  }
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

                            item {
                                Text("Singles", modifier = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 8.dp)
                                    , fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                )
                            }

                            item {
                                LazyRow(modifier = Modifier.fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 25.dp),
                                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                    items(artists.singles) { album ->
                                        Column(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = null
                                                ) {  }
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            painter = painterResource(R.drawable.alert_icon),
            contentDescription = null,
            tint = colorResource(R.color.secondary_text_color),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            fontFamily = fonts,
            fontSize = 14.sp,
            color = colorResource(R.color.secondary_text_color)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(R.color.theme_color))
                .clickable { onRetry() }
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Retry",
                fontFamily = fonts,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000_000 -> String.format(Locale.US,"%.1fB", count / 1_000_000_000.0)
        count >= 1_000_000 -> String.format(Locale.US,"%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US,"%.1fK", count / 1_000.0)
        else -> count.toString()
    }.replace(".0", "")
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

@Preview(showSystemUi = true)
@Composable
fun Artist_ActivityPreview() {
    WaveXTheme {
        Artist_Activity("")
    }
}