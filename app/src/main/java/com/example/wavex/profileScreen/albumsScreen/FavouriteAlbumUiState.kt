package com.example.wavex.profileScreen.albumsScreen

data class FavouriteAlbumUiState(
    val albums: List<FavouriteAlbum> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

