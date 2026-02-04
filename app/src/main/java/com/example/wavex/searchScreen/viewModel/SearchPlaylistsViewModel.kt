package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.searchScreen.repository.SearchPlaylistsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchPlaylistsViewModel(
    private val repository: SearchPlaylistsRepository = SearchPlaylistsRepository()
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<DataItem>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun fetchPlayListByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                _playlists.value = repository.searchPlaylists(query, root)
            } catch (_: CancellationException) {
                // expected cancellation
            } catch (e: Exception) {
                Log.e("SAAVN", "Playlist search failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _playlists.value = emptyList()
        _isLoading.value = false
    }
}