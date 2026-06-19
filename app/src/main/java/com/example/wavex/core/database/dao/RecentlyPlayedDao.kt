package com.example.wavex.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wavex.core.database.entity.RecentlyPlayedEntity
import com.example.wavex.core.model.Download
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {
    @Query("""
        SELECT * FROM recently_played_songs
        ORDER BY playedAt DESC
        LIMIT 24
    """)
    fun getRecentlyPlayed(): Flow<List<RecentlyPlayedEntity>>

    @Query("SELECT * FROM recently_played_songs WHERE id = :songId LIMIT 1")
    suspend fun getSong(songId: String): RecentlyPlayedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: RecentlyPlayedEntity)

    @Query("""
        UPDATE recently_played_songs
        SET downloadUrl = :downloadUrl,
            duration = :duration,
            playedAt = :time
        WHERE id = :songId
    """)
    suspend fun updateStreamData(songId: String, downloadUrl: MutableList<Download>, duration: Int, time: Long)

    @Query("""
        DELETE FROM recently_played_songs
        WHERE id IN (
            SELECT id FROM recently_played_songs
            ORDER BY playedAt ASC
            LIMIT :extraCount
        )
    """)
    suspend fun deleteOldest(extraCount: Int)

    @Query("""
    SELECT * FROM recently_played_songs
    ORDER BY playedAt DESC
""")
    suspend fun getRecentlyPlayedOnce(): List<RecentlyPlayedEntity>

    @Query("DELETE FROM recently_played_songs")
    suspend fun deleteAll()
}