package com.example.wavex.feature.album.presentation

import android.os.Bundle
import android.util.Log
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

enum class ShareType {
    SONG,
    ALBUM,
    PLAYLIST,
    ARTIST,
    USERPLAYLIST
}

@AndroidEntryPoint
class AlbumActivity : ComponentActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()
    private val albumViewModel: AlbumViewModel by viewModels()
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

        val albumId = intent.getStringExtra("album_id")
        val albumImageUrl = intent.getStringExtra("album_imageUrl")
        val albumSource = intent.getStringExtra("album_source") ?: "unknown"

        Log.d("ALBUM_ID", "$albumId")

        setContent {
            WaveXTheme {
                AlbumScreen(
                    albumId = albumId,
                    albumImageUrl = albumImageUrl,
                    albumSource = albumSource,
                    albums = albumViewModel.albums.collectAsStateWithLifecycle().value,
                    isLoading = albumViewModel.isLoading.collectAsStateWithLifecycle().value,
                    onLoadAlbum = { albumID ->
                        albumViewModel.loadAlbum(albumID)
                    },
                    onLoadYTAlbum = { albumID ->
                      albumViewModel.loadYTAlbum(albumID)
                    },
                    onDeleteSong = { songID ->
                        downloadViewModel.deleteSong(songID) { success, message -> }
                    },
                    downloadedIds = downloadViewModel
                        .downloadedSongIds
                        .collectAsState(initial = emptySet()).value,
                    likedSongs = likedSongsViewModel.likedSongs.collectAsStateWithLifecycle().value,
                    onToggleLike = { song ->
                        likedSongsViewModel.toggleLike(song)
                    },
                    onCheckFavourite = { albumID ->
                        albumViewModel.checkFavourite(albumID)
                    },
                    onToggleFavourite = { albumID, albumName, imageUrl, primaryArtists, source, onResult ->
                        albumViewModel.toggleFavourite(
                            albumId = albumID,
                            albumName = albumName,
                            imageUrl = imageUrl,
                            primaryArtists = primaryArtists,
                            source = source
                        ) { message ->
                            onResult(message)
                        }
                    },
                    isFavourite = albumViewModel.isFavourite.collectAsStateWithLifecycle().value,
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