package com.example.wavex.homeScreen

import com.example.wavex.songData.Artists
import com.example.wavex.songData.Image

data class DataItem(
    val id: String = "",
    val name: String = "",
    val artist: MutableList<Artists> = mutableListOf(),
    val image: MutableList<Image> = mutableListOf()
)