package com.example.wavex.profileScreen.artistsScreen

data class FavouriteArtistUiState(
    val artists: List<FavouriteArtist> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

