package com.example.wavex.profileScreen.artistsScreen

data class FavouriteArtist(
    val artistId: String = "",
    val artistName: String = "",
    val artistImageUrl: String = "",
    val isFavourite: Boolean = false,
    val source: String = ""
)

