package com.example.wavex.feature.library.sheets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.feature.library.sheets.data.PlaylistEditorRepository
import com.example.wavex.feature.library.sheets.model.PlaylistEditorState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlaylistEditorViewModel @Inject constructor(
    private val repository: PlaylistEditorRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PlaylistEditorState>(PlaylistEditorState.Idle)

    val state = _state.asStateFlow()

    fun createPlaylist(title: String, description: String) {
        viewModelScope.launch {
            _state.value = PlaylistEditorState.Loading

            repository.createPlaylist(title = title, description = description).fold(
                onSuccess = {
                    _state.value =
                        PlaylistEditorState.Success(
                            "Playlist Created Successfully"
                        )
                },
                onFailure = {
                    _state.value =
                        PlaylistEditorState.Error(
                            it.message ?: "Something went wrong"
                        )
                }
            )
        }
    }

    fun renamePlaylist(playlistId: String, title: String, description: String) {
        viewModelScope.launch {
            _state.value = PlaylistEditorState.Loading

            repository.renamePlaylist(playlistId, title, description).fold(
                onSuccess = {
                    _state.value =
                        PlaylistEditorState.Success(
                            "Playlist Renamed Successfully"
                        )
                },
                onFailure = {
                    _state.value =
                        PlaylistEditorState.Error(
                            it.message ?: "Something went wrong"
                        )
                }
            )
        }
    }

    fun resetState() {
        _state.value = PlaylistEditorState.Idle
    }
}