package com.example.wavex.albumScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val repository: AlbumRepository = AlbumRepository()
) : ViewModel() {

    private val _albums = MutableStateFlow(AlbumDetailUiState())
    val albums = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _albums.value = repository.fetchAlbumById(albumId)
            } catch (e: Exception) {
                _albums.value = AlbumDetailUiState(
                    isError = true,
                    errorMessage = "Something went wrong"
                )
                Log.e("SAAVN", "Album load failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}