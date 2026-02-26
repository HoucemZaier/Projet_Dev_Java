package Controllers;

import Models.User;
import Services.NotificationService;
import utils.UserSession;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ExploreController implements Initializable {

    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView userAvatarImage;

    @FXML
    private TextField searchField;

    @FXML
    private HBox userProfileHBox;

    @FXML
    private Button exploreLogoutBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserInfo();
        setupSearchFunctionality();
        setupUserProfileClick();
        setupLogoutButtonAnimation();

        // Check for client notifications (guide application status, etc.)
        checkClientNotifications();
    }

    private void loadUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Update user name
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }

            // Load user image if available
            if (userAvatarImage != null && currentUser.getImageurl() != null && !currentUser.getImageurl().isEmpty()) {
                try {
                    Image image = new Image(currentUser.getImageurl());
                    userAvatarImage.setImage(image);
                } catch (Exception e) {
                    // Use default image if loading fails
                    loadDefaultUserImage();
                }
            } else {
                loadDefaultUserImage();
            }
        }
    }

    private void loadDefaultUserImage() {
        try {
            if (userAvatarImage != null) {
                var imageStream = getClass().getResourceAsStream("/user.png");
                if (imageStream != null) {
                    Image defaultImage = new Image(imageStream);
                    if (!defaultImage.isError()) {
                        userAvatarImage.setImage(defaultImage);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load default user image: " + e.getMessage());
        }
    }

    private void setupSearchFunctionality() {
        if (searchField != null) {
            searchField.setOnAction(event -> handleSearch());
        }
    }

    private void setupUserProfileClick() {
        if (userProfileHBox != null) {
            userProfileHBox.setOnMouseClicked(event -> handleUserProfileClick());
            userProfileHBox.setStyle(userProfileHBox.getStyle() + "; -fx-cursor: hand;");
        }
    }

    private void setupLogoutButtonAnimation() {
        if (exploreLogoutBtn == null) return;

        // Add hover effects for the logout button
        exploreLogoutBtn.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), exploreLogoutBtn);
            scaleUp.setToX(1.2);
            scaleUp.setToY(1.2);
            scaleUp.play();

            // Add hover effect
            exploreLogoutBtn.setStyle(exploreLogoutBtn.getStyle() + "; -fx-background-color: rgba(255,255,255,0.2);");
        });

        exploreLogoutBtn.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), exploreLogoutBtn);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();

            // Remove hover effect
            String style = exploreLogoutBtn.getStyle();
            exploreLogoutBtn.setStyle(style.replace("; -fx-background-color: rgba(255,255,255,0.2)", ""));
        });

        exploreLogoutBtn.setOnMousePressed(e -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), exploreLogoutBtn);
            scalePress.setToX(0.9);
            scalePress.setToY(0.9);
            scalePress.play();
        });

        exploreLogoutBtn.setOnMouseReleased(e -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), exploreLogoutBtn);
            scaleRelease.setToX(1.0);
            scaleRelease.setToY(1.0);
            scaleRelease.play();
        });
    }

    @FXML
    private void handleSearch() {
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) {
            // Implement search functionality
            System.out.println("Searching for: " + searchQuery);
            showAlert(Alert.AlertType.INFORMATION, "Recherche", "Recherche en cours pour: " + searchQuery);
        }
    }

    @FXML
    private void handleUserProfileClick() {
        openUserAccountPage();
    }

    @FXML
    private void handleLogout() {
        createCustomLogoutDialog();
    }

    private void createCustomLogoutDialog() {
        // Create custom dialog stage
        Stage dialogStage = new Stage();
        dialogStage.initOwner(exploreLogoutBtn.getScene().getWindow());
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setResizable(false);
        dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        // Create dialog content
        VBox dialogContent = new VBox();
        dialogContent.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef); " +
                              "-fx-background-radius: 20; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10); " +
                              "-fx-padding: 0;");
        dialogContent.setPrefWidth(400);
        dialogContent.setPrefHeight(250);

        // Header with gradient and icon
        HBox header = new HBox();
        header.setStyle("-fx-background-color: linear-gradient(to right, #dc3545, #c82333); " +
                       "-fx-background-radius: 20 20 0 0; " +
                       "-fx-padding: 20; " +
                       "-fx-alignment: center-left;");

        Label iconLabel = new Label("🚪");
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        Label titleLabel = new Label("  Confirm Logout");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        header.getChildren().addAll(iconLabel, titleLabel);

        // Content area
        VBox contentArea = new VBox();
        contentArea.setStyle("-fx-padding: 30; -fx-spacing: 20; -fx-alignment: center;");

        Label messageLabel = new Label("Are you sure you want to logout?");
        messageLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label subMessageLabel = new Label("You will be redirected to the login screen and all windows will be closed.");
        subMessageLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px; -fx-wrap-text: true; -fx-text-alignment: center;");
        subMessageLabel.setMaxWidth(340);

        contentArea.getChildren().addAll(messageLabel, subMessageLabel);

        // Button area
        HBox buttonArea = new HBox();
        buttonArea.setStyle("-fx-padding: 0 30 30 30; -fx-spacing: 15; -fx-alignment: center;");

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: linear-gradient(to bottom, #dc3545, #c82333); " +
                             "-fx-text-fill: white; " +
                             "-fx-background-radius: 25; " +
                             "-fx-padding: 12 25; " +
                             "-fx-font-size: 14px; " +
                             "-fx-font-weight: bold; " +
                             "-fx-cursor: hand; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: linear-gradient(to bottom, #6c757d, #5a6268); " +
                             "-fx-text-fill: white; " +
                             "-fx-background-radius: 25; " +
                             "-fx-padding: 12 25; " +
                             "-fx-font-size: 14px; " +
                             "-fx-font-weight: bold; " +
                             "-fx-cursor: hand; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Add button animations
        addCustomButtonAnimation(logoutButton, "#dc3545", "#c82333");
        addCustomButtonAnimation(cancelButton, "#6c757d", "#5a6268");

        buttonArea.getChildren().addAll(cancelButton, logoutButton);

        // Assemble dialog
        dialogContent.getChildren().addAll(header, contentArea, buttonArea);

        // Button actions
        logoutButton.setOnAction(e -> {
            dialogStage.close();
            performLogoutWithAnimation();
        });

        cancelButton.setOnAction(e -> {
            // Add close animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), dialogContent);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> dialogStage.close());
            fadeOut.play();
        });

        // Create scene and show dialog
        Scene dialogScene = new Scene(dialogContent);
        dialogScene.getRoot().setStyle("-fx-background-color: transparent;");
        dialogStage.setScene(dialogScene);

        // Add entrance animation
        dialogContent.setOpacity(0);
        dialogContent.setScaleX(0.8);
        dialogContent.setScaleY(0.8);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogContent);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), dialogContent);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn);
        entrance.play();

        // Center dialog on parent window
        dialogStage.setX(exploreLogoutBtn.getScene().getWindow().getX() +
                        (exploreLogoutBtn.getScene().getWindow().getWidth() - 400) / 2);
        dialogStage.setY(exploreLogoutBtn.getScene().getWindow().getY() +
                        (exploreLogoutBtn.getScene().getWindow().getHeight() - 250) / 2);

        dialogStage.show();
    }

    private void addCustomButtonAnimation(Button button, String normalColor, String hoverColor) {
        String baseStyle = button.getStyle();

        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();

            button.setStyle(baseStyle.replace(normalColor, hoverColor) +
                           "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 4);");
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();

            button.setStyle(baseStyle);
        });

        button.setOnMousePressed(e -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), button);
            scalePress.setToX(0.95);
            scalePress.setToY(0.95);
            scalePress.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), button);
            scaleRelease.setToX(1.05);
            scaleRelease.setToY(1.05);
            scaleRelease.play();
        });
    }

    private void performLogoutWithAnimation() {
        // Clear user session
        UserSession.getInstance().logout();

        try {
            // Close all open windows gracefully with animation
            javafx.stage.Window currentWindow = exploreLogoutBtn.getScene().getWindow();

            // Find and close the account management window if it exists
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof javafx.stage.Stage) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) window;
                    if (stage.getTitle() != null && stage.getTitle().contains("Account Management")) {
                        // Animate close for account management window
                        FadeTransition fadeOutAccount = new FadeTransition(Duration.seconds(0.3), stage.getScene().getRoot());
                        fadeOutAccount.setToValue(0);
                        fadeOutAccount.setOnFinished(e -> stage.close());
                        fadeOutAccount.play();
                    }
                }
            }

            // Create fade out animation for current window before navigation
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.4), currentWindow.getScene().getRoot());
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                // Close current window
                if (currentWindow instanceof javafx.stage.Stage) {
                    ((javafx.stage.Stage) currentWindow).close();
                }
                // Navigate to login
                navigateToLoginFromLogout();
            });
            fadeOut.play();

        } catch (Exception e) {
            System.err.println("Error during logout animation: " + e.getMessage());
            navigateToLoginFromLogout();
        }
    }

    private void navigateToLoginFromLogout() {
        try {
            // Find the main application window or create a new one
            javafx.stage.Stage primaryStage = null;
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof javafx.stage.Stage) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) window;
                    if (stage.getTitle() != null && !stage.getTitle().contains("Explore") && !stage.getTitle().contains("Account Management")) {
                        primaryStage = stage;
                        break;
                    }
                }
            }

            // If no existing window, create a new one
            if (primaryStage == null) {
                primaryStage = new javafx.stage.Stage();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("PlaNova - Login");
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Add professional entrance animation for login screen
            root.setOpacity(0);
            root.setScaleX(0.9);
            root.setScaleY(0.9);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.5), root);
            scaleIn.setFromX(0.9);
            scaleIn.setFromY(0.9);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn);
            entrance.play();

            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }

            System.out.println("Successfully logged out from explore interface and returned to login screen");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", "Échec du retour à l'écran de connexion: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Inattendue", "Une erreur inattendue s'est produite lors de la déconnexion: " + e.getMessage());
        }
    }

    private void openUserAccountPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/useraccount.fxml"));
            Parent root = loader.load();

            // Get the controller and pass user info
            accountmanagement controller = loader.getController();
            if (controller != null) {
                controller.setCurrentUser(UserSession.getInstance().getCurrentUser());

                // Set callback to refresh notifications when account window is closed
                controller.setProfileUpdateCallback(() -> {
                    System.out.println("🔄 Account updated, refreshing notifications...");
                    // Delay the notification check to allow time for any guide application changes
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1000); // Small delay
                            checkClientNotifications();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    });
                });
            }

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("PlaNova - Account Management");
            newStage.setResizable(true);

            // Add callback when window closes
            newStage.setOnHidden(e -> {
                // Check for new notifications when account window closes
                javafx.concurrent.Task<Void> refreshTask = new javafx.concurrent.Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Thread.sleep(500); // Small delay to allow notifications to be created
                        return null;
                    }
                };

                refreshTask.setOnSucceeded(event -> checkClientNotifications());
                new Thread(refreshTask).start();
            });

            newStage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", "Échec de l'ouverture de la page de compte: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Check for client notifications about guide application status
     */
    private void checkClientNotifications() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        // Run notification check in background to avoid blocking UI
        javafx.concurrent.Task<List<String>> notificationTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<String> call() throws Exception {
                NotificationService notificationService = new NotificationService();
                return notificationService.getClientNotifications(currentUser.getEmail());
            }
        };

        notificationTask.setOnSucceeded(e -> {
            List<String> notifications = notificationTask.getValue();
            if (!notifications.isEmpty()) {
                Platform.runLater(() -> showClientNotifications(notifications));
            }
        });

        notificationTask.setOnFailed(e -> {
            Throwable exception = notificationTask.getException();
            System.err.println("Erreur lors du chargement des notifications client: " + exception.getMessage());
        });

        new Thread(notificationTask).start();
    }

    /**
     * Show client notifications in a professional dialog
     */
    private void showClientNotifications(List<String> notifications) {
        try {
            // Create notification dialog
            Alert notificationAlert = new Alert(Alert.AlertType.INFORMATION);
            notificationAlert.setTitle("Notifications");
            notificationAlert.setHeaderText("Nouvelles notifications");

            // Process notifications to create readable content
            StringBuilder content = new StringBuilder();
            for (String notification : notifications) {
                if (notification.contains("DEMANDE GUIDE REJETÉE")) {
                    content.append("❌ Demande de Guide Rejetée\n\n");

                    // Extract rejection reason
                    if (notification.contains("Raison:")) {
                        String reason = notification.substring(notification.indexOf("Raison:") + 7);
                        if (reason.contains("\n")) {
                            reason = reason.substring(0, reason.indexOf("\n")).trim();
                        }
                        content.append("Raison: ").append(reason).append("\n\n");
                    }

                    content.append("Vous pouvez soumettre une nouvelle demande après avoir corrigé les points mentionnés.\n\n");

                } else if (notification.contains("DEMANDE GUIDE APPROUVÉE")) {
                    content.append("✅ Félicitations! Demande de Guide Approuvée\n\n");
                    content.append("Votre compte a été converti en compte Guide.\n");
                    content.append("Vous pouvez maintenant accéder aux fonctionnalités de guide.\n\n");
                }
            }

            notificationAlert.setContentText(content.toString());

            // Style the dialog
            DialogPane dialogPane = notificationAlert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());
            dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #e2e8f0); " +
                               "-fx-border-color: #e2e8f0; -fx-border-radius: 15; -fx-background-radius: 15;");

            // Show dialog and mark notifications as read after user acknowledges
            notificationAlert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    markNotificationsAsRead(notifications);
                }
            });

        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des notifications: " + e.getMessage());
            // Simple fallback
            showAlert(Alert.AlertType.INFORMATION, "Notifications",
                     "Vous avez " + notifications.size() + " nouvelle(s) notification(s).");
        }
    }

    /**
     * Mark client notifications as read
     */
    private void markNotificationsAsRead(List<String> notifications) {
        javafx.concurrent.Task<Void> markAsReadTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                NotificationService notificationService = new NotificationService();
                User currentUser = UserSession.getInstance().getCurrentUser();

                for (String notification : notifications) {
                    notificationService.markClientNotificationAsRead(currentUser.getEmail(), notification);
                }
                return null;
            }
        };

        markAsReadTask.setOnSucceeded(e -> {
            System.out.println("✅ Notifications marquées comme lues");
        });

        markAsReadTask.setOnFailed(e -> {
            System.err.println("❌ Erreur lors du marquage des notifications comme lues: " +
                             markAsReadTask.getException().getMessage());
        });

        new Thread(markAsReadTask).start();
    }
}

