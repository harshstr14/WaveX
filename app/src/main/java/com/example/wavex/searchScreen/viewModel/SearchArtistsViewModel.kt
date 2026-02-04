package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musify.songData.Artists
import com.example.wavex.searchScreen.repository.SearchArtistsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchArtistsViewModel(
    private val repository: SearchArtistsRepository = SearchArtistsRepository()
) : ViewModel() {

    private val _artists = MutableStateFlow<List<Artists>>(emptyList())
    val artists = _artists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun fetchArtistsByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                _artists.value = repository.searchArtists(query, root)
            } catch (_: CancellationException) {
                // normal cancellation
            } catch (e: Exception) {
                Log.e("SAAVN", "Artist search failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _artists.value = emptyList()
        _isLoading.value = false
    }
}