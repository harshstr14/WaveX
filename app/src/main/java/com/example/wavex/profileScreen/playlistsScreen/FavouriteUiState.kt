package com.example.wavex.profileScreen.playlistsScreen

data class FavouriteUiState(
    val playlists: List<FavouritePlaylist> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

