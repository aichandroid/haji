package com.haji2027.core.network.api

import com.haji2027.core.network.dto.ClaimTokenRequest
import com.haji2027.core.network.dto.ClaimTokenResponse
import com.haji2027.core.network.dto.GenerateTokenRequest
import com.haji2027.core.network.dto.GenerateTokenResponse
import com.haji2027.core.network.dto.LocationPointDto
import com.haji2027.core.network.dto.PurgeUserDataResponse
import com.haji2027.core.network.dto.RevokeAccessRequest
import com.haji2027.core.network.dto.RevokeAccessResponse

/**
 * Kontrak klien jaringan untuk seluruh operasi pelacakan keluarga dan privasi ke Cloud Functions.
 */
interface TrackingApiClient {
    /**
     * Meminta penerbitan token QR acak bertempo 5 menit bagi jemaah yang bersedia dilacak.
     */
    suspend fun generatePairingToken(request: GenerateTokenRequest = GenerateTokenRequest()): Result<GenerateTokenResponse>

    /**
     * Mengklaim token QR yang dipindai kamera untuk mengaktifkan hubungan pelacakan.
     */
    suspend fun claimPairingToken(request: ClaimTokenRequest): Result<ClaimTokenResponse>

    /**
     * Mencabut hak akses salah satu pelacak secara instan.
     */
    suspend fun revokeTrackingAccess(request: RevokeAccessRequest): Result<RevokeAccessResponse>

    /**
     * Menghapus identitas dan riwayat telemetri di server (Right to be Forgotten).
     */
    suspend fun purgeUserData(): Result<PurgeUserDataResponse>

    /**
     * Mengirimkan sekumpulan data koordinat lokasi yang sempat tersimpan di antrean offline lokal.
     */
    suspend fun syncLocations(locations: List<LocationPointDto>): Result<Unit>
}
