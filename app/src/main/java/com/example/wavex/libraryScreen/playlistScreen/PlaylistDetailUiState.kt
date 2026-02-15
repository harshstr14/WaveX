package com.example.wavex.libraryScreen.playlistScreen

import com.example.wavex.libraryScreen.PlaylistData

data class PlaylistDetailUiState(
    val playlist: PlaylistData? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)
