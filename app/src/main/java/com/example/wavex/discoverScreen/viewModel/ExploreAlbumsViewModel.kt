package com.example.wavex.discoverScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.discoverScreen.repository.ExploreAlbumsRepository
import com.example.wavex.homeScreen.DataItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreAlbumsViewModel(
    private val repository: ExploreAlbumsRepository =
        ExploreAlbumsRepository()
) : ViewModel() {

    private val _albums = MutableStateFlow<List<DataItem>>(emptyList())
    val albums = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchAlbumByQuery(query: String, root: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _albums.value =
                    repository.fetchAlbums(query, root)
            } catch (e: Exception) {
                Log.e("SAAVN", "Album fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}