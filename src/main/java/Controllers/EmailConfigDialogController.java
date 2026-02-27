package Controllers;

import utils.Services.EmailConfigService;
import utils.Services.EmailService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EmailConfigDialogController implements Initializable {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnTester;

    @FXML
    private Button btnAnnuler;

    @FXML
    private Button btnSauvegarder;

    @FXML
    private Label lblMessage;

    @FXML
    private Label lblPasswordHint;

    private final EmailConfigService configService = new EmailConfigService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger la configuration existante
        loadExistingConfig();

        // Configuration des boutons
        btnTester.setOnAction(e -> testerConnexion());
        btnSauvegarder.setOnAction(e -> sauvegarderConfig());
        btnAnnuler.setOnAction(e -> handleAnnuler());

        // Validation en temps réel
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            validerChamps();
            lblMessage.setText("");
        });

        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validerChamps();
            lblMessage.setText("");
        });
    }

    private void loadExistingConfig() {
        txtEmail.setText(configService.getEmailUsername());
        txtPassword.setText(configService.getEmailPassword());
        
        // Afficher un message si la configuration est déjà valide
        if (configService.isConfigValid()) {
            lblMessage.setText("✅ Configuration email déjà valide");
            lblMessage.setStyle("-fx-text-fill: #10b981;");
        }
    }

    private boolean validerChamps() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        boolean emailValid = email.contains("@") && email.contains(".");
        boolean passwordValid = !password.isEmpty() && !password.equals("qhft digu dfpi risg");

        btnSauvegarder.setDisable(!(emailValid && passwordValid));
        btnTester.setDisable(!(emailValid && passwordValid));

        return emailValid && passwordValid;
    }

    private void testerConnexion() {
        if (!validerChamps()) {
            afficherMessage("Veuillez remplir correctement tous les champs", "#ef4444");
            return;
        }

        // Créer un service email temporaire avec les nouveaux paramètres
        EmailService testService = new EmailService() {
            @Override
            public boolean testConnection() {
                try {
                    java.util.Properties props = new java.util.Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", configService.getSmtpHost());
                    props.put("mail.smtp.port", configService.getSmtpPort());
                    props.put("mail.smtp.ssl.trust", configService.getSmtpHost());

                    javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
                        @Override
                        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                            return new javax.mail.PasswordAuthentication(txtEmail.getText().trim(), txtPassword.getText());
                        }
                    });

                    javax.mail.Transport transport = session.getTransport("smtp");
                    transport.connect(configService.getSmtpHost(), txtEmail.getText().trim(), txtPassword.getText());
                    transport.close();
                    return true;
                } catch (Exception e) {
                    System.err.println("Erreur de connexion: " + e.getMessage());
                    return false;
                }
            }
        };

        afficherMessage("🔄 Test de connexion en cours...", "#6366f1");

        // Tester la connexion dans un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            boolean success = testService.testConnection();
            
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    afficherMessage("✅ Connexion réussie ! La configuration email est valide.", "#10b981");
                } else {
                    afficherMessage("❌ Échec de connexion. Vérifiez vos identifiants.\n" +
                                  "Assurez-vous d'utiliser un mot de passe d'application Gmail.", "#ef4444");
                }
            });
        }).start();
    }

    private void sauvegarderConfig() {
        if (!validerChamps()) {
            afficherMessage("Veuillez remplir correctement tous les champs", "#ef4444");
            return;
        }

        try {
            configService.setEmailUsername(txtEmail.getText().trim());
            configService.setEmailPassword(txtPassword.getText());

            afficherMessage("✅ Configuration sauvegardée avec succès !", "#10b981");

            // Fermer la fenêtre après 2 secondes
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(2000);
                    handleAnnuler();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

        } catch (Exception e) {
            afficherMessage("❌ Erreur lors de la sauvegarde: " + e.getMessage(), "#ef4444");
        }
    }

    private void afficherMessage(String message, String color) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12; -fx-font-weight: bold; -fx-wrap-text: true;");
    }

    private void handleAnnuler() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }
}
