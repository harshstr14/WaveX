package com.example.wavex.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    var quality: String = "",
    var url: String = ""
): Parcelable