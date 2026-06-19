package com.example.wavex.feature.library.presentation.favourite.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wavex.core.shared.LikedSongsViewModel
import com.example.wavex.feature.profile.presentation.downloads.presentation.DownloadViewModel
import com.example.wavex.ui.theme.WaveXTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouriteSongActivity : ComponentActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()
    private val likedSongsViewModel: LikedSongsViewModel by viewModels()
    private val favouriteSongViewModel: FavouriteSongViewModel by viewModels()

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
                FavouriteSongScreen(
                    uiState = favouriteSongViewModel.uiState.collectAsStateWithLifecycle().value,
                    onDeleteSong = { songID ->
                        downloadViewModel.deleteSong(songID) { success, message -> }
                    },
                    downloadedIds = downloadViewModel
                        .downloadedSongIds
                        .collectAsState(initial = emptySet()).value,
                    likedSongs = likedSongsViewModel.likedSongs.collectAsStateWithLifecycle().value,
                    totalDuration = favouriteSongViewModel.totalDuration.collectAsStateWithLifecycle().value,
                    onToggleLike = { song ->
                        likedSongsViewModel.toggleLike(song)
                    },
                    onRemoveSong = { songID, onResult ->
                        favouriteSongViewModel.removeSong(songID) { success, message ->
                            onResult(success,message)
                        }
                    }
                )
            }
        }
    }
}