package controller;

import dao.IkanDAO;
import dao.PenjualanDAO;
import model.CartItem;
import model.Ikan;
import model.Keranjang;
import model.Penjualan;
import model.User;
import service.UserService;
import utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class UserViewController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(UserViewController.class);

    @FXML private Label namalengkap;
    @FXML private Label usernameLabel;
    @FXML private Button user_shopButton;
    @FXML private Button user_historyButton;
    @FXML private Button user_profileButton;
    @FXML private Button logOut_Bt;

    // Marketplace / Shop Tab
    @FXML private HBox user_marketPlace;
    @FXML private TextField searchFishField;
    @FXML private ScrollPane menu_scrollPane;
    @FXML private GridPane ikan_gridPane;

    // Cart
    @FXML private Label cartItemCountLabel;
    @FXML private TableView<CartItem> shop_listBeli;
    @FXML private TableColumn<CartItem, String> shop_namaIkan;
    @FXML private TableColumn<CartItem, Number> shop_kuantitas;
    @FXML private TableColumn<CartItem, Number> shop_harga;
    @FXML private TextField cart_alamatField;
    @FXML private Label User_totalBayar;
    @FXML private Button shop_bayar;

    // History Tab
    @FXML private VBox user_history;
    @FXML private TableView<Penjualan> history_table;
    @FXML private TableColumn<Penjualan, Number> hist_idTransaksi;
    @FXML private TableColumn<Penjualan, Number> hist_idIkan;
    @FXML private TableColumn<Penjualan, Number> hist_kuantitas;
    @FXML private TableColumn<Penjualan, String> hist_tanggal;
    @FXML private TableColumn<Penjualan, String> hist_alamat;
    @FXML private TableColumn<Penjualan, Number> hist_total;

    // Profile Tab
    @FXML private VBox user_profile;
    @FXML private Label profileHeaderName;
    @FXML private TextField profile_namaLengkap;
    @FXML private TextField profile_username;
    @FXML private TextField email_profile;
    @FXML private TextField alamat_profile;
    @FXML private PasswordField profile_newPassword;
    @FXML private Button editProfile_bt;

    private User currentUser;
    private final IkanDAO ikanDAO = new IkanDAO();
    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final UserService userService = new UserService();
    private final Keranjang keranjang = new Keranjang();
    private final ObservableList<CartItem> cartItemsObservable = FXCollections.observableArrayList();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCartTable();
        setupHistoryTable();

        if (searchFishField != null) {
            searchFishField.textProperty().addListener((obs, oldVal, newVal) -> filterCatalog(newVal));
        }

        // Delay loading until user session is ready
        Platform.runLater(() -> {
            if (currentUser == null) {
                currentUser = SessionManager.getCurrentUser();
            }
            if (currentUser != null) {
                setUser(currentUser);
            }
            refreshCatalog();
            showMarketPlace();
        });
    }

    public void setUser(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        updateProfileUI();
    }

    private void updateProfileUI() {
        if (currentUser == null) return;

        namalengkap.setText(currentUser.getNamaLengkap());
        usernameLabel.setText("@" + currentUser.getUsername());

        if (profileHeaderName != null) profileHeaderName.setText(currentUser.getNamaLengkap());
        if (profile_namaLengkap != null) profile_namaLengkap.setText(currentUser.getNamaLengkap());
        if (profile_username != null) profile_username.setText(currentUser.getUsername());
        if (email_profile != null) email_profile.setText(currentUser.getEmail());
        if (alamat_profile != null) alamat_profile.setText(currentUser.getAlamat());
        if (cart_alamatField != null && (cart_alamatField.getText() == null || cart_alamatField.getText().isEmpty())) {
            cart_alamatField.setText(currentUser.getAlamat());
        }
    }

    // ----------------- NAVIGATION -----------------
    @FXML
    public void showMarketPlace() {
        user_marketPlace.setVisible(true);
        user_marketPlace.setManaged(true);
        user_history.setVisible(false);
        user_history.setManaged(false);
        user_profile.setVisible(false);
        user_profile.setManaged(false);

        setActiveNav(user_shopButton);
    }

    @FXML
    public void showHistory() {
        user_marketPlace.setVisible(false);
        user_marketPlace.setManaged(false);
        user_history.setVisible(true);
        user_history.setManaged(true);
        user_profile.setVisible(false);
        user_profile.setManaged(false);

        setActiveNav(user_historyButton);
        loadUserHistory();
    }

    @FXML
    public void showProfile() {
        user_marketPlace.setVisible(false);
        user_marketPlace.setManaged(false);
        user_history.setVisible(false);
        user_history.setManaged(false);
        user_profile.setVisible(true);
        user_profile.setManaged(true);

        setActiveNav(user_profileButton);
        updateProfileUI();
    }

    private void setActiveNav(Button activeBtn) {
        user_shopButton.getStyleClass().remove("nav-btn-active");
        user_historyButton.getStyleClass().remove("nav-btn-active");
        user_profileButton.getStyleClass().remove("nav-btn-active");

        if (activeBtn != null && !activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }

    // ----------------- CATALOG / MARKETPLACE -----------------
    @FXML
    public void refreshCatalog() {
        List<Ikan> ikanList = ikanDAO.getAllIkan();
        populateGrid(ikanList);
    }

    private void filterCatalog(String query) {
        List<Ikan> all = ikanDAO.getAllIkan();
        if (query == null || query.trim().isEmpty()) {
            populateGrid(all);
            return;
        }

        String lower = query.trim().toLowerCase();
        List<Ikan> filtered = all.stream()
                .filter(i -> i.getNamaIkan().toLowerCase().contains(lower))
                .toList();
        populateGrid(filtered);
    }

    private void populateGrid(List<Ikan> items) {
        ikan_gridPane.getChildren().clear();
        int column = 0;
        int row = 0;

        try {
            for (Ikan ikan : items) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fishCard.fxml"));
                Pane card = loader.load();

                FishCardController cardController = loader.getController();
                cardController.setData(ikan, this::handleAddToCart);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                ikan_gridPane.add(card, column++, row);
            }
        } catch (IOException e) {
            logger.error("Failed to load fish cards: {}", e.getMessage(), e);
        }
    }

    private void handleAddToCart(Ikan ikan, int quantity) {
        keranjang.addItem(ikan, quantity);
        cartItemsObservable.setAll(keranjang.getItems());
        updateCartSummary();
    }

    private void updateCartSummary() {
        double total = keranjang.getTotalPrice();
        User_totalBayar.setText(currencyFormatter.format(total).replace(",00", ""));
        cartItemCountLabel.setText(keranjang.getTotalQuantity() + " Item");
    }

    @FXML
    public void handleClearCart() {
        keranjang.clear();
        cartItemsObservable.clear();
        updateCartSummary();
    }

    @FXML
    public void handleCheckout() {
        if (keranjang.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Keranjang Kosong", "Silakan tambahkan ikan terlebih dahulu ke keranjang.");
            return;
        }

        String alamat = cart_alamatField.getText() != null ? cart_alamatField.getText().trim() : "";
        if (alamat.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Alamat Kosong", "Mohon isi alamat pengiriman pesanan Anda.");
            return;
        }

        int userId = currentUser != null ? currentUser.getIdUser() : 1;
        boolean success = penjualanDAO.processCheckout(userId, alamat, keranjang.getItems());

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Pembayaran Berhasil",
                    "Pesanan Anda telah diproses dengan total " + User_totalBayar.getText() + "!\nIkan segar akan segera dikirim ke: " + alamat);
            handleClearCart();
            refreshCatalog(); // Refresh stock in catalog
        } else {
            showAlert(Alert.AlertType.ERROR, "Pembayaran Gagal", "Terjadi kesalahan saat memproses pesanan.");
        }
    }

    private void setupCartTable() {
        shop_namaIkan.setCellValueFactory(new PropertyValueFactory<>("namaIkan"));
        shop_kuantitas.setCellValueFactory(new PropertyValueFactory<>("kuantitas"));
        shop_harga.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Format subtotal column in Rupiah
        shop_harga.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(item.doubleValue()).replace(",00", ""));
                }
            }
        });

        shop_listBeli.setItems(cartItemsObservable);
    }

    // ----------------- HISTORY -----------------
    private void setupHistoryTable() {
        hist_idTransaksi.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        hist_idIkan.setCellValueFactory(new PropertyValueFactory<>("idIkan"));
        hist_kuantitas.setCellValueFactory(new PropertyValueFactory<>("kuantitas"));
        hist_tanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        hist_alamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        hist_total.setCellValueFactory(new PropertyValueFactory<>("total"));

        hist_total.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(item.doubleValue()).replace(",00", ""));
                }
            }
        });
    }

    @FXML
    public void loadUserHistory() {
        if (currentUser != null) {
            List<Penjualan> historyList = penjualanDAO.getPenjualanByUserId(currentUser.getIdUser());
            history_table.setItems(FXCollections.observableArrayList(historyList));
        }
    }

    // ----------------- PROFILE -----------------
    @FXML
    public void handleEditProfile() {
        if (currentUser == null) return;

        String namaLengkap = profile_namaLengkap.getText() != null ? profile_namaLengkap.getText().trim() : "";
        String email = email_profile.getText() != null ? email_profile.getText().trim() : "";
        String alamat = alamat_profile.getText() != null ? alamat_profile.getText().trim() : "";
        String newPass = profile_newPassword.getText() != null ? profile_newPassword.getText() : "";

        if (namaLengkap.isEmpty() || email.isEmpty() || alamat.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama lengkap, email, dan alamat tidak boleh kosong.");
            return;
        }

        currentUser.setNamaLengkap(namaLengkap);
        currentUser.setEmail(email);
        currentUser.setAlamat(alamat);
        if (!newPass.isEmpty()) {
            if (newPass.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Password baru minimal harus 6 karakter.");
                return;
            }
            currentUser.setPassword(newPass);
        }

        boolean success = userService.updateUserProfile(currentUser);
        if (success) {
            updateProfileUI();
            profile_newPassword.clear();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Profil Anda berhasil diperbarui!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui profil di database.");
        }
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loginRegisterView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logOut_Bt.getScene().getWindow();
            stage.setTitle("Fish Market - Login");
            stage.setScene(new Scene(root, 840, 540));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load login screen: {}", e.getMessage(), e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
