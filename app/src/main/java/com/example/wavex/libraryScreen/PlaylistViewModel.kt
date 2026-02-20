package com.example.wavex.libraryScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val repository: PlaylistRepository = PlaylistRepository()
): ViewModel() {
    private val _playlists = MutableStateFlow(PlaylistDetailUiState())
    val playlists: StateFlow<PlaylistDetailUiState> = _playlists

    init {
        observePlaylists()
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            repository.observePlaylists()
                .onStart {
                    _playlists.value = _playlists.value.copy(
                        isLoading = true,
                        isError = false
                    )
                }
                .catch { e ->
                    _playlists.value = _playlists.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
                .collect { playlists ->
                    _playlists.value = _playlists.value.copy(
                        playlists = playlists,
                        isLoading = false,
                        isError = false
                    )
                }
        }
    }

    fun deletePlaylist(
        playlistId: String,
        onResult: (Boolean) -> Unit
    ) {
        repository.deletePlaylist(playlistId, onResult)
    }

    fun addSongToPlaylist(
        playlistId: String,
        song: SongItem,
        onResult: (Boolean, String) -> Unit
    ) {
        repository.addSongToPlaylist(playlistId, song, onResult)
    }
}