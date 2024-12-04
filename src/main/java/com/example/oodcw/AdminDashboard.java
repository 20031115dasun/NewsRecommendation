package com.example.oodcw;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AdminDashboard {

    @FXML
    private TableView<List<String>> userTable;

    @FXML
    private TableColumn<List<String>, String> nameColumn;

    @FXML
    private TableColumn<List<String>, String> usernameColumn;

    @FXML
    private Button addUserButton;

    @FXML
    private Button deleteUserButton;

    private ObservableList<List<String>> users;

    @FXML
    public void initialize() {
        // Initialize the TableView with users from the database
        populateUserTable();

        // Handle Add User button click
        addUserButton.setOnAction(event -> handleAddUser());

        // Handle Delete User button click
        deleteUserButton.setOnAction(event -> handleDeleteUser());
    }

    // Method to populate the TableView with users from the database
    private void populateUserTable() {
        // Fetch all users from the database
        List<List<String>> userList = Database.getAllUsers();

        // Create an ObservableList to bind to the TableView
        users = FXCollections.observableArrayList(userList);

        // Set up table columns
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(1)));  // Name is the second column in the list
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(2)));  // Username is the third column in the list

        // Add users to the TableView
        userTable.setItems(users);
    }

    @FXML
    private void handleAddUser() {
        navigateTo("Registration.fxml"); // This will navigate to the registration page (Add User).
    }


    // Handle Delete User (Deletes the selected user)
    private void handleDeleteUser() {
        List<String> selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            int userId = Integer.parseInt(selectedUser.get(0)); // Get the ID (first element in the list)

            // Delete the selected user from the database
            boolean success = Database.deleteUser(userId);

            if (success) {
                // Update the table view to reflect the changes
                populateUserTable();
                showAlert("Success", "User deleted successfully.");
            } else {
                showAlert("Error", "Failed to delete the user.");
            }
        } else {
            showAlert("Error", "Please select a user to delete.");
        }
    }

    // Method to navigate to a different page (FXML)
    private void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            Stage currentStage = (Stage) addUserButton.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to navigate to " + fxmlFile);
        }
    }

    // Method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
