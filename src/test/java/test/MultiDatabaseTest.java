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

public class MultiDatabaseTest {

    @Test
    public void testSQLiteDialectInitialization() {
        assertDoesNotThrow(() -> {
            // In-memory or temp SQLite connection
            String sqliteUrl = "jdbc:sqlite::memory:";
            try (Connection conn = DriverManager.getConnection(sqliteUrl)) {
                assertNotNull(conn);

                // Run schema-sqlite.sql
                try (InputStream in = getClass().getResourceAsStream("/database/schema-sqlite.sql")) {
                    assertNotNull(in, "schema-sqlite.sql should exist on classpath");
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

                // Verify tables exist and seeded data is present
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ikan")) {
                    assertTrue(rs.next());
                    int count = rs.getInt(1);
                    assertTrue(count >= 8, "Expected at least 8 fish records seeded in SQLite");
                }

                // Verify nelayan seeded
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM nelayan")) {
                    assertTrue(rs.next());
                    int count = rs.getInt(1);
                    assertEquals(3, count, "Expected 3 fishermen seeded in SQLite");
                }
            }
        });
    }
}
