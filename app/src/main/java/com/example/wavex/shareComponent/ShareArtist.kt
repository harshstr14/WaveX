package com.example.wavex.shareComponent

import android.content.ClipData
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wavex.R
import com.example.wavex.albumScreen.ShareType
import com.example.wavex.fonts
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.pressScale
import kotlinx.coroutines.launch

data class ShareArtistItem(
    val id: String,
    val name: String,
    val followerCount: String,
    val fanCount: String,
    val isVerified: Boolean,
    val topSongs: List<SongItem> = emptyList(),
    val image: String?,
    val type: ShareType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareArtist(
    artist: ShareArtistItem,
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

    LaunchedEffect(Unit) {
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
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(artist.image?.takeIf { it.isNotBlank() })
                                .allowHardware(false)
                                .build(),
                            contentDescription = null,
                            placeholder = painterResource(R.drawable.default_artist),
                            error = painterResource(R.drawable.default_artist),
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        if (artist.isVerified) {
                            Icon(
                                painter = painterResource(R.drawable.verified_icon),
                                contentDescription = "Verified Icon",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .padding(end = 6.dp, bottom = 4.dp)
                                    .align(Alignment.BottomEnd)
                                    .size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column (
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = htmlToText(artist.name)
                                .takeIf { it.isNotBlank() }
                                ?: "Unknown Title",
                            maxLines = 1,
                            fontSize = 19.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color),
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.followers_icon),
                                contentDescription = "Followers Icon",
                                tint = colorResource(R.color.secondary_text_color),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Followers : ${artist.followerCount}",
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
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.headset_icon),
                                contentDescription = "Headset Icon",
                                tint = colorResource(R.color.secondary_text_color),
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Listeners : ${artist.fanCount}",
                                fontSize = 13.sp,
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
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
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "TOP TRACKS",
                                color = colorResource(R.color.secondary_text_color),
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontFamily = fonts,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Normal,
                            )

                            Icon(
                                painter = painterResource(R.drawable.arrow_up),
                                contentDescription = "Arrow Icon",
                                tint = colorResource(R.color.theme_color),
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            Text(
                                text = "TRENDING",
                                color = colorResource(R.color.theme_color),
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontFamily = fonts,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Normal,
                            )
                        }

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = colorResource(R.color.secondary_text_color).copy(alpha = 0.7f),
                        )

                        artist.topSongs.take(3).forEachIndexed { index, song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("%02d", index + 1),
                                    color = colorResource(R.color.theme_color),
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = fonts,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Normal,
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
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal,
                                )
                            }

                            if (index != artist.topSongs.take(3).lastIndex) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.7f),
                                )
                            }
                        }

                        if (artist.topSongs.size > 3) {
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
                                    text = "+ ${artist.topSongs.size - 3} MORE",
                                    color = colorResource(R.color.secondary_text_color),
                                    fontSize = 12.sp,
                                    fontFamily = fonts,
                                    fontStyle = FontStyle.Normal,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Normal
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
                        .background(
                            color = colorResource(R.color.theme_color),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(
                            interactionSource = copyLinkInteraction,
                            indication = null
                        ) {
                            scope.launch {
                                val link = generateShareLink(artist)

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
                        text = "Copy Artist Link",
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
                        modifier = Modifier.weight(1f),
                        interactionSource = whatsAppInteraction,
                        onClick = {
                            val link = generateShareLink(artist)

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
                        modifier = Modifier.weight(1f),
                        interactionSource = messageInteraction,
                        onClick = {
                            val link = generateShareLink(artist)

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
                            val link = generateShareLink(artist)

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

private fun generateShareLink(item: ShareArtistItem): String {
    return when (item.type) {
        ShareType.SONG -> "https://wavex-edd95.web.app/song/${item.id}"
        ShareType.ALBUM -> "https://wavex-edd95.web.app/album/${item.id}"
        ShareType.PLAYLIST -> "https://wavex-edd95.web.app/playlist/${item.id}"
        ShareType.ARTIST -> "https://wavex-edd95.web.app/artist/${item.id}"
    }
}

@Composable
@Preview(showSystemUi = true)
private fun ShowShareArtist() {
    val sampleArtist = ShareArtistItem(
        id = "",
        name = "",
        followerCount = "",
        fanCount = "",
        isVerified = false,
        topSongs = emptyList(),
        image = "",
        type = ShareType.ARTIST
    )
    ShareArtist(
        artist = sampleArtist,
        onDismiss = {}
    )
}