package com.haji2027.core.network.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representasi profil pengguna terautentikasi (murni anonim).
 * Jemaah haji tidak diminta email, kata sandi, atau nomor telepon saat pertama menggunakan aplikasi.
 */
@Serializable
data class AuthUser(
    @SerialName("uid") val uid: String,
    @SerialName("is_anonymous") val isAnonymous: Boolean = true,
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)
