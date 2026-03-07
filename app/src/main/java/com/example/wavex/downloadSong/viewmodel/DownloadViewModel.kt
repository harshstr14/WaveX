package com.example.wavex.downloadSong.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.downloadSong.data.DownloadedSong
import com.example.wavex.downloadSong.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val repository: DownloadRepository
) : ViewModel() {

    val downloadedSongs = repository.allDownloads

    val downloadedSongIds = repository.allDownloads
        .map { songs ->
            songs.map { it.id }.toSet()
        }

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

    fun isDownloaded(id: String): Flow<Boolean> {
        return repository.isDownloaded(id)
    }
}