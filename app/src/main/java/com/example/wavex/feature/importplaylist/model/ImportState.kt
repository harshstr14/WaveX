package com.example.wavex.feature.importplaylist.model

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Success(val message: String) : ImportState()
    data class Error(val message: String) : ImportState()
}