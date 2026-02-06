package com.example.wavex.artistScreen.allSongsScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllSongsViewModel(
    private val repository: AllSongsRepository = AllSongsRepository()
) : ViewModel() {

    private val _songs = MutableStateFlow(AllSongsUiState())
    val songs = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchSongsByArtistID(artistId: String, root: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _songs.value =
                    repository.fetchSongsByArtistID(artistId, root)
            } catch (e: Exception) {
                _songs.value = AllSongsUiState(
                    isError = true,
                    errorMessage = "Something went wrong"
                )
                Log.e("SAAVN", "Songs fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}