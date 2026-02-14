package Controllers;

import Models.Admin;
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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
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
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private ImageView userProfileImage;

    private User currentUser;
    private Stage accountStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize controller
        System.out.println("Dashboard controller initialized");

        // Hide user management button for Moderateur
        if (UserSession.getInstance().isModerator()) {
            userManagementBtn.setVisible(false);
            userManagementBtn.setManaged(false);
        }

        // Initialize account stage as null
        accountStage = null;

        // Show current user name, role and profile image from session (e.g. when dashboard loads)
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null) {
            currentUser = user;
            updateProfileDisplay(user);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            UserSession.getInstance().setCurrentUser(user);
            System.out.println("Current user set: " + user.getNom() + " " + user.getPrenom() + " (" + user.getClass().getSimpleName() + ")");
            updateProfileDisplay(user);
        }
    }

    /** Sets the top-right profile area to the current user's name, role and image from the database. */
    private void updateProfileDisplay(User user) {
        if (userNameLabel != null) {
            String prenom = user.getPrenom() != null ? user.getPrenom() : "";
            String nom = user.getNom() != null ? user.getNom() : "";
            userNameLabel.setText((prenom + " " + nom).trim().isEmpty() ? "User" : (prenom + " " + nom).trim());
        }
        if (userRoleLabel != null) {
            String role = UserSession.getInstance().getCurrentUserType();
            userRoleLabel.setText(role != null ? role : "User");
        }
        if (userProfileImage != null && user.getImageurl() != null && !user.getImageurl().trim().isEmpty()) {
            try {
                String path = user.getImageurl().trim();
                String url = path;
                if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("file:")) {
                    File f = new File(path);
                    url = f.exists() ? f.toURI().toASCIIString() : ("file:" + path.replace("\\", "/"));
                }
                Image img = new Image(url, 36, 36, true, true);
                if (!img.isError()) {
                    userProfileImage.setImage(img);
                    userProfileImage.setFitWidth(36);
                    userProfileImage.setFitHeight(36);
                    userProfileImage.setPreserveRatio(true);
                }
            } catch (Exception e) {
                System.err.println("Failed to load dashboard profile image: " + e.getMessage());
            }
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
    private void handleUserProfile(javafx.scene.input.MouseEvent event) {
        // Open account view in a new window without closing dashboard
        openAccountWindow();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Close account window if open
        if (accountStage != null && accountStage.isShowing()) {
            accountStage.close();
        }

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

    private void openAccountWindow() {
        try {
            // Check if account window is already open
            if (accountStage != null && accountStage.isShowing()) {
                accountStage.requestFocus(); // Bring to front
                return;
            }

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/useraccount.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the current user
            accountmanagement controller = loader.getController();
            if (controller != null) {
                // The user data will be loaded automatically from session in initialize
                System.out.println("Account controller initialized");
            }

            // Create new stage
            accountStage = new Stage();
            accountStage.initModality(Modality.WINDOW_MODAL);
            accountStage.initOwner(overviewBtn.getScene().getWindow());
            accountStage.initStyle(StageStyle.DECORATED);
            accountStage.setTitle("PlaNova - My Account");
            accountStage.setScene(new Scene(root));
            accountStage.setMinWidth(800);
            accountStage.setMinHeight(600);

            // Center relative to parent
            accountStage.setX(overviewBtn.getScene().getWindow().getX() + 100);
            accountStage.setY(overviewBtn.getScene().getWindow().getY() + 50);

            // Handle close event
            accountStage.setOnCloseRequest(e -> {
                accountStage = null;
            });

            // Show the window
            accountStage.show();

            System.out.println("Account window opened");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open account settings: " + e.getMessage());
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

            // Get the stage from any button
            Stage stage = (Stage) overviewBtn.getScene().getWindow();
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