package com.PlaNova.controllers;

import com.PlaNova.models.TransportPrive;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import com.PlaNova.services.ServiceTransportPrive;
import com.PlaNova.models.Destination;
import com.PlaNova.utils.MyDatabase;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AjouterTransportPrive implements Initializable {

    @FXML
    private TextField marqueField;

    @FXML
    private TextField prixField;

    @FXML
    private TextField etatField;

    @FXML
    private ComboBox<Destination> destinationCombo;

    @FXML
    private Button uploadButton;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Label imageNameLabel;

    @FXML
    private VBox imagePreviewArea;

    // chemin de l'image sélectionnée
    private String selectedImagePath = null;

    private ServiceTransportPrive service;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        service = new ServiceTransportPrive();
        // Initialiser le champ d'état
        etatField.setPromptText("Disponible ou Indisponible");
        loadDestinations();
    }

    private void loadDestinations() {
        if (destinationCombo == null)
            return;
        List<Destination> destinations = new ArrayList<>();
        String sql = "SELECT * FROM destination";
        try {
            Connection connection = MyDatabase.getInstance().getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Destination d = new Destination();
                d.setId_destination(rs.getInt("id_destination"));
                d.setNom_destination(rs.getString("nom_destination"));
                d.setPays(rs.getString("pays"));
                d.setImage(rs.getString("image"));
                destinations.add(d);
            }
            destinationCombo.setItems(FXCollections.observableArrayList(destinations));
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des destinations: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadImage() {
        Window window = uploadButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        // Définir le répertoire initial
        fileChooser.setInitialDirectory(new File("src/main/resources/images"));

        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();

            // Afficher l'aperçu de l'image
            try {
                Image image = new Image(new File(selectedImagePath).toURI().toString());
                imagePreview.setImage(image);
                imageNameLabel.setText("Fichier: " + selectedFile.getName());

                showAlert(Alert.AlertType.INFORMATION,
                        "Succès",
                        "Image sélectionnée: " + selectedFile.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR,
                        "Erreur",
                        "Impossible de charger l'image: " + e.getMessage());
                selectedImagePath = null;
                imagePreview.setImage(null);
                imageNameLabel.setText("");
            }
        }
    }

    @FXML
    private void handleSaveTransport() {
        // Valider les champs
        if (!validateInputs()) {
            return;
        }

        try {
            // Créer un nouvel objet TransportPrive
            TransportPrive transport = new TransportPrive();
            transport.setMarque(marqueField.getText().trim());
            transport.setPrix_loc(Double.parseDouble(prixField.getText().trim()));
            transport.setEtat(etatField.getText().trim());
            transport.setImage_path(selectedImagePath);
            if (destinationCombo.getValue() != null) {
                transport.setId_destination(destinationCombo.getValue().getId_destination());
            }

            // Sauvegarder dans la base de données
            service.ajouter(transport);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Transport privé ajouté avec succès!");

            // Réinitialiser les champs
            clearFields();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    /**
     * Valide tous les champs d'entrée
     */
    private boolean validateInputs() {
        // Valider la marque (non vide)
        String marque = marqueField.getText().trim();
        if (marque.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "La marque de voiture ne doit pas être vide!");
            return false;
        }

        // Valider le prix (format avec 2 décimales)
        String prixText = prixField.getText().trim();
        if (prixText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "Le prix de location ne doit pas être vide!");
            return false;
        }

        try {
            double prix = Double.parseDouble(prixText);
            if (prix <= 0) {
                showAlert(Alert.AlertType.ERROR,
                        "Erreur de validation",
                        "Le prix doit être supérieur à 0!");
                return false;
            }
            // Vérifier le format à 2 décimales
            if (!prixText.matches("\\d+(\\.\\d{1,2})?")) {
                showAlert(Alert.AlertType.ERROR,
                        "Erreur de validation",
                        "Le prix doit avoir maximum 2 chiffres après la virgule (ex: 50.00)!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "Le prix doit être un nombre valide (ex: 50 ou 50.00)!");
            return false;
        }

        // Valider l'état (Disponible ou Indisponible)
        String etat = etatField.getText().trim();
        if (etat.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "L'état ne doit pas être vide!");
            return false;
        }

        if (!etat.equalsIgnoreCase("Disponible") && !etat.equalsIgnoreCase("Indisponible")) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "L'état doit être 'Disponible' ou 'Indisponible'!");
            return false;
        }

        // Valider l'image
        if (selectedImagePath == null || selectedImagePath.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "Vous devez sélectionner une image!");
            return false;
        }

        // Valider la destination
        if (destinationCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de validation",
                    "Vous devez sélectionner une destination!");
            return false;
        }

        return true;
    }

    /**
     * Affiche une alerte à l'utilisateur
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Réinitialise tous les champs
     */
    private void clearFields() {
        marqueField.clear();
        prixField.clear();
        etatField.clear();
        destinationCombo.setValue(null);
        selectedImagePath = null;
        imagePreview.setImage(null);
        imageNameLabel.setText("");
    }
}