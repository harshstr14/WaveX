package com.example.wavex.discoverScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.songData.Artists
import com.example.wavex.discoverScreen.repository.ExploreArtistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreArtistsViewModel(
    private val repository: ExploreArtistsRepository =
        ExploreArtistsRepository()
) : ViewModel() {

    private val _artists = MutableStateFlow<List<Artists>>(emptyList())
    val artists = _artists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchArtistsByQuery(query: String, root: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _artists.value =
                    repository.fetchArtists(query, root)
            } catch (e: Exception) {
                Log.e("SAAVN", "Artist fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}