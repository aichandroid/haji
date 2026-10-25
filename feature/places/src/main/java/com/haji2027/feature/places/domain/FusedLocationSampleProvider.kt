package com.haji2027.feature.places.domain

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementasi [LocationSampleProvider] berbasis Google Play Services [FusedLocationProviderClient].
 * Menggunakan callbackFlow dengan awaitClose untuk mencegah kebocoran listener GPS saat coroutine dibatalkan.
 */
class FusedLocationSampleProvider(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationSampleProvider {

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(intervalMs: Long): Flow<GpsSample> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMaxUpdateDelayMillis(intervalMs)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val sample = GpsSample(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        timestampEpochMs = loc.time
                    )
                    trySend(sample)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}
