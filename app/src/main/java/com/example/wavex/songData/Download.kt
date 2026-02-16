package com.example.wavex.songData

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Download(
    var quality: String = "",
    var url: String = ""
): Parcelable
