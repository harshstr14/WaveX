package com.example.wavex.homeScreen

import com.example.musify.songData.Artists
import com.example.musify.songData.Download
import com.example.musify.songData.Image

data class SongItem(
    val id: String,
    val name: String,
    val artist: MutableList<Artists>,
    val image: MutableList<Image>,
    val duration: Int,
    val downloadUrl: MutableList<Download>,
    var isFav: Boolean = false
)