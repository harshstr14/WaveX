package com.example.wavex.libraryScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val(backIcon,text,addIcon,logo,row) = createRefs()

        Text("Library", modifier = Modifier.constrainAs(text) {
            top.linkTo(parent.top, margin = 22.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
            fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFFF6F6F6), lineHeight = 22.sp
        )

        Box(modifier = Modifier.constrainAs(backIcon) {
            top.linkTo(text.top)
            bottom.linkTo(text.bottom)
            start.linkTo(parent.start, margin = 25.dp)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .size(36.dp).clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                color = Color(0xFF797979),
                shape = RoundedCornerShape(20.dp)
            ).clickable {
                navController.popBackStack()
            }, contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_icon),
                contentDescription = "add Icon",
                tint = Color(0xFFF6F6F6),
                modifier = Modifier.size(20.dp)
            )
        }

        Box(modifier = Modifier.constrainAs(addIcon) {
            top.linkTo(text.top)
            bottom.linkTo(text.bottom)
            end.linkTo(parent.end, margin = 25.dp)
        }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .size(36.dp).clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                color = Color(0xFF797979),
                shape = RoundedCornerShape(20.dp)
            ).clickable {

            }, contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.plus_icon),
                contentDescription = "add Icon",
                tint = Color(0xFFF6F6F6),
                modifier = Modifier.size(22.dp)
            )
        }

        Icon(
            painter = painterResource(R.drawable.spotify_logo),
            contentDescription = "add Icon",
            tint = Color.Unspecified,
            modifier = Modifier.constrainAs(logo){
                top.linkTo(text.top)
                bottom.linkTo(text.bottom)
                end.linkTo(addIcon.start, margin = 15.dp)
            }.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).size(38.dp)
        )

        Row (
            modifier = Modifier.constrainAs(row){
                top.linkTo(text.bottom, margin = 35.dp)
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
                    color = Color(0xFFF6F6F6), maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "total songs",
                    fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    color = Color(0xFF797979), maxLines = 1,
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
                        tint = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LibraryScreenPreview() {
    val navController = rememberNavController()
    LibraryScreen(navController)
}