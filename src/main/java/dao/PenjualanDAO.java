package dao;

import model.CartItem;
import model.Penjualan;
import service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PenjualanDAO {
    private static final Logger logger = LoggerFactory.getLogger(PenjualanDAO.class);

    private Connection connection;

    public PenjualanDAO() {}

    public PenjualanDAO(Connection connection) {
        this.connection = connection;
    }

    private Connection getActiveConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        return BaseDAO.getConnection();
    }

    public List<Penjualan> getAllPenjualan() {
        List<Penjualan> list = new ArrayList<>();
        String query = "SELECT * FROM penjualan ORDER BY id_transaksi DESC";
        try (Connection conn = getActiveConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Penjualan(
                        rs.getInt("id_transaksi"),
                        rs.getInt("id_user"),
                        rs.getInt("id_ikan"),
                        rs.getInt("kuantitas"),
                        rs.getString("tanggal"),
                        rs.getString("alamat"),
                        rs.getDouble("total")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving penjualan: {}", e.getMessage(), e);
        }
        return list;
    }

    public List<Penjualan> getPenjualanByUserId(int userId) {
        List<Penjualan> list = new ArrayList<>();
        String query = "SELECT * FROM penjualan WHERE id_user = ? ORDER BY id_transaksi DESC";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Penjualan(
                            rs.getInt("id_transaksi"),
                            rs.getInt("id_user"),
                            rs.getInt("id_ikan"),
                            rs.getInt("kuantitas"),
                            rs.getString("tanggal"),
                            rs.getString("alamat"),
                            rs.getDouble("total")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving user penjualan history: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean addPenjualan(Penjualan penjualan) {
        String query = "INSERT INTO penjualan (id_user, id_ikan, kuantitas, tanggal, alamat, total) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, penjualan.getIdUser());
            stmt.setInt(2, penjualan.getIdIkan());
            stmt.setInt(3, penjualan.getKuantitas());
            stmt.setString(4, penjualan.getTanggal());
            stmt.setString(5, penjualan.getAlamat());
            stmt.setDouble(6, penjualan.getTotal());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        penjualan.setIdTransaksi(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error adding penjualan: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * Executes a complete cart checkout in a single atomic transaction.
     */
    public boolean processCheckout(int userId, String alamat, List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return false;
        }

        String insertSale = "INSERT INTO penjualan (id_user, id_ikan, kuantitas, tanggal, alamat, total) VALUES (?, ?, ?, ?, ?, ?)";
        // Cross-DB compatible: CASE WHEN stok >= ? THEN stok - ? ELSE 0 END
        String reduceStock = "UPDATE ikan SET stok = CASE WHEN stok >= ? THEN stok - ? ELSE 0 END WHERE id_ikan = ?";
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        try (Connection conn = BaseDAO.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psSale = conn.prepareStatement(insertSale);
                 PreparedStatement psStock = conn.prepareStatement(reduceStock)) {

                double totalCart = 0.0;
                for (CartItem item : cartItems) {
                    totalCart += item.getSubtotal();

                    psSale.setInt(1, userId);
                    psSale.setInt(2, item.getIkan().getIdIkan());
                    psSale.setInt(3, item.getKuantitas());
                    psSale.setString(4, today);
                    psSale.setString(5, alamat != null ? alamat : "");
                    psSale.setDouble(6, item.getSubtotal());
                    psSale.addBatch();

                    psStock.setInt(1, item.getKuantitas());
                    psStock.setInt(2, item.getKuantitas());
                    psStock.setInt(3, item.getIkan().getIdIkan());
                    psStock.addBatch();
                }

                psSale.executeBatch();
                psStock.executeBatch();

                // Also record in pesanan
                String insertPesanan = "INSERT INTO pesanan (id_user, total_pembelian, status) VALUES (?, ?, 'Selesai')";
                try (PreparedStatement psPesanan = conn.prepareStatement(insertPesanan)) {
                    psPesanan.setInt(1, userId);
                    psPesanan.setDouble(2, totalCart);
                    psPesanan.executeUpdate();
                }

                conn.commit();

                // Invalidate Redis cache
                RedisCacheService.invalidateCatalogCache();

                logger.info("Checkout processed successfully for user id {}, total items: {}", userId, cartItems.size());
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Checkout failed, transaction rolled back: {}", e.getMessage(), e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Database error during checkout: {}", e.getMessage(), e);
            return false;
        }
    }

    public double getTotalRevenue() {
        String query = "SELECT COALESCE(SUM(total), 0) FROM penjualan";
        try (Connection conn = getActiveConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            logger.error("Error calculating total revenue: {}", e.getMessage(), e);
        }
        return 0.0;
    }

    public double getTodayIncome() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String query = "SELECT COALESCE(SUM(total), 0) FROM penjualan WHERE tanggal LIKE ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, today + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Error calculating today's income: {}", e.getMessage(), e);
        }
        return 0.0;
    }
}
