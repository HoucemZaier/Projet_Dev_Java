package com.PlaNova.controllers;

import com.PlaNova.models.TransportPrive;
import com.PlaNova.services.EmailService;
import com.PlaNova.services.ServiceTransportPrive;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLDataException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LocationDialogController implements Initializable {

    @FXML
    private TextField txtEmail;

    @FXML
    private Button btnValider;

    @FXML
    private Button btnAnnuler;

    @FXML
    private Label lblInfoVehicule;

    @FXML
    private Label lblDetailsVehicule;

    @FXML
    private Label lblEmailError;

    @FXML
    private Label lblConfirmationMessage;

    private TransportPrive transportPrive;
    private final ServiceTransportPrive service = new ServiceTransportPrive();
    private final EmailService emailService = new EmailService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnValider.setOnAction(e -> handleValider());
        btnAnnuler.setOnAction(e -> handleAnnuler());

        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmail();
        });

        txtEmail.setOnKeyPressed(e -> {
            lblEmailError.setText("");
            lblConfirmationMessage.setText("");
        });
    }

    public void setTransportPrive(TransportPrive transport) {
        this.transportPrive = transport;
        afficherInfosVehicule();
    }

    private void afficherInfosVehicule() {
        if (transportPrive != null) {
            lblInfoVehicule.setText("🚗 " + transportPrive.getMarque());
            lblDetailsVehicule.setText(String.format(
                    "Prix de location: %.2f DT/jour\nÉtat: %s",
                    transportPrive.getPrix_loc(),
                    transportPrive.getEtat()
            ));
        }
    }

    private boolean validateEmail() {
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            lblEmailError.setText("L'email est obligatoire");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            lblEmailError.setText("Veuillez entrer un email valide (ex: exemple@domaine.com)");
            return false;
        }

        lblEmailError.setText("");
        return true;
    }

    private void handleValider() {
        if (!validateEmail()) {
            return;
        }

        String email = txtEmail.getText().trim();

        try {
            transportPrive.setEtat("indisponible");
            service.modifier(transportPrive);

            boolean emailSent = emailService.sendLocationConfirmation(
                    email,
                    transportPrive,
                    LocalDate.now()
            );

            if (emailSent) {
                String message = String.format(
                        "✅ Location confirmée ! Vous avez loué la voiture %s au prix de %.2f DT. Un email de confirmation a été envoyé à %s",
                        transportPrive.getMarque(),
                        transportPrive.getPrix_loc(),
                        email
                );
                lblConfirmationMessage.setText(message);

                txtEmail.setDisable(true);
                btnValider.setDisable(true);

                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(3000);
                        handleAnnuler();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } else {
                lblEmailError.setText("Erreur lors de l'envoi de l'email. Veuillez réessayer.");
                transportPrive.setEtat("disponible");
                service.modifier(transportPrive);
            }

        } catch (SQLDataException e) {
            lblEmailError.setText("Erreur lors de la mise à jour: " + e.getMessage());
        } catch (Exception e) {
            lblEmailError.setText("Une erreur est survenue: " + e.getMessage());
        }
    }

    private void handleAnnuler() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }
}