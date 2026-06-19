package com.example.wavex.feature.importplaylist.data

sealed class ImportResult {
    data class Success(val message: String) : ImportResult()
    data class Error(val message: String) : ImportResult()
}