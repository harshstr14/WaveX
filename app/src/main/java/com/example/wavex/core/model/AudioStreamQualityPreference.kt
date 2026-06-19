package com.example.wavex.core.model

enum class AudioStreamQualityPreference(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    companion object {
        fun fromLabel(value: String?): AudioStreamQualityPreference {
            return when (
                value?.trim()?.lowercase()
            ) {
                "low" -> LOW
                "high" -> HIGH
                else -> MEDIUM
            }
        }
    }
}