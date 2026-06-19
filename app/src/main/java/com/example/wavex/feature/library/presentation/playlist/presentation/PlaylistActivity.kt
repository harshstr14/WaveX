package com.example.wavex.feature.library.presentation.playlist.presentation

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
class PlaylistActivity : ComponentActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()
    private val likedSongsViewModel: LikedSongsViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()

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

        val playlistId = intent.getStringExtra("playlist_Id")

        setContent {
            WaveXTheme {
                PlaylistScreen(
                    playlistId = playlistId,
                    playlist = playlistViewModel.playlist.collectAsStateWithLifecycle().value,
                    onObservePlaylists = { playlistID ->
                        playlistViewModel.observePlaylists(playlistID)
                    },
                    onRemoveSong = { playlistID, songID, onResult ->
                        playlistViewModel.removeSong(playlistID, songID) { success, message ->
                            onResult(success,message)
                        }
                    },
                    onDeleteSong = { songID ->
                        downloadViewModel.deleteSong(songID) { success, message -> }
                    },
                    downloadedIds = downloadViewModel
                        .downloadedSongIds
                        .collectAsState(initial = emptySet()).value,
                    likedSongs = likedSongsViewModel.likedSongs.collectAsState().value,
                    onToggleLike = { song ->
                        likedSongsViewModel.toggleLike(song)
                    }
                )
            }
        }
    }
}