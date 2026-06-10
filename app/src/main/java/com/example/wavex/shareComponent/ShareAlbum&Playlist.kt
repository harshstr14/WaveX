package com.example.wavex.shareComponent

import android.content.ClipData
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wavex.CryptoUtils
import com.example.wavex.R
import com.example.wavex.albumScreen.ShareType
import com.example.wavex.fonts
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.pressScale
import kotlinx.coroutines.launch
import java.net.URLEncoder

data class ShareAlbumPlaylistItem(
    val id: String,
    val title: String,
    val artists: String,
    val songs: List<SongItem> = emptyList(),
    val songCount: String,
    val totalDuration: String,
    val image: String?,
    val type: ShareType,
    val source: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAlbum_Playlist(
    album: ShareAlbumPlaylistItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val (copyLinkInteraction, copyLinkScale) = pressScale()
    val (whatsAppInteraction, whatsAppScale) = pressScale()
    val (messageInteraction, messageScale) = pressScale()
    val (moreInteraction, moreScale) = pressScale()

    val clipboardManager = LocalClipboard.current
    val startAnimation = remember { mutableStateOf(false) }

    val shadowAlpha by animateFloatAsState(
        targetValue = if (startAnimation.value) 0.8f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowAlpha"
    )

    val shadowBlur by animateFloatAsState(
        targetValue = if (startAnimation.value) 60f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowBlur"
    )

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }

    LaunchedEffect(Unit) {
        startAnimation.value = true
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.background_color),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .width(58.dp)
                        .height(4.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(50))
                        .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(album.image?.takeIf { it.isNotBlank() })
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        placeholder = painterResource(R.drawable.default_image),
                        error = painterResource(R.drawable.default_image),
                        onSuccess = { result ->
                            val drawable = result.result.drawable
                            val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return@AsyncImage

                            Palette.from(bitmap).generate { palette ->
                                palette?.dominantSwatch?.rgb?.let { colorInt ->
                                    shadowColor = Color(colorInt)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(120.dp)
                            .drawBehind {
                                val safeBlur = shadowBlur.coerceAtLeast(0.1f)
                                val cornerRadius = 18.dp.toPx()

                                drawIntoCanvas { canvas ->
                                    val frameworkPaint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        color = shadowColor.copy(alpha = shadowAlpha).toArgb()

                                        maskFilter = if (shadowBlur > 0f) {
                                            android.graphics.BlurMaskFilter(
                                                safeBlur,
                                                android.graphics.BlurMaskFilter.Blur.NORMAL
                                            )
                                        } else {
                                            null
                                        }
                                    }

                                    canvas.nativeCanvas.drawRoundRect(
                                        0f,
                                        0f,
                                        size.width,
                                        size.height,
                                        cornerRadius,
                                        cornerRadius,
                                        frameworkPaint
                                    )
                                }
                            }
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    Column (
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = htmlToText(album.title)
                                .takeIf { it.isNotBlank() }
                                ?: "Unknown Title",
                            maxLines = 2,
                            fontSize = 22.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.mic_icon),
                                contentDescription = "Artist Icon",
                                tint = colorResource(R.color.secondary_text_color),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Artists • ${htmlToText(album.artists).takeIf { it.isNotBlank() }
                                    ?: "Unknown Title"}",
                                fontSize = 13.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.secondary_text_color),
                                lineHeight = 16.sp,
                                maxLines = 1,
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(3.dp))

                            Text(
                                text = "${album.songCount.takeIf { it.isNotBlank() }
                                    ?: "0"} Tracks",
                                fontSize = 12.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.secondary_text_color),
                                lineHeight = 16.sp,
                                maxLines = 1,
                            )

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .width(1.8.dp)
                                    .height(10.dp)
                                    .background(
                                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.width(3.dp))

                            Text(
                                text = album.totalDuration.takeIf { it.isNotBlank() }
                                    ?: "0",
                                fontSize = 12.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.secondary_text_color),
                                lineHeight = 16.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.primary_text_color).copy(alpha = 0.90f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        album.songs.take(3).forEachIndexed { index, song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = (index + 1).toString().padStart(2, '0'),
                                    color = colorResource(R.color.theme_color),
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = fonts,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(36.dp)
                                )

                                Text(
                                    text = htmlToText(song.name).ifBlank { "Unknown Song" },
                                    color = colorResource(R.color.off_white),
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                        .padding(end = 10.dp)
                                )

                                Text(
                                    text = if (song.duration > 0) {
                                        formatDuration(song.duration)
                                    } else {
                                        "--:--"
                                    },
                                    color = colorResource(R.color.secondary_text_color),
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.Medium,
                                    fontStyle = FontStyle.Normal,
                                )
                            }

                            if (index != album.songs.take(3).lastIndex) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.7f),
                                )
                            }
                        }

                        if (album.songs.size > 3) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.7f),
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+ ${album.songs.size - 3} MORE",
                                    color = colorResource(R.color.secondary_text_color),
                                    fontSize = 12.sp,
                                    fontFamily = fonts,
                                    fontStyle = FontStyle.Normal,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .graphicsLayer(
                            scaleX = copyLinkScale,
                            scaleY = copyLinkScale
                        )
                        .background(
                            color = colorResource(R.color.theme_color),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(
                            interactionSource = copyLinkInteraction,
                            indication = null
                        ) {
                            scope.launch {
                                val link = generateShareLink(album)

                                val clipData = ClipData.newPlainText("link", link)
                                val clipEntry = ClipEntry(clipData)

                                clipboardManager.setClipEntry(clipEntry)

                                snackBarHostState.showSnackbar("Link copied")
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 18.dp),
                        text = if (album.type == ShareType.ALBUM) "Copy Album Link" else "Copy Playlist Link",
                        fontSize = 14.sp,
                        fontFamily = fonts,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.background_color),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .width(1.8.dp)
                            .height(16.dp)
                            .background(
                                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )

                    Icon(
                        painter = painterResource(R.drawable.link_icon),
                        contentDescription = "Link Icon",
                        tint = colorResource(R.color.background_color),
                        modifier = Modifier
                            .padding(end = 18.dp, start = 13.dp)
                            .size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ShareAppItem(
                        title = "WhatsApp",
                        icon = R.drawable.whatsapp_icon,
                        iconBackground = Color(0xFF123A24),
                        iconTint = Color(0xFF25D366),
                        modifier = Modifier.weight(1f)
                            .graphicsLayer(
                                scaleX = whatsAppScale,
                                scaleY = whatsAppScale
                            ),
                        interactionSource = whatsAppInteraction,
                        onClick = {
                            val link = generateShareLink(album)

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                this.type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, link)
                                setPackage("com.whatsapp")
                            }

                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                scope.launch {
                                    snackBarHostState.showSnackbar("WhatsApp not installed")
                                }
                            }
                        }
                    )

                    ShareAppItem(
                        title = "Message",
                        icon = R.drawable.message_icon,
                        iconBackground = Color(0xFF132B4A),
                        iconTint = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                            .graphicsLayer(
                                scaleX = messageScale,
                                scaleY = messageScale
                            ),
                        interactionSource = messageInteraction,
                        onClick = {
                            val link = generateShareLink(album)

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "smsto:".toUri()
                                putExtra("sms_body", link)
                            }

                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    modifier = Modifier
                        .clickable(
                            interactionSource = moreInteraction,
                            indication = null
                        ) {
                            val link = generateShareLink(album)

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                this.type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, link)
                            }

                            context.startActivity(
                                Intent.createChooser(shareIntent, "Share via")
                            )
                        }
                        .graphicsLayer(
                            scaleX = moreScale,
                            scaleY = moreScale
                        ),
                    text = "More Options",
                    fontSize = 16.sp,
                    fontFamily = fonts,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(15.dp))

                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 30.dp)
                        .clip(RectangleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.wavex_logo_dark),
                        contentDescription = "Logo Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(width = 120.dp, height = 30.dp)
                            .graphicsLayer {
                                scaleX = 1.6f
                                scaleY = 1.6f
                            }
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    containerColor = Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(
                                when {
                                    data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
                                    data.visuals.message.contains("Link") -> R.drawable.link_icon
                                    else -> R.drawable.alert_icon
                                }
                            ),
                            contentDescription = null,
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
    }
}

private fun generateShareLink(item: ShareAlbumPlaylistItem): String {
    val payload = "${item.source}|${item.id}"
    val token = URLEncoder.encode(
        CryptoUtils.encrypt(payload),
        "UTF-8"
    )

    return when (item.type) {
        ShareType.SONG ->
            "https://wavex-edd95.web.app/song/$token"

        ShareType.ALBUM ->
            "https://wavex-edd95.web.app/album/$token"

        ShareType.PLAYLIST ->
            "https://wavex-edd95.web.app/playlist/$token"

        ShareType.ARTIST ->
            "https://wavex-edd95.web.app/artist/$token"
    }
}

@Composable
@Preview(showSystemUi = true)
private fun ShowShareAlbumPlaylist() {
    val sampleAlbum = ShareAlbumPlaylistItem(
        id = "",
        title = "",
        artists = "",
        songs = emptyList(),
        songCount = "",
        totalDuration = "",
        image = "",
        type = ShareType.ALBUM,
        source = ""
    )

    ShareAlbum_Playlist(
        album = sampleAlbum,
        onDismiss = {}
    )
}