package com.haji2027.core.database.search

import com.haji2027.core.common.parser.HajjContentParser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HajjSearchIndexerTest {

    @Test
    fun `buildIndex correctly indexes all days activities steps and doas`() {
        val assetFile = File("../../app/src/main/assets/content_v1.0.0.json")
            .takeIf { it.exists() }
            ?: File("app/src/main/assets/content_v1.0.0.json")

        assertTrue("Asset file content_v1.0.0.json must exist", assetFile.exists())

        val jsonString = assetFile.readText()
        val contentPackage = HajjContentParser.parse(jsonString)

        val indexList = HajjSearchIndexer.buildIndex(contentPackage)

        // Verifikasi keberadaan indeks
        assertFalse("Index list should not be empty", indexList.isEmpty())

        // Verifikasi indeks Hari
        val dayEntries = indexList.filter { it.contentId.startsWith("day_") }
        assertTrue("Must index at least 8 days", dayEntries.size >= 8)
        assertTrue(dayEntries.any { it.title.contains("Arafah", ignoreCase = true) })

        // Verifikasi indeks Aktivitas
        val activityEntries = indexList.filter { it.contentId.startsWith("activity_") }
        assertTrue("Must index activities", activityEntries.isNotEmpty())

        // Verifikasi indeks Langkah & Catatan Ikhtilaf
        val stepEntries = indexList.filter { it.contentId.startsWith("step_") }
        assertTrue("Must index steps", stepEntries.isNotEmpty())
        val ikhtilafIndexed = stepEntries.any { it.bodyText.contains("Ikhtilaf", ignoreCase = true) }
        assertTrue("Must index at least one ikhtilaf note in step body", ikhtilafIndexed)

        // Verifikasi indeks Doa & Teks Arab
        val doaEntries = indexList.filter { it.contentId.startsWith("doa_") }
        assertTrue("Must index doas", doaEntries.isNotEmpty())
        for (doa in doaEntries) {
            assertTrue("Doa title must start with Doa:", doa.title.startsWith("Doa:"))
            assertFalse("Arabic text must not be empty", doa.arabicText.isBlank())
        }
    }
}
