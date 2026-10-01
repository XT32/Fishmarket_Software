package dao;

import model.Ikan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IkanDAO {
    private static final Logger logger = LoggerFactory.getLogger(IkanDAO.class);

    private Connection connection;

    public IkanDAO() {
    }

    public IkanDAO(Connection connection) {
        this.connection = connection;
    }

    private Connection getActiveConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        return BaseDAO.getConnection();
    }

    public List<Ikan> getAllIkan() {
        List<Ikan> list = new ArrayList<>();
        String query = "SELECT * FROM ikan ORDER BY id_ikan ASC";
        try (Connection conn = getActiveConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Ikan(
                        rs.getInt("id_ikan"),
                        rs.getString("nama_ikan"),
                        rs.getDouble("harga"),
                        rs.getString("gambar_ikan"),
                        rs.getInt("stok"),
                        rs.getInt("id_nelayan")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving ikan list: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean addIkan(Ikan ikan) {
        String query = "INSERT INTO ikan (nama_ikan, harga, gambar_ikan, stok, id_nelayan) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ikan.getNamaIkan());
            stmt.setDouble(2, ikan.getHarga());
            stmt.setString(3, ikan.getGambarIkan());
            stmt.setInt(4, ikan.getStok());
            stmt.setInt(5, ikan.getIdNelayan() > 0 ? ikan.getIdNelayan() : 1);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ikan.setIdIkan(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error adding ikan: {}", e.getMessage(), e);
        }
        return false;
    }

    public boolean updateIkan(Ikan ikan) {
        String query = "UPDATE ikan SET nama_ikan = ?, harga = ?, gambar_ikan = ?, stok = ?, id_nelayan = ? WHERE id_ikan = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ikan.getNamaIkan());
            stmt.setDouble(2, ikan.getHarga());
            stmt.setString(3, ikan.getGambarIkan());
            stmt.setInt(4, ikan.getStok());
            stmt.setInt(5, ikan.getIdNelayan() > 0 ? ikan.getIdNelayan() : 1);
            stmt.setInt(6, ikan.getIdIkan());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating ikan: {}", e.getMessage(), e);
        }
        return false;
    }

    public boolean deleteIkan(int idIkan) {
        String query = "DELETE FROM ikan WHERE id_ikan = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idIkan);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting ikan {}: {}", idIkan, e.getMessage(), e);
        }
        return false;
    }

    public boolean reduceStock(int idIkan, int quantity) {
        String query = "UPDATE ikan SET stok = GREATEST(0, stok - ?) WHERE id_ikan = ? AND stok >= ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, idIkan);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error reducing stock for ikan {}: {}", idIkan, e.getMessage(), e);
        }
        return false;
    }

    public int countIkan() {
        String query = "SELECT COUNT(*) FROM ikan";
        try (Connection conn = getActiveConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting ikan: {}", e.getMessage(), e);
        }
        return 0;
    }

    public int getTotalStock() {
        String query = "SELECT COALESCE(SUM(stok), 0) FROM ikan";
        try (Connection conn = getActiveConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error calculating total stock: {}", e.getMessage(), e);
        }
        return 0;
    }
}
