package com.example.wavex.feature.artist.allalbums.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wavex.ui.theme.WaveXTheme

class AllAlbumsActivity : ComponentActivity() {
    private val allAlbumsViewModel: AllAlbumsViewModel by viewModels()

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
                AllAlbumsScreen(
                    artistId = artistId,
                    albums = allAlbumsViewModel.albums.collectAsStateWithLifecycle().value,
                    isLoading = allAlbumsViewModel.isLoading.collectAsStateWithLifecycle().value,
                    onFetchAlbum = { artistID, root ->
                        allAlbumsViewModel.fetchAlbumsByArtistID(artistID, root)
                    },
                    onLoadNextPage = {
                        allAlbumsViewModel.loadNextPage()
                    }
                )
            }
        }
    }
}