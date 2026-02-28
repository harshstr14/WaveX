package com.example.wavex.playerScreen

import android.app.Activity
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeRenderEffect
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
import coil.compose.AsyncImage
import com.example.wavex.R
import com.example.wavex.fonts
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.service.ServiceLocator
import com.example.wavex.ui.theme.WaveXTheme
import kotlin.random.Random

class PlayerActivityScreen : ComponentActivity() {
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
                Player_Activity_Screen()
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.fade_in,
            R.anim.slide_down
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Player_Activity_Screen() {
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val (backInteraction, backScale) = pressScale()
    val (playInteraction, playScale) = pressScale()
    val (nextInteraction, nextScale) = pressScale()
    val (prevInteraction, prevScale) = pressScale()
    val (repeatInteraction, repeatScale) = pressScale()
    val (shuffleInteraction, shuffleScale) = pressScale()
    val (threeDotsInteraction, threeDotsScale) = pressScale()

    val context = LocalContext.current
    val activity = context as? Activity

    var showSongSheet by remember { mutableStateOf(false) }

    val animatedBlur by animateFloatAsState(
        targetValue = if (showSongSheet) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    val musicService = ServiceLocator.musicService

    val isPlaying by musicService?.isPlaying?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val progress by musicService?.progress?.collectAsState(initial = 0)
        ?: remember { mutableStateOf(0) }

    val duration by musicService?.duration?.collectAsState(initial = 0)
        ?: remember { mutableStateOf(0) }

    val isShuffle by musicService?.isShuffle?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val isRepeat by musicService?.repeatMode?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val amplitudes = remember {
        List(100) { Random.nextFloat() }
    }

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color))
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
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
                                    color = colorResource(R.color.secondary_text_color).copy(
                                        alpha = 0.6f
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ).clickable(
                                    interactionSource = threeDotsInteraction,
                                    indication = null
                                ) {

                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.three_dots_icon),
                                contentDescription = "Three Dots Icon",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier.padding(end = 2.dp).size(18.dp)
                                    .graphicsLayer {
                                        scaleX = threeDotsScale
                                        scaleY = threeDotsScale
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
                snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 25.dp)
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
                        Icon(
                            painter = painterResource(
                                when {
                                    data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.background_color))
                .padding(paddingValues)
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
                },
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val(songImage, songName, albumName, progressBar, startTime, endTime, row) = createRefs()

                AsyncImage(
                    model = currentSong?.image?.getOrNull(2)?.url,
                    contentDescription = currentSong?.name,
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.default_image),
                    modifier = Modifier
                        .constrainAs(songImage) {
                            top.linkTo(parent.top, margin = 24.dp)
                            start.linkTo(parent.start, margin = 24.dp)
                            end.linkTo(parent.end, margin = 24.dp)
                        }
                        .size(310.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                Text(
                    modifier = Modifier.constrainAs(songName) {
                        top.linkTo(songImage.bottom, margin = 24.dp)
                        start.linkTo(songImage.start, margin = 14.dp)
                        end.linkTo(songImage.end, margin = 14.dp)
                        width = Dimension.fillToConstraints
                    },
                    text = htmlToText(currentSong?.name),
                    fontSize = 18.sp, lineHeight = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 2, textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    modifier = Modifier.constrainAs(albumName) {
                        top.linkTo(songName.bottom, margin = 8.dp)
                        start.linkTo(songImage.start, margin = 14.dp)
                        end.linkTo(songImage.end, margin = 14.dp)
                        width = Dimension.fillToConstraints
                    },
                    text = "Album • ${htmlToText(currentSong?.album?.name)}",
                    fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color), maxLines = 1, textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )

                Column(modifier = Modifier.constrainAs(progressBar) {
                    top.linkTo(albumName.bottom, margin = 24.dp)
                    start.linkTo(parent.start, margin = 24.dp)
                    end.linkTo(parent.end, margin = 24.dp)
                }) {
                    AudioWaveform(
                        amplitudes = amplitudes,
                        progress = if (duration > 0)
                            progress.toFloat() / duration.toFloat()
                        else 0f,
                    )
                }

                Text(
                    modifier = Modifier.constrainAs(startTime) {
                        top.linkTo(progressBar.bottom, margin = 12.dp)
                        start.linkTo(parent.start, margin = 14.dp)
                    }.padding(start = 14.dp),
                    text = formatTime(progress),
                    fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 1
                )

                Text(
                    modifier = Modifier.constrainAs(endTime) {
                        top.linkTo(progressBar.bottom, margin = 12.dp)
                        end.linkTo(parent.end, margin = 14.dp)
                    }.padding(end = 14.dp),
                    text = formatTime(duration),
                    fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), maxLines = 1
                )

                Row(
                    modifier = Modifier.constrainAs(row) {
                        top.linkTo(endTime.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.notificationshufflebutton),
                        contentDescription = "Shuffle Icon",
                        tint = if (isShuffle) colorResource(R.color.theme_color) else colorResource(R.color.primary_text_color),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(
                                interactionSource = shuffleInteraction,
                                indication = null
                            ) {
                                musicService?.shuffleToggle()
                            }
                            .graphicsLayer {
                                scaleX = shuffleScale
                                scaleY = shuffleScale
                            }
                    )

                    Spacer(modifier = Modifier.width(34.dp))

                    Icon(
                        painter = painterResource(R.drawable.notificationprevbutton),
                        contentDescription = "Previous Icon",
                        tint = colorResource(R.color.primary_text_color),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(
                                interactionSource = prevInteraction,
                                indication = null
                            ) {
                                musicService?.previous()
                            }
                            .graphicsLayer {
                                scaleX = prevScale
                                scaleY = prevScale
                            }
                    )

                    Spacer(modifier = Modifier.width(34.dp))

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(52.dp)
                            .clip(RoundedCornerShape(80.dp))
                            .background(colorResource(R.color.theme_color))
                            .clickable(
                                interactionSource = playInteraction,
                                indication = null
                            ) {
                                musicService?.togglePlayPause()
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
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = playScale
                                    scaleY = playScale
                                }
                        )
                    }

                    Spacer(modifier = Modifier.width(34.dp))

                    Icon(
                        painter = painterResource(R.drawable.notificationnextbutton),
                        contentDescription = "Next Icon",
                        tint = colorResource(R.color.primary_text_color),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(
                                interactionSource = nextInteraction,
                                indication = null
                            ) {
                                musicService?.next()
                            }
                            .graphicsLayer {
                                scaleX = nextScale
                                scaleY = nextScale
                            }
                    )

                    Spacer(modifier = Modifier.width(34.dp))

                    Icon(
                        painter = painterResource(R.drawable.notificationrepeatbutton),
                        contentDescription = "Repeat Icon",
                        tint = if (isRepeat) colorResource(R.color.theme_color) else colorResource(R.color.primary_text_color),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(
                                interactionSource = repeatInteraction,
                                indication = null
                            ) {
                                musicService?.repeatToggle()
                            }
                            .graphicsLayer {
                                scaleX = repeatScale
                                scaleY = repeatScale
                            }
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d : %02d".format(minutes, seconds)
}

@Composable
fun AudioWaveform(
    amplitudes: List<Float>,
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = colorResource(R.color.theme_color),
    inactiveColor: Color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val widthPerBar = size.width / amplitudes.size
        val centerY = size.height / 2

        amplitudes.forEachIndexed { index, amplitude ->

            val barHeight = amplitude * size.height
            val x = index * widthPerBar

            val barProgress = index.toFloat() / amplitudes.size

            drawLine(
                color = if (barProgress <= progress) activeColor else inactiveColor,
                start = Offset(x, centerY - barHeight / 2),
                end = Offset(x, centerY + barHeight / 2),
                strokeWidth = widthPerBar * 0.7f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun pressScale(
    pressedScale: Float = 1.15f
): Pair<MutableInteractionSource, Float> {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PressScale"
    )

    return interactionSource to scale
}

@Preview(showSystemUi = true)
@Composable
private fun PlayerActivityScreenPreview() {
    WaveXTheme {

    }
}