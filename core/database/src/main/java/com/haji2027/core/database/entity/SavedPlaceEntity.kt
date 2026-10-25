package com.haji2027.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import java.util.UUID

/**
 * Entitas Room untuk tabel `saved_places`.
 * Menyimpan tempat penting jemaah (Pintu Masjid, Hotel, Tenda Mina/Arafah, Bus, dll.)
 * secara persisten dan 100% offline di perangkat.
 */
@Entity(
    tableName = "saved_places",
    indices = [
        Index(value = ["category"], name = "idx_saved_places_category")
    ]
)
data class SavedPlaceEntity(
    @PrimaryKey
    @ColumnInfo(name = "place_id")
    val placeId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String, // DOOR, HOTEL, CAMP, BUS, OTHER

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "accuracy_meters")
    val accuracyMeters: Float,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long = System.currentTimeMillis()
) {
    /**
     * Konversi entitas Room ke domain model [SavedPlace].
     */
    fun toDomain(): SavedPlace = SavedPlace(
        placeId = placeId,
        name = name,
        category = try {
            PlaceCategory.valueOf(category)
        } catch (_: IllegalArgumentException) {
            PlaceCategory.OTHER
        },
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        notes = notes,
        photoUri = photoUri,
        createdAtEpochMs = createdAtEpochMs
    )

    companion object {
        /**
         * Konversi domain model [SavedPlace] ke entitas Room [SavedPlaceEntity].
         */
        fun fromDomain(domain: SavedPlace): SavedPlaceEntity = SavedPlaceEntity(
            placeId = domain.placeId,
            name = domain.name,
            category = domain.category.name,
            latitude = domain.latitude,
            longitude = domain.longitude,
            accuracyMeters = domain.accuracyMeters,
            notes = domain.notes,
            photoUri = domain.photoUri,
            createdAtEpochMs = domain.createdAtEpochMs
        )
    }
}
