-- PostgreSQL Fishmarket Database Schema and Seed Data

CREATE TABLE IF NOT EXISTS users (
    id_user SERIAL PRIMARY KEY,
    nama_lengkap VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    alamat TEXT,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nelayan (
    id_nelayan SERIAL PRIMARY KEY,
    nama_nelayan VARCHAR(100) NOT NULL,
    nomor_telepon VARCHAR(30),
    alamat VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS ikan (
    id_ikan SERIAL PRIMARY KEY,
    nama_ikan VARCHAR(100) NOT NULL,
    harga DOUBLE PRECISION NOT NULL,
    gambar_ikan VARCHAR(255),
    stok INT NOT NULL DEFAULT 0,
    id_nelayan INT REFERENCES nelayan(id_nelayan) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS pembelian (
    id_pembelian SERIAL PRIMARY KEY,
    id_ikan INT NOT NULL,
    id_nelayan INT NOT NULL,
    jumlah_beli INT NOT NULL,
    harga_total DOUBLE PRECISION NOT NULL,
    tanggal_pembelian TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pesanan (
    id_order SERIAL PRIMARY KEY,
    id_user INT NOT NULL,
    total_pembelian DOUBLE PRECISION NOT NULL,
    tanggal_transaksi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'Selesai'
);

CREATE TABLE IF NOT EXISTS penjualan (
    id_transaksi SERIAL PRIMARY KEY,
    id_user INT NOT NULL,
    id_ikan INT NOT NULL,
    kuantitas INT NOT NULL,
    tanggal VARCHAR(50),
    alamat TEXT,
    total DOUBLE PRECISION NOT NULL
);

-- Seed Fisherman
INSERT INTO nelayan (id_nelayan, nama_nelayan, nomor_telepon, alamat) VALUES
(1, 'Pak Joko Raharjo', '081234567890', 'Pelabuhan Ratu No. 12'),
(2, 'Pak Bambang Santoso', '082198765432', 'Muara Baru Dermaga A'),
(3, 'Pak Hendra Wijaya', '085711223344', 'Pantai Indah No. 5')
ON CONFLICT (id_nelayan) DO NOTHING;

-- Seed Fish
INSERT INTO ikan (id_ikan, nama_ikan, harga, gambar_ikan, stok, id_nelayan) VALUES
(1, 'Ikan Salmon Segar', 85000, 'salmon.png', 45, 1),
(2, 'Ikan Tuna Sirip Kuning', 65000, 'tuna.png', 30, 2),
(3, 'Ikan Kerapu Cantang', 75000, 'kerapu.png', 25, 1),
(4, 'Ikan Kakap Merah', 55000, 'kakap.png', 50, 3),
(5, 'Ikan Bawal Hitam', 45000, 'bawal.png', 40, 2),
(6, 'Ikan Gurame Hidup', 38000, 'gurame.png', 60, 3),
(7, 'Cumi Segar Tube', 58000, 'cumi.png', 35, 1),
(8, 'Udang Vaname Super', 70000, 'udang.png', 55, 2)
ON CONFLICT (id_ikan) DO NOTHING;
