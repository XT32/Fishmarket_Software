package utils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * PascalCase alias for BaseDAO.
 */
public class BaseDAO {
    public static Connection getConnection() throws SQLException {
        return dao.BaseDAO.getConnection();
    }
}
