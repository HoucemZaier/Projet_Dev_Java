package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;

public class CreateExcursionController {

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

    @FXML
    private void handleSave() {
        try {
            Excursion e = new Excursion();
            e.setTitre(titreField.getText());
            e.setDestination(destinationField.getText());
            e.setDateDepart(Date.valueOf(dateDepartPicker.getValue()));
            e.setDateRetour(Date.valueOf(dateRetourPicker.getValue()));
            e.setPrix(Double.parseDouble(prixField.getText()));
            e.setNbPlaces(Integer.parseInt(nbPlacesField.getText()));
            e.setStatut(statutComboBox.getValue());

            service.ajouter(e);
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
