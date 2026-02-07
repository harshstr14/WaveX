package com.example.wavex.artistScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.artistScreen.allAlbumsScreen.AllAlbumsActivity
import com.example.wavex.artistScreen.allSongsScreen.AllSongsActivity
import com.example.wavex.fonts
import com.example.wavex.ui.theme.WaveXTheme
import kotlinx.coroutines.launch
import java.util.Locale

class ArtistActivity : ComponentActivity() {
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

        val artistId = intent.getStringExtra("artist_id")

        setContent {
            WaveXTheme {
                Artist_Activity(artistId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Artist_Activity(artistId: String?,viewModel: ArtistViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(artistId) {
        artistId?.let {
            viewModel.loadArtist(it)
        }
    }

    var isLiked by remember { mutableStateOf(false) }

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

    val progress by remember {
        derivedStateOf {
            val firstOffset = listState.firstVisibleItemScrollOffset
            (firstOffset / 600f).coerceIn(0f, 1f)
        }
    }

    val titleVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > 180
        }
    }

    val startSize = 120.dp
    val startOffsetX = 25.dp
    val startOffsetY = 0.dp

    val endSize = 48.dp
    val endOffsetX = 12.dp
    val endOffsetY = 0.dp

    val size = lerpDp(startSize, endSize, progress)
    val offsetX = lerpDp(startOffsetX, endOffsetX, progress)
    val offsetY = lerpDp(startOffsetY, endOffsetY, progress)

    val cornerRadius = lerpDp(60.dp, 60.dp, progress)
    val showMetaInfo = progress < 0.3f
    val metaAlpha = (1f - progress * 1.3f).coerceIn(0f, 1f)
    val titleTopPadding = lerpDp(15.dp, 0.dp, progress)
    val titleStartPadding = lerpDp(25.dp, 8.dp, progress)
    val titleFontSize = lerpDp(20.dp, 18.dp, progress)
    val iconScale = lerpDp(1.dp, 0.95.dp, progress).value
    val titleOffsetY = lerpDp((-12).dp, 0.dp, progress)

    val isTitleVisible = !isLoading && !artists.isError

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
                                    color = colorResource(R.color.secondary_text_color),
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
                        AnimatedVisibility(
                            visible = titleVisible,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 220,
                                    easing = FastOutSlowInEasing
                                )
                            ) + scaleIn(
                                initialScale = 0.95f,
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = FastOutSlowInEasing
                                )
                            ),

                            exit = fadeOut(
                                animationSpec = tween(
                                    durationMillis = 160,
                                    easing = LinearOutSlowInEasing
                                )
                            ) + scaleOut(
                                targetScale = 0.92f,
                                animationSpec = tween(
                                    durationMillis = 160,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = artists.imageUrl,
                                    contentDescription = "Profile Image",
                                    contentScale = ContentScale.Crop,
                                    modifier =  Modifier.padding(start = 8.dp)
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(cornerRadius))
                                        .graphicsLayer {
                                            shadowElevation = 8f * (1f - progress)
                                        }
                                        .zIndex(10f),
                                    placeholder = painterResource(R.drawable.logo),
                                    error = painterResource(R.drawable.logo)
                                )

                                Row(
                                    modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = artists.name,
                                        fontSize = 18.sp,
                                        lineHeight = 20.sp,
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.primary_text_color),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (artists.isVerified) {
                                        Icon(
                                            painter = painterResource(R.drawable.verified_icon),
                                            contentDescription = "Verified Icon",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(20.dp)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        Row(modifier = Modifier.padding(end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = colorResource(R.color.secondary_text_color),
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
                                        color = colorResource(R.color.secondary_text_color),
                                        shape = RoundedCornerShape(20.dp)
                                    ).clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        isLiked = !isLiked

                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Click on like",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
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
                    colors = TopAppBarDefaults.largeTopAppBarColors(
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
                .background(colorResource(R.color.background_color))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    LoadingEffect()
                }

                artists.isError -> {
                    ErrorState(
                        message = artists.errorMessage,
                        onRetry = {
                            artistId?.let { viewModel.loadArtist(it) }
                        }
                    )
                }

                else -> {
                    ConstraintLayout(modifier = Modifier.fillMaxSize().background(colorResource(R.color.background_color))) {
                        val (contentList) = createRefs()

                        LazyColumn (
                            state = listState,
                            modifier = Modifier.constrainAs(contentList){
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                height = Dimension.fillToConstraints
                            }) {

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = artists.imageUrl,
                                        contentDescription = "Profile Image",
                                        contentScale = ContentScale.Crop,
                                        modifier =  Modifier
                                            .offset(x = offsetX, y = offsetY)
                                            .size(size)
                                            .clip(RoundedCornerShape(cornerRadius))
                                            .graphicsLayer {
                                                alpha = metaAlpha
                                                shadowElevation = 8f * (1f - progress)
                                            }
                                            .zIndex(10f),
                                        placeholder = painterResource(R.drawable.logo),
                                        error = painterResource(R.drawable.logo)
                                    )

                                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                                        .offset(y = titleOffsetY).animateContentSize()) {
                                        Row(
                                            modifier = Modifier
                                                .padding(top = titleTopPadding, start = titleStartPadding),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = artists.name,
                                                fontSize = titleFontSize.value.sp,
                                                lineHeight = 22.sp,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.Bold,
                                                fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            if (artists.isVerified) {
                                                Icon(
                                                    painter = painterResource(R.drawable.verified_icon),
                                                    contentDescription = "Verified Icon",
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(20.dp)
                                                        .graphicsLayer {
                                                            scaleX = iconScale
                                                            scaleY = iconScale
                                                        }
                                                )
                                            }
                                        }

                                        if (showMetaInfo) {
                                            Row(
                                                modifier = Modifier.padding(top = 12.dp, start = 25.dp)
                                                    .graphicsLayer {
                                                        alpha = metaAlpha
                                                        scaleX = 1f - progress * 0.04f
                                                        scaleY = 1f - progress * 0.04f
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.followers_icon),
                                                    contentDescription = "Followers Icon",
                                                    tint = colorResource(R.color.primary_text_color),
                                                    modifier = Modifier.size(18.dp)
                                                )

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Text(
                                                    text = "Followers : ${
                                                        formatCount(artists.followerCount.toLong())
                                                    }",
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
                                                modifier = Modifier.padding(top = 4.dp, start = 25.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.headset_icon),
                                                    contentDescription = "Followers Icon",
                                                    tint = colorResource(R.color.primary_text_color),
                                                    modifier = Modifier.size(18.dp)
                                                )

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Text(
                                                    text = "Listeners : ${
                                                        formatCount(artists.fanCount.toLongOrNull() ?: 0L)
                                                    }",
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
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 24.dp, top = 15.dp, bottom = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Top Songs", fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                    )

                                    Text(modifier = Modifier.clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        val intent = Intent(context, AllSongsActivity::class.java).apply {
                                            putExtra("artist_id", artists.id)
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                        context.startActivity(intent)
                                    }, text = "See All", fontSize = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.theme_color), lineHeight = 18.sp
                                    )
                                }
                            }

                            items(
                                items = artists.topSongs,
                                key = { it.id }
                            ) { song ->
                                Row (
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(start = 24.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) {

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
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = song.name,
                                            fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                            color = colorResource(R.color.primary_text_color), maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        val artistsName = song.artist
                                            .takeIf { it.isNotEmpty() }
                                            ?.joinToString(", ") { it.name }
                                            ?: "Unknown Artist"

                                        Text(
                                            text = artistsName,
                                            fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
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

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 24.dp, top = 15.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Top Albums", fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                    )

                                    Text(modifier = Modifier.clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        val intent = Intent(context, AllAlbumsActivity::class.java).apply {
                                            putExtra("artist_id", artists.id)
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                        context.startActivity(intent)
                                    }, text = "See All", fontSize = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                        color = colorResource(R.color.theme_color), lineHeight = 18.sp
                                    )
                                }
                            }

                            item {
                                LazyRow(modifier = Modifier.fillMaxWidth()
                                    .padding(top = 10.dp),
                                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                    items(artists.topAlbums) { album ->
                                        Column(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = null
                                                ) {  }
                                        ) {
                                            AsyncImage(
                                                model = album.image[2].url,
                                                contentDescription = album.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .height(110.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                                                text = album.name,
                                                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color), maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            val artistsName = album.artist
                                                .takeIf { it.isNotEmpty() }
                                                ?.joinToString(", ") { it.name }
                                                ?: "Unknown Artist"

                                            Text( modifier = Modifier.padding(horizontal = 2.dp ),
                                                text = artistsName,
                                                fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color), maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Singles", modifier = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 8.dp)
                                    , fontSize = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                    color = colorResource(R.color.primary_text_color), lineHeight = 20.sp
                                )
                            }

                            item {
                                LazyRow(modifier = Modifier.fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 25.dp),
                                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                    items(artists.singles) { album ->
                                        Column(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = null
                                                ) {  }
                                        ) {
                                            AsyncImage(
                                                model = album.image[2].url,
                                                contentDescription = album.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .height(110.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text( modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                                                text = album.name,
                                                fontSize = 14.sp, lineHeight = 16.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.primary_text_color), maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            val artistsName = album.artist
                                                .takeIf { it.isNotEmpty() }
                                                ?.joinToString(", ") { it.name }
                                                ?: "Unknown Artist"

                                            Text( modifier = Modifier.padding(horizontal = 2.dp ),
                                                text = artistsName,
                                                fontSize = 12.sp, lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                                                color = colorResource(R.color.secondary_text_color), maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
            fontSize = 16.sp, lineHeight = 18.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
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

fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000_000 -> String.format(Locale.US,"%.1fB", count / 1_000_000_000.0)
        count >= 1_000_000 -> String.format(Locale.US,"%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US,"%.1fK", count / 1_000.0)
        else -> count.toString()
    }.replace(".0", "")
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
fun Artist_ActivityPreview() {
    WaveXTheme {
        Artist_Activity("")
    }
}