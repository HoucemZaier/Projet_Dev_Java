package Controllers;

import Services.FaceRecognitionService;
import Services.ServiceUser;
import Models.User;
import utils.UserSession;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for automatic Face ID login on startup
 */
public class AutoFaceLoginController implements Initializable {

    @FXML private Circle scanCircle;
    @FXML private Circle outerRing;
    @FXML private Circle middleRing;
    @FXML private Circle innerRing;
    @FXML private Circle scanBeam;
    @FXML private Circle statusOverlay;
    @FXML private Circle pulseCircle1;
    @FXML private Circle pulseCircle2;
    @FXML private ImageView faceIdAnimation;
    @FXML private Text statusText;
    @FXML private Text attemptText;
    @FXML private Text attemptsLeftText;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button startScanBtn;
    @FXML private Button skipToLoginBtn;
    @FXML private Button closeBtn;

    private FaceRecognitionService faceRecognitionService;
    private ServiceUser serviceUser;
    private int attemptsRemaining = 3;
    private boolean scanningInProgress = false;
    private Timeline circleAnimation;
    private Timeline ringAnimation;
    private Timeline pulseAnimation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        faceRecognitionService = new FaceRecognitionService();
        serviceUser = new ServiceUser();

        // Check if there are users with Face ID enabled
        checkFaceIdUsersAvailable();

        // Start circle animation
        startCircleAnimation();
    }

    private void checkFaceIdUsersAvailable() {
        Task<Boolean> checkTask = new Task<>() {
            @Override
            protected Boolean call() {
                try {
                    return serviceUser.hasUsersWithFaceId();
                } catch (SQLException e) {
                    System.err.println("❌ Erreur lors de la vérification des utilisateurs Face ID: " + e.getMessage());
                    return false;
                }
            }
        };

        checkTask.setOnSucceeded(e -> {
            boolean hasFaceIdUsers = checkTask.getValue();
            if (!hasFaceIdUsers) {
                Platform.runLater(() -> {
                    statusText.setText("Aucun utilisateur avec Face ID configuré");
                    startScanBtn.setText("Aller à la Connexion Normale");
                    startScanBtn.setOnAction(event -> skipToNormalLogin());
                });
            }
        });

        new Thread(checkTask).start();
    }

    private void startCircleAnimation() {
        stopAllAnimations();
        startIdleAnimation();
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
        outerRing.getTransforms().clear();
        middleRing.getTransforms().clear();
        innerRing.getTransforms().clear();

        // Reset scales and positions
        scanCircle.setScaleX(1.0);
        scanCircle.setScaleY(1.0);
        scanCircle.setTranslateX(0);
        scanCircle.setTranslateY(0);

        pulseCircle1.setRadius(35);
        pulseCircle2.setRadius(35);
        pulseCircle1.setOpacity(0);
        pulseCircle2.setOpacity(0);

        scanBeam.setOpacity(0);
        statusOverlay.setOpacity(0);
    }

    @FXML
    private void startFaceScan() {
        if (scanningInProgress) {
            return;
        }

        scanningInProgress = true;
        startScanBtn.setDisable(true);
        progressIndicator.setVisible(true);
        statusText.setText("Analyse en cours...");
        attemptText.setText("");

        // Start iPhone-style scanning animation
        startScanningAnimation();

        // Start face verification task
        Task<User> faceVerificationTask = new Task<>() {
            @Override
            protected User call() {
                try {
                    // Get all users with face ID enabled
                    java.util.List<User> faceIdUsers = serviceUser.getUsersWithFaceId();

                    if (faceIdUsers.isEmpty()) {
                        return null;
                    }

                    Platform.runLater(() -> statusText.setText("Recherche de correspondance faciale..."));

                    // Try to verify against each user with face ID
                    for (User user : faceIdUsers) {
                        if (user.getFaceModelData() != null) {
                            Platform.runLater(() -> statusText.setText("Vérification avec " + user.getPrenom() + "..."));

                            boolean isMatch = faceRecognitionService.verifyIdentity(user.getFaceModelData());

                            if (isMatch) {
                                // Check if user is blocked
                                if (user.isBlocked()) {
                                    throw new SQLException("COMPTE_BLOQUE:Votre compte a été bloqué par l'administrateur. Contactez le support pour plus d'informations.");
                                }
                                return user;
                            }
                        }
                    }

                    return null; // No match found

                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };

        faceVerificationTask.setOnSucceeded(e -> {
            User authenticatedUser = faceVerificationTask.getValue();

            if (authenticatedUser != null) {
                // Success - proceed with login
                Platform.runLater(() -> {
                    statusText.setText("✅ Authentification réussie!");
                    attemptText.setText("Bienvenue, " + authenticatedUser.getPrenom() + "!");
                    progressIndicator.setVisible(false);

                    // Start success animation
                    startSuccessAnimation();

                    // Store user in session
                    UserSession.getInstance().setCurrentUser(authenticatedUser);

                    // Proceed with login after a short delay
                    Timeline delay = new Timeline(new KeyFrame(Duration.seconds(2.0),
                        event1 -> proceedWithLogin(authenticatedUser)));
                    delay.play();
                });
            } else {
                // Failed - reduce attempts
                attemptsRemaining--;
                Platform.runLater(() -> {
                    statusText.setText("❌ Aucune correspondance trouvée");
                    attemptText.setText("Tentative " + (4 - attemptsRemaining) + "/3 échouée");
                    attemptsLeftText.setText(String.valueOf(attemptsRemaining));
                    progressIndicator.setVisible(false);
                    scanningInProgress = false;

                    // Start error animation
                    startErrorAnimation();

                    if (attemptsRemaining > 0) {
                        startScanBtn.setDisable(false);
                        startScanBtn.setText("Réessayer (" + attemptsRemaining + " restantes)");
                    } else {
                        // No more attempts - show normal login option
                        startScanBtn.setVisible(false);
                        skipToLoginBtn.setVisible(true);
                        statusText.setText("Limite d'essais atteinte");
                        attemptText.setText("Utilisez la connexion normale");
                    }
                });
            }
        });

        faceVerificationTask.setOnFailed(e -> {
            Throwable exception = faceVerificationTask.getException();
            attemptsRemaining--;

            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                scanningInProgress = false;

                String errorMessage = exception != null ? exception.getMessage() : "Erreur inconnue";

                // Check if it's a blocked user error
                if (errorMessage.startsWith("COMPTE_BLOQUE:")) {
                    String message = errorMessage.substring("COMPTE_BLOQUE:".length());
                    showAlert("Compte Bloqué", message);
                    skipToNormalLogin();
                    return;
                }

                statusText.setText("❌ Erreur de vérification");
                attemptText.setText("Erreur: " + errorMessage);
                attemptsLeftText.setText(String.valueOf(attemptsRemaining));

                if (attemptsRemaining > 0) {
                    startScanBtn.setDisable(false);
                    startScanBtn.setText("Réessayer (" + attemptsRemaining + " restantes)");
                } else {
                    startScanBtn.setVisible(false);
                    skipToLoginBtn.setVisible(true);
                    statusText.setText("Limite d'essais atteinte");
                    attemptText.setText("Utilisez la connexion normale");
                }
            });
        });

        new Thread(faceVerificationTask).start();
    }

    @FXML
    private void skipToNormalLogin() {
        try {
            // Load normal login interface
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) closeBtn.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("PlaNova - Connexion");
            currentStage.centerOnScreen();

        } catch (IOException e) {
            showAlert("Erreur de Navigation",
                     "Impossible de charger l'interface de connexion: " + e.getMessage());
        }
    }

    @FXML
    private void closeWindow() {
        Platform.exit();
    }

    private void proceedWithLogin(User user) {
        try {
            if (UserSession.getInstance().isClient() || UserSession.getInstance().isGuide()) {
                // Clients and Guides go to explore interface
                navigateToExplore();
            } else if (UserSession.getInstance().canAccessDashboard()) {
                // Admin and Moderator go to dashboard
                navigateToDashboard(user);
            } else {
                // Other roles not allowed
                UserSession.getInstance().logout();
                showAlert("Accès refusé",
                    "Ce type de compte n'a pas d'interface dédiée. Contactez l'administrateur.");
                skipToNormalLogin();
            }
        } catch (Exception e) {
            showAlert("Erreur de Navigation",
                     "Erreur lors de la navigation: " + e.getMessage());
            skipToNormalLogin();
        }
    }

    private void navigateToExplore() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/explore.fxml"));
        Parent root = loader.load();


        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("PlaNova - Explore");
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }

    private void navigateToDashboard(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
        Parent root = loader.load();

        // Get the controller and pass user info
        dashboardController controller = loader.getController();
        controller.setCurrentUser(user);

        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("PlaNova - Dashboard");
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
