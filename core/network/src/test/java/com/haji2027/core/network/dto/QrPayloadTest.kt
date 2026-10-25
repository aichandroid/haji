package com.haji2027.core.network.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QrPayloadTest {

    @Test
    fun `createUriString formats correct hajj2027 pair URI`() {
        val uri = QrPayload.createUriString(
            tokenId = "tok_9f8c2e1b4a7d",
            targetUid = "usr_ayah_123"
        )
        assertEquals("hajj2027://pair?t=tok_9f8c2e1b4a7d&u=usr_ayah_123", uri)
    }

    @Test
    fun `toUriString on QrPayload returns matching string`() {
        val payload = QrPayload(tokenId = "tok_abc", targetUid = "usr_xyz")
        assertEquals("hajj2027://pair?t=tok_abc&u=usr_xyz", payload.toUriString())
    }

    @Test
    fun `parse extracts tokenId and targetUid successfully`() {
        val qrString = "hajj2027://pair?t=tok_9f8c2e1b4a7d&u=usr_ayah_123"
        val result = QrPayload.parse(qrString)

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("tok_9f8c2e1b4a7d", payload.tokenId)
        assertEquals("usr_ayah_123", payload.targetUid)
    }

    @Test
    fun `parse trims surrounding whitespace`() {
        val qrString = "   hajj2027://pair?t=tok_123&u=usr_456 \n  "
        val result = QrPayload.parse(qrString)

        assertTrue(result.isSuccess)
        assertEquals("tok_123", result.getOrThrow().tokenId)
        assertEquals("usr_456", result.getOrThrow().targetUid)
    }

    @Test
    fun `parse handles swapped query parameter order`() {
        val qrString = "hajj2027://pair?u=usr_ayah_123&t=tok_9f8c2e1b4a7d"
        val result = QrPayload.parse(qrString)

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("tok_9f8c2e1b4a7d", payload.tokenId)
        assertEquals("usr_ayah_123", payload.targetUid)
    }

    @Test
    fun `parse fails when scheme is not hajj2027`() {
        val invalidScheme = "https://pair?t=tok_123&u=usr_456"
        val result = QrPayload.parse(invalidScheme)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `parse fails when query parameters are missing`() {
        val missingParams = "hajj2027://pair"
        val result = QrPayload.parse(missingParams)

        assertTrue(result.isFailure)
    }

    @Test
    fun `parse fails when tokenId is missing`() {
        val missingToken = "hajj2027://pair?u=usr_ayah_123"
        val result = QrPayload.parse(missingToken)

        assertTrue(result.isFailure)
    }

    @Test
    fun `parse fails when targetUid is missing`() {
        val missingUser = "hajj2027://pair?t=tok_123"
        val result = QrPayload.parse(missingUser)

        assertTrue(result.isFailure)
    }

    @Test
    fun `parse fails on null or blank input`() {
        assertTrue(QrPayload.parse(null).isFailure)
        assertTrue(QrPayload.parse("   ").isFailure)
    }
}
