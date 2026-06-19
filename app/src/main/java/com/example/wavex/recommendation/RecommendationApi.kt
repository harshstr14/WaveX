package com.example.wavex.recommendation

import com.example.wavex.core.model.SongItem
import com.example.wavex.recommendation.dataClass.PlayedSong
import com.example.wavex.recommendation.dataClass.RecommendationRequest
import com.example.wavex.recommendation.dataClass.RecommendationResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RecommendationApi {
    private val gson = Gson()
    private val client = OkHttpClient()
    private val BASE_URL =
        "http://192.168.1.5:8000/recommend"

    fun getRecommendations(
        history: List<PlayedSong>
    ): List<SongItem> {
        return try {
            val requestData =
                RecommendationRequest(history)

            val json =
                gson.toJson(requestData)

            val requestBody =
                json.toRequestBody(
                    "application/json".toMediaType()
                )

            val request = Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build()

            val response =
                client.newCall(request).execute()

            if (response.isSuccessful) {
                val body =
                    response.body?.string()

                val recommendationResponse =
                    gson.fromJson(
                        body,
                        RecommendationResponse::class.java
                    )

                recommendationResponse.recommendations
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()

            emptyList()
        }
    }
}