package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLDataException;

public class DeleteExcursionController {

    @FXML private Label titreLabel;

    private Excursion excursion;
    private final ServiceExcursion service = new ServiceExcursion();

    public void setExcursion(Excursion e) {
        this.excursion = e;
        titreLabel.setText(e.getTitre());
    }

    @FXML
    private void handleDelete() {
        try {
            service.supprimer(excursion.getIdExcursion());
            Stage stage = (Stage) titreLabel.getScene().getWindow();
            stage.close();
        } catch (SQLDataException ex) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).show();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titreLabel.getScene().getWindow();
        stage.close();
    }
}
