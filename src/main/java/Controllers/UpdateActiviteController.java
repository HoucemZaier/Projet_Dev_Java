package Controllers;

import Models.Activite;
import Models.Excursion;
import Services.ServiceActivite;
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

    @FXML private ComboBox<Integer> destinationComboBox;
    @FXML private Label destinationError;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private Label messageLabel;

    private ServiceActivite service;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        service = new ServiceActivite();

        loadComboBoxes();
        setupDatePicker();
        setupListeners();
        setupSaveButtonBinding();
    }

    private void loadComboBoxes() {
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

        List<Integer> destinationIds = service.getAllDestinationIds();
        destinationComboBox.setItems(FXCollections.observableArrayList(destinationIds));
    }

    private void setupDatePicker() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color:#f0f0f0;");
                }
            }
        });
    }

    public void setActivite(Activite a) {
        idField.setText(String.valueOf(a.getIdActivite()));
        nomField.setText(a.getNom());
        descriptionField.setText(a.getDescription());
        datePicker.setValue(a.getDateActivite().toLocalDate());
        heureField.setText(a.getHeureActivite().toString());
        lieuField.setText(a.getLieu());
        prixField.setText(String.valueOf(a.getPrix()));

        // Sélectionner excursion par objet
        for (Excursion e : excursionComboBox.getItems()) {
            if (e.getIdExcursion() == a.getIdExcursion()) {
                excursionComboBox.setValue(e);
                break;
            }
        }

        // Sélectionner destination par ID
        destinationComboBox.setValue(a.getIdDestination());
    }

    private void setupListeners() {
        // Nom
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                nomError.setText("Le nom est obligatoire");
                nomError.setVisible(true);
            } else if (!newVal.matches("[a-zA-Z\\s]+")) {
                nomError.setText("Le nom doit contenir uniquement des lettres");
                nomError.setVisible(true);
            } else {
                nomError.setVisible(false);
            }
        });

        // Description
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                descriptionError.setText("La description est obligatoire");
                descriptionError.setVisible(true);
            } else {
                descriptionError.setVisible(false);
            }
        });

        // Lieu
        lieuField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                lieuError.setText("Le lieu est obligatoire");
                lieuError.setVisible(true);
            } else if (!newVal.matches("[a-zA-Z\\s]+")) {
                lieuError.setText("Le lieu doit contenir uniquement des lettres");
                lieuError.setVisible(true);
            } else {
                lieuError.setVisible(false);
            }
        });

        // Prix
        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) prixField.setText(oldVal);
            try {
                double p = Double.parseDouble(prixField.getText());
                if (p <= 0) { prixError.setText("Prix invalide ou <= 0"); prixError.setVisible(true);}
                else prixError.setVisible(false);
            } catch (Exception e) {
                prixError.setText("Prix invalide"); prixError.setVisible(true);
            }
        });

        // Heure
        heureField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (heureField.getText().trim().isEmpty()) {
                    heureError.setText("Veuillez saisir une heure");
                    heureError.setVisible(true);
                } else {
                    try {
                        LocalTime.parse(heureField.getText());
                        heureError.setVisible(false);
                    } catch (Exception e) {
                        heureError.setText("Format heure invalide (HH:mm:ss)");
                        heureError.setVisible(true);
                    }
                }
            }
        });

        // Excursion
        excursionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            excursionError.setVisible(newVal == null);
        });

        // Destination
        destinationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            destinationError.setVisible(newVal == null);
        });
    }

    private void setupSaveButtonBinding() {
        saveButton.disableProperty().bind(
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
            a.setIdDestination(destinationComboBox.getValue());

            service.modifier(a);

            showSuccess("Activité mise à jour avec succès !");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::closeWindow);
                } catch (InterruptedException ignored) {}
            }).start();

        } catch (Exception e) {
            showError("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean valid = true;

        if (nomField.getText().trim().isEmpty() || !nomField.getText().matches("[a-zA-Z\\s]+")) { nomError.setVisible(true); valid=false; }
        if (descriptionField.getText().trim().isEmpty()) { descriptionError.setVisible(true); valid=false; }
        if (datePicker.getValue() == null) { dateError.setText("Date obligatoire"); dateError.setVisible(true); valid=false; }
        if (heureField.getText().trim().isEmpty()) { heureError.setText("Veuillez saisir une heure"); heureError.setVisible(true); valid=false; }
        else { try { LocalTime.parse(heureField.getText()); heureError.setVisible(false);} catch(Exception e){heureError.setText("Format heure invalide"); heureError.setVisible(true); valid=false;} }
        if (lieuField.getText().trim().isEmpty() || !lieuField.getText().matches("[a-zA-Z\\s]+")) { lieuError.setVisible(true); valid=false; }
        try { double p = Double.parseDouble(prixField.getText()); if(p<=0){prixError.setVisible(true); valid=false;} else prixError.setVisible(false);} catch(Exception e){prixError.setVisible(true); valid=false;}
        if(excursionComboBox.getValue() == null){excursionError.setVisible(true); valid=false;}
        if(destinationComboBox.getValue() == null){destinationError.setVisible(true); valid=false;}

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
