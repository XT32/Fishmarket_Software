package controller;

import dao.IkanDAO;
import dao.NelayanDAO;
import dao.PenjualanDAO;
import dao.UserDAO;
import model.Ikan;
import model.Nelayan;
import model.Penjualan;
import utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

public class AdminViewController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AdminViewController.class);

    // Sidebar & Navigation
    @FXML private Label adminNameLabel;
    @FXML private Button dashboard_button;
    @FXML private Button inventory_button;
    @FXML private Button dataJual_button;
    @FXML private Button nelayan_button;
    @FXML private Button logout_button;

    // View Panels
    @FXML private VBox Dashboard_form;
    @FXML private VBox inventory_form;
    @FXML private VBox datajual_form;
    @FXML private VBox nelayan_form;

    // Dashboard Statistics
    @FXML private Label stat_customers;
    @FXML private Label stat_todayIncome;
    @FXML private Label stat_totalRevenue;
    @FXML private Label stat_totalStock;
    @FXML private TableView<Penjualan> dash_recentSalesTable;
    @FXML private TableColumn<Penjualan, Number> dash_colId;
    @FXML private TableColumn<Penjualan, Number> dash_colUser;
    @FXML private TableColumn<Penjualan, Number> dash_colIkan;
    @FXML private TableColumn<Penjualan, Number> dash_colQty;
    @FXML private TableColumn<Penjualan, String> dash_colDate;
    @FXML private TableColumn<Penjualan, Number> dash_colTotal;

    // Inventory Controls
    @FXML private TextField inventory_searchField;
    @FXML private TableView<Ikan> inventory_table;
    @FXML private TableColumn<Ikan, Number> inventory_IDikan;
    @FXML private TableColumn<Ikan, String> inventory_namaIkan;
    @FXML private TableColumn<Ikan, Number> inventory_harga;
    @FXML private TableColumn<Ikan, Number> inventory_stok;
    @FXML private TableColumn<Ikan, Number> inventory_idNelayan;

    @FXML private TextField inventory_namaField;
    @FXML private TextField inventory_hargaField;
    @FXML private TextField inventory_stokField;
    @FXML private TextField inventory_idNelayanField;
    @FXML private TextField inventory_gambarField;

    @FXML private Button inventory_annButton;
    @FXML private Button inventory_updateButton;
    @FXML private Button inventory_deleteButton;
    @FXML private Button inventory_clearButton;

    // Sales Table
    @FXML private TableView<Penjualan> tabeldataPenjualan;
    @FXML private TableColumn<Penjualan, Number> col_transaksiId;
    @FXML private TableColumn<Penjualan, Number> col_userId;
    @FXML private TableColumn<Penjualan, Number> col_ikanId;
    @FXML private TableColumn<Penjualan, Number> col_qty;
    @FXML private TableColumn<Penjualan, String> col_date;
    @FXML private TableColumn<Penjualan, String> col_address;
    @FXML private TableColumn<Penjualan, Number> col_total;

    // Nelayan Table
    @FXML private TableView<Nelayan> tabelNelayan;
    @FXML private TableColumn<Nelayan, Number> col_nelayanId;
    @FXML private TableColumn<Nelayan, String> col_nelayanNama;
    @FXML private TableColumn<Nelayan, String> col_nelayanTelepon;
    @FXML private TextField nelayan_namaField;
    @FXML private TextField nelayan_teleponField;

    private final IkanDAO ikanDAO = new IkanDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final NelayanDAO nelayanDAO = new NelayanDAO();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private ObservableList<Ikan> ikanObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupInventoryTable();
        setupDashboardSalesTable();
        setupSalesTable();
        setupNelayanTable();

        // Row selection listener for inventory table
        inventory_table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedIkan) -> {
            if (selectedIkan != null) {
                populateInventoryForm(selectedIkan);
            }
        });

        // Search filter listener
        if (inventory_searchField != null) {
            inventory_searchField.textProperty().addListener((obs, oldVal, newVal) -> filterInventory(newVal));
        }

        Platform.runLater(() -> {
            loadDashboardStats();
            loadInventoryData();
            loadPenjualanData();
            loadNelayanData();
            showDashboard();
        });
    }

    // ----------------- NAVIGATION -----------------
    @FXML
    public void showDashboard() {
        Dashboard_form.setVisible(true);
        Dashboard_form.setManaged(true);
        inventory_form.setVisible(false);
        inventory_form.setManaged(false);
        datajual_form.setVisible(false);
        datajual_form.setManaged(false);
        nelayan_form.setVisible(false);
        nelayan_form.setManaged(false);

        setActiveNav(dashboard_button);
        loadDashboardStats();
    }

    @FXML
    public void showInventory() {
        Dashboard_form.setVisible(false);
        Dashboard_form.setManaged(false);
        inventory_form.setVisible(true);
        inventory_form.setManaged(true);
        datajual_form.setVisible(false);
        datajual_form.setManaged(false);
        nelayan_form.setVisible(false);
        nelayan_form.setManaged(false);

        setActiveNav(inventory_button);
        loadInventoryData();
    }

    @FXML
    public void showDataJual() {
        Dashboard_form.setVisible(false);
        Dashboard_form.setManaged(false);
        inventory_form.setVisible(false);
        inventory_form.setManaged(false);
        datajual_form.setVisible(true);
        datajual_form.setManaged(true);
        nelayan_form.setVisible(false);
        nelayan_form.setManaged(false);

        setActiveNav(dataJual_button);
        loadPenjualanData();
    }

    @FXML
    public void showNelayan() {
        Dashboard_form.setVisible(false);
        Dashboard_form.setManaged(false);
        inventory_form.setVisible(false);
        inventory_form.setManaged(false);
        datajual_form.setVisible(false);
        datajual_form.setManaged(false);
        nelayan_form.setVisible(true);
        nelayan_form.setManaged(true);

        setActiveNav(nelayan_button);
        loadNelayanData();
    }

    private void setActiveNav(Button activeBtn) {
        dashboard_button.getStyleClass().remove("nav-btn-active");
        inventory_button.getStyleClass().remove("nav-btn-active");
        dataJual_button.getStyleClass().remove("nav-btn-active");
        nelayan_button.getStyleClass().remove("nav-btn-active");

        if (activeBtn != null && !activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }

    // ----------------- DASHBOARD -----------------
    @FXML
    public void loadDashboardStats() {
        int totalCustomers = userDAO.countUsers();
        double todayIncome = penjualanDAO.getTodayIncome();
        double totalRev = penjualanDAO.getTotalRevenue();
        int totalStock = ikanDAO.getTotalStock();

        stat_customers.setText(String.valueOf(totalCustomers));
        stat_todayIncome.setText(currencyFormatter.format(todayIncome).replace(",00", ""));
        stat_totalRevenue.setText(currencyFormatter.format(totalRev).replace(",00", ""));
        stat_totalStock.setText(totalStock + " kg");

        List<Penjualan> allSales = penjualanDAO.getAllPenjualan();
        dash_recentSalesTable.setItems(FXCollections.observableArrayList(allSales));
    }

    private void setupDashboardSalesTable() {
        dash_colId.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        dash_colUser.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        dash_colIkan.setCellValueFactory(new PropertyValueFactory<>("idIkan"));
        dash_colQty.setCellValueFactory(new PropertyValueFactory<>("kuantitas"));
        dash_colDate.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        dash_colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        dash_colTotal.setCellFactory(col -> new TableCell<>() {
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

    // ----------------- INVENTORY -----------------
    private void setupInventoryTable() {
        inventory_IDikan.setCellValueFactory(new PropertyValueFactory<>("idIkan"));
        inventory_namaIkan.setCellValueFactory(new PropertyValueFactory<>("namaIkan"));
        inventory_harga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        inventory_stok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        inventory_idNelayan.setCellValueFactory(new PropertyValueFactory<>("idNelayan"));

        inventory_harga.setCellFactory(col -> new TableCell<>() {
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

        inventory_stok.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.intValue() + " kg");
                }
            }
        });
    }

    private void loadInventoryData() {
        List<Ikan> list = ikanDAO.getAllIkan();
        ikanObservableList = FXCollections.observableArrayList(list);
        inventory_table.setItems(ikanObservableList);
    }

    private void filterInventory(String query) {
        if (query == null || query.trim().isEmpty()) {
            inventory_table.setItems(ikanObservableList);
            return;
        }
        String lower = query.trim().toLowerCase();
        ObservableList<Ikan> filtered = ikanObservableList.filtered(i ->
                i.getNamaIkan().toLowerCase().contains(lower) ||
                String.valueOf(i.getIdIkan()).contains(lower)
        );
        inventory_table.setItems(filtered);
    }

    private void populateInventoryForm(Ikan ikan) {
        inventory_namaField.setText(ikan.getNamaIkan());
        inventory_hargaField.setText(String.format(Locale.US, "%.0f", ikan.getHarga()));
        inventory_stokField.setText(String.valueOf(ikan.getStok()));
        inventory_idNelayanField.setText(String.valueOf(ikan.getIdNelayan()));
        inventory_gambarField.setText(ikan.getGambarIkan() != null ? ikan.getGambarIkan() : "");
    }

    @FXML
    public void addIkan() {
        try {
            String nama = inventory_namaField.getText() != null ? inventory_namaField.getText().trim() : "";
            String hargaStr = inventory_hargaField.getText() != null ? inventory_hargaField.getText().trim() : "";
            String stokStr = inventory_stokField.getText() != null ? inventory_stokField.getText().trim() : "";
            String idNelayanStr = inventory_idNelayanField.getText() != null ? inventory_idNelayanField.getText().trim() : "1";
            String gambar = inventory_gambarField.getText() != null ? inventory_gambarField.getText().trim() : "ikan.png";

            if (nama.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama ikan, harga, dan stok wajib diisi!");
                return;
            }

            double harga = Double.parseDouble(hargaStr);
            int stok = Integer.parseInt(stokStr);
            int idNelayan = idNelayanStr.isEmpty() ? 1 : Integer.parseInt(idNelayanStr);

            Ikan newIkan = new Ikan(0, nama, harga, gambar, stok, idNelayan);
            boolean success = ikanDAO.addIkan(newIkan);

            if (success) {
                loadInventoryData();
                clearInventoryFields();
                loadDashboardStats();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Ikan baru '" + nama + "' berhasil ditambahkan.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menambahkan data ikan ke database.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Error", "Harga, stok, dan ID Nelayan harus berupa angka valid.");
        }
    }

    @FXML
    public void updateIkan() {
        Ikan selected = inventory_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih ikan yang ingin diperbarui dari tabel.");
            return;
        }

        try {
            String nama = inventory_namaField.getText() != null ? inventory_namaField.getText().trim() : "";
            String hargaStr = inventory_hargaField.getText() != null ? inventory_hargaField.getText().trim() : "";
            String stokStr = inventory_stokField.getText() != null ? inventory_stokField.getText().trim() : "";
            String idNelayanStr = inventory_idNelayanField.getText() != null ? inventory_idNelayanField.getText().trim() : "1";
            String gambar = inventory_gambarField.getText() != null ? inventory_gambarField.getText().trim() : "";

            if (nama.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom utama wajib diisi.");
                return;
            }

            selected.setNamaIkan(nama);
            selected.setHarga(Double.parseDouble(hargaStr));
            selected.setStok(Integer.parseInt(stokStr));
            selected.setIdNelayan(idNelayanStr.isEmpty() ? 1 : Integer.parseInt(idNelayanStr));
            selected.setGambarIkan(gambar);

            boolean success = ikanDAO.updateIkan(selected);
            if (success) {
                loadInventoryData();
                clearInventoryFields();
                loadDashboardStats();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data ikan '" + nama + "' berhasil diperbarui.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui data ikan di database.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Error", "Harga, stok, dan ID Nelayan harus berupa angka valid.");
        }
    }

    @FXML
    public void deleteIkan() {
        Ikan selected = inventory_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih ikan yang ingin dihapus dari tabel.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Apakah Anda yakin ingin menghapus ikan '" + selected.getNamaIkan() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                boolean success = ikanDAO.deleteIkan(selected.getIdIkan());
                if (success) {
                    loadInventoryData();
                    clearInventoryFields();
                    loadDashboardStats();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data ikan berhasil dihapus.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus data ikan.");
                }
            }
        });
    }

    @FXML
    public void clearInventoryFields() {
        inventory_table.getSelectionModel().clearSelection();
        inventory_namaField.clear();
        inventory_hargaField.clear();
        inventory_stokField.clear();
        inventory_idNelayanField.clear();
        inventory_gambarField.clear();
    }

    // ----------------- DATA PENJUALAN -----------------
    private void setupSalesTable() {
        col_transaksiId.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        col_userId.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        col_ikanId.setCellValueFactory(new PropertyValueFactory<>("idIkan"));
        col_qty.setCellValueFactory(new PropertyValueFactory<>("kuantitas"));
        col_date.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        col_address.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        col_total.setCellValueFactory(new PropertyValueFactory<>("total"));

        col_total.setCellFactory(col -> new TableCell<>() {
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
    public void loadPenjualanData() {
        List<Penjualan> list = penjualanDAO.getAllPenjualan();
        tabeldataPenjualan.setItems(FXCollections.observableArrayList(list));
    }

    // ----------------- MITRA NELAYAN -----------------
    private void setupNelayanTable() {
        col_nelayanId.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_nelayanNama.setCellValueFactory(new PropertyValueFactory<>("namaNelayan"));
        col_nelayanTelepon.setCellValueFactory(new PropertyValueFactory<>("nomorTelepon"));
    }

    @FXML
    public void loadNelayanData() {
        List<Nelayan> list = nelayanDAO.getAllNelayan();
        tabelNelayan.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void addNelayan() {
        String nama = nelayan_namaField.getText() != null ? nelayan_namaField.getText().trim() : "";
        String telepon = nelayan_teleponField.getText() != null ? nelayan_teleponField.getText().trim() : "";

        if (nama.isEmpty() || telepon.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama dan nomor telepon nelayan wajib diisi.");
            return;
        }

        Nelayan nelayan = new Nelayan(0, nama, telepon);
        boolean success = nelayanDAO.addNelayan(nelayan);
        if (success) {
            nelayan_namaField.clear();
            nelayan_teleponField.clear();
            loadNelayanData();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Nelayan mitra berhasil ditambahkan.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menambahkan data nelayan.");
        }
    }

    // ----------------- LOGOUT -----------------
    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loginRegisterView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logout_button.getScene().getWindow();
            stage.setTitle("Fish Market - Login");
            stage.setMinWidth(840);
            stage.setMinHeight(540);
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
