package com.example.oodcw;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Registration {

    @FXML
    private TextField newName;

    @FXML
    private TextField newUsername;

    @FXML
    private TextField newmail;

    @FXML
    private PasswordField newPassword1;

    @FXML
    private PasswordField newPassword2;

    @FXML
    private CheckBox techCheckBox;

    @FXML
    private CheckBox healthCheckBox;

    @FXML
    private CheckBox aiCheckBox;

    @FXML
    private CheckBox sportsCheckBox;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink AccountLink;

    // Flag to track if the user came from the AdminDashboard
    private boolean isAdminContext = false;

    public void setAdminContext(boolean isAdminContext) {
        this.isAdminContext = isAdminContext;
    }

    @FXML
    private void initialize() {
        registerButton.setOnAction(event -> handleRegistration());
        AccountLink.setOnAction(event -> openLoginWindow());
    }

    private void handleRegistration() {
        String name = newName.getText();
        String username = newUsername.getText();
        String email = newmail.getText();
        String password1 = newPassword1.getText();
        String password2 = newPassword2.getText();
        String preferences = getSelectedPreferences();

        // Validate input
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty() || preferences.isEmpty()) {
            showAlert("Error", "Please fill in all fields and select at least one preference.");
            return;
        }

        if (!password1.equals(password2)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Error", "Invalid email format.");
            return;
        }

        if (!isPasswordStrong(password1)) {
            showAlert("Error", "Password must be at least 8 characters long, contain an uppercase letter, a number, and a special character.");
            return;
        }

        if (isUsernameTaken(username)) {
            showAlert("Error", "Username is already taken.");
            return;
        }

        if (isEmailRegistered(email)) {
            showAlert("Error", "Email is already registered.");
            return;
        }

        // Register the user
        if (registerUser(name, username, email, password1, preferences)) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Registration Successful");
            successAlert.setHeaderText(null);
            successAlert.setContentText("User added successfully!");

            successAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (isAdminContext) {
                        navigateToAdminDashboard();
                    } else {
                        openLoginWindow();
                    }
                }
            });
        } else {
            showAlert("Error", "Failed to register. Please try again.");
        }
    }

    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            Stage currentStage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            currentStage.setScene(scene);

            // Refresh the AdminDashboard after adding the user
            AdminDashboard controller = loader.getController();
            controller.populateUserTable();
            controller.showAlert("Success", "User added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to navigate back to Admin Dashboard.");
        }
    }

    private String getSelectedPreferences() {
        List<String> selectedPreferences = new ArrayList<>();
        if (techCheckBox.isSelected()) selectedPreferences.add("Technology");
        if (healthCheckBox.isSelected()) selectedPreferences.add("Health");
        if (aiCheckBox.isSelected()) selectedPreferences.add("AI");
        if (sportsCheckBox.isSelected()) selectedPreferences.add("Sports");
        return String.join(",", selectedPreferences);
    }

    private boolean isUsernameTaken(String username) {
        String query = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while checking the username. Please try again later.");
        }
        return false;
    }

    private boolean isEmailRegistered(String email) {
        String query = "SELECT email FROM users WHERE email = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while checking the email. Please try again later.");
        }
        return false;
    }

    private boolean registerUser(String name, String username, String email, String password, String preferences) {
        String insertQuery = "INSERT INTO users (name, username, email, password, preferences) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setString(5, preferences);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while registering. Please try again later.");
        }
        return false;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isPasswordStrong(String password) {
        String passwordRegex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
        return Pattern.matches(passwordRegex, password);
    }

    private void openLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.show();

            Stage currentStage = (Stage) AccountLink.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load the login page.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
