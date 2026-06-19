package com.example.wavex.feature.album.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import com.example.wavex.feature.album.data.AlbumRepository
import com.example.wavex.feature.album.model.AlbumDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: AlbumRepository
) : ViewModel() {
    companion object {
        private const val TAG = "AlbumViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    private val _albums = MutableStateFlow(AlbumDetailUiState())

    val albums = _albums.asStateFlow()

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite = _isFavourite.asStateFlow()

    fun checkFavourite(albumId: String) {
        viewModelScope.launch {
            try {
                _isFavourite.value =
                    repository.isFavouriteAlbum(albumId)
            } catch (_: Exception) {

            }
        }
    }

    fun toggleFavourite(
        albumId: String,
        albumName: String,
        imageUrl: String,
        primaryArtists: String,
        source: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (_isFavourite.value) {
                    repository.removeFromFavourite(albumId)

                    _isFavourite.value = false

                    onResult("Removed From Favourite")
                } else {
                    repository.addToFavourite(
                        albumId = albumId,
                        albumName = albumName,
                        imageUrl = imageUrl,
                        primaryArtists = primaryArtists,
                        source = source
                    )

                    _isFavourite.value = true

                    onResult("Added To Favourite")
                }
            } catch (e: Exception) {
                onResult(e.message ?: "Something went wrong")
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)

    val isLoading = _isLoading.asStateFlow()

    private var loadJob: Job? = null

    fun loadAlbum(albumId: String) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _isLoading.value = true

            try {
                _albums.value =
                    repository.fetchAlbumById(
                        albumId
                    )

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _albums.value =
                    AlbumDetailUiState(
                        isError = true,
                        errorMessage =
                            "Something went wrong"
                    )

                Log.e(TAG,"Album load failed",e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadYTAlbum(albumId: String) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _isLoading.value = true

            try {
                _albums.value =
                    repository.fetchYTMusicAlbum(
                        albumId = albumId,
                        baseUrl = BASE_URL
                    )

            } catch (_: CancellationException) {

            } catch (e: Exception) {
                _albums.value =
                    AlbumDetailUiState(
                        isError = true,
                        errorMessage =
                            "Something went wrong"
                    )

                Log.e(TAG,"YT Album load failed",e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}