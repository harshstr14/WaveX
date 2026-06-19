package com.example.wavex.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.core.model.SongItem
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel : ViewModel() {
    @Inject
    lateinit var historyRepository: MusicHistoryRepository

    private val recommendationRepository =
        RecommendationRepository()

    private val _recommendedSongs =
        MutableStateFlow<List<SongItem>>(emptyList())

    val recommendedSongs:
            StateFlow<List<SongItem>>
            = _recommendedSongs

    fun loadRecommendations() {
        historyRepository.getUserHistory { history ->

            viewModelScope.launch {
                val recommendations =
                    recommendationRepository
                        .getRecommendations(history)

                _recommendedSongs.value =
                    recommendations
            }
        }
    }
}