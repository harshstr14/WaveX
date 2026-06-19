package com.example.wavex.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Download(
    var quality: Quality = Quality.MEDIUM,
    var url: String = "",
    val expiresAt: Long? = null
): Parcelable
