package com.example.wavex.songData

import android.os.Parcelable
import com.example.wavex.profileScreen.settingScreen.Quality
import kotlinx.parcelize.Parcelize

@Parcelize
data class Download(
    var quality: Quality = Quality.MEDIUM,
    var url: String = "",
    val expiresAt: Long? = null
): Parcelable
