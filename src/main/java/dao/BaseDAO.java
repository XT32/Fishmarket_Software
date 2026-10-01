package dao;

import utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;

/**
 * Centralized Database Access Object base providing database connections,
 * configuration loading, and auto-initialization of schema and admin account.
 */
public class BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);

    private static String jdbcUrl;
    private static String username;
    private static String password;
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    private static synchronized void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = BaseDAO.class.getResourceAsStream("/database.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            logger.warn("Could not load database.properties from classpath: {}", e.getMessage());
        }

        // Environment variables take precedence over config file
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        jdbcUrl = envUrl != null ? envUrl : props.getProperty("db.url",
                "jdbc:mysql://localhost:3306/fishmarket?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        username = envUser != null ? envUser : props.getProperty("db.user", "fishmarket_user");
        password = envPass != null ? envPass : props.getProperty("db.password", "fishmarket_pass");

        logger.info("Database configured for URL: {} and user: {}", jdbcUrl, username);
    }

    /**
     * Obtains a new database connection.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            if (!initialized) {
                initializeDatabase(conn);
            }
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to connect to database [{}]: {}", jdbcUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * Initializes database tables and default admin if needed.
     */
    private static synchronized void initializeDatabase(Connection conn) {
        if (initialized) return;

        try {
            // Check if users table exists
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "users", null)) {
                if (!rs.next()) {
                    executeSchemaScript(conn);
                }
            }

            // Ensure admin account exists
            ensureAdminUser(conn);

            initialized = true;
            logger.info("Database initialized successfully.");
        } catch (Exception e) {
            logger.error("Error during database initialization: {}", e.getMessage(), e);
        }
    }

    private static void executeSchemaScript(Connection conn) {
        try (InputStream in = BaseDAO.class.getResourceAsStream("/database/schema.sql")) {
            if (in == null) {
                logger.warn("schema.sql not found in resources.");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue;
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sb.toString());
                    } catch (SQLException e) {
                        logger.debug("Statement execution notice: {}", e.getMessage());
                    }
                    sb.setLength(0);
                }
            }
            logger.info("schema.sql executed successfully.");
        } catch (Exception e) {
            logger.error("Failed to execute schema.sql: {}", e.getMessage());
        }
    }

    private static void ensureAdminUser(Connection conn) {
        String checkQuery = "SELECT 1 FROM users WHERE username = 'admin'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            if (!rs.next()) {
                String insertAdmin = "INSERT INTO users (nama_lengkap, username, email, password, alamat, role) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertAdmin)) {
                    ps.setString(1, "Administrator");
                    ps.setString(2, "admin");
                    ps.setString(3, "admin@fishmarket.com");
                    ps.setString(4, PasswordUtil.hashPassword("admin123"));
                    ps.setString(5, "Kantor Pusat Fishmarket");
                    ps.setString(6, "ADMIN");
                    ps.executeUpdate();
                    logger.info("Default admin user created: admin / admin123");
                }
            }
        } catch (SQLException e) {
            logger.warn("Could not check/create default admin: {}", e.getMessage());
        }
    }

    /**
     * Tests database connectivity.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}
