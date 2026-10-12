package com.kanha.extractor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object Http {
    const val UA = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
    val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true).followSslRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build()

    suspend fun get(url: String, referer: String? = null, headers: Map<String, String> = emptyMap()): String =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).header("User-Agent", UA).apply {
                referer?.let { header("Referer", it) }
                headers.forEach { (key, value) -> header(key, value) }
            }.build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                response.body?.string().orEmpty()
            }
        }

    suspend fun post(url: String, form: Map<String, String>, referer: String? = null): String =
        withContext(Dispatchers.IO) {
            val body = FormBody.Builder().apply { form.forEach { (key, value) -> add(key, value) } }.build()
            val request = Request.Builder().url(url).header("User-Agent", UA)
                .apply { referer?.let { header("Referer", it) } }.post(body).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                response.body?.string().orEmpty()
            }
        }
}
