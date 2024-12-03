package com.example.oodcw;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class Login {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button LoginButton;

    @FXML
    private Button ResetButton;

    @FXML
    private Hyperlink RegisterLink;

    @FXML
    private Hyperlink ForgetPassword;

    public static String currentUser;
    public static String userPreference;

    private int loginAttempts = 0; // Tracks login attempts

    @FXML
    private void handleLogin() {
        String enteredUsername = username.getText();
        String enteredPassword = password.getText();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        if (validateUser(enteredUsername, enteredPassword)) {
            currentUser = enteredUsername; // Store logged-in user
            userPreference = loadUserPreferences(enteredUsername); // Load user preference

            showAlert(Alert.AlertType.INFORMATION, "Success", "Login Successful!");
            redirectToDashboardPage(); // Redirect to the next page
        } else {
            loginAttempts++;
            if (loginAttempts >= 3) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Too many incorrect attempts. Please reset your password.");
                return;
            }
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect username or password. Attempt " + loginAttempts + "/3.");
        }
    }

    private boolean validateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error occurred.");
        }
        return false;
    }

    private String loadUserPreferences(String username) {
        String query = "SELECT preferences FROM users WHERE username = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("preferences");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load user preferences.");
        }
        return null;
    }

    private void redirectToDashboardPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            AnchorPane dashboardPage = loader.load();

            Scene dashboardScene = new Scene(dashboardPage);
            Stage currentStage = (Stage) LoginButton.getScene().getWindow();
            currentStage.setScene(dashboardScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load the dashboard page.");
        }
    }

    @FXML
    private void clearFields() {
        username.clear();
        password.clear();
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Registration.fxml"));
            AnchorPane registerPage = loader.load();
            Scene registerScene = new Scene(registerPage);

            Stage currentStage = (Stage) RegisterLink.getScene().getWindow();
            currentStage.setScene(registerScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load the registration page.");
        }
    }

    @FXML
    private void handleForgetPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password",
                "If you have forgotten your password, please contact support for assistance.");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
