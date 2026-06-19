package com.example.wavex.core.util

import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Quality

object DownloadQualitySelector {
    private val downloadFallbackOrder =
        mapOf(
            AudioStreamQualityPreference.LOW to listOf(
                Quality.LOW,
                Quality.MEDIUM,
                Quality.HIGH,
                Quality.LOSSLESS
            ),

            AudioStreamQualityPreference.MEDIUM to listOf(
                Quality.MEDIUM,
                Quality.HIGH,
                Quality.LOW,
                Quality.LOSSLESS
            ),

            AudioStreamQualityPreference.HIGH to listOf(
                Quality.HIGH,
                Quality.LOSSLESS,
                Quality.MEDIUM,
                Quality.LOW
            )
        )

    fun selectDownload(
        downloads: List<Download>,
        preference: AudioStreamQualityPreference
    ): Download? {

        val fallbackOrder = downloadFallbackOrder[preference] ?: emptyList()

        for (quality in fallbackOrder) {
            downloads.lastOrNull {
                it.quality == quality
            }?.let {
                return it
            }
        }

        return downloads.lastOrNull()
    }
}