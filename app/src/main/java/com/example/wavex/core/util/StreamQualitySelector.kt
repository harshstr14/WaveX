package com.example.wavex.core.util

import androidx.core.net.toUri
import com.example.wavex.core.model.AudioStreamQualityPreference
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Quality
import timber.log.Timber

object StreamQualitySelector {
    private val playbackFallbackOrder =
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

    fun selectPlaybackStream(
        streams: List<Download>,
        preference: AudioStreamQualityPreference
    ): Download? {
        Timber.tag("PLAY_FLOW").d("Preferred Quality = $preference")

        val usable = streams.filter { isUsable(it) }

        Timber.tag("PLAY_FLOW").d("Usable Streams = ${usable.size}")

        usable.forEach {
            Timber.tag("PLAY_FLOW").d(
                """
            Quality : ${it.quality}
            Expire  : ${it.expiresAt}
            """.trimIndent()
            )
        }

        if (usable.isEmpty()) {
            Timber.tag("PLAY_FLOW").e("No usable streams found")
            return null
        }

        val fallbackOrder = playbackFallbackOrder[preference] ?: emptyList()

        for (quality in fallbackOrder) {
            usable.lastOrNull {
                it.quality == quality
            }?.let {
                Timber.tag("PLAY_FLOW").d(
                    """
                Selected Stream
                Quality : ${it.quality}
                """.trimIndent()
                )

                return it
            }
        }

        return usable.lastOrNull()
    }

    private fun isUsable(stream: Download): Boolean {
        val url = stream.url.trim()

        if (url.isEmpty()) {
            return false
        }

        val uri = url.toUri()
        val scheme = uri.scheme ?: return false

        if (
            scheme != "http" &&
            scheme != "https" &&
            scheme != "file"
        ) {
            return false
        }

        val expiresAt = stream.expiresAt ?: return true
        val now = System.currentTimeMillis() / 1000

        return expiresAt > now
    }
}