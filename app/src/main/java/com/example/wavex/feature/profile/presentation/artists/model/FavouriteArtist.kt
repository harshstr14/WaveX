package com.example.wavex.feature.profile.presentation.artists.model

data class FavouriteArtist(
    val artistId: String = "",
    val artistName: String = "",
    val artistImageUrl: String = "",
    val isFavourite: Boolean = false,
    val source: String = ""
)

