package com.example.wavex.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,

    val title: String,
    val artist: String,
    val image: String,
    val audioUrl: String,
    val duration: Long
)