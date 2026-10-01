package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Application Entry Point
 */
public class Newfishmarket extends Application {

    private static final Logger LOGGER = Logger.getLogger(Newfishmarket.class.getName());

    @Override
    public void start(Stage stage) {
        try {
            String fxmlPath = "view/loginRegisterView.fxml";

            URL fxmlURL = getClass().getClassLoader().getResource(fxmlPath);
            if (fxmlURL == null) {
                LOGGER.log(Level.SEVERE, "File FXML tidak ditemukan di classpath: {0}", fxmlPath);
                throw new IOException("File FXML tidak ditemukan di classpath: " + fxmlPath);
            }

            LOGGER.log(Level.INFO, "Memuat file FXML dari: {0}", fxmlURL);

            FXMLLoader loader = new FXMLLoader(fxmlURL);
            Parent root = loader.load();

            Scene scene = new Scene(root, 860, 560);
            stage.setScene(scene);
            stage.setTitle("Fish Market - Seafood Hub & Management System");
            stage.setMinWidth(860);
            stage.setMinHeight(560);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Gagal memuat file FXML", e);
            showErrorAlert("Error", "Gagal memuat file FXML: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Kesalahan tidak terduga", e);
            showErrorAlert("Error", "Terjadi kesalahan tidak terduga: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
