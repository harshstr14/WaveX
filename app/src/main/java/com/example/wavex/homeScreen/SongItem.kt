package com.example.wavex.homeScreen

import android.os.Parcelable
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongItem(
    val id: String = "",
    val name: String = "",
    val artist: MutableList<Artists> = mutableListOf(),
    val image: MutableList<Image> = mutableListOf(),
    val duration: Int = 0,
    val downloadUrl: MutableList<Download> = mutableListOf(),
    var isFav: Boolean = false
) : Parcelable