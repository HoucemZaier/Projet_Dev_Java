package Controllers;

import Services.TOTPService;
import Services.FaceRecognitionService;
import Models.User;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Two-Factor Authentication Verification during login
 */
public class TwoFactorVerificationController implements Initializable {

    @FXML private VBox methodSelectionSection;
    @FXML private Button faceIdBtn;
    @FXML private Button totpBtn;
    @FXML private VBox totpInputSection;
    @FXML private TextField totpCodeField;
    @FXML private Text totpMessage;
    @FXML private Button verifyTotpBtn;
    @FXML private Button backBtn;
    @FXML private Text statusText;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button cancelBtn;

    private TOTPService totpService;
    private FaceRecognitionService faceRecognitionService;
    private User userToVerify;
    private Runnable onSuccessCallback;
    private Runnable onFailureCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        totpService = new TOTPService();
        faceRecognitionService = new FaceRecognitionService();

        // Setup input validation for TOTP
        totpCodeField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                totpCodeField.setText(newText.replaceAll("[^\\d]", ""));
            }
            if (newText.length() > 6) {
                totpCodeField.setText(newText.substring(0, 6));
            }

            // Auto-verify when 6 digits are entered
            if (newText.length() == 6) {
                verifyTotp();
            }
        });
    }

    /**
     * Set the user to verify - PUBLIC METHOD
     */
    public void setUserToVerify(User user) {
        this.userToVerify = user;
        updateAvailableMethods();
    }

    /**
     * Set callbacks for success and failure - PUBLIC METHOD
     */
    public void setCallbacks(Runnable onSuccess, Runnable onFailure) {
        this.onSuccessCallback = onSuccess;
        this.onFailureCallback = onFailure;
    }

    private void updateAvailableMethods() {
        if (userToVerify == null) return;

        boolean hasFaceId = userToVerify.getFaceModelData() != null;
        boolean hasTotp = userToVerify.isTotpEnabled();

        faceIdBtn.setVisible(hasFaceId);
        faceIdBtn.setManaged(hasFaceId);

        totpBtn.setVisible(hasTotp);
        totpBtn.setManaged(hasTotp);

        // If only one method is available, show it directly
        if (hasFaceId && !hasTotp) {
            useFaceId();
        } else if (hasTotp && !hasFaceId) {
            useTotp();
        }
    }

    @FXML
    private void useFaceId() {
        statusText.setText("Démarrage de la vérification faciale...");
        progressIndicator.setVisible(true);

        // Hide method selection
        methodSelectionSection.setVisible(false);
        methodSelectionSection.setManaged(false);

        // Start face verification task
        Task<Boolean> faceVerificationTask = new Task<>() {
            @Override
            protected Boolean call() {
                Platform.runLater(() -> statusText.setText("Positionnez votre visage devant la caméra..."));
                return faceRecognitionService.verifyIdentity(userToVerify.getFaceModelData());
            }
        };

        faceVerificationTask.setOnSucceeded(e -> {
            boolean success = faceVerificationTask.getValue();
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                if (success) {
                    statusText.setText("✅ Vérification faciale réussie!");
                    // Close dialog after short delay
                    Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1),
                        event -> {
                            if (onSuccessCallback != null) {
                                onSuccessCallback.run();
                            }
                            closeDialog();
                        }));
                    delay.play();
                } else {
                    statusText.setText("❌ Vérification faciale échouée");
                    showMethodSelectionWithDelay();
                }
            });
        });

        faceVerificationTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                statusText.setText("❌ Erreur lors de la vérification faciale");
                showMethodSelectionWithDelay();
            });
        });

        new Thread(faceVerificationTask).start();
    }

    @FXML
    private void useTotp() {
        showTotpInput();
    }

    private void showTotpInput() {
        // Hide method selection
        methodSelectionSection.setVisible(false);
        methodSelectionSection.setManaged(false);

        // Show TOTP input with animation
        totpInputSection.setVisible(true);
        totpInputSection.setManaged(true);

        // Animation
        totpInputSection.setOpacity(0.0);
        totpInputSection.setScaleX(0.8);
        totpInputSection.setScaleY(0.8);

        Timeline animation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(totpInputSection.opacityProperty(), 0.0),
                new KeyValue(totpInputSection.scaleXProperty(), 0.8),
                new KeyValue(totpInputSection.scaleYProperty(), 0.8)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(totpInputSection.opacityProperty(), 1.0),
                new KeyValue(totpInputSection.scaleXProperty(), 1.0),
                new KeyValue(totpInputSection.scaleYProperty(), 1.0)
            )
        );
        animation.play();

        // Focus on input field
        Platform.runLater(() -> totpCodeField.requestFocus());
    }

    @FXML
    private void verifyTotp() {
        String code = totpCodeField.getText().trim();

        if (code.length() != 6 || !code.matches("\\d+")) {
            totpMessage.setText("❌ Veuillez entrer un code à 6 chiffres");
            totpMessage.setFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            int codeInt = Integer.parseInt(code);
            String secretKey = userToVerify.getTotpSecretKey();

            if (secretKey == null || secretKey.isEmpty()) {
                totpMessage.setText("❌ Configuration TOTP invalide");
                totpMessage.setFill(javafx.scene.paint.Color.RED);
                return;
            }

            boolean isValid = totpService.verifyCode(secretKey, codeInt);

            if (isValid) {
                totpMessage.setText("✅ Code vérifié avec succès!");
                totpMessage.setFill(javafx.scene.paint.Color.GREEN);

                // Disable input
                totpCodeField.setDisable(true);
                verifyTotpBtn.setDisable(true);

                // Close dialog after short delay
                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1.5),
                    event -> {
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                        closeDialog();
                    }));
                delay.play();
            } else {
                totpMessage.setText("❌ Code incorrect. Vérifiez l'heure de votre appareil");
                totpMessage.setFill(javafx.scene.paint.Color.RED);
                totpCodeField.clear();
            }
        } catch (NumberFormatException e) {
            totpMessage.setText("❌ Format de code invalide");
            totpMessage.setFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void showMethodSelection() {
        // Hide TOTP input
        totpInputSection.setVisible(false);
        totpInputSection.setManaged(false);

        // Reset TOTP input
        totpCodeField.clear();
        totpCodeField.setDisable(false);
        totpMessage.setText("");
        verifyTotpBtn.setDisable(false);

        // Show method selection
        methodSelectionSection.setVisible(true);
        methodSelectionSection.setManaged(true);

        statusText.setText("");
    }

    private void showMethodSelectionWithDelay() {
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(2),
            event -> showMethodSelection()));
        delay.play();
    }

    @FXML
    private void cancelVerification() {
        if (onFailureCallback != null) {
            onFailureCallback.run();
        }
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
