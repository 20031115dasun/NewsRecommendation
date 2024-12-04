package com.example.oodcw;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AdminLogin {

    @FXML
    private TextField adminUsername;

    @FXML
    private PasswordField adminPassword;

    @FXML
    private Button loginButton;

    @FXML
    private void handleAdminLogin() {
        String username = adminUsername.getText();
        String password = adminPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (validateAdminCredentials(username, password)) {
            navigateToAdminDashboard();
        } else {
            showAlert("Error", "Invalid credentials. Please try again.");
        }
    }

    private boolean validateAdminCredentials(String username, String password) {
        // Hard-coded admin credentials for simplicity
        return "admin".equals(username) && "password".equals(password);
    }

    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            AnchorPane dashboard = loader.load();

            Scene scene = new Scene(dashboard);
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load Admin Dashboard.");
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
