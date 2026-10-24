package com.haji2027.core.common.parser

import com.haji2027.core.model.HajjContentPackage
import kotlinx.serialization.json.Json
import java.io.InputStream

/**
 * Parser tangguh dan aman untuk memuat data materi manasik berversi (JSON).
 * Mengabaikan key yang tidak dikenal agar kompatibel ke depan (forward-compatible).
 */
object HajjContentParser {

    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = false
    }

    /**
     * Mem-parsing string JSON menjadi model domain [HajjContentPackage].
     */
    fun parseJson(jsonString: String): Result<HajjContentPackage> {
        return runCatching {
            jsonConfig.decodeFromString<HajjContentPackage>(jsonString)
        }
    }

    /**
     * Mem-parsing [InputStream] menjadi model domain [HajjContentPackage].
     */
    fun parseJson(inputStream: InputStream): Result<HajjContentPackage> {
        return runCatching {
            val content = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            jsonConfig.decodeFromString<HajjContentPackage>(content)
        }
    }
}
