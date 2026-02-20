package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import Services.ServiceDestination;
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

public class UpdateExcursionController implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField titreField;
    @FXML private Label titreError;

    @FXML private ComboBox<String> destinationComboBox;
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

    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    @FXML private Label messageLabel;

    private final ServiceExcursion serviceExcursion = new ServiceExcursion();
    private final ServiceDestination serviceDestination = new ServiceDestination();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statutComboBox.setItems(FXCollections.observableArrayList("ouverte", "complète", "annulée"));

        // Charger les destinations depuis ServiceDestination
        try {
            destinationComboBox.setItems(FXCollections.observableArrayList(serviceDestination.getAllDestinationNames()));
        } catch (Exception e) {
            showError("Erreur chargement destinations : " + e.getMessage());
        }

        // Dates
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
        setupUpdateButtonBinding();
    }

    public void setExcursion(Excursion e) {
        if (e != null) {
            idField.setText(String.valueOf(e.getIdExcursion()));
            titreField.setText(e.getTitre());
            destinationComboBox.setValue(e.getNomDestination()); // <-- utiliser nomDestination
            dateDepartPicker.setValue(e.getDateDepart().toLocalDate());
            dateRetourPicker.setValue(e.getDateRetour().toLocalDate());
            prixField.setText(String.valueOf(e.getPrix()));
            nbPlacesField.setText(String.valueOf(e.getNbPlaces()));
            statutComboBox.setValue(e.getStatut());
        }
    }

    private void setupListeners() {
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) { titreError.setText("Le titre est obligatoire"); titreError.setVisible(true); }
            else if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]+")) { titreError.setText("Le titre doit contenir uniquement des lettres"); titreError.setVisible(true); }
            else { titreError.setVisible(false); }
        });

        destinationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            destinationError.setText("La destination est obligatoire");
            destinationError.setVisible(newVal == null || newVal.trim().isEmpty());
        });

        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) prixField.setText(oldVal);
            boolean invalid = newVal.trim().isEmpty() || !isDoubleValid(newVal);
            prixError.setText("Prix invalide ou ≤ 0");
            prixError.setVisible(invalid);
        });

        nbPlacesField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) nbPlacesField.setText(oldVal);
            boolean invalid = newVal.trim().isEmpty() || !isIntValid(newVal);
            nbPlacesError.setText("Nombre de places invalide ou ≤ 0");
            nbPlacesError.setVisible(invalid);
        });

        statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> statutError.setVisible(newVal == null));

        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> dateDepartError.setVisible(newVal == null));
        dateRetourPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (dateRetourPicker.getValue() != null && dateDepartPicker.getValue() != null
                    && dateRetourPicker.getValue().isBefore(dateDepartPicker.getValue())) {
                dateRetourError.setText("La date de retour doit être ≥ date de départ");
                dateRetourError.setVisible(true);
            } else dateRetourError.setVisible(newVal == null);
        });
    }

    private void setupUpdateButtonBinding() {
        updateButton.disableProperty().bind(
                titreField.textProperty().isEmpty()
                        .or(destinationComboBox.valueProperty().isNull())
                        .or(dateDepartPicker.valueProperty().isNull())
                        .or(dateRetourPicker.valueProperty().isNull())
                        .or(prixField.textProperty().isEmpty())
                        .or(nbPlacesField.textProperty().isEmpty())
                        .or(statutComboBox.valueProperty().isNull())
        );
    }

    @FXML
    private void handleUpdate() {
        if (!validateInputs()) return;

        try {
            Excursion e = new Excursion();
            e.setIdExcursion(Integer.parseInt(idField.getText()));
            e.setTitre(titreField.getText().trim());
            e.setNomDestination(destinationComboBox.getValue()); // <-- utiliser ComboBox
            e.setDateDepart(Date.valueOf(dateDepartPicker.getValue()));
            e.setDateRetour(Date.valueOf(dateRetourPicker.getValue()));
            e.setPrix(Double.parseDouble(prixField.getText()));
            e.setNbPlaces(Integer.parseInt(nbPlacesField.getText()));
            e.setStatut(statutComboBox.getValue());

            serviceExcursion.modifier(e);

            showSuccess("Excursion mise à jour avec succès !");
            new Thread(() -> {
                try { Thread.sleep(1500); Platform.runLater(this::closeWindow); }
                catch (InterruptedException ignored) {}
            }).start();
        } catch (Exception ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean valid = true;
        if (titreField.getText().trim().isEmpty() || !titreField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) { titreError.setVisible(true); valid=false; }
        if (destinationComboBox.getValue() == null || destinationComboBox.getValue().trim().isEmpty()) { destinationError.setVisible(true); valid=false; }
        if (dateDepartPicker.getValue() == null) { dateDepartError.setVisible(true); valid=false; }
        if (dateRetourPicker.getValue() == null || dateRetourPicker.getValue().isBefore(dateDepartPicker.getValue())) { dateRetourError.setVisible(true); valid=false; }
        if (!isDoubleValid(prixField.getText())) { prixError.setVisible(true); valid=false; }
        if (!isIntValid(nbPlacesField.getText())) { nbPlacesError.setVisible(true); valid=false; }
        if (statutComboBox.getValue() == null) { statutError.setVisible(true); valid=false; }
        return valid;
    }

    private boolean isDoubleValid(String s) {
        try { return Double.parseDouble(s) > 0; } catch (Exception e) { return false; }
    }

    private boolean isIntValid(String s) {
        try { return Integer.parseInt(s) > 0; } catch (Exception e) { return false; }
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