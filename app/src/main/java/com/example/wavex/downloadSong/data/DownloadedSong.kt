package com.example.wavex.downloadSong.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wavex.songData.Album
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image

@Entity(tableName = "downloaded_songs")
data class DownloadedSong(
    @PrimaryKey
    val id: String,

    var name: String,
    var artist: MutableList<Artists> = mutableListOf(),
    var album: Album? = null,
    var image: MutableList<Image> = mutableListOf(),
    var duration: Int = 0,
    var playCount: Int = 0,
    var downloadUrl: MutableList<Download> = mutableListOf(),

    val localPath: String
)