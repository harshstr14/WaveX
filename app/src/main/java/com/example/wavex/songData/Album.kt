package com.example.wavex.songData

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val id: String = "",
    val name: String = ""
): Parcelable