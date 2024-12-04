package com.example.oodcw;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AdminDashboard {

    public AnchorPane adminlogin;
    @FXML
    private Button addUserButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Button homeButton;

    @FXML
    private TableView<List<String>> userTable;

    @FXML
    private TableColumn<List<String>, String> nameColumn;

    @FXML
    private TableColumn<List<String>, String> usernameColumn;

    @FXML
    public void initialize() {
        populateUserTable();

        addUserButton.setOnAction(event -> handleAddUser());
        deleteUserButton.setOnAction(event -> handleDeleteUser());
    }

    // Method to populate the TableView with users from the database
    void populateUserTable() {
        List<List<String>> userList = Database.getAllUsers();
        ObservableList<List<String>> users = FXCollections.observableArrayList(userList);

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(1)));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(2)));

        userTable.setItems(users);
    }

    @FXML
    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Registration.fxml"));
            AnchorPane page = loader.load();

            Registration controller = loader.getController();
            controller.setAdminContext(true);

            Scene scene = new Scene(page);
            Stage currentStage = (Stage) addUserButton.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load the Registration page.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        List<String> selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            int userId = Integer.parseInt(selectedUser.get(0)); // Get the ID (first element in the list)

            boolean success = Database.deleteUser(userId);

            if (success) {
                populateUserTable();
                showAlert("Success", "User deleted successfully.");
            } else {
                showAlert("Error", "Failed to delete the user.");
            }
        } else {
            showAlert("Error", "Please select a user to delete.");
        }
    }

    @FXML
    private void homebutton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Home.fxml"));
            AnchorPane page = loader.load();
            Scene scene = new Scene(page);
            Stage currentStage = (Stage) homeButton.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load the Home page.");
        }
    }

    private void navigateTo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Registration.fxml"));
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            Stage currentStage = (Stage) addUserButton.getScene().getWindow(); // Get the current window
            currentStage.setScene(scene); // Set the new scene
            currentStage.show(); // Show the new scene
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to navigate to " + "Registration.fxml");
        }
    }


    void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
