package service;

import dao.UserDAO;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO = new UserDAO();

    public User loginUser(String username, String password) {
        try {
            return userDAO.loginUser(username, password);
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage(), e);
            throw new RuntimeException("Error during login: " + e.getMessage());
        }
    }

    public boolean registerUser(User user) {
        try {
            if (userDAO.isUsernameExists(user.getUsername())) {
                throw new IllegalArgumentException("Username already exists.");
            }
            if (userDAO.isEmailExists(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists.");
            }
            return userDAO.registerUser(user);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage(), e);
            throw new RuntimeException("Error during registration: " + e.getMessage());
        }
    }

    public boolean updateUserProfile(User user) {
        try {
            return userDAO.updateUserProfile(user);
        } catch (Exception e) {
            logger.error("Error updating user profile: {}", e.getMessage(), e);
            return false;
        }
    }
}
