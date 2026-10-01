# 🐟 Fish Market Desktop Software

Aplikasi desktop manajemen pasar ikan dan marketplace ritel modern berbasis **JavaFX (Java 21+)** dan **MySQL**, dilengkapi sistem otentikasi aman (BCrypt), manajemen inventaris cerdas, portal belanja interaktif, pelaporan omset real-time, serta arsitektur kode bersih berstandar enterprise.

---

## ✨ Fitur Utama

### 1. 🔐 Keamanan & Otentikasi
- **Enkripsi Password BCrypt**: Password pengguna dan admin di-hash secara aman (`PasswordUtil`) tanpa menyimpan teks polos di database.
- **Role-Based Access Control**:
  - **Administrator**: Akses penuh ke dashboard metrik, inventaris produk, rekap penjualan, dan data nelayan mitra.
  - **Pembeli (Customer)**: Akses ke katalog ikan segar, keranjang belanja real-time, riwayat belanja, dan manajemen profil.
- **Akun Bawaan (Default)**:
  - **Admin**: Username `admin`, Password `admin123`
- **Sesi Terpusat**: Pengelolaan sesi berbasis `SessionManager`.

### 2. 🛒 Portal Pembeli (Marketplace)
- **Katalog Ikan Segar**: Tampilan kartu ikan interaktif (*fish card*) dengan informasi harga per kg, status stok (Tersedia/Habis), dan kuantitas pemesanan.
- **Pencarian Real-Time**: Filter cepat nama ikan langsung dari kolom pencarian.
- **Keranjang Belanja Dinamis**:
  - Penambahan barang langsung dengan penghitungan subtotal otomatis.
  - Pengaturan alamat pengiriman fleksibel.
  - Checkout instan yang secara otomatis mencatat transaksi dan mengurangi stok ikan di database dalam satu transaksi aman (*atomic transaction*).
- **Riwayat Belanja**: Tabel riwayat semua transaksi pembelian terdahulu milik pengguna.
- **Edit Profil**: Pembaruan nama lengkap, email, alamat rumah, dan pergantian password baru secara mandiri.

### 3. 📊 Portal Administrator (Backoffice)
- **Dashboard Ringkasan Eksekutif**:
  - Metrik statistik: Total Pelanggan, Omset Hari Ini, Total Pendapatan, dan Total Stok Ikan Tersedia (kg).
  - Tabel ringkasan transaksi terbaru.
- **Manajemen Inventaris Ikan (CRUD)**:
  - Tambah varietas ikan baru dengan harga, stok, dan pemasok nelayan.
  - Klik baris tabel untuk mengisi formulir secara otomatis (*autofill*).
  - Pembaruan (*Update*) dan Penghapusan (*Delete*) data ikan dengan konfirmasi keamanan.
  - Filter pencarian instan pada tabel inventaris.
- **Laporan Penjualan**: Rekap seluruh transaksi belanja yang dilakukan pelanggan.
- **Mitra Nelayan**: Kelola daftar nelayan pemasok hasil laut beserta nomor kontak.

---

## 🛠️ Arsitektur & Teknologi

- **Bahasa**: Java 21 (LTS)
- **GUI Framework**: JavaFX 23 (FXML + Vanilla Modern CSS)
- **Database**: MySQL 8+
- **Enkripsi**: `jbcrypt` (0.4)
- **Logging**: SLF4J 2.0.16 + Logback Classic 1.5.16
- **Testing**: JUnit 5.11 & Mockito 5.11
- **Build Tool**: Maven Wrapper (`mvnw`) terintegrasi

---

## 🚀 Panduan Menjalankan Aplikasi

### 1. Prasyarat
- **JDK 21** atau lebih baru terpasang di sistem (`java -version`).
- **MySQL Server** berjalan (lokal atau cloud).

### 2. Konfigurasi Database
Konfigurasi koneksi database disimpan di `src/main/resources/database.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/fishmarket?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=fishmarket_user
db.password=fishmarket_pass
```

> **Tips:** Anda juga dapat menggunakan Environment Variable:
> - `DB_URL`
> - `DB_USER`
> - `DB_PASSWORD`

Database dan tabel akan diinisialisasi secara otomatis saat aplikasi pertama kali dijalankan dari skrip `src/main/resources/database/schema.sql`.

### 3. Menjalankan Tes Unit
```bash
./mvnw clean test
```

### 4. Menjalankan Aplikasi
```bash
./mvnw javafx:run
```

### 5. Membangun Executable JAR
```bash
./mvnw clean package -DskipTests
```
Berkas JAR akan berada di direktori `target/fishmarket-software-1.0.0.jar`.

---

## 📁 Struktur Proyek

```
Fishmarket_Software/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/      # Controller JavaFX (Login, Admin, User, FishCard)
│   │   │   ├── dao/             # Data Access Objects (BaseDAO, UserDAO, IkanDAO, dll.)
│   │   │   ├── model/           # Model data dengan properti binding JavaFX
│   │   │   ├── service/         # Business logic layer
│   │   │   ├── utils/           # PasswordUtil (BCrypt), SessionManager, BaseDAO
│   │   │   └── main/            # Titik masuk utama aplikasi (Newfishmarket.java)
│   │   └── resources/
│   │       ├── css/             # Stylesheet tema modern (userView.css, loginView.css)
│   │       ├── database/        # Skrip DDL & seed data (schema.sql)
│   │       ├── icon/            # Aset grafis & ikon
│   │       ├── view/            # Tampilan FXML (login, admin, user, kartu ikan)
│   │       └── database.properties
│   └── test/
│       └── java/test/           # Unit test JUnit 5 (Auth, DAO, Cart, Password)
├── pom.xml                      # Konfigurasi Maven dependencies & plugins
├── .gitignore                   # Aturan pengabaian file build & IDE
└── README.md                    # Dokumentasi proyek
```
