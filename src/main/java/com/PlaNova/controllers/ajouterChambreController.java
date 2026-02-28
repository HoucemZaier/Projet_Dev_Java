package com.PlaNova.controllers;

import javafx.application.Platform;
import com.PlaNova.models.Chambre;
import com.PlaNova.models.Hotel;
import com.PlaNova.services.ServiceChambre;
import com.PlaNova.services.ServiceHotel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.List;
import java.util.ResourceBundle;

public class ajouterChambreController implements Initializable {

    @FXML
    private Label titleLabel;
    @FXML
    private ComboBox<Hotel> hotelComboBox;
    @FXML
    private TextField numeroChambreField;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private Spinner<Integer> capaciteSpinner;
    @FXML
    private TextField prixField;
    @FXML
    private ComboBox<String> statutComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private CheckBox wifiCheckBox;
    @FXML
    private CheckBox tvCheckBox;
    @FXML
    private CheckBox climatisationCheckBox;
    @FXML
    private CheckBox miniBarCheckBox;
    @FXML
    private CheckBox balconCheckBox;
    @FXML
    private CheckBox coffreCheckBox;
    @FXML
    private CheckBox telephoneCheckBox;
    @FXML
    private CheckBox chauffageCheckBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button clearButton;
    @FXML
    private Label errorLabel;
    @FXML
    private VBox equipementsContainer;

    private ServiceChambre serviceChambre;
    private ServiceHotel serviceHotel;
    private ObservableList<Hotel> hotelList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceChambre = new ServiceChambre();
        serviceHotel = new ServiceHotel();

        // Initialiser les composants
        initializeComboBoxes();
        initializeSpinner();
        setupListeners();
        setupValidation();
        setDefaultValues();
    }

    private void initializeComboBoxes() {
        // Charger les hôtels
        try {
            List<Hotel> hotels = serviceHotel.recuperer();
            hotelList = FXCollections.observableArrayList(hotels);
            hotelComboBox.setItems(hotelList);

            // Personnaliser l'affichage des hôtels
            hotelComboBox.setCellFactory(param -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    if (empty || hotel == null) {
                        setText(null);
                    } else {
                        setText(hotel.getNomHotel() + " - " + hotel.getNomHotel());
                    }
                }
            });

            hotelComboBox.setButtonCell(new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    if (empty || hotel == null) {
                        setText(null);
                    } else {
                        setText(hotel.getNomHotel());
                    }
                }
            });

        } catch (SQLDataException e) {
            showError("Erreur de chargement des hôtels: " + e.getMessage());
        }

        // Types de chambres
        ObservableList<String> types = FXCollections.observableArrayList(
                "Simple", "Double", "Suite", "Familiale", "Deluxe", "Présidentielle"
        );
        typeComboBox.setItems(types);
        typeComboBox.setValue("Double"); // Valeur par défaut

        // Statuts
        ObservableList<String> statuts = FXCollections.observableArrayList(
                "Disponible", "Occupée", "En maintenance", "Réservée"
        );
        statutComboBox.setItems(statuts);
        statutComboBox.setValue("Disponible"); // Valeur par défaut
    }

    private void initializeSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2);
        capaciteSpinner.setValueFactory(valueFactory);
        capaciteSpinner.setEditable(true);
    }

    private void setupListeners() {
        // Validation du prix (uniquement nombres avec décimales)
        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                return;
            }
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                prixField.setText(oldValue);
            }
        });

        // Formatage automatique du prix à la perte de focus
        prixField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Perte de focus
                formatPrix();
            }
        });
    }

    private void setupValidation() {
        // Désactiver le bouton sauvegarder si les champs obligatoires sont vides
        saveButton.disableProperty().bind(
                hotelComboBox.valueProperty().isNull()
                        .or(typeComboBox.valueProperty().isNull())
                        .or(prixField.textProperty().isEmpty())
                        .or(statutComboBox.valueProperty().isNull())
        );
    }

    private void setDefaultValues() {
        // Sélectionner le premier hôtel par défaut
        if (hotelList != null && !hotelList.isEmpty()) {
            hotelComboBox.setValue(hotelList.get(0));
        }

        // Générer un numéro de chambre suggéré (optionnel)
        try {
            List<Chambre> chambres = serviceChambre.recuperer();
            int nextId = chambres.stream()
                    .mapToInt(Chambre::getIdChambre)
                    .max()
                    .orElse(0) + 1;
            numeroChambreField.setText("Sera généré automatiquement");
            numeroChambreField.setStyle("-fx-background-color: #f1f5f9;");
            numeroChambreField.setEditable(false);
        } catch (SQLDataException e) {
            numeroChambreField.setText("Automatique");
        }

        // Prix par défaut
        prixField.setText("120.00");
    }

    private void formatPrix() {
        String text = prixField.getText();
        if (text != null && !text.isEmpty()) {
            try {
                double prix = Double.parseDouble(text);
                prixField.setText(String.format("%.2f", prix));
            } catch (NumberFormatException e) {
                // Ignorer
            }
        }
    }
    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Créer une nouvelle chambre
            Chambre nouvelleChambre = new Chambre();
            Hotel selectedHotel = hotelComboBox.getValue();
            nouvelleChambre.setIdHotel(selectedHotel.getIdHotel());
            nouvelleChambre.setNomHotel(selectedHotel.getNomHotel()); // AJOUTER CETTE LIGNE
            nouvelleChambre.setTypeChambre(typeComboBox.getValue());
            nouvelleChambre.setCapacite(capaciteSpinner.getValue());
            nouvelleChambre.setPrixChambre(Double.parseDouble(prixField.getText()));
            nouvelleChambre.setStatutChambre(statutComboBox.getValue());

            // Ajouter la description (si elle existe)
            if (descriptionArea.getText() != null && !descriptionArea.getText().trim().isEmpty()) {
                nouvelleChambre.setDescription(descriptionArea.getText().trim());
            }

            // Ajouter les équipements
            StringBuilder equipements = new StringBuilder();
            if (wifiCheckBox.isSelected()) equipements.append("Wi-Fi,");
            if (tvCheckBox.isSelected()) equipements.append("Télévision,");
            if (climatisationCheckBox.isSelected()) equipements.append("Climatisation,");
            if (miniBarCheckBox.isSelected()) equipements.append("Mini-bar,");
            if (balconCheckBox.isSelected()) equipements.append("Balcon,");
            if (coffreCheckBox.isSelected()) equipements.append("Coffre-fort,");
            if (telephoneCheckBox.isSelected()) equipements.append("Téléphone,");
            if (chauffageCheckBox.isSelected()) equipements.append("Chauffage,");

            String equipementsStr = equipements.length() > 0 ?
                    equipements.substring(0, equipements.length() - 1) : "";

            nouvelleChambre.setEquipement(equipementsStr);

            // Ajouter à la base de données
            serviceChambre.ajouter(nouvelleChambre);

            showSuccess("Chambre ajoutée avec succès!");

            // Fermer la fenêtre après un court délai
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(this::closeWindow);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLDataException e) {
            showError("Erreur lors de l'ajout: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Format de prix invalide");
        }
    }

    @FXML
    private void handleClear() {
        // Réinitialiser le formulaire
        if (hotelList != null && !hotelList.isEmpty()) {
            hotelComboBox.setValue(hotelList.get(0));
        }
        typeComboBox.setValue("Double");
        capaciteSpinner.getValueFactory().setValue(2);
        prixField.setText("120.00");
        statutComboBox.setValue("Disponible");
        descriptionArea.clear();

        // Décocher tous les équipements
        wifiCheckBox.setSelected(false);
        tvCheckBox.setSelected(false);
        climatisationCheckBox.setSelected(false);
        miniBarCheckBox.setSelected(false);
        balconCheckBox.setSelected(false);
        coffreCheckBox.setSelected(false);
        telephoneCheckBox.setSelected(false);
        chauffageCheckBox.setSelected(false);

        showSuccess("Formulaire réinitialisé");
    }

    @FXML
    private void handleCancel() {
        // Demander confirmation avant d'annuler
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler l'ajout");
        alert.setContentText("Êtes-vous sûr de vouloir annuler ? Les données saisies seront perdues.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                closeWindow();
            }
        });
    }

    private boolean validateInputs() {
        errorLabel.setVisible(false);
        errorLabel.setStyle("-fx-text-fill: #ef4444;");

        if (hotelComboBox.getValue() == null) {
            showError("Veuillez sélectionner un hôtel");
            hotelComboBox.requestFocus();
            return false;
        }

        if (typeComboBox.getValue() == null) {
            showError("Veuillez sélectionner un type de chambre");
            typeComboBox.requestFocus();
            return false;
        }

        if (prixField.getText() == null || prixField.getText().trim().isEmpty()) {
            showError("Veuillez saisir un prix");
            prixField.requestFocus();
            return false;
        }

        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix <= 0) {
                showError("Le prix doit être supérieur à 0");
                prixField.requestFocus();
                return false;
            }
            if (prix > 10000) {
                showError("Le prix ne peut pas dépasser 10 000 €");
                prixField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Format de prix invalide. Utilisez un nombre (ex: 120.50)");
            prixField.requestFocus();
            return false;
        }

        if (statutComboBox.getValue() == null) {
            showError("Veuillez sélectionner un statut");
            statutComboBox.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 600;");
        errorLabel.setVisible(true);

        // Animation rapide pour attirer l'attention
        errorLabel.setScaleX(1.05);
        errorLabel.setScaleY(1.05);
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(200), errorLabel);
        st.setToX(1);
        st.setToY(1);
        st.play();
    }

    private void showSuccess(String message) {
        errorLabel.setText("✓ " + message);
        errorLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 600;");
        errorLabel.setVisible(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSelectAllEquipements() {
        wifiCheckBox.setSelected(true);
        tvCheckBox.setSelected(true);
        climatisationCheckBox.setSelected(true);
        miniBarCheckBox.setSelected(true);
        balconCheckBox.setSelected(true);
        coffreCheckBox.setSelected(true);
        telephoneCheckBox.setSelected(true);
        chauffageCheckBox.setSelected(true);
    }

    @FXML
    private void handleClearAllEquipements() {
        wifiCheckBox.setSelected(false);
        tvCheckBox.setSelected(false);
        climatisationCheckBox.setSelected(false);
        miniBarCheckBox.setSelected(false);
        balconCheckBox.setSelected(false);
        coffreCheckBox.setSelected(false);
        telephoneCheckBox.setSelected(false);
        chauffageCheckBox.setSelected(false);
    }

    @FXML
    private void handleQuickPrice() {
        // Récupérer le bouton qui a déclenché l'événement
        Button source = (Button) prixField.getScene().getFocusOwner();
        if (source != null && source instanceof Button) {
            String text = source.getText();
            switch (text) {
                case "Standard":
                    prixField.setText("120.00");
                    break;
                case "Confort":
                    prixField.setText("180.00");
                    break;
                case "Luxe":
                    prixField.setText("250.00");
                    break;
                case "Suite":
                    prixField.setText("400.00");
                    break;
            }
        }
    }
}