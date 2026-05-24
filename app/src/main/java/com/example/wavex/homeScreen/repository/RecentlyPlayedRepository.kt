package com.example.wavex.homeScreen.repository

import com.example.wavex.homeScreen.localDB.dao.RecentlyPlayedDao
import com.example.wavex.homeScreen.localDB.entity.RecentlyPlayedEntity
import javax.inject.Inject

class RecentlyPlayedRepository @Inject constructor(
    private val dao: RecentlyPlayedDao
) {
    companion object {
        private const val MAX_SIZE = 24
    }

    fun getRecentlyPlayed() = dao.getRecentlyPlayed()

    suspend fun addSong(song: RecentlyPlayedEntity) {
        val existing = dao.getSong(song.id)
        val now = System.currentTimeMillis()

        if (existing != null) {
            dao.updateTime(song.id, now)
        } else {
            dao.insert(song.copy(playedAt = now))
        }

        val currentList = dao.getRecentlyPlayedOnce()
        val extra = currentList.size - MAX_SIZE

        if (extra > 0) {
            dao.deleteOldest(extra)
        }
    }

    suspend fun clearRecentlyPlayed() {
        dao.deleteAll()
    }
}