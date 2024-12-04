package com.example.oodcw;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboard {

    @FXML
    private Button addUserButton;

    @FXML
    private Button removeUserButton;

    @FXML
    private Button editArticlesButton;

    @FXML
    private Button logoutButton;

    @FXML
    private void handleAddUser() {
        navigateTo("AddUser.fxml");
    }

    @FXML
    private void handleRemoveUser() {
        navigateTo("RemoveUser.fxml");
    }

    @FXML
    private void handleEditArticles() {
        navigateTo("EditArticles.fxml");
    }

    @FXML
    private void handleLogout() {
        navigateTo("Home.fxml");
    }

    private void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to navigate to " + fxmlFile);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
