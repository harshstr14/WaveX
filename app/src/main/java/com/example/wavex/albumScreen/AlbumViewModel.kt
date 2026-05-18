package com.example.wavex.albumScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class AlbumViewModel(
    private val repository: AlbumRepository =
        AlbumRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "AlbumViewModel"
        const val BASE_URL = BuildConfig.YT_API_BASE_URL
    }

    private val _albums = MutableStateFlow(AlbumDetailUiState())

    val albums = _albums.asStateFlow()

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