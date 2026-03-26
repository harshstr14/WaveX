package com.example.wavex.libraryScreen.playlistScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val repository: PlaylistRepository = PlaylistRepository()
): ViewModel() {
    private val _playlist = MutableStateFlow(PlaylistDetailUiState())
    val playlist: StateFlow<PlaylistDetailUiState> = _playlist

    fun observePlaylists(playlistId: String) {
        viewModelScope.launch {
            repository.observePlaylistById(playlistId)
                .onStart {
                    _playlist.value = _playlist.value.copy(
                        isLoading = true,
                        isError = false
                    )
                }
                .catch { e ->
                    _playlist.value = _playlist.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
                .collect { playlists ->
                    _playlist.value = _playlist.value.copy(
                        playlist = playlists,
                        isLoading = false,
                        isError = false
                    )
                }
        }
    }

    fun removeSong(
        playlistId: String,
        songId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        repository.removeSongFromPlaylist(playlistId, songId, onResult)
    }
}