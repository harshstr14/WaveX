package com.example.wavex.feature.library.presentation

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.core.model.PlaylistData
import com.example.wavex.core.service.ServiceLocator
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.importplaylist.model.ImportState
import com.example.wavex.feature.library.model.LibraryUiState
import com.example.wavex.feature.library.presentation.favourite.presentation.FavouriteSongActivity
import com.example.wavex.feature.library.presentation.playlist.presentation.PlaylistActivity
import com.example.wavex.feature.profile.presentation.settings.presentation.IOSStyleBottomDialog
import kotlinx.coroutines.launch

enum class SheetType {
    CREATE_PLAYLIST,
    ADD_SPOTIFY_PLAYLIST,
    ADD_WAVEX_PLAYLIST,
    RENAME_PLAYLIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onClickBack: () -> Unit,
    snackBarHostState: SnackbarHostState,
    showSheet: Boolean,
    playlists: LibraryUiState,
    onDeletePlaylist: (String, onResult: (Boolean) -> Unit) -> Unit,
    onOpenSheet: (SheetType) -> Unit,
    showBottomSheet: Boolean,
    onPlaylistSelect: (PlaylistData) -> Unit,
    importState: ImportState,
    onCancelImport: () -> Unit,
    likedSongs: Set<String>
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<PlaylistData?>(null) }

    val playlistState = rememberLazyListState()
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService
    val currentSong by musicService?.currentSong?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val scope = rememberCoroutineScope()

    val (backInteraction, backScale) = pressScale()
    val (addInteraction, addScale) = pressScale()
    val (spotifyInteraction, spotifyScale) = pressScale()
    val (cancelInteraction, cancelScale) = pressScale()
    val interactionSource = remember { MutableInteractionSource() }

    val shouldBlur = importState is ImportState.Loading || showBottomSheet || showSheet

    val animatedBlur by animateFloatAsState(
        targetValue = if (shouldBlur) 22f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BlurAnim"
    )

    val isLoading = importState is ImportState.Loading
    var menuExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isLoading) {}

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ConstraintLayout(modifier = Modifier
            .fillMaxSize()
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
            val(backButton, titleText, addButton, importLogo, playlistList) = createRefs()

            Text(
                text = "Library",
                modifier = Modifier
                    .constrainAs(titleText) {
                        top.linkTo(parent.top, margin = 22.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 22.sp
            )

            Box(
                modifier = Modifier
                    .constrainAs(backButton) {
                        top.linkTo(titleText.top)
                        bottom.linkTo(titleText.bottom)
                        start.linkTo(parent.start, margin = 25.dp)
                    }
                    .size(36.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.5.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = backInteraction,
                        indication = null
                    ) {
                       onClickBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_icon),
                    contentDescription = "Back Icon",
                    tint = colorResource(R.color.primary_text_color),
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = backScale
                            scaleY = backScale
                        }
                )
            }

            Box(
                modifier = Modifier
                    .constrainAs(addButton) {
                        top.linkTo(titleText.top)
                        bottom.linkTo(titleText.bottom)
                        end.linkTo(parent.end, margin = 25.dp)
                    }
                    .size(36.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.5.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = addInteraction,
                        indication = null
                    ) {
                        onOpenSheet(
                            SheetType.CREATE_PLAYLIST
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus_icon),
                    contentDescription = "Add Icon",
                    tint = colorResource(R.color.primary_text_color),
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = addScale
                            scaleY = addScale
                        }
                )
            }

            Box(
                modifier = Modifier
                    .constrainAs(importLogo) {
                        top.linkTo(titleText.top)
                        bottom.linkTo(titleText.bottom)
                        end.linkTo(addButton.start, margin = 15.dp)
                    }
                    .size(36.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.5.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = spotifyInteraction,
                        indication = null
                    ) {
                        menuExpanded = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.file_import_icon),
                    contentDescription = "Import Icon",
                    tint = colorResource(R.color.primary_text_color),
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = spotifyScale
                            scaleY = spotifyScale
                        }
                )

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = Color(0xFF3a3a3a),
                    shape = RoundedCornerShape(14.dp),
                    offset = DpOffset(x = 0.dp, y = 8.dp),
                    modifier = Modifier.width(200.dp),
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.height(38.dp),
                        text = {
                            Text(
                                text = "Import from Spotify",
                                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.background_color),
                                modifier = Modifier.offset(x = (-6).dp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.spotify_logo),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenSheet(SheetType.ADD_SPOTIFY_PLAYLIST)
                        }
                    )

                    DropdownMenuItem(
                        modifier = Modifier.height(38.dp),
                        text = {
                            Text(
                                text = "Import from WaveX",
                                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.background_color),
                                modifier = Modifier.offset(x = (-6).dp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.logo2),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenSheet(SheetType.ADD_WAVEX_PLAYLIST)
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .constrainAs(playlistList) {
                        top.linkTo(titleText.bottom, margin = 25.dp)
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                        height = Dimension.fillToConstraints
                    }
                    .background(colorResource(R.color.background_color)),
            ) {
                when {
                    playlists.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingEffect()
                        }
                    }

                    playlists.isError -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorState(
                                message = playlists.errorMessage
                            )
                        }
                    }

                    playlists.playlists.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorState(
                                message = "No Playlist Created"
                            )
                        }
                    }

                    else -> {
                        LazyColumn (
                            state = playlistState,
                            contentPadding = PaddingValues(start = 22.dp, end = 12.dp, bottom = if (currentSong != null) 168.dp else 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row (
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {
                                            val intent = Intent(context, FavouriteSongActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            }
                                            context.startActivity(intent)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.liked),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Liked Songs",
                                            fontSize = 15.sp, lineHeight = 18.sp, fontFamily = fonts,
                                            fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color), maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "${likedSongs.size} Songs",
                                            fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.secondary_text_color), maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            items(
                                playlists.playlists
                            ) { playlist ->
                                var menuExpanded by remember { mutableStateOf(false) }

                                Row (
                                    modifier = Modifier.clickable(
                                        interactionSource = spotifyInteraction,
                                        indication = null
                                    ) {
                                        val intent = Intent(context, PlaylistActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            putExtra("playlist_Id",playlist.playlistId)
                                        }
                                        context.startActivity(intent)
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val songImages = playlist.songs
                                        .mapNotNull { song ->
                                            song.image.getOrNull(2)?.url
                                        }
                                        .filter { it.isNotBlank() }
                                        .take(4)

                                    val shouldShowGrid =
                                        playlist.imageUrl.isBlank() ||
                                                playlist.imageUrl.contains("default_image")

                                    if (shouldShowGrid && songImages.size >= 4) {
                                        PlaylistImageGrid(
                                            images = songImages,
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    } else {
                                        AsyncImage(
                                            model = playlist.imageUrl,
                                            contentDescription = null,
                                            error = painterResource(R.drawable.default_image),
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = playlist.playlistName,
                                            fontSize = 15.sp, lineHeight = 18.sp, fontFamily = fonts,
                                            fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color), maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "${playlist.totalSongs} Songs",
                                            fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.secondary_text_color), maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Box {
                                        IconButton(
                                            onClick = { menuExpanded = true }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.three_dots_icon),
                                                contentDescription = "Three Dots",
                                                tint = colorResource(R.color.primary_text_color).copy(alpha = 0.6f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false },
                                            containerColor = Color(0xFF3a3a3a),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.width(150.dp),
                                        ) {
                                            DropdownMenuItem(
                                                modifier = Modifier.height(38.dp),
                                                text = {
                                                    Text(
                                                        text = "Rename",
                                                        fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
                                                        fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                                        color = colorResource(R.color.background_color),
                                                        modifier = Modifier.offset(x = (-6).dp)
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(R.drawable.edit_icon),
                                                        contentDescription = null,
                                                        tint = colorResource(R.color.theme_color),
                                                        modifier = Modifier
                                                            .padding(start = 2.dp)
                                                            .size(20.dp)
                                                    )
                                                },
                                                onClick = {
                                                    menuExpanded = false
                                                    onPlaylistSelect(playlist)
                                                    onOpenSheet(SheetType.RENAME_PLAYLIST)
                                                }
                                            )

                                            DropdownMenuItem(
                                                modifier = Modifier.height(38.dp),
                                                text = {
                                                    Text(
                                                        text = "Remove",
                                                        fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts,
                                                        fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                                        color = colorResource(R.color.background_color),
                                                        modifier = Modifier.offset(x = (-6).dp)
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(R.drawable.delete_icon),
                                                        contentDescription = null,
                                                        tint = colorResource(R.color.theme_color),
                                                        modifier = Modifier
                                                            .padding(start = 2.dp)
                                                            .size(20.dp)
                                                    )
                                                },
                                                onClick = {
                                                    menuExpanded = false
                                                    playlistToDelete = playlist
                                                    showDeleteDialog = true
                                                }
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

        val progress = remember { Animatable(0f) }

        LaunchedEffect(isLoading) {
            if (isLoading) {
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 30_000,
                        easing = LinearEasing
                    )
                )
            } else {
                progress.stop()
                progress.snapTo(0f)
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colorResource(R.color.background_color)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Importing Playlist",
                            fontSize = 16.sp, lineHeight = 16.sp,
                            fontFamily = fonts, fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color), maxLines = 1,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.happy_spaceman)
                        )

                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(124.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { progress.value },
                                color = colorResource(R.color.theme_color),
                                trackColor = colorResource(R.color.secondary_text_color).copy(alpha = 0.3f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Cancel",
                                fontSize = 14.sp, lineHeight = 15.sp,
                                fontFamily = fonts, fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.theme_color), maxLines = 1,
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = cancelInteraction,
                                        indication = null
                                    ) {
                                        scope.launch {
                                            progress.stop()
                                            onCancelImport()
                                        }
                                    }
                                    .graphicsLayer(
                                        scaleX = cancelScale,
                                        scaleY = cancelScale
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && playlistToDelete != null) {
        IOSStyleBottomDialog(
            title = "Delete Playlist",
            message = "Are you sure you want to delete \"${playlistToDelete!!.playlistName}\" ?",
            confirmText = "Delete",
            icon = R.drawable.delete_icon,
            onConfirm = {
                onDeletePlaylist(playlistToDelete?.playlistId ?: "") { success ->
                    if (success) {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                "Playlist deleted",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                "Failed to delete playlist",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }

                    showDeleteDialog = false
                    playlistToDelete = null
                }
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun PlaylistImageGrid(images: List<String>, modifier: Modifier = Modifier) {
    if (images.size < 4) {
        AsyncImage(
            model = images.firstOrNull(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f)) {
            images.take(2).forEach { image ->
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            images.drop(2).take(2).forEach { image ->
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun pressScale(
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
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
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
private fun LibraryScreenPreview() {
    LibraryScreen(
        onClickBack = {},
        snackBarHostState = SnackbarHostState(),
        showSheet = false,
        playlists = LibraryUiState(),
        onDeletePlaylist = { _,_ -> },
        onOpenSheet = {},
        showBottomSheet = false,
        onPlaylistSelect = {},
        importState = ImportState.Idle,
        onCancelImport = {},
        likedSongs = setOf()
    )
}