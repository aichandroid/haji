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
| **Fase 0**   | Baseline Konten Manasik (`content_v1.0.0.json`)| 🟡 **IN PROGRESS** | - | Ekstraksi terstruktur dari `ringkasan.md` ke format JSON. |
| **TASK-01**  | Setup Multi-Module & Design System | 🟡 **IN PROGRESS** | - | Scaffolding Gradle multi-module & modul `:core:designsystem`. |
| **TASK-02**  | Model Domain Manasik & Parser JSON | ⚪ *QUEUED* | - | Modul `:core:model` & parser data. |
| **TASK-03**  | Mesin Kalender Hijrah & Date Override | ⚪ *QUEUED* | - | Modul `:core:common` (`HijriCalendarEngine`). |
| **TASK-04**  | Room DB: Checklist & FTS4 Search Table | ⚪ *QUEUED* | - | Modul `:core:database` (Room SQLite). |

---

## 2. LOG EKSEKUSI TUGAS AKTIF

### TASK-01: Setup Multi-Module & Design System + Fase 0 Baseline Konten
- **Slice 1.1:** Setup Root Multi-Module Gradle (`settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle-wrapper.properties`).
- **Slice 1.2:** Modul `:core:designsystem` (`Color.kt`, `Type.kt`, `Dimens.kt`, `Theme.kt`).
- **Slice 1.3:** Baseline Konten Manasik (`app/src/main/assets/content_v1.0.0.json`).
- **Slice 1.4:** Unit Test `:core:designsystem` (`TokensTest.kt`).
