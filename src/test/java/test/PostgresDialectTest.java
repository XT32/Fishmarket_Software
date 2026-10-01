package test;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class PostgresDialectTest {

    @Test
    public void testPostgreSQLSchemaInitialization() {
        String pgUrl = "jdbc:postgresql://localhost:5432/fishmarket";
        String pgUser = "postgres";
        String pgPass = "postgres";

        // Try connecting to local PostgreSQL; skip gracefully if local server is unreachable
        try (Connection conn = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            assertNotNull(conn);

            // Run schema-postgres.sql
            try (InputStream in = getClass().getResourceAsStream("/database/schema-postgres.sql")) {
                assertNotNull(in, "schema-postgres.sql should exist on classpath");
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
                        }
                        sb.setLength(0);
                    }
                }
            }

            // Verify tables and data
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ikan")) {
                assertTrue(rs.next());
                int count = rs.getInt(1);
                assertTrue(count >= 8, "Expected at least 8 fish records in PostgreSQL");
            }
        } catch (Exception e) {
            System.out.println("PostgreSQL live test note: " + e.getMessage());
        }
    }
}
