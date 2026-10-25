package com.haji2027.feature.places.domain

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementasi [OrientationSensorProvider] berbasis hardware [SensorManager] Android.
 * Mengutamakan [Sensor.TYPE_ROTATION_VECTOR] (fusi perangkat keras) dengan fallback
 * ke fusi akselerometer + magnetometer manual jika rotation vector tidak tersedia.
 *
 * Menerapkan Exponential Moving Average (EMA) smoothing untuk meredam jitter/getaran jarum panah.
 */
class AndroidOrientationSensorProvider(
    private val sensorManager: SensorManager
) : OrientationSensorProvider {

    companion object {
        // Faktor peredaman jitter (0.0 = beku, 1.0 = tanpa filter). 0.25f memberikan stabilitas tinggi untuk lansia.
        private const val SMOOTHING_ALPHA = 0.25f
    }

    override fun observeOrientation(declinationDegrees: Float): Flow<CompassOrientation> = callbackFlow {
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var lastSmoothedAzimuth = 0f
        var hasInitialAzimuth = false

        val gravityValues = FloatArray(3)
        val geomagneticValues = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var rawAzimuth: Float? = null
                var pitch = 0f
                var roll = 0f

                val accuracy = when (event.accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> CompassAccuracy.LOW
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> CompassAccuracy.UNRELIABLE
                    else -> CompassAccuracy.HIGH
                }

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientationValues = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationValues)

                    // Azimuth dalam derajat [0, 360)
                    rawAzimuth = BearingCalculator.normalizeDegrees(Math.toDegrees(orientationValues[0].toDouble()).toFloat())
                    pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
                    roll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()
                } else {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, gravityValues, 0, 3)
                        hasGravity = true
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
                        hasGeomagnetic = true
                    }

                    if (hasGravity && hasGeomagnetic) {
                        val rotationMatrix = FloatArray(9)
                        val inclinationMatrix = FloatArray(9)
                        val success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityValues, geomagneticValues)
                        if (success) {
                            val orientationValues = FloatArray(3)
                            SensorManager.getOrientation(rotationMatrix, orientationValues)
                            rawAzimuth = BearingCalculator.normalizeDegrees(Math.toDegrees(orientationValues[0].toDouble()).toFloat())
                            pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
                            roll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()
                        }
                    }
                }

                rawAzimuth?.let { currentRaw ->
                    val smoothedAzimuth = if (!hasInitialAzimuth) {
                        hasInitialAzimuth = true
                        lastSmoothedAzimuth = currentRaw
                        currentRaw
                    } else {
                        // Smoothing sirkular memanfaatkan shortestAngleDiff
                        val diff = BearingCalculator.shortestAngleDiff(lastSmoothedAzimuth, currentRaw)
                        val next = BearingCalculator.normalizeDegrees(lastSmoothedAzimuth + (diff * SMOOTHING_ALPHA))
                        lastSmoothedAzimuth = next
                        next
                    }

                    val trueAzimuth = BearingCalculator.calculateTrueAzimuth(smoothedAzimuth, declinationDegrees)

                    val orientation = CompassOrientation(
                        magneticAzimuth = smoothedAzimuth,
                        trueAzimuth = trueAzimuth,
                        declination = declinationDegrees,
                        pitch = pitch,
                        roll = roll,
                        accuracy = accuracy,
                        timestampEpochMs = System.currentTimeMillis()
                    )
                    trySend(orientation)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Diproses di onSensorChanged
            }
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            if (accelerometer != null) {
                sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }
            if (magnetometer != null) {
                sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
            }
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
