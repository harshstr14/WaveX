package com.example.wavex.feature.artist.allsongs.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wavex.core.shared.LikedSongsViewModel
import com.example.wavex.feature.library.presentation.LibraryViewModel
import com.example.wavex.feature.profile.presentation.downloads.presentation.DownloadViewModel
import com.example.wavex.ui.theme.WaveXTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllSongsActivity : ComponentActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()
    private val likedSongsViewModel: LikedSongsViewModel by viewModels()
    private val allSongsViewModel: AllSongsViewModel by viewModels()
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

        val artistId = intent.getStringExtra("artist_id")

        setContent {
            WaveXTheme {
                AllSongsScreen(
                    artistId = artistId,
                    songs = allSongsViewModel.songs.collectAsStateWithLifecycle().value,
                    isLoading = allSongsViewModel.isLoading.collectAsStateWithLifecycle().value,
                    onFetchSongs = { artistID, root ->
                        allSongsViewModel.fetchSongsByArtistID(artistID, root)
                    },
                    onLoadNextPage = {
                        allSongsViewModel.loadNextPage()
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
                    playlists = libraryViewModel.playlists.collectAsStateWithLifecycle().value,
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