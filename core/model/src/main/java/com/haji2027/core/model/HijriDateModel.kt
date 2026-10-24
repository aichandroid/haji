package com.haji2027.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representasi tanggal kalender Hijriah Umm al-Qura terkompensasi manual offset.
 */
@Serializable
data class HijriDateModel(
    @SerialName("day_of_month") val dayOfMonth: Int,
    @SerialName("month_number") val monthNumber: Int,
    @SerialName("month_name") val monthName: String,
    @SerialName("year") val year: Int,
    @SerialName("is_hajj_season") val isHajjSeason: Boolean,
    @SerialName("applied_offset_days") val appliedOffsetDays: Int,
    @SerialName("formatted_date") val formattedDate: String
)
