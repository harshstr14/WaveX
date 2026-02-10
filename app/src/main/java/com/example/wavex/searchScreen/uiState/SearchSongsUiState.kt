package com.example.wavex.searchScreen.uiState

sealed class SearchSongsUiState {
    object Idle : SearchSongsUiState()
    object Loading : SearchSongsUiState()
    object Empty : SearchSongsUiState()
    data class Error(val message: String) : SearchSongsUiState()
    object Success : SearchSongsUiState()
}