package Controllers;

import Services.FaceRecognitionService;
import Models.User;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Face Verification during login
 */
public class FaceVerificationController implements Initializable {

    @FXML private StackPane cameraContainer;
    @FXML private Label cameraStatusLabel;
    @FXML private Label statusLabel;
    @FXML private Text instructionLabel;
    @FXML private ProgressIndicator verificationProgress;
    @FXML private Button startVerificationBtn;
    @FXML private Button cancelBtn;

    private FaceRecognitionService faceRecognitionService;
    private User userToVerify;
    private boolean verificationSuccessful = false;
    private Runnable onSuccessCallback;
    private Runnable onFailureCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        faceRecognitionService = new FaceRecognitionService();

        // Check camera availability
        checkCameraAvailability();

        // Add entrance animation
        playEntranceAnimation();
    }

    /**
     * Set the user to verify
     */
    public void setUserToVerify(User user) {
        this.userToVerify = user;
    }

    /**
     * Set callbacks for success and failure
     */
    public void setCallbacks(Runnable onSuccess, Runnable onFailure) {
        this.onSuccessCallback = onSuccess;
        this.onFailureCallback = onFailure;
    }

    private void playEntranceAnimation() {
        Platform.runLater(() -> {
            cameraContainer.setScaleX(0.8);
            cameraContainer.setScaleY(0.8);
            cameraContainer.setOpacity(0.0);

            Timeline animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(cameraContainer.scaleXProperty(), 0.8),
                    new KeyValue(cameraContainer.scaleYProperty(), 0.8),
                    new KeyValue(cameraContainer.opacityProperty(), 0.0)
                ),
                new KeyFrame(Duration.millis(500),
                    new KeyValue(cameraContainer.scaleXProperty(), 1.0),
                    new KeyValue(cameraContainer.scaleYProperty(), 1.0),
                    new KeyValue(cameraContainer.opacityProperty(), 1.0)
                )
            );
            animation.play();
        });
    }

    private void checkCameraAvailability() {
        Task<Boolean> cameraCheckTask = new Task<>() {
            @Override
            protected Boolean call() {
                return faceRecognitionService.isCameraAvailable();
            }
        };

        cameraCheckTask.setOnSucceeded(e -> {
            boolean cameraAvailable = cameraCheckTask.getValue();
            if (cameraAvailable) {
                cameraStatusLabel.setText("📷 Caméra prête");
                statusLabel.setText("Prêt pour la vérification");
                startVerificationBtn.setDisable(false);
            } else {
                cameraStatusLabel.setText("❌ Caméra indisponible");
                statusLabel.setText("Erreur caméra");
                statusLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 16px; -fx-font-weight: bold;");
                instructionLabel.setText("Veuillez connecter une caméra et réessayer");
                startVerificationBtn.setDisable(true);
            }
        });

        cameraCheckTask.setOnFailed(e -> {
            cameraStatusLabel.setText("⚠️ Erreur système");
            statusLabel.setText("Erreur technique");
            statusLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 16px; -fx-font-weight: bold;");
            instructionLabel.setText("Erreur lors de l'initialisation de la caméra");
            startVerificationBtn.setDisable(true);
        });

        new Thread(cameraCheckTask).start();
    }

    @FXML
    private void handleStartVerification(ActionEvent event) {
        if (userToVerify == null || userToVerify.getFaceModelData() == null) {
            showError("Données manquantes", "Impossible de vérifier l'identité - données 2FA non trouvées");
            return;
        }

        // Disable button and show progress
        startVerificationBtn.setDisable(true);
        verificationProgress.setVisible(true);

        statusLabel.setText("Vérification en cours...");
        statusLabel.setStyle("-fx-text-fill: #2196f3; -fx-font-size: 16px; -fx-font-weight: bold;");
        instructionLabel.setText("Regardez directement la caméra et restez immobile.\nLa vérification prend 10-15 secondes maximum.");
        cameraStatusLabel.setText("🔍 Analyse faciale pratique...");

        // Start verification task with realistic timeout
        Task<Boolean> verificationTask = new Task<>() {
            @Override
            protected Boolean call() {
                try {
                    return faceRecognitionService.verifyFace(
                        userToVerify.getFaceModelData(),
                        userToVerify.getIdUtilisateur()
                    );
                } catch (Exception e) {
                    System.err.println("Erreur verification: " + e.getMessage());
                    return false;
                }
            }
        };

        // Add progress updates
        Timeline progressUpdater = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Analyse en cours...");
                    instructionLabel.setText("Détection de votre visage - restez face à la caméra");
                });
            }),
            new KeyFrame(Duration.seconds(7), e -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Comparaison biométrique...");
                    instructionLabel.setText("Comparaison avec votre profil enregistré...");
                });
            }),
            new KeyFrame(Duration.seconds(12), e -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Finalisation...");
                    instructionLabel.setText("Validation des correspondances détectées...");
                });
            })
        );
        progressUpdater.play();

        verificationTask.setOnSucceeded(e -> {
            progressUpdater.stop();
            boolean verified = verificationTask.getValue();
            verificationProgress.setVisible(false);

            if (verified) {
                showSuccess();
            } else {
                showVerificationFailed();
            }
        });

        verificationTask.setOnFailed(e -> {
            progressUpdater.stop();
            verificationProgress.setVisible(false);
            Throwable exception = verificationTask.getException();
            String errorMessage = exception != null ? exception.getMessage() : "Erreur technique inconnue";
            showError("Erreur de vérification", errorMessage);
        });

        // Set reasonable timeout for the task
        Timeline taskTimeout = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
            if (verificationTask.isRunning()) {
                verificationTask.cancel();
                verificationProgress.setVisible(false);
                showError("Timeout", "La vérification a pris trop de temps. Veuillez réessayer.");
            }
        }));
        taskTimeout.play();

        new Thread(verificationTask).start();
    }

    private void showSuccess() {
        verificationSuccessful = true;

        Platform.runLater(() -> {
            statusLabel.setText("✅ Vérification réussie!");
            statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 18px; -fx-font-weight: bold;");
            instructionLabel.setText("Identité confirmée - Connexion en cours...");
            cameraStatusLabel.setText("🎉 Succès");

            // Success animation
            Timeline successAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(statusLabel.scaleXProperty(), 1.0),
                    new KeyValue(statusLabel.scaleYProperty(), 1.0)
                ),
                new KeyFrame(Duration.millis(200),
                    new KeyValue(statusLabel.scaleXProperty(), 1.2),
                    new KeyValue(statusLabel.scaleYProperty(), 1.2)
                ),
                new KeyFrame(Duration.millis(400),
                    new KeyValue(statusLabel.scaleXProperty(), 1.0),
                    new KeyValue(statusLabel.scaleYProperty(), 1.0)
                )
            );

            successAnimation.setOnFinished(e -> {
                // Auto-close after 1.5 seconds
                Timeline autoClose = new Timeline(
                    new KeyFrame(Duration.seconds(1.5), ae -> {
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                        closeWindow();
                    })
                );
                autoClose.play();
            });

            successAnimation.play();
        });
    }

    private void showVerificationFailed() {
        Platform.runLater(() -> {
            statusLabel.setText("❌ Vérification échouée");
            statusLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 16px; -fx-font-weight: bold;");
            instructionLabel.setText("❌ Visage non reconnu ou pas assez de correspondances détectées.\n\n💡 Conseils:\n• Assurez-vous d'avoir un bon éclairage\n• Regardez directement la caméra\n• Utilisez le même angle que lors de l'enregistrement\n• Restez immobile pendant la vérification\n\nVous pouvez réessayer ou utiliser votre mot de passe.");
            cameraStatusLabel.setText("⚠️ Accès refusé");

            // Re-enable verification button
            startVerificationBtn.setText("🔄 Réessayer");
            startVerificationBtn.setDisable(false);

            // Error animation
            Timeline errorShake = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(statusLabel.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(100), new KeyValue(statusLabel.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(200), new KeyValue(statusLabel.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(300), new KeyValue(statusLabel.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(400), new KeyValue(statusLabel.translateXProperty(), 0))
            );
            errorShake.play();
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            statusLabel.setText("⚠️ " + title);
            statusLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 16px; -fx-font-weight: bold;");
            instructionLabel.setText(message);
            cameraStatusLabel.setText("❌ Erreur");

            startVerificationBtn.setText("🔄 Réessayer");
            startVerificationBtn.setDisable(false);
        });
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (onFailureCallback != null) {
            onFailureCallback.run();
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();

        // Exit animation
        Timeline exitAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(stage.getScene().getRoot().scaleXProperty(), 1.0),
                new KeyValue(stage.getScene().getRoot().scaleYProperty(), 1.0),
                new KeyValue(stage.getScene().getRoot().opacityProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(stage.getScene().getRoot().scaleXProperty(), 0.8),
                new KeyValue(stage.getScene().getRoot().scaleYProperty(), 0.8),
                new KeyValue(stage.getScene().getRoot().opacityProperty(), 0.0)
            )
        );

        exitAnimation.setOnFinished(e -> stage.close());
        exitAnimation.play();
    }

    /**
     * Check if verification was successful
     */
    public boolean isVerificationSuccessful() {
        return verificationSuccessful;
    }
}
