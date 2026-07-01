package com.example.wavex.feature.artist.presentation

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
import kotlin.getValue

@AndroidEntryPoint
class ArtistActivity : ComponentActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()
    private val artistViewModel: ArtistViewModel by viewModels()
    private val likedSongsViewModel: LikedSongsViewModel by viewModels()
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
        val artistImageUrl = intent.getStringExtra("artist_imageUrl")
        val artistSource = intent.getStringExtra("artist_source") ?: "unknown"

        setContent {
            WaveXTheme {
                ArtistScreen(
                    artistId = artistId,
                    artistImageUrl = artistImageUrl,
                    artistSource = artistSource,
                    artists = artistViewModel.artists.collectAsStateWithLifecycle().value,
                    isLoading = artistViewModel.isLoading.collectAsStateWithLifecycle().value,
                    onLoadArtist = { artistID ->
                        artistViewModel.loadArtist(artistID)
                    },
                    onLoadYTArtist = { artistID ->
                        artistViewModel.loadYTArtist(artistID)
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
                    onCheckFavourite ={ artistID ->
                        artistViewModel.checkFavouriteArtist(artistID)
                    },
                    onToggleFavourite = { artistID, artistName, imageUrl, source, onResult ->
                        artistViewModel.toggleFavouriteArtist(
                            artistId = artistID,
                            artistName = artistName,
                            artistImageUrl = imageUrl,
                            source = source
                        ) { message ->
                            onResult(message)
                        }
                    },
                    isFavourite = artistViewModel.isFavourite.collectAsStateWithLifecycle().value,
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