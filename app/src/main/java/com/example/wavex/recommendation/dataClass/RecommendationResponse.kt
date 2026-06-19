package com.example.wavex.recommendation.dataClass

import com.example.wavex.core.model.SongItem

data class RecommendationResponse(
    val success: Boolean = false,
    val recommendations: List<SongItem> = emptyList()
)