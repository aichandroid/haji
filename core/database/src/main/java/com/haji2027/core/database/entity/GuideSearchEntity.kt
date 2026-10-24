package com.haji2027.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.PrimaryKey

/**
 * Tabel virtual FTS4 Room untuk pencarian konten panduan manasik haji.
 * Menggunakan tokenizer unicode61 untuk mendukung pencarian teks Latin dan Arab secara cepat (latensi <= 80ms).
 */
@Entity(tableName = "guide_search_fts")
@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61)
data class GuideSearchEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowId: Int = 0,

    @ColumnInfo(name = "content_id")
    val contentId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "body_text")
    val bodyText: String,

    @ColumnInfo(name = "arabic_text")
    val arabicText: String
)
