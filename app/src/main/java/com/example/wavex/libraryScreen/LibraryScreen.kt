package com.example.wavex.libraryScreen

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.BuildConfig
import com.example.wavex.R
import com.example.wavex.fonts
import com.example.wavex.libraryScreen.importPlaylist.ImportPlaylistViewModel
import com.example.wavex.libraryScreen.importPlaylist.ImportState
import com.example.wavex.libraryScreen.playlistScreen.PlaylistActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement

enum class SheetType {
    CREATE_PLAYLIST,
    ADD_PLAYLIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController, snackbarHostState: SnackbarHostState,
    viewModel: PlaylistViewModel = viewModel()
) {
    val apiUrl = BuildConfig.SPOTIFY_API_BASE_URL
    val importViewModel: ImportPlaylistViewModel = viewModel()
    val importState by importViewModel.importState.collectAsState()

    LaunchedEffect(importState) {
        when (importState) {
            is ImportState.Success -> {
                snackbarHostState.showSnackbar(
                    "Playlist imported successfully",
                    duration = SnackbarDuration.Short
                )
            }
            is ImportState.Error -> {
                snackbarHostState.showSnackbar(
                    (importState as ImportState.Error).message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> Unit
        }
    }

    val showSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val currentSheet = remember { mutableStateOf<SheetType?>(null) }
    val playlistState = rememberLazyListState()
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val (backInteraction, backScale) = pressScale()
    val (addInteraction, addScale) = pressScale()
    val (spotifyInteraction, spotifyScale) = pressScale(1.12f)

    val playlistsList by viewModel.playlists.collectAsStateWithLifecycle()

    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    showSheet.value = false
                }
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = colorResource(R.color.background_color),
            dragHandle = null
        ) {
            when (currentSheet.value) {
                SheetType.CREATE_PLAYLIST -> {
                    CreatePlaylistBottomSheet(
                        onClose = {
                            scope.launch {
                                sheetState.hide()
                                showSheet.value = false
                            }
                        }, onShowMessage = { message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = message,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }

                SheetType.ADD_PLAYLIST -> {
                    AddPlaylistBottomSheet(
                        apiUrl,
                        importViewModel,
                        onClose = {
                            scope.launch {
                                sheetState.hide()
                                showSheet.value = false
                            }
                        }
                    )
                }

                null -> {}
            }
        }
    }

    val shouldBlur = importState is ImportState.Loading || showSheet.value

    val animatedBlur by animateFloatAsState(
        targetValue = if (shouldBlur) 25f else 0f,
        label = ""
    )

    val isLoading = importState is ImportState.Loading

    BackHandler(enabled = isLoading) {}

    Box(modifier = Modifier
        .fillMaxSize()
        )
    {
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
            val(backButton,titleText,addButton,spotifyLogo,likedSongsRow,playlistList) = createRefs()

            Text("Library", modifier = Modifier
                .constrainAs(titleText) {
                    top.linkTo(parent.top, margin = 22.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
                fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 22.sp
            )

            Box(modifier = Modifier
                .constrainAs(backButton) {
                    top.linkTo(titleText.top)
                    bottom.linkTo(titleText.bottom)
                    start.linkTo(parent.start, margin = 25.dp)
                }
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
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
                    navController.popBackStack()
                }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_icon),
                    contentDescription = "add Icon",
                    tint = colorResource(R.color.primary_text_color),
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = backScale
                            scaleY = backScale
                        }
                )
            }

            Box(modifier = Modifier
                .constrainAs(addButton) {
                    top.linkTo(titleText.top)
                    bottom.linkTo(titleText.bottom)
                    end.linkTo(parent.end, margin = 25.dp)
                }
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
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
                    currentSheet.value = SheetType.CREATE_PLAYLIST
                    showSheet.value = true
                    scope.launch { sheetState.show() }
                }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus_icon),
                    contentDescription = "add Icon",
                    tint = colorResource(R.color.primary_text_color),
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = addScale
                            scaleY = addScale
                        }
                )
            }

            Icon(
                painter = painterResource(R.drawable.spotify_logo),
                contentDescription = "Spotify Icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .constrainAs(spotifyLogo) {
                        top.linkTo(titleText.top)
                        bottom.linkTo(titleText.bottom)
                        end.linkTo(addButton.start, margin = 15.dp)
                    }
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .size(42.dp)
                    .graphicsLayer {
                        scaleX = spotifyScale
                        scaleY = spotifyScale
                    }
                    .clickable(
                        interactionSource = spotifyInteraction,
                        indication = null
                    ) {
                        currentSheet.value = SheetType.ADD_PLAYLIST
                        showSheet.value = true
                        scope.launch { sheetState.show() }
                    }
            )

            Row (
                modifier = Modifier
                    .constrainAs(likedSongsRow) {
                        top.linkTo(titleText.bottom, margin = 35.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(start = 24.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.liked),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Liked Songs",
                        fontSize = 15.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color), maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "total songs",
                        fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
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
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(R.drawable.three_dots_icon),
                            contentDescription = "Three Dots",
                            tint = colorResource(R.color.primary_text_color).copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .constrainAs(playlistList) {
                        top.linkTo(likedSongsRow.bottom, margin = 12.dp)
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                        height = Dimension.fillToConstraints
                    }
                    .background(colorResource(R.color.background_color)),
            ) {
                when {
                    playlistsList.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingEffect()
                        }
                    }

                    playlistsList.isError -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorState(
                                message = playlistsList.errorMessage
                            )
                        }
                    }

                    playlistsList.playlists.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
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
                            contentPadding = PaddingValues(start = 24.dp, end = 12.dp, bottom = 25.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            items(playlistsList.playlists) { playlist ->
                                Row (
                                    modifier = Modifier.clickable(
                                        interactionSource = spotifyInteraction,
                                        indication = null
                                    ) {
                                        val intent = Intent(context, PlaylistActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                            putExtra("playlist_name",playlist.playlistName)
                                        }
                                        context.startActivity(intent)
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = playlist.imageUrl,
                                        contentDescription = null,
                                        error = painterResource(R.drawable.default_image),
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = playlist.playlistName,
                                            fontSize = 15.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color), maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "${playlist.totalSongs} Songs",
                                            fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
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
                                                modifier = Modifier.size(20.dp),
                                                painter = painterResource(R.drawable.three_dots_icon),
                                                contentDescription = "Three Dots",
                                                tint = colorResource(R.color.primary_text_color).copy(alpha = 0.6f)
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

        if (isLoading) {
            val progress = remember { Animatable(0f) }
            val interactionSource = remember { MutableInteractionSource() }

            LaunchedEffect(isLoading) {
                if (isLoading) {
                    progress.snapTo(0f)
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 100_000,
                            easing = LinearEasing
                        )
                    )
                } else {
                    progress.stop()
                    progress.snapTo(0f)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) { awaitPointerEvent() }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp).padding(horizontal = 24.dp)
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
                                modifier = Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    scope.launch {
                                        progress.stop()
                                        importViewModel.cancelImport()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPlaylistBottomSheet(apiUrl: String,
                                   viewModel: ImportPlaylistViewModel,
                                   onClose: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val trimUrl = url.substringAfter("playlist/").substringBefore("?")

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .width(58.dp)
                .height(4.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Import Spotify Playlist", modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Playlist Url", modifier = Modifier.padding(start = 28.dp),
            fontSize = 13.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier
            .padding(horizontal = 25.dp)
            .height(52.dp)
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (urlError) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (inputField, placeholderText, copyIcon) = createRefs()

                if (url.isEmpty()) {
                    Text(modifier = Modifier.constrainAs(placeholderText) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 15.dp)
                        end.linkTo(parent.end, margin = 15.dp)
                        width = Dimension.fillToConstraints },
                        text = "Enter Url",
                        fontFamily = fonts,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = 14.sp, lineHeight = 17.sp,
                        color = colorResource(R.color.secondary_text_color)
                    )
                }

                val selectionColors = TextSelectionColors(
                    handleColor = Color(0xFF1C1C1C),
                    backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
                )

                CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                    BasicTextField(
                        value = url,
                        onValueChange = {
                            url = it

                            if (it.isNotBlank()) {
                                urlError = false
                            }
                        },
                        modifier = Modifier
                            .constrainAs(inputField) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                                end.linkTo(copyIcon.start, margin = 15.dp)
                                width = Dimension.fillToConstraints
                            },
                        textStyle = TextStyle(
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp,
                            color = colorResource(R.color.secondary_text_color)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFF1C1C1C))
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.copy_icon),
                    contentDescription = null,
                    tint = colorResource(R.color.theme_color),
                    modifier = Modifier
                        .constrainAs(copyIcon) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end, margin = 15.dp)
                        }
                        .size(22.dp)
                        .clickable {
                            scope.launch {
                                val clipEntry = clipboard.getClipEntry()
                                val clipboardText = clipEntry?.clipData
                                    ?.getItemAt(0)
                                    ?.text
                                    ?.toString()

                                if (!clipboardText.isNullOrBlank()) {
                                    url = clipboardText
                                    urlError = false
                                }
                            }
                        }
                )
            }
        }

        if (urlError) {
            Text(
                text = "Url cannot be empty",
                color = Color.Red,
                fontSize = 12.sp,
                lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
                modifier = Modifier.padding(start = 28.dp, top = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(148.dp)
                    .padding(top = 25.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
                    .clickable { onClose() }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.theme_color)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Box(
                modifier = Modifier
                    .width(148.dp)
                    .padding(top = 25.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorResource(R.color.theme_color))
                    .clickable {
                        urlError = url.isBlank()

                        if (!urlError) {
                            viewModel.importPlaylistByUrl(apiUrl, trimUrl)
                            onClose()
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Import",
                    fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.background_color)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun CreatePlaylistBottomSheet(onClose: () -> Unit, onShowMessage: (String) -> Unit) {
    var titleName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }

    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("MyPlaylists")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .width(58.dp)
                .height(4.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Name Your Playlist", modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Title", modifier = Modifier.padding(start = 28.dp),
            fontSize = 13.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier
            .padding(horizontal = 25.dp)
            .height(52.dp)
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (titleError) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (inputField, placeholderText) = createRefs()

                if (titleName.isEmpty()) {
                    Text(modifier = Modifier.constrainAs(placeholderText) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 15.dp)
                        end.linkTo(parent.end, margin = 15.dp)
                        width = Dimension.fillToConstraints },
                        text = "Enter Title",
                        fontFamily = fonts,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = 14.sp, lineHeight = 17.sp,
                        color = colorResource(R.color.secondary_text_color)
                    )
                }

                val selectionColors = TextSelectionColors(
                    handleColor = Color(0xFF1C1C1C),
                    backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
                )

                CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                    BasicTextField(
                        value = titleName,
                        onValueChange = {
                            titleName = it

                            if (it.isNotBlank()) {
                                titleError = false
                            }
                        },
                        modifier = Modifier
                            .constrainAs(inputField) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                                end.linkTo(parent.end, margin = 15.dp)
                                width = Dimension.fillToConstraints
                            },
                        textStyle = TextStyle(
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp,
                            color = colorResource(R.color.secondary_text_color)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFF1C1C1C))
                    )
                }
            }
        }

        if (titleError) {
            Text(
                text = "Title cannot be empty",
                color = Color.Red,
                fontSize = 12.sp,
                lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
                modifier = Modifier.padding(start = 28.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Description", modifier = Modifier.padding(start = 28.dp),
            fontSize = 13.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier
            .padding(horizontal = 25.dp)
            .height(52.dp)
            .fillMaxWidth()
            .background(
                colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (inputField, placeholderText) = createRefs()

                if (description.isEmpty()) {
                    Text(modifier = Modifier.constrainAs(placeholderText) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 15.dp)
                        end.linkTo(parent.end, margin = 15.dp)
                        width = Dimension.fillToConstraints },
                        text = "Enter Short Description",
                        fontFamily = fonts,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = 14.sp, lineHeight = 17.sp,
                        color = colorResource(R.color.secondary_text_color)
                    )
                }

                val selectionColors = TextSelectionColors(
                    handleColor = Color(0xFF1C1C1C),
                    backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
                )

                CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                    BasicTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .constrainAs(inputField) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                                end.linkTo(parent.end, margin = 15.dp)
                                width = Dimension.fillToConstraints
                            },
                        textStyle = TextStyle(
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp,
                            color = colorResource(R.color.secondary_text_color)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFF1C1C1C))
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(148.dp)
                    .padding(top = 25.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
                    .clickable { onClose() }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.theme_color)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Box(
                modifier = Modifier
                    .width(148.dp)
                    .padding(top = 25.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorResource(R.color.theme_color))
                    .clickable {
                        titleError = titleName.isBlank()

                        if (!titleError) {
                            favouriteReference.child(titleName.trim()).get()
                                .addOnSuccessListener { snapshot ->
                                    if (!snapshot.exists()) {
                                        val playlistData = mutableMapOf<String, Any>()

                                        titleName.trim().takeIf { it.isNotEmpty() }
                                            ?.let { playlistData["playlistName"] = it }
                                        description.trim().takeIf { it.isNotEmpty() }
                                            ?.let { playlistData["description"] = it }
                                        playlistData["imageUrl"] =
                                            "https://res.cloudinary.com/dcdg3s1pf/image/upload/v1771090319/default_image_hrsmd7.jpg"

                                        favouriteReference.child(titleName.trim())
                                            .setValue(playlistData).addOnSuccessListener {
                                                onShowMessage("PlayList Created Successfully")
                                                onClose()
                                            }.addOnFailureListener {
                                                onShowMessage("Failed to Create PlayList")
                                                onClose()
                                            }
                                    } else {
                                        onShowMessage("PlayList Already Exist")
                                        onClose()
                                    }
                                }
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create",
                    fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.background_color)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
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

@Composable
private fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier
            .fillMaxWidth()
            .size(144.dp)
    )
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

        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(134.dp)
        )

        Spacer(modifier = Modifier.height(0.dp))

        Text(
            modifier = Modifier.offset(y = (-8).dp),
            text = message,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color), maxLines = 2
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LibraryScreenPreview() {
    val navController = rememberNavController()
    val snackbarHostState = SnackbarHostState()
    LibraryScreen(navController,snackbarHostState)
}