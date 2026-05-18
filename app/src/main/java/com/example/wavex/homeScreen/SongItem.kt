package com.example.wavex.homeScreen

import android.os.Parcelable
import com.example.wavex.songData.Album
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongItem(
    var id: String = "",
    var name: String = "",
    var artist: MutableList<Artists> = mutableListOf(),
    var album: Album? = null,
    var image: MutableList<Image> = mutableListOf(),
    var duration: Int = 0,
    var playCount: Int = 0,
    var downloadUrl: MutableList<Download> = mutableListOf(),
    var isFav: Boolean = false,
    var localPath: String? = null,
    val source: String? = null,
    val songSource: String ?= null
) : Parcelable