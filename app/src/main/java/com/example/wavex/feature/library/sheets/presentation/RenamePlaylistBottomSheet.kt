package com.example.wavex.feature.library.sheets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.wavex.R
import com.example.wavex.core.model.PlaylistData
import com.example.wavex.feature.auth.presentation.signup.fonts

@Composable
fun RenamePlaylistBottomSheet(
    playlist: PlaylistData?,
    onClose: () -> Unit,
    onRename: (String, String, String) -> Unit
) {
    var titleName by rememberSaveable { mutableStateOf(playlist?.playlistName ?: "") }
    var description by rememberSaveable { mutableStateOf(playlist?.description ?: "") }
    var titleError by rememberSaveable { mutableStateOf(false) }
    var titleFocused by rememberSaveable { mutableStateOf(false) }
    var descriptionFocused by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Rename Your Playlist", modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Title", modifier = Modifier.padding(start = 28.dp),
            fontSize = 14.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 25.dp)
                .height(52.dp)
                .fillMaxWidth()
                .border(
                    width = 1.1.dp,
                    color = when {
                        titleError -> Color.Red
                        titleFocused -> colorResource(R.color.theme_color)
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = Color(0xFFfefefe),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (inputField, placeholderText) = createRefs()

                if (titleName.isEmpty()) {
                    Text(
                        modifier = Modifier
                            .constrainAs(placeholderText) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                                end.linkTo(parent.end, margin = 15.dp)
                                width = Dimension.fillToConstraints
                            },
                        text = "Enter Title",
                        fontFamily = fonts,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = 15.sp, lineHeight = 18.sp,
                        color = colorResource(R.color.secondary_text_color)
                    )
                }

                val selectionColors = TextSelectionColors(
                    handleColor = colorResource(R.color.primary_text_color).copy(alpha = 0.88f),
                    backgroundColor = colorResource(R.color.primary_text_color).copy(alpha = 0.3f)
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
                            }
                            .onFocusChanged {
                                titleFocused = it.isFocused
                            },
                        textStyle = TextStyle(
                            fontFamily = fonts,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            fontSize = 15.sp, lineHeight = 18.sp,
                            color = colorResource(R.color.secondary_text_color)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(colorResource(R.color.primary_text_color).copy(alpha = 0.88f))
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

        Text(
            text = "Description", modifier = Modifier.padding(start = 28.dp),
            fontSize = 14.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 25.dp)
                .height(52.dp)
                .fillMaxWidth()
                .border(
                    width = 1.1.dp,
                    color = when {
                        descriptionFocused -> colorResource(R.color.theme_color)
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = Color(0xFFfefefe),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (inputField, placeholderText) = createRefs()

                if (description.isEmpty()) {
                    Text(
                        modifier = Modifier
                            .constrainAs(placeholderText) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                                end.linkTo(parent.end, margin = 15.dp)
                                width = Dimension.fillToConstraints
                            },
                        text = "Enter Short Description",
                        fontFamily = fonts,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = 15.sp, lineHeight = 18.sp,
                        color = colorResource(R.color.secondary_text_color)
                    )
                }

                val selectionColors = TextSelectionColors(
                    handleColor = colorResource(R.color.primary_text_color).copy(alpha = 0.88f),
                    backgroundColor = colorResource(R.color.primary_text_color).copy(alpha = 0.3f)
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
                            }
                            .onFocusChanged {
                                descriptionFocused = it.isFocused
                            },
                        textStyle = TextStyle(
                            fontFamily = fonts,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            fontSize = 15.sp, lineHeight = 18.sp,
                            color = colorResource(R.color.secondary_text_color)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(colorResource(R.color.primary_text_color).copy(alpha = 0.88f))
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 25.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
                    .clickable { onClose() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 25.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colorResource(R.color.theme_color))
                    .clickable {
                        titleError = titleName.isBlank()

                        if (!titleError) {
                            val newName = titleName.trim()
                            val newDescription = description.trim()

                            titleError = newName.isBlank()
                            if (titleError) return@clickable

                            val playlistId = playlist?.playlistId ?: return@clickable

                            onRename(playlistId, newName, newDescription)
                            onClose()
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Rename",
                    fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.background_color)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RenamePlaylistBottomSheetPreview() {
    RenamePlaylistBottomSheet(
        playlist = PlaylistData(),
        onClose = {},
        onRename = { _,_,_ -> }
    )
}