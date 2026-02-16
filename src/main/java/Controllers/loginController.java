package Controllers;

import Models.User;
import Services.ServiceUser;
import utils.PasswordUtils;
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
    private TextField passwordTextField;
    @FXML
    private Button loginBtn;
    @FXML
    private Button signupBtn;
    @FXML
    private Button passwordToggleBtn;
    @FXML
    private javafx.scene.control.Hyperlink forgotPasswordLink;

    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    private void initialize() {
        // Bind password fields and set up visibility toggle
        if (passwordTextField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
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

            String storedPassword = userByEmail.getMotDePasse();
            if (storedPassword == null || storedPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Compte invalide", "Ce compte n'a pas de mot de passe défini. Utilisez \"Mot de passe oublié\" pour en définir un.");
                return;
            }

            if (!PasswordUtils.verifyPassword(password, storedPassword)) {
                showAlert(Alert.AlertType.ERROR, "Connexion échouée", "Mot de passe incorrect. Réessayez ou utilisez \"Mot de passe oublié\".");
                return;
            }

            // Store user in session
            UserSession.getInstance().setCurrentUser(userByEmail);

            // Navigate based on user role
            if (UserSession.getInstance().isClient()) {
                // Clients go to explore interface
                navigateToExplore(userByEmail);
            } else if (UserSession.getInstance().canAccessDashboard()) {
                // Admin and Moderator go to dashboard
                navigateToDashboard(userByEmail);
            } else {
                // Guide and other roles not allowed
                UserSession.getInstance().logout();
                showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Ce type de compte n'a pas encore d'interface dédiée. Seuls Admin, Moderateur et Client peuvent se connecter.");
                return;
            }


        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de données", "Échec de l'authentification : " + e.getMessage());
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
