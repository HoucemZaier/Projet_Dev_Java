package Controllers;

import Models.Activite;
import Services.ServiceActivite;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.Time;

public class UpdateActiviteController {

    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField lieuField;
    @FXML private TextField prixField;
    @FXML private TextField excursionField;
    @FXML private TextField destinationField;

    private ServiceActivite service = new ServiceActivite();

    public void setActivite(Activite a) {
        idField.setText(String.valueOf(a.getIdActivite()));
        nomField.setText(a.getNom());
        descriptionField.setText(a.getDescription());
        datePicker.setValue(a.getDateActivite().toLocalDate());
        heureField.setText(a.getHeureActivite().toString());
        lieuField.setText(a.getLieu());
        prixField.setText(String.valueOf(a.getPrix()));
        excursionField.setText(String.valueOf(a.getIdExcursion()));
        destinationField.setText(String.valueOf(a.getIdDestination()));
    }

    @FXML
    private void handleUpdate() {
        try {
            Activite a = new Activite();
            a.setIdActivite(Integer.parseInt(idField.getText()));
            a.setNom(nomField.getText());
            a.setDescription(descriptionField.getText());
            a.setDateActivite(Date.valueOf(datePicker.getValue()));
            a.setHeureActivite(Time.valueOf(heureField.getText()));
            a.setLieu(lieuField.getText());
            a.setPrix(Double.parseDouble(prixField.getText()));
            a.setIdExcursion(Integer.parseInt(excursionField.getText()));
            a.setIdDestination(Integer.parseInt(destinationField.getText()));

            service.modifier(a);

            showAlert("Succès", "Activité mise à jour avec succès !");
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
