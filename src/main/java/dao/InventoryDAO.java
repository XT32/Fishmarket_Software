package dao;

import model.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(InventoryDAO.class);

    public void addInventoryItem(InventoryItem item) {
        String query = "INSERT INTO ikan (nama_ikan, harga, gambar_ikan, stok, id_nelayan) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = BaseDAO.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, item.getName());
            stmt.setDouble(2, 0.0);
            stmt.setString(3, "");
            stmt.setInt(4, item.getStock());
            stmt.setInt(5, 1);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding inventory item: {}", e.getMessage(), e);
        }
    }

    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT id_ikan, nama_ikan, stok FROM ikan";
        try (Connection conn = BaseDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                items.add(new InventoryItem(
                        rs.getInt("id_ikan"),
                        rs.getString("nama_ikan"),
                        "Ikan Segar",
                        rs.getInt("stok"),
                        rs.getInt("stok") > 0 ? "Tersedia" : "Habis",
                        "Hari ini"
                ));
            }
        } catch (SQLException e) {
            logger.error("Error loading inventory items: {}", e.getMessage(), e);
        }
        return items;
    }
}
