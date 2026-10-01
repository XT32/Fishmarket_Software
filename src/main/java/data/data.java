package data;

import utils.SessionManager;

/**
 * Global session data holder kept for backward compatibility.
 */
public class data {
    public static String username;
    public static String path;

    public static void setUsername(String user) {
        username = user;
    }

    public static String getUsername() {
        if (username != null) return username;
        if (SessionManager.getCurrentUser() != null) {
            return SessionManager.getCurrentUser().getUsername();
        }
        return null;
    }
}
