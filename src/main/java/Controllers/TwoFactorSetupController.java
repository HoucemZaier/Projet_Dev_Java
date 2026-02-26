package Controllers;

import Services.TOTPService;
import Services.ServiceUser;
import Models.User;
import utils.UserSession;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for Two-Factor Authentication Setup
 */
public class TwoFactorSetupController implements Initializable {

    @FXML private Button setupFaceIdBtn;
    @FXML private Button setupTotpBtn;
    @FXML private Text faceIdStatusIcon;
    @FXML private Text faceIdStatusText;
    @FXML private Text totpStatusIcon;
    @FXML private Text totpStatusText;

    @FXML private VBox qrCodeSection;
    @FXML private ImageView qrCodeImage;
    @FXML private TextField secretKeyField;
    @FXML private TextField verificationCodeField;
    @FXML private Button verifyCodeBtn;
    @FXML private Text verificationMessage;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private TOTPService totpService;
    private ServiceUser serviceUser;

    private User currentUser;
    private String pendingTotpSecret;
    private boolean faceIdConfigured = false;
    private boolean totpConfigured = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        totpService = new TOTPService();
        serviceUser = new ServiceUser();

        currentUser = UserSession.getInstance().getCurrentUser();

        // Check current 2FA status
        updateStatusDisplay();

        // Setup input validation
        verificationCodeField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                verificationCodeField.setText(newText.replaceAll("[^\\d]", ""));
            }
            if (newText.length() > 6) {
                verificationCodeField.setText(newText.substring(0, 6));
            }

            // Auto-verify when 6 digits are entered
            if (newText.length() == 6) {
                verifyTotpCode();
            }
        });
    }

    private void updateStatusDisplay() {
        if (currentUser == null) return;

        // Debug logging
        System.out.println("=== TwoFactorSetupController Status Update ===");
        System.out.println("User ID: " + currentUser.getIdUtilisateur());
        System.out.println("TOTP Secret Key: " + (currentUser.getTotpSecretKey() != null ? "Present (length=" + currentUser.getTotpSecretKey().length() + ")" : "NULL"));
        System.out.println("Has TOTP: " + currentUser.isTotpEnabled());
        System.out.println("Has FaceID: " + (currentUser.getFaceModelData() != null));
        System.out.println("2FA Enabled Flag: " + currentUser.isTwoFactorEnabled());

        // Update Face ID status
        if (currentUser.isTwoFactorEnabled() && currentUser.getFaceModelData() != null) {
            faceIdStatusIcon.setText("✅");
            faceIdStatusText.setText("Configuré et activé");
            faceIdStatusText.setFill(javafx.scene.paint.Color.GREEN);
            setupFaceIdBtn.setText("Reconfigurer");
            faceIdConfigured = true;
        } else {
            faceIdStatusIcon.setText("⚪");
            faceIdStatusText.setText("Non configuré");
            faceIdStatusText.setFill(javafx.scene.paint.Color.GRAY);
            setupFaceIdBtn.setText("Configurer");
            faceIdConfigured = false;
        }

        // Update TOTP status
        if (currentUser.isTotpEnabled()) {
            totpStatusIcon.setText("✅");
            totpStatusText.setText("Configuré et activé");
            totpStatusText.setFill(javafx.scene.paint.Color.GREEN);
            setupTotpBtn.setText("Reconfigurer");
            totpConfigured = true;

            // If TOTP is already configured, set the pendingTotpSecret to the current one
            // This allows reconfiguration or saving without losing the current setup
            if (pendingTotpSecret == null || pendingTotpSecret.isEmpty()) {
                pendingTotpSecret = currentUser.getTotpSecretKey();
            }
        } else {
            totpStatusIcon.setText("⚪");
            totpStatusText.setText("Non configuré");
            totpStatusText.setFill(javafx.scene.paint.Color.GRAY);
            setupTotpBtn.setText("Configurer");
            totpConfigured = false;
        }

        // Enable save button if any method is configured
        saveBtn.setDisable(!(faceIdConfigured || totpConfigured));

        System.out.println("Status after update - FaceID configured: " + faceIdConfigured + ", TOTP configured: " + totpConfigured);
        System.out.println("==============================================");
    }

    @FXML
    private void setupFaceId() {
        try {
            // Load face enrollment dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/faceEnrollment.fxml"));
            Parent root = loader.load();

            // Create and show modal window
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Configuration Reconnaissance Faciale");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Center on parent window
            Stage parentStage = (Stage) setupFaceIdBtn.getScene().getWindow();
            stage.initOwner(parentStage);

            // Handle window close to update status
            stage.setOnCloseRequest(e -> {
                faceIdConfigured = true;
                updateStatusDisplay();
                showMessage("✅ Reconnaissance faciale configurée!");
            });

            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                     "Impossible de charger l'interface de configuration Face ID: " + e.getMessage());
        }
    }

    @FXML
    private void setupTotp() {
        try {
            // Generate new TOTP secret
            String secret = totpService.generateSecretKey();
            pendingTotpSecret = secret;

            // Generate QR code
            String email = currentUser.getEmail();
            Image qrCodeImg = totpService.generateQRCodeImage(email, secret);

            if (qrCodeImg == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de générer le QR code.");
                return;
            }

            // Display QR code
            qrCodeImage.setImage(qrCodeImg);
            secretKeyField.setText(secret);

            // Show QR code section with animation
            showQrCodeSection();

            // Reset verification
            verificationCodeField.clear();
            verificationMessage.setText("");
            verifyCodeBtn.setDisable(false);

            showMessage("📱 Scannez le QR code avec Microsoft Authenticator");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                     "Erreur lors de la génération du QR code: " + e.getMessage());
        }
    }

    private void showQrCodeSection() {
        qrCodeSection.setVisible(true);
        qrCodeSection.setManaged(true);

        // Animation
        qrCodeSection.setOpacity(0.0);
        qrCodeSection.setScaleX(0.8);
        qrCodeSection.setScaleY(0.8);

        Timeline animation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(qrCodeSection.opacityProperty(), 0.0),
                new KeyValue(qrCodeSection.scaleXProperty(), 0.8),
                new KeyValue(qrCodeSection.scaleYProperty(), 0.8)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(qrCodeSection.opacityProperty(), 1.0),
                new KeyValue(qrCodeSection.scaleXProperty(), 1.0),
                new KeyValue(qrCodeSection.scaleYProperty(), 1.0)
            )
        );
        animation.play();
    }

    @FXML
    private void verifyTotpCode() {
        String code = verificationCodeField.getText().trim();

        if (code.length() != 6 || !code.matches("\\d+")) {
            verificationMessage.setText("❌ Veuillez entrer un code à 6 chiffres");
            verificationMessage.setFill(javafx.scene.paint.Color.RED);
            return;
        }

        if (pendingTotpSecret == null || pendingTotpSecret.isEmpty()) {
            verificationMessage.setText("❌ Aucun secret TOTP généré");
            verificationMessage.setFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            int codeInt = Integer.parseInt(code);
            boolean isValid = totpService.verifyCode(pendingTotpSecret, codeInt);

            if (isValid) {
                verificationMessage.setText("✅ Code vérifié avec succès!");
                verificationMessage.setFill(javafx.scene.paint.Color.GREEN);
                totpConfigured = true;

                // IMPORTANT FIX: Update the user object in memory immediately
                // This ensures that the status displays correctly
                currentUser.setTotpSecretKey(pendingTotpSecret);
                UserSession.getInstance().setCurrentUser(currentUser);

                updateStatusDisplay();

                // Disable verification section
                verificationCodeField.setDisable(true);
                verifyCodeBtn.setDisable(true);

                showMessage("✅ Microsoft Authenticator configuré avec succès!");
            } else {
                verificationMessage.setText("❌ Code incorrect. Vérifiez l'heure de votre appareil");
                verificationMessage.setFill(javafx.scene.paint.Color.RED);
            }
        } catch (NumberFormatException e) {
            verificationMessage.setText("❌ Format de code invalide");
            verificationMessage.setFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void saveConfiguration() {
        if (!faceIdConfigured && !totpConfigured) {
            showAlert(Alert.AlertType.WARNING, "Aucune Configuration",
                     "Veuillez configurer au moins une méthode d'authentification.");
            return;
        }

        try {
            // Update user's 2FA settings in database
            if (totpConfigured && pendingTotpSecret != null) {
                // Save TOTP secret to database
                serviceUser.updateTotpSecretKey(currentUser.getIdUtilisateur(), pendingTotpSecret);
                currentUser.setTotpSecretKey(pendingTotpSecret);
            }

            // Enable 2FA if any method is configured
            boolean enable2FA = faceIdConfigured || totpConfigured;
            currentUser.setTwoFactorEnabled(enable2FA);
            serviceUser.modifier(currentUser);

            // Update session with fresh user data
            UserSession.getInstance().setCurrentUser(currentUser);

            showAlert(Alert.AlertType.INFORMATION, "Configuration Enregistrée",
                     "L'authentification à double facteur a été configurée avec succès.\n" +
                     "Elle sera active lors de votre prochaine connexion.");

            closeDialog();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Sauvegarde",
                     "Impossible de sauvegarder la configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void closeDialog() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message) {
        // This could be implemented with a temporary label or toast notification
        System.out.println("Message: " + message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
