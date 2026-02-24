package Controllers;

import Models.Client;
import Models.Guide;
import Models.User;
import Services.ServiceUser;
import utils.UserSession;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class GuideRequirementsController implements Initializable {

    @FXML private Button confirmBtn;
    @FXML private Button cancelBtn;

    private User currentUser;
    private ServiceUser serviceUser = new ServiceUser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = UserSession.getInstance().getCurrentUser();

        // Add hover animations to buttons
        addButtonAnimations(confirmBtn);
        addButtonAnimations(cancelBtn);
    }

    private void addButtonAnimations(Button button) {
        // Scale animation on hover
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> {
            scaleOut.stop();
            scaleIn.play();
        });

        button.setOnMouseExited(e -> {
            scaleIn.stop();
            scaleOut.play();
        });
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        try {
            // Validate that current user is a client
            if (!(currentUser instanceof Client)) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Seuls les clients peuvent devenir des guides.");
                return;
            }

            // Create new Guide from current Client
            Client client = (Client) currentUser;
            Guide newGuide = new Guide(
                client.getNom(),
                client.getPrenom(),
                client.getEmail(),
                client.getMotDePasse(),
                client.getPays(),
                client.getImageurl()
            );

            // Set the ID to maintain data integrity
            newGuide.setIdUtilisateur(client.getIdUtilisateur());

            // Convert client to guide in database
            boolean conversionSuccess = convertClientToGuide(client, newGuide);

            if (conversionSuccess) {
                // Update session with new guide user
                UserSession.getInstance().setCurrentUser(newGuide);

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                         "Félicitations! Votre compte a été converti en compte Guide.\n" +
                         "Vous pouvez maintenant créer et gérer des excursions touristiques.");

                // Close dialog
                closeDialog();

                // Refresh parent window if needed
                refreshParentWindow();

            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                         "Échec de la conversion du compte. Veuillez réessayer ou contacter le support.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Base de Données",
                     "Erreur lors de la conversion: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Inattendue",
                     "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private boolean convertClientToGuide(Client client, Guide guide) throws SQLException {
        // Begin transaction-like operation
        try {
            // First, delete client-specific data
            serviceUser.supprimer(client.getIdUtilisateur());

            // Then, add as guide
            serviceUser.ajouter(guide);

            return true;

        } catch (SQLException e) {
            // Log error and re-throw
            System.err.println("Erreur lors de la conversion Client vers Guide: " + e.getMessage());
            throw e;
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Add closing animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), confirmBtn.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> closeDialog());
        fadeOut.play();
    }

    private void closeDialog() {
        Stage stage = (Stage) confirmBtn.getScene().getWindow();
        stage.close();
    }

    private void refreshParentWindow() {
        // This method could trigger a refresh of the parent account window
        // Implementation depends on your architecture
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Add some styling to the alert
        alert.getDialogPane().setStyle("-fx-background-color: #f8f9fa;");

        alert.showAndWait();
    }
}
