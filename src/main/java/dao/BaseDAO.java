package dao;

import utils.EnvConfig;
import utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;

/**
 * Enterprise BaseDAO providing multi-database connectivity (MySQL, PostgreSQL, SQLite),
 * environment configuration loading (.env), and automatic dialect-specific schema initialization.
 */
public class BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);

    private static String dbType = "mysql";
    private static String jdbcUrl;
    private static String username;
    private static String password;
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        // Fallback properties from classpath
        Properties props = new Properties();
        try (InputStream in = BaseDAO.class.getResourceAsStream("/database.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            logger.debug("Could not load database.properties: {}", e.getMessage());
        }

        // Determine DB Type
        dbType = EnvConfig.get("DB_TYPE", props.getProperty("db.type", "mysql")).toLowerCase();

        // Check for full DB_URL
        String customUrl = EnvConfig.get("DB_URL", props.getProperty("db.url", null));

        if (customUrl != null && !customUrl.trim().isEmpty()) {
            jdbcUrl = customUrl.trim();
            if (jdbcUrl.startsWith("jdbc:postgresql:") || jdbcUrl.startsWith("jdbc:pgsql:")) {
                dbType = "postgresql";
            } else if (jdbcUrl.startsWith("jdbc:sqlite:")) {
                dbType = "sqlite";
            } else if (jdbcUrl.startsWith("jdbc:mysql:")) {
                dbType = "mysql";
            }
        } else {
            // Construct URL based on DB_TYPE
            String host = EnvConfig.get("DB_HOST", "localhost");
            String port = EnvConfig.get("DB_PORT", dbType.contains("postgre") ? "5432" : "3306");
            String name = EnvConfig.get("DB_NAME", "fishmarket");

            if (dbType.contains("postgre") || dbType.equals("pgsql")) {
                dbType = "postgresql";
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + name;
            } else if (dbType.equals("sqlite")) {
                jdbcUrl = "jdbc:sqlite:" + name + ".db";
            } else {
                dbType = "mysql";
                jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + name +
                        "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            }
        }

        username = EnvConfig.get("DB_USER", props.getProperty("db.user", "fishmarket_user"));
        password = EnvConfig.get("DB_PASSWORD", props.getProperty("db.password", "fishmarket_pass"));

        // Register driver
        try {
            switch (dbType) {
                case "postgresql":
                    Class.forName("org.postgresql.Driver");
                    break;
                case "sqlite":
                    Class.forName("org.sqlite.JDBC");
                    break;
                case "mysql":
                default:
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    break;
            }
        } catch (ClassNotFoundException e) {
            logger.error("JDBC Driver class not found for {}: {}", dbType, e.getMessage());
        }

        logger.info("Database configured: Type=[{}], URL=[{}], User=[{}]", dbType, jdbcUrl, username);
    }

    /**
     * Obtains an active connection to the configured database.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn;
            if ("sqlite".equals(dbType)) {
                conn = DriverManager.getConnection(jdbcUrl);
            } else {
                conn = DriverManager.getConnection(jdbcUrl, username, password);
            }

            if (!initialized) {
                initializeDatabase(conn);
            }
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to connect to database [{}] (Type: {}): {}", jdbcUrl, dbType, e.getMessage());
            throw e;
        }
    }

    private static synchronized void initializeDatabase(Connection conn) {
        if (initialized) return;

        try {
            DatabaseMetaData meta = conn.getMetaData();
            boolean tablesExist = false;

            // Check if users table exists (handle case sensitivity for PostgreSQL/MySQL)
            try (ResultSet rs = meta.getTables(null, null, "users", null)) {
                if (rs.next()) tablesExist = true;
            }
            if (!tablesExist) {
                try (ResultSet rs = meta.getTables(null, null, "USERS", null)) {
                    if (rs.next()) tablesExist = true;
                }
            }

            if (!tablesExist) {
                logger.info("Tables not detected. Running schema auto-initialization for {}...", dbType);
                executeSchemaScript(conn);
            }

            ensureAdminUser(conn);
            initialized = true;
            logger.info("Database initialized successfully for dialect: {}", dbType);
        } catch (Exception e) {
            logger.error("Error during database initialization: {}", e.getMessage(), e);
        }
    }

    private static void executeSchemaScript(Connection conn) {
        String scriptPath = "/database/schema.sql";
        if ("postgresql".equals(dbType)) {
            scriptPath = "/database/schema-postgres.sql";
        } else if ("sqlite".equals(dbType)) {
            scriptPath = "/database/schema-sqlite.sql";
        }

        try (InputStream in = BaseDAO.class.getResourceAsStream(scriptPath)) {
            if (in == null) {
                logger.warn("Schema script not found: {}", scriptPath);
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
                        logger.debug("Script execution note: {}", e.getMessage());
                    }
                    sb.setLength(0);
                }
            }
            logger.info("Successfully executed dialect script: {}", scriptPath);
        } catch (Exception e) {
            logger.error("Failed to execute schema script [{}]: {}", scriptPath, e.getMessage());
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
            logger.warn("Could not verify/create default admin: {}", e.getMessage());
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDbType() {
        return dbType;
    }
}
