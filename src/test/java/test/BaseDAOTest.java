package test;

import dao.BaseDAO;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for Database Connection.
 */
public class BaseDAOTest {

    @Test
    public void testDatabaseConnection() {
        assertDoesNotThrow(() -> {
            try (Connection conn = BaseDAO.getConnection()) {
                assertNotNull(conn, "Database connection should not be null");
                assertFalse(conn.isClosed(), "Database connection should be open");
            }
        });
    }

    public static void main(String[] args) {
        if (BaseDAO.testConnection()) {
            System.out.println("Database connection test successful!");
        } else {
            System.err.println("Database connection test failed!");
        }
    }
}
