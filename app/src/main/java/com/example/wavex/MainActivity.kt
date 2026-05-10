package com.example.wavex

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val isLoggedIn = firebaseUser != null
        val isAnonymous = firebaseUser?.isAnonymous == true
        val isEmailVerified = firebaseUser?.isEmailVerified == true

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = 0xFFFAF9F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFFAF9F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                SplashScreen {
                    when {
                        // ✅ Anonymous user
                        isLoggedIn && isAnonymous -> {
                            startActivity(Intent(this, MainScreen::class.java))
                        }

                        // ✅ Email user & verified
                        isLoggedIn && isEmailVerified -> {
                            startActivity(Intent(this, MainScreen::class.java))
                        }

                        // ❌ Email user but not verified
                        isLoggedIn && !isEmailVerified -> {
                            startActivity(Intent(this, SignIn::class.java))
                        }

                        // ❌ Not logged in
                        else -> {
                            startActivity(Intent(this, SignIn::class.java))
                        }
                    }
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize()
        .background(colorResource(R.color.off_white)
        )
    ) {
        Image(painter = painterResource(R.drawable.wavex_logo_dark),
            contentDescription = "Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun Preview() {
    WaveXTheme {
        SplashScreen(onTimeout = {})
    }
}