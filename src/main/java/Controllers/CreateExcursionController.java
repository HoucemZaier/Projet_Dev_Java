package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CreateExcursionController implements Initializable {

    @FXML private TextField titreField;
    @FXML private Label titreError;

    @FXML private TextField destinationField;
    @FXML private Label destinationError;

    @FXML private DatePicker dateDepartPicker;
    @FXML private Label dateDepartError;

    @FXML private DatePicker dateRetourPicker;
    @FXML private Label dateRetourError;

    @FXML private TextField prixField;
    @FXML private Label prixError;

    @FXML private TextField nbPlacesField;
    @FXML private Label nbPlacesError;

    @FXML private ComboBox<String> statutComboBox;
    @FXML private Label statutError;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private Label messageLabel;

    private final ServiceExcursion service = new ServiceExcursion();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation ComboBox
        statutComboBox.setItems(FXCollections.observableArrayList("ouverte", "complète", "annulée"));

        // Désactiver les dates passées
        dateDepartPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) setDisable(true);
            }
        });

        dateRetourPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate depart = dateDepartPicker.getValue() != null ? dateDepartPicker.getValue() : LocalDate.now();
                if (date.isBefore(depart)) {
                    setDisable(true);
                    setStyle("-fx-background-color:#f0f0f0;");
                }
            }
        });

        // Mettre à jour la dateRetour minimale lorsque dateDepart change
        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (dateRetourPicker.getValue() != null && newVal != null && dateRetourPicker.getValue().isBefore(newVal)) {
                dateRetourPicker.setValue(newVal);
            }
            dateRetourPicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date.isBefore(newVal != null ? newVal : LocalDate.now())) {
                        setDisable(true);
                        setStyle("-fx-background-color:#f0f0f0;");
                    }
                }
            });
        });

        setupListeners();
        setupSaveButtonBinding();
    }

    private void setupListeners() {
        // TITRE : obligatoire + lettres uniquement
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                titreError.setText("Le titre est obligatoire");
                titreError.setVisible(true);
            } else if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]+")) { // lettres et espaces seulement
                titreError.setText("Le titre doit contenir uniquement des lettres");
                titreError.setVisible(true);
            } else {
                titreError.setVisible(false);
            }
        });

        // DESTINATION : obligatoire + lettres uniquement
        destinationField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                destinationError.setText("La destination est obligatoire");
                destinationError.setVisible(true);
            } else if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                destinationError.setText("La destination doit contenir uniquement des lettres");
                destinationError.setVisible(true);
            } else {
                destinationError.setVisible(false);
            }
        });

        // PRIX : obligatoire + nombre > 0
        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) prixField.setText(oldVal);
            boolean invalid = newVal.trim().isEmpty() || Double.parseDouble(newVal.isEmpty() ? "0" : newVal) <= 0;
            prixError.setText("Prix invalide ou ≤ 0");
            prixError.setVisible(invalid);
        });

        // NB PLACES : obligatoire + entier > 0
        nbPlacesField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) nbPlacesField.setText(oldVal);
            boolean invalid = newVal.trim().isEmpty() || Integer.parseInt(newVal.isEmpty() ? "0" : newVal) <= 0;
            nbPlacesError.setText("Nombre de places invalide ou ≤ 0");
            nbPlacesError.setVisible(invalid);
        });

        // STATUT
        statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> statutError.setVisible(newVal == null));

        // DATES
        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> dateDepartError.setVisible(newVal == null));
        dateRetourPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (dateRetourPicker.getValue() != null && dateDepartPicker.getValue() != null && dateRetourPicker.getValue().isBefore(dateDepartPicker.getValue())) {
                dateRetourError.setText("La date de retour doit être ≥ date de départ");
                dateRetourError.setVisible(true);
            } else dateRetourError.setVisible(newVal == null);
        });
    }

    private void setupSaveButtonBinding() {
        saveButton.disableProperty().bind(
                titreField.textProperty().isEmpty()
                        .or(destinationField.textProperty().isEmpty())
                        .or(dateDepartPicker.valueProperty().isNull())
                        .or(dateRetourPicker.valueProperty().isNull())
                        .or(prixField.textProperty().isEmpty())
                        .or(nbPlacesField.textProperty().isEmpty())
                        .or(statutComboBox.valueProperty().isNull())
        );
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) return;

        try {
            Excursion e = new Excursion();
            e.setTitre(titreField.getText().trim());
            e.setDestination(destinationField.getText().trim());
            e.setDateDepart(Date.valueOf(dateDepartPicker.getValue()));
            e.setDateRetour(Date.valueOf(dateRetourPicker.getValue()));
            e.setPrix(Double.parseDouble(prixField.getText()));
            e.setNbPlaces(Integer.parseInt(nbPlacesField.getText()));
            e.setStatut(statutComboBox.getValue());

            service.ajouter(e);

            showSuccess("Excursion ajoutée avec succès !");
            new Thread(() -> {
                try { Thread.sleep(2000); Platform.runLater(this::closeWindow); }
                catch (InterruptedException ignored) {}
            }).start();

        } catch (Exception ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean valid = true;

        // Vérifications complètes
        if (titreField.getText().trim().isEmpty() || !titreField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) {
            titreError.setVisible(true); valid = false;
        }
        if (destinationField.getText().trim().isEmpty() || !destinationField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) {
            destinationError.setVisible(true); valid = false;
        }
        if (dateDepartPicker.getValue() == null) { dateDepartError.setVisible(true); valid = false; }
        if (dateRetourPicker.getValue() == null || (dateDepartPicker.getValue() != null && dateRetourPicker.getValue().isBefore(dateDepartPicker.getValue()))) {
            dateRetourError.setText("La date de retour doit être ≥ date de départ");
            dateRetourError.setVisible(true); valid = false;
        }
        if (prixField.getText().trim().isEmpty() || Double.parseDouble(prixField.getText()) <= 0) { prixError.setVisible(true); valid = false; }
        if (nbPlacesField.getText().trim().isEmpty() || Integer.parseInt(nbPlacesField.getText()) <= 0) { nbPlacesError.setVisible(true); valid = false; }
        if (statutComboBox.getValue() == null) { statutError.setVisible(true); valid = false; }

        return valid;
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText("✓ " + message);
        messageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }
}
