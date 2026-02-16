package com.example.wavex.libraryScreen.importPlaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavex.homeScreen.SongItem
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
        .connectTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(100, TimeUnit.SECONDS)
        .build()

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

                    songList.add(
                        SongItem(
                            id = song.optString("id"),
                            name = song.optString("name"),
                            artist = parsePrimaryArtists(song.optJSONObject("artists")).toMutableList(),
                            image = parseImages(song.optJSONArray("image")).toMutableList(),
                            duration = duration,
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
        return List(array.length()) {
            val obj = array.getJSONObject(it)
            Download(
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

    private suspend fun saveToFirebaseSuspend (
        name: String,
        image: String,
        songList: List<SongItem>,
        totalDuration: Int
    ) {
        val userID = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val playListRef = database
            .child("Users")
            .child(userID)
            .child("Favourites")
            .child("MyPlaylists")

        withContext(Dispatchers.IO) {

            val snapshot = playListRef.child(name.trim()).get().await()

            if (snapshot.exists()) {
                withContext(Dispatchers.Main) {
                    _importState.value = ImportState.Error("Playlist already exists")
                }
                return@withContext
            }

            val playlistData = mutableMapOf<String, Any>()

            playlistData["playlistName"] = name
            playlistData["imageUrl"] = image
            playlistData["totalSongs"] = songList.size
            playlistData["songs"] = songList
            playlistData["totalDuration"] = totalDuration

            playListRef.child(name).setValue(playlistData).await()

            withContext(Dispatchers.Main) {
                _importState.value = ImportState.Success("Playlist Imported")
            }
        }
    }
}