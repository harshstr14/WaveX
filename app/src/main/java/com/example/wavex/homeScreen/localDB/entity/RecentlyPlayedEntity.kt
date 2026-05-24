package com.example.wavex.homeScreen.localDB.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wavex.songData.Album
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image

@Entity(tableName = "recently_played_songs")
data class RecentlyPlayedEntity(
    @PrimaryKey
    var id: String,

    var name: String = "",
    var artist: MutableList<Artists> = mutableListOf(),
    var album: Album? = null,
    var image: MutableList<Image> = mutableListOf(),
    var duration: Int = 0,
    var playCount: Int = 0,
    var downloadUrl: MutableList<Download> = mutableListOf(),

    val playedAt: Long = System.currentTimeMillis(),
    var localPath: String? = null,
    val songSource: String ?= null
)
