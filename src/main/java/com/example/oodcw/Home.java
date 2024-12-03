package com.example.oodcw;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import java.io.IOException;

public class Home {

    // Method to load a new FXML file and close the current window
    private void loadNewScene(String fxmlFile, ActionEvent event) {
        try {
            // Load the specified FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Create a new stage for the new screen
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(fxmlFile.replace(".fxml", ""));
            stage.show();

            // Close the current window
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handler for Login button
    public void handleLogin(ActionEvent event) {
        loadNewScene("Login.fxml", event);
    }

    // Handler for Register button
    public void handleRegister(ActionEvent event) {
        loadNewScene("Registration.fxml", event);
    }

    // Load recommendations based on user preferences on Login or Registration success
    public void loadRecommendationsOnLogin() {
        // Assuming preferences are updated after successful login/registration
        if (Login.userPreference != null && !Login.userPreference.isEmpty()) {
            // Create an instance of Recommendation to call updateRecommendations
            Recommendation recommendation = new Recommendation();
            recommendation.updateRecommendations(Login.userPreference);
        }
    }
}
