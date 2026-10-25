package com.haji2027.core.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenDtosSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    @Test
    fun `GenerateTokenRequest serializes to expected json`() {
        val request = GenerateTokenRequest(clientTimestampMs = 1779600000000L)
        val jsonString = json.encodeToString(request)

        assertTrue(jsonString.contains("\"client_timestamp_ms\":1779600000000"))
    }

    @Test
    fun `GenerateTokenResponse deserializes from Section 21_1 json sample`() {
        val rawJson = """
            {
              "status": "SUCCESS",
              "token_id": "tok_9f8c2e1b4a7d",
              "qr_payload": "hajj2027://pair?t=tok_9f8c2e1b4a7d&u=usr_ayah_123",
              "expires_at_epoch_ms": 1779600300000,
              "validity_seconds": 300
            }
        """.trimIndent()

        val response = json.decodeFromString<GenerateTokenResponse>(rawJson)

        assertEquals("SUCCESS", response.status)
        assertEquals("tok_9f8c2e1b4a7d", response.tokenId)
        assertEquals("hajj2027://pair?t=tok_9f8c2e1b4a7d&u=usr_ayah_123", response.qrPayload)
        assertEquals(1779600300000L, response.expiresAtEpochMs)
        assertEquals(300, response.validitySeconds)
    }

    @Test
    fun `ClaimTokenRequest serializes and deserializes correctly`() {
        val rawJson = """
            {
              "token_id": "tok_9f8c2e1b4a7d",
              "tracker_label": "Fatimah (Anak)"
            }
        """.trimIndent()

        val request = json.decodeFromString<ClaimTokenRequest>(rawJson)
        assertEquals("tok_9f8c2e1b4a7d", request.tokenId)
        assertEquals("Fatimah (Anak)", request.trackerLabel)

        val serialized = json.encodeToString(request)
        assertTrue(serialized.contains("\"token_id\":\"tok_9f8c2e1b4a7d\""))
        assertTrue(serialized.contains("\"tracker_label\":\"Fatimah (Anak)\""))
    }

    @Test
    fun `ClaimTokenResponse deserializes from Section 21_1 json sample`() {
        val rawJson = """
            {
              "status": "SUCCESS",
              "relation_id": "rel_usr_ayah_123_usr_fatimah_456",
              "target_user_id": "usr_ayah_123",
              "paired_at_epoch_ms": 1779600050000,
              "message": "Penyambungan keluarga berhasil. Lokasi mulai dipantau."
            }
        """.trimIndent()

        val response = json.decodeFromString<ClaimTokenResponse>(rawJson)
        assertEquals("SUCCESS", response.status)
        assertEquals("rel_usr_ayah_123_usr_fatimah_456", response.relationId)
        assertEquals("usr_ayah_123", response.targetUserId)
        assertEquals(1779600050000L, response.pairedAtEpochMs)
        assertEquals("Penyambungan keluarga berhasil. Lokasi mulai dipantau.", response.message)
    }

    @Test
    fun `RevokeAccessRequest and Response round-trip matches schema`() {
        val requestJson = """{"tracker_user_id":"usr_fatimah_456"}"""
        val request = json.decodeFromString<RevokeAccessRequest>(requestJson)
        assertEquals("usr_fatimah_456", request.trackerUserId)

        val responseJson = """
            {
              "status": "SUCCESS",
              "revoked_tracker_user_id": "usr_fatimah_456",
              "timestamp_ms": 1779600100000
            }
        """.trimIndent()
        val response = json.decodeFromString<RevokeAccessResponse>(responseJson)
        assertEquals("SUCCESS", response.status)
        assertEquals("usr_fatimah_456", response.revokedTrackerUserId)
        assertEquals(1779600100000L, response.timestampMs)
    }

    @Test
    fun `PurgeUserDataResponse deserializes correctly`() {
        val responseJson = """
            {
              "status": "SUCCESS",
              "purged_user_id": "usr_ayah_123",
              "message": "Seluruh data Anda telah dihapus secara permanen dari server."
            }
        """.trimIndent()

        val response = json.decodeFromString<PurgeUserDataResponse>(responseJson)
        assertEquals("SUCCESS", response.status)
        assertEquals("usr_ayah_123", response.purgedUserId)
        assertTrue(response.message.contains("dihapus secara permanen"))
    }

    @Test
    fun `LocationSyncRequest serializes batch of coordinate points`() {
        val batch = LocationSyncRequest(
            locations = listOf(
                LocationPointDto(
                    userId = "usr_123",
                    latitude = 21.4225,
                    longitude = 39.8262,
                    accuracyMeters = 4.2f,
                    timestampEpochMs = 1779600000000L
                ),
                LocationPointDto(
                    userId = "usr_123",
                    latitude = 21.4226,
                    longitude = 39.8263,
                    accuracyMeters = 3.8f,
                    timestampEpochMs = 1779600010000L
                )
            )
        )

        val encoded = json.encodeToString(batch)
        val decoded = json.decodeFromString<LocationSyncRequest>(encoded)

        assertEquals(2, decoded.locations.size)
        assertEquals(21.4225, decoded.locations[0].latitude, 0.0001)
        assertEquals(39.8263, decoded.locations[1].longitude, 0.0001)
    }

    @Test
    fun `ApiErrorResponse parses correctly from error contract`() {
        val errorJson = """
            {
              "error_code": "RATE_LIMITED",
              "message": "Terlalu banyak permintaan token. Tunggu 60 detik sebelum membuat kode baru."
            }
        """.trimIndent()

        val error = json.decodeFromString<ApiErrorResponse>(errorJson)
        assertEquals("RATE_LIMITED", error.errorCode)
        val enumCode = ApiErrorCode.fromCode(error.errorCode)
        assertEquals(ApiErrorCode.RATE_LIMITED, enumCode)
        assertEquals(429, enumCode.defaultHttpStatus)
    }
}
