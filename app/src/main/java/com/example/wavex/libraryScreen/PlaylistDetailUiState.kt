package com.example.wavex.libraryScreen

data class PlaylistDetailUiState(
    val playlists: List<PlaylistData> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)
