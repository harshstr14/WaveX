package com.example.wavex.feature.profile.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels()

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

        val uid = FirebaseAuth.getInstance().currentUser?.uid

//        LaunchedEffect(uid) {
//            uid?.let { profileViewModel.refreshUserData(it) }
//        }

        setContent {
            WaveXTheme {
                ProfileScreen(
                    imageUrl = profileViewModel.profileImageUrl.collectAsStateWithLifecycle().value,
                    name = profileViewModel.userName.collectAsStateWithLifecycle().value
                )
            }
        }
    }
}