package com.example.wavex.feature.profile.presentation.playlists.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wavex.ui.theme.WaveXTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistsActivity : ComponentActivity() {
    private val favouritePlaylistViewModel: FavouritePlaylistViewModel by viewModels()

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

        setContent {
            WaveXTheme {
                PlaylistsScreen(
                    playlists = favouritePlaylistViewModel.playlists.collectAsStateWithLifecycle().value,
                    onDeletePlaylist = {
                        favouritePlaylistViewModel.deleteAllPlaylists()
                    }
                )
            }
        }
    }
}