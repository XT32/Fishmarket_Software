package dao;

import model.Nelayan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NelayanDAO {
    private static final Logger logger = LoggerFactory.getLogger(NelayanDAO.class);

    private Connection connection;

    public NelayanDAO() {}

    public NelayanDAO(Connection connection) {
        this.connection = connection;
    }

    private Connection getActiveConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        return BaseDAO.getConnection();
    }

    public List<Nelayan> getAllNelayan() {
        List<Nelayan> list = new ArrayList<>();
        String query = "SELECT * FROM nelayan ORDER BY id_nelayan ASC";
        try (Connection conn = getActiveConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Nelayan(
                        rs.getInt("id_nelayan"),
                        rs.getString("nama_nelayan"),
                        rs.getString("nomor_telepon")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving nelayan: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean addNelayan(Nelayan nelayan) {
        String query = "INSERT INTO nelayan (nama_nelayan, nomor_telepon) VALUES (?, ?)";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nelayan.getNamaNelayan());
            stmt.setString(2, nelayan.getNomorTelepon());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        nelayan.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error adding nelayan: {}", e.getMessage(), e);
        }
        return false;
    }

    public boolean updateNelayan(Nelayan nelayan) {
        String query = "UPDATE nelayan SET nama_nelayan = ?, nomor_telepon = ? WHERE id_nelayan = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nelayan.getNamaNelayan());
            stmt.setString(2, nelayan.getNomorTelepon());
            stmt.setInt(3, nelayan.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating nelayan: {}", e.getMessage(), e);
        }
        return false;
    }

    public boolean deleteNelayan(int idNelayan) {
        String query = "DELETE FROM nelayan WHERE id_nelayan = ?";
        try (Connection conn = getActiveConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idNelayan);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting nelayan: {}", e.getMessage(), e);
        }
        return false;
    }

    public int countNelayan() {
        String query = "SELECT COUNT(*) FROM nelayan";
        try (Connection conn = getActiveConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting nelayan: {}", e.getMessage(), e);
        }
        return 0;
    }
}
