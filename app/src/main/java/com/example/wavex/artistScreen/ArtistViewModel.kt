package com.example.wavex.artistScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ArtistViewModel(
    private val repository: ArtistRepository = ArtistRepository()
) : ViewModel() {

    private val _artists = MutableStateFlow(ArtistDetailUiState())
    val artists = _artists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _artists.value = repository.fetchArtistById(artistId)
            } catch (e: Exception) {
                Log.e("SAAVN", "Artist load failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}