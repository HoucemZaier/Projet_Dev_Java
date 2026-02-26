package Controllers;

import Models.User;
import Services.NotificationService;
import Services.FileUploadService;
import Services.ServiceUser;
import utils.UserSession;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminNotificationsController implements Initializable {

    @FXML private VBox notificationsList;
    @FXML private Label notificationCountLabel;
    @FXML private Button refreshBtn;
    @FXML private ScrollPane notificationsScrollPane;

    private NotificationService notificationService;
    private FileUploadService fileUploadService;
    private ServiceUser serviceUser;
    private Runnable onNotificationUpdate; // Callback to refresh dashboard

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        notificationService = new NotificationService();
        fileUploadService = new FileUploadService();
        serviceUser = new ServiceUser();

        // Load notifications
        loadNotifications();

        // Add refresh button animation
        addButtonAnimation(refreshBtn);
    }

    /**
     * Set callback to refresh notification count in dashboard
     */
    public void setOnNotificationUpdate(Runnable callback) {
        this.onNotificationUpdate = callback;
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadNotifications();

        // Add refresh animation
        ScaleTransition scaleAnimation = new ScaleTransition(Duration.millis(150), refreshBtn);
        scaleAnimation.setFromX(1.0);
        scaleAnimation.setFromY(1.0);
        scaleAnimation.setToX(1.2);
        scaleAnimation.setToY(1.2);
        scaleAnimation.setCycleCount(2);
        scaleAnimation.setAutoReverse(true);
        scaleAnimation.play();
    }

    private void loadNotifications() {
        try {
            List<String> applications = notificationService.getPendingGuideApplications();

            // Update notification count
            notificationCountLabel.setText(applications.size() + " demande(s) en attente");

            // Clear existing notifications
            notificationsList.getChildren().clear();

            if (applications.isEmpty()) {
                Label noNotifications = new Label("📭 Aucune demande de guide en attente");
                noNotifications.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-padding: 20;");
                notificationsList.getChildren().add(noNotifications);
            } else {
                // Add each application as a notification card
                for (String application : applications) {
                    VBox notificationCard = createNotificationCard(application);
                    notificationsList.getChildren().add(notificationCard);
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                "Erreur lors du chargement des notifications: " + e.getMessage());
        }
    }

    private VBox createNotificationCard(String applicationData) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; " +
                     "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Parse application data
        String[] lines = applicationData.split("\n");
        String clientName = "";
        String email = "";
        String cvLink = "";
        String timestamp = "";

        for (String line : lines) {
            if (line.contains("Client:")) {
                clientName = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.contains("Email:")) {
                email = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.contains("CV:")) {
                cvLink = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.startsWith("[")) {
                timestamp = line.substring(1, line.indexOf("]"));
            }
        }

        // Header with timestamp
        Label timestampLabel = new Label("🕒 " + timestamp);
        timestampLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        // Client info
        Label titleLabel = new Label("🎯 Nouvelle demande de guide");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        Label clientLabel = new Label("👤 Candidat: " + clientName);
        clientLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-font-weight: bold;");

        Label emailLabel = new Label("📧 Email: " + email);
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        // CV section
        Label cvLabel = new Label("📄 CV: " + (cvLink.length() > 50 ? cvLink.substring(0, 50) + "..." : cvLink));
        cvLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #007bff;");

        // Action buttons
        Button viewCvBtn = new Button("👀 Voir CV");
        viewCvBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-radius: 15; " +
                          "-fx-background-radius: 15; -fx-padding: 8 15; -fx-font-size: 12px; -fx-cursor: hand;");

        Button approveBtn = new Button("✅ Approuver");
        approveBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 15; " +
                           "-fx-background-radius: 15; -fx-padding: 8 15; -fx-font-size: 12px; -fx-cursor: hand;");

        Button rejectBtn = new Button("❌ Rejeter");
        rejectBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 15; " +
                          "-fx-background-radius: 15; -fx-padding: 8 15; -fx-font-size: 12px; -fx-cursor: hand;");

        // Button actions
        final String finalEmail = email;
        final String finalCvLink = cvLink;
        final String finalClientName = clientName;

        viewCvBtn.setOnAction(e -> openCV(finalCvLink));
        approveBtn.setOnAction(e -> approveApplication(finalEmail, finalClientName, card));
        rejectBtn.setOnAction(e -> rejectApplication(finalEmail, finalClientName, card));

        // Add hover animations
        addButtonAnimation(viewCvBtn);
        addButtonAnimation(approveBtn);
        addButtonAnimation(rejectBtn);

        // Button container
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox();
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(viewCvBtn, approveBtn, rejectBtn);

        // Add all elements to card
        card.getChildren().addAll(timestampLabel, titleLabel, clientLabel, emailLabel, cvLabel, buttonBox);

        return card;
    }

    private void openCV(String cvLink) {
        try {
            String localPath = fileUploadService.getLocalFilePath(cvLink);
            if (localPath != null && new File(localPath).exists()) {
                Desktop.getDesktop().open(new File(localPath));
            } else {
                showAlert(Alert.AlertType.WARNING, "CV Non Trouvé",
                    "Le fichier CV n'a pas pu être ouvert.\nLien: " + cvLink);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                "Erreur lors de l'ouverture du CV: " + e.getMessage());
        }
    }

    private void approveApplication(String clientEmail, String clientName, VBox card) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Approuver la Demande");
        confirmAlert.setHeaderText("Approuver " + clientName + " comme guide?");
        confirmAlert.setContentText("Cette action convertira le compte client en compte guide.");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                System.out.println("✅ DÉBUT DU PROCESSUS D'APPROBATION");
                System.out.println("📧 Email client: " + clientEmail);
                System.out.println("👤 Nom client: " + clientName);

                // Convert client to guide in database
                convertClientToGuide(clientEmail);

                // Update notification status
                notificationService.updateApplicationStatus(clientEmail, "APPROUVÉ",
                    "Demande approuvée par " + UserSession.getInstance().getCurrentUser().getPrenom());

                System.out.println("✅ updateApplicationStatus (APPROUVÉ) appelé avec succès");

                // Remove card from UI with animation
                removeCardWithAnimation(card);

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "La demande de " + clientName + " a été approuvée avec succès!");

            } catch (Exception e) {
                System.err.println("❌ ERREUR dans l'approbation: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'approbation: " + e.getMessage());
            }
        }
    }

    private void rejectApplication(String clientEmail, String clientName, VBox card) {
        try {
            // Load the professional rejection dialog
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/rejectGuideDialog.fxml"));
            javafx.scene.Parent root = loader.load();

            // Get the controller and configure it
            RejectGuideDialogController dialogController = loader.getController();
            dialogController.setCandidateName(clientName); // This will hide the ID automatically

            // Create new stage for the dialog
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Rejeter la Demande");
            dialogStage.setScene(new javafx.scene.Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(450);
            dialogStage.setMinHeight(400);
            dialogStage.setMaxWidth(600);
            dialogStage.setMaxHeight(600);
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initStyle(javafx.stage.StageStyle.DECORATED);

            // Center the dialog
            dialogStage.centerOnScreen();

            // Set up the confirmation callback
            dialogController.setOnConfirmCallback(reason -> {
                try {
                    System.out.println("🚫 DÉBUT DU PROCESSUS DE REJET");
                    System.out.println("📧 Email client: " + clientEmail);
                    System.out.println("📝 Raison: " + reason);

                    // Update notification status with the rejection reason
                    notificationService.updateApplicationStatus(clientEmail, "REJETÉ",
                        "Rejeté par " + UserSession.getInstance().getCurrentUser().getPrenom() + ". Raison: " + reason);

                    System.out.println("✅ updateApplicationStatus appelé avec succès");

                    // Remove card from UI with animation
                    removeCardWithAnimation(card);

                    showAlert(Alert.AlertType.INFORMATION, "Demande Rejetée",
                        "La demande a été rejetée. Le candidat sera informé par notification.");

                } catch (Exception e) {
                    System.err.println("❌ ERREUR dans le callback de rejet: " + e.getMessage());
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors du rejet: " + e.getMessage());
                }
            });

            // Show the dialog
            dialogStage.showAndWait();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de l'interface de rejet: " + e.getMessage());

            // Enhanced fallback dialog without showing ID
            String displayName = clientName;
            if (clientName.contains("(ID:")) {
                displayName = clientName.substring(0, clientName.indexOf("(ID:")).trim();
            }

            TextInputDialog inputDialog = new TextInputDialog();
            inputDialog.setTitle("Rejeter la Demande");
            inputDialog.setHeaderText("Raison du rejet pour " + displayName);
            inputDialog.setContentText("Veuillez indiquer la raison du rejet (minimum 10 caractères):");
            inputDialog.getEditor().setPromptText("Ex: CV non conforme, manque d'expérience...");

            // Style the dialog
            DialogPane dialogPane = inputDialog.getDialogPane();
            dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #e2e8f0); " +
                               "-fx-border-color: #e2e8f0; -fx-border-radius: 15; -fx-background-radius: 15;");

            inputDialog.showAndWait().ifPresent(reason -> {
                if (reason.trim().length() >= 10) {
                    try {
                        notificationService.updateApplicationStatus(clientEmail, "REJETÉ",
                            "Rejeté par " + UserSession.getInstance().getCurrentUser().getPrenom() + ". Raison: " + reason);

                        removeCardWithAnimation(card);

                        showAlert(Alert.AlertType.INFORMATION, "Demande Rejetée",
                            "La demande a été rejetée. Le candidat sera informé par notification.");

                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur lors du rejet: " + ex.getMessage());
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Raison Insuffisante",
                        "Veuillez fournir une raison plus détaillée (minimum 10 caractères).");
                }
            });
        }
    }

    private void convertClientToGuide(String clientEmail) throws Exception {
        // Find client by email and convert to guide
        User client = serviceUser.getUserByEmail(clientEmail);
        if (client != null) {
            System.out.println("🔄 Conversion en cours: " + clientEmail);

            // First: Add guide entry (this creates entry in guide table while keeping utilisateur record)
            Models.Guide newGuide = new Models.Guide(
                client.getNom(),
                client.getPrenom(),
                client.getEmail(),
                client.getMotDePasse(),
                client.getPays(),
                client.getImageurl()
            );

            // Important: Set the same ID so it references the same utilisateur record
            newGuide.setIdUtilisateur(client.getIdUtilisateur());

            // Add guide entry first
            serviceUser.ajouter(newGuide);
            System.out.println("📝 Entrée guide créée dans la base de données");

            // Second: Remove client entry (this removes entry from client table only)
            // Note: The order matters - add guide first, then remove client
            serviceUser.supprimer(client.getIdUtilisateur());
            System.out.println("🗑️ Entrée client supprimée de la base de données");

            System.out.println("✅ Conversion terminée: " + client.getNom() + " " + client.getPrenom() + " est maintenant un Guide");
            System.out.println("🎯 Le guide peut maintenant accéder au dashboard administrateur");
        } else {
            throw new Exception("Client non trouvé avec l'email: " + clientEmail);
        }
    }

    private void removeCardWithAnimation(VBox card) {
        System.out.println("🎬 Démarrage de l'animation de suppression de carte...");

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), card);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            System.out.println("🗑️ Suppression de la carte de l'interface...");
            notificationsList.getChildren().remove(card);

            // Force refresh of notifications to update count and remove processed ones
            System.out.println("🔄 Rechargement des notifications...");
            loadNotifications();

            // Notify dashboard to refresh notification count
            if (onNotificationUpdate != null) {
                System.out.println("📊 Mise à jour du compteur de notifications dans le dashboard...");
                onNotificationUpdate.run();
            }

            // Debug: Print current notification file content
            notificationService.debugPrintNotifications();
        });
        fadeOut.play();
    }

    private void addButtonAnimation(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(100), button);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(100), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> scaleIn.play());
        button.setOnMouseExited(e -> scaleOut.play());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) refreshBtn.getScene().getWindow();
        stage.close();
    }
}
