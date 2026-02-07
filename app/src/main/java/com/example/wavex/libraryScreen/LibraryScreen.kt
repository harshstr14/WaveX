package com.example.wavex.libraryScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wavex.R
import com.example.wavex.fonts

@Composable
fun LibraryScreen(navController: NavController) {
    val (backInteraction, backScale) = pressScale()
    val (addInteraction, addScale) = pressScale()
    val (spotifyInteraction, spotifyScale) = pressScale(1.12f)

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
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
                color = colorResource(R.color.secondary_text_color),
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
                color = colorResource(R.color.secondary_text_color),
                shape = RoundedCornerShape(20.dp)
            ).clickable(
                interactionSource = addInteraction,
                indication = null
            ) {

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
                .size(38.dp).graphicsLayer {
                    scaleX = spotifyScale
                    scaleY = spotifyScale
                }.clickable(
                    interactionSource = spotifyInteraction,
                    indication = null
                ) { }
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