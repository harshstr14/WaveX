package com.example.wavex.profileScreen.playlistsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FavouriteViewModel(
    private val repository: FavouritePlaylistRepository = FavouritePlaylistRepository()
) : ViewModel() {

    private val _playlists = MutableStateFlow(FavouriteUiState())
    val playlists: StateFlow<FavouriteUiState> = _playlists

    init {
        observeFavourites()
    }

    private fun observeFavourites() {
        viewModelScope.launch {

            repository.observeFavouritePlaylists()
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
                .collect { list ->
                    _playlists.value = _playlists.value.copy(
                        playlists = list,
                        isLoading = false,
                        isError = false
                    )
                }
        }
    }

    fun deleteAllPlaylists() {
        viewModelScope.launch {
            try {
                repository.deleteAllPlaylists()
            } catch (e: Exception) {
                _playlists.value = _playlists.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Failed to delete playlists"
                )
            }
        }
    }
}