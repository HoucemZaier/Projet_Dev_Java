package Controllers;

import Models.User;
import Models.Client;
import Services.ServiceUser;
import Services.SocialAuthService;
import Controllers.ExploreController;
import Controllers.dashboardController;
import utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
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
    @FXML
    private javafx.scene.control.Hyperlink forgotPasswordLink;

    private final ServiceUser serviceUser = new ServiceUser();
    private SocialAuthService socialAuthService;

    @FXML
    private void initialize() {
        // Bind password fields and set up visibility toggle
        if (passwordTextField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        // Initialize social auth service
        try {
            socialAuthService = new SocialAuthService();
        } catch (Exception e) {
            System.err.println("Warning: SocialAuthService could not be initialized. Social login will be disabled.");
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

            // Navigate based on user role
            if (UserSession.getInstance().isClient() || UserSession.getInstance().isGuide()) {
                // Clients and Guides go to explore interface
                navigateToExplore(authenticatedUser);
            } else if (UserSession.getInstance().canAccessDashboard()) {
                // Admin and Moderator go to dashboard
                navigateToDashboard(authenticatedUser);
            } else {
                // Other roles not allowed
                UserSession.getInstance().logout();
                showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Ce type de compte n'a pas d'interface dédiée. Contactez l'administrateur.");
                return;
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

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Réinitialiser le mot de passe");
        emailDialog.setHeaderText("Entrez l'email de votre compte");
        emailDialog.setContentText("Email:");
        emailDialog.showAndWait().ifPresent(email -> {
            if (email.trim().isEmpty()) return;
            Dialog<String> passDialog = new Dialog<>();
            passDialog.setTitle("Réinitialiser le mot de passe");
            passDialog.setHeaderText("Entrez un nouveau mot de passe pour " + email.trim());
            javafx.scene.control.PasswordField newPass = new javafx.scene.control.PasswordField();
            newPass.setPromptText("Nouveau mot de passe");
            passDialog.getDialogPane().setContent(newPass);
            passDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            passDialog.setResultConverter(btn -> btn == ButtonType.OK ? newPass.getText() : null);
            passDialog.showAndWait().ifPresent(newPassword -> {
                if (newPassword == null || newPassword.isEmpty()) return;
                try {
                    if (serviceUser.resetPasswordByEmail(email.trim(), newPassword)) {
                        showAlert(Alert.AlertType.INFORMATION, "Mot de passe réinitialisé", "Mot de passe mis à jour. Vous pouvez maintenant vous connecter avec votre email et le nouveau mot de passe.");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Échec de la réinitialisation", "Aucun compte trouvé avec cet email, ou une erreur s'est produite.");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la réinitialisation du mot de passe: " + e.getMessage());
                }
            });
        });
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
                         "Le service d'authentification sociale n'est pas disponible.");
                return;
            }

            // Check if callback server is running
            if (!socialAuthService.isAvailable()) {
                showAlert(AlertType.ERROR, "Serveur Indisponible",
                         "Le serveur de callback OAuth n'est pas disponible.\n" +
                         "Cela peut être dû à un conflit de port.\n" +
                         "Veuillez redémarrer l'application.");
                return;
            }

            // Disable button to prevent multiple clicks
            facebookBtn.setDisable(true);
            facebookBtn.setText("Connexion...");

            // Start ngrok OAuth flow
            System.out.println("🔗 Starting Facebook OAuth flow via ngrok...");
            socialAuthService.startOAuth("facebook")
                .thenAccept(result -> {
                    System.out.println("📨 OAuth callback received - Success: " + result.success + ", Error: " + result.error + ", Code present: " + (result.code != null));
                    Platform.runLater(() -> {
                        try {
                            if (result.success) {
                                System.out.println("✅ OAuth success via ngrok, processing authorization code...");
                                // Get user info from Facebook using the authorization code
                                processFacebookOAuthSuccess(result.code);
                            } else if ("timeout".equals(result.error)) {
                                System.out.println("⏰ ngrok OAuth timeout, ngrok may not be running...");
                                showAlert(AlertType.WARNING, "ngrok Timeout",
                                         "Délai d'attente dépassé. Assurez-vous que:\n" +
                                         "1. ngrok est en cours d'exécution\n" +
                                         "2. L'URL ngrok est correctement configurée\n" +
                                         "3. L'URL de redirection est ajoutée à votre app Facebook");
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
        alert.setContentText("Facebook n'accepte que les redirections HTTPS, mais votre navigateur bloque le certificat auto-signé.\n\n" +
                           "SOLUTIONS:\n\n" +
                           "1. ACCEPTER LE CERTIFICAT:\n" +
                           "   • Ouvrez https://127.0.0.1:8081 dans votre navigateur\n" +
                           "   • Cliquez 'Avancé' puis 'Continuer vers 127.0.0.1'\n" +
                           "   • Puis réessayez l'authentification Facebook\n\n" +
                           "2. MÉTHODE ALTERNATIVE:\n" +
                           "   • Utiliser l'authentification manuelle avec le code\n\n" +
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
                    Desktop.getDesktop().browse(new URI("https://127.0.0.1:8080"));
                    showAlert(AlertType.INFORMATION, "Acceptation du Certificat",
                             "1. Acceptez le certificat dans la nouvelle fenêtre\n" +
                             "2. Fermez cette fenêtre\n" +
                             "3. Cliquez à nouveau sur le bouton Facebook");
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
            "4. IMPORTANT: La page va rediriger vers https://127.0.0.1:8080/...\n" +
            "5. Vous verrez une erreur de certificat - C'EST NORMAL!\n" +
            "6. Dans la barre d'adresse, copiez SEULEMENT la partie après 'code='\n" +
            "7. Collez ce code dans le champ ci-dessous:\n"
        );

        // Create clickable URL
        TextField urlField = new TextField();
        String facebookUrl = "https://www.facebook.com/v18.0/dialog/oauth?client_id=925022893757906&redirect_uri=https%3A%2F%2F127.0.0.1%3A8080%2Fauth%2Ffacebook%2Fcallback&scope=public_profile%2Cemail&response_type=code&state=facebook_auth";
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

                // Navigate to explore interface
                navigateToExplore(client);

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
        // Google authentication not implemented in ngrok-only solution
        showAlert(AlertType.INFORMATION, "Google Login", 
                 "L'authentification Google n'est pas encore implémentée.\n" +
                 "Veuillez utiliser l'authentification Facebook ou créer un compte.");
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
