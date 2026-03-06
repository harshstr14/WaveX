package com.example.wavex.downloadSong.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.downloadSong.data.DownloadedSong
import com.example.wavex.downloadSong.repository.DownloadRepository
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val repository: DownloadRepository
) : ViewModel() {

    val downloadedSongs = repository.allDownloads

    fun insertSong(song: DownloadedSong) {
        viewModelScope.launch {
            repository.insert(song)
        }
    }

    fun deleteSong(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}