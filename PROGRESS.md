# PROGRESS TRACKER — PENGEMBANGAN APLIKASI PANDUAN HAJI 2027
**Target Rilis:** Musim Haji 1448 H (Mei – Juni 2027)  
**Platform:** Android Native (Kotlin · Jetpack Compose · Offline-First)  
**Single Source of Truth Desain:** `SYSTEM_DESIGN_DOCUMENT.md`  
**Terakhir Diperbarui:** 24 Oktober 2026  

---

## 1. STATUS RINGKASAN TAHAPAN

| Fase / Task | Nama Tahapan / Irisan Vertikal | Status | Tanggal Selesai | Catatan Kunci |
| :--- | :--- | :---: | :---: | :--- |
| **Tahap I** | Analisis Kebutuhan & Gap Analysis (`Konsep2.md` & `ringkasan.md`) | ✅ **SELESAI** | 24-10-2026 | Pemetaan 25+ FR, 10 titik ikhtilaf fikih, batasan non-goals. |
| **Tahap II** | Analisis Risiko, Asumsi, & ADR | ✅ **SELESAI** | 24-10-2026 | ADR-001 (Compose), ADR-002 (Firebase), ADR-003 (Dual-Layer JSON). |
| **Tahap III**| Arsitektur Sistem & Offline-First C4 | ✅ **SELESAI** | 24-10-2026 | Diagram Konteks, Diagram Kontainer, Arsitektur Multi-Module. |
| **Tahap IV** | Desain Teknis per Fitur & State Machine | ✅ **SELESAI** | 24-10-2026 | Formula panah $A_t = (A_m + D + 360) \pmod{360}$, GPS sampling. |
| **Tahap V**  | Model Data, Skema Room, & Kontrak API | ✅ **SELESAI** | 24-10-2026 | 12 Kamus Data, DDL Room, Firestore Rules, 4 Cloud Functions. |
| **Tahap VI** | Desain UI/UX & Interaksi (UI Pro Max) | ✅ **SELESAI** | 24-10-2026 | Standar lansia WCAG AAA, sentuh 56dp, 6 Wireframe ASCII. |
| **Tahap VII**| Tata Kelola Konten & Validasi 10 Ikhtilaf | ✅ **SELESAI** | 24-10-2026 | Siklus review ustadz, disclaimer hukum, independensi fikih. |
| **Tahap VIII**| Keamanan, Privasi (STRIDE & UU PDP) | ✅ **SELESAI** | 24-10-2026 | STRIDE model, Zero-secret APK, TTL 24h lokasi, Right to be Forgotten. |
| **Tahap IX** | Strategi Pengujian (Pyramid & 15 Test Cases)| ✅ **SELESAI** | 24-10-2026 | 15 Test Cases P0/P1, benchmark baterai 4 jam, field test. |
| **Tahap X**  | Technical & Security Audit Checklist | ✅ **SELESAI** | 24-10-2026 | Checklist pra-rilis APK, verifikasi tanda tangan offline keystore. |
| **Tahap XI** | Roadmap & AI Agent Task Breakdown | ✅ **SELESAI** | 24-10-2026 | 18 Tugas berurutan (*thin vertical slices*) berbasis dependensi. |
| **Tahap XII**| CI/CD, Sideloading, Runbook, & Biaya | ✅ **SELESAI** | 24-10-2026 | Skenario biaya < $30, RTM lengkap, Glosarium manasik/teknis. |
| **Fase 0**   | Baseline Konten Manasik (`content_v1.0.0.json`)| ✅ **SELESAI** | 24-10-2026 | Berkas JSON valid 8 hari manasik, doa Arab, & 10 ikhtilaf. |
| **TASK-01**  | Setup Multi-Module & Design System | ✅ **SELESAI** | 24-10-2026 | Scaffolding 9 modul Gradle, Version Catalog, Design Tokens. |
| **TASK-02**  | Model Domain Manasik & Parser JSON | ✅ **SELESAI** | 24-10-2026 | Model domain `:core:model` & parser tangguh `:core:common`. |
| **TASK-03**  | Mesin Kalender Hijrah & Date Override | ✅ **SELESAI** | 24-10-2026 | `HijriCalendarEngine` Umm al-Qura + manual offset (-3..+3). |
| **TASK-04**  | Room DB: Checklist & FTS4 Search Table | 🟡 **NEXT** | - | Modul `:core:database` (Room SQLite). |

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-03: Mesin Kalender Hijrah & Date Override (Selesai pada Commit: d1f048d)
- **Model Domain `:core:model`:**
  - `HijriDateModel.kt`: Representasi terstruktur tanggal Hijriah Umm al-Qura (`dayOfMonth`, `monthNumber`, `monthName`, `year`, `isHajjSeason`, `appliedOffsetDays`, `formattedDate`).
- **Mesin Kalender `:core:common`:**
  - `HijriCalendarEngine.kt`:
    - Menggunakan kalender Umm al-Qura bawaan Java/Android (`java.time.chrono.HijrahChronology.INSTANCE`).
    - Mendukung zona waktu Arab Saudi (`Asia/Riyadh` / UTC+3).
    - Mendukung manual date offset (-3 s.d. +3 hari) untuk mengantisipasi perbedaan rukyatul hilal resmi Kerajaan Arab Saudi.
    - Fungsi pembantu deteksi hari puncak manasik haji (`isPeakHajjDays`, 8..13 Zulhijah).
  - `HijriCalendarEngineTest.kt`: Unit test menyeluruh untuk konversi tanggal Masehi ke Hijriah, pengujian offset positif/negatif, pembatasan rentang offset (*clamping* safety), format tanggal Bahasa Indonesia, dan deteksi hari Tarwiyah/Arafah/Nahar/Tasyrik.

### TASK-02: Model Domain Manasik & Parser JSON (Selesai pada Commit: 47993f9)
- **Modul Domain `:core:model`:**
  - `HajjContent.kt`: Domain data classes `@Serializable` untuk `HajjContentPackage`, `HajjDay`, `ManasikActivity`, `RulingCategory` (RUKUN, WAJIB, SUNAH), `ManasikStep`, `DoaReading`, dan `DifferencesNote` (penanganan ikhtilaf fikih).
  - `SavedPlace.kt`: Model data `SavedPlace` dan `PlaceCategory` (DOOR, HOTEL, CAMP, BUS, OTHER).
  - `TrackingModels.kt`: Model data `PairingToken` (TTL 5 menit), `TrackingRelation`, `UserLocation`, dan `RelativeTarget`.
  - `ChecklistProgress.kt`: Model data progres centang langkah manasik.
- **Modul Parser `:core:common`:**
  - `HajjContentParser.kt`: Parser JSON menggunakan `kotlinx.serialization` dengan konfigurasi `ignoreUnknownKeys = true` (forward-compatible), aman terhadap input malformed.
  - `HajjContentParserTest.kt`: Unit test komprehensif menguji deserialisasi berkas `content_v1.0.0.json` riil, validasi data Hari Arafah, teks doa Arab tahlil, penanganan error JSON korup, dan kompatibilitas atribut masa depan.

### TASK-01 & Fase 0 (Selesai pada Commit: e775d3a)
- **Slice 1.1 (Multi-Module Gradle Scaffolding):**
  - `gradle/libs.versions.toml`: Version catalog modern (Kotlin 2.0.21, AGP 8.7.1, Compose BOM 2024.10.00, Hilt 2.52, Room 2.6.1).
  - `settings.gradle.kts`: Konfigurasi multi-module 9 modul terisolasi (`:app`, `:core:designsystem`, `:core:common`, `:core:database`, `:core:model`, `:feature:guide`, `:feature:places`, `:feature:tracking`, `:feature:settings`).
  - `build.gradle.kts` & `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.10.2).
- **Slice 1.2 (Core Design System `:core:designsystem`):**
  - `Color.kt`: Palet warna *Accessible & Ethical* (Navy `#0C4A6E`, Trust Blue `#0369A1`, Hajj Green `#15803D`, Anti-Glare Background `#F8FAFC`, Status Sensor Kompas).
  - `Dimens.kt`: Standar aksesibilitas lansia (`MinTouchTargetSize = 56.dp`, `TouchTargetGap = 12.dp`, Irama spasi 8dp).
  - `Type.kt`: Atkinson Hyperlegible & Amiri Quran (Arabic 24-28sp, Body 18sp, Title 22sp).
  - `Theme.kt`: Composable wrapper `HajjTheme` dengan penegakan status bar dinamis.
  - `TokensTest.kt`: Unit test verifikasi kepatuhan target sentuh $\ge 56\text{ dp}$ dan kontras token.
- **Slice 1.3 (Baseline Konten Manasik `content_v1.0.0.json`):**
  - Tersimpan di `app/src/main/assets/content_v1.0.0.json`.
  - Berisi 8 fase/hari manasik terstruktur, teks doa Arab berharakat, transliterasi Latin, terjemahan, dan 10 catatan perbedaan fikih terverifikasi dari `ringkasan.md`.
- **Slice 1.4 (Git Repository & Version Control):**
  - Repository git diinisialisasi pada branch `main` dengan `.gitignore` Android standar. Commit awal: `e775d3a`.
