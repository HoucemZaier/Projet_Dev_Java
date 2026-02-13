package Controllers;

import Models.Activite;
import Services.ServiceActivite;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

public class CreateActiviteController {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField lieuField;
    @FXML private TextField prixField;
    @FXML private ComboBox<Integer> excursionComboBox;
    @FXML private ComboBox<Integer> destinationComboBox;

    private ServiceActivite service = new ServiceActivite();

    @FXML
    private void initialize() {
        // Charger les IDs existants pour sécuriser les FK
        List<Integer> excursionIds = service.getAllExcursionIds();
        excursionComboBox.setItems(FXCollections.observableArrayList(excursionIds));

        List<Integer> destinationIds = service.getAllDestinationIds();
        destinationComboBox.setItems(FXCollections.observableArrayList(destinationIds));
    }

    @FXML
    private void handleSave() {
        try {
            // Vérifier que l'utilisateur a bien sélectionné un ID valide
            Integer selectedExcursion = excursionComboBox.getValue();
            Integer selectedDestination = destinationComboBox.getValue();

            if (selectedExcursion == null || selectedDestination == null) {
                showAlert("Erreur", "Veuillez sélectionner une excursion et une destination !");
                return;
            }

            Activite a = new Activite();
            a.setNom(nomField.getText());
            a.setDescription(descriptionField.getText());
            a.setDateActivite(Date.valueOf(datePicker.getValue()));
            a.setHeureActivite(Time.valueOf(heureField.getText()));
            a.setLieu(lieuField.getText());
            a.setPrix(Double.parseDouble(prixField.getText()));
            a.setIdExcursion(selectedExcursion);
            a.setIdDestination(selectedDestination);

            service.ajouter(a);

            showAlert("Succès", "Activité ajoutée avec succès !");
            ((Stage) nomField.getScene().getWindow()).close();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nomField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
