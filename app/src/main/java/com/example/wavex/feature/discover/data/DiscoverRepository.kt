package com.example.wavex.feature.discover.data

import com.example.wavex.core.model.Album
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.DataItem
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Image
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.network.requestWithFallback
import com.example.wavex.core.model.Quality
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DiscoverRepository @Inject constructor() {
    suspend fun fetchAlbums(query: String,root: String):
            List<DataItem> = withContext(Dispatchers.IO)
    {
        val response = requestWithFallback("/search/albums?query=$query&limit=30")

        if (response.isEmpty()) return@withContext emptyList()

        parseAlbumListJson(response, root)
    }

    suspend fun fetchArtists(query: String,root: String):
            List<Artists> = withContext(Dispatchers.IO)
    {
        val response = requestWithFallback("/search/artists?query=$query&limit=20")

        if (response.isEmpty()) return@withContext emptyList()

        parseArtistsJson(response, root)
    }

    suspend fun fetchPlaylists(query: String,root: String):
            List<DataItem> = withContext(Dispatchers.IO)
    {
        val response = requestWithFallback("/search/playlists?query=$query&limit=20")

        if (response.isEmpty()) return@withContext emptyList()

        parsePlaylistJson(response, root)
    }

    suspend fun fetchSongsByPlaylist(playlistId: String,root: String):
            List<SongItem> = withContext(Dispatchers.IO)
    {
        val response = requestWithFallback("/playlists?id=$playlistId&limit=40")

        if (response.isEmpty()) return@withContext emptyList()

        parseSongsJson(response, root)
    }

    private fun parseSongsJson(jsonString: String,root: String): List<SongItem> {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val songsArray =
            json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

        val songs = mutableListOf<SongItem>()

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val images = parseImages(song.optJSONArray("image"))
            val downloads = parseDownloads(song.optJSONArray("downloadUrl"))
            val artists = parsePrimaryArtists(song.optJSONObject("artists"))
            val albumObject = song.optJSONObject("album")
            val album = Album(
                id = albumObject?.optString("id") ?: "",
                name = albumObject?.optString("name") ?: ""
            )

            songs.add(
                SongItem(
                    id = song.optString("id"),
                    name = song.optString("name"),
                    artist = artists.toMutableList(),
                    album = album,
                    image = images.toMutableList(),
                    duration = song.optInt("duration"),
                    playCount = song.optInt("playCount"),
                    downloadUrl = downloads.toMutableList(),
                    songSource = "jiosaavn"
                )
            )
        }

        return songs
    }

    private fun parsePlaylistJson(jsonString: String,root: String): List<DataItem> {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val playlistsArray =
            json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

        val playlists = mutableListOf<DataItem>()

        for (i in 0 until playlistsArray.length()) {
            val playlist = playlistsArray.getJSONObject(i)

            val images = parseImages(playlist.optJSONArray("image"))
            val artists = parsePrimaryArtists(playlist.optJSONObject("artists"))

            playlists.add(
                DataItem(
                    id = playlist.optString("id"),
                    name = playlist.optString("name"),
                    artist = artists.toMutableList(),
                    image = images.toMutableList(),
                    searchSource = "jiosaavn"
                )
            )
        }

        return playlists
    }

    private fun parseArtistsJson(jsonString: String,root: String): List<Artists> {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val resultArray = json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

        val artists = mutableListOf<Artists>()

        for (i in 0 until resultArray.length()) {
            val artist = resultArray.getJSONObject(i)

            val imageUrl = artist.optJSONArray("image")
                ?.optJSONObject(2)
                ?.optString("url") ?: ""

            artists.add(
                Artists(
                    id = artist.optString("id"),
                    name = artist.optString("name"),
                    role = artist.optString("role"),
                    image = imageUrl,
                    type = artist.optString("type")
                )
            )
        }

        return artists
    }

    private fun parseAlbumListJson(jsonString: String,root: String): List<DataItem> {

        val json = JSONObject(jsonString)
        if (!json.optBoolean("success")) return emptyList()

        val albumsArray = json.getJSONObject("data").optJSONArray(root) ?: return emptyList()

        val albums = mutableListOf<DataItem>()

        for (i in 0 until albumsArray.length()) {
            val album = albumsArray.getJSONObject(i)

            val images = parseImages(album.optJSONArray("image"))
            val artists = parsePrimaryArtists(album.optJSONObject("artists"))

            albums.add(
                DataItem(
                    id = album.optString("id"),
                    name = album.optString("name"),
                    artist = artists.toMutableList(),
                    image = images.toMutableList(),
                    searchSource = "jiosaavn"
                )
            )
        }

        return albums
    }

    private fun parseImages(array: org.json.JSONArray?): List<Image> {
        if (array == null) return emptyList()
        return List(array.length()) {
            val obj = array.getJSONObject(it)
            Image(
                quality = obj.optString("quality"),
                url = obj.optString("url")
            )
        }
    }

    private fun parsePrimaryArtists(artistsObj: JSONObject?): List<Artists> {
        val primary = artistsObj?.optJSONArray("primary") ?: return emptyList()

        val artists = mutableListOf<Artists>()

        for (i in 0 until primary.length()) {
            val artist = primary.getJSONObject(i)

            val imageUrl = artist.optJSONArray("image")
                ?.optJSONObject(2)
                ?.optString("url") ?: ""

            artists.add(
                Artists(
                    id = artist.optString("id"),
                    name = artist.optString("name"),
                    role = artist.optString("role"),
                    image = imageUrl,
                    type = artist.optString("type")
                )
            )
        }

        return artists
    }

    private fun parseDownloads(array: org.json.JSONArray?): List<Download> {
        if (array == null) return emptyList()
        return List(array.length()) { index ->
            val obj = array.getJSONObject(index)
            val qualityString = obj.optString("quality")

            val quality = when (qualityString.lowercase()) {
                "12kbps", "48kbps" -> Quality.LOW
                "96kbps", "160kbps" -> Quality.MEDIUM
                "320kbps" -> Quality.HIGH
                else -> Quality.MEDIUM
            }

            Download(
                quality = quality,
                url = obj.optString("url")
            )
        }
    }
}