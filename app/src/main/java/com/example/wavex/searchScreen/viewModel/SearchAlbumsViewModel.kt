package com.example.wavex.searchScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.searchScreen.repository.SearchAlbumsRepository
import com.example.wavex.searchScreen.uiState.SearchAlbumsUiState
import com.example.wavex.searchScreen.uiState.SearchSongsUiState
import com.example.wavex.searchScreen.viewModel.SearchSongsViewModel.Companion.BASE_URL
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchAlbumsViewModel(
    private val repository: SearchAlbumsRepository = SearchAlbumsRepository()
) : ViewModel() {

    private val _albums = MutableStateFlow<List<DataItem>>(emptyList())
    val albums = _albums.asStateFlow()

    private val _ytMusicAlbums = MutableStateFlow<List<DataItem>>(emptyList())
    val ytMusicAlbums = _ytMusicAlbums.asStateFlow()

    private val _uiState =
        MutableStateFlow<SearchAlbumsUiState>(SearchAlbumsUiState.Idle)
    val uiState = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun fetchAlbumByQuery(query: String, root: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            _uiState.value = SearchAlbumsUiState.Idle
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _uiState.value = SearchAlbumsUiState.Loading
            try {
                val result = repository.searchAlbums(query, root)
                _albums.value = result

                _uiState.value =
                    if (result.isEmpty()) SearchAlbumsUiState.Empty
                    else SearchAlbumsUiState.Success

            } catch (_: CancellationException) {
            } catch (e: Exception) {
                _uiState.value =
                    SearchAlbumsUiState.Error("Something went wrong")
                Log.e("SAAVN", "Album search failed", e)
            }
        }
    }

    fun fetchYTMusicAlbums(query: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            clearResults()
            _uiState.value = SearchAlbumsUiState.Idle
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _uiState.value = SearchAlbumsUiState.Loading

            try {
                val result = repository.ytMusicSearch(
                    query = query,
                    baseUrl = BASE_URL
                )

                _ytMusicAlbums.value = result

                _uiState.value =
                    if (result.isEmpty())
                        SearchAlbumsUiState.Empty
                    else
                        SearchAlbumsUiState.Success

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _uiState.value =
                    SearchAlbumsUiState.Error("Something went wrong")

                Log.e("YT_MUSIC", "YT Music search failed", e)
            }
        }
    }

    fun clearResults() {
        _albums.value = emptyList()
    }

    fun setIdle() {
        _uiState.value = SearchAlbumsUiState.Idle
    }
}