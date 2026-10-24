package com.haji2027.core.common

import com.haji2027.core.common.parser.HajjContentParser
import com.haji2027.core.model.RulingCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Pengujian unit untuk [HajjContentParser] memastikan seluruh materi JSON manasik dapat dimuat
 * secara deterministik dan aman.
 */
class HajjContentParserTest {

    @Test
    fun testParseBundledContentJsonSuccessfully() {
        // Baca berkas baseline JSON riil
        val assetFile = File("src/main/assets/content_v1.0.0.json").let {
            if (it.exists()) it else File("../../app/src/main/assets/content_v1.0.0.json")
        }

        assertTrue("Berkas content_v1.0.0.json harus ada", assetFile.exists())
        val jsonString = assetFile.readText(Charsets.UTF_8)

        val parseResult = HajjContentParser.parseJson(jsonString)
        assertTrue("Parsing JSON harus berhasil", parseResult.isSuccess)

        val hajjPackage = parseResult.getOrThrow()
        assertEquals("1.0.0", hajjPackage.version)
        assertEquals(1448, hajjPackage.hajjYear)
        assertEquals("TAMATTU", hajjPackage.hajjTypePrimary)
        assertEquals(8, hajjPackage.days.size)

        // Verifikasi detail Hari Arafah (9 Zulhijah)
        val arafahDay = hajjPackage.days.firstOrNull { it.dayId == "day_09_zulhijah" }
        assertNotNull("Hari Arafah wajib ada dalam dataset", arafahDay)
        assertEquals(9, arafahDay!!.hijriDay)
        assertEquals(12, arafahDay.hijriMonth)

        // Verifikasi rukun Wukuf
        val wukufAct = arafahDay.activities.firstOrNull { it.activityId == "act_wukuf_arafah" }
        assertNotNull("Kegiatan wukuf wajib ada", wukufAct)
        assertEquals(RulingCategory.RUKUN, wukufAct!!.rulingCategory)

        // Verifikasi bacaan doa
        val wukufStep = wukufAct.steps.firstOrNull { it.stepId == "step_arafah_02" }
        assertNotNull(wukufStep)
        assertFalse(wukufStep!!.readings.isEmpty())
        val tahlil = wukufStep.readings.first()
        assertTrue("Teks Arab tahlil harus memuat lafaz tauhid", tahlil.arabicText.contains("لَا إِلَهَ إِلَّا اللَّهُ"))
        assertTrue("Transliterasi Latin harus valid", tahlil.latinTransliteration.startsWith("Laa ilaaha"))
    }

    @Test
    fun testParseInvalidJsonReturnsFailureSafely() {
        val corruptedJson = "{ \"version\": \"1.0.0\", \"days\": [ incomplete "
        val result = HajjContentParser.parseJson(corruptedJson)
        
        assertTrue("Parser harus menangkap error dan mengembalikan failure", result.isFailure)
    }

    @Test
    fun testForwardCompatibilityWithUnknownKeys() {
        val jsonWithFutureFields = """
            {
                "version": "1.1.0",
                "revision_date": "2027-01-01",
                "approved_by": "Ustadz",
                "hajj_year": 1448,
                "unknown_future_field_2028": "extra_value",
                "days": []
            }
        """.trimIndent()

        val result = HajjContentParser.parseJson(jsonWithFutureFields)
        assertTrue("Parser harus forward-compatible dan mengabaikan key baru", result.isSuccess)
        assertEquals("1.1.0", result.getOrThrow().version)
    }
}
