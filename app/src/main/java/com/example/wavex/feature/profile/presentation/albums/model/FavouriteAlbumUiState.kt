package com.example.wavex.feature.profile.presentation.albums.model

data class FavouriteAlbumUiState(
    val albums: List<FavouriteAlbum> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

