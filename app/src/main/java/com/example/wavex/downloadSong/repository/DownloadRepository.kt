package com.example.wavex.downloadSong.repository

import com.example.wavex.downloadSong.data.DownloadDao
import com.example.wavex.downloadSong.data.DownloadedSong

class DownloadRepository(
    private val dao: DownloadDao
) {
    val allDownloads = dao.getAllSongs()

    suspend fun insert(song: DownloadedSong) {
        dao.insertSong(song)
    }

    suspend fun delete(id: String) {
        dao.deleteSong(id)
    }

    suspend fun isDownloaded(id: String): Boolean {
        return dao.isDownloaded(id)
    }
}