package com.PlaNova.controllers;

import com.PlaNova.models.Admin;
import com.PlaNova.models.User;
import com.PlaNova.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class dashboardController implements Initializable {

    @FXML
    private Button exploreBtn;
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
    private Button chambresBtn;
    @FXML
    private Button excursionsBtn;
    @FXML
    private Button transportPubliqueBtn;
    @FXML
    private Button transportPriveeBtn;
    @FXML
    private Button statistiquebtn;
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
    @FXML
    private HBox userProfileHBox;
    @FXML
    private StackPane notificationStackPane;
    @FXML
    private Label notificationBadge;
    @FXML
    private javafx.scene.chart.LineChart<String, Number> bookingChart;
    @FXML
    private VBox recentBookingsList;
    @FXML
    private VBox dynamicContentContainer;
    @FXML
    private HBox defaultStatsContainer;
    @FXML
    private HBox defaultChartsContainer;

    private User currentUser;
    private Stage accountStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize controller
        System.out.println("Dashboard controller initialized");

        // Configure the stage to be resizable and allow maximizing
        javafx.application.Platform.runLater(() -> {
            try {
                Stage stage = (Stage) overviewBtn.getScene().getWindow();
                if (stage != null) {
                    stage.setResizable(true);
                    stage.setMaximized(true); // Allow full screen for better visibility
                    stage.setMinWidth(1100);
                    stage.setMinHeight(700);
                }
            } catch (Exception e) {
                System.err.println("Could not configure dashboard stage: " + e.getMessage());
            }
        });

        // Hide user management button for Moderateur and Guide
        if (UserSession.getInstance().isModerator() || UserSession.getInstance().isGuide()) {
            userManagementBtn.setVisible(false);
            userManagementBtn.setManaged(false);
        }

        // Hide notification icon for non-admin users (Moderateur and Guide can't see
        // admin notifications)
        if (!UserSession.getInstance().isAdmin()) {
            notificationStackPane.setVisible(false);
            notificationStackPane.setManaged(false);
        } else {
            // Load notification count for admin users
            loadNotificationCount();
            // Start periodic notification refresh
            startNotificationRefresh();
        }

        // Initialize account stage as null
        accountStage = null;

        // Show current user name, role and profile image from session (e.g. when
        // dashboard loads)
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null) {
            currentUser = user;
            updateProfileDisplay(user);
        }

        // Load default overview (Statistics)
        handleOverview(null);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            UserSession.getInstance().setCurrentUser(user);
            System.out.println("Current user set: " + user.getNom() + " " + user.getPrenom() + " ("
                    + user.getClass().getSimpleName() + ")");
            updateProfileDisplay(user);
        }
    }

    /**
     * Sets the top-right profile area to the current user's name, role and image
     * from the database.
     */
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
                    userProfileImage.setSmooth(true); // Enable smooth scaling for better quality
                    userProfileImage.setCache(true); // Cache for better performance

                    // Apply circular clip for better appearance
                    Circle clip = new Circle(18);
                    clip.setCenterX(18);
                    clip.setCenterY(18);
                    userProfileImage.setClip(clip);
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
                    "Only Admin users can access User Management. Your role: "
                            + UserSession.getInstance().getCurrentUserType());
            return;
        }
        // Intégrer la page Gestion Utilisateurs directement dans le dashboard
        loadFXMLIntoDashboard("/ui/user/gestionuser.fxml", "Gestion Utilisateurs");
    }

    @FXML
    private void handleNotifications(javafx.scene.input.MouseEvent event) {
        // Check if current user is Admin
        if (!UserSession.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès Refusé",
                    "Seuls les administrateurs peuvent accéder aux notifications.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/user/adminnotifications.fxml"));
            Parent root = loader.load();

            // Get the controller and set the callback for notification updates
            AdminNotificationsController notificationController = loader.getController();
            notificationController.setOnNotificationUpdate(this::loadNotificationCount);

            Stage notificationStage = new Stage();
            notificationStage.setTitle("PlaNova - Notifications Administrateur");
            notificationStage.setScene(new Scene(root));
            notificationStage.setResizable(true);
            notificationStage.initModality(Modality.NONE); // Allow working with dashboard while notifications are open
            notificationStage.setWidth(650);
            notificationStage.setHeight(750);

            // Center on screen
            notificationStage.centerOnScreen();

            // When notification window is closed, refresh the notification count
            notificationStage.setOnHidden(e -> loadNotificationCount());

            notificationStage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les notifications: " + e.getMessage());
        }
    }

    /**
     * Load and display notification count in the badge
     */
    private void loadNotificationCount() {
        try {
            com.PlaNova.services.NotificationService notificationService = new com.PlaNova.services.NotificationService();
            int notificationCount = notificationService.getUnreadNotificationsCount();

            javafx.application.Platform.runLater(() -> {
                if (notificationCount > 0) {
                    // Check if count increased for pulse animation
                    boolean countIncreased = false;
                    if (notificationBadge.isVisible()) {
                        try {
                            int currentCount = Integer.parseInt(notificationBadge.getText());
                            countIncreased = notificationCount > currentCount;
                        } catch (NumberFormatException ignored) {
                        }
                    } else {
                        countIncreased = true; // First time showing
                    }

                    notificationBadge.setText(String.valueOf(notificationCount));
                    notificationBadge.setVisible(true);

                    // Add pulse animation for new notifications
                    if (countIncreased) {
                        addNotificationPulseAnimation();
                    }
                } else {
                    notificationBadge.setVisible(false);
                }
            });

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du nombre de notifications: " + e.getMessage());
            // Hide badge on error
            javafx.application.Platform.runLater(() -> notificationBadge.setVisible(false));
        }
    }

    /**
     * Start periodic notification refresh every 30 seconds
     */
    private void startNotificationRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(30), e -> loadNotificationCount()));
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Add pulse animation to notification badge for new notifications
     */
    private void addNotificationPulseAnimation() {
        javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(600), notificationStackPane);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.2);
        scaleTransition.setToY(1.2);
        scaleTransition.setCycleCount(4);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();
    }

    /**
     * Public method to refresh notification count (can be called from external
     * sources)
     */
    public void refreshNotifications() {
        if (UserSession.getInstance().isAdmin() && notificationStackPane.isVisible()) {
            loadNotificationCount();
        }
    }

    @FXML
    private void handleOverview(ActionEvent event) {
        loadFXMLIntoDashboard("/ui/dashboard/statistiques.fxml", "Tableau de Bord");
    }

    @FXML
    private void handleExplore(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/explore.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) exploreBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - Travel Management");
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load explore view: " + e.getMessage());
        }
    }

    @FXML
    private void handleDestinations(ActionEvent event) {
        loadFXMLIntoDashboard("/ui/Destinations.fxml", "Destinations");
    }

    @FXML
    private void handleBillets(ActionEvent event) {
        loadFXMLIntoDashboard("/ui/Billets.fxml", "Billets");
    }

    @FXML
    public void handleHotels(ActionEvent event) {
        loadFXMLIntoDashboard("/ui/hotel/listhotel.fxml", "Gestion des Hôtels");
    }

    @FXML
    public void handleChambres(ActionEvent event) {
        loadFXMLIntoDashboard("/ui/hotel/listchambre.fxml", "Gestion des Chambres");
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
        // Show professional confirmation dialog before logout
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        alert.setContentText("Vous serez redirigé vers l'écran de connexion et perdrez votre session actuelle.");

        // Add custom professional styling to the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog");

        // Customize button text and styling
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        okButton.setText("Se déconnecter");
        okButton.getStyleClass().add("logout-confirm-btn");

        cancelButton.setText("Annuler");
        cancelButton.getStyleClass().add("logout-cancel-btn");

        java.util.Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Close account window if open
            if (accountStage != null && accountStage.isShowing()) {
                accountStage.close();
            }

            // Clear user session
            UserSession.getInstance().logout();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/user/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Planova - Connexion");
                stage.centerOnScreen();
                stage.setResizable(false);

                System.out.println("Utilisateur déconnecté avec succès");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la déconnexion: " + e.getMessage());
                e.printStackTrace();
            }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/user/useraccount.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the current user
            accountmanagement controller = loader.getController();
            if (controller != null) {
                // Set a callback to refresh dashboard when profile is updated
                controller.setProfileUpdateCallback(this::refreshProfileFromSession);
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
                // Refresh profile display when window closes in case anything was updated
                refreshProfileFromSession();
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

    /**
     * Refreshes the profile display from the current user session
     */
    public void refreshProfileFromSession() {
        User sessionUser = UserSession.getInstance().getCurrentUser();
        if (sessionUser != null) {
            currentUser = sessionUser;
            updateProfileDisplay(sessionUser);
            System.out.println("Dashboard profile display refreshed from session");
        }
    }

    private void navigateTo(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // If navigating to gestionUser, set the user context
            if (fxmlFile.equals("/ui/user/gestionuser.fxml")) {
                UserManagementController controller = loader.getController();
                if (controller != null && currentUser != null) {
                    controller.setCurrentUser(currentUser);
                }
            }

            // Get the stage from any button
            Stage stage = (Stage) overviewBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - " + title);

            // Allow full screen and resizing for better visibility
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setMinWidth(1100);
            stage.setMinHeight(700);

            stage.show();

            System.out.println("Navigated to: " + title);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to navigate to " + title + ": " + e.getMessage());
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

    @FXML
    private void handleTransportPublique(ActionEvent event) {
        // Intégrer la page Transport Publique directement dans le dashboard
        loadFXMLIntoDashboard("/ui/transport/affichierpublique.fxml", "Transport Publique");
    }

    @FXML
    private void handleTransportPrivee(ActionEvent event) {
        // Intégrer la page Transport Privée directement dans le dashboard
        loadFXMLIntoDashboard("/ui/transport/affichierprive.fxml", "Transport Privée");
    }

    @FXML
    public void handlestatistiqueHotels(ActionEvent event) {
        loadFXMLIntoDashboard("/ui/dashboard/statistiques.fxml", "Statistiques Hôtels");
    }

    /**
     * Charge une page FXML dans le conteneur dynamique du dashboard
     */
    private void loadFXMLIntoDashboard(String fxmlPath, String title) {
        try {
            // Masquer le contenu par défaut
            defaultStatsContainer.setVisible(false);
            defaultStatsContainer.setManaged(false);
            defaultChartsContainer.setVisible(false);
            defaultChartsContainer.setManaged(false);

            // Charger le nouveau contenu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            // S'il s'agit d'un HBox avec une barre latérale (sidebar), n'extraire que le
            // contenu principal
            if (content instanceof HBox hbox && !hbox.getChildren().isEmpty()) {
                javafx.scene.Node firstChild = hbox.getChildren().get(0);
                if (firstChild instanceof VBox vbox && vbox.getStyleClass().contains("sidebar")) {
                    // Supposer que le deuxième enfant est le contenu principal
                    if (hbox.getChildren().size() > 1) {
                        content = (Parent) hbox.getChildren().get(1);
                        // On doit le retirer de l'ancien parent avant de l'ajouter au nouveau
                        hbox.getChildren().remove(content);
                    }
                }
            }

            // Adapter le contenu pour s'intégrer dans le dashboard
            content.setStyle("-fx-background-color: transparent;");

            // Vider le conteneur dynamique et ajouter le nouveau contenu
            dynamicContentContainer.getChildren().clear();
            dynamicContentContainer.getChildren().add(content);

            // Mettre à jour le titre du dashboard
            updateDashboardTitle(title);

            System.out.println("Intégré avec succès: " + title);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'intégration",
                    "Impossible d'intégrer " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * }
     * 
     * /**
     * Met à jour le titre du dashboard
     */
    private void updateDashboardTitle(String title) {
        // Guard: the scene may be null during initialize() — defer until it's ready
        if (overviewBtn.getScene() == null) {
            javafx.application.Platform.runLater(() -> updateDashboardTitle(title));
            return;
        }
        javafx.scene.Node titleNode = overviewBtn.getScene().lookup(".dashboard-title");
        if (titleNode instanceof Label) {
            ((Label) titleNode).setText(title);
        }
    }
}