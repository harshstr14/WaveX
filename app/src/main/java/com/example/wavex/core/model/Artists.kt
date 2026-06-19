package com.example.wavex.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artists(
    var id: String = "",
    var name: String = "",
    var role: String = "",
    var image: String = "",
    var type: String = "",
    val searchSource: String = ""
) : Parcelable
