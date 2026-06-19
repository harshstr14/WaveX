package com.example.wavex.feature.artist.allalbums.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.artist.allalbums.data.AllAlbumsRepository
import com.example.wavex.feature.artist.allalbums.model.AllAlbumsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllAlbumsViewModel(
    private val repository: AllAlbumsRepository =
        AllAlbumsRepository()
) : ViewModel() {

    private var currentPage = 1
    private var currentArtistId: String? = null

    private val _albums = MutableStateFlow(AllAlbumsUiState())
    val albums = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchAlbumsByArtistID(
        artistId: String, root: String,
        page: Int = 1, isLoadMore: Boolean = false
    ) {
        viewModelScope.launch {
            if (!isLoadMore) {
                _isLoading.value = true
                _albums.value = AllAlbumsUiState()
                currentPage = 1
                currentArtistId = artistId
            }

            try {
                val response = repository.fetchAlbums(artistId, root, page)

                _albums.value = if (isLoadMore) {
                    _albums.value.copy(
                        albums = _albums.value.albums + response.albums
                    )
                } else {
                    AllAlbumsUiState(albums = response.albums)
                }

                currentPage = page
            } catch (e: Exception) {
                _albums.value = AllAlbumsUiState(
                    isError = true,
                    errorMessage = "Something went wrong"
                )
                Log.e("SAAVN", "Album fetch failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        val artistId = currentArtistId ?: return
        fetchAlbumsByArtistID(
            artistId = artistId,
            root = "albums",
            page = currentPage + 1,
            isLoadMore = true
        )
    }
}