package dao;

import model.Pembelian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PembelianDAO {
    private static final Logger logger = LoggerFactory.getLogger(PembelianDAO.class);

    private Connection connection;

    public PembelianDAO() {}

    public PembelianDAO(Connection connection) {
        this.connection = connection;
    }

    private Connection getActiveConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        return BaseDAO.getConnection();
    }

    public List<Pembelian> getAllPembelian() {
        List<Pembelian> list = new ArrayList<>();
        String query = "SELECT * FROM pembelian ORDER BY tanggal_pembelian DESC";
        try (Connection conn = getActiveConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Pembelian(
                        rs.getInt("id_pembelian"),
                        rs.getInt("id_ikan"),
                        rs.getInt("id_nelayan"),
                        rs.getInt("jumlah_beli"),
                        rs.getDouble("harga_total"),
                        rs.getTimestamp("tanggal_pembelian").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving pembelian: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean addPembelian(Pembelian pembelian) {
        String query = "INSERT INTO pembelian (id_ikan, id_nelayan, jumlah_beli, harga_total, tanggal_pembelian) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pembelian.getIdIkan());
            stmt.setInt(2, pembelian.getIdNelayan());
            stmt.setInt(3, pembelian.getJumlahBeli());
            stmt.setDouble(4, pembelian.getHargaTotal());
            stmt.setTimestamp(5, Timestamp.valueOf(pembelian.getTanggalPembelian()));
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        pembelian.setIdPembelian(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error adding pembelian: {}", e.getMessage(), e);
        }
        return false;
    }

    public boolean deletePembelian(int idPembelian) {
        String query = "DELETE FROM pembelian WHERE id_pembelian = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idPembelian);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting pembelian: {}", e.getMessage(), e);
        }
        return false;
    }
}
