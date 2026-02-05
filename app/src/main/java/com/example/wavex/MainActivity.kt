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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                scrim = 0xFF0D0C0C.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFF0D0C0C.toInt()
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
        .background( Color(0xFF0D0C0C))) {
        Image(painter = painterResource(R.drawable.wavex_logo_light), contentDescription = "Logo",
            modifier = Modifier.align(Alignment.Center).size(280.dp))
    }
}

@Preview(showSystemUi = true)
@Composable
fun Preview() {
    WaveXTheme {
        SplashScreen(onTimeout = {})
    }
}