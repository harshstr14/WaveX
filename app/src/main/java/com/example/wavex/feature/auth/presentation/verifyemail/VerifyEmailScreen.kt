package com.example.wavex.feature.auth.presentation.verifyemail

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.MainScreen
import com.example.wavex.R
import com.example.wavex.feature.auth.model.VerifyEmailUiState
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.ui.theme.WaveXTheme

@Composable
fun VerifyEmailScreen(
    email: String,
    uiState: VerifyEmailUiState,
    onResendEmail: () -> Unit,
    onVerifyEmail: () -> Unit,
    onClearSnackbar: () -> Unit,
    onRefreshUser: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState.snackBarMessage) {
        uiState.snackBarMessage?.let { message ->
            snackBarHostState.showSnackbar(message)

            onClearSnackbar()
        }
    }

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            context.startActivity(
                Intent(context, MainScreen::class.java)
            ).apply {
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                onRefreshUser()
            }

            activity?.finish()
        }
    }

    Scaffold (
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color(0xFF2C2C2C),
                            spotColor = Color(0xFF2C2C2C)
                        ),
                    containerColor = Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(when {
                            data.visuals.message.contains("email") -> R.drawable.email_icon
                            data.visuals.message.contains("Email") -> R.drawable.email_icon
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
                            color = colorResource(R.color.off_white)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val context = LocalContext.current
        val activity = remember(context) { context as? Activity }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.background_color))
        ) {
            val (backIcon, mailAnimation, titleText, descriptionText, resendSection, verifyEmailButton) = createRefs()

            Box(
                modifier = Modifier
                    .constrainAs(backIcon) {
                        top.linkTo(parent.top, margin = 25.dp)
                        start.linkTo(parent.start, margin = 25.dp)
                    }
                    .size(36.dp).clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.5.dp,
                        color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { activity?.finish() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_icon),
                    contentDescription = "add Icon",
                    tint = colorResource(R.color.primary_text_color),
                    modifier = Modifier.size(20.dp)
                )
            }

            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes (R.raw.mail)
            )

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .constrainAs(mailAnimation) {
                        top.linkTo(backIcon.bottom, margin = 25.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .size(144.dp)
            )

            Text(
                text = "Verify Your Email",
                modifier = Modifier
                    .constrainAs(titleText) {
                        top.linkTo(mailAnimation.bottom, margin = 25.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                fontSize = 22.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 26.sp
            )

            Text(
                text = "We’ve sent a verification link to \nyour email $email.\nPlease check your inbox and \nclick the link to verify your account",
                modifier = Modifier
                    .constrainAs(descriptionText) {
                        top.linkTo(titleText.bottom, margin = 18.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                fontSize = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 20.sp
            )

            Row (
                modifier = Modifier
                    .constrainAs(resendSection) {
                        top.linkTo(descriptionText.bottom, margin = 25.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Text(
                    text = "Didn't receive the email?" ,
                    fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.width(3.dp))

                Text(
                    text = if (uiState.canResend) "Resend" else "Resend in ${uiState.secondsLeft}s",
                    fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = if (uiState.canResend) colorResource(R.color.theme_color) else Color(0xFF555555),
                    textAlign = TextAlign.Center, lineHeight = 15.sp,
                    modifier = Modifier
                        .clickable(enabled = uiState.canResend) {
                            onResendEmail()
                        }
                )
            }

            Button(
                modifier = Modifier
                    .constrainAs(verifyEmailButton) {
                        top.linkTo(resendSection.bottom, margin = 25.dp)
                    }
                    .fillMaxWidth().padding(horizontal = 25.dp)
                    .height(54.dp)
                    .shadow(
                        elevation = 26.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = colorResource(R.color.theme_color).copy(alpha = 0.2f),
                        spotColor = colorResource(R.color.theme_color).copy(alpha = 0.4f)
                    ),
                onClick = {
                    onVerifyEmail()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.theme_color),
                    contentColor = colorResource(R.color.background_color)
                ),
                shape = RoundedCornerShape(22.dp)) {

                Text(
                    text = "Verify Email", fontFamily = fonts, fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal, fontSize = 15.sp
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun VerifyEmailScreenPreview() {
    WaveXTheme {
        VerifyEmailScreen(
            email = "harsh@gmail.com",
            uiState = VerifyEmailUiState(
                canResend = false,
                secondsLeft = 45
            ),
            onResendEmail = {},
            onVerifyEmail = {},
            onRefreshUser = {},
            onClearSnackbar = {},
        )
    }
}