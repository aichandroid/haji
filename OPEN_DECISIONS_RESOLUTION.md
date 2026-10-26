# DOKUMEN RESOLUSI KEPUTUSAN TERBUKA (OPEN DECISIONS RESOLUTION)
**Aplikasi Panduan Haji 2027 (Musim Haji 1448 H)**  
*Dokumen Referensi: Bagian 11 SYSTEM_DESIGN_DOCUMENT.md (OD-001 s.d. OD-006)*  
*Tanggal Pengesahan: 26 Oktober 2026*  
*Status: **SELURUH KEPUTUSAN TERKUNCI & DISETUJUI (STATUS: CLOSED & LOCKED)***  

---

## 1. RINGKASAN EKSEKUTIF

Sesuai metodologi perancangan sistem terstruktur, enam keputusan terbuka (*Open Decisions* OD-001 s.d. OD-006) yang diwariskan dari tahap konseptual awal telah dibahas, dianalisis risikonya, dan disepakati secara aklamasi bersama pemangku kepentingan utama: **Product Owner**, **Ustadz Pembimbing Ibadah Rombongan**, **Lead Systems Architect**, dan **Bendahara/Legal Rombongan**.

Dengan ditetapkannya dokumen resolusi ini, seluruh variabel strategis, hukum, fikih, dan arsitektural dinyatakan **KUNCI MATI (FINAL & LOCKED)** sebagai acuan operasional rilis musim haji 1448 H.

---

## 2. MATRIKS RESOLUSI KEPUTUSAN TERBUKA

```
+-----------------------------------------------------------------------------------------------------------------------+
|                                        REGISTER RESOLUSI KEPUTUSAN TERBUKA                                            |
+--------+----------------------------+-----------------------+---------------------+-----------------+-----------------+
| ID     | Topik Keputusan            | Pilihan Terpilih      | Pengambil Keputusan | Status Final    | Tanggal Kunci   |
+--------+----------------------------+-----------------------+---------------------+-----------------+-----------------+
| OD-001 | Nama Resmi Aplikasi        | Panduan Haji 2027     | Product Owner       | CLOSED & LOCKED | 26 Oktober 2026 |
| OD-002 | Penanggung Jawab Fikih     | Ustadz Rombongan      | Pembimbing Ibadah   | CLOSED & LOCKED | 26 Oktober 2026 |
| OD-003 | Model Biaya & Kapasitas    | Gratis Murni          | PO & Bendahara      | CLOSED & LOCKED | 26 Oktober 2026 |
| OD-004 | Jangkauan Distribusi       | Publik Bebas (Web/QR) | Tech Lead & PO      | CLOSED & LOCKED | 26 Oktober 2026 |
| OD-005 | Koordinat Pintu Masjid     | Tandai Mandiri        | QA & Lead Architect | CLOSED & LOCKED | 26 Oktober 2026 |
| OD-006 | Kepemilikan Keystore       | Yayasan / Organisasi  | Tech Lead & Legal   | CLOSED & LOCKED | 26 Oktober 2026 |
+--------+----------------------------+-----------------------+---------------------+-----------------+-----------------+
```

---

## 3. RINCIAN RESOLUSI & JUSTIFIKASI SETIAP KEPUTUSAN

### OD-001: Penetapan Nama Resmi Aplikasi
- **Keputusan:** Menggunakan nama **"Panduan Haji 2027"** (Application Name: `Panduan Haji 2027`, Package ID: `com.haji2027.app`).
- **Pilihan yang Dikesampingkan:**
  - *HajiGuide 2027:* Terlalu kebarat-baratan, berpotensi membingungkan bagi jemaah lansia daerah.
  - *Manasik Bersama:* Kurang mencerminkan fungsi spasial (kompas & penandaan tempat darurat).
- **Justifikasi:** Sederhana, bermartabat, berbahasa Indonesia baku, langsung dipahami oleh jemaah segala usia, dan tepat mengidentifikasi target operasional musim haji 1448 H / 2027 M.
- **Dampak Kode Sumber:** Diselaraskan di `app/src/main/res/values/strings.xml`, `distribution/index.html`, dan manifes rilis.

---

### OD-002: Penunjukan Penanggung Jawab Materi Fikih & Manasik
- **Keputusan:** Menunjuk **Ustadz Pembimbing Ibadah Rombongan / Dewan Asatidz Kloter** sebagai otoritas tunggal penelaah dan pengesah materi konten.
- **Pilihan yang Dikesampingkan:**
  - *Tim Gabungan Eksternal:* Berisiko memperlambat respon rilis darurat jika terjadi perubahan jadwal mendadak oleh otoritas Arab Saudi saat berada di Armuzna.
- **Justifikasi:** Memastikan keselarasan bimbingan tatap muka di tanah air dengan teks yang dibaca jemaah di ponsel. Ustadz bertanggung jawab memvalidasi 10 titik ikhtilaf fikih dan menandatangani metadata `content_v1.0.0.json`.
- **Dampak Kode Sumber:** Setiap pembaruan materi manasik via modul OTA (`OtaEngine.kt`) wajib menyertakan atribut versi ustadz penelaah (`author` & `publishedDate`) sebelum hash SHA-256 diaktifkan.

---

### OD-003: Model Biaya, Lisensi & Kapasitas Infrastruktur
- **Keputusan:** Menetapkan model **Gratis Murni 100% (Non-Profit, Zero Ads, Bebas Donasi In-App)** dengan biaya infrastruktur cloud disubsidi kas rombongan/yayasan.
- **Pilihan yang Dikesampingkan:**
  - *Iuran / Pembayaran In-App:* Membebani jemaah lansia, merusak kekhusyukan ibadah, dan memicu kerumitan regulasi Google Play Billing.
- **Justifikasi:** Berdasarkan analisis Bagian 39 SDD, total biaya serverless Firebase selama 40 hari masa operasional haji hanya berkisar **$25.00 – $28.20 (< Rp 500.000)** untuk 400–1.200 jemaah. Biaya ini sangat terjangkau untuk ditanggung penuh oleh kas rombongan/yayasan.
- **Dampak Kode Sumber:** Menghapus seluruh dependensi pustaka iklan, analitik komersial, atau modul gateway pembayaran. Menegakkan kepatuhan asas minimisasi data UU PDP No. 27/2022.

---

### OD-004: Jangkauan Akses & Distribusi Aplikasi
- **Keputusan:** Menetapkan distribusi **Publik Bebas via Web Statis Mandiri HTTPS & Kode QR Buku Saku**.
- **Pilihan yang Dikesampingkan:**
  - *Khusus Anggota Rombongan (Private Closed Group):* Membatasi kemanfaatan aplikasi bagi jemaah mandiri atau jemaah kloter lain yang tersesat di Armuzna.
- **Justifikasi:** Sesuai prinsip jariyah dan kemaslahatan umum, aplikasi dapat diunduh bebas oleh siapa saja tanpa perlu memasukkan nomor keanggotaan atau verifikasi KTP. Sideloading web statis memangkas hambatan birokrasi *closed testing* Google Play Store 14 hari 20 tester.
- **Dampak Kode Sumber:** Portal web mandiri `distribution/index.html` dan skrip otomatisasi hash rilis `distribution/generate_release_artifacts.sh`.

---

### OD-005: Strategi Penandaan Koordinat Pintu Masjid & Tempat Penting
- **Keputusan:** Menetapkan sistem **Tanpa Preset Hardcode — Jemaah Menandai Mandiri Secara Riil di Lokasi**.
- **Pilihan yang Dikesampingkan:**
  - *Koordinat Preset / Hardcoded Gate:* Pintu Masjidil Haram (misal Pintu Raja Abdulaziz / Pintu Salam) dan maktab Mina sering mengalami penutupan berkala atau pengalihan jalur oleh Askar/keamanan Saudi. Koordinat hardcode rentan menyesatkan jemaah ke pintu yang sedang ditutup beton proyek.
- **Justifikasi:** Jemaah diajarkan menandai pintu masuk riil saat mereka tiba di pelataran terbuka menggunakan mesin penstabilan `GpsAveragingEngine` (median 5 sampel). Dengan demikian, titik target adalah 100% jalur yang benar-benar mereka lewati saat berangkat.
- **Dampak Kode Sumber:** Memastikan basis data Room `saved_places` di `:feature:places` beroperasi dinamis tanpa ketergantungan pada seed data koordinat statis.

---

### OD-006: Tata Kelola Kepemilikan Keystore & Sertifikat Rilis
- **Keputusan:** Kepemilikan sertifikat penandatanganan rilis (*Release Keystore*) dipegang atas nama **Organisasi / Yayasan Lembaga Rombongan**, bukan perorangan developer.
- **Pilihan yang Dikesampingkan:**
  - *Keystore Pribadi Pengembang:* Berisiko hilang atau tidak dapat dipulihkan jika pengembang berhalangan atau berpindah tugas.
- **Justifikasi:** Menjamin keberlanjutan kelembagaan jangka panjang (*institutional continuity*). Berkas keystore produksi dicadangkan secara offline terenkripsi AES-256 pada 2 buah USB flash drive fisik terpisah (*cold storage*) yang disimpan di brankas kantor rombongan dan brankas penasihat teknis.
- **Dampak Kode Sumber:** Pipeline CI/CD GitHub Actions (`release.yml`) hanya menerima kunci via rahasia repository aman (*GitHub Repository Secrets*) dan dilarang keras menaruh berkas `.jks` mentah di dalam git tree.

---

## 4. LEMBAR PENGESAHAN KEPUTUSAN TERBUKA (SIGN-OFF BLOCK)

Dengan ditandatanganinya lembar pengesahan ini, seluruh keputusan terbuka OD-001 s.d OD-006 dinyatakan **SAH, DITERIMA, DAN DITUTUP PENUH**:

```
+----------------------------------------------------------------------------------------------------+
|                                    LEMBAR PENGESAHAN RESOLUSI                                      |
|                                    APLIKASI PANDUAN HAJI 2027                                      |
+----------------------------------------------------------------------------------------------------+
|                                                                                                    |
|  1. Product Owner / Pimpinan Rombongan        : [ DISETUJUI & DITANDATANGANI ]                     |
|     Nama                                      : H. Muhammad Ridwan, M.Pd.                          |
|     Peran                                     : Pengambil Keputusan OD-001, OD-003, OD-004         |
|     Tanggal                                   : 26 Oktober 2026                                    |
|                                                                                                    |
|  2. Pembimbing Ibadah / Otoritas Fikih        : [ DISETUJUI & DITANDATANGANI ]                     |
|     Nama                                      : Ustadz Ahmad Syafi'i, Lc., M.A.                    |
|     Peran                                     : Pengambil Keputusan OD-002 (Materi Fikih Manasik)  |
|     Tanggal                                   : 26 Oktober 2026                                    |
|                                                                                                    |
|  3. Lead Systems Architect & Tech Lead        : [ DISETUJUI & DITANDATANGANI ]                     |
|     Nama                                      : Lead Mobile & Cloud Architect                      |
|     Peran                                     : Pengambil Keputusan OD-004, OD-005, OD-006         |
|     Tanggal                                   : 26 Oktober 2026                                    |
|                                                                                                    |
|  4. Bendahara & Legal Organisasi              : [ DISETUJUI & DITANDATANGANI ]                     |
|     Nama                                      : Hj. Nurul Hidayah, S.E.                            |
|     Peran                                     : Pengambil Keputusan OD-003, OD-006                 |
|     Tanggal                                   : 26 Oktober 2026                                    |
|                                                                                                    |
+----------------------------------------------------------------------------------------------------+
| KESIMPULAN: SELURUH 6 OPEN DECISIONS SELESAI DITUNTASKAN. ARSITEKTUR SISTEM DIKUNCI MATI.          |
+----------------------------------------------------------------------------------------------------+
```
