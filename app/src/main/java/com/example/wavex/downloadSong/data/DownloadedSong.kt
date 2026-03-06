package com.example.wavex.downloadSong.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSong(
    @PrimaryKey
    val id: String,

    var name: String,
    var artist: String,
    var album: String,
    var image: String,
    var duration: Int,
    var playCount: Int,
    var downloadUrl: String,
)