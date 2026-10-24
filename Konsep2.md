# Konsep Aplikasi Panduan Haji 2027

*Draf konsep v2 (tanpa Play Store) · Android native · disusun dari diskusi dan ringkasan gabungan materi haji dan umrah*

## 1. Ringkasan

Aplikasi Android yang menemani jemaah haji 2027 dari persiapan sampai selesai. Ada tiga pilar:

1. **Panduan per tanggal Hijriah**: jemaah langsung tahu apa yang harus dilakukan pada hari H.
2. **Tandai tempat dan panah arah**: jemaah menandai pintu, hotel, atau titik kumpul, lalu dipandu panah arah dan jarak ke tempat itu.
3. **Lacak keluarga**: hanya orang yang didaftarkan lewat barcode, dengan persetujuan orang yang dilacak.

Prinsip rancangan: **offline-first** (panduan dan tandai tempat berjalan tanpa internet), **sederhana untuk lansia**, dan **privasi lebih dulu**.

**Distribusi:** aplikasi dibagikan langsung sebagai file APK (tautan unduh atau kode QR), tanpa Google Play, sehingga tidak ada kewajiban closed testing. Pendaftaran pengembang di Android Developer Console tetap disarankan (lihat bagian 7).

## 2. Masalah dan pengguna

**Masalah yang diselesaikan**

- Jemaah bingung urutan kegiatan pada hari-hari padat 8–13 Zulhijah.
- Jemaah tersesat di area yang luas dan padat, misalnya Masjidil Haram.
- Jemaah terpisah dari pasangan atau orang tua.

**Pengguna**

- **Jemaah**, termasuk lansia dan yang tidak terbiasa dengan aplikasi.
- **Pelacak**: suami, istri, anak, atau kerabat yang memantau posisi jemaah yang mereka daftarkan.
- **Pengelola konten**: pembimbing atau ustaz yang memeriksa dan menetapkan isi panduan.

**Prinsip antarmuka**

- Huruf dan tombol besar, kontras tinggi, sedikit langkah, bahasa Indonesia sederhana.
- Beranda berupa satu layar “Hari ini” dengan tiga tombol besar: Panduan, Tempat saya, Keluarga.

## 3. Perjalanan pengguna dari awal sampai akhir

| Tahap | Yang dilakukan jemaah | Dukungan aplikasi |
| --- | --- | --- |
| 0. Sebelum berangkat | Memasang aplikasi dari file APK (dibantu keluarga atau pembimbing), mengatur izin, mendaftarkan keluarga lewat barcode, mengunduh data | Onboarding, pairing barcode, unduh konten offline, panduan persiapan (kewajiban, keutamaan, persiapan hati, tobat) |
| 1. Perjalanan, tiba, dan hotel | Menandai hotel, titik kumpul, dan titik bus; berihram di miqat | Tandai tempat, panah arah, panduan miqat, niat, talbiah, dan larangan ihram |
| 2. Umrah (tamattu’) | Tawaf, sa’i, tahalul | Panduan langkah demi langkah, bacaan, ceklis |
| 3. Masa menunggu haji | Ibadah dan tawaf sunah | Panduan masa menunggu, tips kondisi Makkah, tempat tersimpan |
| 4. 8–13 Zulhijah | Mina, Arafah, Muzdalifah, jumrah, hadyu, tahalul, tawaf ifadah, hari tasyrik | Beranda “Hari ini” sesuai tanggal Hijriah, ceklis, pengingat, pelacakan keluarga aktif |
| 5. Tawaf wada dan Madinah | Tawaf perpisahan, ibadah di Madinah | Panduan tawaf wada dan Madinah |
| 6. Setelah haji | Pulang | Pelacakan berhenti otomatis, data lokasi dihapus, akun dapat dihapus |

## 4. Fitur 1: Panduan per tanggal Hijriah

**Tujuan:** jemaah membuka aplikasi dan langsung melihat apa yang harus dilakukan hari ini.

**Cara kerja**

- Tanggal Hijriah dihitung otomatis dari kalender Umm al-Qura bawaan Android (HijrahChronology, tanpa pustaka tambahan), dengan tombol geser tanggal manual karena tanggal pelaksanaan mengikuti pengumuman resmi (Zulhijah 1448 H, diperkirakan sekitar Mei 2027).
- Beranda menampilkan kegiatan hari ini secara berurutan, lalu kegiatan berikutnya.
- Setiap kegiatan dapat dibuka menjadi: langkah-langkah, ketentuan penting, larangan terkait, dan bacaan.
- Bacaan (talbiah, doa tawaf, doa Zamzam, zikir Safa dan Marwah, doa Arafah) ditampilkan dalam huruf Arab, Latin, dan arti.
- Ceklis per kegiatan dan penanda progres hari ini.
- Pengingat lokal (notifikasi) pada jam yang diisi jemaah atau pembimbing, misalnya jadwal berangkat ke Mina atau Arafah.
- Pencarian cepat (misalnya “jumrah”, “fidyah”) dan rujukan cepat untuk larangan ihram serta fidyah dan dam.

**Pengelompokan isi** (mengikuti ringkasan gabungan)

1. Sebelum berangkat: kewajiban, keutamaan, cara meraih haji mabrur, persiapan hati dan tobat.
2. Persiapan: waktu dan jenis haji, sunah sebelum ihram, perjalanan ke Makkah, miqat dan niat, talbiah, larangan ihram, fidyah dan dam.
3. Umrah dan masa menunggu: tawaf, salat dan Zamzam, sa’i, tahalul.
4. Per tanggal: 8 Zulhijah (Tarwiyah), 9 (Arafah), malam 10 (Muzdalifah), 10 (Nahar), 11, 12, dan 13 (hari tasyrik).
5. Setelah haji: tawaf wada, Madinah, kekompakan dan keikhlasan.

**Cara menyimpan isi:** data terstruktur (Hari → Kegiatan → Langkah), berformat JSON di dalam aplikasi (bukan di database), disertai nomor versi dan tanggal revisi yang tampil di aplikasi. Isi cukup ditulis sekali dan dapat diperbarui tanpa memasang ulang aplikasi.

## 5. Fitur 2: Tandai tempat dan panah arah

**Tujuan:** jemaah dapat kembali ke pintu, hotel, atau titik kumpul yang sudah ditandai, dipandu panah arah dan jarak.

**Alur**

1. Jemaah berdiri di suatu tempat, lalu menekan “Tandai tempat ini”.
2. Memberi nama dan kategori (pintu, hotel, titik kumpul, bus, lainnya), misalnya “Pintu 15”.
3. Aplikasi menyimpan koordinat, tingkat akurasi, dan waktu penandaan, serta (opsional) foto dan catatan singkat tentang ciri tempat itu, misalnya papan nomor pintu.
4. Kemudian saat berjalan, jemaah memilih tempat itu dari daftar. Layar panah menampilkan arah dan jarak yang diperbarui terus-menerus.

**Cara kerja teknis**

- Posisi sekarang dari API lokasi Android, arah hadap HP dari sensor rotasi (kompas) dengan koreksi deklinasi magnetik (GeomagneticField).
- Arah dan jarak dihitung dengan fungsi bawaan Android (distanceTo dan bearingTo); panah diputar mengikuti arah hadap HP. Komponen panah Compose yang sudah ada (misalnya hj-qibla-compass) dapat dipakai bila lolos uji.
- Saat menandai, aplikasi merata-ratakan beberapa pembacaan GPS selama beberapa detik agar titik lebih stabil.
- Seluruhnya berjalan di HP, **tanpa server dan tanpa internet**.

**Batasan yang jujur (ditampilkan juga kepada pengguna)**

- Di dalam bangunan besar, akurasi GPS menurun dan lantai tidak terdeteksi; panah bisa meleset puluhan meter.
- Panah menunjukkan garis lurus, bukan jalur jalan; aplikasi tidak tahu tembok, tangga, atau jalur satu arah.
- Kompas dapat terganggu logam dan perlu dikalibrasi; aplikasi menampilkan petunjuk kalibrasi.
- Aplikasi menampilkan indikator akurasi sehingga jemaah tahu seberapa bisa dipercaya panahnya.

**Data pintu Masjidil Haram:** aplikasi tidak membawa koordinat pintu kecuali ada sumber resmi yang terverifikasi. Selama belum ada, jemaah menandai sendiri, atau pembimbing menandai lalu membagikannya ke rombongan.

**Pengembangan lanjutan (opsional):** membagikan tempat tertentu kepada keluarga terdaftar; menampilkan peta dasar bila ada data yang tersedia.

## 6. Fitur 3: Lacak keluarga lewat barcode

**Prinsip:** hanya orang yang didaftarkan; yang menampilkan barcode adalah yang mengizinkan dirinya dilacak.

**Alur pendaftaran**

1. Orang yang akan dilacak (misalnya ayah) membuka “Bagikan lokasi saya”, lalu aplikasi menampilkan barcode.
2. Pelacak (misalnya anak) memindai barcode itu dengan aplikasinya.
3. Pelacak masuk ke daftar “Orang yang saya lacak”; orang yang dilacak melihat pelacak itu di “Siapa yang melacak saya”.
4. Pasangan yang ingin saling melacak masing-masing menampilkan dan memindai barcode.

**Pengamanan barcode:** berisi token sekali pakai yang kedaluwarsa dalam beberapa menit; tidak memuat data pribadi.

**Kontrol orang yang dilacak**

- Melihat daftar siapa saja yang melacaknya.
- Menjeda atau menghentikan pelacakan kapan saja, dan mencabut izin satu orang.
- Notifikasi tetap di HP selama lokasinya dibagikan.
- Pelacakan berhenti otomatis pada tanggal akhir perjalanan.

**Tampilan bagi pelacak**

- Daftar orang yang dilacak dengan keterangan “terakhir terlihat X menit lalu” dan tingkat akurasi.
- Panah arah dan jarak menuju orang tersebut, memakai layar panah yang sama dengan fitur tandai tempat.

**Cara kerja teknis**

- HP orang yang dilacak mengirim lokasi ke server memakai *foreground service*, sehingga tetap jalan saat layar terkunci.
- Interval adaptif untuk menghemat baterai (misalnya setiap 30–60 detik saat bergerak, lebih jarang saat diam).
- Jika tidak ada internet, lokasi diantre dan dikirim ketika sinyal kembali.

**Aturan Android untuk layanan lokasi:** layanan dideklarasikan bertipe lokasi (izin FOREGROUND\_SERVICE\_LOCATION) dan dimulai saat aplikasi sedang terbuka, karena layanan lokasi tidak bisa dimulai dari latar belakang tanpa izin lokasi latar belakang. Layanan tidak dijalankan otomatis saat HP menyala; jika HP restart, aplikasi menampilkan pengingat agar dibuka lagi. Dengan begitu izin lokasi latar belakang kemungkinan tidak diperlukan. Pada merek tertentu (Xiaomi, Oppo, Vivo) sistem hemat baterai dapat mematikan pelacakan, sehingga aplikasi menyediakan panduan pengaturan baterai per merek.

**Keterbatasan:** kedua sisi memerlukan internet; akurasi di dalam bangunan rendah; pelacakan menambah konsumsi baterai. Untuk jemaah lansia, pendaftaran sebaiknya dilakukan bersama keluarga sebelum berangkat dan aplikasinya cukup dibiarkan berjalan.

## 7. Arsitektur teknis

**Aplikasi Android**

- Bahasa dan antarmuka: Kotlin dengan Jetpack Compose.
- Penyimpanan lokal: Room (SQLite) untuk tempat tersimpan dan ceklis; konten panduan berupa berkas JSON.
- Lokasi: Fused Location Provider; arah: sensor rotasi.
- Barcode: pembuat QR (ZXing) dan pemindai ML Kit.
- Pengingat: WorkManager atau AlarmManager.
- Pelacakan: foreground service dengan izin lokasi bertahap.

**Server** (hanya untuk fitur pelacakan dan pembaruan konten)

- Pilihan ringkas: Firebase (autentikasi, database waktu nyata, fungsi server untuk token barcode dan aturan akses). Alternatif: server sendiri.
- Pembaruan konten panduan lewat file data berversi.

**Model data utama**

| Data | Isi pokok |
| --- | --- |
| Hari | Tanggal Hijriah, judul, urutan |
| Kegiatan | Hari, nama, ringkasan, langkah, ketentuan, larangan terkait, bacaan |
| Tempat | Nama, kategori, koordinat, akurasi, waktu ditandai |
| Hubungan pelacakan | Pelacak, yang dilacak, status, waktu kedaluwarsa |
| Lokasi terakhir | Pemilik, koordinat, akurasi, waktu |

**Distribusi dan pembaruan (tanpa Play Store)**

- Aplikasi dibagikan sebagai file APK bertanda tangan lewat tautan unduh (HTTPS), kode QR, dan grup rombongan. Tidak ada review maupun closed testing Google Play.
- Pemasangan membutuhkan izin pasang dari sumber tidak dikenal di HP; halaman unduh menyertakan panduan bergambar, dan pemasangan dibantu keluarga atau pembimbing sebelum berangkat.
- Aturan verifikasi pengembang Google: batas 30 September 2026 di Indonesia baru berlaku untuk toko aplikasi yang berpartisipasi. Aplikasi yang dibagikan langsung (sideload) belum terkena, tetapi Google menyarankan verifikasi selesai sebelum penerapan global pada 2027, dan jadwal pastinya belum jelas. Musim haji sekitar Mei 2027, jadi daftar lebih awal agar pemasangan dan pembaruan tidak terhalang.
- Dua jalur akun di Android Developer Console: **Limited distribution** (gratis, tanpa ID pemerintah, maksimal 20 perangkat yang didaftarkan) dan **Full distribution** (biaya sekali US$25, perlu verifikasi identitas, tanpa batas 20 perangkat). Pengujian dan keluarga inti cukup dengan Limited; satu rombongan atau lebih membutuhkan Full. Limited dapat dinaikkan ke Full kemudian, tidak sebaliknya.
- Nama paket dan kunci tanda tangan didaftarkan. Kunci (keystore) dicadangkan di dua tempat aman; jika hilang, nama paket tidak dapat didaftarkan.
- Tidak ada pembaruan otomatis: aplikasi memeriksa nomor versi terbaru ke server dan menampilkan tombol unduh; konten panduan diperbarui lewat berkas data berversi tanpa memasang ulang aplikasi.
- Untuk pengembangan dan pengujian sendiri, pemasangan lewat ADB tidak memerlukan verifikasi.

## 8. Privasi, keamanan, dan kepatuhan

- Persetujuan eksplisit dari setiap orang yang dilacak; dapat dicabut kapan saja.
- Data minimal: hanya lokasi terakhir dan hubungan pelacakan; riwayat lokasi disimpan sesingkat mungkin atau tidak disimpan.
- Data lokasi dihapus otomatis setelah perjalanan berakhir; pengguna dapat menghapus akun dan seluruh datanya.
- Komunikasi terenkripsi, aturan akses server hanya mengizinkan pelacak yang terdaftar.
- Kebijakan privasi yang jelas, tersedia di dalam aplikasi dan di halaman unduhan, serta terkaji terhadap peraturan pelindungan data pribadi yang berlaku.
- Tanpa Google Play tidak ada review Play, tetapi aturan Android tetap berlaku: izin lokasi diminta bertahap, layanan lokasi dideklarasikan bertipe lokasi, dan notifikasi tetap tampil selama lokasi dibagikan.
- Perlu dicek ketentuan penggunaan aplikasi pelacakan selama di Arab Saudi, dan berkoordinasi dengan penyelenggara perjalanan.

## 9. Isi konten dan validasi

- Sumber awal: ringkasan gabungan materi haji dan umrah (8 ringkasan).
- Ringkasan memuat **10 titik perbedaan antar sumber** (misalnya rukun umrah, salat jamak di Mina, raml pada tawaf ifadah, jumlah doa di Safa). Sebelum dipakai jemaah, pembimbing atau ustaz perlu menetapkan satu versi final.
- Tunjuk satu penanggung jawab konten; tampilkan nomor versi dan tanggal revisi di aplikasi.
- Konten tidak ditambah dari luar sumber tanpa pemeriksaan penanggung jawab.

## 10. Peta jalan

| Fase | Isi | Hasil |
| --- | --- | --- |
| 0. Persiapan | Putuskan versi final konten, nama aplikasi, dan jalur distribusi; daftar akun Android Developer Console dan selesaikan verifikasi | Konten final, keputusan dasar, dan akun pengembang terdaftar |
| 1. MVP offline | Panduan per tanggal Hijriah dan tandai tempat dengan panah arah | Aplikasi yang sudah berguna tanpa server |
| 2. Pelacakan | Akun, barcode, server, pelacakan dengan persetujuan | Fitur lacak keluarga |
| 3. Uji lapangan | Uji akurasi di area terbuka dan masjid, uji baterai, uji dengan jemaah lansia, uji tanpa sinyal, uji di HP sungguhan berbagai merek (termasuk Xiaomi, Oppo, Vivo) | Perbaikan berdasarkan hasil uji |
| 4. Distribusi | Daftarkan nama paket dan kunci tanda tangan, siapkan halaman unduh dan kode QR, panduan pemasangan, uji pemasangan dan pembaruan di berbagai HP | Aplikasi terpasang di HP jemaah sebelum musim haji |
| 5. Operasional | Dukungan pengguna, pembaruan aplikasi, dan pembaruan konten selama musim haji | Aplikasi stabil selama perjalanan |
| 6. Penutupan | Hapus data lokasi dan evaluasi | Data bersih, bahan perbaikan tahun berikutnya |

## 11. Risiko dan penanganan

| Risiko | Penanganan |
| --- | --- |
| GPS lemah di dalam bangunan | Tampilkan indikator akurasi, janjikan hanya “arah umum dan jarak kasar”, anjurkan menandai di area terbuka |
| Kompas terganggu | Petunjuk kalibrasi di aplikasi |
| Baterai cepat habis | Interval adaptif, mode hemat, anjurkan membawa penyedia daya |
| Pelacakan dimatikan sistem hemat baterai (Xiaomi, Oppo, Vivo) | Panduan pengaturan baterai per merek di aplikasi; uji di merek tersebut |
| Tidak ada internet | Panduan dan tempat tersimpan tetap jalan offline; lokasi pelacakan diantre |
| Isi panduan keliru atau berbeda pendapat | Validasi pembimbing, penanggung jawab konten, versi dan tanggal revisi |
| Privasi dan penyalahgunaan pelacakan | Persetujuan lewat barcode, kontrol cabut izin, notifikasi tetap, hapus data otomatis |
| Aplikasi tidak bisa dipasang atau diperbarui karena pengembang belum terverifikasi | Daftar dan verifikasi lebih awal di Android Developer Console; pilih jalur Limited (maks. 20 perangkat) atau Full (US$25 sekali) |
| Pembaruan tidak sampai ke jemaah | Pemeriksaan versi di dalam aplikasi, tautan unduh tetap, pengumuman lewat grup rombongan |
| Kunci tanda tangan hilang | Cadangkan keystore di dua tempat aman |
| Pengguna lansia kesulitan memasang atau memakai | Pemasangan dibantu keluarga atau pembimbing sebelum berangkat; antarmuka sederhana |

## 12. Di luar cakupan versi 1 dan keputusan terbuka

**Di luar cakupan versi 1**

- Rute jalan di dalam masjid atau peta dalam ruangan dan deteksi lantai.
- Versi iPhone (dapat dipertimbangkan kemudian, misalnya dengan kerangka kerja lintas platform).
- Fitur percakapan dan tombol darurat.
- Distribusi lewat Google Play (tidak direncanakan, untuk menghindari kewajiban closed testing).

**Keputusan yang perlu dijawab**

1. Nama aplikasi.
2. Siapa penanggung jawab konten (pembimbing atau ustaz).
3. Gratis atau berbayar, dan perkiraan jumlah pengguna (menentukan biaya server dan jalur akun Android: Limited maks. 20 perangkat atau Full US$25).
4. Apakah aplikasi untuk rombongan tertentu atau umum.
5. Apakah ada sumber koordinat pintu Masjidil Haram yang terverifikasi.
6. Atas nama siapa akun pengembang didaftarkan (pribadi atau organisasi) dan siapa yang menyimpan kunci tanda tangan.

## Sumber

- [Android developer verification: FAQ (Google, diperbarui 30 September 2026)](https://developer.android.com/developer-verification/guides/faq): penerapan di toko yang berpartisipasi, jalur Limited dan Full, kunci tanda tangan. Diakses 24 Oktober 2026.
