package test;

import model.User;
import org.junit.jupiter.api.Test;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class LoginRegisterTest {

    @Test
    public void testUserValidation() {
        UserService service = new UserService();

        // Testing duplicate registration rejection for admin
        User testUser = new User(0, "admin", "admin@fishmarket.com", "admin123", "Pusat", "Admin");
        assertThrows(IllegalArgumentException.class, () -> {
            service.registerUser(testUser);
        });
    }

    @Test
    public void testAdminLoginSuccess() {
        UserService service = new UserService();
        User admin = service.loginUser("admin", "admin123");
        assertNotNull(admin, "Admin should be able to log in with admin/admin123");
        assertTrue(admin.isAdmin(), "User should have ADMIN role");
    }
}
