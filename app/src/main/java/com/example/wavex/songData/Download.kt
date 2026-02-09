package com.example.wavex.songData

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Download(
    val quality: String,
    val url: String
) : Parcelable
