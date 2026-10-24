package com.haji2027.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paket data panduan manasik haji berversi.
 */
@Serializable
data class HajjContentPackage(
    @SerialName("version") val version: String,
    @SerialName("revision_date") val revisionDate: String,
    @SerialName("approved_by") val approvedBy: String,
    @SerialName("hajj_year") val hajjYear: Int,
    @SerialName("hajj_type_primary") val hajjTypePrimary: String = "TAMATTU",
    @SerialName("notes") val notes: String? = null,
    @SerialName("days") val days: List<HajjDay> = emptyList()
)

/**
 * Hari atau fase pelaksanaan manasik haji/umrah.
 */
@Serializable
data class HajjDay(
    @SerialName("day_id") val dayId: String,
    @SerialName("hijri_day") val hijriDay: Int,
    @SerialName("hijri_month") val hijriMonth: Int,
    @SerialName("title") val title: String,
    @SerialName("location") val location: String,
    @SerialName("summary") val summary: String,
    @SerialName("activities") val activities: List<ManasikActivity> = emptyList()
)

/**
 * Kegiatan manasik tertentu di dalam satu hari.
 */
@Serializable
data class ManasikActivity(
    @SerialName("activity_id") val activityId: String,
    @SerialName("sequence_order") val sequenceOrder: Int,
    @SerialName("title") val title: String,
    @SerialName("ruling_category") val rulingCategory: RulingCategory = RulingCategory.SUNAH,
    @SerialName("time_window") val timeWindow: String? = null,
    @SerialName("location_notes") val locationNotes: String? = null,
    @SerialName("steps") val steps: List<ManasikStep> = emptyList()
)

/**
 * Kategori status hukum ibadah.
 */
@Serializable
enum class RulingCategory {
    @SerialName("RUKUN") RUKUN,
    @SerialName("WAJIB") WAJIB,
    @SerialName("SUNAH") SUNAH
}

/**
 * Langkah rinci dari suatu kegiatan manasik.
 */
@Serializable
data class ManasikStep(
    @SerialName("step_id") val stepId: String,
    @SerialName("order") val order: Int,
    @SerialName("instruction") val instruction: String,
    @SerialName("prohibitions") val prohibitions: List<String> = emptyList(),
    @SerialName("readings") val readings: List<DoaReading> = emptyList(),
    @SerialName("differences_note") val differencesNote: DifferencesNote? = null
)

/**
 * Bacaan doa, zikir, atau talbiah multilingual.
 */
@Serializable
data class DoaReading(
    @SerialName("reading_id") val readingId: String,
    @SerialName("title") val title: String,
    @SerialName("arabic_text") val arabicText: String,
    @SerialName("latin_transliteration") val latinTransliteration: String,
    @SerialName("translation_id") val translationId: String,
    @SerialName("source_ref") val sourceRef: String? = null
)

/**
 * Catatan perbedaan pendapat fikih (ikhtilaf).
 */
@Serializable
data class DifferencesNote(
    @SerialName("has_ikhtilaf") val hasIkhtilaf: Boolean = false,
    @SerialName("summary") val summary: String,
    @SerialName("opinions") val opinions: List<IkhtilafOpinion> = emptyList()
)

/**
 * Rincian satu pendapat fikih dari sumber rujukan.
 */
@Serializable
data class IkhtilafOpinion(
    @SerialName("source_ref") val sourceRef: String,
    @SerialName("opinion") val opinion: String
)
