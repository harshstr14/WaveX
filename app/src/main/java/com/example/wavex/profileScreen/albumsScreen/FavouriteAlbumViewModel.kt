package com.example.wavex.profileScreen.albumsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FavouriteAlbumViewModel(
    private val repository: FavouriteAlbumRepository = FavouriteAlbumRepository()
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
}