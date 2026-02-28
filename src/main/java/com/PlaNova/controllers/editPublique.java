package com.PlaNova.controllers;

import com.PlaNova.models.TransportPublique;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.PlaNova.services.ServiceTransportPublique;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.ResourceBundle;

public class editPublique implements Initializable {

    @FXML
    private TextField tarifField;

    @FXML
    private TextField horaireField;

    @FXML
    private Button updateButton;

    private final ServiceTransportPublique service = new ServiceTransportPublique();
    private TransportPublique transport;

    public void setTransport(TransportPublique transport) {
        this.transport = transport;
        if (transport != null) {
            tarifField.setText(String.valueOf(transport.getTarif()));
            horaireField.setText(transport.getHoraire());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // rien de spécial pour l'instant
    }

    @FXML
    private void handleUpdateTransport() {
        if (transport == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun transport sélectionné.");
            return;
        }

        // Validation simple du tarif
        double tarif;
        try {
            tarif = Double.parseDouble(tarifField.getText().trim());
            if (tarif <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le tarif doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le tarif doit être un nombre valide.");
            return;
        }

        String horaire = horaireField.getText().trim();
        if (horaire.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "L'horaire ne doit pas être vide.");
            return;
        }

        // Mise à jour de l'objet et persistance
        transport.setTarif(tarif);
        transport.setHoraire(horaire);

        try {
            service.modifier(transport);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transport mis à jour avec succès.");
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) updateButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
