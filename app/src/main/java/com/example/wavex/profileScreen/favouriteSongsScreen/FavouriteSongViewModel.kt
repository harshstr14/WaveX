package com.example.wavex.profileScreen.favouriteSongsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavouriteSongViewModel(
    private val repository: FavouriteSongRepository = FavouriteSongRepository()
): ViewModel() {
    private val _songs = MutableStateFlow(FavouriteSongUiState())
    val songs: StateFlow<FavouriteSongUiState> = _songs

    val totalDuration: StateFlow<Int> =
        songs.map { uiState ->
            uiState.songs.sumOf { it.duration }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    init {
        observeSongs()
    }

    private fun observeSongs() {
        viewModelScope.launch {

            repository.observeFavouriteSongs()
                .onStart {
                    _songs.value = _songs.value.copy(
                        isLoading = true,
                        isError = false
                    )
                }
                .catch { e ->
                    _songs.value = _songs.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
                .collect { songs ->
                    _songs.value = _songs.value.copy(
                        songs = songs,
                        isLoading = false,
                        isError = false
                    )
                }
        }
    }

    fun deleteAllSongs() {
        viewModelScope.launch {
            try {
                repository.deleteAllSongs()
            } catch (e: Exception) {
                _songs.value = _songs.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Failed to delete songs"
                )
            }
        }
    }
}