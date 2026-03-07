package com.example.wavex.downloadSong.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wavex.downloadSong.repository.DownloadRepository

class DownloadViewModelFactory(
    private val repository: DownloadRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DownloadViewModel(repository) as T
    }
}