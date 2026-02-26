package Controllers;

import Services.FaceRecognitionService;
import Models.User;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Face Verification during login with iPhone-style Face ID animation
 */
public class FaceVerificationController implements Initializable {

    @FXML private StackPane cameraContainer;
    @FXML private Label cameraStatusLabel;
    @FXML private Label statusLabel;
    @FXML private Text instructionLabel;
    @FXML private ProgressIndicator verificationProgress;
    @FXML private Button startVerificationBtn;
    @FXML private Button cancelBtn;

    // iPhone-style Face ID animation elements
    @FXML private Circle scanCircle;
    @FXML private Circle outerRing;
    @FXML private Circle middleRing;
    @FXML private Circle innerRing;
    @FXML private Circle scanBeam;
    @FXML private Circle statusOverlay;
    @FXML private Circle pulseCircle1;
    @FXML private Circle pulseCircle2;

    private FaceRecognitionService faceRecognitionService;
    private User userToVerify;
    private boolean verificationSuccessful = false;
    private Runnable onSuccessCallback;
    private Runnable onFailureCallback;

    // Animation timelines
    private Timeline circleAnimation;
    private Timeline ringAnimation;
    private Timeline pulseAnimation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        faceRecognitionService = new FaceRecognitionService();

        // Check camera availability
        checkCameraAvailability();

        // Add entrance animation
        playEntranceAnimation();

        // Start iPhone-style idle animation
        Platform.runLater(() -> {
            if (scanCircle != null) {
                startIdleAnimation();
            }
        });
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
            statusLabel.setText("Erreur technique");
            statusLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 16px; -fx-font-weight: bold;");
            instructionLabel.setText("Erreur lors de l'initialisation de la caméra");
            startVerificationBtn.setDisable(true);

            // Start error animation
            startErrorAnimation();
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

        // Start iPhone-style scanning animation
        startScanningAnimation();

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

            // Start iPhone-style success animation
            startSuccessAnimation();

            // Success animation for status label
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
                // Auto-close after 2 seconds to show the animation
                Timeline autoClose = new Timeline(
                    new KeyFrame(Duration.seconds(2.0), ae -> {
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

            // Start iPhone-style error animation
            startErrorAnimation();

            // Re-enable verification button
            startVerificationBtn.setText("🔄 Réessayer");
            startVerificationBtn.setDisable(false);

            // Error animation for status label
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

            startVerificationBtn.setText("🔄 Réessayer");
            startVerificationBtn.setDisable(false);

            // Start iPhone-style error animation
            startErrorAnimation();
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

    private void startIdleAnimation() {
        // Gentle pulse animation for idle state
        Timeline idlePulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(scanCircle.opacityProperty(), 0.6),
                new KeyValue(innerRing.opacityProperty(), 0.4),
                new KeyValue(middleRing.opacityProperty(), 0.3),
                new KeyValue(outerRing.opacityProperty(), 0.2)
            ),
            new KeyFrame(Duration.seconds(2),
                new KeyValue(scanCircle.opacityProperty(), 1.0),
                new KeyValue(innerRing.opacityProperty(), 0.8),
                new KeyValue(middleRing.opacityProperty(), 0.7),
                new KeyValue(outerRing.opacityProperty(), 0.6)
            ),
            new KeyFrame(Duration.seconds(4),
                new KeyValue(scanCircle.opacityProperty(), 0.6),
                new KeyValue(innerRing.opacityProperty(), 0.4),
                new KeyValue(middleRing.opacityProperty(), 0.3),
                new KeyValue(outerRing.opacityProperty(), 0.2)
            )
        );
        idlePulse.setCycleCount(Timeline.INDEFINITE);
        idlePulse.play();
        circleAnimation = idlePulse;
    }

    private void startScanningAnimation() {
        stopAllAnimations();

        // Ring rotation animations
        RotateTransition outerRotation = new RotateTransition(Duration.seconds(8), outerRing);
        outerRotation.setFromAngle(0);
        outerRotation.setToAngle(360);
        outerRotation.setCycleCount(Timeline.INDEFINITE);
        outerRotation.setInterpolator(Interpolator.LINEAR);

        RotateTransition middleRotation = new RotateTransition(Duration.seconds(6), middleRing);
        middleRotation.setFromAngle(0);
        middleRotation.setToAngle(-360);
        middleRotation.setCycleCount(Timeline.INDEFINITE);
        middleRotation.setInterpolator(Interpolator.LINEAR);

        RotateTransition innerRotation = new RotateTransition(Duration.seconds(4), innerRing);
        innerRotation.setFromAngle(0);
        innerRotation.setToAngle(360);
        innerRotation.setCycleCount(Timeline.INDEFINITE);
        innerRotation.setInterpolator(Interpolator.LINEAR);

        // Scanning beam animation
        Timeline beamAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(scanBeam.opacityProperty(), 0)),
            new KeyFrame(Duration.millis(500), new KeyValue(scanBeam.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(scanBeam.opacityProperty(), 0))
        );
        beamAnimation.setCycleCount(Timeline.INDEFINITE);

        // Enhanced core pulse
        Timeline corePulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(scanCircle.scaleXProperty(), 0.8),
                new KeyValue(scanCircle.scaleYProperty(), 0.8),
                new KeyValue(scanCircle.opacityProperty(), 1.0)
            ),
            new KeyFrame(Duration.seconds(1),
                new KeyValue(scanCircle.scaleXProperty(), 1.2),
                new KeyValue(scanCircle.scaleYProperty(), 1.2),
                new KeyValue(scanCircle.opacityProperty(), 0.7)
            ),
            new KeyFrame(Duration.seconds(2),
                new KeyValue(scanCircle.scaleXProperty(), 0.8),
                new KeyValue(scanCircle.scaleYProperty(), 0.8),
                new KeyValue(scanCircle.opacityProperty(), 1.0)
            )
        );
        corePulse.setCycleCount(Timeline.INDEFINITE);

        // Start all animations
        outerRotation.play();
        middleRotation.play();
        innerRotation.play();
        beamAnimation.play();
        corePulse.play();

        ringAnimation = new Timeline(); // Placeholder to keep reference
    }

    private void startSuccessAnimation() {
        stopAllAnimations();

        // Success color change
        statusOverlay.setFill(javafx.scene.paint.Color.web("rgba(76,175,80,0.8)"));

        // Success pulse waves
        Timeline successPulse1 = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(pulseCircle1.radiusProperty(), 35),
                new KeyValue(pulseCircle1.opacityProperty(), 1.0),
                new KeyValue(pulseCircle1.strokeProperty(), javafx.scene.paint.Color.web("rgba(76,175,80,0.8)"))
            ),
            new KeyFrame(Duration.seconds(0.8),
                new KeyValue(pulseCircle1.radiusProperty(), 120),
                new KeyValue(pulseCircle1.opacityProperty(), 0)
            )
        );

        Timeline successPulse2 = new Timeline(
            new KeyFrame(Duration.millis(400),
                new KeyValue(pulseCircle2.radiusProperty(), 35),
                new KeyValue(pulseCircle2.opacityProperty(), 1.0),
                new KeyValue(pulseCircle2.strokeProperty(), javafx.scene.paint.Color.web("rgba(76,175,80,0.6)"))
            ),
            new KeyFrame(Duration.seconds(1.2),
                new KeyValue(pulseCircle2.radiusProperty(), 120),
                new KeyValue(pulseCircle2.opacityProperty(), 0)
            )
        );

        // Core success glow
        Timeline successGlow = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(statusOverlay.opacityProperty(), 0)),
            new KeyFrame(Duration.millis(200), new KeyValue(statusOverlay.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(statusOverlay.opacityProperty(), 0.6))
        );

        successPulse1.play();
        successPulse2.play();
        successGlow.play();

        pulseAnimation = successPulse1;
    }

    private void startErrorAnimation() {
        stopAllAnimations();

        // Error color change
        statusOverlay.setFill(javafx.scene.paint.Color.web("rgba(244,67,54,0.8)"));

        // Shake animation
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), scanCircle);
        shake.setFromX(-5);
        shake.setToX(5);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        // Error glow
        Timeline errorGlow = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(statusOverlay.opacityProperty(), 0)),
            new KeyFrame(Duration.millis(100), new KeyValue(statusOverlay.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(600), new KeyValue(statusOverlay.opacityProperty(), 0))
        );

        shake.play();
        errorGlow.play();

        // Reset to idle after error animation
        errorGlow.setOnFinished(e -> {
            Platform.runLater(() -> {
                statusOverlay.setOpacity(0);
                startIdleAnimation();
            });
        });
    }

    private void stopAllAnimations() {
        if (circleAnimation != null) {
            circleAnimation.stop();
        }
        if (ringAnimation != null) {
            ringAnimation.stop();
        }
        if (pulseAnimation != null) {
            pulseAnimation.stop();
        }

        // Stop any running transitions on rings
        if (outerRing != null) {
            outerRing.getTransforms().clear();
        }
        if (middleRing != null) {
            middleRing.getTransforms().clear();
        }
        if (innerRing != null) {
            innerRing.getTransforms().clear();
        }

        // Reset scales and positions
        if (scanCircle != null) {
            scanCircle.setScaleX(1.0);
            scanCircle.setScaleY(1.0);
            scanCircle.setTranslateX(0);
            scanCircle.setTranslateY(0);
        }

        if (pulseCircle1 != null) {
            pulseCircle1.setRadius(35);
            pulseCircle1.setOpacity(0);
        }
        if (pulseCircle2 != null) {
            pulseCircle2.setRadius(35);
            pulseCircle2.setOpacity(0);
        }

        if (scanBeam != null) {
            scanBeam.setOpacity(0);
        }
        if (statusOverlay != null) {
            statusOverlay.setOpacity(0);
        }
    }
}
