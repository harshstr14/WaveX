package com.example.wavex.feature.library.sheets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.wavex.BuildConfig
import com.example.wavex.R
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.importplaylist.presentation.ImportPlaylistViewModel
import kotlinx.coroutines.launch

@Composable
fun AddSpotifyPlaylistBottomSheet(
    onClose: () -> Unit,
    onImportPlaylist: (String, String) -> Unit
) {
    val spotifyApiUrl = BuildConfig.SPOTIFY_API_BASE_URL
    var url by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf(false) }
    var urlFocused by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val trimUrl = url.substringAfter("playlist/").substringBefore("?")
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Import Spotify Playlist", modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Playlist Url", modifier = Modifier.padding(start = 28.dp),
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
                        urlError -> Color.Red
                        urlFocused -> colorResource(R.color.theme_color)
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
                val (inputField, placeholderText, copyIcon) = createRefs()

                if (url.isEmpty()) {
                    Text(
                        modifier = Modifier
                            .constrainAs(placeholderText) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start, margin = 15.dp)
                                end.linkTo(parent.end, margin = 15.dp)
                                width = Dimension.fillToConstraints
                            },
                        text = "Enter Url",
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
                            }
                            .onFocusChanged {
                                urlFocused = it.isFocused
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
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
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
                    .background(Color(0xFFe4e4e4))
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
                        urlError = url.isBlank()

                        if (!urlError) {
                            onImportPlaylist(spotifyApiUrl, trimUrl)
                            onClose()
                        }
                    }
                    .padding(vertical = 16.dp),
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

@Preview(showSystemUi = true)
@Composable
private fun AddSpotifyPlaylistBottomSheetPreview() {
    AddSpotifyPlaylistBottomSheet(
        onClose = {},
        onImportPlaylist = { _,_ ->}
    )
}