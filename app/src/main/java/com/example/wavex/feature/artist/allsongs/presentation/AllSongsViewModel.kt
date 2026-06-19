package com.example.wavex.feature.artist.allsongs.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.artist.allsongs.data.AllSongsRepository
import com.example.wavex.feature.artist.allsongs.model.AllSongsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllSongsViewModel(
    private val repository: AllSongsRepository = AllSongsRepository()
) : ViewModel() {

    private var currentPage = 1
    private var currentArtistId: String? = null

    private val _songs = MutableStateFlow(AllSongsUiState())
    val songs = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchSongsByArtistID (
        artistId: String, root: String,
        page: Int = 1, isLoadMore: Boolean = false
    ) {
        viewModelScope.launch {
            if (!isLoadMore) {
                _isLoading.value = true
                _songs.value = AllSongsUiState()
                currentPage = 1
                currentArtistId = artistId
            }

            try {
                val response = repository.fetchSongsByArtistID(artistId, root, page)

                _songs.value = if (isLoadMore) {
                    _songs.value.copy(
                        songs = _songs.value.songs + response.songs
                    )
                } else {
                    AllSongsUiState(songs = response.songs)
                }

                currentPage = page
            } catch (e: Exception) {
                _songs.value = AllSongsUiState(
                    isError = true,
                    errorMessage = "Something went wrong"
                )
                Log.e("SAAVN", "Songs fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        val artistId = currentArtistId ?: return
        fetchSongsByArtistID(
            artistId = artistId,
            root = "songs",
            page = currentPage + 1,
            isLoadMore = true
        )
    }
}