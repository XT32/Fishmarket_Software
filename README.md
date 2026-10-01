# 🐟 Fish Market Desktop Software

Aplikasi desktop manajemen pasar ikan dan marketplace ritel modern berbasis **JavaFX (Java 21+)**, arsitektur kode bersih berstandar enterprise, antarmuka responsif adaptif (**Responsive FlowPane & Adaptive Grids**), konfigurasi dinamis via **`.env`**, dukungan multi-database (**MySQL, PostgreSQL, SQLite, dll.**), serta akselerasi cache performa tinggi dengan **Redis**.

---

## ✨ Fitur Utama & Pembaruan

### 1. 📱 UI Responsif & Maintainable
- **Responsive Fish FlowPane**: Penggantian grid kaku menjadi `FlowPane` responsif di dalam `ScrollPane(fitToWidth="true")`. Kartu produk menyesuaikan jumlah kolom secara dinamis (2 hingga 6 kolom) saat ukuran jendela diubah.
- **Adaptive Sidebar & Data Tables**:
  - Kolom tabel menggunakan `TableView.CONSTRAINED_RESIZE_POLICY` sehingga tabel selalu membentang penuh tanpa menyisakan ruang kosong atau terpotong saat jendela dimaksimalkan (*maximize*).
  - Kartu metrik statistik di dashboard admin menggunakan `HBox.hgrow="ALWAYS"` dengan batas `minWidth` adaptif.
  - Form panel terstruktur dan terkontrol dengan pemisahan komponen FXML dan CSS modular.
- **Stage Minimum Constraints**: Window memiliki batasan ukuran minimum (`minWidth="960"`, `minHeight="600"`) untuk mencegah elemen bertabrakan saat di-resize.

### 2. ⚙️ Konfigurasi Berbasis `.env` (Single Source of Truth)
- Manajemen konfigurasi terpusat melalui berkas `.env` dengan fallback otomatis ke System Environment Variables, System Properties, dan nilai default.
- Disediakan berkas template [`.env.example`](file:///.env.example).
- Hierarki prioritas: `System Properties (-D)` > `OS Environment Variables` > `.env` > `Default Values`.

### 3. 🗄️ Dukungan Multi-Database (MySQL, PostgreSQL, SQLite, dll.)
- **Auto-Dialect Detection**: Aplikasi secara otomatis mendeteksi tipe database dari konfigurasi `DB_TYPE` atau prefix `DB_URL` (`jdbc:mysql:`, `jdbc:postgresql:`, `jdbc:sqlite:`).
- **Dialect DDL Scripts**: Skrip inisialisasi skema tabel otomatis untuk setiap database:
  - MySQL: `schema.sql` (`AUTO_INCREMENT`, `INSERT IGNORE`)
  - PostgreSQL: `schema-postgres.sql` (`SERIAL PRIMARY KEY`, `ON CONFLICT DO NOTHING`)
  - SQLite: `schema-sqlite.sql` (`INTEGER PRIMARY KEY AUTOINCREMENT`, `INSERT OR IGNORE`)
- **Cross-Database ANSI SQL**: Seluruh query data layer dirancang portabel dan kompatibel dengan semua mesin database (misal penyesuaian pengurangan stok atomik portabel: `CASE WHEN stok >= ? THEN stok - ? ELSE 0 END`).

### 4. ⚡ Akselerasi Cache Redis dengan Graceful Fallback
- **Koneksi Jedis Pool**: Menggunakan connection pool thread-safe (`RedisManager`).
- **Katalog & Statistik Ter-cache**: Katalog ikan diserialisasi ke JSON melalui DTO bersih dan disimpan di Redis untuk akses instan tanpa query berulang ke database.
- **Cache Invalidation Otomatis**: Setiap perubahan inventaris (tambah, edit, hapus ikan) dan transaksi checkout otomatis menghapus (*invalidate*) cache agar data selalu sinkron.
- **Graceful Offline Degradation**: Jika Redis tidak aktif atau server offline, aplikasi **tidak akan crash**. Sistem secara otomatis mencatat peringatan dan mengalirkan data langsung dari database (*seamless fallback*).

### 5. 🔐 Keamanan & Otentikasi
- **Enkripsi Password BCrypt**: Password pengguna dan admin di-hash secara aman (`PasswordUtil`).
- **Role-Based Access Control**:
  - **Administrator**: Dashboard metrik, inventaris produk, rekap penjualan, data nelayan.
  - **Pembeli (Customer)**: Katalog ikan segar, keranjang belanja real-time, riwayat belanja, manajemen profil.
- **Default Akun**: Username `admin`, Password `admin123`.

---

## 🛠️ Arsitektur & Teknologi

- **Bahasa**: Java 21 (LTS)
- **GUI Framework**: JavaFX 23 (FXML + Modern CSS)
- **Database Driver**:
  - MySQL Connector/J (`com.mysql:mysql-connector-j:8.3.0`)
  - PostgreSQL JDBC Driver (`org.postgresql:postgresql:42.7.4`)
  - SQLite JDBC Driver (`org.xerial:sqlite-jdbc:3.47.1.0`)
- **In-Memory Cache**: Jedis (`redis.clients:jedis:5.2.0`) & Gson (`com.google.code.gson:gson:2.11.0`)
- **Enkripsi**: `jbcrypt` (0.4)
- **Logging**: SLF4J 2.0.16 + Logback Classic 1.5.16
- **Testing**: JUnit 5.11 & Mockito 5.11
- **Build Tool**: Maven Wrapper (`mvnw`)

---

## ⚙️ Panduan Konfigurasi `.env`

Salin berkas `.env.example` menjadi `.env`:
```bash
cp .env.example .env
```

Contoh konfigurasi `.env`:

```env
# ==========================================
# DATABASE CONFIGURATION
# Supported DB_TYPE: mysql | postgresql | sqlite
# ==========================================
DB_TYPE=mysql
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fishmarket
DB_USER=fishmarket_user
DB_PASSWORD=fishmarket_pass

# Atau gunakan direct JDBC URL (jika diisi, akan mengabaikan host/port individual):
# DB_URL=jdbc:postgresql://localhost:5432/fishmarket
# DB_URL=jdbc:sqlite:fishmarket.db

# ==========================================
# REDIS CACHE CONFIGURATION
# ==========================================
REDIS_ENABLED=true
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_TIMEOUT=2000
REDIS_TTL_SECONDS=300
```

### Cara Beralih Database:
- **Untuk MySQL**: Atur `DB_TYPE=mysql`, `DB_PORT=3306`, sesuaikan `DB_USER` dan `DB_PASSWORD`.
- **Untuk PostgreSQL**: Atur `DB_TYPE=postgresql`, `DB_PORT=5432`, `DB_USER=postgres`, `DB_PASSWORD=postgres`.
- **Untuk SQLite**: Atur `DB_TYPE=sqlite`, `DB_NAME=fishmarket` (akan otomatis membuat database lokal `fishmarket.db`).

---

## 🚀 Menjalankan Aplikasi

### 1. Menjalankan Tes Unit & Integrasi
```bash
./mvnw clean test
```
*Mencakup pengujian koneksi database, dialect schema PostgreSQL & SQLite, operasi cache Redis, resolusi EnvConfig, dan otentikasi login/register.*

### 2. Menjalankan GUI Aplikasi Desktop
```bash
./mvnw javafx:run
```

### 3. Membangun Executable JAR
```bash
./mvnw clean package -DskipTests
```
Berkas JAR akan berada di direktori `target/fishmarket-software-1.0.0.jar`.

---

## 📁 Struktur Proyek

```
Fishmarket_Software/
├── .env.example                 # Template konfigurasi environment
├── .env                         # Konfigurasi aktif (diabaikan oleh git)
├── pom.xml                      # Maven dependencies (JavaFX, MySQL, Postgres, SQLite, Jedis, Gson)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/      # Controller JavaFX (Login, Admin, User, FishCard)
│   │   │   ├── dao/             # Data Access Objects (BaseDAO multi-db, UserDAO, IkanDAO, dll.)
│   │   │   ├── model/           # Model data dengan properti binding JavaFX
│   │   │   ├── service/         # Business logic & RedisCacheService
│   │   │   ├── utils/           # EnvConfig, RedisManager, PasswordUtil, SessionManager
│   │   │   └── main/            # Titik masuk utama aplikasi (Newfishmarket.java)
│   │   └── resources/
│   │       ├── css/             # Modern stylesheet responsif (userView.css, loginView.css)
│   │       ├── database/        # Skrip DDL (schema.sql, schema-postgres.sql, schema-sqlite.sql)
│   │       ├── icon/            # Aset grafis & ikon
│   │       └── view/            # Tampilan responsif FXML (login, admin, user, kartu ikan)
│   └── test/
│       └── java/test/           # Test suite (Auth, BaseDAO, Postgres, SQLite, Redis, EnvConfig)
└── README.md                    # Dokumentasi lengkap proyek
```
