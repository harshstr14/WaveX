package com.example.wavex.feature.profile.presentation.playlists.model

data class FavouritePlaylistUiState(
    val playlists: List<FavouritePlaylist> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

