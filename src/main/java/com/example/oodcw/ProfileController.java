package com.example.oodcw;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProfileController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextArea preferencesField;

    @FXML
    private Button saveButton;

    @FXML
    private Button homeButton;

    // ExecutorService for concurrency
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Reference to the Recommendation class
    private final Recommendation recommendation = new Recommendation();

    @FXML
    private void initialize() {
        usernameField.setText(Login.currentUser);
        preferencesField.setText(Login.userPreference);

        saveButton.setOnAction(event -> savePreferences());
    }

    @FXML
    private void savePreferences() {
        String updatedPreferences = preferencesField.getText();

        if (updatedPreferences.isEmpty()) {
            showAlert("Error", "Preferences cannot be empty.");
            return;
        }

        // Update preferences in the database asynchronously
        executorService.submit(() -> {
            String query = "UPDATE users SET preferences = ? WHERE username = ?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, updatedPreferences);
                pstmt.setString(2, Login.currentUser);
                pstmt.executeUpdate();

                // Update in-memory preferences
                Login.userPreference = updatedPreferences;

                // Update recommendations with the new preferences
                recommendation.updateRecommendations(Login.userPreference);

                // Notify the user of success
                javafx.application.Platform.runLater(() -> showAlert("Success", "Preferences updated successfully."));
            } catch (SQLException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showAlert("Error", "Unable to update preferences. Please try again later."));
            }
        });
    }

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            AnchorPane homePage = loader.load();
            Scene homeScene = new Scene(homePage);
            Stage currentStage = (Stage) homeButton.getScene().getWindow();
            currentStage.setScene(homeScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load the home page.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Shutdown ExecutorService when the controller is destroyed
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
