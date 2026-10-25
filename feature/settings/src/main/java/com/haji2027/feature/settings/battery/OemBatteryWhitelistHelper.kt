package com.haji2027.feature.settings.battery

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Utilitas deteksi produsen perangkat dan pembuat intent halaman setelan baterai OEM.
 * Memenuhi kebutuhan FR-ONB-004 dan NFR-BAT-001.
 */
object OemBatteryWhitelistHelper {

    /**
     * Mendeteksi apakah aplikasi saat ini telah masuk dalam daftar pengecualian optimasi baterai sistem.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Menghasilkan daftar kandidat Intent untuk membuka halaman pengaturan baterai vendor terkait,
     * diurutkan dari yang paling spesifik hingga intent fallback standar Android.
     */
    fun createVendorIntents(context: Context, vendor: OemVendor): List<Intent> {
        return createVendorIntents(context.packageName, vendor)
    }

    /**
     * Overload mandiri berbasis nama paket untuk kemudahan pengujian unit dan fleksibilitas pemanggilan.
     */
    @SuppressLint("BatteryLife")
    fun createVendorIntents(packageName: String, vendor: OemVendor): List<Intent> {
        val intents = mutableListOf<Intent>()

        when (vendor) {
            OemVendor.XIAOMI -> {
                // 1. Auto-start management MIUI
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    )
                )
                // 2. MIUI Power hide mode (Battery Saver No Restrictions)
                intents.add(
                    Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST")
                        .addCategory(Intent.CATEGORY_DEFAULT)
                )
                // 3. MIUI Security Center App Details
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.appmanager.ApplicationsDetailsActivity"
                        )
                    ).putExtra("package_name", packageName)
                )
            }

            OemVendor.OPPO -> {
                // 1. ColorOS Startup App List
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    )
                )
                // 2. Oppo Safe Startup List
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.oppo.safe",
                            "com.oppo.safe.permission.startup.StartupAppListActivity"
                        )
                    )
                )
                // 3. ColorOS Startup App Activity Alternatif
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.startupapp.StartupAppListActivity"
                        )
                    )
                )
            }

            OemVendor.VIVO -> {
                // 1. iQOO / Vivo Background StartUp Manager
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                        )
                    )
                )
                // 2. Vivo Permission Manager
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                    )
                )
                // 3. Vivo High Power Consumption Activity
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                        )
                    )
                )
            }

            OemVendor.SAMSUNG -> {
                // 1. Samsung Device Care Battery UI
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.samsung.android.lool",
                            "com.samsung.android.sm.battery.ui.BatteryActivity"
                        )
                    )
                )
                // 2. Samsung Smart Manager Battery
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.samsung.android.sm",
                            "com.samsung.android.sm.battery.ui.BatteryActivity"
                        )
                    )
                )
            }

            OemVendor.HUAWEI -> {
                // 1. Huawei Protected Apps
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.optimize.process.ProtectActivity"
                        )
                    )
                )
                // 2. Huawei Startup App Control
                intents.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                        )
                    )
                )
            }

            OemVendor.GENERIC -> {
                // Tidak ada intent proprietary spesifik, langsung ke fallback AOSP
            }
        }

        // --- FALLBACK INTENTS STANDAR ANDROID ---
        // Fallback 1: Dialog permintaan ignore battery optimization aplikasi saat ini
        intents.add(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
        )

        // Fallback 2: Daftar umum optimasi baterai
        intents.add(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))

        // Fallback 3: Halaman Info Aplikasi (App Details)
        intents.add(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
        )

        // Pastikan setiap intent siap diluncurkan di luar Activity context
        intents.forEach { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        return intents
    }

    /**
     * Membuka halaman pengaturan baterai vendor terbaik yang didukung oleh perangkat.
     * Mengembalikan true jika berhasil meluncurkan salah satu Activity, atau false jika seluruhnya gagal.
     */
    fun openBatteryOptimization(
        context: Context,
        vendor: OemVendor = OemVendor.fromManufacturer(Build.MANUFACTURER, Build.BRAND)
    ): Boolean {
        val intents = createVendorIntents(context, vendor)
        for (intent in intents) {
            try {
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (_: Exception) {
                // Abaikan ActivityNotFoundException atau SecurityException, coba kandidat fallback berikutnya
            }
        }

        // Upaya darurat terakhir: langsung luncurkan fallback details settings
        return try {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
