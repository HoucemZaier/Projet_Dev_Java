package Controllers;

import Models.User;
import Models.Client;
import Services.ServiceUser;
import Services.SocialAuthService;

import utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class  loginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button loginBtn;
    @FXML
    private Button signupBtn;
    @FXML
    private Button passwordToggleBtn;
    @FXML
    private Button facebookBtn;
    @FXML
    private Button googleBtn;


    private final ServiceUser serviceUser = new ServiceUser();
    private SocialAuthService socialAuthService;

    @FXML
    private void initialize() {
        // Bind password fields and set up visibility toggle
        if (passwordTextField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        // Add focus listeners for password container styling
        if (passwordField != null && passwordTextField != null) {
            passwordField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                updatePasswordContainerFocus(isNowFocused || passwordTextField.isFocused());
            });
            passwordTextField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                updatePasswordContainerFocus(isNowFocused || passwordField.isFocused());
            });
        }

        // Initialize social auth service using singleton to prevent port conflicts
        // CRITICAL: Only use port 8080 for ngrok compatibility - NO alternative ports
        try {
            socialAuthService = SocialAuthService.getInstance(8080); // FORCE port 8080 for ngrok
            System.out.println("✅ OAuth server initialized on port 8080 (ngrok compatible)");
        } catch (Exception e) {
            System.err.println("❌ CRITICAL: Port 8080 is required for ngrok tunnel but not available!");
            System.err.println("❌ Error: " + e.getMessage());
            socialAuthService = null;

            showAlert(AlertType.ERROR, "Port 8080 Required",
                     "OAuth server must run on port 8080 for ngrok compatibility.\n\n" +
                     "SOLUTIONS:\n" +
                     "1. Close any application using port 8080\n" +
                     "2. Run in terminal: netstat -ano | findstr :8080\n" +
                     "3. Kill the process using port 8080\n" +
                     "4. Restart PlaNova application\n\n" +
                     "Social media login will be disabled until port 8080 is available.");
        }
    }

    private void updatePasswordContainerFocus(boolean focused) {
        // Find the password container (HBox parent)
        if (passwordField.getParent() != null) {
            if (focused) {
                passwordField.getParent().getStyleClass().removeAll("password-container");
                passwordField.getParent().getStyleClass().add("password-container-focused");
            } else {
                passwordField.getParent().getStyleClass().removeAll("password-container-focused");
                passwordField.getParent().getStyleClass().add("password-container");
            }
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        if (passwordField.isVisible()) {
            // Switch to visible text
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordToggleBtn.setText("🙈"); // closed eye
        } else {
            // Switch to hidden password
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordToggleBtn.setText("👁"); // open eye
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        if (password != null) password = password.trim();

        if (email.isEmpty() || password == null || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir à la fois l'email et le mot de passe.");
            return;
        }

        try {
            User userByEmail = serviceUser.findByEmail(email);

            if (userByEmail == null) {
                showAlert(Alert.AlertType.ERROR, "Échec de la connexion", "Email incorrect ou inexistant. Vérifiez l'adresse email.");
                return;
            }

            // Try to authenticate - this will check for blocked status
            User authenticatedUser = serviceUser.authenticate(email, password);

            if (authenticatedUser == null) {
                showAlert(Alert.AlertType.ERROR, "Connexion échouée", "Mot de passe incorrect. Réessayez ou utilisez \"Mot de passe oublié\".");
                return;
            }

            // Store user in session
            UserSession.getInstance().setCurrentUser(authenticatedUser);

            // Check if 2FA is enabled for this user
            if (authenticatedUser.isTwoFactorEnabled()) {
                // Check which 2FA methods are available
                boolean hasFaceId = authenticatedUser.getFaceModelData() != null;
                boolean hasTotp = authenticatedUser.isTotpEnabled();

                if (hasFaceId || hasTotp) {
                    // Show 2FA verification dialog
                    show2FAVerification(authenticatedUser);
                } else {
                    // 2FA is enabled but no methods configured - proceed with normal login
                    proceedWithLogin(authenticatedUser);
                }
            } else {
                // Proceed with normal login flow
                proceedWithLogin(authenticatedUser);
            }

        } catch (SQLException e) {
            // Check if it's a blocked user error
            if (e.getMessage() != null && e.getMessage().startsWith("COMPTE_BLOQUE:")) {
                String message = e.getMessage().substring("COMPTE_BLOQUE:".length());
                showAlert(Alert.AlertType.ERROR, "Compte Bloqué", message);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur base de données", "Échec de l'authentification : " + e.getMessage());
            }
        }
    }

    /**
     * Show beautiful styled alert with animations
     */
    private void showStyledAlert(String title, String message, String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        
        // Create custom styled content
        VBox mainContainer = new VBox();
        
        String headerColor;
        String headerIcon;
        String borderColor;
        
        switch (type) {
            case "success":
                headerColor = "linear-gradient(to right, #10b981, #059669)";
                headerIcon = "✅";
                borderColor = "rgba(16,185,129,0.3)";
                break;
            case "error":
                headerColor = "linear-gradient(to right, #ef4444, #dc2626)";
                headerIcon = "❌";
                borderColor = "rgba(239,68,68,0.3)";
                break;
            case "warning":
                headerColor = "linear-gradient(to right, #f59e0b, #d97706)";
                headerIcon = "⚠️";
                borderColor = "rgba(245,158,11,0.3)";
                break;
            default:
                headerColor = "linear-gradient(to right, #667eea, #764ba2)";
                headerIcon = "ℹ️";
                borderColor = "rgba(103,126,234,0.3)";
        }
        
        mainContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f8fafc, #ffffff);" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5);" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 15;" +
            "-fx-min-width: 350;" +
            "-fx-pref-width: 350;"
        );
        
        // Header section
        VBox headerSection = new VBox(10);
        headerSection.setPadding(new javafx.geometry.Insets(25));
        headerSection.setStyle("-fx-background-color: " + headerColor + "; -fx-background-radius: 15 15 0 0;");
        headerSection.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label iconLabel = new Label(headerIcon);
        iconLabel.setStyle("-fx-font-size: 30px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;" +
            "-fx-text-alignment: center;"
        );
        
        headerSection.getChildren().addAll(iconLabel, titleLabel);
        
        // Content section
        VBox contentSection = new VBox(20);
        contentSection.setPadding(new javafx.geometry.Insets(25));
        contentSection.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label messageLabel = new Label(message);
        messageLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #374151;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;" +
            "-fx-text-alignment: center;" +
            "-fx-wrap-text: true;"
        );
        messageLabel.setWrapText(true);
        
        contentSection.getChildren().add(messageLabel);
        
        // Button section
        javafx.scene.layout.HBox buttonSection = new javafx.scene.layout.HBox();
        buttonSection.setPadding(new javafx.geometry.Insets(0, 25, 25, 25));
        buttonSection.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button okButton = new Button("OK");
        okButton.setStyle(
            "-fx-background-color: " + headerColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 12 30;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(103,126,234,0.3), 8, 0, 0, 3);"
        );
        
        // Button hover effect
        okButton.setOnMouseEntered(e -> okButton.setOpacity(0.9));
        okButton.setOnMouseExited(e -> okButton.setOpacity(1.0));
        
        buttonSection.getChildren().add(okButton);
        mainContainer.getChildren().addAll(headerSection, contentSection, buttonSection);
        
        alert.getDialogPane().setContent(mainContainer);
        alert.getDialogPane().getButtonTypes().clear();
        
        // Add entrance animation
        Platform.runLater(() -> {
            mainContainer.setScaleX(0.8);
            mainContainer.setScaleY(0.8);
            mainContainer.setOpacity(0.0);
            
            javafx.animation.Timeline scaleTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                    new javafx.animation.KeyValue(mainContainer.scaleXProperty(), 0.8),
                    new javafx.animation.KeyValue(mainContainer.scaleYProperty(), 0.8),
                    new javafx.animation.KeyValue(mainContainer.opacityProperty(), 0.0)
                ),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                    new javafx.animation.KeyValue(mainContainer.scaleXProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT),
                    new javafx.animation.KeyValue(mainContainer.scaleYProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT),
                    new javafx.animation.KeyValue(mainContainer.opacityProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT)
                )
            );
            scaleTimeline.play();
        });
        
        okButton.setOnAction(e -> alert.close());
        alert.show();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        // Create beautiful custom email input dialog
        Dialog<String> emailDialog = new Dialog<>();
        emailDialog.setTitle("Réinitialiser le mot de passe");
        emailDialog.setHeaderText(null);

        // Ensure dialog can be closed properly
        emailDialog.setResizable(false);

        // Create custom styled content
        VBox mainContainer = new VBox();
        mainContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f8fafc, #ffffff);" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5);" +
            "-fx-border-color: rgba(59,130,246,0.2);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 15;" +
            "-fx-min-width: 400;" +
            "-fx-pref-width: 400;"
        );

        // Header section
        VBox headerSection = new VBox(10);
        headerSection.setPadding(new javafx.geometry.Insets(25));
        headerSection.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-background-radius: 15 15 0 0;");
        headerSection.setAlignment(javafx.geometry.Pos.CENTER);

        Label iconLabel = new Label("🔑");
        iconLabel.setStyle("-fx-font-size: 40px;");

        Label titleLabel = new Label("Mot de passe oublié?");
        titleLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );

        Label subtitleLabel = new Label("Entrez votre email pour réinitialiser");
        subtitleLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: rgba(255,255,255,0.9);" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );

        headerSection.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        // Content section
        VBox contentSection = new VBox(20);
        contentSection.setPadding(new javafx.geometry.Insets(25));

        Label emailLabel = new Label("Adresse email");
        emailLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #374151;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );

        TextField emailField = new TextField();
        emailField.setPromptText("exemple@planova.tn");
        emailField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 15 20;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #d1d5db;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );

        // Focus effect for email field
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                emailField.setStyle(emailField.getStyle().replace("-fx-border-color: #d1d5db;", "-fx-border-color: #667eea;"));
            } else {
                emailField.setStyle(emailField.getStyle().replace("-fx-border-color: #667eea;", "-fx-border-color: #d1d5db;"));
            }
        });

        // Info section
        VBox infoContainer = new VBox(8);
        infoContainer.setStyle(
            "-fx-background-color: rgba(16,185,129,0.1);" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 15;" +
            "-fx-border-color: rgba(16,185,129,0.3);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;"
        );

        Label infoLabel = new Label("ℹ️ Information importante");
        infoLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #059669;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );

        Label infoText = new Label("Si votre compte a l'authentification à double facteur activée, vous devrez vérifier votre identité avant de pouvoir réinitialiser votre mot de passe.");
        infoText.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #047857;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;" +
            "-fx-wrap-text: true;"
        );
        infoText.setWrapText(true);

        infoContainer.getChildren().addAll(infoLabel, infoText);
        contentSection.getChildren().addAll(emailLabel, emailField, infoContainer);

        // Button section
        javafx.scene.layout.HBox buttonSection = new javafx.scene.layout.HBox(15);
        buttonSection.setPadding(new javafx.geometry.Insets(0, 25, 25, 25));
        buttonSection.setAlignment(javafx.geometry.Pos.CENTER);

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #9ca3af;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-text-fill: #6b7280;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 12 25;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;" +
            "-fx-background-radius: 10;"
        );

        Button continueButton = new Button("🚀 Continuer");
        continueButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 12 25;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(103,126,234,0.3), 8, 0, 0, 3);"
        );

        // Button hover effects
        cancelButton.setOnMouseEntered(e -> {
            cancelButton.setStyle(cancelButton.getStyle().replace("-fx-border-color: #9ca3af;", "-fx-border-color: #667eea;")
                                                              .replace("-fx-text-fill: #6b7280;", "-fx-text-fill: #667eea;"));
        });
        cancelButton.setOnMouseExited(e -> {
            cancelButton.setStyle(cancelButton.getStyle().replace("-fx-border-color: #667eea;", "-fx-border-color: #9ca3af;")
                                                              .replace("-fx-text-fill: #667eea;", "-fx-text-fill: #6b7280;"));
        });

        continueButton.setOnMouseEntered(e -> {
            continueButton.setStyle(continueButton.getStyle().replace(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);",
                "-fx-background-color: linear-gradient(to right, #5a6fd8, #6b46a3);"
            ));
        });
        continueButton.setOnMouseExited(e -> {
            continueButton.setStyle(continueButton.getStyle().replace(
                "-fx-background-color: linear-gradient(to right, #5a6fd8, #6b46a3);",
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);"
            ));
        });

        buttonSection.getChildren().addAll(cancelButton, continueButton);
        mainContainer.getChildren().addAll(headerSection, contentSection, buttonSection);

        emailDialog.getDialogPane().setContent(mainContainer);
        emailDialog.getDialogPane().getButtonTypes().clear(); // Remove default buttons

        // Add entrance animation
        Platform.runLater(() -> {
            mainContainer.setScaleX(0.8);
            mainContainer.setScaleY(0.8);
            mainContainer.setOpacity(0.0);

            javafx.animation.Timeline scaleTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                    new javafx.animation.KeyValue(mainContainer.scaleXProperty(), 0.8),
                    new javafx.animation.KeyValue(mainContainer.scaleYProperty(), 0.8),
                    new javafx.animation.KeyValue(mainContainer.opacityProperty(), 0.0)
                ),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                    new javafx.animation.KeyValue(mainContainer.scaleXProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT),
                    new javafx.animation.KeyValue(mainContainer.scaleYProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT),
                    new javafx.animation.KeyValue(mainContainer.opacityProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT)
                )
            );
            scaleTimeline.play();
        });

        // Button actions
        cancelButton.setOnAction(e -> {
            // Force close by accessing the underlying Stage
            try {
                Stage stage = (Stage) emailDialog.getDialogPane().getScene().getWindow();
                stage.close();
                returnToLoginInterface();
            } catch (Exception ex) {
                // Fallback to normal close
                emailDialog.setResult(null);
                emailDialog.close();
                returnToLoginInterface();
            }
        });

        continueButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showStyledAlert("❌ Erreur", "Veuillez entrer une adresse email.", "error");
                return;
            }

            emailDialog.close();

            try {
                // First, check if the user exists and has 2FA enabled
                User user = serviceUser.findByEmail(email);
                if (user == null) {
                    showStyledAlert("❌ Compte non trouvé", "Aucun compte trouvé avec cet email.", "error");
                    return;
                }

                // Check if user has 2FA enabled
                if (user.isTwoFactorEnabled() && (user.getFaceModelData() != null || user.isTotpEnabled())) {
                    // Show 2FA verification before allowing password reset
                    show2FAVerificationForPasswordReset(user, email);
                } else {
                    // No 2FA, proceed with direct password reset
                    showPasswordResetDialog(email);
                }

            } catch (SQLException ex) {
                showStyledAlert("❌ Erreur", "Erreur lors de la vérification du compte: " + ex.getMessage(), "error");
            }
        });

        emailDialog.show();

        // Focus on email field
        Platform.runLater(() -> emailField.requestFocus());
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

    private void navigateToExplore(User user) {
        try {
            // Navigate to explore interface for clients
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/explore.fxml"));

            if (loader.getLocation() == null) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to find explore file (ui/explore.fxml)");
                return;
            }

            Parent root = loader.load();

            // Get the controller and pass user info
            ExploreController controller = loader.getController();
            if (controller == null) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load explore controller");
                return;
            }

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - Explore");

            // Allow full screen and resizing for better visibility
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setMinWidth(1100);
            stage.setMinHeight(700);

            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open explore interface: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
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

            // Allow full screen and resizing for better visibility
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setMinWidth(1100);
            stage.setMinHeight(700);

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

    @FXML
    private void handleFacebookLogin(ActionEvent event) {
        try {
            // Check if social auth service is available
            if (socialAuthService == null) {
                showAlert(AlertType.ERROR, "Service Indisponible",
                         "Le service d'authentification Facebook n'a pas pu démarrer.\n" +
                         "Ceci est généralement dû à un conflit de port 8080.\n\n" +
                         "Solutions:\n" +
                         "1. Fermez toute application utilisant le port 8080\n" +
                         "2. Ou redémarrez votre ordinateur\n" +
                         "3. Puis redémarrez cette application");
                return;
            }

            // Check if callback server is running
            if (!socialAuthService.isAvailable()) {
                showAlert(AlertType.ERROR, "Serveur Indisponible",
                         "Le serveur OAuth n'est pas disponible.\n" +
                         "Port 8080 est probablement occupé par une autre application.\n\n" +
                         "Solutions:\n" +
                         "1. Fermez toute application utilisant le port 8080\n" +
                         "2. Redémarrez l'application\n" +
                         "3. Ou redémarrez votre ordinateur");
                return;
            }

            // Disable button to prevent multiple clicks
            facebookBtn.setDisable(true);
            facebookBtn.setText("Connexion...");

            // Start ngrok OAuth flow
            System.out.println("🔗 Starting Facebook OAuth flow...");
            socialAuthService.startOAuth("facebook")
                .thenAccept(result -> {
                    System.out.println("📨 OAuth callback received - Success: " + result.success + ", Error: " + result.error + ", Code present: " + (result.code != null));
                    Platform.runLater(() -> {
                        try {
                            if (result.success) {
                                System.out.println("✅ OAuth success, processing authorization code...");
                                // Get user info from Facebook using the authorization code
                                processFacebookOAuthSuccess(result.code);
                            } else if ("timeout".equals(result.error)) {
                                System.out.println("⏰ OAuth timeout...");
                                showAlert(AlertType.WARNING, "Délai d'Attente",
                                         "Délai d'attente dépassé. Assurez-vous que:\n" +
                                         "1. ngrok est en cours d'exécution avec: ngrok http 8080 --scheme=https\n" +
                                         "2. Vous avez terminé l'authentification Facebook\n" +
                                         "3. L'URL de redirection ngrok est ajoutée à votre app Facebook");
                            } else if (result.error.contains("ngrok n'est pas détecté")) {
                                showAlert(AlertType.ERROR, "ngrok Non Détecté",
                                         "ngrok n'est pas en cours d'exécution.\n\n" +
                                         "1. Ouvrez un terminal/invite de commandes\n" +
                                         "2. Exécutez: ngrok http 8080 --scheme=https\n" +
                                         "3. Ajoutez l'URL HTTPS ngrok aux redirect URIs Facebook\n" +
                                         "4. Réessayez la connexion Facebook");
                                showAlternativeFacebookLogin();
                            } else {
                                System.out.println("❌ OAuth failed: " + result.error);
                                showAlert(AlertType.ERROR, "Erreur Facebook",
                                         "Erreur d'authentification: " + result.error);
                            }
                        } catch (Exception e) {
                            System.out.println("💥 Exception in OAuth callback: " + e.getMessage());
                            showAlert(AlertType.ERROR, "Erreur",
                                     "Erreur lors du traitement de la réponse Facebook: " + e.getMessage());
                        } finally {
                            // Re-enable button
                            facebookBtn.setDisable(false);
                            facebookBtn.setText("");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showAlert(AlertType.ERROR, "Erreur",
                                 "Erreur lors de l'authentification Facebook: " + throwable.getMessage());
                        // Re-enable button
                        facebookBtn.setDisable(false);
                        facebookBtn.setText("");
                    });
                    return null;
                });

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur Facebook",
                     "Erreur lors de l'ouverture de l'authentification Facebook: " + e.getMessage());
            // Re-enable button
            facebookBtn.setDisable(false);
            facebookBtn.setText("");
        }
    }

    private void showManualOAuthCodeInput() {
        // Create a comprehensive dialog to help with HTTPS certificate issues
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Facebook OAuth - Problème HTTPS");
        alert.setHeaderText("Facebook exige HTTPS - Problème de certificat");

        String serverInfo = socialAuthService != null ? socialAuthService.getServerInfo() : "Service non disponible";

        alert.setContentText("Facebook n'accepte que les redirections HTTPS, mais votre navigateur bloque le certificat auto-signé.\n\n" +
                           "SOLUTIONS:\n\n" +
                           "1. ACCEPTER LE CERTIFICAT:\n" +
                           "   • Ouvrez le lien HTTPS ngrok dans votre navigateur\n" +
                           "   • Cliquez 'Avancé' puis 'Continuer'\n" +
                           "   • Puis réessayez l'authentification Facebook\n\n" +
                           "2. MÉTHODE ALTERNATIVE:\n" +
                           "   • Utiliser l'authentification manuelle avec le code\n\n" +
                           "Info serveur: " + serverInfo + "\n\n" +
                           "Que voulez-vous faire?");

        ButtonType acceptCertButton = new ButtonType("Accepter le Certificat", ButtonBar.ButtonData.OK_DONE);
        ButtonType manualButton = new ButtonType("Méthode Manuelle", ButtonBar.ButtonData.APPLY);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(acceptCertButton, manualButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == acceptCertButton) {
                // Open HTTPS URL to accept certificate
                try {
                    String redirectUri = socialAuthService != null ? socialAuthService.getRedirectUri() : null;
                    if (redirectUri != null) {
                        // Extract the base ngrok URL (without the callback path)
                        String baseUrl = redirectUri.substring(0, redirectUri.lastIndexOf("/auth"));
                        Desktop.getDesktop().browse(new URI(baseUrl));
                        showAlert(AlertType.INFORMATION, "Acceptation du Certificat",
                                 "1. Acceptez le certificat dans la nouvelle fenêtre\n" +
                                 "2. Fermez cette fenêtre\n" +
                                 "3. Cliquez à nouveau sur le bouton Facebook");
                    } else {
                        showAlert(AlertType.ERROR, "Erreur", "URL ngrok non disponible.");
                    }
                } catch (Exception e) {
                    showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir le lien HTTPS: " + e.getMessage());
                }
            } else if (result.get() == manualButton) {
                // Show manual method
                showAlternativeFacebookLogin();
            }
        }
    }

    private void showAlternativeFacebookLogin() {
        // Check if ngrok is detected automatically
        if (socialAuthService != null && socialAuthService.isAvailable()) {
            // Use the automatically detected ngrok URL
            String redirectUri = socialAuthService.getRedirectUri();
            String facebookUrl = String.format(
                "https://www.facebook.com/v18.0/dialog/oauth?client_id=%s&redirect_uri=%s&scope=public_profile,email&response_type=code&state=facebook_auth",
                "925022893757906",
                java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8)
            );

            // Create a dialog with manual steps
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Facebook Login - Méthode Alternative");
            dialog.setHeaderText("Authentification Facebook manuelle");

            // Create the text area for instructions
            TextArea instructions = new TextArea();
            instructions.setEditable(false);
            instructions.setPrefRowCount(8);
            instructions.setPrefColumnCount(60);
            instructions.setText(
                "ÉTAPES MANUELLES:\n\n" +
                "1. Le lien Facebook va s'ouvrir dans votre navigateur\n" +
                "2. Connectez-vous avec votre compte Facebook\n" +
                "3. Autorisez l'application\n" +
                "4. IMPORTANT: La page va rediriger vers l'URL ngrok\n" +
                "5. Vous verrez peut-être une erreur de certificat - C'EST NORMAL!\n" +
                "6. Dans la barre d'adresse, copiez SEULEMENT la partie après 'code='\n" +
                "7. Collez ce code dans le champ ci-dessous:\n"
            );

            // Create clickable URL
            TextField urlField = new TextField();
            urlField.setText(facebookUrl);
            urlField.setEditable(false);

            Button openUrlButton = new Button("🌐 Ouvrir Facebook Login");
            openUrlButton.setOnAction(e -> {
                try {
                    Desktop.getDesktop().browse(new URI(urlField.getText()));
                } catch (Exception ex) {
                    showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir le lien: " + ex.getMessage());
                }
            });

            TextField codeField = new TextField();
            codeField.setPromptText("Collez le code d'autorisation Facebook ici...");

            VBox content = new VBox(10);
            content.getChildren().addAll(
                instructions,
                new Label("🔗 URL Facebook (cliquez le bouton pour ouvrir):"),
                urlField,
                openUrlButton,
                new Label("📋 Code d'autorisation Facebook:"),
                codeField
            );

            dialog.getDialogPane().setContent(content);

            ButtonType submitButton = new ButtonType("🔐 Se Connecter", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == submitButton) {
                    return codeField.getText().trim();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().isEmpty()) {
                String authCode = result.get();
                // Clean the code - remove any extra parameters
                if (authCode.contains("&")) {
                    authCode = authCode.substring(0, authCode.indexOf("&"));
                }
                if (authCode.contains("#")) {
                    authCode = authCode.substring(0, authCode.indexOf("#"));
                }

                System.out.println("📝 Manual Facebook code entered: " + authCode.substring(0, Math.min(10, authCode.length())) + "...");
                processFacebookOAuthSuccess(authCode);
            }
        } else {
            showAlert(AlertType.ERROR, "Service Indisponible",
                     "Le service d'authentification n'est pas disponible.\n" +
                     "Assurez-vous que ngrok est en cours d'exécution.");
        }
    }

    private void processFacebookOAuthSuccess(String authorizationCode) {
        System.out.println("🚀 Processing Facebook OAuth success with code: " +
                          (authorizationCode != null ? authorizationCode.substring(0, Math.min(10, authorizationCode.length())) + "..." : "null"));

        try {
            // Exchange authorization code for access token and get user info
            Client client = socialAuthService.authenticateWithFacebookCode(authorizationCode);

            if (client != null) {
                System.out.println("✅ Client authentication successful: " + client.getEmail());

                // Store user session
                UserSession.getInstance().setCurrentUser(client);

                // Show success message
                showAlert(AlertType.INFORMATION, "Connexion Facebook Réussie",
                         "Bienvenue " + client.getPrenom() + " " + client.getNom() + "!\n" +
                         "Connexion via Facebook établie avec succès.");

                // Check if client has 2FA enabled
                if (client.isTwoFactorEnabled()) {
                    // Check which 2FA methods are available
                    boolean hasFaceId = client.getFaceModelData() != null;
                    boolean hasTotp = client.isTotpEnabled();

                    if (hasFaceId || hasTotp) {
                        // Show 2FA verification dialog for client
                        show2FAVerification(client);
                    } else {
                        // 2FA is enabled but no methods configured - proceed with normal login
                        proceedWithLogin(client);
                    }
                } else {
                    // No 2FA, proceed directly to explore interface
                    proceedWithLogin(client);
                }

            } else {
                System.out.println("❌ Client is null after authentication");
                showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer les informations utilisateur de Facebook.");
            }

        } catch (SQLException e) {
            System.out.println("💥 SQL Exception: " + e.getMessage());
            if (e.getMessage().contains("COMPTE_BLOQUE")) {
                showAlert(AlertType.ERROR, "Compte Bloqué",
                         "Votre compte a été bloqué par l'administrateur. Veuillez contacter le support.");
            } else {
                showAlert(AlertType.ERROR, "Erreur Base de Données",
                         "Erreur lors de l'accès à la base de données: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("💥 General Exception: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        try {
            // Check if social auth service is available
            if (socialAuthService == null) {
                showAlert(AlertType.ERROR, "Service Indisponible",
                         "Le service d'authentification Google n'a pas pu démarrer.\n" +
                         "Ceci est généralement dû à un conflit de port 8080.\n\n" +
                         "Solutions:\n" +
                         "1. Fermez toute application utilisant le port 8080\n" +
                         "2. Ou redémarrez votre ordinateur\n" +
                         "3. Puis redémarrez cette application");
                return;
            }

            // Check if callback server is running
            if (!socialAuthService.isAvailable()) {
                showAlert(AlertType.ERROR, "Serveur Indisponible",
                         "Le serveur OAuth n'est pas disponible.\n" +
                         "Port 8080 est probablement occupé par une autre application.\n\n" +
                         "Solutions:\n" +
                         "1. Fermez toute application utilisant le port 8080\n" +
                         "2. Redémarrez l'application\n" +
                         "3. Ou redémarrez votre ordinateur");
                return;
            }

            // Disable button to prevent multiple clicks
            googleBtn.setDisable(true);
            googleBtn.setText("Connexion...");

            // Start ngrok OAuth flow for Google
            System.out.println("🔗 Starting Google OAuth flow...");
            socialAuthService.startOAuth("google")
                .thenAccept(result -> {
                    System.out.println("📨 Google OAuth callback received - Success: " + result.success + ", Error: " + result.error + ", Code present: " + (result.code != null));
                    Platform.runLater(() -> {
                        try {
                            if (result.success) {
                                System.out.println("✅ Google OAuth success, processing authorization code...");
                                // Get user info from Google using the authorization code
                                processGoogleOAuthSuccess(result.code);
                            } else if ("timeout".equals(result.error)) {
                                System.out.println("⏰ Google OAuth timeout...");
                                showAlert(AlertType.WARNING, "Délai d'Attente",
                                         "Délai d'attente dépassé. Assurez-vous que:\n" +
                                         "1. ngrok est en cours d'exécution avec: ngrok http 8080 --scheme=https\n" +
                                         "2. Vous avez terminé l'authentification Google\n" +
                                         "3. L'URL de redirection ngrok est ajoutée à votre projet Google Cloud");
                            } else if (result.error.contains("ngrok n'est pas détecté")) {
                                showAlert(AlertType.ERROR, "ngrok Non Détecté",
                                         "ngrok n'est pas en cours d'exécution.\n\n" +
                                         "1. Ouvrez un terminal/invite de commandes\n" +
                                         "2. Exécutez: ngrok http 8080 --scheme=https\n" +
                                         "3. Ajoutez l'URL HTTPS ngrok aux redirect URIs Google Cloud\n" +
                                         "4. Réessayez la connexion Google");
                                showAlternativeGoogleLogin();
                            } else {
                                System.out.println("❌ Google OAuth failed: " + result.error);
                                showAlert(AlertType.ERROR, "Erreur Google",
                                         "Erreur d'authentification: " + result.error);
                            }
                        } catch (Exception e) {
                            System.out.println("💥 Exception in Google OAuth callback: " + e.getMessage());
                            showAlert(AlertType.ERROR, "Erreur",
                                     "Erreur lors du traitement de la réponse Google: " + e.getMessage());
                        } finally {
                            // Re-enable button
                            googleBtn.setDisable(false);
                            googleBtn.setText("");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        showAlert(AlertType.ERROR, "Erreur",
                                 "Erreur lors de l'authentification Google: " + throwable.getMessage());
                        // Re-enable button
                        googleBtn.setDisable(false);
                        googleBtn.setText("");
                    });
                    return null;
                });

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur Google",
                     "Erreur lors de l'ouverture de l'authentification Google: " + e.getMessage());
            // Re-enable button
            googleBtn.setDisable(false);
            googleBtn.setText("");
        }
    }

    private void processGoogleOAuthSuccess(String authorizationCode) {
        System.out.println("🚀 Processing Google OAuth success with code: " +
                          (authorizationCode != null ? authorizationCode.substring(0, Math.min(10, authorizationCode.length())) + "..." : "null"));

        try {
            // Exchange authorization code for access token and get user info
            Client client = socialAuthService.authenticateWithGoogleCode(authorizationCode);

            if (client != null) {
                System.out.println("✅ Google Client authentication successful: " + client.getEmail());

                // Store user session
                UserSession.getInstance().setCurrentUser(client);

                // Show success message
                showAlert(AlertType.INFORMATION, "Connexion Google Réussie",
                         "Bienvenue " + client.getPrenom() + " " + client.getNom() + "!\n" +
                         "Connexion via Google établie avec succès.");

                // Check if client has 2FA enabled
                if (client.isTwoFactorEnabled()) {
                    // Check which 2FA methods are available
                    boolean hasFaceId = client.getFaceModelData() != null;
                    boolean hasTotp = client.isTotpEnabled();

                    if (hasFaceId || hasTotp) {
                        // Show 2FA verification dialog for client
                        show2FAVerification(client);
                    } else {
                        // 2FA is enabled but no methods configured - proceed with normal login
                        proceedWithLogin(client);
                    }
                } else {
                    // No 2FA, proceed directly to explore interface
                    proceedWithLogin(client);
                }
            } else {
                System.out.println("❌ Google Client is null after authentication");
                showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer les informations utilisateur de Google.");
            }

        } catch (SQLException e) {
            System.out.println("💥 SQL Exception: " + e.getMessage());
            if (e.getMessage().contains("COMPTE_BLOQUE")) {
                showAlert(AlertType.ERROR, "Compte Bloqué",
                         "Votre compte a été bloqué par l'administrateur. Veuillez contacter le support.");
            } else {
                showAlert(AlertType.ERROR, "Erreur Base de Données",
                         "Erreur lors de l'accès à la base de données: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("💥 General Exception: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur Google",
                     "Erreur lors de l'authentification Google: " + e.getMessage());
        }
    }

    private void showAlternativeGoogleLogin() {
        // Create manual Google login dialog similar to Facebook
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Authentification Google Manuelle");
        dialog.setHeaderText("Saisie manuelle du code d'autorisation Google");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextArea instructions = new TextArea();
        instructions.setEditable(false);
        instructions.setPrefRowCount(8);
        instructions.setPrefColumnCount(60);
        instructions.setText(
            "ÉTAPES MANUELLES:\n\n" +
            "1. Le lien Google va s'ouvrir dans votre navigateur\n" +
            "2. Connectez-vous avec votre compte Google\n" +
            "3. Autorisez l'application\n" +
            "4. IMPORTANT: La page va rediriger vers l'URL ngrok\n" +
            "5. Vous verrez peut-être une erreur de certificat - C'EST NORMAL!\n" +
            "6. Dans la barre d'adresse, copiez SEULEMENT la partie après 'code='\n" +
            "7. Collez ce code dans le champ ci-dessous:\n"
        );

        // Create clickable URL
        TextField urlField = new TextField();
        urlField.setEditable(false);
        urlField.setText("Cliquez sur le bouton ci-dessous pour ouvrir Google OAuth");

        Button openUrlBtn = new Button("🌐 Ouvrir l'authentification Google");
        openUrlBtn.setOnAction(e -> {
            try {
                socialAuthService.openGoogleAuth();
                urlField.setText("✅ Google OAuth ouvert - suivez les étapes ci-dessus");
            } catch (Exception ex) {
                urlField.setText("❌ Erreur: " + ex.getMessage());
            }
        });

        TextField codeField = new TextField();
        codeField.setPromptText("Collez le code d'autorisation Google ici...");

        content.getChildren().addAll(
            new Label("Instructions:"), instructions,
            new Label("Authentification Google:"), openUrlBtn, urlField,
            new Label("Code d'autorisation:"), codeField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return codeField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(authCode -> {
            if (authCode != null && !authCode.trim().isEmpty()) {
                processGoogleOAuthSuccess(authCode.trim());
            }
        });
    }

    /**
     * Show 2FA verification dialog with support for both Face ID and TOTP
     */
    private void show2FAVerification(User user) {
        try {
            // Load unified 2FA verification dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/twoFactorVerification.fxml"));
            Parent root = loader.load();

            // Get the controller
            TwoFactorVerificationController controller = loader.getController();
            controller.setUserToVerify(user);

            // Set callbacks
            controller.setCallbacks(
                // Success callback
                () -> {
                    Platform.runLater(() -> proceedWithLogin(user));
                },
                // Failure callback
                () -> {
                    Platform.runLater(() -> {
                        UserSession.getInstance().logout();
                        showAlert(Alert.AlertType.WARNING, "Vérification 2FA échouée",
                                "La vérification d'authentification à double facteur a échoué ou a été annulée.\n" +
                                "Veuillez réessayer ou contactez votre administrateur.");
                    });
                }
            );

            // Create and show modal window
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Vérification 2FA - PlaNova");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Center on parent window
            Stage parentStage = (Stage) emailField.getScene().getWindow();
            stage.initOwner(parentStage);

            // Add icon
            try {
                stage.getIcons().add(new Image("/logo.PNG"));
            } catch (Exception e) {
                // Icon loading failed, continue without icon
            }

            stage.show();

        } catch (IOException e) {
            // If 2FA dialog fails to load, proceed without 2FA verification
            System.err.println("Failed to load 2FA verification dialog: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur 2FA",
                    "Impossible de charger l'interface de vérification 2FA.\n" +
                    "Connexion en mode normal.");
            proceedWithLogin(user);
        }
    }

    /**
     * Show 2FA verification dialog for password reset
     */
    private void show2FAVerificationForPasswordReset(User user, String email) {
        try {
            // Load unified 2FA verification dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/twoFactorVerification.fxml"));
            Parent root = loader.load();

            // Get the controller
            TwoFactorVerificationController controller = loader.getController();
            controller.setUserToVerify(user);

            // Set callbacks
            controller.setCallbacks(
                // Success callback - proceed with password reset
                () -> {
                    Platform.runLater(() -> {
                        showPasswordResetDialog(email);
                    });
                },
                // Failure callback
                () -> {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.WARNING, "Vérification 2FA échouée",
                                "La vérification d'authentification à double facteur a échoué.\n" +
                                "Vous devez vérifier votre identité pour réinitialiser votre mot de passe.");
                    });
                }
            );

            // Create and show modal window
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Vérification 2FA - PlaNova");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Center on parent window
            Stage parentStage = (Stage) emailField.getScene().getWindow();
            stage.initOwner(parentStage);

            // Add icon
            try {
                stage.getIcons().add(new Image("/logo.PNG"));
            } catch (Exception e) {
                // Icon loading failed, continue without icon
            }

            stage.show();

        } catch (IOException e) {
            // If 2FA dialog fails to load, show error message
            System.err.println("Failed to load 2FA verification dialog for password reset: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur 2FA",
                    "Impossible de charger l'interface de vérification 2FA.\n" +
                    "Veuillez contacter l'administrateur.");
        }
    }

    /**
     * Show password reset dialog after successful verification
     */
    private void showPasswordResetDialog(String email) {
        Dialog<String> passDialog = new Dialog<>();
        passDialog.setTitle("Réinitialiser le mot de passe");
        passDialog.setHeaderText(null);
        
        // Ensure dialog can be closed properly
        passDialog.setResizable(false);

        // Create custom styled content
        VBox mainContainer = new VBox();
        mainContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f8fafc, #ffffff);" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5);" +
            "-fx-border-color: rgba(59,130,246,0.2);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 15;" +
            "-fx-min-width: 450;" +
            "-fx-pref-width: 450;"
        );
        
        // Header section
        VBox headerSection = new VBox(10);
        headerSection.setPadding(new javafx.geometry.Insets(25, 25, 20, 25));
        headerSection.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-background-radius: 15 15 0 0;");
        
        Label titleLabel = new Label("🔐 Nouveau Mot de Passe");
        titleLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );
        
        Label subtitleLabel = new Label("pour " + email);
        subtitleLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: rgba(255,255,255,0.9);" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );
        
        headerSection.getChildren().addAll(titleLabel, subtitleLabel);
        
        // Content section
        VBox contentSection = new VBox(20);
        contentSection.setPadding(new javafx.geometry.Insets(25));
        
        // Password fields with beautiful styling
        VBox passwordContainer = new VBox(15);
        
        Label newPassLabel = new Label("Nouveau mot de passe");
        newPassLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #374151;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );
        
        javafx.scene.control.PasswordField newPass = new javafx.scene.control.PasswordField();
        newPass.setPromptText("Entrez votre nouveau mot de passe");
        newPass.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 15 20;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #d1d5db;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );
        
        // Focus effect for new password field
        newPass.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                newPass.setStyle(newPass.getStyle().replace("-fx-border-color: #d1d5db;", "-fx-border-color: #667eea;"));
            } else {
                newPass.setStyle(newPass.getStyle().replace("-fx-border-color: #667eea;", "-fx-border-color: #d1d5db;"));
            }
        });
        
        Label confirmPassLabel = new Label("Confirmer le mot de passe");
        confirmPassLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #374151;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );
        
        javafx.scene.control.PasswordField confirmPass = new javafx.scene.control.PasswordField();
        confirmPass.setPromptText("Confirmez votre nouveau mot de passe");
        confirmPass.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 15 20;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #d1d5db;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );
        
        // Focus effect for confirm password field
        confirmPass.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                confirmPass.setStyle(confirmPass.getStyle().replace("-fx-border-color: #d1d5db;", "-fx-border-color: #667eea;"));
            } else {
                confirmPass.setStyle(confirmPass.getStyle().replace("-fx-border-color: #667eea;", "-fx-border-color: #d1d5db;"));
            }
        });
        
        passwordContainer.getChildren().addAll(newPassLabel, newPass, confirmPassLabel, confirmPass);
        
        // Requirements section with beautiful styling
        VBox requirementsContainer = new VBox(10);
        requirementsContainer.setStyle(
            "-fx-background-color: rgba(103,126,234,0.05);" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 20;" +
            "-fx-border-color: rgba(103,126,234,0.2);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;"
        );
        
        Label requirementsTitle = new Label("📋 Exigences du mot de passe");
        requirementsTitle.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #667eea;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
        );
        
        VBox requirementsList = new VBox(8);
        String[] requirements = {
            "• Au moins 8 caractères",
            "• Une lettre majuscule",
            "• Un chiffre",
            "• Un caractère spécial (!@#$%^&*)"
        };
        
        for (String req : requirements) {
            Label reqLabel = new Label(req);
            reqLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #6b7280;" +
                "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;"
            );
            requirementsList.getChildren().add(reqLabel);
        }
        
        requirementsContainer.getChildren().addAll(requirementsTitle, requirementsList);
        contentSection.getChildren().addAll(passwordContainer, requirementsContainer);
        
        // Button section
        javafx.scene.layout.HBox buttonSection = new javafx.scene.layout.HBox(15);
        buttonSection.setPadding(new javafx.geometry.Insets(0, 25, 25, 25));
        buttonSection.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #9ca3af;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-text-fill: #6b7280;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 12 30;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;" +
            "-fx-background-radius: 10;"
        );
        
        Button confirmButton = new Button("✅ Confirmer");
        confirmButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 12 30;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Inter', 'Segoe UI', sans-serif;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(103,126,234,0.3), 8, 0, 0, 3);"
        );
        
        // Button hover effects
        cancelButton.setOnMouseEntered(e -> {
            cancelButton.setStyle(cancelButton.getStyle().replace("-fx-border-color: #9ca3af;", "-fx-border-color: #667eea;")
                                                              .replace("-fx-text-fill: #6b7280;", "-fx-text-fill: #667eea;"));
        });
        cancelButton.setOnMouseExited(e -> {
            cancelButton.setStyle(cancelButton.getStyle().replace("-fx-border-color: #667eea;", "-fx-border-color: #9ca3af;")
                                                              .replace("-fx-text-fill: #667eea;", "-fx-text-fill: #6b7280;"));
        });
        
        confirmButton.setOnMouseEntered(e -> {
            confirmButton.setStyle(confirmButton.getStyle().replace(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);",
                "-fx-background-color: linear-gradient(to right, #5a6fd8, #6b46a3);"
            ));
        });
        confirmButton.setOnMouseExited(e -> {
            confirmButton.setStyle(confirmButton.getStyle().replace(
                "-fx-background-color: linear-gradient(to right, #5a6fd8, #6b46a3);",
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);"
            ));
        });
        
        buttonSection.getChildren().addAll(cancelButton, confirmButton);
        
        mainContainer.getChildren().addAll(headerSection, contentSection, buttonSection);
        
        passDialog.getDialogPane().setContent(mainContainer);
        passDialog.getDialogPane().getButtonTypes().clear(); // Remove default buttons
        
        // Add entrance animation
        Platform.runLater(() -> {
            mainContainer.setScaleX(0.8);
            mainContainer.setScaleY(0.8);
            mainContainer.setOpacity(0.0);
            
            javafx.animation.Timeline scaleTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                    new javafx.animation.KeyValue(mainContainer.scaleXProperty(), 0.8),
                    new javafx.animation.KeyValue(mainContainer.scaleYProperty(), 0.8),
                    new javafx.animation.KeyValue(mainContainer.opacityProperty(), 0.0)
                ),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                    new javafx.animation.KeyValue(mainContainer.scaleXProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT),
                    new javafx.animation.KeyValue(mainContainer.scaleYProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT),
                    new javafx.animation.KeyValue(mainContainer.opacityProperty(), 1.0, javafx.animation.Interpolator.EASE_OUT)
                )
            );
            scaleTimeline.play();
        });
        
        // Button actions
        cancelButton.setOnAction(e -> {
            // Force close by accessing the underlying Stage
            try {
                Stage stage = (Stage) passDialog.getDialogPane().getScene().getWindow();
                stage.close();
                returnToLoginInterface();
            } catch (Exception ex) {
                // Fallback to normal close
                passDialog.setResult(null);
                passDialog.close();
                returnToLoginInterface();
            }
        });

        confirmButton.setOnAction(e -> {
            String password = newPass.getText();
            String confirm = confirmPass.getText();
            
            // Validate inputs without blocking the interface
            if (password == null || password.trim().isEmpty()) {
                showNonBlockingAlert("❌ Erreur", "Veuillez entrer un mot de passe.", "error");
                newPass.requestFocus();
                return;
            }
            
            if (!password.equals(confirm)) {
                showNonBlockingAlert("❌ Erreur", "Les mots de passe ne correspondent pas.", "error");
                confirmPass.requestFocus();
                return;
            }
            
            if (!isPasswordStrong(password)) {
                showNonBlockingAlert("⚠️ Mot de passe faible",
                             "Le mot de passe doit contenir au moins 8 caractères avec une majuscule, un chiffre et un caractère spécial.", "warning");
                newPass.requestFocus();
                return;
            }
            
            // If validation passes, proceed with password reset
            try {
                if (serviceUser.resetPasswordByEmail(email, password)) {
                    passDialog.close(); // Close the password dialog first
                    showNonBlockingAlert("✅ Succès",
                             "Mot de passe mis à jour avec succès!\n\nVous pouvez maintenant vous connecter avec votre nouveau mot de passe.", "success");
                } else {
                    showNonBlockingAlert("❌ Échec", "Erreur lors de la mise à jour du mot de passe. Veuillez réessayer.", "error");
                }
            } catch (SQLException ex) {
                showNonBlockingAlert("❌ Erreur", "Échec de la réinitialisation du mot de passe: " + ex.getMessage(), "error");
            }
        });
        
        passDialog.show();
    }

    /**
     * Validate password strength
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasUpper && hasDigit && hasSpecial;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Show non-blocking alert that doesn't interfere with other dialogs
     */
    private void showNonBlockingAlert(String title, String message, String type) {
        // Use a simple, lightweight Alert that actually closes properly
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.NONE); // Non-modal
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert with the appropriate icon
        String icon;
        switch (type) {
            case "success":
                icon = "✅ ";
                break;
            case "error":
                icon = "❌ ";
                break;
            case "warning":
                icon = "⚠️ ";
                break;
            default:
                icon = "ℹ️ ";
        }
        alert.setContentText(icon + message);

        // Ensure the alert closes properly
        alert.showAndWait();
    }

    /**
     * Proceed with login after successful authentication (and 2FA if enabled)
     */
    private void proceedWithLogin(User user) {
        // Navigate based on user type
        if (user instanceof Client) {
            navigateToExplore(user);
        } else {
            // Admin, Moderateur, or Guide - go to dashboard
            navigateToDashboard(user);
        }

        System.out.println("Utilisateur connecté avec succès: " + user.getEmail());
    }

    private void returnToLoginInterface() {
        try {
            // Reset all form fields
            emailField.clear();
            passwordField.clear();
            passwordTextField.clear();
            // Reset visibility toggle
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            passwordToggleBtn.setText("👁");

            // Focus on email field
            Platform.runLater(emailField::requestFocus);

        } catch (Exception e) {
            System.err.println("Erreur lors du retour à l'interface de connexion: " + e.getMessage());
        }
    }
}
