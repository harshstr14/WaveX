package com.example.wavex.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wavex.core.model.Album
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Image

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey
    val id: String,

    var name: String,
    var artist: MutableList<Artists> = mutableListOf(),
    var album: Album? = null,
    var image: MutableList<Image> = mutableListOf(),
    var duration: Int = 0,
    var playCount: Int = 0,
    var downloadUrl: MutableList<Download> = mutableListOf(),

    val localPath: String,
    val songSource: String ?= null,
    val playedAt: Long = System.currentTimeMillis(),
)