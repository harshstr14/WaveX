package com.example.wavex.albumScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.artistScreen.ArtistActivity
import com.example.wavex.fonts
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.formatDuration
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.service.MusicPlayerService
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class AlbumActivity : ComponentActivity() {
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

        val albumId = intent.getStringExtra("album_id")
        val albumImageUrl = intent.getStringExtra("album_imageUrl")

        setContent {
            WaveXTheme {
                Album_Activity(albumId, albumImageUrl)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Album_Activity(albumId: String?, albumImageUrl: String?, viewModel: AlbumViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity
    var isLiked by remember { mutableStateOf(false) }

    val userID = FirebaseAuth.getInstance().currentUser?.uid

    val favouriteReference = FirebaseDatabase.getInstance().getReference().child("Users")
        .child(userID!!).child("Favourites").child("Albums").child(albumId.toString())

    LaunchedEffect(albumId) {
        albumId?.let {
            viewModel.loadAlbum(it)
        }
    }

    LaunchedEffect(Unit) {
        favouriteReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLiked = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HeartPop"
    )

    val (backInteraction, backScale) = pressScale()
    val (shareInteraction, shareScale) = pressScale()

    val rawProgress by remember {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
            (offset / 600f).coerceIn(0f, 1f)
        }
    }

    val smoothProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ImageCollapseSpring"
    )

    val startSize = 160.dp
    val startOffsetX = 24.dp
    val startOffsetY = 0.dp

    val endSize = 88.dp
    val endOffsetX = 12.dp
    val endOffsetY = 0.dp

    val size = lerpDp(startSize, endSize, smoothProgress)
    val offsetX = lerpDp(startOffsetX, endOffsetX, smoothProgress)
    val offsetY = lerpDp(startOffsetY, endOffsetY, smoothProgress)

    val cornerRadius = lerpDp(16.dp, 14.dp, smoothProgress)
    val metaAlpha = (1f - smoothProgress * 1.3f).coerceIn(0f, 1f)
    val titleTopPadding = lerpDp(12.dp, 0.dp, smoothProgress)
    val titleStartPadding = lerpDp(25.dp, 8.dp, smoothProgress)
    val titleFontSize = lerpDp(20.dp, 18.dp, smoothProgress)

    val isTitleVisible = !isLoading && !albums.isError

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)).
        nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            if (isTitleVisible) {
                TopAppBar(
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

                    },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(20.dp)
                                    ).clickable(
                                        interactionSource = shareInteraction,
                                        indication = null
                                    ) {

                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.share_icon),
                                    contentDescription = "Share Icon",
                                    tint = colorResource(R.color.primary_text_color),
                                    modifier = Modifier.padding(end = 2.dp).size(18.dp)
                                        .graphicsLayer {
                                            scaleX = shareScale
                                            scaleY = shareScale
                                        }
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
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
                                        favouriteReference.addListenerForSingleValueEvent(object : ValueEventListener {

                                            override fun onDataChange(snapshot: DataSnapshot) {

                                                if (!snapshot.exists()) {
                                                    val artistsList = albums.primaryArtists
                                                        .takeIf { it.isNotEmpty() }
                                                        ?.joinToString(", ") { it.name }
                                                        ?: "Unknown Artist"

                                                    val artistsName = htmlToText(artistsList)

                                                    val albumData = mapOf(
                                                        "albumId" to albumId,
                                                        "albumName" to albums.albumName,
                                                        "albumImageUrl" to albumImageUrl,
                                                        "primaryArtists" to artistsName,
                                                        "isFavourite" to true
                                                    )

                                                    favouriteReference.setValue(albumData)
                                                        .addOnSuccessListener {
                                                            isLiked = true
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Added To Favourite",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Failed To Add in Favourite",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }

                                                } else {

                                                    favouriteReference.removeValue()
                                                        .addOnSuccessListener {
                                                            isLiked = false
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Removed From Favourite",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Database Error: ${error.message}")
                                                }
                                            }
                                        })
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(if (isLiked) R.drawable.heart_filled else R.drawable.heart_outline),
                                    contentDescription = "Like Icon",
                                    tint = if (isLiked)
                                        colorResource(R.color.theme_color)
                                    else
                                        colorResource(R.color.primary_text_color),
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = heartScale
                                        scaleY = heartScale
                                    }.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(R.color.background_color),
                        scrolledContainerColor = colorResource(R.color.background_color)
                    )
                )
            }
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
                            data.visuals.message.contains("Favourite") -> R.drawable.heart_outline
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
            when {
                isLoading -> {
                    LoadingEffect()
                }

                albums.isError -> {
                    ErrorState(
                        message = albums.errorMessage,
                        onRetry = {
                            albumId?.let { viewModel.loadAlbum(it) }
                        }
                    )
                }

                else -> {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize().background(colorResource(R.color.background_color))
                    ) {
                        val (contentList) = createRefs()

                        LazyColumn (
                            state = listState,
                            modifier = Modifier.constrainAs(contentList){
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                height = Dimension.fillToConstraints
                            },
                            contentPadding = PaddingValues(bottom = 25.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                                ) {
                                    AsyncImage(
                                        model = albumImageUrl ?: R.drawable.default_image,
                                        contentDescription = "Album Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .offset(x = offsetX, y = offsetY)
                                            .size(size)
                                            .clip(RoundedCornerShape(cornerRadius))
                                            .graphicsLayer {
                                                alpha = metaAlpha
                                            }
                                            .zIndex(10f)
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 15.dp)
                                            .animateContentSize()
                                    ) {
                                        val albumName = htmlToText(albums.albumName)

                                        Text(
                                            modifier = Modifier.padding(top = titleTopPadding,start = titleStartPadding, end = 10.dp),
                                            text = albumName,
                                            fontSize = titleFontSize.value.sp,
                                            lineHeight = 22.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        val description = htmlToText(albums.description)

                                        Text(
                                            modifier = Modifier.padding(top = 6.dp,start = titleStartPadding, end = 10.dp),
                                            text = description,
                                            fontSize = 12.sp,
                                            lineHeight = 14.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.secondary_text_color),
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            modifier = Modifier.padding(top = 8.dp, start = titleStartPadding)
                                                .graphicsLayer {
                                                    alpha = metaAlpha
                                                    scaleX = 1f - smoothProgress * 0.04f
                                                    scaleY = 1f - smoothProgress * 0.04f
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.headset_icon),
                                                contentDescription = "Headset Icon",
                                                tint = colorResource(R.color.primary_text_color),
                                                modifier = Modifier.size(18.dp)
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "Songs : ${albums.songCount}",
                                                fontSize = 12.sp,
                                                lineHeight = 12.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.padding(top = 4.dp, start = titleStartPadding)
                                                .graphicsLayer {
                                                    alpha = metaAlpha
                                                    scaleX = 1f - smoothProgress * 0.04f
                                                    scaleY = 1f - smoothProgress * 0.04f
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.clock),
                                                contentDescription = "Time Icon",
                                                tint = colorResource(R.color.primary_text_color),
                                                modifier = Modifier.size(18.dp)
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = formatTotalDuration(albums.totalDuration),
                                                fontSize = 12.sp,
                                                lineHeight = 12.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier.width(158.dp).padding(top = 25.dp)
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(colorResource(R.color.theme_color))
                                            .clickable { }
                                            .padding(horizontal = 24.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.shuffle_icon),
                                                contentDescription = "Shuffle Icon",
                                                tint = colorResource(R.color.background_color),
                                                modifier = Modifier.size(24.dp)
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "Shuffle",
                                                fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.background_color)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(18.dp))

                                    Box(
                                        modifier = Modifier.width(158.dp).padding(top = 25.dp)
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f))
                                            .clickable { }
                                            .padding(horizontal = 24.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.play_icon),
                                                contentDescription = "Play Icon",
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(22.dp)
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "Play",
                                                fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.theme_color)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    modifier = Modifier.padding(top = 15.dp, start = 24.dp),
                                    text = "Artists", fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                )
                            }

                            item {
                                LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 15.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    items(albums.primaryArtists) { artist ->
                                        Column(
                                            modifier = Modifier
                                                .clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                val intent = Intent(context, ArtistActivity::class.java).apply {
                                                    putExtra("artist_id", artist.id)
                                                    putExtra("artist_imageUrl", artist.image)
                                                }
                                                context.startActivity(intent)
                                            },
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            AsyncImage(
                                                model = artist.image.takeIf { it.isNotBlank() },
                                                contentDescription = artist.name,
                                                contentScale = ContentScale.Crop,
                                                error = painterResource(R.drawable.default_artist),
                                                modifier = Modifier
                                                    .size(78.dp)
                                                    .clip(CircleShape)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            val artistName = htmlToText(artist.name)

                                            Text( modifier = Modifier.width(78.dp),
                                                text = artistName,
                                                fontSize = 13.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color), maxLines = 2, textAlign = TextAlign.Center,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    modifier = Modifier.padding(top = 15.dp, start = 24.dp, bottom = 10.dp),
                                    text = "Songs", fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                )
                            }

                            itemsIndexed(albums.songs, key = { _, song -> song.id }) { index, song ->
                                Row (
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(start = 24.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {
                                            val intent = Intent(context, MusicPlayerService::class.java).apply {
                                                action = MusicPlayerService.ACTION_PLAY_NEW
                                                putParcelableArrayListExtra("playlist", ArrayList(albums.songs))
                                                putExtra("index", index)
                                            }

                                            ContextCompat.startForegroundService(context, intent)

                                            scope.launch {
                                                RecentlyPlayedManager.add(context, song)
                                            }
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = song.image[2].url,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        val songName = htmlToText(song.name)

                                        Text(
                                            text = songName,
                                            fontSize = 15.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val artistsList = song.artist
                                            .takeIf { it.isNotEmpty() }
                                            ?.joinToString(", ") { it.name }
                                            ?: "Unknown Artist"

                                        val artistsName = htmlToText(artistsList)

                                        Text(
                                            text = artistsName,
                                            fontSize = 13.sp,
                                            lineHeight = 14.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.secondary_text_color),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = formatDuration(song.duration),
                                            fontSize = 12.sp,
                                            lineHeight = 14.sp,
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.secondary_text_color),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { }) {
                                            Icon(
                                                modifier = Modifier.size(22.dp),
                                                painter = painterResource(R.drawable.download_icon),
                                                contentDescription = "Download",
                                                tint = colorResource(R.color.primary_text_color).copy(alpha = 0.6f)
                                            )
                                        }

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
    }
}

@Composable
fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
    return start + (end - start) * fraction
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes (R.raw.spaceman)
        )

        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(144.dp)
        )

        Spacer(modifier = Modifier.height(0.dp))

        Text(
            modifier = Modifier.offset(y = (-8).dp),
            text = message,
            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = colorResource(R.color.secondary_text_color), maxLines = 2
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier.offset(y = (-8).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(R.color.theme_color))
                .clickable { onRetry() }
                .padding(horizontal = 24.dp, vertical = 10.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Retry",
                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.background_color)
            )
        }
    }
}

private fun formatTotalDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("$hours h ")
        if (minutes > 0) append("$minutes min  ")
        if (seconds > 0) append("$seconds s")
        if (isEmpty()) append("0s") // handle 0 case
    }.trim()
}

@Composable
fun LoadingEffect() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes (R.raw.astronaut_and_music)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.fillMaxWidth().size(144.dp)
    )
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
fun Album_ActivityPreview() {
    WaveXTheme {
        Album_Activity("1245648", "")
    }
}