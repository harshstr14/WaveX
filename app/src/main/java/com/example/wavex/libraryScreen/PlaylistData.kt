package com.example.wavex.libraryScreen

import com.example.wavex.homeScreen.SongItem

data class PlaylistData(
    var playlistName: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var totalSongs: Int = 0,
    var totalDuration: Int = 0,
    var songs: MutableList<SongItem> = mutableListOf()
)
