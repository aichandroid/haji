package com.haji2027.feature.settings.ota

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Kontrak pengunduhan berkas OTA dari HTTPS static server.
 */
interface OtaDownloader {
    suspend fun fetchManifest(url: String): Result<String>
    suspend fun downloadContent(url: String): Result<ByteArray>
}

/**
 * Implementasi standar klien pengunduhan berbasis [HttpURLConnection]
 * dengan timeout terkelola dan enkripsi TLS bawaan platform Android.
 */
class DefaultOtaDownloader(
    private val connectTimeoutMs: Int = 15_000,
    private val readTimeoutMs: Int = 30_000
) : OtaDownloader {

    override suspend fun fetchManifest(url: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Hajj2027-OTA-Client/1.0")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("Gagal mengunduh manifest OTA: HTTP $responseCode")
            }

            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
    }

    override suspend fun downloadContent(url: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                setRequestProperty("User-Agent", "Hajj2027-OTA-Client/1.0")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("Gagal mengunduh paket materi OTA: HTTP $responseCode")
            }

            connection.inputStream.use { it.readBytes() }
        }
    }
}
