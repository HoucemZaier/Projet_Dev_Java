package Controllers;

import Models.Activite;
import Models.Excursion;
import Services.ServiceActivite;
import Services.ServiceDestination;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class UpdateActiviteController implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private Label nomError;

    @FXML private TextArea descriptionField;
    @FXML private Label descriptionError;

    @FXML private DatePicker datePicker;
    @FXML private Label dateError;

    @FXML private TextField heureField;
    @FXML private Label heureError;

    @FXML private TextField lieuField;
    @FXML private Label lieuError;

    @FXML private TextField prixField;
    @FXML private Label prixError;

    @FXML private ComboBox<Excursion> excursionComboBox;
    @FXML private Label excursionError;

    @FXML private ComboBox<String> destinationComboBox;
    @FXML private Label destinationError;

    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    @FXML private Label messageLabel;

    private final ServiceActivite service = new ServiceActivite();
    private final ServiceDestination serviceDestination = new ServiceDestination();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadExcursions();
        loadDestinations();

        // Désactiver les dates passées
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) setDisable(true);
            }
        });

        setupListeners();
        setupUpdateButtonBinding();
    }

    private void loadExcursions() {
        List<Excursion> excursions = service.getAllExcursions();
        excursionComboBox.setItems(FXCollections.observableArrayList(excursions));

        excursionComboBox.setCellFactory(param -> new ListCell<Excursion>() {
            @Override
            protected void updateItem(Excursion item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitre());
            }
        });

        excursionComboBox.setButtonCell(new ListCell<Excursion>() {
            @Override
            protected void updateItem(Excursion item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitre());
            }
        });
    }

    private void loadDestinations() {
        try {
            List<String> destinations = serviceDestination.getAllDestinationNames();
            destinationComboBox.setItems(FXCollections.observableArrayList(destinations));
        } catch (Exception e) {
            showError("Erreur chargement destinations : " + e.getMessage());
        }
    }

    public void setActivite(Activite a) {
        if (a != null) {
            idField.setText(String.valueOf(a.getIdActivite()));
            nomField.setText(a.getNom());
            descriptionField.setText(a.getDescription());
            datePicker.setValue(a.getDateActivite().toLocalDate());
            heureField.setText(a.getHeureActivite().toString());
            lieuField.setText(a.getLieu());
            prixField.setText(String.valueOf(a.getPrix()));

            // Sélectionner excursion
            for (Excursion e : excursionComboBox.getItems()) {
                if (e.getIdExcursion() == a.getIdExcursion()) {
                    excursionComboBox.setValue(e);
                    break;
                }
            }

            // Sélectionner destination
            if (a.getNomDestination() != null) {
                destinationComboBox.setValue(a.getNomDestination());
            }
        }
    }

    private void setupListeners() {
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) { nomError.setText("Le nom est obligatoire"); nomError.setVisible(true); }
            else { nomError.setVisible(false); }
        });

        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) { descriptionError.setText("La description est obligatoire"); descriptionError.setVisible(true); }
            else { descriptionError.setVisible(false); }
        });

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateError.setText("La date est obligatoire");
            dateError.setVisible(newVal == null);
        });

        heureField.textProperty().addListener((obs, oldVal, newVal) -> {
            try { LocalTime.parse(newVal); heureError.setVisible(false); }
            catch (Exception e) { heureError.setText("Format HH:mm:ss invalide"); heureError.setVisible(true); }
        });

        lieuField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) { lieuError.setText("Le lieu est obligatoire"); lieuError.setVisible(true); }
            else { lieuError.setVisible(false); }
        });

        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            try { double p = Double.parseDouble(newVal); prixError.setVisible(p <= 0); }
            catch (Exception e) { prixError.setText("Prix invalide"); prixError.setVisible(true); }
        });

        excursionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> excursionError.setVisible(newVal == null));
        destinationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> destinationError.setVisible(newVal == null));
    }

    private void setupUpdateButtonBinding() {
        updateButton.disableProperty().bind(
                nomField.textProperty().isEmpty()
                        .or(descriptionField.textProperty().isEmpty())
                        .or(datePicker.valueProperty().isNull())
                        .or(heureField.textProperty().isEmpty())
                        .or(lieuField.textProperty().isEmpty())
                        .or(prixField.textProperty().isEmpty())
                        .or(excursionComboBox.valueProperty().isNull())
                        .or(destinationComboBox.valueProperty().isNull())
        );
    }

    @FXML
    private void handleUpdate() {
        if (!validateInputs()) return;

        try {
            Activite a = new Activite();
            a.setIdActivite(Integer.parseInt(idField.getText()));
            a.setNom(nomField.getText().trim());
            a.setDescription(descriptionField.getText().trim());
            a.setDateActivite(Date.valueOf(datePicker.getValue()));
            a.setHeureActivite(Time.valueOf(heureField.getText()));
            a.setLieu(lieuField.getText().trim());
            a.setPrix(Double.parseDouble(prixField.getText()));
            a.setIdExcursion(excursionComboBox.getValue().getIdExcursion());
            a.setNomDestination(destinationComboBox.getValue());

            service.modifier(a);

            showSuccess("Activité mise à jour avec succès !");
            new Thread(() -> {
                try { Thread.sleep(1500); Platform.runLater(this::closeWindow); }
                catch (InterruptedException ignored) {}
            }).start();
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean valid = true;
        if (nomField.getText().trim().isEmpty()) { nomError.setVisible(true); valid=false; }
        if (descriptionField.getText().trim().isEmpty()) { descriptionError.setVisible(true); valid=false; }
        if (datePicker.getValue() == null) { dateError.setVisible(true); valid=false; }
        try { LocalTime.parse(heureField.getText()); } catch(Exception e){ heureError.setVisible(true); valid=false; }
        if (lieuField.getText().trim().isEmpty()) { lieuError.setVisible(true); valid=false; }
        try { if (Double.parseDouble(prixField.getText()) <= 0) { prixError.setVisible(true); valid=false; } } catch(Exception e){ prixError.setVisible(true); valid=false; }
        if (excursionComboBox.getValue() == null) { excursionError.setVisible(true); valid=false; }
        if (destinationComboBox.getValue() == null) { destinationError.setVisible(true); valid=false; }
        return valid;
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill:#ef4444; -fx-font-weight:bold;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText("✓ " + message);
        messageLabel.setStyle("-fx-text-fill:#10b981; -fx-font-weight:bold;");
        messageLabel.setVisible(true);
    }
}