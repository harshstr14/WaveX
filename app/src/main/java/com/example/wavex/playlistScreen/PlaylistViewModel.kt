package com.example.wavex.playlistScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.searchScreen.viewModel.SearchSongsViewModel.Companion.BASE_URL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val repository: PlaylistRepository = PlaylistRepository()
) : ViewModel() {

    private val _playlists = MutableStateFlow(PlaylistDetailUiState())
    val playlists = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _playlists.value = repository.fetchPlaylistById(playlistId)
            } catch (e: Exception) {
                _playlists.value = PlaylistDetailUiState(
                    isError = true,
                    errorMessage = "Something went wrong"
                )
                Log.e("SAAVN", "Playlist load failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadYTPlaylist(playlistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _playlists.value = repository.fetchYTMusicPlaylist(playlistId = playlistId, baseUrl = BASE_URL)
            } catch (e: Exception) {
                _playlists.value = PlaylistDetailUiState(
                    isError = true,
                    errorMessage = "Something went wrong"
                )
                Log.e("SAAVN", "Artist load failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}