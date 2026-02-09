package com.example.wavex.songData

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artists(
    val id: String,
    val name: String,
    val role: String,
    val image: String,
    val type: String
) : Parcelable
