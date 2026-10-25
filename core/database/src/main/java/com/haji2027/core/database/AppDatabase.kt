package com.haji2027.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.haji2027.core.database.dao.ChecklistDao
import com.haji2027.core.database.dao.GuideSearchDao
import com.haji2027.core.database.dao.PlacesDao
import com.haji2027.core.database.entity.ChecklistEntity
import com.haji2027.core.database.entity.GuideSearchEntity
import com.haji2027.core.database.entity.SavedPlaceEntity

/**
 * Basis data SQLite lokal aplikasi Panduan Haji 2027 berbasis Jetpack Room.
 * Berjalan 100% offline tanpa membutuhkan akses jaringan.
 */
@Database(
    entities = [
        ChecklistEntity::class,
        GuideSearchEntity::class,
        SavedPlaceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun checklistDao(): ChecklistDao
    abstract fun guideSearchDao(): GuideSearchDao
    abstract fun placesDao(): PlacesDao

    companion object {
        const val DATABASE_NAME = "haji_2027.db"
    }
}
