package com.haji2027.core.network.dto

/**
 * Data terurai dari kode QR penyambungan pelacakan keluarga.
 */
data class QrPayload(
    val tokenId: String,
    val targetUid: String
) {
    /**
     * Membentuk URI string standar: hajj2027://pair?t={tokenId}&u={targetUid}
     */
    fun toUriString(): String = createUriString(tokenId, targetUid)

    companion object {
        const val SCHEME = "hajj2027"
        const val HOST = "pair"
        const val PARAM_TOKEN = "t"
        const val PARAM_USER = "u"

        fun createUriString(tokenId: String, targetUid: String): String {
            require(tokenId.isNotBlank()) { "Token ID tidak boleh kosong" }
            require(targetUid.isNotBlank()) { "Target UID tidak boleh kosong" }
            return "$SCHEME://$HOST?$PARAM_TOKEN=$tokenId&$PARAM_USER=$targetUid"
        }

        /**
         * Mem-parse string QR code menjadi objek [QrPayload].
         * Mengembalikan Result.failure jika format tidak sesuai skema hajj2027://pair.
         */
        fun parse(qrString: String?): Result<QrPayload> = runCatching {
            val input = qrString?.trim() ?: throw IllegalArgumentException("Teks QR kosong atau bernilai null")

            if (!input.startsWith("$SCHEME://$HOST", ignoreCase = true)) {
                throw IllegalArgumentException("Skema QR tidak valid (harus diawali $SCHEME://$HOST)")
            }

            val queryIndex = input.indexOf('?')
            if (queryIndex == -1 || queryIndex == input.length - 1) {
                throw IllegalArgumentException("QR tidak memiliki parameter kueri (?t=...&u=...)")
            }

            val queryString = input.substring(queryIndex + 1)
            val queryParams = queryString.split("&").associate { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
            }

            val tokenId = queryParams[PARAM_TOKEN]?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Parameter '$PARAM_TOKEN' (Token ID) tidak ditemukan")

            val targetUid = queryParams[PARAM_USER]?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Parameter '$PARAM_USER' (Target UID) tidak ditemukan")

            QrPayload(tokenId = tokenId, targetUid = targetUid)
        }
    }
}
