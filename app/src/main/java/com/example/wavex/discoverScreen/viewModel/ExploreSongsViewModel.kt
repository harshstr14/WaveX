package com.example.wavex.discoverScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.discoverScreen.repository.ExploreSongsRepository
import com.example.wavex.homeScreen.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreSongsViewModel(
    private val repository: ExploreSongsRepository =
        ExploreSongsRepository()
) : ViewModel() {

    private val _songs = MutableStateFlow<List<SongItem>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchPlaylistsByID(playListId: String, root: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _songs.value =
                    repository.fetchSongsByPlaylist(playListId, root)
            } catch (e: Exception) {
                Log.e("SAAVN", "Songs fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}