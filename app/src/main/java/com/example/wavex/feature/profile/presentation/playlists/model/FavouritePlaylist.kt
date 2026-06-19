package com.example.wavex.feature.profile.presentation.playlists.model

data class FavouritePlaylist(
    val playlistId: String = "",
    val playlistName: String = "",
    val playlistImageUrl: String = "",
    val isFavourite: Boolean = false,
    val source: String = ""
)