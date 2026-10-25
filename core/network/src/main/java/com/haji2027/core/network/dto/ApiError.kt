package com.haji2027.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload error standar dari backend Cloud Functions & API Gateway.
 */
@Serializable
data class ApiErrorResponse(
    @SerialName("error_code") val errorCode: String,
    @SerialName("message") val message: String
)

/**
 * Kode error standar sesuai kontrak Bagian 21.2 SYSTEM_DESIGN_DOCUMENT.md
 */
enum class ApiErrorCode(val defaultHttpStatus: Int, val description: String) {
    AUTH_REQUIRED(401, "Sesi autentikasi habis; butuh login anonim ulang"),
    TOKEN_EXPIRED(410, "Kode QR telah kedaluwarsa (> 5 menit)"),
    TOKEN_ALREADY_USED(409, "Token telah diklaim sebelumnya (anti-replay)"),
    TRACKING_ACCESS_REVOKED(403, "Izin pelacakan telah dicabut oleh target"),
    RATE_LIMITED(429, "Terlalu banyak permintaan token/API"),
    NETWORK_UNAVAILABLE(503, "Jaringan internet tidak tersedia"),
    UNKNOWN(500, "Terjadi kesalahan internal server");

    companion object {
        fun fromCode(code: String): ApiErrorCode {
            return entries.firstOrNull { it.name.equals(code, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

/**
 * Exception representatif untuk error panggilan API / Cloud Functions.
 */
class ApiException(
    val code: ApiErrorCode,
    override val message: String,
    val httpStatus: Int = code.defaultHttpStatus,
    cause: Throwable? = null
) : Exception(message, cause)
