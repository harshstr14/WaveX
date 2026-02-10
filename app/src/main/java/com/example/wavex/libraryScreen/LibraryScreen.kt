package com.example.wavex.libraryScreen

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wavex.R
import com.example.wavex.fonts
import androidx.compose.ui.graphics.asComposeRenderEffect
import kotlinx.coroutines.launch

enum class SheetType {
    CREATE_PLAYLIST,
    ADD_PLAYLIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {
    val showSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val currentSheet = remember { mutableStateOf<SheetType?>(null) }

    val scope = rememberCoroutineScope()

    val (backInteraction, backScale) = pressScale()
    val (addInteraction, addScale) = pressScale()
    val (spotifyInteraction, spotifyScale) = pressScale(1.12f)

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
                        }
                    )
                }

                SheetType.ADD_PLAYLIST -> {
                    AddPlaylistBottomSheet(
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

    val blur by animateFloatAsState(
        targetValue = if (showSheet.value) 20f else 0f,
        label = "BlurAnim"
    )

    ConstraintLayout(modifier = Modifier.fillMaxSize()
        .graphicsLayer {
            if (showSheet.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blur > 0f) {
                renderEffect = RenderEffect
                    .createBlurEffect(
                        blur,
                        blur,
                        Shader.TileMode.CLAMP
                    )
                    .asComposeRenderEffect()
            }
        }
    ) {
        val(backButton,titleText,addButton,spotifyLogo,likedSongsRow) = createRefs()

        Text("Library", modifier = Modifier.constrainAs(titleText) {
            top.linkTo(parent.top, margin = 22.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
            fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color), lineHeight = 22.sp
        )

        Box(modifier = Modifier.constrainAs(backButton) {
            top.linkTo(titleText.top)
            bottom.linkTo(titleText.bottom)
            start.linkTo(parent.start, margin = 25.dp)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .size(36.dp).clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            ).clickable(
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
                modifier = Modifier.size(20.dp)
                    .graphicsLayer {
                        scaleX = backScale
                        scaleY = backScale
                    }
            )
        }

        Box(modifier = Modifier.constrainAs(addButton) {
            top.linkTo(titleText.top)
            bottom.linkTo(titleText.bottom)
            end.linkTo(parent.end, margin = 25.dp)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .size(36.dp).clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            ).clickable(
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
                modifier = Modifier.size(22.dp)
                    .graphicsLayer {
                        scaleX = addScale
                        scaleY = addScale
                    }
            )
        }

        Icon(
            painter = painterResource(R.drawable.spotify_logo),
            contentDescription = "add Icon",
            tint = Color.Unspecified,
            modifier = Modifier.constrainAs(spotifyLogo){
                top.linkTo(titleText.top)
                bottom.linkTo(titleText.bottom)
                end.linkTo(addButton.start, margin = 15.dp)
            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .size(42.dp).graphicsLayer {
                    scaleX = spotifyScale
                    scaleY = spotifyScale
                }.clickable(
                    interactionSource = spotifyInteraction,
                    indication = null
                ) {
                    currentSheet.value = SheetType.ADD_PLAYLIST
                    showSheet.value = true
                    scope.launch { sheetState.show() }
                }
        )

        Row (
            modifier = Modifier.constrainAs(likedSongsRow){
                top.linkTo(titleText.bottom, margin = 35.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.liked),
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
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
    }
}

@Composable
fun AddPlaylistBottomSheet(onClose: () -> Unit) {
    var url by remember { mutableStateOf("") }

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

        Box(modifier = Modifier.padding(horizontal = 25.dp).height(52.dp)
            .fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (inputField, placeholderText) = createRefs()

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
                        onValueChange = { url = it },
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
                modifier = Modifier.width(148.dp).padding(top = 25.dp)
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
                modifier = Modifier.width(148.dp).padding(top = 25.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorResource(R.color.theme_color))
                    .clickable { }
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
fun CreatePlaylistBottomSheet(onClose: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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

        Box(modifier = Modifier.padding(horizontal = 25.dp).height(52.dp)
            .fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (inputField, placeholderText) = createRefs()

                if (name.isEmpty()) {
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
                        value = name,
                        onValueChange = { name = it },
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Description", modifier = Modifier.padding(start = 28.dp),
            fontSize = 13.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.padding(horizontal = 25.dp).height(52.dp)
            .fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)),
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
                modifier = Modifier.width(148.dp).padding(top = 25.dp)
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
                modifier = Modifier.width(148.dp).padding(top = 25.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorResource(R.color.theme_color))
                    .clickable { }
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

@Preview(showSystemUi = true)
@Composable
private fun LibraryScreenPreview() {
    val navController = rememberNavController()
    LibraryScreen(navController)
}