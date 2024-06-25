package application.Controller;

import application.Util.DB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUpController {

    @FXML
    private TextField idField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private Connection connection;

    @FXML
    public void initialize() {
        // Initialize database connection
        try {
            connection = DB.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Failed to connect to the database. Please try again later.");
        }
    }

    @FXML
    void signUp(ActionEvent event) {
        if (connection == null) {
            showErrorAlert("Database connection is not available. Please try again later.");
            return;
        }

        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || surname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showErrorAlert("All fields are required.");
            return;
        }

        // Query to insert new user
        String query = "INSERT INTO user (id, name, surname, username, password) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, Integer.parseInt(id));
            statement.setString(2, name);
            statement.setString(3, surname);
            statement.setString(4, username);
            statement.setString(5, password);
            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                // User successfully registered
                showInfoAlert("Sign Up successful!");
                backToLogin(event); // Navigate back to login after successful sign up
            } else {
                // Failed to register user
                showErrorAlert("Failed to sign up!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error occurred while signing up. Please try again.");
        }
    }

    @FXML
    void backToLogin(ActionEvent event) {
        // Navigate back to login screen
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/application/Login.fxml"));
            Stage stage = (Stage) idField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 300));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
