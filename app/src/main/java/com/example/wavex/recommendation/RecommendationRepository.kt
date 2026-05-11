package com.example.wavex.recommendation

import com.example.wavex.homeScreen.SongItem
import com.example.wavex.recommendation.dataClass.PlayedSong

class RecommendationRepository {
    private val api =
        RecommendationApi()

    suspend fun getRecommendations(
        history: List<PlayedSong>
    ): List<SongItem> {
        return api.getRecommendations(history)
    }
}