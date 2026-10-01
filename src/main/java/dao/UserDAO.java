package dao;

import model.User;
import utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    // Register a new user with BCrypt hashed password
    public boolean registerUser(User user) {
        String query = "INSERT INTO users (nama_lengkap, username, alamat, email, password, role) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = BaseDAO.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());

            stmt.setString(1, user.getNamaLengkap());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getAlamat());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, hashedPassword);
            stmt.setString(6, user.getRole() != null ? user.getRole() : "USER");

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setIdUser(rs.getInt(1));
                    }
                }
                logger.info("User registered successfully: {}", user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
        }
        return false;
    }

    // Login a user and verify BCrypt hash
    public User loginUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = BaseDAO.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");

                    if (PasswordUtil.checkPassword(password, storedHash)) {
                        String role = rs.getString("role");
                        if (role == null || role.isEmpty()) {
                            role = "admin".equalsIgnoreCase(username) ? "ADMIN" : "USER";
                        }

                        return new User(
                                rs.getInt("id_user"),
                                rs.getString("username"),
                                rs.getString("email"),
                                storedHash,
                                rs.getString("alamat"),
                                rs.getString("nama_lengkap"),
                                role
                        );
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error during user login: {}", e.getMessage(), e);
        }
        return null;
    }

    // Check if username already exists
    public boolean isUsernameExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = BaseDAO.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking username existence: {}", e.getMessage(), e);
        }
        return false;
    }

    // Check if email already exists
    public boolean isEmailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = BaseDAO.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking email existence: {}", e.getMessage(), e);
        }
        return false;
    }

    // Update user profile information
    public boolean updateUserProfile(User user) {
        boolean hasPasswordUpdate = user.getPassword() != null && !user.getPassword().trim().isEmpty()
                && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$");

        String query = hasPasswordUpdate
                ? "UPDATE users SET nama_lengkap = ?, email = ?, alamat = ?, password = ? WHERE id_user = ?"
                : "UPDATE users SET nama_lengkap = ?, email = ?, alamat = ? WHERE id_user = ?";

        try (Connection conn = BaseDAO.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getNamaLengkap());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getAlamat());

            if (hasPasswordUpdate) {
                stmt.setString(4, PasswordUtil.hashPassword(user.getPassword()));
                stmt.setInt(5, user.getIdUser());
            } else {
                stmt.setInt(4, user.getIdUser());
            }

            int rows = stmt.executeUpdate();
            logger.info("Updated profile for user id {}: {} rows affected", user.getIdUser(), rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating user profile: {}", e.getMessage(), e);
            return false;
        }
    }

    // Count total registered users/customers
    public int countUsers() {
        String query = "SELECT COUNT(*) FROM users WHERE role = 'USER' OR role IS NULL";
        try (Connection conn = BaseDAO.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting users: {}", e.getMessage(), e);
        }
        return 0;
    }

    public boolean addUser(User user) {
        return registerUser(user);
    }

    public User getUserByUsernameAndPassword(String username, String password) {
        return loginUser(username, password);
    }
}
