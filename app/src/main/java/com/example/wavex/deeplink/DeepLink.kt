package com.example.wavex.deeplink

sealed class DeepLink {
    data class Song(val id: String) : DeepLink()
    data class Album(val id: String, val source: String?) : DeepLink()
    data class Playlist(val id: String, val source: String?) : DeepLink()
    data class Artist(val id: String, val source: String?) : DeepLink()
    data class Library(val url: String) : DeepLink()
    data object Unknown : DeepLink()
}