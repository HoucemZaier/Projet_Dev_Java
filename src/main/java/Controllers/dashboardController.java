package Controllers;

import Models.User;
import utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class dashboardController implements Initializable {

    @FXML
    private Button userManagementBtn;
    @FXML
    private Button overviewBtn;
    @FXML
    private Button destinationsBtn;
    @FXML
    private Button billetsBtn;
    @FXML
    private Button hotelsBtn;
    @FXML
    private Button excursionsBtn;
    @FXML
    private Button blogBtn;
    @FXML
    private Button forumBtn;
    @FXML
    private Button activitiesBtn;
    @FXML
    private Button settingsBtn;
    @FXML
    private Button logoutBtn;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize controller
        System.out.println("Dashboard controller initialized");

        // Hide user management button for Moderateur
        if (UserSession.getInstance().isModerator()) {
            userManagementBtn.setVisible(false);
            userManagementBtn.setManaged(false);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            UserSession.getInstance().setCurrentUser(user);
            System.out.println("Current user set: " + user.getNom() + " " + user.getPrenom() + " (" + user.getClass().getSimpleName() + ")");
        }
    }

    @FXML
    private void handleUserManagement(ActionEvent event) {
        // Check if current user is Admin
        if (!UserSession.getInstance().canAccessUserManagement()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied",
                "Only Admin users can access User Management. Your role: " + UserSession.getInstance().getCurrentUserType());
            return;
        }
        navigateTo("/gestionUser.fxml", "User Management");
    }

    @FXML
    private void handleOverview(ActionEvent event) {
        navigateTo("/dashboard.fxml", "Dashboard Overview");
    }

    @FXML
    private void handleDestinations(ActionEvent event) {
        showNotImplemented("Destinations");
    }

    @FXML
    private void handleBillets(ActionEvent event) {
        showNotImplemented("Billets");
    }

    @FXML
    private void handleHotels(ActionEvent event) {
        showNotImplemented("Hotels");
    }

    @FXML
    private void handleExcursions(ActionEvent event) {
        showNotImplemented("Excursions");
    }

    @FXML
    private void handleBlog(ActionEvent event) {
        showNotImplemented("Blog Posts");
    }

    @FXML
    private void handleForum(ActionEvent event) {
        showNotImplemented("Community Forum");
    }

    @FXML
    private void handleActivities(ActionEvent event) {
        showNotImplemented("Activities");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        showNotImplemented("Settings");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Clear user session
        UserSession.getInstance().logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - Login");
            stage.show();

            System.out.println("Logged out successfully");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // If navigating to gestionUser, set the user context
            if (fxmlFile.equals("/gestionUser.fxml")) {
                UserManagementController controller = loader.getController();
                if (controller != null && currentUser != null) {
                    controller.setCurrentUser(currentUser);
                }
            }

            Stage stage = (Stage) userManagementBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - " + title);
            stage.setMaximized(true);
            stage.show();

            System.out.println("Navigated to: " + title);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showNotImplemented(String feature) {
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon",
            "The " + feature + " feature is coming soon!");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

