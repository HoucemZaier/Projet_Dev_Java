package Controllers;

import Models.Client;
import Models.Guide;
import Models.User;
import Services.ServiceUser;
import Services.FileUploadService;
import Services.NotificationService;
import utils.UserSession;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class GuideRequirementsController implements Initializable {

    @FXML private Button confirmBtn;
    @FXML private Button cancelBtn;
    @FXML private Button uploadCvBtn;
    @FXML private Label cvStatusLabel;

    private User currentUser;
    private ServiceUser serviceUser = new ServiceUser();
    private FileUploadService fileUploadService = new FileUploadService();
    private NotificationService notificationService = new NotificationService();

    private File selectedCvFile;
    private String cvCloudLink;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = UserSession.getInstance().getCurrentUser();

        // Add hover animations to buttons
        addButtonAnimations(confirmBtn);
        addButtonAnimations(cancelBtn);
        addButtonAnimations(uploadCvBtn);

        // Initially disable confirm button until CV is uploaded
        confirmBtn.setDisable(true);
        cvStatusLabel.setText("📄 Veuillez télécharger votre CV (PDF, DOC, DOCX - Max 5MB)");
        cvStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
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
    private void handleUploadCV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez votre CV");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Documents Word", "*.doc", "*.docx"),
            new FileChooser.ExtensionFilter("Tous les documents", "*.pdf", "*.doc", "*.docx")
        );

        Stage stage = (Stage) uploadCvBtn.getScene().getWindow();
        selectedCvFile = fileChooser.showOpenDialog(stage);

        if (selectedCvFile != null) {
            try {
                // Upload CV to cloud storage
                String clientFullName = currentUser.getPrenom() + "_" + currentUser.getNom();
                cvCloudLink = fileUploadService.uploadCV(selectedCvFile, clientFullName);

                // Update UI
                cvStatusLabel.setText("✅ CV téléchargé avec succès: " + selectedCvFile.getName());
                cvStatusLabel.setStyle("-fx-text-fill: #27ae60;");

                // Enable confirm button
                confirmBtn.setDisable(false);

                // Add success animation
                FadeTransition fadeSuccess = new FadeTransition(Duration.millis(300), cvStatusLabel);
                fadeSuccess.setFromValue(0.5);
                fadeSuccess.setToValue(1.0);
                fadeSuccess.play();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'Upload",
                    "Impossible de télécharger le CV: " + e.getMessage());

                cvStatusLabel.setText("❌ Erreur lors du téléchargement du CV");
                cvStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        }
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        try {
            // Validate that current user is a client
            if (!(currentUser instanceof Client)) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Seuls les clients peuvent devenir des guides.");
                return;
            }

            // Validate that CV has been uploaded
            if (cvCloudLink == null || cvCloudLink.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "CV Requis",
                    "Veuillez télécharger votre CV avant de confirmer votre demande.");
                return;
            }

            Client client = (Client) currentUser;

            // Send notification to admin with CV link
            notificationService.sendGuideApplicationNotification(client, cvCloudLink);

            // Show success message - application is now pending
            showAlert(Alert.AlertType.INFORMATION, "Demande Envoyée",
                "🎯 Votre demande pour devenir Guide a été envoyée avec succès!\n\n" +
                "📄 CV: " + selectedCvFile.getName() + "\n" +
                "⏳ Status: En attente d'approbation\n\n" +
                "Un administrateur examinera votre CV et vous contactera bientôt.\n" +
                "Vous recevrez une notification une fois que votre demande aura été traitée.");

            // Close dialog
            closeDialog();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                "Une erreur s'est produite lors de l'envoi de votre demande: " + e.getMessage());
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
