package controller;

import model.User;
import service.UserService;
import utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginRegisterViewController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginRegisterViewController.class);

    private final UserService userService = new UserService();

    @FXML private VBox si_loginForm;
    @FXML private TextField si_username;
    @FXML private PasswordField si_password;
    @FXML private Button si_loginButton;
    @FXML private Button tabLogin;
    @FXML private Button tabRegister;

    @FXML private VBox su_signupForm;
    @FXML private TextField su_namaLengkap;
    @FXML private TextField su_username;
    @FXML private TextField su_email;
    @FXML private TextField su_alamat;
    @FXML private PasswordField su_password;
    @FXML private PasswordField su_confirmPass;
    @FXML private Button su_registerButton;

    @FXML private Button side_switchButton;
    @FXML private Label brandBottomNote;

    private boolean isLoginMode = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showLoginForm();
    }

    @FXML
    public void showLoginForm() {
        isLoginMode = true;
        si_loginForm.setVisible(true);
        si_loginForm.setManaged(true);
        su_signupForm.setVisible(false);
        su_signupForm.setManaged(false);

        if (side_switchButton != null) {
            side_switchButton.setText("Buat Akun Baru");
        }
        if (brandBottomNote != null) {
            brandBottomNote.setText("Belum punya akun? Daftar gratis sekarang:");
        }
    }

    @FXML
    public void showSignupForm() {
        isLoginMode = false;
        si_loginForm.setVisible(false);
        si_loginForm.setManaged(false);
        su_signupForm.setVisible(true);
        su_signupForm.setManaged(true);

        if (side_switchButton != null) {
            side_switchButton.setText("Beralih ke Form Masuk");
        }
        if (brandBottomNote != null) {
            brandBottomNote.setText("Sudah punya akun? Masuk langsung di sini:");
        }
    }

    @FXML
    public void switchForm(ActionEvent event) {
        if (isLoginMode) {
            showSignupForm();
        } else {
            showLoginForm();
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = si_username.getText() != null ? si_username.getText().trim() : "";
        String password = si_password.getText() != null ? si_password.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Username dan password tidak boleh kosong.");
            return;
        }

        try {
            User user = userService.loginUser(username, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                logger.info("Login successful for user: {} (Role: {})", user.getUsername(), user.getRole());

                if (user.isAdmin()) {
                    loadAdminView();
                } else {
                    loadUserView(user);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau password salah.");
            }
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Kesalahan Sistem", "Gagal melakukan autentikasi: " + e.getMessage());
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String namaLengkap = su_namaLengkap.getText() != null ? su_namaLengkap.getText().trim() : "";
        String username = su_username.getText() != null ? su_username.getText().trim() : "";
        String email = su_email.getText() != null ? su_email.getText().trim() : "";
        String alamat = su_alamat.getText() != null ? su_alamat.getText().trim() : "";
        String password = su_password.getText() != null ? su_password.getText() : "";
        String confirmPass = su_confirmPass.getText() != null ? su_confirmPass.getText() : "";

        if (namaLengkap.isEmpty() || username.isEmpty() || email.isEmpty() || alamat.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Pendaftaran Gagal", "Semua kolom wajib diisi.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Pendaftaran Gagal", "Format email tidak valid.");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Pendaftaran Gagal", "Password minimal harus 6 karakter.");
            return;
        }

        if (!password.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "Pendaftaran Gagal", "Konfirmasi password tidak cocok.");
            return;
        }

        try {
            User newUser = new User(0, username, email, password, alamat, namaLengkap, "USER");
            boolean success = userService.registerUser(newUser);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Pendaftaran Berhasil",
                        "Akun berhasil dibuat! Silakan masuk dengan akun baru Anda.");
                si_username.setText(username);
                si_password.clear();
                showLoginForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Pendaftaran Gagal", "Terjadi kesalahan saat mendaftarkan akun.");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Pendaftaran Gagal", e.getMessage());
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Kesalahan Sistem", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private void loadUserView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/userView.fxml"));
            Parent root = loader.load();

            UserViewController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) si_loginButton.getScene().getWindow();
            stage.setTitle("Fish Market - Seafood Marketplace");
            stage.setScene(new Scene(root, 1100, 650));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load user view: {}", e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat tampilan pembeli: " + e.getMessage());
        }
    }

    private void loadAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/adminView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) si_loginButton.getScene().getWindow();
            stage.setTitle("Fish Market - Panel Admin & Inventaris");
            stage.setScene(new Scene(root, 1150, 680));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load admin view: {}", e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat tampilan admin: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
