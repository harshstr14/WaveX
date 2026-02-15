package com.example.wavex.profileScreen

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wavex.R
import com.example.wavex.fonts
import com.example.wavex.homeScreen.viewModel.ProfileViewModel
import com.example.wavex.profileScreen.albumsScreen.AlbumsActivity
import com.example.wavex.profileScreen.artistsScreen.ArtistsActivity
import com.example.wavex.profileScreen.favouriteSongsScreen.FavouriteSongsActivity
import com.example.wavex.profileScreen.playlistsScreen.PlaylistsActivity
import com.example.wavex.profileScreen.settingScreen.SettingActivity
import com.example.wavex.profileScreen.yourProfileScreen.YourProfileActivity
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth

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
private fun Profile_Activity() {
    val viewModel: ProfileViewModel = viewModel()

    val imageUrl by viewModel.profileImageUrl.collectAsStateWithLifecycle()
    val name by viewModel.userName.collectAsState()

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        uid?.let { viewModel.silentRefresh(it) }
    }

    ProfileScreen(
        imageUrl = imageUrl,
        name = name
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(imageUrl: String?, name: String) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (profileAvatar, userIcon, text1, row2, row3, row4, row5, row6, editIcon) = createRefs()

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .memoryCacheKey("profile_image")
                        .diskCacheKey("profile_image")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.constrainAs(profileAvatar) {
                        top.linkTo(parent.top, margin = 15.dp)
                        end.linkTo(parent.end)
                        start.linkTo(parent.start)
                    }.size(162.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.logo)
                )

                Box(
                    modifier = Modifier.constrainAs(editIcon) {
                        bottom.linkTo(profileAvatar.bottom)
                        end.linkTo(profileAvatar.end, margin = 8.dp)
                    }.size(36.dp).clip(RoundedCornerShape(20.dp))
                        .background(colorResource(R.color.theme_color))
                        .border(
                            width = 1.5.dp,
                            color = colorResource(R.color.background_color),
                            shape = RoundedCornerShape(20.dp)
                        ).clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, YourProfileActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
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
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }

                Text(
                    text = name,
                    modifier = Modifier.constrainAs(text1) {
                        top.linkTo(profileAvatar.bottom, margin = 20.dp)
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

                Row(
                    modifier = Modifier.constrainAs(userIcon){
                        top.linkTo(text1.bottom, margin = 30.dp)
                    }.fillMaxWidth().padding(horizontal = 25.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, YourProfileActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.user_icon),
                        contentDescription = "User Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(20.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )

                    Spacer(modifier = Modifier.width(15.dp))

                    Text(
                        text = "Your Profile",
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
                        contentDescription = "User Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }

                Row(
                    modifier = Modifier.constrainAs(row2){
                        top.linkTo(userIcon.bottom, margin = 30.dp)
                    }.fillMaxWidth().padding(horizontal = 25.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, FavouriteSongsActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.song_icon),
                        contentDescription = "Song Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Favourite Songs",
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
                        contentDescription = "Arrow Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }

                Row(
                    modifier = Modifier.constrainAs(row3){
                        top.linkTo(row2.bottom, margin = 30.dp)
                    }.fillMaxWidth().padding(horizontal = 25.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, ArtistsActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.mic_icon),
                        contentDescription = "Mic Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Artists",
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
                        contentDescription = "Arrow Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }

                Row(
                    modifier = Modifier.constrainAs(row4){
                        top.linkTo(row3.bottom, margin = 30.dp)
                    }.fillMaxWidth().padding(horizontal = 25.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, AlbumsActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.album_icon),
                        contentDescription = "Album Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Albums",
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
                        contentDescription = "Arrow Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }

                Row(
                    modifier = Modifier.constrainAs(row6){
                        top.linkTo(row4.bottom, margin = 30.dp)
                    }.fillMaxWidth().padding(horizontal = 25.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, PlaylistsActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.playlist_icon),
                        contentDescription = "Playlist Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Playlists",
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
                        contentDescription = "Arrow Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }

                Row(
                    modifier = Modifier.constrainAs(row5){
                        top.linkTo(row6.bottom, margin = 30.dp)
                    }.fillMaxWidth().padding(horizontal = 25.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val intent = Intent(context, SettingActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.setting_icon),
                        contentDescription = "Setting Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Settings",
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
                        contentDescription = "Arrow Icon",
                        tint = colorResource(R.color.theme_color),
                        modifier = Modifier.size(22.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }
            }
        }
    }
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