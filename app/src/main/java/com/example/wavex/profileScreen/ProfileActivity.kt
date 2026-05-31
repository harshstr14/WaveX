package com.example.wavex.profileScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wavex.R
import com.example.wavex.fonts
import com.example.wavex.homeScreen.viewModel.ProfileViewModel
import com.example.wavex.profileScreen.albumsScreen.AlbumsActivity
import com.example.wavex.profileScreen.artistsScreen.ArtistsActivity
import com.example.wavex.profileScreen.downloadedSongScreen.DownloadedSongActivity
import com.example.wavex.profileScreen.favouriteSongsScreen.FavouriteSongsActivity
import com.example.wavex.profileScreen.playlistsScreen.PlaylistsActivity
import com.example.wavex.profileScreen.settingScreen.SettingActivity
import com.example.wavex.profileScreen.yourProfileScreen.YourProfileActivity
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {
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
                Profile_Activity()
            }
        }
    }
}

@Composable
private fun Profile_Activity(
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val imageUrl by profileViewModel.profileImageUrl.collectAsStateWithLifecycle()
    val name by profileViewModel.userName.collectAsState()

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        uid?.let { profileViewModel.refreshUserData(it) }
    }

    ProfileScreen(
        imageUrl = imageUrl,
        name = name
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(imageUrl: String?, name: String) {
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val (backInteraction, backScale) = pressScale()

    var startAnimation by remember { mutableStateOf(false) }

    val shadowAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.8f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowAlpha"
    )

    val shadowScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowScale"
    )

    val shadowBlur by animateFloatAsState(
        targetValue = if (startAnimation) 60f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowBlur"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

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
                        text = "Profile",
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
            SnackbarHost(hostState = snackBarHostState) { data ->
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
                .background(colorResource(R.color.background_color))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            PortraitMode(
                context = context, name =  name, imageUrl =  imageUrl,
                shadowAlpha = shadowAlpha, shadowScale = shadowScale,
                shadowBlur = shadowBlur
            )
        }
    }
}

@Composable
private fun PortraitMode(
    context: Context, name: String, imageUrl: String?,
    shadowAlpha: Float, shadowScale: Float, shadowBlur: Float
) {
    val (userInteraction, userScale) = pressScale()
    val (songInteraction, songScale) = pressScale()
    val (artistsInteraction, artistsScale) = pressScale()
    val (albumsInteraction, albumsScale) = pressScale()
    val (playlistsInteraction, playlistsScale) = pressScale()
    val (downloadedInteraction, downloadedScale) = pressScale()
    val (settingInteraction, settingScale) = pressScale()
    val (editInteraction, editScale) = pressScale()

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background_color))
            .verticalScroll(rememberScrollState())
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val (profileImageRef, yourProfileRowRef, userNameTextRef, profileEditIconRef, downloadedSongsRowRwf
            ) = createRefs()

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build(),
                contentDescription = "Profile Image",
                onSuccess = { result ->
                    val drawable = result.result.drawable
                    val bitmap =
                        (drawable as? BitmapDrawable)?.bitmap ?: return@AsyncImage

                    Palette.from(bitmap).generate { palette ->
                        palette?.dominantSwatch?.rgb?.let { colorInt ->
                            shadowColor = Color(colorInt)
                        }
                    }
                },
                contentScale = ContentScale.Crop,
                modifier = Modifier.constrainAs(profileImageRef) {
                    top.linkTo(parent.top, margin = 15.dp)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }.padding(top = 10.dp).size(162.dp)
                    .drawBehind {
                        val glowRadius = (size.minDimension / 2) * shadowScale
                        val safeBlur = shadowBlur.coerceAtLeast(0.1f)

                        drawIntoCanvas { canvas ->
                            val frameworkPaint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = shadowColor.copy(alpha = shadowAlpha).toArgb()

                                maskFilter = android.graphics.BlurMaskFilter(
                                    safeBlur,
                                    android.graphics.BlurMaskFilter.Blur.NORMAL
                                )
                            }

                            canvas.nativeCanvas.drawCircle(
                                center.x,
                                center.y,
                                glowRadius,
                                frameworkPaint
                            )
                        }
                    }
                    .clip(CircleShape)
            )

            Box(
                modifier = Modifier.constrainAs(profileEditIconRef) {
                    bottom.linkTo(profileImageRef.bottom)
                    end.linkTo(profileImageRef.end, margin = 8.dp)
                }.size(36.dp).clip(RoundedCornerShape(20.dp))
                    .background(colorResource(R.color.theme_color))
                    .border(
                        width = 1.5.dp,
                        color = colorResource(R.color.background_color),
                        shape = RoundedCornerShape(20.dp)
                    ).clickable(
                        interactionSource = editInteraction,
                        indication = null
                    ) {
                        val intent =
                            Intent(context, YourProfileActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        context.startActivity(intent)
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.edit_icon),
                    contentDescription = "Edit Icon",
                    tint = colorResource(R.color.background_color),
                    modifier = Modifier.size(18.dp)
                        .graphicsLayer {
                            scaleX = editScale
                            scaleY = editScale
                        }
                )
            }

            Text(
                text = name,
                modifier = Modifier.constrainAs(userNameTextRef) {
                    top.linkTo(profileImageRef.bottom, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                fontSize = 20.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color),
                lineHeight = 22.sp
            )

            Column(
                modifier = Modifier
                    .constrainAs(yourProfileRowRef) {
                        top.linkTo(userNameTextRef.bottom, margin = 30.dp)
                        height = Dimension.fillToConstraints
                    }
                    .padding(start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFfefefe))
            ) {
                ProfileItem(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    icon = R.drawable.user_icon,
                    title = "Your Profile",
                    scale = userScale,
                    interactionSource = userInteraction,
                    onClick = {
                        val intent =
                            Intent(context, YourProfileActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                ProfileItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    icon = R.drawable.song_icon,
                    title = "Favourite Songs",
                    scale = songScale,
                    interactionSource = songInteraction,
                    onClick = {
                        val intent =
                            Intent(context, FavouriteSongsActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                ProfileItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    icon = R.drawable.mic_icon,
                    title = "Artists",
                    scale = artistsScale,
                    interactionSource = artistsInteraction,
                    onClick = {
                        val intent =
                            Intent(context, ArtistsActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                ProfileItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    icon = R.drawable.album_icon,
                    title = "Albums",
                    scale = albumsScale,
                    interactionSource = albumsInteraction,
                    onClick = {
                        val intent = Intent(context, AlbumsActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                ProfileItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    icon = R.drawable.playlist_icon,
                    title = "Playlists",
                    scale = playlistsScale,
                    interactionSource = playlistsInteraction,
                    onClick = {
                        val intent =
                            Intent(context, PlaylistsActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                ProfileItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    icon = R.drawable.downloaded_icon,
                    title = "Downloaded Songs",
                    scale = downloadedScale,
                    interactionSource = downloadedInteraction,
                    onClick = {
                        val intent =
                            Intent(context, DownloadedSongActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = colorResource(R.color.background_color)
                )

                ProfileItem(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    icon = R.drawable.setting_icon,
                    title = "Settings",
                    scale = settingScale,
                    interactionSource = settingInteraction,
                    onClick = {
                        val intent =
                            Intent(context, SettingActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileItem(
    modifier: Modifier = Modifier,
    icon: Int,
    title: String,
    scale: Float,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = title,
            tint = colorResource(R.color.theme_color),
            modifier = Modifier.size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontFamily = fonts,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.right_arrow_icon),
            contentDescription = "Right Arrow Icon",
            tint = colorResource(R.color.theme_color),
            modifier = Modifier.size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
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
private fun Profile_ActivityPreview() {
    WaveXTheme {
        ProfileScreen(
            imageUrl = "",
            name = ""
        )
    }
}