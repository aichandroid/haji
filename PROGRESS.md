# PROGRESS TRACKER — PENGEMBANGAN APLIKASI PANDUAN HAJI 2027
**Target Rilis:** Musim Haji 1448 H (Mei – Juni 2027)  
**Platform:** Android Native (Kotlin · Jetpack Compose · Offline-First)  
**Single Source of Truth Desain:** `SYSTEM_DESIGN_DOCUMENT.md`  
**Terakhir Diperbarui:** 25 Oktober 2026  

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
| **TASK-04**  | Room DB: Checklist & FTS4 Search Table | ✅ **SELESAI** | 24-10-2026 | Room SQLite `AppDatabase`, tabel `checklist_progress`, tabel virtual FTS4 `guide_search_fts`, query sanitizer & indexer, repository layer. |
| **TASK-05**  | Feature Guide: Beranda "Hari Ini" & UI  | ✅ **SELESAI** | 24-10-2026 | `HomeScreen`, `HomeViewModel`, 3 tombol aksi raksasa ramah lansia ($\ge 56\text{ dp}$), bilah progres checklist hari ini, integrasi kalender Umm al-Qura. |
| **TASK-06**  | Feature Guide: Bacaan Doa & Detail Step | ✅ **SELESAI** | 24-10-2026 | `GuideDetailScreen` (SCR-02), `DoaScreen` (SCR-03), integrasi dalil doa dari muslim.or.id, rumaysho.com, almanhaj.or.id, & yufid.com, tombol ukuran teks lansia. |
| **TASK-07**  | Room DB: Saved Places & DAOs            | ✅ **SELESAI** | 25-10-2026 | Entitas Room `SavedPlaceEntity`, indeks `idx_saved_places_category`, `PlacesDao`, `PlacesRepository` & `DefaultPlacesRepository`, unit test komprehensif. |
| **TASK-08**  | GPS Multi-Sampling Averaging Engine     | 🟡 **NEXT** | - | Modul `:feature:places` (`GpsEngine.kt`). |

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-07: Room DB: Saved Places & DAOs (Selesai pada Commit: 6e9b205)
- **Entitas Room Tempat Tersimpan (`SavedPlaceEntity.kt`):**
  - Tabel `saved_places` dengan kolom: `place_id` (PK), `name`, `category` (DOOR, HOTEL, CAMP, BUS, OTHER), `latitude`, `longitude`, `accuracy_meters`, `notes`, `photo_uri`, dan `created_at_epoch_ms`.
  - Indeks performa kategori `idx_saved_places_category` untuk query cepat per jenis tempat.
  - Pemetaan dua arah aman domain-ke-entitas (`toDomain()` & `fromDomain()`) dengan fallback ke `PlaceCategory.OTHER` jika kategori asing.
- **Data Access Object (`PlacesDao.kt`):**
  - `getAllPlacesFlow()`: Mengalirkan seluruh tempat tersimpan terurut mundur berdasarkan `created_at_epoch_ms DESC`.
  - `getPlacesByCategoryFlow(category)`: Query reaktif Flow untuk menyaring berdasarkan kategori tempat.
  - `getPlaceById()` & `getPlaceByIdFlow()`: Akses instan titik tempat berdasarkan ID.
  - `insertPlace()` & `insertPlaces()`: Penyimpanan titik dengan strategi `OnConflictStrategy.REPLACE`.
  - `updatePlace()`, `deletePlace()`, dan `deletePlaceById()`: Operasi manipulasi dan penghapusan titik tempat.
  - `deleteAllPlaces()`: Pembersihan seluruh entri basis data untuk mendukung kepatuhan *Right to be Forgotten* (UU PDP & Kebijakan Privasi).
  - `getPlacesCount()`: Penghitungan jumlah tempat tersimpan.
- **Integrasi Basis Data (`AppDatabase.kt`):**
  - Memasukkan `SavedPlaceEntity::class` ke dalam deklarasi Room `@Database`.
  - Menyediakan abstract getter `placesDao(): PlacesDao`.
- **Abstraksi Bersih Repositori (`PlacesRepository.kt` & `DefaultPlacesRepository.kt`):**
  - Mengisolasi dependensi langsung Room DAO dari layer UI `:feature:places`.
  - Menyediakan kontrak reaktif Flow: `observeAllPlaces()`, `observePlacesByCategory()`, dan `observePlaceById()`.
- **Pengujian Unit & Validasi SQLite:**
  - `PlacesRepositoryTest.kt`:
    1. Pengujian simpan dan baca tempat (`savePlace`, `getPlaceById`, verifikasi seluruh atribut).
    2. Pengujian pengurutan `observeAllPlaces` terurut descending berdasarkan waktu pembuatan.
    3. Pengujian penyaringan kategori `observePlacesByCategory` (DOOR, HOTEL, CAMP).
    4. Pengujian penghapusan entitas (`deletePlace` & `deletePlaceById`).
    5. Pengujian pembersihan total `deleteAllPlaces` (Right to be Forgotten).
    6. Pengujian ketahanan pemetaan enum `toDomain()` terhadap kategori tak dikenal.
  - Skrip pengujian mandiri SQLite3: Memverifikasi DDL SQLite tabel `saved_places`, indeks `idx_saved_places_category`, dan seluruh operasi SQL terhadap SQLite engine langsung.

### TASK-06: Feature Guide: Bacaan Doa & Detail Step (Selesai pada Commit: c219477)
- **Sumber Data Doa & Validasi Referensi Shahih:**
  - Melakukan riset komprehensif ke 4 rujukan terpercaya sesuai arahan pengguna: `muslim.or.id`, `rumaysho.com`, `almanhaj.or.id`, dan `yufid.com`.
  - Memperkaya berkas `content_v1.0.0.json` dengan doa-doa manasik berdalil:
    1. *Lafaz Niat Umrah & Haji*: HR. Bukhari & Muslim (Muslim.or.id & Rumaysho.com).
    2. *Doa Isytirat (Syarat Ihram bagi jemaah sakit/lansia)*: HR. Bukhari no. 5089 & Muslim no. 1207 (Rumaysho.com & Yufid.com).
    3. *Talbiah Utama*: HR. Bukhari no. 1549 & Muslim no. 19 (Muslim.or.id, Rumaysho.com & Almanhaj.or.id).
    4. *Doa Sapu Jagad Antara Rukun Yamani & Hajar Aswad*: HR. Abu Dawud no. 1892 (Muslim.or.id & Rumaysho.com).
    5. *Doa Minum Air Zamzam*: HR. Ad-Daruquthni no. 238 dari Ibnu Abbas (Rumaysho.com & Muslim.or.id).
    6. *Zikir & Doa di Atas Bukit Shafa dan Marwah*: HR. Muslim no. 1218 (Almanhaj.or.id & Muslim.or.id).
    7. *Doa Melewati Dua Pilar Hijau Sa'i*: Atsar Ibnu Umar & Ibnu Abbas, Shahih (Almanhaj.or.id & Yufid.com).
    8. *Zikir Utama Hari Arafah*: HR. Tirmidzi no. 3585, Hasan (Muslim.or.id & Almanhaj.or.id).
    9. *Zikir di Masy'aril Haram Setelah Subuh*: QS. Al-Baqarah: 198 & HR. Muslim no. 1218 (Almanhaj.or.id & Muslim.or.id).
    10. *Takbir Saat Melempar Setiap Kerikil Jumrah*: HR. Bukhari no. 1752 & Muslim no. 1218 (Almanhaj.or.id & Rumaysho.com).
- **Layar Detail Kegiatan Manasik (`GuideDetailScreen.kt` / SCR-02):**
  - `GuideDetailViewModel.kt` & `GuideDetailUiState.kt`: Memuat detail langkah, rincian aktivitas, larangan ihram, dan status progres centang.
  - `ActivityCard.kt`: Menampilkan judul aktivitas, badge status hukum (Rukun/Wajib/Sunah), catatan waktu dan tempat.
  - `StepItem.kt`: Komponen satu langkah berukuran teks 18sp, kotak larangan bernuansa peringatan lembut, kotak catatan ikhtilaf, tombol akses doa `[BACA DOA >>]`, dan tombol centang selesai (target sentuh $\ge 56\text{ dp}$).
  - `IkhtilafWarningBox.kt`: Menyajikan 10 titik perbedaan pendapat fikih secara netral dan menenangkan jemaah dengan menonjolkan konsensus (*ijma'*) para ulama.
- **Layar Bacaan Doa Multilingual (`DoaScreen.kt` / SCR-03):**
  - `DoaReadingViewModel.kt` & `DoaReadingUiState.kt`: Memuat konten doa berdasarkan `readingId` dan menyediakan fungsi pengalih skala ukuran teks (`TextSizeScale`: Normal vs Besar Lansia).
  - Teks Arab berukuran besar (28sp s.d. 36sp) dengan penegakan arah teks RTL (*Right-to-Left*).
  - Transliterasi Latin (18sp s.d. 24sp) dan terjemahan Bahasa Indonesia (17sp s.d. 22sp).
  - Kartu sumber rujukan dalil eksplisit mencantumkan perawi dan rujukan situs terpercaya.
- **Pengujian Unit (`GuideDetailViewModelTest.kt` & `DoaReadingViewModelTest.kt`):**
  - Menguji pemuatan data aktivitas dan langkah Hari Arafah (`day_09_zulhijah`).
  - Menguji toggle centang checklist secara reaktif dengan perubahan status instan.
  - Menguji pemuatan doa, teks Arab, Latin, terjemahan, dan rujukan sumber `Muslim.or.id`/`Almanhaj.or.id`.
  - Menguji mekanisme perbesaran huruf dinamis (*dynamic text scaling*) untuk lansia.

### TASK-05: Feature Guide: Beranda "Hari Ini" & UI (Selesai pada Commit: b335b7a)
- **Modul Fitur Panduan `:feature:guide`:**
  - `HajjGuideRepository.kt` & `AssetHajjGuideRepository.kt`: Abstraksi repositori konten manasik berbasis berkas JSON lokal (`content_v1.0.0.json`) yang independen dari Android Framework dan siap diuji pada JVM.
  - `HomeUiState.kt`: Model state Compose reaktif mencakup tanggal Hijriah & Masehi, hari manasik aktif, status puncak haji, persentase progres, dan status kesiapan offline.
  - `HomeViewModel.kt`:
    - Mengintegrasikan `HijriCalendarEngine` (zona waktu `Asia/Riyadh` + penyesuaian manual offset -3..+3 hari) dengan dataset manasik.
    - Mencocokkan hari aktif otomatis (misal: 9 Zulhijah langsung memuat "Hari Arafah — Wukuf Puncak Haji").
    - Menghitung progres langkah manasik hari ini secara reaktif dari `ChecklistRepository` Room Database.
    - Mekanisme fallback anggun (*graceful fallback*) ke hari persiapan pertama jika tanggal perangkat berada di luar musim haji.
  - **Komponen Antarmuka Pengguna (Accessible & WCAG AAA Lansia):**
    - `HomeScreen.kt`: Layout utama berbasis `Scaffold` dengan TopAppBar ber-badge status `Offline: Siap`, ikon pengaturan aksesibel, dan banner bantuan darurat rombongan.
    - `HomeDateHeader.kt`: Menampilkan tanggal Hijriah & Masehi dengan kontras tinggi serta badge status offset manual.
    - `HomeDaySummaryCard.kt`: Kartu informasi hari ini ber-border tegas, indikator lokasi, dan `LinearProgressIndicator` (tinggi 14dp, sudut membulat) yang menampilkan persentase penyelesaian langkah.
    - `BigActionButton.kt`: Tiga tombol aksi raksasa (tinggi $\ge 84\text{ dp}$, jauh melampaui standar $56\text{ dp}$ lansia) untuk `PANDUAN HARI INI`, `TEMPAT SAYA`, dan `LACAK KELUARGA`.
  - **Pengujian Unit (`HomeViewModelTest.kt`):**
    - Verifikasi pemuatan otomatis hari aktif 9 Zulhijah (Arafah) dengan penandaan puncak haji.
    - Verifikasi pergeseran tanggal instan saat manual offset +1 hari diterapkan.
    - Verifikasi akurasi persentase progres langkah manasik (0% s.d. 100%).
    - Verifikasi penanganan tanggal di luar musim haji.

### TASK-04: Room DB: Checklist & FTS4 Search Table (Selesai pada Commit: c5ecb51)
- **Modul Basis Data `:core:database`:**
  - `AppDatabase.kt`: Abstract Room Database (versi 1, exportSchema = false) mendefinisikan entitas checklist & virtual search FTS4.
  - `ChecklistEntity.kt` & `ChecklistDao.kt`:
    - Tabel `checklist_progress` (`step_id` PK, `is_completed`, `completed_at_epoch_ms`).
    - Query Flow reaktif Compose: `getAllChecklistFlow()`, `getCompletedStepIdsFlow()`, lookup per langkah, dan `upsertChecklist` dengan `OnConflictStrategy.REPLACE`.
  - `GuideSearchEntity.kt` & `GuideSearchDao.kt`:
    - Tabel virtual SQLite FTS4 `guide_search_fts` (`content_id`, `title`, `body_text`, `arabic_text`) dengan tokenizer `unicode61`.
    - Query MATCH FTS4 berlatensi rendah (< 80ms) untuk pencarian offline instan.
  - `FtsQueryUtils.kt`:
    - Sanitasi input query dari karakter injeksi/kontrol SQLite FTS (`*`, `"`, `'`, `()`, `[]`, `~`, `^`, dll).
    - Menghasilkan token ber-wildcard prefix otomatis (misal: "arafah wukuf" -> "arafah* wukuf*") yang aman dan akurat.
  - `HajjSearchIndexer.kt`:
    - Pengindeks otomatis dari model domain `HajjContentPackage` ke baris entitas FTS4 SQLite.
    - Mengindeks seluruh 8 hari, aktivitas, langkah manasik, catatan ikhtilaf fikih, serta doa bahasa Arab dan Latin.
  - `ChecklistRepository.kt` & `DefaultChecklistRepository.kt`:
    - Abstraksi Clean Architecture yang mengisolasi UI dari implementasi internal Room DAO.
  - `GuideSearchRepository.kt` & `DefaultGuideSearchRepository.kt`:
    - Abstraksi pencarian dan populasi indeks yang memproses query tersanitasi secara aman.
  - **Pengujian Unit & Validasi SQLite:**
    - `FtsQueryUtilsTest.kt`: Pengujian sanitasi string kosong, karakter ilegal, multi-token, dan teks Arab.
    - `HajjSearchIndexerTest.kt`: Pengujian pemetaan berkas konten riil `content_v1.0.0.json` ke entitas FTS4.
    - `ChecklistRepositoryTest.kt`: Pengujian operasi centang, pembatalan centang, pencatatan timestamp epoch, dan reset data jemaah.
    - `GuideSearchRepositoryTest.kt`: Pengujian delegasi query tersanitasi ke DAO FTS4.
    - Skrip pengujian mandiri Python/SQLite3: Memverifikasi DDL SQLite, indeks FTS4 tokenizer unicode61, dan pencarian multi-kata (Arab & Latin) pada seluruh korpus 42 entri manasik haji.

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
