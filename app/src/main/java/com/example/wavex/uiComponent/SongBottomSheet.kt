package com.example.wavex.uiComponent

import android.content.Intent
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
import androidx.compose.ui.window.Dialog
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wavex.R
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.service.ParallelDownloader
import com.example.wavex.core.service.ServiceLocator
import com.example.wavex.feature.album.presentation.AlbumActivity
import com.example.wavex.feature.album.presentation.ShareType
import com.example.wavex.feature.artist.presentation.ArtistActivity
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.home.presentation.htmlToText
import com.example.wavex.feature.library.model.LibraryUiState
import com.example.wavex.feature.library.presentation.pressScale
import com.example.wavex.feature.profile.presentation.downloads.presentation.rememberNetworkState
import com.example.wavex.feature.search.presentation.SearchSource
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongBottomSheet(
    song: SongItem,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onDismiss: () -> Unit,
    onPlayNow: () -> Unit,
    isFavourite: Boolean,
    isDownloaded: Boolean,
    onToggleFavourite: (SongItem) -> Unit,
    onToggleDownload: (SongItem) -> Unit,
    playlists: LibraryUiState,
    onAddSongToPlaylist: (String, SongItem, onResult: (Boolean, String) -> Unit) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val isOnline = rememberNetworkState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showArtistsDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val artistsGridState = rememberLazyGridState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    LaunchedEffect(Unit) {
        sheetState.partialExpand()
    }

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = colorResource(R.color.background_color),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            BottomSheetContent(
                song,
                isPlaying = isPlaying,
                isCurrentSong = isCurrentSong,
                onPlayNow,
                isFavourite,
                isDownloaded,
                isOnline = isOnline,
                onToggleFavourite,
                onAddToPlaylistClick = {
                    showPlaylistDialog = true
                },
                onShowArtistsClick = {
                    showArtistsDialog = true
                },
                onShowShareSheet =  {
                    showShareSheet = true
                },
                onShowSnackBar = { message ->
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onToggleDownload
            )

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF414141),
                            spotColor = Color(0xFF414141)
                        ),
                    containerColor = Color(0xFF414141),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(
                                when {
                                    data.visuals.message.contains("Song") -> R.drawable.song_icon
                                    data.visuals.message.contains("playlist") -> R.drawable.playlist_icon
                                    data.visuals.message.contains("queue") -> R.drawable.queue_icon
                                    data.visuals.message.contains("downloads") ||
                                            data.visuals.message.contains("downloading") -> R.drawable.download_icon
                                    data.visuals.message.contains("Downloading") ||
                                            data.visuals.message.contains("downloaded") -> R.drawable.downloaded_icon
                                    data.visuals.message.contains("internet") -> R.drawable.cellular_network_icon
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

    if (showArtistsDialog) {
        Dialog(
            onDismissRequest = { showArtistsDialog = false}
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colorResource(R.color.off_white)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth().padding(20.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.mic_icon),
                            contentDescription = null,
                            tint = colorResource(R.color.theme_color),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Artists",
                            fontFamily = fonts,
                            fontSize = 18.sp, lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
                    )

                    LazyVerticalGrid(
                        state = artistsGridState,
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(song.artist) { artist ->
                            Column(
                                modifier = Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (isOnline) {
                                        val intent = Intent(context, ArtistActivity::class.java).apply {
                                            putExtra("artist_id", artist.id)
                                            putExtra("artist_imageUrl", artist.image)
                                            putExtra("artist_source",
                                                when(song.songSource) {
                                                    SearchSource.YTMUSIC.name -> {
                                                        SearchSource.YTMUSIC.name
                                                    }
                                                    SearchSource.JIOSAAVN.name -> {
                                                        SearchSource.JIOSAAVN.name
                                                    }

                                                    else -> {
                                                        "Unknown"
                                                    }
                                                }
                                            )
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                message = "No internet connection",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                } ,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = artist.image.takeIf { it.isNotBlank() },
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(R.drawable.default_artist),
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val artistName = htmlToText(artist.name)

                                Text(
                                    modifier = Modifier.width(78.dp),
                                    text = artistName,
                                    fontSize = 13.sp, lineHeight = 18.sp, fontFamily = fonts,
                                    fontWeight = FontWeight.Medium, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color),
                                    maxLines = 2, textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        ShareSong(
            song = ShareSongItem(
                title = htmlToText(song.name),
                subtitle = song.album?.name ?: "Unknown",
                artists = song.artist.joinToString(", ") { htmlToText(it.name) },
                image = when (song.songSource) {
                    SearchSource.YTMUSIC.name ->
                        song.image.getOrNull(0)?.url

                    SearchSource.JIOSAAVN.name ->
                        song.image.getOrNull(2)?.url
                            ?: song.image.lastOrNull()?.url

                    else ->
                        song.image.lastOrNull()?.url
                },
                id = song.id,
                type = ShareType.SONG
            ),
            onDismiss = { showShareSheet = false }
        )
    }

    if (showPlaylistDialog) {
        Dialog(
            onDismissRequest = { showPlaylistDialog = false }
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colorResource(R.color.off_white)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth().padding(20.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.playlist_icon),
                            contentDescription = null,
                            tint = colorResource(R.color.theme_color),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Select Playlist",
                            fontFamily = fonts,
                            fontSize = 18.sp, lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.2.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(
                            items = playlists.playlists,
                            key = { it.playlistId }
                        ) { playlist ->
                            Text(
                                text = playlist.playlistName,
                                fontFamily = fonts,
                                fontSize = 14.sp, lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.secondary_text_color),
                                maxLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showPlaylistDialog = false
                                        onAddSongToPlaylist(playlist.playlistId, song) { success, message ->
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSheetContent(
    song: SongItem,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onPlayNow: () -> Unit,
    isFavourite: Boolean,
    isDownloaded: Boolean,
    isOnline: Boolean,
    onToggleFavourite: (SongItem) -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onShowArtistsClick: () -> Unit,
    onShowShareSheet: () -> Unit,
    onShowSnackBar: (String) -> Unit,
    onToggleDownload: (SongItem) -> Unit
) {
    val context = LocalContext.current

    val musicService = ServiceLocator.musicService

    val queue by musicService?.queueFlow?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val isInQueue = queue.any { it.id == song.id }

    val playlist by musicService?.playlistFlow?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val isInPlaylist = playlist.any { it.id == song.id }

    var startAnimation by remember { mutableStateOf(false) }

    val shadowAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.6f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowAlpha"
    )

    val shadowBlur by animateFloatAsState(
        targetValue = if (startAnimation) 30f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowBlur"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }

    ParallelDownloader.downloadStates[song.id]
    val state = ParallelDownloader.downloadStates[song.id]

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
                .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(
                        when (song.songSource) {
                            SearchSource.YTMUSIC.name ->
                                song.image.getOrNull(0)?.url

                            SearchSource.JIOSAAVN.name ->
                                song.image.getOrNull(2)?.url
                                    ?: song.image.lastOrNull()?.url

                            else ->
                                song.image.lastOrNull()?.url
                        }
                    )
                    .allowHardware(false)
                    .build(),
                placeholder = painterResource(R.drawable.default_image),
                contentDescription = null,
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
                    .size(80.dp)
                    .drawBehind {
                        val safeBlur = shadowBlur.coerceAtLeast(0.1f)
                        val cornerRadius = 16.dp.toPx()

                        drawIntoCanvas { canvas ->
                            val frameworkPaint = Paint().apply {
                                isAntiAlias = true
                                color = shadowColor.copy(alpha = shadowAlpha).toArgb()

                                maskFilter = if (shadowBlur > 0f) {
                                    BlurMaskFilter(
                                        safeBlur,
                                        BlurMaskFilter.Blur.NORMAL
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
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = htmlToText(song.name), maxLines = 2,
                    fontSize = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.primary_text_color), lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = htmlToText(
                        song.artist.takeIf { it.isNotEmpty() }
                            ?.joinToString(", ") { it.name } ?: "Unknown Artist"
                    ), maxLines = 1,
                    fontSize = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color), lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            thickness = 1.dp,
            color = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ActionCard(
                title = if (isCurrentSong && isPlaying)
                    "Pause"
                else
                    "Play Now",
                icon = if (isCurrentSong && isPlaying)
                    R.drawable.notificationpausebutton
                else
                    R.drawable.notificationplaybutton,
                modifier = Modifier.weight(1f),
                onClick = {
                    onPlayNow()
                }
            )

            ActionCard(
                title = when {
                    isDownloaded -> "Remove From Download"
                    state == ParallelDownloader.DownloadState.DOWNLOADING -> "Download in Progress"
                    state == ParallelDownloader.DownloadState.PAUSED -> "Resume Download"
                    state == ParallelDownloader.DownloadState.FAILED -> "Retry Download"
                    else -> "Download"
                },
                icon = when {
                    isDownloaded -> R.drawable.downloaded_icon
                    state == ParallelDownloader.DownloadState.DOWNLOADING -> R.drawable.download_icon
                    else -> R.drawable.download_icon
                },
                modifier = Modifier.weight(1f),
                onClick = {
                    when {
                        isDownloaded -> {
                            onToggleDownload(song)
                            onShowSnackBar("Removed from downloads")
                        }

                        state == ParallelDownloader.DownloadState.DOWNLOADING -> {
                            onShowSnackBar("Song is already downloading")
                        }

                        state == ParallelDownloader.DownloadState.PAUSED -> {
                            onToggleDownload(song) // resume
                        }

                        state == ParallelDownloader.DownloadState.FAILED -> {
                            onToggleDownload(song) // retry
                        }

                        else -> {
                            onToggleDownload(song) // start
                            onShowSnackBar("Downloading started")
                        }
                    }
                }
            )

            ActionCard(
                title = "Share",
                icon = R.drawable.share_icon,
                modifier = Modifier.weight(1f),
                onClick = {
                    onShowShareSheet()
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFfefefe))
        ) {
            SheetOptionItem(
                verticalPadding = true,
                shape = RoundedCornerShape(22.dp),
                icon = if (isFavourite) R.drawable.heart_filled else R.drawable.heart_outline,
                title = if (isFavourite) "Remove from Favourite" else "Save to Favourite",
                subTitle = if (isFavourite)
                    "Remove this song from your favourites."
                else
                    "Save this song to your favourites.",
                enabled = isOnline
            ) {
                if (isOnline) {
                    onToggleFavourite(song)
                } else {
                    onShowSnackBar("No internet connection")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFfefefe))
        ) {
            SheetOptionItem(
                verticalPadding = true,
                shape = RoundedCornerShape(22.dp),
                icon = R.drawable.headset_icon,
                title = "Play Count",
                subTitle = formatCount(song.playCount.toLong())
            ) {

            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFfefefe))
        ) {
            SheetOptionItem(
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                icon = R.drawable.add_playlist_icon,
                title = "Add to Playlist",
                subTitle = "Add it to one of your playlists.",
                enabled = isOnline
            ) {
                if (isOnline) {
                    onAddToPlaylistClick()
                } else {
                    onShowSnackBar("No internet connection")
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                thickness = 1.dp,
                color = colorResource(R.color.background_color)
            )

            SheetOptionItem(
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                icon = R.drawable.queue_icon,
                title = if (isInQueue) "Remove from queue" else "Add to queue",
                subTitle = if (isInQueue)
                    "Remove this song from the queue."
                else
                    "Add to the end of the queue."
            ) {
                when {
                    isInQueue -> {
                        musicService?.removeFromQueue(song.id)
                        onShowSnackBar("Removed from queue")
                    }

                    isInPlaylist -> {
                        onShowSnackBar("Song already in playlist")
                    }

                    else -> {
                        musicService?.addToQueue(song)
                        onShowSnackBar("Added to queue")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFfefefe))
        ) {
            SheetOptionItem(
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                icon = R.drawable.mic_icon,
                title = "View Artist",
                subTitle =
                    htmlToText(
                        song.artist.takeIf { it.isNotEmpty() }
                            ?.joinToString(", ") { it.name } ?: "Unknown Artist"
                    )
            ) {
                onShowArtistsClick()
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                thickness = 1.dp,
                color = colorResource(R.color.background_color)
            )

            SheetOptionItem(
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                icon = R.drawable.album_icon,
                title = "View Album",
                subTitle = htmlToText(song.album?.name ?: "Unknown"),
                enabled = isOnline
            ) {
                if (isOnline) {
                    val intent = Intent(context, AlbumActivity::class.java).apply {
                        putExtra("album_id", song.album?.id)
                        putExtra("album_imageUrl", "")
                        putExtra("album_source",
                            when(song.songSource) {
                                SearchSource.YTMUSIC.name -> {
                                    SearchSource.YTMUSIC.name
                                }
                                SearchSource.JIOSAAVN.name -> {
                                    SearchSource.JIOSAAVN.name
                                }

                                else -> {
                                    "Unknown"
                                }
                            }
                        )
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(intent)
                } else {
                    onShowSnackBar("No internet connection")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .height(75.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.15f)
                ),
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFfefefe)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
               painter = painterResource(icon),
                contentDescription = title,
                tint = colorResource(R.color.theme_color),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        velocity = 35.dp,
                        animationMode = MarqueeAnimationMode.Immediately
                    ),
                textAlign = TextAlign.Center,
                text = title, overflow = TextOverflow.Visible,
                fontSize = 13.sp, fontFamily = fonts, maxLines = 1,
                fontWeight = FontWeight.Medium, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun SheetOptionItem(
    modifier: Modifier = Modifier,
    verticalPadding: Boolean = false,
    shape: Shape = RectangleShape,
    icon: Int,
    title: String,
    subTitle: String = "",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val(itemInteraction, itemScale) = pressScale()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(
                interactionSource = itemInteraction,
                indication = ripple(
                    bounded = true,
                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.15f)
                )
            ) {
                onClick()
            }
            .alpha(if (enabled) 1f else 0.4f)
            .padding(horizontal = 16.dp, vertical = if (verticalPadding) 14.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
                .graphicsLayer {
                    scaleX = itemScale
                    scaleY = itemScale
                },
            tint = colorResource(R.color.theme_color)
        )

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp, fontFamily = fonts,
                fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subTitle,
                fontSize = 12.sp, fontFamily = fonts,
                fontWeight = FontWeight.Medium, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.secondary_text_color), lineHeight = 16.sp
            )
        }
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000_000 -> String.format(Locale.US,"%.1fB", count / 1_000_000_000.0)
        count >= 1_000_000 -> String.format(Locale.US,"%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US,"%.1fK", count / 1_000.0)
        else -> count.toString()
    }.replace(".0", "")
}

@Preview(showSystemUi = true)
@Composable
private fun SongBottomSheetPreview() {
    SongBottomSheet(
        song = SongItem(),
        isPlaying = false,
        isCurrentSong = false,
        onPlayNow = {},
        isFavourite = false,
        isDownloaded = false,
        onToggleFavourite = {},
        onToggleDownload = {},
        playlists = LibraryUiState(),
        onAddSongToPlaylist = { _, _, _ -> },
        onDismiss = {}
    )
}