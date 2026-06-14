package com.example.wavex.libraryScreen.importPlaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.profileScreen.settingScreen.Quality
import com.example.wavex.songData.Album
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ImportPlaylistViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference()
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun importWaveXPlaylist(apiUrl: String, url: String) {
        viewModelScope.launch {

            _importState.value = ImportState.Loading

            try {
                val responseBody = withContext(Dispatchers.IO) {

                    val request = Request.Builder()
                        .url("$apiUrl/userplaylist/$url")
                        .get()
                        .build()

                    val response = okHttpClient.newCall(request).execute()

                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}")
                    }

                    response.body?.string() ?: ""
                }

                if (responseBody.isEmpty()) {
                    _importState.value = ImportState.Error("Empty playlist")
                    return@launch
                }

                val json = JSONObject(responseBody)

                val success = json.optBoolean("success", true)

                if (!success) {
                    val message = json.optString("message", "Playlist not found")
                    _importState.value = ImportState.Error(message)
                    return@launch
                }

                val songs = json.optJSONArray("songs")

                if (songs == null || songs.length() == 0) {
                    _importState.value = ImportState.Error("Playlist is empty")
                    return@launch
                }

                parseWaveXPlaylist(responseBody)

            } catch (_: SocketTimeoutException) {
                _importState.value = ImportState.Error("Request timed out")

            } catch (_: IOException) {
                _importState.value = ImportState.Error("Playlist not available")

            } catch (_: Exception) {
                _importState.value = ImportState.Error("Something went wrong")
            }
        }
    }

    fun importPlaylistByUrl(apiUrl: String, url: String) {
        viewModelScope.launch {

            _importState.value = ImportState.Loading

            try {
                val responseBody = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("$apiUrl/$url")
                        .get()
                        .build()

                    val response = okHttpClient.newCall(request).execute()

                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}")
                    }

                    response.body?.string() ?: ""
                }

                if (responseBody.isEmpty()) {
                    _importState.value = ImportState.Error("Empty response from server")
                    return@launch
                }

                parsePlaylistData(responseBody)

            } catch (_: SocketTimeoutException) {
                _importState.value = ImportState.Error("Request timed out")

            } catch (_: IOException) {
                _importState.value = ImportState.Error("Playlist not available")

            } catch (_: Exception) {
                _importState.value = ImportState.Error("Something went wrong")
            }
        }
    }

    fun cancelImport() {
        _importState.value = ImportState.Idle
    }

    private suspend fun parseWaveXPlaylist(jsonString: String) {
        val json = JSONObject(jsonString)

        val name = json.optString("playlistName")
        val image = json.optString("imageUrl")
        val songsArray = json.optJSONArray("songs")

        val songList = mutableListOf<SongItem>()

        for (i in 0 until (songsArray?.length() ?: 0)) {

            val song = songsArray!!.getJSONObject(i)

            val albumObj = song.optJSONObject("album")

            val album = Album(
                id = albumObj?.optString("id") ?: "",
                name = albumObj?.optString("name") ?: ""
            )

            val artists = parseWaveXArtists(song.optJSONArray("artist"))

            songList.add(
                SongItem(
                    id = song.optString("id"),
                    name = song.optString("name"),
                    artist = artists.toMutableList(),
                    album = album,
                    image = parseImages(song.optJSONArray("image")).toMutableList(),
                    duration = song.optInt("duration"),
                    playCount = song.optInt("playCount"),
                    downloadUrl = parseDownloads(song.optJSONArray("downloadUrl")).toMutableList()
                )
            )
        }

        val totalDuration = songList.sumOf { it.duration }

        saveToFirebaseSuspend(name, image, songList, totalDuration)
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

    private suspend fun parsePlaylistData(jsonString: String) {

        val (name, image, songList) = withContext(Dispatchers.Default) {
            val json = JSONObject(jsonString)
            val success = json.optBoolean("success", false)
            if (!success) throw Exception("Invalid response")

            val data = json.getJSONObject("data")
            val name = data.optString("name").trim()
            val image = data.optString("image")
            val songsArray = data.optJSONArray("songs")

            val songList = ArrayList<SongItem>()

            songsArray?.let {
                for (i in 0 until it.length()) {
                    val song = it.optJSONObject(i)

                    val duration = song.optInt("duration")
                    val albumObject = song.optJSONObject("album")
                    val album = Album(
                        id = albumObject?.optString("id") ?: "",
                        name = albumObject?.optString("name") ?: ""
                    )

                    songList.add(
                        SongItem(
                            id = song.optString("id"),
                            name = song.optString("name"),
                            artist = parsePrimaryArtists(song.optJSONObject("artists")).toMutableList(),
                            album = album,
                            image = parseImages(song.optJSONArray("image")).toMutableList(),
                            duration = duration,
                            playCount = song.optInt("playCount"),
                            downloadUrl = parseDownloads(song.optJSONArray("downloadUrl")).toMutableList()
                        )
                    )
                }
            }

            Triple(name, image, songList)
        }

        val totalDuration = songList.sumOf { it.duration }

        saveToFirebaseSuspend(name, image, songList, totalDuration)
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

    private suspend fun saveToFirebaseSuspend(
        name: String,
        image: String,
        songList: List<SongItem>,
        totalDuration: Int
    ) {
        val userID = auth.currentUser?.uid
            ?: throw Exception("User not logged in")

        val playListRef = database
            .child("Users")
            .child(userID)
            .child("Favourites")
            .child("MyPlaylists")

        withContext(Dispatchers.IO) {

            val trimmedName = name.trim()

            val snapshot = playListRef
                .orderByChild("playlistName")
                .equalTo(trimmedName)
                .get()
                .await()

            if (snapshot.exists()) {
                withContext(Dispatchers.Main) {
                    _importState.value = ImportState.Error("Playlist already exists")
                }
                return@withContext
            }

            val playlistId = playListRef.push().key
                ?: throw Exception("Failed to generate playlistId")

            val playlistData = mutableMapOf<String, Any>()

            playlistData["playlistId"] = playlistId
            playlistData["playlistName"] = name
            playlistData["imageUrl"] = image
            playlistData["totalSongs"] = songList.size
            playlistData["songs"] = songList
            playlistData["totalDuration"] = totalDuration

            playListRef.child(playlistId)
                .setValue(playlistData)
                .await()

            withContext(Dispatchers.Main) {
                _importState.value = ImportState.Success("Playlist Imported")
            }
        }
    }
}