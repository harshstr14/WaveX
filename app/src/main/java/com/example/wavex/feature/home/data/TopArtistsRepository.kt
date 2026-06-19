package com.example.wavex.feature.home.data

import android.util.Log
import com.example.wavex.BuildConfig
import com.example.wavex.HttpClientProvider
import com.example.wavex.core.database.dao.ArtistDao
import com.example.wavex.core.datastore.CacheManager
import com.example.wavex.core.database.entity.ArtistEntity
import com.example.wavex.feature.search.presentation.SearchSource
import com.example.wavex.core.model.Artists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class TopArtistsRepository @Inject constructor(
    private val dao: ArtistDao,
    private val cacheManager: CacheManager
) {
    private val baseUrl = BuildConfig.MUSIC_AI_API_BASE_URL

    val artists: Flow<List<Artists>> =
        dao.getArtists().map { entities ->
            entities.map {
                Artists(
                    id = it.id,
                    name = it.name,
                    image = it.image,
                    type = it.type,
                    searchSource = it.searchSource
                )
            }
        }

    suspend fun refreshArtists() {
        val lastRefresh = cacheManager.getArtistRefreshTime()

        val currentTime = System.currentTimeMillis()

        val oneDay = 24 * 60 * 60 * 1000L

        val shouldRefresh = currentTime - lastRefresh > oneDay || !dao.hasData()

        if (!shouldRefresh) {
            Log.d("FROM_CACHE","true")
            return
        }

        try {
            val freshArtists = fetchArtistsFromApi()

            dao.clearArtists()

            dao.insertArtists(
                freshArtists.map {
                    ArtistEntity(
                        id = it.id,
                        name = it.name,
                        image = it.image,
                        type = it.type,
                        searchSource = it.searchSource
                    )
                }
            )

            cacheManager.saveArtistRefreshTime(
                currentTime
            )
        } catch (e: Exception) {
            Log.e("TOP_ARTISTS","Refresh Error",e)
        }
    }

    private suspend fun fetchArtistsFromApi(): List<Artists> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$baseUrl/top-artists")
                .get()
                .build()

            val response = HttpClientProvider.client.newCall(request).execute()

            response.use {
                if (!response.isSuccessful) {
                    throw Exception("Network Error")
                }

                val body = response.body?.string().orEmpty()

                parseMusicAiJson(body)
            }
        }
    }

    private fun parseMusicAiJson(jsonString: String): List<Artists> {
        try {
            val parsedList = mutableListOf<Artists>()

            val json = JSONObject(jsonString)

            if (!json.optBoolean("success")) {
                return emptyList()
            }

            val dataArray = json.optJSONArray("data") ?: return emptyList()

            for (i in 0 until dataArray.length()) {
                val item = dataArray.optJSONObject(i) ?: continue

                val imageArray = item.optJSONArray("image")

                val imageUrl =
                    imageArray
                        ?.optJSONObject(
                            imageArray.length() - 1
                        )
                        ?.optString("url")
                        ?: ""

                parsedList.add(
                    Artists(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        image = imageUrl,
                        type = item.optString("type"),
                        searchSource = SearchSource.JIOSAAVN.name
                    )
                )
            }
            return parsedList
        } catch (e: Exception) {
            Log.e("TOP_ARTISTS","Parse Error",e)

            return emptyList()
        }
    }
}