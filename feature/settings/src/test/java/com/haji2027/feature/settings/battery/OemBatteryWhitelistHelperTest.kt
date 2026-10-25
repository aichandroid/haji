package com.haji2027.feature.settings.battery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk deteksi vendor OEM dan pembentukan panduan whitelist baterai.
 * Memverifikasi kepatuhan FR-ONB-004 dan NFR-BAT-001.
 */
class OemBatteryWhitelistHelperTest {

    @Test
    fun fromManufacturer_detectsXiaomiAndSubsidiariesCorrectly() {
        assertEquals(OemVendor.XIAOMI, OemVendor.fromManufacturer("Xiaomi"))
        assertEquals(OemVendor.XIAOMI, OemVendor.fromManufacturer("xiaomi", "Redmi Note 11"))
        assertEquals(OemVendor.XIAOMI, OemVendor.fromManufacturer("Redmi", "Note 10"))
        assertEquals(OemVendor.XIAOMI, OemVendor.fromManufacturer("POCO", "X3 Pro"))
    }

    @Test
    fun fromManufacturer_detectsOppoRealmeOnePlusCorrectly() {
        assertEquals(OemVendor.OPPO, OemVendor.fromManufacturer("OPPO"))
        assertEquals(OemVendor.OPPO, OemVendor.fromManufacturer("oppo", "CPH2211"))
        assertEquals(OemVendor.OPPO, OemVendor.fromManufacturer("Realme"))
        assertEquals(OemVendor.OPPO, OemVendor.fromManufacturer("realme", "RMX3085"))
        assertEquals(OemVendor.OPPO, OemVendor.fromManufacturer("OnePlus"))
    }

    @Test
    fun fromManufacturer_detectsVivoAndIqooCorrectly() {
        assertEquals(OemVendor.VIVO, OemVendor.fromManufacturer("vivo"))
        assertEquals(OemVendor.VIVO, OemVendor.fromManufacturer("Vivo", "V21"))
        assertEquals(OemVendor.VIVO, OemVendor.fromManufacturer("iQOO"))
        assertEquals(OemVendor.VIVO, OemVendor.fromManufacturer("iqoo", "Z7"))
    }

    @Test
    fun fromManufacturer_detectsSamsungCorrectly() {
        assertEquals(OemVendor.SAMSUNG, OemVendor.fromManufacturer("samsung"))
        assertEquals(OemVendor.SAMSUNG, OemVendor.fromManufacturer("Samsung", "SM-A525F"))
    }

    @Test
    fun fromManufacturer_detectsHuaweiAndHonorCorrectly() {
        assertEquals(OemVendor.HUAWEI, OemVendor.fromManufacturer("HUAWEI"))
        assertEquals(OemVendor.HUAWEI, OemVendor.fromManufacturer("huawei", "P30"))
        assertEquals(OemVendor.HUAWEI, OemVendor.fromManufacturer("HONOR"))
    }

    @Test
    fun fromManufacturer_fallsBackToGenericForOtherBrands() {
        assertEquals(OemVendor.GENERIC, OemVendor.fromManufacturer("Google"))
        assertEquals(OemVendor.GENERIC, OemVendor.fromManufacturer("Motorola"))
        assertEquals(OemVendor.GENERIC, OemVendor.fromManufacturer("Sony"))
        assertEquals(OemVendor.GENERIC, OemVendor.fromManufacturer(""))
    }

    @Test
    fun instructions_containClearStepsForEachVendor() {
        OemVendor.entries.forEach { vendor ->
            assertNotNull(vendor.displayName)
            assertTrue(vendor.displayName.isNotBlank())
            assertTrue(
                "Vendor ${vendor.name} harus memiliki minimal 3 langkah instruksi",
                vendor.instructions.size >= 3
            )
            vendor.instructions.forEach { step ->
                assertTrue(
                    "Langkah instruksi tidak boleh kosong pada vendor ${vendor.name}",
                    step.isNotBlank()
                )
            }
        }
    }

    @Test
    fun createVendorIntents_providesOrderedCandidates() {
        val packageName = "com.haji2027.app"
        val xiaomiIntents = OemBatteryWhitelistHelper.createVendorIntents(packageName, OemVendor.XIAOMI)

        // Harus memiliki intent spesifik Xiaomi ditambah fallback standar
        assertTrue(xiaomiIntents.size >= 4)

        // Verifikasi intent pertama mengarah ke auto-start MIUI
        val firstIntent = xiaomiIntents.first()
        assertEquals(
            "com.miui.securitycenter",
            firstIntent.component?.packageName
        )
        assertEquals(
            "com.miui.permcenter.autostart.AutoStartManagementActivity",
            firstIntent.component?.className
        )
    }

    @Test
    fun createVendorIntents_genericContainsStandardFallbacks() {
        val packageName = "com.haji2027.app"
        val genericIntents = OemBatteryWhitelistHelper.createVendorIntents(packageName, OemVendor.GENERIC)

        // Generic harus memiliki minimal 3 fallback standar AOSP
        assertTrue(genericIntents.size >= 3)
    }
}
