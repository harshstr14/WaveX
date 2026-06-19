package com.example.wavex.feature.library.presentation.favourite.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.library.presentation.favourite.data.FavouriteSongRepository
import com.example.wavex.feature.library.presentation.favourite.model.FavouriteSongUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteSongViewModel @Inject constructor(
    private val repository: FavouriteSongRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavouriteSongUiState())
    val uiState = _uiState.asStateFlow()

    val totalDuration: StateFlow<Int> =
        uiState
            .map { state ->
                state.songs.sumOf { song ->
                    song.duration
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    init {
        observeSongs()
    }

    private fun observeSongs() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            repository.observeFavouriteSongs()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = e.message
                        )
                    }
                }
                .collect { songs ->
                    _uiState.update {
                        it.copy(
                            songs = songs,
                            isLoading = false,
                            isError = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun removeSong(
        songId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.removeSong(songId)

                onResult(
                    true,
                    "Song removed from favourites"
                )
            } catch (e: Exception) {
                onResult(
                    false,
                    e.message ?: "Failed to remove song"
                )
            }
        }
    }
}