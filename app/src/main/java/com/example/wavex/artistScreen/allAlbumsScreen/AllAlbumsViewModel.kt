package com.example.wavex.artistScreen.allAlbumsScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllAlbumsViewModel(
    private val repository: AllAlbumsRepository =
        AllAlbumsRepository()
) : ViewModel() {

    private val _albums = MutableStateFlow(AllAlbumsUiState())
    val albums = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchAlbumsByArtistID(query: String, root: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _albums.value =
                    repository.fetchAlbums(query, root)
            } catch (e: Exception) {
                _albums.value = AllAlbumsUiState(
                    isError = true,
                    errorMessage = "Something went wrong"
                )
                Log.e("SAAVN", "Album fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}