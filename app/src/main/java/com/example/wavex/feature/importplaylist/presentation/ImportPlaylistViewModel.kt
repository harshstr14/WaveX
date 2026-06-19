package com.example.wavex.feature.importplaylist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.importplaylist.data.ImportPlaylistRepository
import com.example.wavex.feature.importplaylist.data.ImportResult
import com.example.wavex.feature.importplaylist.model.ImportState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

@HiltViewModel
class ImportPlaylistViewModel @Inject constructor(
    private val repository: ImportPlaylistRepository
) : ViewModel() {
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    fun importWaveXPlaylist(apiUrl: String, playlistId: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading

            try {
                when (
                    val result = repository.importWaveXPlaylist(apiUrl, playlistId)
                ) {
                    is ImportResult.Success -> {
                        _importState.value =
                            ImportState.Success(
                                result.message
                            )
                    }

                    is ImportResult.Error -> {
                        _importState.value =
                            ImportState.Error(
                                result.message
                            )
                    }
                }
            } catch (_: SocketTimeoutException) {
                _importState.value = ImportState.Error("Request timed out")
            } catch (_: IOException) {
                _importState.value = ImportState.Error("Playlist not available")
            } catch (_: Exception) {
                _importState.value = ImportState.Error("Something went wrong")
            }
        }
    }

    fun importSpotifyPlaylist(apiUrl: String, url: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading

            try {
                when (
                    val result = repository.importSpotifyPlaylist(apiUrl, url)
                ) {
                    is ImportResult.Success -> {
                        _importState.value =
                            ImportState.Success(
                                result.message
                            )
                    }

                    is ImportResult.Error -> {
                        _importState.value =
                            ImportState.Error(
                                result.message
                            )
                    }
                }
            } catch (_: Exception) {
                _importState.value = ImportState.Error("Something went wrong")
            }
        }
    }

    fun cancelImport() {
        _importState.value = ImportState.Idle
    }
}