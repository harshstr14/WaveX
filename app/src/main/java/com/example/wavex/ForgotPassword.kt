package com.example.wavex

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ForgotPassword : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                ForgotPasswordScreen(auth = auth)
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    auth: FirebaseAuth
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        var email by rememberSaveable { mutableStateOf("") }
        val activity = remember(context) { context as? Activity }
        var emailError by rememberSaveable { mutableStateOf(false) }
        var emailFocused by rememberSaveable { mutableStateOf(false) }
        var emailErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.background_color))
        ) {
            val (
                backIcon, titleText, descriptionText, emailLabel,
                emailInputContainer, sendLinkButton
            ) = createRefs()

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

            Text(
                text = "Forgot Password",
                modifier = Modifier
                    .constrainAs(titleText) {
                        top.linkTo(backIcon.bottom, margin = 25.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                fontSize = 24.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 28.sp
            )

            Text(
                text = "Please enter your email address to\nreceive a reset password link",
                modifier = Modifier
                    .constrainAs(descriptionText) {
                        top.linkTo(titleText.bottom, margin = 14.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                fontSize = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 16.sp
            )

            Text(
                text = "Email",
                modifier = Modifier
                    .constrainAs(emailLabel) {
                        top.linkTo(descriptionText.bottom, margin = 25.dp)
                        start.linkTo(parent.start, margin = 28.dp)
                    },
                fontSize = 14.sp, lineHeight = 18.sp, fontFamily = fonts,
                fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color)
            )

            Box(
                modifier = Modifier
                    .constrainAs(emailInputContainer) {
                        top.linkTo(emailLabel.bottom, margin = 10.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(horizontal = 25.dp)
                    .height(52.dp).fillMaxWidth()
                    .border(
                        width = 1.1.dp,
                        color = when {
                            emailError -> Color.Red
                            emailFocused -> colorResource(R.color.theme_color)
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
                ConstraintLayout(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val (inputField, placeholderText) = createRefs()

                    if (email.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .constrainAs(placeholderText) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start, margin = 15.dp)
                                    end.linkTo(parent.end, margin = 15.dp)
                                    width = Dimension.fillToConstraints
                                },
                            text = "Enter Email",
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
                            value = email,
                            onValueChange = {
                                email = it

                                if (it.isNotBlank()) {
                                    emailError = false
                                }
                            },
                            modifier = Modifier
                                .constrainAs(inputField) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start, margin = 15.dp)
                                    end.linkTo(parent.end, margin = 15.dp)
                                    width = Dimension.fillToConstraints
                                }
                                .onFocusChanged {
                                    emailFocused = it.isFocused
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
                }
            }

            Column(
                modifier = Modifier
                    .constrainAs(sendLinkButton) {
                        top.linkTo(emailInputContainer.bottom, margin = 10.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(horizontal = 25.dp)
            ) {
                AnimatedVisibility(emailError) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .align(Alignment.Start),
                        text = emailErrorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal
                    )
                }

                Spacer(modifier = Modifier.height(if (emailError) 25.dp else 25.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 26.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = colorResource(R.color.theme_color).copy(alpha = 0.2f),
                            spotColor = colorResource(R.color.theme_color).copy(alpha = 0.4f)
                        ),
                    onClick = {
                        keyboardController?.hide()

                        when {
                            email.isBlank() -> {
                                emailError = true
                                emailErrorMessage = "Email is required"

                                return@Button
                            }

                            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                emailError = true
                                emailErrorMessage = "Enter a valid email"

                                return@Button
                            }
                        }

                        auth.sendPasswordResetEmail(email.trim()).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("RecoveryPassword", "Password reset email sent")
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = "Password reset email sent",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                Log.e("RecoveryPassword", "Failed", task.exception)
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = task.exception?.localizedMessage ?: "Something went wrong",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.theme_color),
                        contentColor = colorResource(R.color.background_color)
                    ),
                    shape = RoundedCornerShape(22.dp)) {

                    Text(
                        text = "Send Link", fontSize = 16.sp, lineHeight = 18.sp,
                        fontFamily = fonts, fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal, color = colorResource(R.color.off_white)
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    WaveXTheme {
        
    }
}