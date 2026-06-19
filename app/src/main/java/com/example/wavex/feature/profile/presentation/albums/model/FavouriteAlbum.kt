package com.example.wavex.feature.profile.presentation.albums.model

data class FavouriteAlbum(
    val albumId: String = "",
    val albumName: String = "",
    val albumImageUrl: String = "",
    val primaryArtists: String = "",
    val isFavourite: Boolean = false,
    val source: String = ""
)
