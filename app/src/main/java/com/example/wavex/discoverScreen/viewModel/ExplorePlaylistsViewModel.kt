package com.example.wavex.discoverScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.discoverScreen.repository.ExplorePlaylistsRepository
import com.example.wavex.homeScreen.DataItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExplorePlaylistsViewModel(
    private val repository: ExplorePlaylistsRepository =
        ExplorePlaylistsRepository()
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<DataItem>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchPlayListByQuery(query: String, root: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _playlists.value =
                    repository.fetchPlaylists(query, root)
            } catch (e: Exception) {
                Log.e("SAAVN", "Playlist fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}