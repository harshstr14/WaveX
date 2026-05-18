package com.example.wavex.profileScreen.albumsScreen

data class FavouriteAlbum(
    val albumId: String = "",
    val albumName: String = "",
    val albumImageUrl: String = "",
    val primaryArtists: String = "",
    val isFavourite: Boolean = false,
    val source: String = ""
)
