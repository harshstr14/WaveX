package com.example.wavex.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataItem(
    val id: String = "",
    val name: String = "",
    val artist: MutableList<Artists> = mutableListOf(),
    val image: MutableList<Image> = mutableListOf(),
    val searchSource: String = ""
): Parcelable