package com.yourname.httpcustom

import android.util.Log
import okhttp3.*
import java.io.IOException

object HttpClient {
    private val tag = "HttpClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    fun sendCustomRequest(
        url: String,
        payload: String,
        headers: Map<String, String>,
        onSuccess: (response: String) -> Unit = {},
        onError: (error: String) -> Unit = {}
    ) {
        val body = RequestBody.create(MediaType.parse("application/octet-stream"), payload)
        val builder = Request.Builder().url(url).post(body)

        headers.forEach { (key, value) -> builder.addHeader(key, value) }

        val request = builder.build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(tag, "Request failed: ${e.message}", e)
                onError("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: "No response body"
                    Log.d(tag, "Response Code: ${response.code}")
                    Log.d(tag, "Response Body: $responseBody")
                    
                    if (response.isSuccessful) {
                        onSuccess(responseBody)
                    } else {
                        onError("HTTP Error: ${response.code} - $responseBody")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error parsing response: ${e.message}", e)
                    onError("Error parsing response: ${e.message}")
                }
            }
        })
    }

    fun parseHeaders(headersStr: String): Map<String, String> {
        return headersStr.split("\n")
            .filter { it.isNotBlank() }
            .associate { line ->
                val parts = line.split(":", limit = 2)
                parts[0].trim() to (parts.getOrNull(1)?.trim() ?: "")
            }
    }
}
