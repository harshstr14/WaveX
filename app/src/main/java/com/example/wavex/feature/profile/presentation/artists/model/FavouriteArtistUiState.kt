package com.example.wavex.feature.profile.presentation.artists.model

data class FavouriteArtistUiState(
    val artists: List<FavouriteArtist> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

