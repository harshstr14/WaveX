package com.example.wavex.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.coroutineContext

suspend fun requestWithFallback(
    endpoint: String
): String = withContext(Dispatchers.IO) {

    val apis = listOf(
        NetworkConstants.API_URL_1,
        NetworkConstants.API_URL_2,
        NetworkConstants.API_URL_3
    )

    for (baseUrl in apis) {
        try {
            val request = Request.Builder()
                .url("$baseUrl$endpoint")
                .get()
                .build()

            val call = HttpClientProvider.client.newCall(request)

            coroutineContext.job.invokeOnCompletion {
                call.cancel()
            }

            val response = call.execute()

            response.use {
                if (it.isSuccessful) {
                    return@withContext it.body.string()
                }

                if (it.code in 500..599) {
                    continue
                }

                if (it.code in 400..499) {
                    throw Exception("Client Error ${it.code}")
                }
            }

        } catch (e: Exception) {
            if (
                e is SocketTimeoutException ||
                e is ConnectException ||
                e is UnknownHostException
            ) {
                continue
            }

            throw e
        }
    }

    throw Exception("All APIs failed")
}