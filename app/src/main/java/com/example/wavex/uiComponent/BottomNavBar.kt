package com.example.wavex.uiComponent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wavex.R
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.navigation.BottomItem
import com.example.wavex.navigation.BottomNavRoute

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        BottomItem(
            BottomNavRoute.Home.route,
            "Home",
            R.drawable.home_filled,
            R.drawable.home_outline
        ),
        BottomItem(
            BottomNavRoute.Browse.route,
            "Browse",
            R.drawable.discover_filled,
            R.drawable.discover_outline
        ),
        BottomItem(
            BottomNavRoute.Search.route,
            "Search",
            R.drawable.search_filled,
            R.drawable.search_outline
        ),
        BottomItem(
            BottomNavRoute.Library.route,
            "Library",
            R.drawable.library_filled,
            R.drawable.library_outline
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, bottom = 6.dp)
            .navigationBarsPadding().height(68.dp)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF2C2C2C).copy(alpha = 0.2f),
                spotColor = Color(0xFF2C2C2C).copy(alpha = 0.4f)
            ).background(
                color = Color(0xFF2C2C2C),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute?.startsWith(item.route) == true
                val weight by animateFloatAsState(
                    targetValue = if (selected) 1.2f else 0.9f,
                    animationSpec = tween(600),
                    label = "tabWeight"
                )

                Row(
                    modifier = Modifier
                        .weight(weight)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = if (selected)
                                Brush.horizontalGradient(
                                    listOf(
                                        colorResource(R.color.off_white).copy(alpha = 0.2F),
                                        colorResource(R.color.off_white).copy(alpha = 0.2F)
                                    )
                                )
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (selected) item.filledIcon else item.outlineIcon
                        ),
                        contentDescription = item.label,
                        tint = if (selected) colorResource(R.color.background_color)
                            else colorResource(R.color.off_white).copy(alpha = 0.40f),
                        modifier = Modifier.size(24.dp)
                    )

                    AnimatedVisibility(
                        visible = selected,
                        enter = expandHorizontally(
                            expandFrom = Alignment.Start,
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(),
                        exit = shrinkHorizontally(
                            shrinkTowards = Alignment.Start,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeOut()
                    ) {
                        Text(
                            text = item.label,
                            modifier = Modifier.padding(start = 6.dp),
                            color = Color(0xFFF6F6F6),
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontFamily = fonts,
                            maxLines = 1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavBarPreview() {
    BottomNavBar(navController = rememberNavController())
}