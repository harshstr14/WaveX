package com.example.wavex.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.core.model.SongItem
import com.example.wavex.feature.library.data.LibraryRepository
import com.example.wavex.feature.library.model.LibraryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository
): ViewModel() {
    private val _playlists = MutableStateFlow(LibraryUiState())
    val playlists: StateFlow<LibraryUiState> = _playlists

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