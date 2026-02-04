package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.searchScreen.repository.SearchAlbumsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchAlbumsViewModel(
    private val repository: SearchAlbumsRepository =
        SearchAlbumsRepository()
) : ViewModel() {

    private val _albums = MutableStateFlow<List<DataItem>>(emptyList())
    val albums = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private var searchJob: Job? = null

    fun fetchAlbumByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            _albums.value = emptyList()
            _isLoading.value = false
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                _albums.value =
                    repository.searchAlbums(query, root)
            } catch (_: CancellationException) {
                // expected — ignore
            } catch (e: Exception) {
                Log.e("SAAVN", "Album search failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        searchJob?.cancel()
        _albums.value = emptyList()
        _isLoading.value = false
    }
}