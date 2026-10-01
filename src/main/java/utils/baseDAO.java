package utils;

import dao.BaseDAO;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Compatibility delegate for dao.BaseDAO.
 */
public class baseDAO {

    public static Connection getConnection() throws SQLException {
        return BaseDAO.getConnection();
    }

    public static void main(String[] args) {
        if (BaseDAO.testConnection()) {
            System.out.println("Database connection successful!");
        } else {
            System.err.println("Database connection failed!");
        }
    }
}
