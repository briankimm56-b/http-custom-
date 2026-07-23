package com.yourname.httpcustomclone

import okhttp3.*
import java.io.IOException

object HttpClient {
    private val client = OkHttpClient()

    fun sendRequest(
        url: String,
        payload: String,
        headers: Map<String, String>,
        callback: (String) -> Unit
    ) {
        val body = RequestBody.create(MediaType.parse("text/plain"), payload)
        val builder = Request.Builder().url(url).post(body)

        headers.forEach { builder.addHeader(it.key, it.value) }

        client.newCall(builder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Error: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                callback("Response: ${response.code()} \n ${response.body()?.string()}")
            }
        })
    }
}
