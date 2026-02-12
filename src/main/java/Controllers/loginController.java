package Controllers;

import Models.User;
import Services.ServiceUser;
import utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class loginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginBtn;
    @FXML
    private Button signupBtn;

    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter both email and password.");
            return;
        }

        try {
            User authenticatedUser = serviceUser.authenticate(email, password);

            if (authenticatedUser != null) {
                // Store user in session
                UserSession.getInstance().setCurrentUser(authenticatedUser);

                // Check if user has access to dashboard
                if (!UserSession.getInstance().canAccessDashboard()) {
                    UserSession.getInstance().logout();
                    showAlert(Alert.AlertType.ERROR, "Access Denied",
                        "Only Admin and Moderateur can access the dashboard. Client and Guide users are not allowed.");
                    return;
                }

                // Successful login - navigate to dashboard
                navigateToDashboard(authenticatedUser);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to authenticate: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            // Navigate to create account screen in a new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/create.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("PlaNova - Create Account");
            newStage.setFullScreen(false);
            newStage.setMaximized(false);
            newStage.setResizable(true);
            newStage.show();

            // Close the login window
            Stage loginStage = (Stage) signupBtn.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open create account screen: " + e.getMessage());
        }
    }

    private void navigateToDashboard(User user) {
        try {
            // Navigate to dashboard instead of gestionUser
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));

            if (loader.getLocation() == null) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to find dashboard file (dashboard.fxml)");
                return;
            }

            Parent root = loader.load();

            // Get the controller and pass user info
            dashboardController controller = loader.getController();
            if (controller == null) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load dashboard controller");
                return;
            }

            // Pass the authenticated user to the controller
            controller.setCurrentUser(user);

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - Dashboard");
            stage.setMaximized(true); // Maximize for better dashboard view
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open dashboard: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Clear user session
        UserSession.getInstance().logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - Login");
            stage.show();

            System.out.println("Logged out successfully");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
