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
    private Button loginBtn;
    @FXML
    private Button signupBtn;
    @FXML
    private javafx.scene.control.Hyperlink forgotPasswordLink;

    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        if (password != null) password = password.trim();

        if (email.isEmpty() || password == null || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter both email and password.");
            return;
        }

        try {
            User userByEmail = serviceUser.findByEmail(email);

            if (userByEmail == null) {
                showAlert(Alert.AlertType.ERROR, "Connexion échouée", "Email incorrect ou inexistant. Vérifiez l'adresse email.");
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

            // Check if user has access to dashboard
            if (!UserSession.getInstance().canAccessDashboard()) {
                UserSession.getInstance().logout();
                showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Seuls Admin et Moderateur peuvent accéder au dashboard. Les comptes Client et Guide ne sont pas autorisés.");
                return;
            }

            // Successful login - navigate to dashboard
            navigateToDashboard(userByEmail);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de données", "Échec de l'authentification : " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Reset password");
        emailDialog.setHeaderText("Enter your account email");
        emailDialog.setContentText("Email:");
        emailDialog.showAndWait().ifPresent(email -> {
            if (email.trim().isEmpty()) return;
            Dialog<String> passDialog = new Dialog<>();
            passDialog.setTitle("Reset password");
            passDialog.setHeaderText("Enter new password for " + email.trim());
            javafx.scene.control.PasswordField newPass = new javafx.scene.control.PasswordField();
            newPass.setPromptText("New password");
            passDialog.getDialogPane().setContent(newPass);
            passDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            passDialog.setResultConverter(btn -> btn == ButtonType.OK ? newPass.getText() : null);
            passDialog.showAndWait().ifPresent(newPassword -> {
                if (newPassword == null || newPassword.isEmpty()) return;
                try {
                    if (serviceUser.resetPasswordByEmail(email.trim(), newPassword)) {
                        showAlert(Alert.AlertType.INFORMATION, "Password reset", "Password updated. You can now log in with your email and the new password.");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Reset failed", "No account found with that email, or error occurred.");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset password: " + e.getMessage());
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
