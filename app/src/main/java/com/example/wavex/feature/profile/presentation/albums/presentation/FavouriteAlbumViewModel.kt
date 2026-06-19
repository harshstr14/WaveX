package com.example.wavex.feature.profile.presentation.albums.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.profile.presentation.albums.data.FavouriteAlbumRepository
import com.example.wavex.feature.profile.presentation.albums.model.FavouriteAlbumUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class FavouriteAlbumViewModel @Inject constructor(
    private val repository: FavouriteAlbumRepository
) : ViewModel() {
    private val _albums = MutableStateFlow(FavouriteAlbumUiState())
    val albums: StateFlow<FavouriteAlbumUiState> = _albums

    init {
        observeAlbums()
    }

    private fun observeAlbums() {
        viewModelScope.launch {
            repository.observeFavouriteAlbums()
                .onStart {
                    _albums.value = _albums.value.copy(
                        isLoading = true,
                        isError = false
                    )
                }
                .catch { e ->
                    _albums.value = _albums.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
                .collect { albums ->
                    _albums.value = _albums.value.copy(
                        albums = albums,
                        isLoading = false,
                        isError = false
                    )
                }
        }
    }

    fun deleteAllAlbums() {
        viewModelScope.launch {
            try {
                repository.deleteAllAlbums()
            } catch (e: Exception) {
                _albums.value = _albums.value.copy(
                    isError = true,
                    errorMessage = e.message ?: "Failed to delete albums"
                )
            }
        }
    }
}