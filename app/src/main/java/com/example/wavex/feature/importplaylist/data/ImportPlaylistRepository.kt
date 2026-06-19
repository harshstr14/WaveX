package com.example.wavex.feature.importplaylist.data

import com.example.wavex.core.model.Album
import com.example.wavex.core.model.Artists
import com.example.wavex.core.model.Download
import com.example.wavex.core.model.Image
import com.example.wavex.core.model.SongItem
import com.example.wavex.core.model.Quality
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.collections.toMutableList

@Singleton
class ImportPlaylistRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun importWaveXPlaylist(apiUrl: String, playlistId: String): ImportResult {
        val request = Request.Builder()
            .url("$apiUrl/userplaylist/$playlistId")
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            return ImportResult.Error("Playlist not available")
        }

        val body = response.body?.string().orEmpty()

        if (body.isEmpty()) {
            return ImportResult.Error("Empty playlist")
        }

        return parseWaveXPlaylist(body)
    }

    suspend fun importSpotifyPlaylist(apiUrl: String, url: String): ImportResult {
        val request = Request.Builder()
            .url("$apiUrl/$url")
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            return ImportResult.Error("Playlist not available")
        }

        val body = response.body?.string().orEmpty()

        if (body.isEmpty()) {
            return ImportResult.Error(
                "Empty response from server"
            )
        }

        return parsePlaylistData(body)
    }

    private suspend fun parseWaveXPlaylist(jsonString: String): ImportResult {
        val json = JSONObject(jsonString)

        val playlistName = json.optString("playlistName")

        val imageUrl = json.optString("imageUrl")

        val songsArray =
            json.optJSONArray("songs")
                ?: return ImportResult.Error(
                    "Playlist is empty"
                )

        val songs = mutableListOf<SongItem>()

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val albumObject =
                song.optJSONObject("album")

            songs.add(
                SongItem(
                    id = song.optString("id"),
                    name = song.optString("name"),
                    album = Album(
                        id = albumObject?.optString("id")
                            .orEmpty(),
                        name = albumObject?.optString("name")
                            .orEmpty()
                    ),
                    artist = parseWaveXArtists(
                        song.optJSONArray("artist")
                    ).toMutableList(),
                    image = parseImages(
                        song.optJSONArray("image")
                    ).toMutableList(),
                    duration = song.optInt("duration"),
                    playCount = song.optInt("playCount"),
                    downloadUrl = parseDownloads(
                        song.optJSONArray("downloadUrl")
                    ).toMutableList()
                )
            )
        }

        return savePlaylist(
            playlistName,
            imageUrl,
            songs
        )
    }

    private suspend fun parsePlaylistData(jsonString: String): ImportResult {
        val json = JSONObject(jsonString)

        val data =
            json.optJSONObject("data")
                ?: return ImportResult.Error(
                    "Invalid response"
                )

        val playlistName = data.optString("name")

        val imageUrl = data.optString("image")

        val songsArray =
            data.optJSONArray("songs")
                ?: return ImportResult.Error(
                    "Playlist is empty"
                )

        val songs = mutableListOf<SongItem>()

        for (i in 0 until songsArray.length()) {
            val song = songsArray.getJSONObject(i)

            val albumObject = song.optJSONObject("album")

            songs.add(
                SongItem(
                    id = song.optString("id"),
                    name = song.optString("name"),
                    album = Album(
                        id = albumObject?.optString("id")
                            .orEmpty(),
                        name = albumObject?.optString("name")
                            .orEmpty()
                    ),
                    artist = parsePrimaryArtists(
                        song.optJSONObject("artists")
                    ).toMutableList(),
                    image = parseImages(
                        song.optJSONArray("image")
                    ).toMutableList(),
                    duration = song.optInt("duration"),
                    playCount = song.optInt("playCount"),
                    downloadUrl = parseDownloads(
                        song.optJSONArray("downloadUrl")
                    ).toMutableList()
                )
            )
        }

        return savePlaylist(
            playlistName,
            imageUrl,
            songs
        )
    }

    private suspend fun savePlaylist(
        name: String,
        image: String,
        songs: List<SongItem>
    ): ImportResult {

        val userId =
            firebaseAuth.currentUser?.uid
                ?: return ImportResult.Error(
                    "User not logged in"
                )

        val playlistRef =
            firebaseDatabase.getReference("Users")
                .child(userId)
                .child("Favourites")
                .child("MyPlaylists")

        val snapshot =
            playlistRef
                .orderByChild("playlistName")
                .equalTo(name.trim())
                .get()
                .await()

        if (snapshot.exists()) {
            return ImportResult.Error(
                "Playlist already exists"
            )
        }

        val playlistId =
            playlistRef.push().key
                ?: return ImportResult.Error(
                    "Failed to create playlist"
                )

        val playlistData = mapOf(
            "playlistId" to playlistId,
            "playlistName" to name,
            "imageUrl" to image,
            "songs" to songs,
            "totalSongs" to songs.size,
            "totalDuration" to songs.sumOf { it.duration }
        )

        playlistRef
            .child(playlistId)
            .setValue(playlistData)
            .await()

        return ImportResult.Success(
            "Playlist Imported"
        )
    }

    private fun parseWaveXArtists(array: JSONArray?): List<Artists> {
        if (array == null) return emptyList()

        val artists = mutableListOf<Artists>()

        for (i in 0 until array.length()) {

            val artist = array.getJSONObject(i)

            artists.add(
                Artists(
                    id = artist.optString("id"),
                    name = artist.optString("name"),
                    role = artist.optString("role"),
                    image = artist.optString("image"),
                    type = artist.optString("type")
                )
            )
        }

        return artists
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

    private fun parseImages(array: JSONArray?): List<Image> {
        if (array == null) return emptyList()
        return List(array.length()) {
            val obj = array.getJSONObject(it)
            Image(
                quality = obj.optString("quality"),
                url = obj.optString("url")
            )
        }
    }

    private fun parseDownloads(array: JSONArray?): List<Download> {
        if (array == null) return emptyList()

        return List(array.length()) { index ->
            val obj = array.getJSONObject(index)

            val quality = when (obj.optString("quality")) {
                "96kbps" -> Quality.LOW

                "160kbps" -> Quality.MEDIUM

                "320kbps" -> Quality.HIGH

                "48kbps" -> Quality.LOSSLESS

                else -> Quality.MEDIUM
            }

            Download(
                quality = quality,
                url = obj.optString("url")
            )
        }
    }
}