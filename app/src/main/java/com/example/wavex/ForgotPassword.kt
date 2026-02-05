package com.example.wavex

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

private lateinit var auth: FirebaseAuth
class ForgotPassword : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                ForgotPasswordScreen()
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen() {
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold (
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
                            data.visuals.message.contains("email") -> R.drawable.email_icon
                            else -> {
                                R.drawable.alert_icon
                            }
                        } ), contentDescription = "Icons",
                            tint = Color(0xFF34A853), modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = data.visuals.message,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 13.sp,
                            color = Color(0xFFF6F6F6)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val context = LocalContext.current
        var email by remember { mutableStateOf("") }
        val activity = remember(context) { context as? Activity }

        ConstraintLayout(modifier = Modifier.fillMaxSize().padding(paddingValues).background(colorResource(R.color.background_color))) {
            val (backIcon,titleText,descriptionText,emailLabel,emailInputContainer,sendLinkButton) = createRefs()

            Box(modifier = Modifier.constrainAs(backIcon) {
                top.linkTo(parent.top, margin = 25.dp)
                start.linkTo(parent.start, margin = 25.dp)
            }.size(36.dp).clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.5.dp,
                    color = colorResource(R.color.secondary_text_color),
                    shape = RoundedCornerShape(20.dp)
                ).clickable { activity?.finish() }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_icon),
                    contentDescription = "add Icon",
                    tint = colorResource(R.color.primary_text_color),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text("Forgot Password", modifier = Modifier.constrainAs(titleText) {
                top.linkTo(backIcon.bottom, margin = 25.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, fontSize = 22.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 26.sp
            )

            Text("Please enter your email address to\nreceive a reset password link", modifier = Modifier.constrainAs(descriptionText) {
                top.linkTo(titleText.bottom, margin = 14.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 18.sp
            )

            Text("Email", modifier = Modifier.constrainAs(emailLabel) {
                top.linkTo(descriptionText.bottom, margin = 25.dp)
                start.linkTo(parent.start, margin = 28.dp)
            }, fontSize = 12.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color)
            )

            Box(modifier = Modifier.constrainAs(emailInputContainer) {
                top.linkTo(emailLabel.bottom, margin = 10.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.padding(horizontal = 25.dp).height(52.dp).fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                    val (inputField, placeholderText) = createRefs()

                    if (email.isEmpty()) {
                        Text(modifier = Modifier.constrainAs(placeholderText) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 15.dp)
                            end.linkTo(parent.end, margin = 15.dp)
                            width = Dimension.fillToConstraints },
                            text = "Enter Email",
                            fontFamily = fonts,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp,
                            color = colorResource(R.color.secondary_text_color)
                        )
                    }

                    val selectionColors = TextSelectionColors(
                        handleColor = Color(0xFF1C1C1C),
                        backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
                    )

                    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                        BasicTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .constrainAs(inputField) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start, margin = 15.dp)
                                    end.linkTo(parent.end, margin = 15.dp)
                                    width = Dimension.fillToConstraints
                                },
                            textStyle = TextStyle(
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 14.sp, lineHeight = 17.sp,
                                color = colorResource(R.color.secondary_text_color)
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Color(0xFF1C1C1C))
                        )
                    }
                }
            }

            Button(modifier = Modifier.constrainAs(sendLinkButton) {
                top.linkTo(emailInputContainer.bottom, margin = 35.dp)
            }.fillMaxWidth().padding(horizontal = 25.dp).height(52.dp).shadow(
                elevation = 26.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = colorResource(R.color.theme_color).copy(alpha = 0.2f),
                spotColor = colorResource(R.color.theme_color).copy(alpha = 0.4f)
            ),
                onClick = {
                    keyboardController?.hide()

                    if (email.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Please enter email",
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@Button
                    }

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Enter a valid email",
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@Button
                    }

                    auth.sendPasswordResetEmail(email.trim()).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("RecoveryPassword", "Password reset email sent")
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Password reset email sent",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                Log.e("RecoveryPassword", "Failed", task.exception)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = task.exception?.localizedMessage ?: "Something went wrong",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                    }
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.theme_color),
                    contentColor = colorResource(R.color.background_color)
                ) , shape = RoundedCornerShape(26.dp)) {

                Text("Send Link", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal, fontSize = 18.sp
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    WaveXTheme {
        ForgotPasswordScreen()
    }
}