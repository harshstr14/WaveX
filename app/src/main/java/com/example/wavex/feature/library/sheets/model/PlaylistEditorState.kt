package com.example.wavex.feature.library.sheets.model

sealed class PlaylistEditorState {
    data object Idle : PlaylistEditorState()
    data object Loading : PlaylistEditorState()
    data class Success(val message: String) : PlaylistEditorState()
    data class Error(val message: String) : PlaylistEditorState()
}