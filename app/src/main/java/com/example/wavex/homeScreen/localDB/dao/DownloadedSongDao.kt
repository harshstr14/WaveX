package com.example.wavex.homeScreen.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wavex.homeScreen.localDB.entity.DownloadedSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSong(song: DownloadedSongEntity)

    @Query("SELECT * FROM downloaded_songs")
    fun getAllSongs(): Flow<List<DownloadedSongEntity>>

    @Query("DELETE FROM downloaded_songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("DELETE FROM downloaded_songs")
    suspend fun deleteAllSongs()

    @Query("SELECT * FROM downloaded_songs WHERE id = :id")
    suspend fun getSongById(id: String): DownloadedSongEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE id = :id)")
    fun isDownloaded(id: String): Flow<Boolean>
}