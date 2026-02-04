package com.example.wavex.artistScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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

class ArtistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = 0xFF121212.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFF121212.toInt()
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
                            ambientColor = Color(0xFF1C1C1C).copy(alpha = 0.2f),
                            spotColor = Color(0xFF1C1C1C).copy(alpha = 0.4f)
                        ),
                    containerColor = Color(0xFF1C1C1C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(when {
                            data.visuals.message.contains("Email") -> R.drawable.email_icon
                            data.visuals.message.contains("email") -> R.drawable.email_icon
                            data.visuals.message.contains("Welcome") -> R.drawable.logo2
                            data.visuals.message.contains("password") -> R.drawable.password_icon
                            else -> {
                                R.drawable.alert_icon
                            }
                        } ), contentDescription = "Icons",
                            tint = Color(0xFF34A853), modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = data.visuals.message,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 13.sp,
                            color = Color(0xFFF6F6F6)
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
                .background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                LoadingEffect()
            } else {
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                    val (backIcon, shareIcon, likeIcon, image, text1, text2
                            ,text3) = createRefs()

                    Box(
                        modifier = Modifier.constrainAs(backIcon) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start, margin = 25.dp)
                        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                            .size(36.dp).clip(RoundedCornerShape(20.dp))
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF797979),
                                shape = RoundedCornerShape(20.dp)
                            ).clickable {

                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_icon),
                            contentDescription = "add Icon",
                            tint = Color(0xFFF6F6F6),
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
                                color = Color(0xFF797979),
                                shape = RoundedCornerShape(20.dp)
                            ).clickable {

                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.share_icon),
                            contentDescription = "add Icon",
                            tint = Color(0xFFF6F6F6),
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
                                color = Color(0xFF797979),
                                shape = RoundedCornerShape(20.dp)
                            ).clickable {

                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.heart_outline),
                            contentDescription = "add Icon",
                            tint = Color(0xFFF6F6F6),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AsyncImage(
                        model = artists.imageUrl,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.constrainAs(image) {
                            top.linkTo(backIcon.bottom)
                            start.linkTo(parent.start, margin = 25.dp)
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
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = Color(0xFFF6F6F6),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (artists.isVerified) {
                            Icon(
                                painter = painterResource(R.drawable.verified_icon),
                                contentDescription = "add Icon",
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
                            contentDescription = "add Icon",
                            tint = Color(0xFFF6F6F6),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Followers : ${artists.followerCount}",
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = Color(0xFF797979),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text("Top Songs", modifier = Modifier.constrainAs(text3) {
                        top.linkTo(image.bottom, margin = 25.dp)
                        start.linkTo(parent.start, margin = 25.dp)
                    }, fontSize = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = Color(0xFFF6F6F6), lineHeight = 18.sp
                    )
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