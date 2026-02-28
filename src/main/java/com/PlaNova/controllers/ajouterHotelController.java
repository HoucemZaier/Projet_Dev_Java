package com.PlaNova.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.PlaNova.models.Hotel;
import com.PlaNova.services.GroqService;
import com.PlaNova.services.ServiceHotel;
import com.PlaNova.models.Destination;
import com.PlaNova.utils.MyDatabase;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;

public class ajouterHotelController {

    @FXML
    private TextField nomHotelField;
    @FXML
    private Label nomHotelError;

    @FXML
    private TextField villeField;
    @FXML
    private Label villeError;

    @FXML
    private TextField adresseField;
    @FXML
    private Label adresseError;

    @FXML
    private ComboBox<Integer> etoilesComboBox;
    @FXML
    private Label etoilesError;

    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label descriptionError;

    @FXML
    private TextField imagePathField;
    @FXML
    private Label imageError;

    @FXML
    private ComboBox<Destination> destinationCombo;
    @FXML
    private Label destinationError;

    @FXML
    private Button annulerBtn;
    @FXML
    private Button ajouterBtn;

    // Bouton Groq IA - référencé depuis le FXML
    @FXML
    private Button groqBtn;

    private ServiceHotel serviceHotel;
    private GroqService groqService;

    private TableView<Hotel> tableViewHotels;

    public void setTableViewHotels(TableView<Hotel> tableView) {
        this.tableViewHotels = tableView;
    }

    @FXML
    public void initialize() {
        serviceHotel = new ServiceHotel();
        groqService = new GroqService();
        etoilesComboBox.getItems().addAll(1, 2, 3, 4, 5);
        loadDestinations();
        setupValidationListeners();

        // Tooltip sur le bouton Groq
        if (groqBtn != null) {
            Tooltip tip = new Tooltip("Générer une description automatique avec l'IA Groq (LLaMA).\n" +
                    "Remplissez d'abord le nom, la ville et les étoiles.");
            groqBtn.setTooltip(tip);
        }
    }

    private void setupValidationListeners() {
        nomHotelField.textProperty().addListener((obs, o, n) -> validateNomField());
        villeField.textProperty().addListener((obs, o, n) -> validateVilleField());
        adresseField.textProperty().addListener((obs, o, n) -> validateAdresseField());
        descriptionArea.textProperty().addListener((obs, o, n) -> validateDescriptionField());
        etoilesComboBox.valueProperty().addListener((obs, o, n) -> validateEtoilesField());
        destinationCombo.valueProperty().addListener((obs, o, n) -> validateDestinationField());
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

    /**
     * Génère automatiquement une description via l'API Groq (LLaMA)
     */
    @FXML
    private void handleGenererDescriptionIA() {
        String nom = nomHotelField.getText().trim();
        String ville = villeField.getText().trim();
        Integer etoiles = etoilesComboBox.getValue();

        // Validation des champs nécessaires
        if (nom.isEmpty() || ville.isEmpty() || etoiles == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir le nom de l'hôtel, la ville et le nombre d'étoiles\n" +
                            "avant de générer une description automatique.");
            return;
        }

        if (!groqService.isApiKeyConfigured()) {
            showAlert(Alert.AlertType.INFORMATION, "Clé API requise",
                    "Pour utiliser la génération IA, configurez votre clé API Groq dans :\n" +
                            "src/main/java/services/GroqService.java\n\n" +
                            "Obtenez une clé gratuite sur : https://console.groq.com/keys");
            return;
        }

        // Désactiver le bouton pendant la génération
        groqBtn.setDisable(true);
        groqBtn.setText("⏳ Génération...");
        descriptionArea.setText("🤖 L'IA génère une description pour votre hôtel...");

        // Appel API en thread séparé pour ne pas bloquer l'UI
        Thread apiThread = new Thread(() -> {
            try {
                String description = groqService.genererDescriptionHotel(nom, ville, etoiles);
                Platform.runLater(() -> {
                    descriptionArea.setText(description);
                    groqBtn.setDisable(false);
                    groqBtn.setText("✨ Générer avec IA");
                    validateDescriptionField();
                    showAlert(Alert.AlertType.INFORMATION, "✅ Description générée",
                            "La description a été générée avec succès par l'IA Groq (LLaMA) !\n" +
                                    "Vous pouvez la modifier avant d'enregistrer.");
                });
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    descriptionArea.setText("");
                    groqBtn.setDisable(false);
                    groqBtn.setText("✨ Générer avec IA");
                    showAlert(Alert.AlertType.ERROR, "Erreur API Groq",
                            "Impossible de contacter l'API Groq.\n" +
                                    "Vérifiez votre connexion et votre clé API.\n\n" +
                                    "Erreur: " + e.getMessage());
                });
            }
        });
        apiThread.setDaemon(true);
        apiThread.start();
    }

    @FXML
    private void validateNomField() {
        String nom = nomHotelField.getText().trim();
        if (nom.isEmpty()) {
            showFieldError(nomHotelField, nomHotelError, "Le nom de l'hôtel est obligatoire");
            return;
        }
        if (nom.matches(".*\\d.*")) {
            showFieldError(nomHotelField, nomHotelError, "Le nom ne doit pas contenir de chiffres");
            return;
        }
        if (nom.length() < 5) {
            showFieldError(nomHotelField, nomHotelError, "Le nom doit contenir au moins 5 caractères");
            return;
        }
        if (nom.length() > 100) {
            showFieldError(nomHotelField, nomHotelError, "Le nom ne peut pas dépasser 100 caractères");
            return;
        }
        hideFieldError(nomHotelField, nomHotelError);
    }

    @FXML
    private void validateVilleField() {
        String ville = villeField.getText().trim();
        if (ville.isEmpty()) {
            showFieldError(villeField, villeError, "La ville est obligatoire");
            return;
        }
        if (ville.matches(".*\\d.*")) {
            showFieldError(villeField, villeError, "La ville ne doit pas contenir de chiffres");
            return;
        }
        if (ville.length() < 3) {
            showFieldError(villeField, villeError, "La ville doit contenir au moins 3 caractères");
            return;
        }
        hideFieldError(villeField, villeError);
    }

    @FXML
    private void validateAdresseField() {
        String adresse = adresseField.getText().trim();
        if (adresse.isEmpty()) {
            showFieldError(adresseField, adresseError, "L'adresse est obligatoire");
            return;
        }
        if (adresse.length() < 10) {
            showFieldError(adresseField, adresseError, "L'adresse doit contenir au moins 10 caractères");
            return;
        }
        hideFieldError(adresseField, adresseError);
    }

    @FXML
    private void validateEtoilesField() {
        Integer etoiles = etoilesComboBox.getValue();
        if (etoiles == null) {
            showFieldError(etoilesComboBox, etoilesError, "Le nombre d'étoiles est obligatoire");
            return;
        }
        hideFieldError(etoilesComboBox, etoilesError);
    }

    @FXML
    private void validateDescriptionField() {
        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            showFieldError(descriptionArea, descriptionError, "La description est obligatoire");
            return;
        }
        if (description.length() < 20) {
            showFieldError(descriptionArea, descriptionError, "La description doit contenir au moins 20 caractères");
            return;
        }
        hideFieldError(descriptionArea, descriptionError);
    }

    @FXML
    private void validateDestinationField() {
        Destination destination = destinationCombo.getValue();
        if (destination == null) {
            showFieldError(destinationCombo, destinationError, "La destination est obligatoire");
            return;
        }
        hideFieldError(destinationCombo, destinationError);
    }

    @FXML
    private void handleParcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        Stage stage = (Stage) imagePathField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
            hideFieldError(imagePathField, imageError);
        }
    }

    @FXML
    private void handleAjouterHotel() {
        validateNomField();
        validateVilleField();
        validateAdresseField();
        validateEtoilesField();
        validateDescriptionField();
        validateDestinationField();

        if (nomHotelError.isVisible() || villeError.isVisible() || adresseError.isVisible() ||
                etoilesError.isVisible() || descriptionError.isVisible() || destinationError.isVisible()) {
            showAlert(Alert.AlertType.WARNING, "Formulaire incomplet",
                    "Veuillez corriger les erreurs avant de soumettre le formulaire.");
            return;
        }

        Hotel hotel = new Hotel();
        hotel.setNomHotel(nomHotelField.getText().trim());
        hotel.setVille(villeField.getText().trim());
        hotel.setAdresse(adresseField.getText().trim());
        hotel.setNombreEtoile(etoilesComboBox.getValue());
        hotel.setDescription(descriptionArea.getText().trim());
        hotel.setImage(imagePathField.getText().trim().replace("\\", "/"));
        hotel.setIdDestination(destinationCombo.getValue().getId_destination());

        try {
            serviceHotel.ajouter(hotel);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'hôtel a été ajouté avec succès !");
            if (tableViewHotels != null) {
                tableViewHotels.setItems(FXCollections.observableArrayList(serviceHotel.recuperer()));
            }
            handleAnnuler();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'hôtel : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        stage.close();
    }

    private void showFieldError(Control field, Label errorLabel, String message) {
        field.setStyle(
                "-fx-border-color: #dc2626; -fx-border-width: 1.5; -fx-border-radius: 10; -fx-background-radius: 10;");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideFieldError(Control field, Label errorLabel) {
        field.setStyle("-fx-background-radius: 10; -fx-border-color: transparent;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
