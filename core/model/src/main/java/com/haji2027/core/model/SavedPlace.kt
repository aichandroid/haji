package com.haji2027.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model domain tempat tersimpan (Pintu Masjid, Hotel, Tenda, Bus).
 */
@Serializable
data class SavedPlace(
    @SerialName("place_id") val placeId: String,
    @SerialName("name") val name: String,
    @SerialName("category") val category: PlaceCategory,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("accuracy_meters") val accuracyMeters: Float,
    @SerialName("notes") val notes: String? = null,
    @SerialName("photo_uri") val photoUri: String? = null,
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * Kategori tempat tersimpan.
 */
@Serializable
enum class PlaceCategory {
    @SerialName("DOOR") DOOR,
    @SerialName("HOTEL") HOTEL,
    @SerialName("CAMP") CAMP,
    @SerialName("BUS") BUS,
    @SerialName("OTHER") OTHER
}
