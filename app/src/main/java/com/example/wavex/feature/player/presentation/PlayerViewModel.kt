package com.example.wavex.feature.player.presentation

import androidx.lifecycle.ViewModel
import com.example.wavex.feature.player.data.WaveformRepository
import com.example.wavex.feature.player.model.PlayerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val waveformRepository: WaveformRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    fun loadWaveform(songId: String?) {
        if (songId.isNullOrEmpty()) return

        if (_uiState.value.currentSongId == songId) return

        _uiState.update {
            it.copy(
                currentSongId = songId,
                amplitudes = waveformRepository.generateWaveform(songId)
            )
        }
    }
}