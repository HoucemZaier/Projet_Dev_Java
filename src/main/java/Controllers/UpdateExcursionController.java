package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;

public class UpdateExcursionController {

    @FXML private TextField idField;
    @FXML private TextField titreField;
    @FXML private TextField destinationField;
    @FXML private DatePicker dateDepartPicker;
    @FXML private DatePicker dateRetourPicker;
    @FXML private TextField prixField;
    @FXML private TextField nbPlacesField;
    @FXML private ComboBox<String> statutComboBox;

    private final ServiceExcursion service = new ServiceExcursion();

    @FXML
    public void initialize() {
        statutComboBox.getItems().addAll("ouverte", "complète", "annulée");
    }

    public void setExcursion(Excursion e) {
        idField.setText(String.valueOf(e.getIdExcursion()));
        titreField.setText(e.getTitre());
        destinationField.setText(e.getDestination());
        dateDepartPicker.setValue(e.getDateDepart().toLocalDate());
        dateRetourPicker.setValue(e.getDateRetour().toLocalDate());
        prixField.setText(String.valueOf(e.getPrix()));
        nbPlacesField.setText(String.valueOf(e.getNbPlaces()));
        statutComboBox.setValue(e.getStatut());
    }

    @FXML
    private void handleUpdate() {
        try {
            Excursion e = new Excursion();
            e.setIdExcursion(Integer.parseInt(idField.getText()));
            e.setTitre(titreField.getText());
            e.setDestination(destinationField.getText());
            e.setDateDepart(Date.valueOf(dateDepartPicker.getValue()));
            e.setDateRetour(Date.valueOf(dateRetourPicker.getValue()));
            e.setPrix(Double.parseDouble(prixField.getText()));
            e.setNbPlaces(Integer.parseInt(nbPlacesField.getText()));
            e.setStatut(statutComboBox.getValue());

            service.modifier(e);
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.close();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).show();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }
}
