package com.haji2027.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit navigasi root dan rute modular aplikasi Panduan Haji 2027.
 */
class RootNavigationTest {

    @Test
    fun `testAllAppTabsAreProperlyConfigured`() {
        val tabs = AppTab.entries
        assertEquals(4, tabs.size)

        assertEquals("Panduan", AppTab.GUIDE.title)
        assertEquals("Tempat Saya", AppTab.PLACES.title)
        assertEquals("Lacak", AppTab.TRACKING.title)
        assertEquals("Pengaturan", AppTab.SETTINGS.title)

        tabs.forEach { tab ->
            assertNotNull(tab.icon)
            assertTrue("Deskripsi konten tidak boleh kosong", tab.contentDescription.isNotBlank())
        }
    }

    @Test
    fun `testGuideRouteVariants`() {
        val home: GuideRoute = GuideRoute.Home
        val detail: GuideRoute = GuideRoute.Detail("step_tawaf_wada")
        val doa: GuideRoute = GuideRoute.Doa("doa_sai")

        assertTrue(home is GuideRoute.Home)
        assertTrue(detail is GuideRoute.Detail)
        assertEquals("step_tawaf_wada", (detail as GuideRoute.Detail).stepId)
        assertTrue(doa is GuideRoute.Doa)
        assertEquals("doa_sai", (doa as GuideRoute.Doa).doaId)
    }

    @Test
    fun `testPlacesRouteVariants`() {
        val list: PlacesRoute = PlacesRoute.List
        val compass: PlacesRoute = PlacesRoute.Compass("place_hotel_safwah")

        assertTrue(list is PlacesRoute.List)
        assertTrue(compass is PlacesRoute.Compass)
        assertEquals("place_hotel_safwah", (compass as PlacesRoute.Compass).placeId)
    }
}
