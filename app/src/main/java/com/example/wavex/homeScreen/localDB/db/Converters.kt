package com.example.wavex.homeScreen.localDB.db

import androidx.room.TypeConverter
import com.example.wavex.songData.Album
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromArtistList(value: MutableList<Artists>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toArtistList(value: String): MutableList<Artists> {
        val type = object : TypeToken<MutableList<Artists>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromAlbum(album: Album?): String? {
        return gson.toJson(album)
    }

    @TypeConverter
    fun toAlbum(value: String?): Album? {
        return gson.fromJson(value, Album::class.java)
    }

    @TypeConverter
    fun fromImageList(value: MutableList<Image>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toImageList(value: String): MutableList<Image> {
        val type = object : TypeToken<MutableList<Image>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromDownloadList(value: MutableList<Download>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDownloadList(value: String): MutableList<Download> {
        val type = object : TypeToken<MutableList<Download>>() {}.type
        return gson.fromJson(value, type)
    }
}