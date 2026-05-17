package com.example.wavex.artistScreen

import com.example.wavex.HttpClientProvider
import com.example.wavex.songData.Artists
import com.example.wavex.songData.Download
import com.example.wavex.songData.Image
import com.example.wavex.homeScreen.DataItem
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.requestWithFallback
import com.example.wavex.songData.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class ArtistRepository {
    suspend fun fetchArtistById(artistId: String): ArtistDetailUiState =
        withContext(Dispatchers.IO) {
            val response = requestWithFallback("/artists?id=$artistId")
            if (response.isEmpty()) {
                return@withContext ArtistDetailUiState(
                    isError = true,
                    errorMessage = "Network error. Please try again."
                )
            }

            parseArtist(response)
        }

    private fun parseArtist(jsonString: String): ArtistDetailUiState {
        val json = JSONObject(jsonString)
        if (!json.optBoolean("success", false)) {
            return ArtistDetailUiState(
                isError = true,
                errorMessage = "Artist not found"
            )
        }

        val data = json.getJSONObject("data")

        val imageUrl = data.optJSONArray("image")
            ?.optJSONObject(2)
            ?.optString("url") ?: ""

        return ArtistDetailUiState(
            id = data.optString("id"),
            name = data.optString("name"),
            followerCount = data.optInt("followerCount"),
            fanCount = data.optString("fanCount"),
            isVerified = data.optBoolean("isVerified"),
            imageUrl = imageUrl,
            topSongs = parseTopSongs(data),
            topAlbums = parseAlbums(data.optJSONArray("topAlbums")),
            singles = parseAlbums(data.optJSONArray("singles"))
        )
    }

    private fun parseTopSongs(data: JSONObject): List<SongItem> {
        val list = mutableListOf<SongItem>()
        val array = data.optJSONArray("topSongs") ?: return emptyList()

        for (i in 0 until array.length()) {
            val song = array.getJSONObject(i)

            val images = song.optJSONArray("image")?.let { arr ->
                MutableList(arr.length()) { idx ->
                    val obj = arr.getJSONObject(idx)
                    Image(obj.optString("quality"), obj.optString("url"))
                }
            } ?: emptyList()

            val downloads = song.optJSONArray("downloadUrl")?.let { arr ->
                MutableList(arr.length()) { idx ->
                    val obj = arr.getJSONObject(idx)
                    Download(obj.optString("quality"), obj.optString("url"))
                }
            } ?: emptyList()

            val artists = parsePrimaryArtists(song.optJSONObject("artists"))

            val albumObject = song.optJSONObject("album")
            val album = Album(
                id = albumObject?.optString("id") ?: "",
                name = albumObject?.optString("name") ?: ""
            )

            list.add(
                SongItem(
                    id = song.optString("id"),
                    name = song.optString("name"),
                    artist = artists.toMutableList(),
                    album = album,
                    image = images.toMutableList(),
                    duration = song.optInt("duration"),
                    playCount = song.optInt("playCount"),
                    downloadUrl = downloads.toMutableList(),
                    searchSource = "jiosaavn"
                )
            )
        }

        return list
    }

    private fun parseAlbums(array: org.json.JSONArray?): List<DataItem> {
        if (array == null) return emptyList()

        val list = mutableListOf<DataItem>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            val imageUrl = parseImages(item.optJSONArray("image"))

            val artistName = parsePrimaryArtists(item.optJSONObject("artists"))

            list.add(
                DataItem(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    artist = artistName.toMutableList(),
                    image = imageUrl.toMutableList(),
                    source = "jiosaavn"
                )
            )
        }

        return list
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

    suspend fun fetchYTMusicArtist(
        artistId: String,
        baseUrl: String
    ): ArtistDetailUiState = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/artists/$artistId")
                .get()
                .build()

            val call = HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            val response = call.execute()

            if (!response.isSuccessful) {
                return@withContext ArtistDetailUiState(
                    isError = true,
                    errorMessage = "Network error. Please try again."
                )
            }

            val body = response.body?.string().orEmpty()

            if (body.isEmpty()) {
                return@withContext ArtistDetailUiState(
                    isError = true,
                    errorMessage = "Empty response received."
                )
            }

            parseYTMusicArtist(body)
        } catch (e: Exception) {
            e.printStackTrace()
            ArtistDetailUiState(
                isError = true,
                errorMessage = e.message ?: "Something went wrong."
            )
        }
    }

    fun parseYTMusicArtist(jsonString: String): ArtistDetailUiState {
        val json = JSONObject(jsonString)

        val success = json.optBoolean("success", false)

        if (!success) {
            return ArtistDetailUiState(
                isError = true,
                errorMessage = "Failed to load artist."
            )
        }

        val artistObject = json.optJSONObject("artist")

        val id = artistObject?.optString("browseId").orEmpty()
        val name = artistObject?.optString("name").orEmpty()
        val imageUrl = artistObject
            ?.optString("thumbnail")
            ?.replace(Regex("w\\d+-h\\d+"), "w520-h520")
            .orEmpty()

        val fanCount = artistObject?.optString("subscribers").orEmpty()

        val followerCount = fanCount
            .replace(",", "")
            .filter { it.isDigit() }
            .toIntOrNull() ?: 0

        val topSongs = mutableListOf<SongItem>()
        val topSongsArray = json.optJSONArray("topSongs")

        if (topSongsArray != null) {
            for (i in 0 until topSongsArray.length()) {
                val item = topSongsArray.getJSONObject(i)

                topSongs.add(
                    SongItem(
                        id = item.optString("videoId"),
                        name = item.optString("title"),
                        image = mutableListOf(
                            Image(
                                quality = "high",
                                url = optimizeImage(item.optString("thumbnail"))
                            )
                        ),
                        searchSource = "ytmusic"
                    )
                )
            }
        }

        val albums = mutableListOf<DataItem>()
        val albumsArray = json.optJSONArray("albums")

        if (albumsArray != null) {
            for (i in 0 until albumsArray.length()) {
                val item = albumsArray.getJSONObject(i)

                albums.add(
                    DataItem(
                        id = item.optString("browseId"),
                        name = item.optString("title"),
                        image = mutableListOf(
                            Image(
                                quality = "high",
                                url = optimizeImage(item.optString("thumbnail"))
                            )
                        ),
                        source = "ytmusic"
                    )
                )
            }
        }

        val singles = mutableListOf<DataItem>()
        val singlesArray = json.optJSONArray("singles")

        if (singlesArray != null) {
            for (i in 0 until singlesArray.length()) {
                val item = singlesArray.getJSONObject(i)

                singles.add(
                    DataItem(
                        id = item.optString("browseId"),
                        name = item.optString("title"),
                        image = mutableListOf(
                            Image(
                                quality = "high",
                                url = optimizeImage(item.optString("thumbnail"))
                            )
                        ),
                        source = "ytmusic"
                    )
                )
            }
        }

        return ArtistDetailUiState(
            id = id,
            name = name,
            followerCount = followerCount,
            fanCount = fanCount,
            isVerified = false,
            imageUrl = imageUrl,
            topSongs = topSongs,
            topAlbums = albums,
            singles = singles
        )
    }

    private fun optimizeImage(url: String): String {
        return url.replace(Regex("w\\d+-h\\d+"), "w520-h520")
    }
}