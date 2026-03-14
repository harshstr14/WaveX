package com.example.wavex.profileScreen.artistsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FavouriteArtistViewModel(
    private val repository: FavouriteArtistRepository = FavouriteArtistRepository()
) : ViewModel() {

    private val _artists = MutableStateFlow(FavouriteArtistUiState())
    val artists: StateFlow<FavouriteArtistUiState> = _artists

    init {
        observeArtists()
    }

    private fun observeArtists() {
        viewModelScope.launch {

            repository.observeFavouriteArtists()
                .onStart {
                    _artists.value = _artists.value.copy(
                        isLoading = true,
                        isError = false
                    )
                }
                .catch { e ->
                    _artists.value = _artists.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
                .collect { artists ->
                    _artists.value = _artists.value.copy(
                        artists = artists,
                        isLoading = false,
                        isError = false
                    )
                }
        }
    }

    fun deleteAllArtists() {
        viewModelScope.launch {
            try {
                repository.deleteAllArtists()
            } catch (e: Exception) {
                _artists.value = _artists.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Failed to delete artists"
                )
            }
        }
    }
}