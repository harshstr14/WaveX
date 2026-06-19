package com.example.wavex.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistData(
    var playlistId: String = "",
    var playlistName: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var totalSongs: Int = 0,
    var totalDuration: Int = 0,
    var songs: MutableList<SongItem> = mutableListOf()
): Parcelable