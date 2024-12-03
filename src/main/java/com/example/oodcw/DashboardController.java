package com.example.oodcw;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Button viewArticlesButton;

    @FXML
    private Button viewRecommendationsButton;

    @FXML
    private Button manageProfileButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button homeButton;

    // Handler for View Articles button
    @FXML
    private void handleViewArticles() {
        navigateTo("Articles.fxml", viewArticlesButton, 745, 624); // Articles scene size
    }

    // Handler for View Recommendations button
    @FXML
    private void handleViewRecommendations() {
        // Navigate to Recommendation.fxml and let the Recommendation class handle recommendations
        navigateTo("Recommendation.fxml", viewRecommendationsButton, 659, 650); // Recommendations scene size
    }

    // Handler for Manage Profile button
    @FXML
    private void handleManageProfile() {
        navigateTo("Profile.fxml", manageProfileButton, 304, 406); // Profile scene size
    }

    // Handler for Logout button
    @FXML
    private void handleLogout() {
        // Reset user preference and login information
        Login.currentUser = null;
        Login.userPreference = null;
        navigateTo("Login.fxml", logoutButton, 431, 622); // Login scene size
    }

    // Handler for Home button
    @FXML
    private void goToHome() {
        navigateTo("Home.fxml", homeButton, 400, 600); // Home scene size
    }

    private void navigateTo(String fxmlFile, Button sourceButton, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            AnchorPane page = loader.load();

            Scene scene = new Scene(page, width, height);
            Stage currentStage = (Stage) sourceButton.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Unable to navigate to " + fxmlFile);
        }
    }

    // Display alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
