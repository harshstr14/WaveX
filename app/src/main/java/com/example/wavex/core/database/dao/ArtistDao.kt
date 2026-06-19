package com.example.wavex.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wavex.core.database.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists")
    fun getArtists(): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(
        artists: List<ArtistEntity>
    )

    @Query("DELETE FROM artists")
    suspend fun clearArtists()

    @Query("SELECT EXISTS(SELECT 1 FROM artists LIMIT 1)")
    suspend fun hasData(): Boolean
}