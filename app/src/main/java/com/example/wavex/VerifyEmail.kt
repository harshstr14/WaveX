package com.example.wavex

import android.app.Activity
import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var name: String = ""
private var email: String = ""
private lateinit var auth: FirebaseAuth
private lateinit var database: DatabaseReference

class VerifyEmail : ComponentActivity() {
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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference()
        val user = auth.currentUser

        name = intent.getStringExtra("name")?.takeIf { it.isNotBlank() } ?: "User${user?.uid?.takeLast(4)}"

        email = intent.getStringExtra("email").toString()

        Log.d("User Details","$name, $email" )

        setContent {
            WaveXTheme {
                VerifyEmailScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkIfVerified()
    }

    private fun checkIfVerified() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        user.reload().addOnCompleteListener {
            if (user.isEmailVerified) {
                Log.d("Verify", "Email verified")

                val userID = FirebaseAuth.getInstance().currentUser?.uid
                val userData = mapOf(
                    "name" to name,
                    "mail" to email,
                )
                if (userID != null) {
                    database.child("Users").child(userID).setValue(userData).addOnSuccessListener {
                        startActivity(
                            Intent(this, MainScreen::class.java)
                        )
                        finish()
                    }.addOnFailureListener { e ->
                        Log.e("FirebaseDB", "Failed to save user data: ${e.message}", e)
                    }
                } else {
                    Log.e("Auth", "UID is null after successful registration")
                }
            }
        }
    }
}

@Composable
fun VerifyEmailScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var secondsLeft by rememberSaveable { mutableIntStateOf(60) }
    var canResend by rememberSaveable { mutableStateOf(false) }

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
                            data.visuals.message.contains("Email") -> R.drawable.email_icon
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
        val activity = remember(context) { context as? Activity }

        ConstraintLayout(modifier = Modifier.fillMaxSize().padding(paddingValues).background(colorResource(R.color.background_color))) {
            val (backIcon,mailAnimation,titleText,descriptionText,resendSection,verifyEmailButton) = createRefs()

            Box(
                modifier = Modifier.constrainAs(backIcon) {
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

            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes (R.raw.mail)
            )

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.constrainAs(mailAnimation) {
                    top.linkTo(backIcon.bottom, margin = 25.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.size(144.dp)
            )

            Text("Verify Your Email", modifier = Modifier.constrainAs(titleText) {
                top.linkTo(mailAnimation.bottom, margin = 25.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, fontSize = 22.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 26.sp
            )

            Text("We’ve sent a verification link to \nyour email $email.\nPlease check your inbox and \nclick the link to verify your account", modifier = Modifier.constrainAs(descriptionText) {
                top.linkTo(titleText.bottom, margin = 18.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 20.sp
            )

            Row (modifier = Modifier.constrainAs(resendSection) {
                top.linkTo(descriptionText.bottom, margin = 25.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }) {
                Text("Didn't receive the email?" , fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.width(3.dp))

                Text(if (canResend) "Resend" else "Resend in ${secondsLeft}s", fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                    color = if (canResend) colorResource(R.color.theme_color) else Color(0xFF555555), textAlign = TextAlign.Center, lineHeight = 15.sp, modifier = Modifier
                        .clickable(enabled = canResend) {
                            val user = FirebaseAuth.getInstance().currentUser ?: return@clickable

                            user.sendEmailVerification()
                            secondsLeft = 60
                            canResend = false

                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Verification email sent",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                )
            }

            Button(modifier = Modifier.constrainAs(verifyEmailButton) {
                top.linkTo(resendSection.bottom, margin = 25.dp)
            }.fillMaxWidth().padding(horizontal = 25.dp).height(52.dp).shadow(
                elevation = 26.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = colorResource(R.color.theme_color).copy(alpha = 0.2f),
                spotColor = colorResource(R.color.theme_color).copy(alpha = 0.4f)
            ),
                onClick = {
                    val user = FirebaseAuth.getInstance().currentUser ?: return@Button

                    user.reload().addOnCompleteListener {
                        if (user.isEmailVerified) {
                            val userID = FirebaseAuth.getInstance().currentUser?.uid
                            val userData = mapOf(
                                "name" to name,
                                "mail" to email,
                            )
                            if (userID != null) {
                                database.child("Users").child(userID).setValue(userData).addOnSuccessListener {
                                    context.startActivity(Intent(context, MainScreen::class.java))
                                    activity?.finish()
                                }.addOnFailureListener { e ->
                                    Log.e("FirebaseDB", "Failed to save user data: ${e.message}", e)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Could not save user info. Please try again",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            } else {
                                Log.e("Auth", "UID is null after successful registration")
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Something went wrong. Please try again",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }

                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Email Verification Successfully",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } else {
                            Log.d("Auth", "Email verification error")
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Email not verified yet",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.theme_color),
                    contentColor = colorResource(R.color.background_color)
                ) , shape = RoundedCornerShape(26.dp)) {

                Text("Verify Email", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal, fontSize = 18.sp
                )
            }
        }
    }

    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        } else {
            canResend = true
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun VerifyEmailScreenPreview() {
    WaveXTheme {
        VerifyEmailScreen()
    }
}