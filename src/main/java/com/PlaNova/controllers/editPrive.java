package com.PlaNova.controllers;

import com.PlaNova.models.TransportPrive;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.PlaNova.services.ServiceTransportPrive;

import java.net.URL;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class editPrive implements Initializable {

    @FXML
    private TextField prixLocationField;

    @FXML
    private TextField etatField;

    @FXML
    private Button updateButton;

    private final ServiceTransportPrive service = new ServiceTransportPrive();
    private TransportPrive transport;

    public void setTransport(TransportPrive transport) {
        this.transport = transport;
        if (transport != null) {
            prixLocationField.setText(String.valueOf(transport.getPrix_loc()));
            etatField.setText(transport.getEtat());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void handleUpdateCar() {
        if (transport == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun transport sélectionné.");
            return;
        }

        double prix;
        try {
            prix = Double.parseDouble(prixLocationField.getText().trim());
            if (prix <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le prix doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le prix doit être un nombre valide.");
            return;
        }

        String etat = etatField.getText().trim();
        if (etat.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "L'état ne doit pas être vide.");
            return;
        }

        transport.setPrix_loc(prix);
        transport.setEtat(etat);

        try {
            service.modifier(transport);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transport privé mis à jour avec succès.");
            closeWindow();
        } catch (SQLException e) {
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
