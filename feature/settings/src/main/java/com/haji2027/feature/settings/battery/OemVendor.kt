package com.haji2027.feature.settings.battery

/**
 * Representasi produsen perangkat (OEM) Android untuk kustomisasi panduan whitelist baterai.
 * Memenuhi kebutuhan FR-ONB-004 dan mitigasi pembunuhan agresif latar belakang oleh OS vendor.
 */
enum class OemVendor(
    val displayName: String,
    val instructions: List<String>
) {
    XIAOMI(
        displayName = "Xiaomi / Redmi / POCO (MIUI / HyperOS)",
        instructions = listOf(
            "1. Buka menu 'Mulai Otomatis' (Auto-start) dan aktifkan untuk Panduan Haji 2027.",
            "2. Buka menu 'Penghemat Baterai' lalu pilih 'Tidak Ada Pembatasan' (No Restrictions).",
            "3. Buka layar Recent Apps, tahan kartu aplikasi Panduan Haji lalu ketuk ikon Gembok (Lock)."
        )
    ),
    OPPO(
        displayName = "OPPO / Realme (ColorOS / Realme UI)",
        instructions = listOf(
            "1. Buka 'Kelola Izin Aplikasi' -> aktifkan 'Mulai Otomatis' (Auto-launch).",
            "2. Pada menu Konsumsi Baterai, izinkan 'Aktivitas Latar Belakang Penuh' (Allow background activity).",
            "3. Kunci aplikasi pada layar Recent Apps agar tidak tertutup saat pembersihan memori."
        )
    ),
    VIVO(
        displayName = "Vivo / iQOO (FuntouchOS / OriginOS)",
        instructions = listOf(
            "1. Buka 'Pengelola Izin' -> 'Manajemen Mulai Otomatis' -> aktifkan Panduan Haji 2027.",
            "2. Buka 'Konsumsi Daya Latar Belakang Tinggi' lalu pilih 'Izinkan Penggunaan Daya Latar Belakang'.",
            "3. Kunci aplikasi di daftar Recent Apps."
        )
    ),
    SAMSUNG(
        displayName = "Samsung (One UI)",
        instructions = listOf(
            "1. Buka 'Pengaturan' -> 'Perawatan Perangkat' -> 'Baterai' -> 'Batas Penggunaan Latar Belakang'.",
            "2. Pastikan Panduan Haji TIDAK ADA di dalam 'Aplikasi yang tidur' (Sleeping apps).",
            "3. Masukkan Panduan Haji ke dalam daftar 'Aplikasi yang tidak pernah tidur' (Never sleeping apps)."
        )
    ),
    HUAWEI(
        displayName = "Huawei / Honor (EMUI / MagicOS)",
        instructions = listOf(
            "1. Buka 'Pengaturan' -> 'Baterai' -> 'Peluncuran Aplikasi' (App Launch).",
            "2. Cari Panduan Haji, ubah dari 'Kelola Otomatis' menjadi 'Kelola Manual'.",
            "3. Aktifkan ketiga opsi: Mulai Otomatis, Mulai Sekunder, dan Berjalan di Latar Belakang."
        )
    ),
    GENERIC(
        displayName = "Android Standar / Merk Lainnya",
        instructions = listOf(
            "1. Buka 'Pengaturan' -> 'Aplikasi' -> 'Panduan Haji 2027' -> 'Baterai'.",
            "2. Ubah mode penggunaan baterai menjadi 'Tidak Dibatasi' (Unrestricted).",
            "3. Izinkan aplikasi berjalan di latar belakang secara bebas tanpa optimasi daya."
        )
    );

    companion object {
        /**
         * Mendeteksi kategori vendor berdasarkan string [Build.MANUFACTURER] dan [Build.BRAND].
         */
        fun fromManufacturer(manufacturer: String, brand: String = ""): OemVendor {
            val combined = "${manufacturer.lowercase()} ${brand.lowercase()}"
            return when {
                combined.contains("xiaomi") || combined.contains("redmi") || combined.contains("poco") -> XIAOMI
                combined.contains("oppo") || combined.contains("realme") || combined.contains("oneplus") -> OPPO
                combined.contains("vivo") || combined.contains("iqoo") -> VIVO
                combined.contains("samsung") -> SAMSUNG
                combined.contains("huawei") || combined.contains("honor") -> HUAWEI
                else -> GENERIC
            }
        }
    }
}
