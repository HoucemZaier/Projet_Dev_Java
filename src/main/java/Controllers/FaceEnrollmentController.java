package Controllers;

import Services.FaceRecognitionService;
import Services.ServiceUser;
import Models.User;
import utils.UserSession;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Face Recognition 2FA Enrollment
 */
public class FaceEnrollmentController implements Initializable {

    @FXML private VBox cameraPreviewArea;
    @FXML private StackPane cameraContainer;
    @FXML private Label cameraStatusLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar captureProgressBar;
    @FXML private Text instructionLabel;
    @FXML private VBox statusMessagesArea;
    @FXML private Label statusIcon;
    @FXML private Label statusMessage;
    @FXML private Text statusDetails;
    @FXML private Button startCaptureBtn;
    @FXML private Button cancelBtn;
    @FXML private Button finishBtn;

    private FaceRecognitionService faceRecognitionService;
    private ServiceUser serviceUser;
    private User currentUser;
    private List<String> capturedImages;
    private String trainedModel;
    private boolean enrollmentCompleted = false;
    private Runnable onSuccessCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        faceRecognitionService = new FaceRecognitionService();
        serviceUser = new ServiceUser();
        currentUser = UserSession.getInstance().getCurrentUser();

        // Check camera availability
        checkCameraAvailability();
    }

    /**
     * Set callback to execute when enrollment is successful
     */
    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    private void checkCameraAvailability() {
        Task<Boolean> cameraCheckTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return faceRecognitionService.isCameraAvailable();
            }
        };

        cameraCheckTask.setOnSucceeded(e -> {
            boolean cameraAvailable = cameraCheckTask.getValue();
            if (cameraAvailable) {
                cameraStatusLabel.setText("📷 Caméra prête");
                cameraStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 14px; -fx-font-weight: bold;");
                startCaptureBtn.setDisable(false);
                instructionLabel.setText("Positionnez votre visage dans le cadre et cliquez sur Démarrer");
            } else {
                cameraStatusLabel.setText("❌ Caméra non disponible");
                cameraStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 14px; -fx-font-weight: bold;");
                startCaptureBtn.setDisable(true);
                instructionLabel.setText("Veuillez connecter une caméra et réessayer");
            }
        });

        cameraCheckTask.setOnFailed(e -> {
            cameraStatusLabel.setText("⚠️ Erreur caméra");
            cameraStatusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 14px; -fx-font-weight: bold;");
            startCaptureBtn.setDisable(true);
            instructionLabel.setText("Erreur lors de la vérification de la caméra");
        });

        new Thread(cameraCheckTask).start();
    }

    @FXML
    private void handleStartCapture(ActionEvent event) {
        startCaptureBtn.setDisable(true);
        cameraStatusLabel.setText("🎬 Capture en cours...");
        cameraStatusLabel.setStyle("-fx-text-fill: #007bff; -fx-font-size: 14px; -fx-font-weight: bold;");

        progressLabel.setText("Capture des images faciales...");
        instructionLabel.setText("Regardez directement la caméra. 10 photos seront prises automatiquement.");

        // Start animated progress
        animateProgress();

        // Start face capture task
        Task<List<String>> captureTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                return faceRecognitionService.captureFaceImages();
            }
        };

        captureTask.setOnSucceeded(e -> {
            capturedImages = captureTask.getValue();
            if (capturedImages != null && capturedImages.size() >= 5) {
                // At least 5 images captured successfully
                progressLabel.setText("Images capturées! Entraînement du modèle...");
                instructionLabel.setText("Traitement des données biométriques en cours...");

                // Train the face recognition model
                trainFaceModel();
            } else {
                // Not enough images captured
                showError("Capture insuffisante",
                         "Seulement " + (capturedImages != null ? capturedImages.size() : 0) +
                         " images ont été capturées. Au moins 5 sont nécessaires.");
            }
        });

        captureTask.setOnFailed(e -> {
            Throwable exception = captureTask.getException();
            showError("Erreur de capture",
                     exception != null ? exception.getMessage() : "Erreur inconnue lors de la capture");
        });

        new Thread(captureTask).start();
    }

    private void animateProgress() {
        Timeline progressTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(captureProgressBar.progressProperty(), 0.0)),
            new KeyFrame(Duration.seconds(5), new KeyValue(captureProgressBar.progressProperty(), 1.0))
        );
        progressTimeline.play();
    }

    private void trainFaceModel() {
        Task<String> trainingTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return faceRecognitionService.trainFaceModel(capturedImages, currentUser.getIdUtilisateur());
            }
        };

        trainingTask.setOnSucceeded(e -> {
            trainedModel = trainingTask.getValue();
            if (trainedModel != null && !trainedModel.isEmpty()) {
                // Save to database
                saveToDatabase();
            } else {
                showError("Erreur d'entraînement", "Impossible d'entraîner le modèle de reconnaissance faciale.");
            }
        });

        trainingTask.setOnFailed(e -> {
            Throwable exception = trainingTask.getException();
            showError("Erreur d'entraînement",
                     exception != null ? exception.getMessage() : "Erreur lors de l'entraînement du modèle");
        });

        new Thread(trainingTask).start();
    }

    private void saveToDatabase() {
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Update user with 2FA data
                currentUser.setTwoFactorEnabled(true);
                currentUser.setFaceModelData(trainedModel);

                // Save to database
                serviceUser.modifier(currentUser);

                return null;
            }
        };

        saveTask.setOnSucceeded(e -> {
            enrollmentCompleted = true;
            showSuccess();
        });

        saveTask.setOnFailed(e -> {
            Throwable exception = saveTask.getException();
            showError("Erreur de sauvegarde",
                     exception != null ? exception.getMessage() : "Erreur lors de la sauvegarde en base de données");
        });

        new Thread(saveTask).start();
    }

    private void showSuccess() {
        Platform.runLater(() -> {
            // Hide capture area
            cameraPreviewArea.setVisible(false);
            cameraPreviewArea.setManaged(false);

            // Show success message
            statusMessagesArea.setVisible(true);
            statusMessagesArea.setManaged(true);

            statusIcon.setText("✅");
            statusMessage.setText("Authentification à Deux Facteurs Activée!");
            statusMessage.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
            statusDetails.setText("La reconnaissance faciale est maintenant configurée pour votre compte. " +
                                "Lors de votre prochaine connexion, vous devrez vérifier votre identité par reconnaissance faciale.");

            // Show finish button
            finishBtn.setVisible(true);
            finishBtn.setManaged(true);

            // Hide other buttons
            startCaptureBtn.setVisible(false);
            startCaptureBtn.setManaged(false);
            cancelBtn.setText("Fermer");

            // Success animation
            Timeline successAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(statusMessagesArea.scaleXProperty(), 0.8),
                    new KeyValue(statusMessagesArea.scaleYProperty(), 0.8),
                    new KeyValue(statusMessagesArea.opacityProperty(), 0.0)
                ),
                new KeyFrame(Duration.millis(300),
                    new KeyValue(statusMessagesArea.scaleXProperty(), 1.0),
                    new KeyValue(statusMessagesArea.scaleYProperty(), 1.0),
                    new KeyValue(statusMessagesArea.opacityProperty(), 1.0)
                )
            );
            successAnimation.play();
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            // Reset UI state
            startCaptureBtn.setDisable(false);
            startCaptureBtn.setText("🔄 Réessayer");

            progressLabel.setText("Erreur");
            progressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");

            instructionLabel.setText(message);
            instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc3545; -fx-text-alignment: center;");

            cameraStatusLabel.setText("❌ " + title);
            cameraStatusLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 14px; -fx-font-weight: bold;");

            captureProgressBar.setProgress(0.0);

            // Additional instructions for better face recognition
            if (message.contains("Pas assez d'images")) {
                instructionLabel.setText("❌ " + message + "\n\n💡 Conseils:\n• Assurez-vous d'avoir un bon éclairage\n• Regardez directement la caméra\n• Restez à environ 50cm de la caméra\n• Évitez les mouvements brusques");
            }
        });
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (enrollmentCompleted && onSuccessCallback != null) {
            onSuccessCallback.run();
        }
        closeWindow();
    }

    @FXML
    private void handleFinish(ActionEvent event) {
        if (onSuccessCallback != null) {
            onSuccessCallback.run();
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    /**
     * Check if enrollment was completed successfully
     */
    public boolean isEnrollmentCompleted() {
        return enrollmentCompleted;
    }
}
