package com.example.wavex.service

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response

object DownloaderImpl : Downloader() {
    private val client = OkHttpClient()

    override fun execute(request: Request): Response {

        val requestBuilder = okhttp3.Request.Builder()
            .url(request.url())
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/136.0.0.0 Safari/537.36"
            )
            .header(
                "Accept-Language",
                "en-US,en;q=0.9"
            )
            .header(
                "Referer",
                "https://www.youtube.com/"
            )
            .header(
                "Origin",
                "https://www.youtube.com"
            )

        request.headers().forEach { (name, values) ->
            values.forEach {
                requestBuilder.addHeader(name, it)
            }
        }

        when (request.httpMethod()) {

            "POST" -> {

                val bodyBytes = request.dataToSend() ?: ByteArray(0)

                requestBuilder.post(
                    bodyBytes.toRequestBody(
                        "application/json".toMediaType()
                    )
                )
            }

            else -> requestBuilder.get()
        }

        val response = client.newCall(requestBuilder.build()).execute()

        val bodyString = response.body?.string()

        Log.e("YT_URL", response.request.url.toString())
        Log.e("YT_CODE", response.code.toString())
        Log.e("YT_BODY", bodyString ?: "NULL")

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            bodyString,
            response.request.url.toString()
        )
    }
}