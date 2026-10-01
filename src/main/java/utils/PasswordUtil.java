package utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords using BCrypt.
 */
public class PasswordUtil {

    /**
     * Hashes a plaintext password using BCrypt.
     *
     * @param plainPassword The plain text password
     * @return Hashed password string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Verifies a plain text password against a stored hash (or legacy plaintext).
     *
     * @param plainPassword The entered password
     * @param storedHash    The hashed or stored password from database
     * @return true if matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }

        // Support BCrypt hash format
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(plainPassword, storedHash);
            } catch (Exception e) {
                return false;
            }
        }

        // Fallback for legacy unhashed passwords
        return plainPassword.equals(storedHash);
    }
}
