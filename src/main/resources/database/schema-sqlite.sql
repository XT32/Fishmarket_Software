-- SQLite Fishmarket Database Schema and Seed Data

CREATE TABLE IF NOT EXISTS users (
    id_user INTEGER PRIMARY KEY AUTOINCREMENT,
    nama_lengkap TEXT NOT NULL,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    alamat TEXT,
    role TEXT DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nelayan (
    id_nelayan INTEGER PRIMARY KEY AUTOINCREMENT,
    nama_nelayan TEXT NOT NULL,
    nomor_telepon TEXT,
    alamat TEXT
);

CREATE TABLE IF NOT EXISTS ikan (
    id_ikan INTEGER PRIMARY KEY AUTOINCREMENT,
    nama_ikan TEXT NOT NULL,
    harga REAL NOT NULL,
    gambar_ikan TEXT,
    stok INTEGER NOT NULL DEFAULT 0,
    id_nelayan INTEGER
);

CREATE TABLE IF NOT EXISTS pembelian (
    id_pembelian INTEGER PRIMARY KEY AUTOINCREMENT,
    id_ikan INTEGER NOT NULL,
    id_nelayan INTEGER NOT NULL,
    jumlah_beli INTEGER NOT NULL,
    harga_total REAL NOT NULL,
    tanggal_pembelian TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pesanan (
    id_order INTEGER PRIMARY KEY AUTOINCREMENT,
    id_user INTEGER NOT NULL,
    total_pembelian REAL NOT NULL,
    tanggal_transaksi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status TEXT DEFAULT 'Selesai'
);

CREATE TABLE IF NOT EXISTS penjualan (
    id_transaksi INTEGER PRIMARY KEY AUTOINCREMENT,
    id_user INTEGER NOT NULL,
    id_ikan INTEGER NOT NULL,
    kuantitas INTEGER NOT NULL,
    tanggal TEXT,
    alamat TEXT,
    total REAL NOT NULL
);

INSERT OR IGNORE INTO nelayan (id_nelayan, nama_nelayan, nomor_telepon, alamat) VALUES
(1, 'Pak Joko Raharjo', '081234567890', 'Pelabuhan Ratu No. 12'),
(2, 'Pak Bambang Santoso', '082198765432', 'Muara Baru Dermaga A'),
(3, 'Pak Hendra Wijaya', '085711223344', 'Pantai Indah No. 5');

INSERT OR IGNORE INTO ikan (id_ikan, nama_ikan, harga, gambar_ikan, stok, id_nelayan) VALUES
(1, 'Ikan Salmon Segar', 85000, 'salmon.png', 45, 1),
(2, 'Ikan Tuna Sirip Kuning', 65000, 'tuna.png', 30, 2),
(3, 'Ikan Kerapu Cantang', 75000, 'kerapu.png', 25, 1),
(4, 'Ikan Kakap Merah', 55000, 'kakap.png', 50, 3),
(5, 'Ikan Bawal Hitam', 45000, 'bawal.png', 40, 2),
(6, 'Ikan Gurame Hidup', 38000, 'gurame.png', 60, 3),
(7, 'Cumi Segar Tube', 58000, 'cumi.png', 35, 1),
(8, 'Udang Vaname Super', 70000, 'udang.png', 55, 2);
