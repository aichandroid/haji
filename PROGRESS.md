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
| **TASK-08**  | GPS Multi-Sampling Averaging Engine     | ✅ **SELESAI** | 25-10-2026 | Mesin perataan koordinat median GPS, filter galat > 25m, provider `FusedLocationProviderClient` (`callbackFlow` + `awaitClose`), unit test komprehensif. |
| **TASK-09**  | Fusi Sensor Rotasi Kompas & True Azimuth| ✅ **SELESAI** | 25-10-2026 | `BearingCalculator` (Haversine & Great Circle), kompensasi deklinasi Makkah (+2.8°), smoothing EMA, `CompassNavigationEngine`, unit test komprehensif. |
| **TASK-10**  | Feature Places: UI Kompas Panah Compose | ✅ **SELESAI** | 25-10-2026 | Modul `:feature:places` (`PlacesListScreen`, `CompassScreen` SCR-04, `RotatingCompassArrow`, `AddPlaceDialog`, `CalibrationGuideDialog`, ViewModels, dan unit tests). |
| **TASK-11**  | Firebase Auth Anonymous & Token DTO     | ✅ **SELESAI** | 25-10-2026 | Modul `:core:network` (`AuthManager`, `DefaultFirebaseAuthManager`, DTOs Token/QR, `TrackingApiClient`, dan unit tests). |
| **TASK-12**  | Cloud Functions: Pairing Token Exchange | ✅ **SELESAI** | 25-10-2026 | `/backend/functions` (`index.ts`, `tokenService.ts`, rate limit 3/15m, transaksi atomik anti-replay, purge data, unit tests). |
| **TASK-13**  | Firestore Security Rules Penegakan RBAC | ✅ **SELESAI** | 25-10-2026 | `/backend/firestore.rules` (RBAC, consent verification `hasActiveTrackingConsent`, anti-IDOR, emulator config, unit tests). |
| **TASK-14**  | Android Location Foreground Service     | ✅ **SELESAI** | 25-10-2026 | Modul `:feature:tracking` (`LocationTrackingService.kt`, adaptif 30s/300s, ongoing notification, Android 14+ location type, unit tests). |
| **TASK-15**  | Offline Location Queue & Sync Manager   | ✅ **SELESAI** | 25-10-2026 | Modul `:core:database` & `:feature:tracking` (`location_sync_queue`, FIFO 50, `LocationQueueRepository`, `SyncManager` Data Minimization, unit tests). |
| **TASK-16**  | Feature Tracking: QR Share & Scan UI    | 🟡 **NEXT** | - | Modul `:feature:tracking` (`QrScreen.kt`, ML Kit Scanner, consent manager). |

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-15: Offline Location Queue & Sync Manager (Selesai pada Commit: f7dcdb2)
- **Tabel Antrean Offline SQLite (`location_sync_queue` di `:core:database`):**
  - Entitas Room `LocationSyncQueueEntity` dengan indeks `idx_queue_timestamp`.
  - DAO `LocationSyncQueueDao`:
    - Operasi penyisipan koordinat offline.
    - Penegakan kapasitas maksimal 50 sampel secara FIFO (`trimQueueToSize(50)`): mempertahankan 50 sampel paling mutakhir dan memangkas sampel tertua secara otomatis.
    - Pengambilan sampel paling mutakhir (`getLatestItem()`).
    - Pembersihan tuntas antrean setelah transmisi berhasil (`clearQueue()`).
  - Terdaftar resmi pada basis data `AppDatabase`.
  - Abstraksi Clean Architecture `LocationQueueRepository` dan implementasi `DefaultLocationQueueRepository`.
- **Manajer Sinkronisasi Store-and-Forward (`SyncManager.kt` di `:feature:tracking`):**
  - Menerapkan kepatuhan *Data Minimization* Bagian 17.6:
    - Saat internet pulih, sistem tidak mengirimkan seluruh 50 riwayat pergerakan (menghemat baterai, bandwidth roaming, dan privasi jemaah).
    - Sistem hanya mengambil 1 sampel koordinat paling mutakhir (`getLatestPendingLocation()`), mengirimkannya via `TrackingApiClient.syncLocations()`, lalu membersihkan seluruh sisa antrean lama yang telah basi di SQLite.
  - Memantau perubahan status konektivitas jaringan secara reaktif (`NetworkConnectivityProvider.observeConnectivity()`) untuk memicu sinkronisasi otomatis saat online.
  - State machine reaktif `SyncState` (`Idle`, `Syncing`, `Success(count)`, `Offline(count)`, `Error(msg)`).
  - Integrasi dua arah dengan `LocationTrackingService.locationListener`.
- **Pengujian Unit & Validasi:**
  - `LocationQueueRepositoryTest.kt`: Pengujian penyisipan item, penegakan kapasitas batas 50 FIFO saat 60 sampel masuk (10 sampel tertua terhapus), penghapusan batch id, dan pembersihan total antrean.
  - `SyncManagerTest.kt`: Pengujian penyimpanan ke SQLite saat offline, pemicu sinkronisasi instan saat online, transmisi hanya 1 sampel mutakhir (Data Minimization), auto-sync saat sinyal pulih, dan penautan listener service.
  - Validasi sintaksis struktur kurung pada seluruh 37 file Kotlin di `:core:database` dan `:feature:tracking`.

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-14: Android Location Foreground Service (Selesai pada Commit: 603ac97)
- **Kepatuhan Android 14+ / API 34+ (`feature/tracking/src/main/AndroidManifest.xml`):**
  - Deklarasi layanan dengan atribut eksplisit `android:foregroundServiceType="location"` dan `android:exported="false"`.
  - Deklarasi izin runtime `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, dan `POST_NOTIFICATIONS`.
- **Mesin Klasifikasi Mobilitas Adaptif (`MovementClassifier.kt`):**
  - Penegakan penghematan daya baterai (NFR-BAT-001: konsumsi baterai $\le 2.5\%$/jam):
    - **Mode Bergerak (Moving Window):** Kecepatan $> 0.5\text{ m/s}$ (~$1.8\text{ km/jam}$) atau pergeseran posisi $> 10\text{ meter}$ menghasilkan interval sampling $30\text{ detik}$.
    - **Mode Diam (Stationary Window):** Saat jemaah istirahat di dalam tenda Mina atau masjid (kecepatan $\le 0.5\text{ m/s}$ dan pergeseran $< 10\text{ meter}$ selama $> 3\text{ menit}$), interval sampling otomatis diperpanjang menjadi $5\text{ menit}$ ($300\text{ detik}$).
    - Jika jemaah kembali bergerak, sistem seketika kembali ke interval $30\text{ detik}$ tanpa jeda.
- **Notifikasi Status Bar Persisten (`TrackingNotificationHelper.kt`):**
  - Notification channel `channel_hajj_tracking` bertipe `IMPORTANCE_LOW` (hening tanpa suara berulang).
  - Properti `setOngoing(true)` sehingga notifikasi tidak dapat di-swipe dismiss saat layanan pelacakan aktif (FR-TRACK-004).
  - Menampilkan status dinamis real-time ("Bergerak / Istirahat", timestamp update terakhir) dan tombol aksi cepat `[ Hentikan ]` dan `[ Buka Aplikasi ]`.
- **Layanan Latar Belakang Android (`LocationTrackingService.kt`):**
  - Menginisialisasi `startForeground()` dengan tipe `FOREGROUND_SERVICE_TYPE_LOCATION` via `ServiceCompat`.
  - Mengintegrasikan `FusedLocationProviderClient` dengan `Priority.PRIORITY_HIGH_ACCURACY`.
  - Mengatur ulang interval permintaan lokasi secara dinamis saat terjadi transisi mode mobilitas dari `MovementClassifier`.
  - Menyediakan callback `locationListener` untuk meneruskan sampel koordinat ke antrean sinkronisasi offline (TASK-15).
  - Mempublikasikan state reaktif publik `TrackingServiceStatus` (`isRunning`, `movementMode`, `lastLocation`, `totalUpdatesCount`).
- **Pengendali Layanan (`TrackingServiceController.kt`):**
  - Menyediakan abstraksi `TrackingController` untuk pemanggilan `startTracking(context, userId)` dan `stopTracking(context)` dari UI dan ViewModel.
- **Pengujian Unit & Validasi:**
  - `MovementClassifierTest.kt`: Pengujian kondisi awal moving (30s), mobilitas kecepatan tinggi, periode toleransi diam 3 menit, transisi ke stationary (300s), transisi balik saat bergerak, dan reset status.
  - `TrackingServiceStatusTest.kt`: Pengujian state awal dan pembaruan atribut model status.
  - Skrip mandiri Python: 100% validasi sintaksis kurung pada 7 file Kotlin di `:feature:tracking` dan simulasi matematis pergerakan.

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-13: Firestore Security Rules Penegakan RBAC (Selesai pada Commit: cde0a6a)
- **Aturan Keamanan Tingkat Kernel Database (`backend/firestore.rules`):**
  - Menggunakan Firestore Security Rules Version 2 (`rules_version = '2'`).
  - Fungsi pembantu otentikasi & otorisasi:
    - `isAuthenticated()`: Memastikan token Firebase Auth Bearer aktif dan valid.
    - `isOwner(userId)`: Memastikan `request.auth.uid == userId` (anti-IDOR mutlak).
    - `hasActiveTrackingConsent(targetUserId)`: Memeriksa keberadaan dokumen `tracking_relations/${targetUserId}_${request.auth.uid}` dengan status `ACTIVE` di Firestore sebelum mengizinkan pembacaan koordinat lokasi target.
  - Penegakan izin per koleksi:
    - `/users/{userId}`: Akses baca dan tulis hanya diizinkan bagi pemilik akun (`isOwner(userId)`).
    - `/pairing_tokens/{tokenId}`: Dapat dibaca oleh pengguna terautentikasi; hanya dapat dibuat dengan `target_uid == auth.uid` (mencegah pemalsuan identitas jemaah lain); mutasi `update` dan `delete` langsung dari klien dilarang keras (hanya via Cloud Functions).
    - `/tracking_relations/{relationId}`: Hanya dapat dibaca oleh target atau pelacak yang bersangkutan; pembaruan hanya diizinkan bagi pihak target untuk mencabut izin pelacakan (`status = "REVOKED"`); pembuatan dan penghapusan langsung dari klien dilarang.
    - `/locations/{userId}` (PII Telemetri Sensitif): Penulisan hanya oleh pemilik (`isOwner(userId)`); pembacaan hanya diizinkan untuk pemilik sendiri ATAU pelacak dengan relasi aktif `hasActiveTrackingConsent(userId)`. Pelacak yang dicabut izinnya (`REVOKED`) atau orang asing langsung ditolak oleh kernel Firestore.
  - Konfigurasi Emulator & Proyek: `backend/firebase.json` dan `backend/firestore.indexes.json`.
- **Pengujian Unit Keamanan & RBAC (`firestoreRules.test.ts`):**
  - Menguji keabsahan struktur sintaksis file `firestore.rules`.
  - Menguji 18 skenario evaluasi RBAC terinci (unauthenticated access denied, anti-IDOR user docs, anti-spoofing pairing tokens, anti-leak tracking relations, dan evaluasi izin pembacaan telemetri lokasi saat izin aktif vs saat dicabut seketika).
  - Seluruh 28 pengujian unit backend (`npm --prefix backend test`) lulus 100% tanpa kegagalan.

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-12: Cloud Functions: Pairing Token Exchange (Selesai pada Commit: 18d59ce)
- **Arsitektur Backend Serverless Firebase (`/backend`):**
  - Struktur modular TypeScript di `/backend/functions` dengan Firebase Functions v2 HTTPS Callable.
  - Penataan package root `backend/package.json` yang mendelegasikan eksekusi test dan build.
- **Layanan Pertukaran Token & Konsensus Privasi (`tokenService.ts`):**
  - `generatePairingToken`:
    - Penerbitan token acak berkriptografi tinggi (`tok_${randomHex}`) bertempo ketat 5 menit (300 detik).
    - Format URI QR standar `hajj2027://pair?t={tokenId}&u={targetUid}`.
    - Pembatasan laju (*Rate Limiting*) maksimal 3 token per 15 menit per UID untuk mitigasi DoS / serangan brute-force.
  - `claimPairingToken`:
    - Eksekusi transaksi database atomik (*Firestore Transaction*) untuk mengeliminasi *race conditions*.
    - Proteksi anti-replay (`TOKEN_ALREADY_USED` 409) jika token sudah pernah diklaim.
    - Penolakan token kedaluwarsa (`TOKEN_EXPIRED` 410) jika masa aktif > 5 menit terlewati.
    - Penolakan *self-pairing* (jemaah melacak dirinya sendiri).
    - Pembuatan dokumen relasi pelacakan konsensus (`tracking_relations/${targetUid}_${trackerUid}`) dengan status `ACTIVE` dan masa berlaku hingga akhir musim haji 2027.
  - `revokeTrackingAccess`:
    - Pencabutan instan hak akses pelacak oleh pihak target, mengubah status relasi menjadi `REVOKED`.
  - `purgeUserData` (*Right to be Forgotten* / UU PDP No. 27/2022):
    - Penghapusan tuntas (*clean wipe*) profil pengguna, telemetri lokasi, seluruh pairing tokens, dan relasi tracking terkait.
    - Penghapusan akun pengguna di Firebase Authentication.
  - `syncLocations`:
    - Validasi kepemilikan UID dan pembaruan koordinat mutakhir dari antrean offline SQLite jemaah.
- **Pengujian Unit & Validasi (`npm --prefix backend test`):**
  - Menggunakan Node.js test runner bawaan dengan `FakeFirestoreDatabase` in-memory.
  - 9 pengujian unit mencakup seluruh skenario keberhasilan, batas rate limit, serangan replay, kedaluwarsa waktu, dan penghapusan total data pengguna (100% lulus dalam ~137ms).

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-11: Firebase Auth Anonymous & Token DTO (Selesai pada Commit: 45ef7dd)
- **Scaffolding Modul Jaringan Baru (`:core:network`):**
  - Didaftarkan di `settings.gradle.kts` dan ditambahkan sebagai dependensi modul `:app` dan `:feature:tracking`.
  - Konfigurasi `build.gradle.kts` dengan `kotlinx.serialization` dan dependensi Firebase BOM 33.5.1 (`firebase-auth`, `firebase-firestore`, `firebase-functions`, `kotlinx-coroutines-play-services`).
  - `AndroidManifest.xml` dengan izin `INTERNET` dan `ACCESS_NETWORK_STATE`.
- **Manajemen Autentikasi Anonim (`core/network/auth/`):**
  - `AuthUser.kt`: Model data pengguna anonim (`uid`, `isAnonymous`, `idToken`, `createdAtEpochMs`).
  - `AuthState.kt`: State machine reaktif (`Unauthenticated`, `Loading`, `Authenticated(user)`, `Error(message)`).
  - `AuthManager.kt`: Kontrak antarmuka Clean Architecture untuk mengisolasi lapisan UI dan Service dari Firebase SDK langsung.
  - `FirebaseAuthProvider.kt` & `DefaultFirebaseAuthProvider.kt`: Abstraksi delegate tingkat rendah Firebase Authentication SDK untuk memfasilitasi pengujian unit tanpa server cloud.
  - `DefaultFirebaseAuthManager.kt`:
    - Mengelola alur login anonim otomatis transparan (`ensureAuthenticated()`) tanpa membebani jemaah lansia dengan input email/password.
    - Dilengkapi proteksi konkurensi Coroutines `Mutex` untuk mencegah panggilan ganda secara bersamaan.
    - Pemulihan sesi otomatis (*transparent re-authentication*) jika sesi token habis (`AUTH_REQUIRED` 401).
    - Menghapus akun permanen di Firebase Auth saat pengguna meminta pembersihan data (`deleteAccount()`, Right to be Forgotten).
- **Data Transfer Objects & Kontrak API (`core/network/dto/`):**
  - `TokenDtos.kt`: Implementasi DTO `@Serializable` yang presisi sesuai Bagian 21 `SYSTEM_DESIGN_DOCUMENT.md`:
    - `GenerateTokenRequest` & `GenerateTokenResponse` (token UUID, payload URI QR, expires at 300s TTL).
    - `ClaimTokenRequest` & `ClaimTokenResponse` (token ID, label pelacak, ID relasi, target user ID).
    - `RevokeAccessRequest` & `RevokeAccessResponse` (pencabutan instan hak akses pelacak).
    - `PurgeUserDataResponse` (penghapusan akun total dan konfirmasi).
    - `LocationSyncRequest` & `LocationPointDto` (sinkronisasi batch koordinat dari antrean offline SQLite).
  - `QrPayload.kt`:
    - Generator dan parser URI `hajj2027://pair?t={tokenId}&u={targetUid}`.
    - Validasi ketat terhadap skema URI, authority, dan parameter kueri wajib dengan penanganan whitespace.
  - `ApiError.kt`:
    - Pemetaan kode error standar backend (`AUTH_REQUIRED`, `TOKEN_EXPIRED`, `TOKEN_ALREADY_USED`, `TRACKING_ACCESS_REVOKED`, `RATE_LIMITED`, `NETWORK_UNAVAILABLE`).
    - `ApiException` dengan kode terstruktur dan HTTP status code.
- **Klien Cloud Functions (`core/network/api/`):**
  - `TrackingApiClient.kt` & `FunctionsCaller.kt`: Kontrak operasi jaringan tingkat tinggi dan abstraksi HTTPS Callable.
  - `DefaultTrackingApiClient.kt`: Klien jaringan yang secara otomatis menyematkan autentikasi anonim, mengeksekusi callable functions, dan memvalidasi respon error secara terpusat.
- **Pengujian Unit & Validasi:**
  - `FirebaseAuthManagerTest.kt`: Pengujian alur sign-in anonim, pemakaian ulang sesi yang sudah ada, penanganan kegagalan jaringan, penyegaran ID token, re-autentikasi transparan, sign-out, dan penghapusan akun.
  - `TokenDtosSerializationTest.kt`: Pengujian round-trip serialisasi/deserialisasi terhadap cuplikan JSON riil dari spesifikasi Bagian 21 SDD.
  - `QrPayloadTest.kt`: Pengujian pembuatan URI, penguraian parameter, pemangkasan whitespace, parameter tertukar, serta penolakan skema/parameter tidak valid.
  - `TrackingApiClientTest.kt`: Pengujian pemanggilan Cloud Functions, mapping kode error API ke `ApiException`, dan eksekusi penghapusan akun lokal saat purge data.
  - Verifikasi skrip Python: 100% validasi sintaksis kurung pada seluruh 16 file Kotlin di `:core:network` dan validasi logika QR/JSON.

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-10: Feature Places: UI Kompas Panah Compose (Selesai pada Commit: d6d47b4)
- **Komponen UI Interaktif & Ramah Lansia (`:feature:places`):**
  - `PlacesListScreen.kt`:
    - Menampilkan daftar tempat tersimpan jemaah dengan chip filter kategori (`Semua`, `Pintu Masjid`, `Hotel`, `Tenda`, `Stasiun/Bus`, `Lainnya`).
    - *Empty state* informatif saat belum ada lokasi tersimpan dengan tombol aksi cepat.
    - Setiap item kartu tempat menyediakan nama tempat berukuran kontras tinggi, label kategori berikon, jarak langsung jika lokasi saat ini tersedia, tombol arah panah, dan tombol hapus cepat.
    - Floating Action Button (FAB) raksasa $\ge 56\text{ dp}$ untuk menandai lokasi baru di posisi saat ini.
  - `CompassScreen.kt` (Layar Utama SCR-04):
    - Bilah judul dengan tombol kembali mudah diakses dan nama tempat tujuan berukuran 22sp tebal.
    - `AccuracyStatusBadge.kt`: Lencana status visual dinamis (Hijau $\le 10\text{m}$, Kuning $11\text{--}30\text{m}$, Merah $>30\text{m}$, Oranye saat butuh kalibrasi).
    - `RotatingCompassArrow.kt`: Jarum panah kompas raksasa berbasis Canvas dengan dial derajat sirkular, penanda mata angin (U, T, S, B), dan interpolasi sudut terpendek `shortestAngleDiff()` untuk animasi rotasi halus tanpa lonjakan jarum $360^\circ$.
    - Indikator jarak garis lurus berukuran ekstra besar (36sp bold) kontras tinggi.
    - Kartu catatan tempat tersimpan jika tersedia.
    - Spanduk peringatan permanen garis lurus: *"Panah menunjukkan GARIS LURUS fisik, bukan rute jalan kaki. Hindari menyeberang jalan raya berbahaya atau jalur tertutup."*
    - Dua tombol aksi bawah berukuran $\ge 56\text{ dp}$ ramah lansia: `[ KALIBRASI ]` (manual buka panduan) dan `[ HAPUS TEMPAT ]` (konfirmasi dialog).
  - `AddPlaceDialog.kt`:
    - Dialog penandaan tempat baru dengan input nama tempat, pemilihan kategori radio, catatan opsional, dan bilah progres multi-sampling GPS interaktif (target 5 sampel stabil).
    - Mencegah penyimpanan sebelum GPS selesai menghasilkan koordinat median teragregasi.
  - `CalibrationGuideDialog.kt`:
    - Dialog panduan kalibrasi kompas dengan ilustrasi gerakan angka 8 (*Figure-8 gesture*) dan langkah praktis untuk memulihkan akurasi sensor magnetometer saat terdistorsi medan logam/beton di Masjidil Haram.
- **State Management & ViewModels:**
  - `PlacesListViewModel.kt`:
    - Reaktif memantau Room DB `PlacesRepository.observeAllPlaces()` yang dipadukan dengan filter kategori terpilih.
    - Mengelola alur penandaan tempat baru dengan orkestrasi `GpsAveragingEngine` (menangani progres sampling, timeout, dan galat akurasi).
    - Menangani penyimpanan dan penghapusan tempat.
  - `CompassViewModel.kt`:
    - Memuat entitas target dari repositori secara asinkron.
    - Mengalirkan pembaruan GPS real-time dan sensor orientasi true azimuth secara anti-leak.
    - Memadukan koordinat GPS dan azimuth kompas melalui `CompassNavigationEngine` untuk menghitung jarak dan sudut panah.
    - Secara otomatis menampilkan dialog kalibrasi jika magnetometer melaporkan `CompassAccuracy.UNRELIABLE`.
    - Menyediakan alur konfirmasi penghapusan tempat dari layar kompas.
- **Pengujian Unit & Verifikasi Sistem:**
  - `PlacesListViewModelTest.kt`:
    - Pengujian inisialisasi pemuatan tempat dan penonaktifan status loading.
    - Pengujian penyaringan daftar tempat berdasarkan kategori (DOOR, CAMP, ALL).
    - Pengujian pembukaan dialog dan alur multi-sampling GPS berhasil.
    - Pengujian penyimpanan tempat baru ke dalam repositori dengan penutupan dialog.
    - Pengujian penghapusan tempat dari repositori.
    - Pengujian pembatalan dialog penandaan tempat.
  - `CompassViewModelTest.kt`:
    - Pengujian pemuatan tempat target dan inisialisasi status menunggu GPS.
    - Pengujian fusi koordinat GPS dan sudut orientasi ke navigasi bearing panah.
    - Pengujian pemicu otomatis dialog kalibrasi saat kompas `UNRELIABLE`.
    - Pengujian buka/tutup manual dialog kalibrasi dan dialog hapus.
    - Pengujian eksekusi konfirmasi hapus tempat dan pembaruan flag `isTargetDeleted`.
  - Skrip pengujian mandiri Python:
    - 100% validasi sintaksis struktur kurung pada seluruh 24 berkas Kotlin di `:feature:places`.
    - Validasi matematis navigasi dan sudut panah relatif terhadap pergerakan orientasi jemaah.

---

## 2. LOG EKSEKUSI TUGAS SELESAI

### TASK-09: Fusi Sensor Rotasi Kompas & True Azimuth (Selesai pada Commit: 4ac82d2)
- **Kalkulator Spasial & Bearing Geografis (`BearingCalculator.kt`):**
  - Jarak Haversine $d = 2R \cdot \text{atan2}(\sqrt{a}, \sqrt{1-a})$ dengan radius bumi $6.371\text{ km}$.
  - Initial Great Circle Bearing $T_b = \text{atan2}(\sin(\Delta lng) \cos(lat_2), \dots)$ dalam rentang $[0^\circ, 360^\circ)$.
  - True Azimuth $A_t = (A_m + D + 360^\circ) \pmod{360^\circ}$ dengan kompensasi deklinasi magnetik Makkah $+2.8^\circ$.
  - Sudut Relatif Panah Navigasi $\theta = (T_b - A_t + 360^\circ) \pmod{360^\circ}$ terhadap puncak layar smartphone.
  - `shortestAngleDiff()`: Perhitungan jarak sudut terpendek $[-180^\circ, 180^\circ]$ untuk interpolasi rotasi jarum panah tanpa lompatan liar di perbatasan $360^\circ$.
  - `formatDistance()`: Pemformatan ramah lansia kontras tinggi (meter untuk $<1000\text{m}$, km desimal untuk $\ge 1\text{km}$).
- **Model Orientasi & Navigasi (`CompassModels.kt`):**
  - `CompassOrientation`: Data kelas orientasi (`magneticAzimuth`, `trueAzimuth`, `declination`, `pitch`, `roll`, `accuracy`).
  - `CompassAccuracy`: Enum keandalan sensor magnetometer (`HIGH`, `MEDIUM`, `LOW`, `UNRELIABLE`).
  - `NavigationAccuracyStatus`: Penegakan state machine navigasi Section 17.3.3 (`HIGH` $\le 10\text{m}$, `MODERATE` $11\text{--}30\text{m}$, `DEGRADED` $>30\text{m}$, `CALIBRATION_NEEDED` saat sensor `UNRELIABLE`).
  - `NavigationTargetBearing`: Paket data hasil fusi navigasi lengkap (derajat panah, jarak, teks format, status akurasi).
- **Penyedia Sensor Android Anti-Leak (`AndroidOrientationSensorProvider.kt`):**
  - Mengutamakan sensor fusi hardware `Sensor.TYPE_ROTATION_VECTOR` dengan fallback anggun ke `TYPE_ACCELEROMETER` + `TYPE_MAGNETIC_FIELD`.
  - Menerapkan Exponential Moving Average (EMA) smoothing ($\alpha = 0.25$) sirkular untuk meredam jitter/getaran jarum kompas.
  - Menggunakan `callbackFlow` dengan `awaitClose { sensorManager.unregisterListener(listener) }` untuk mencegah kebocoran baterai saat layar mati/tidak aktif.
- **Mesin Navigasi Kompas (`CompassNavigationEngine.kt`):**
  - Memadukan koordinat GPS pengguna dan target dengan orientasi kompas real-time.
  - Memetakan kualitas sinyal ke `NavigationAccuracyStatus` secara otomatis.
- **Pengujian Unit & Verifikasi Matematis:**
  - `BearingCalculatorTest.kt`:
    1. Pengujian jarak Haversine riil (Hotel Ajyad ke Ka'bah: ~389 meter).
    2. Pengujian target bearing arah utara murni (~0°), timur murni (90°), dan barat murni (270°).
    3. Pengujian kompensasi deklinasi magnetik positif & negatif.
    4. Pengujian sudut relatif panah terhadap berbagai arah hadap pengguna.
    5. Pengujian `shortestAngleDiff` menyeberangi batas $360^\circ$ ($355^\circ \rightarrow 5^\circ = +10^\circ$).
    6. Pengujian format jarak ramah lansia.
  - `CompassNavigationEngineTest.kt`:
    1. Pengujian kalkulasi terpadu bearing panah dan jarak.
    2. Pengujian klasifikasi status akurasi GPS (High, Moderate, Degraded).
    3. Pengujian deteksi kebutuhan kalibrasi angka 8 saat magnetometer `UNRELIABLE`.
  - Skrip verifikasi mandiri Python: Memvalidasi seluruh perhitungan trigonometri bola besar dan normalisasi sudut lingkaran (100% lolos).

### TASK-08: GPS Multi-Sampling Averaging Engine (Selesai pada Commit: dd3c788)
- **Model Data Sampling & Agregasi (`GpsSample.kt`):**
  - `GpsSample`: Model sampel tunggal GPS (`latitude`, `longitude`, `accuracyMeters`, `timestampEpochMs`).
  - `AveragedLocation`: Model koordinat stabil hasil agregasi median lintang & bujur, rata-rata akurasi, serta jumlah sampel valid dan total.
  - `GpsSamplingState`: State reaktif Compose (`Idle`, `Sampling(collected, target, latestAccuracy)`, `Success(location)`, `Error(reason, message)`).
  - `GpsSamplingError`: Kode error terstruktur (`TIMEOUT_INSUFFICIENT_SAMPLES`, `POOR_ACCURACY_ALL_FILTERED`, `GPS_DISABLED`, `PERMISSION_DENIED`).
- **Abstraksi Penyedia Sampel Lokasi (`LocationSampleProvider.kt` & `FusedLocationSampleProvider.kt`):**
  - Mengisolasi pemanggilan platform Google Play Services `FusedLocationProviderClient` agar logika komputasi mudah diuji unit tanpa emulator.
  - Penegakan anti-kebocoran listener GPS: menggunakan `callbackFlow` dengan `awaitClose { fusedLocationClient.removeLocationUpdates(callback) }` jika coroutine sampling dibatalkan di tengah jalan.
  - Konfigurasi `Priority.PRIORITY_HIGH_ACCURACY` dengan interval sampling adaptif (default 800ms).
- **Mesin Komputasi Median & Filter Galat (`GpsAveragingEngine.kt`):**
  - `calculateAveragedLocation()`:
    1. Mengeliminasi otomatis sampel outlier dengan akurasi terburuk ($accuracy > 25\text{m}$).
    2. Menghitung nilai median lintang dan bujur secara deterministik (menangani ukuran sampel ganjil maupun genap).
    3. Menghitung rata-rata akurasi sebagai radius ketidakpastian titik.
    4. Mengembalikan null jika sampel valid kurang dari batas minimal (3 sampel).
  - `startSampling()`:
    1. Mengalirkan status progres interaktif `Sampling` setiap sampel GPS baru diterima.
    2. Mengumpulkan target 5 sampel dalam batas timeout 6 detik.
    3. Menangani penolakan izin `SecurityException` (`PERMISSION_DENIED`), timeout ketiadaan sinyal (`TIMEOUT_INSUFFICIENT_SAMPLES`), dan kondisi multipath ekstrem (`POOR_ACCURACY_ALL_FILTERED`).
- **Pengujian Unit & Verifikasi Matematis:**
  - `GpsAveragingEngineTest.kt`:
    1. Pengujian 5 sampel koordinat acak di sekitar Masjidil Haram (verifikasi akurasi nilai median lat/lng dan rata-rata galat).
    2. Pengujian sampel berjumlah genap (rata-rata dari dua elemen tengah).
    3. Pengujian penyaringan noise/outlier dengan galat $> 25\text{m}$ (3 sampel valid dari 5 sampel).
    4. Pengujian penolakan sampel jika sampel valid kurang dari batas minimum 3.
    5. Pengujian reactive flow `startSampling` dengan fake provider menghasilkan `GpsSamplingState.Success`.
    6. Pengujian error handling: timeout, poor accuracy, permission denied, dan GPS disabled.
  - Skrip verifikasi matematis Python: Memvalidasi algoritma sorting, pemisahan genap/ganjil, dan filter threshold galat secara independen (100% lolos).

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
