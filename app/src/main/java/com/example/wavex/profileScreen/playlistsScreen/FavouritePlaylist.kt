package com.example.wavex.profileScreen.playlistsScreen

data class FavouritePlaylist(
    val playlistId: String = "",
    val playlistName: String = "",
    val playlistImageUrl: String = "",
    val isFavourite: Boolean = false,
    val source: String = ""
)