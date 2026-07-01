package com.example.wavex.feature.playlist.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wavex.core.shared.LikedSongsViewModel
import com.example.wavex.feature.library.presentation.LibraryViewModel
import com.example.wavex.feature.profile.presentation.downloads.presentation.DownloadViewModel
import com.example.wavex.ui.theme.WaveXTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistActivity : ComponentActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()
    private val likedSongsViewModel: LikedSongsViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()

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

        val playlistId = intent.getStringExtra("playlist_id")
        val playlistImageUrl = intent.getStringExtra("playlist_imageUrl")
        val playlistSource = intent.getStringExtra("playlist_source") ?: "unknown"
        val gradientColors = intent.getIntegerArrayListExtra("playlist_gradient")
        val rectangularImage = intent.getBooleanExtra("rectangular_image", false)

        val gradient = gradientColors?.map { Color(it) } ?: emptyList()
        val playlistTitle = intent.getStringExtra("playlist_title") ?: ""

        setContent {
            WaveXTheme {
                PlaylistScreen(
                    playlistId = playlistId,
                    playlistImageUrl = playlistImageUrl,
                    playlistSource = playlistSource,
                    playlistTitle = playlistTitle,
                    rectangularImage = rectangularImage,
                    gradient = gradient,
                    playlists = playlistViewModel.playlists.collectAsStateWithLifecycle().value,
                    isLoading = playlistViewModel.isLoading.collectAsStateWithLifecycle().value,
                    onLoadPlaylist = { playlistID ->
                        playlistViewModel.loadPlaylist(playlistID)
                    },
                    onLoadYTPlaylist = { playlistID ->
                        playlistViewModel.loadYTPlaylist(playlistID)
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
                    },
                    onCheckFavourite = { playlistID ->
                        playlistViewModel.checkFavourite(playlistID)
                    },
                    onToggleFavourite = { playlistID, playlistName, imageUrl, source, onResult ->
                        playlistViewModel.toggleFavourite(
                            playlistId = playlistID,
                            playlistName = playlistName,
                            imageUrl = imageUrl,
                            playlistSource = source
                        ) { message ->
                            onResult(message)
                        }
                    },
                    isFavourite = playlistViewModel.isFavourite.collectAsStateWithLifecycle().value,
                    libraryPlaylists = libraryViewModel.playlists.collectAsStateWithLifecycle().value,
                    onAddSongToPlaylist = { playlistID, song, onResult ->
                        libraryViewModel.addSongToPlaylist(
                            playlistId = playlistID,
                            song = song,
                            onResult = onResult
                        )
                    }
                )
            }
        }
    }
}