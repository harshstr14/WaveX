package com.example.wavex.recommendation.dataClass

import com.example.wavex.songData.Album
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image

data class PlayedSong(
    var id: String = "",
    var name: String = "",
    var artist: MutableList<Artists> = mutableListOf(),
    var album: Album? = null,
    var image: MutableList<Image> = mutableListOf(),
    var duration: Int = 0,
    var playCount: Int = 0,
    var downloadUrl: MutableList<Download> = mutableListOf(),
    val lastPlayed: Long = 0L,
    val songSource: String ?= null
)