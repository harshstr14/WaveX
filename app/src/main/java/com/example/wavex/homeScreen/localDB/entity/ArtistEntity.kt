package com.example.wavex.homeScreen.localDB.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey
    val id: String,

    val name: String,
    val image: String,
    val type: String,
    val searchSource: String
)