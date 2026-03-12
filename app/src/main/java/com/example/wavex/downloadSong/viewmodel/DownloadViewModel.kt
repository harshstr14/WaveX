package com.example.wavex.downloadSong.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.downloadSong.data.DownloadedSong
import com.example.wavex.downloadSong.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

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
            val song = repository.getSongById(id)

            song?.let {
                val file = File(it.localPath)

                if (file.exists()) {
                    file.delete()
                }
            }

            repository.delete(id)
        }
    }

    fun deleteAllSongs() {
        viewModelScope.launch {
            val songs = downloadedSongs.first()

            songs.forEach { song ->
                val file = File(song.localPath)
                if (file.exists()) {
                    file.delete()
                }
            }

            repository.deleteAll()
        }
    }

    fun isDownloaded(id: String): Flow<Boolean> {
        return repository.isDownloaded(id)
    }
}