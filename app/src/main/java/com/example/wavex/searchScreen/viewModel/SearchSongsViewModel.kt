package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.searchScreen.repository.SearchSongsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchSongsViewModel(
    private val repository: SearchSongsRepository = SearchSongsRepository()
) : ViewModel() {

    private val _songs = MutableStateFlow<List<SongItem>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun fetchSongByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                _songs.value = repository.searchSongs(query, root)
            } catch (_: CancellationException) {
                // expected cancellation
            } catch (e: Exception) {
                Log.e("SAAVN", "Song search failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _songs.value = emptyList()
        _isLoading.value = false
    }
}