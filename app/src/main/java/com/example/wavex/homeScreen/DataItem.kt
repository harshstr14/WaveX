package com.example.wavex.homeScreen

import com.example.musify.songData.Artists
import com.example.musify.songData.Image

data class DataItem(
    val id: String,
    val name: String,
    val artist: MutableList<Artists>,
    val image: MutableList<Image>
)