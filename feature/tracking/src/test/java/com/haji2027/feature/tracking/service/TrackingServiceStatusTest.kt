package com.haji2027.feature.tracking.service

import com.haji2027.core.model.UserLocation
import com.haji2027.feature.tracking.domain.MovementMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingServiceStatusTest {

    @Test
    fun `default TrackingServiceStatus has expected initial values`() {
        val status = TrackingServiceStatus()

        assertFalse(status.isRunning)
        assertEquals(MovementMode.MOVING, status.movementMode)
        assertNull(status.lastLocation)
        assertEquals(0L, status.startedAtEpochMs)
        assertEquals(0L, status.totalUpdatesCount)
    }

    @Test
    fun `TrackingServiceStatus copy updates values accurately`() {
        val loc = UserLocation(
            userId = "usr_ayah_123",
            latitude = 21.4225,
            longitude = 39.8262,
            accuracyMeters = 4.5f,
            updatedAtEpochMs = 1779600000000L
        )

        val status = TrackingServiceStatus().copy(
            isRunning = true,
            movementMode = MovementMode.STATIONARY,
            lastLocation = loc,
            startedAtEpochMs = 1779600000000L,
            totalUpdatesCount = 10L
        )

        assertTrue(status.isRunning)
        assertEquals(MovementMode.STATIONARY, status.movementMode)
        assertEquals(loc, status.lastLocation)
        assertEquals(10L, status.totalUpdatesCount)
    }

    @Test
    fun `TrackingController exposes status flow`() {
        val controller: TrackingController = DefaultTrackingController()
        assertEquals(LocationTrackingService.serviceStatus.value, controller.status.value)
    }
}
