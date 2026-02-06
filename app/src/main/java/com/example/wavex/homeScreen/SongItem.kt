package com.example.wavex.homeScreen

import com.example.musify.songData.Artists
import com.example.musify.songData.Download
import com.example.musify.songData.Image

data class SongItem(
    val id: String = "",
    val name: String = "",
    val artist: MutableList<Artists> = mutableListOf(),
    val image: MutableList<Image> = mutableListOf(),
    val duration: Int = 0,
    val downloadUrl: MutableList<Download> = mutableListOf(),
    var isFav: Boolean = false
)