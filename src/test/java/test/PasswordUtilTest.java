package test;

import org.junit.jupiter.api.Test;
import utils.PasswordUtil;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testHashAndCheckPassword() {
        String rawPassword = "securePassword123";
        String hashed = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hashed);
        assertNotEquals(rawPassword, hashed);
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$"));

        // Verify correct password
        assertTrue(PasswordUtil.checkPassword(rawPassword, hashed));

        // Verify wrong password
        assertFalse(PasswordUtil.checkPassword("wrongPassword", hashed));
    }

    @Test
    public void testLegacyPlaintextCheck() {
        // Legacy plaintext fallback
        assertTrue(PasswordUtil.checkPassword("admin123", "admin123"));
        assertFalse(PasswordUtil.checkPassword("admin123", "wrong"));
    }
}
