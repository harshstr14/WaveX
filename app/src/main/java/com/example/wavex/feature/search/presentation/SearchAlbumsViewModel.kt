package com.example.wavex.feature.search.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import com.example.wavex.core.model.DataItem
import com.example.wavex.feature.search.data.SearchAlbumsRepository
import com.example.wavex.feature.search.model.SearchAlbumsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchAlbumsViewModel(
    private val repository: SearchAlbumsRepository = SearchAlbumsRepository()
) : ViewModel() {
    companion object {
        private const val TAG = "SearchAlbumsViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    private val _albums = MutableStateFlow<List<DataItem>>(emptyList())

    val albums = _albums.asStateFlow()

    private val _uiState =
        MutableStateFlow<SearchAlbumsUiState>(
            SearchAlbumsUiState.Idle
        )

    val uiState = _uiState.asStateFlow()

    fun fetchAlbumByQuery(query: String) {
        if (!validateQuery(query)) {
            return
        }

        launchSearch("SAAVN") {
            repository.searchAlbums(query)
        }
    }

    fun fetchYTMusicAlbums(query: String) {
        if (!validateQuery(query)) {
            return
        }

        launchSearch("YT_MUSIC") {
            repository.ytMusicSearch(
                query = query,
                baseUrl = BASE_URL
            )
        }
    }

    private fun launchSearch(
        tag: String,
        block: suspend () -> List<DataItem>
    ) {
        viewModelScope.launch {
            _uiState.value =
                SearchAlbumsUiState.Loading

            try {
                val result = block()

                _albums.value = result

                _uiState.value =
                    if (result.isEmpty()) {
                        SearchAlbumsUiState.Empty
                    } else {
                        SearchAlbumsUiState.Success
                    }

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _uiState.value =
                    SearchAlbumsUiState.Error(
                        "Something went wrong"
                    )

                Log.e(tag, "Album search failed", e)
            }
        }
    }

    private fun validateQuery(query: String): Boolean {
        if (query.length < 2) {
            clearResults()

            _uiState.value = SearchAlbumsUiState.Idle

            return false
        }
        return true
    }

    fun clearResults() {
        _albums.value = emptyList()
    }

    fun setIdle() {
        _uiState.value =
            SearchAlbumsUiState.Idle
    }
}