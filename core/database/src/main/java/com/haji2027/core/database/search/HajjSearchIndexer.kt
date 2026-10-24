package com.haji2027.core.database.search

import com.haji2027.core.database.entity.GuideSearchEntity
import com.haji2027.core.model.HajjContentPackage

/**
 * Pengindeks data konten manasik haji ke format entitas FTS4 SQLite.
 * Mengubah seluruh struktur hari, aktivitas, langkah (termasuk larangan & catatan ikhtilaf),
 * serta doa/bacaan menjadi entitas pencarian terindeks.
 */
object HajjSearchIndexer {

    fun buildIndex(contentPackage: HajjContentPackage): List<GuideSearchEntity> {
        val results = mutableListOf<GuideSearchEntity>()
        val seenDoaIds = mutableSetOf<String>()

        for (day in contentPackage.days) {
            // 1. Hari / Fase Manasik
            val hijriInfo = "${day.hijriDay}/${day.hijriMonth} H"
            results.add(
                GuideSearchEntity(
                    contentId = "day_${day.dayId}",
                    title = "${day.title} ($hijriInfo)",
                    bodyText = "${day.location}. ${day.summary}",
                    arabicText = ""
                )
            )

            // 2. Aktivitas Manasik
            for (activity in day.activities) {
                val locNote = activity.locationNotes?.let { " - $it" } ?: ""
                val timeWin = activity.timeWindow?.let { " ($it)" } ?: ""
                results.add(
                    GuideSearchEntity(
                        contentId = "activity_${activity.activityId}",
                        title = "${activity.title}$timeWin",
                        bodyText = "Hukum: ${activity.rulingCategory}$locNote",
                        arabicText = ""
                    )
                )

                // 3. Langkah Manasik & Ikhtilaf
                for (step in activity.steps) {
                    val prohibitionsText = if (step.prohibitions.isNotEmpty()) {
                        " Larangan: " + step.prohibitions.joinToString(", ")
                    } else ""

                    val ikhtilafText = step.differencesNote?.let { diff ->
                        val opinions = diff.opinions.joinToString("; ") { "${it.sourceRef}: ${it.opinion}" }
                        " Ikhtilaf: ${diff.summary}. Pendapat: $opinions"
                    } ?: ""

                    results.add(
                        GuideSearchEntity(
                            contentId = "step_${step.stepId}",
                            title = "${activity.title}: Langkah ${step.order}",
                            bodyText = "${step.instruction}$prohibitionsText$ikhtilafText",
                            arabicText = ""
                        )
                    )

                    // 4. Doa / Bacaan Terkait Langkah
                    for (doa in step.readings) {
                        if (seenDoaIds.add(doa.readingId)) {
                            val refText = doa.sourceRef?.let { " Sumber: $it" } ?: ""
                            results.add(
                                GuideSearchEntity(
                                    contentId = "doa_${doa.readingId}",
                                    title = "Doa: ${doa.title}",
                                    bodyText = "${doa.latinTransliteration} ${doa.translationId}$refText",
                                    arabicText = doa.arabicText
                                )
                            )
                        }
                    }
                }
            }
        }

        return results
    }
}
