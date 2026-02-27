package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLDataException;

public class DeleteExcursionController {

    @FXML private Label titreLabel;
    @FXML private Label statutLabel;
    @FXML private Label messageLabel;
    @FXML private Button deleteButton;

    private Excursion excursion;
    private final ServiceExcursion service = new ServiceExcursion();

    public void setExcursion(Excursion e) {
        this.excursion = e;
        if (e != null) {
            titreLabel.setText(e.getTitre());
            statutLabel.setText("Statut : " + e.getStatut());

            // Désactiver le bouton si l'excursion est ouverte
            String statut = e.getStatut().trim().toLowerCase();
            if (statut.equals("ouverte")) {
                deleteButton.setDisable(true);
                messageLabel.setText("Impossible de supprimer une excursion ouverte !");
                messageLabel.setVisible(true);
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (excursion == null) {
            new Alert(Alert.AlertType.ERROR, "Aucune excursion sélectionnée !").show();
            return;
        }

        String statut = excursion.getStatut().trim().toLowerCase();
        if (statut.equals("ouverte")) {
            messageLabel.setText("Vous ne pouvez pas supprimer une excursion ouverte !");
            messageLabel.setVisible(true);
            return;
        }

        try {
            service.supprimer(excursion.getIdExcursion());
            new Alert(Alert.AlertType.INFORMATION, "Excursion supprimée avec succès !").show();

            Stage stage = (Stage) titreLabel.getScene().getWindow();
            stage.close();

        } catch (SQLDataException ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur inattendue : " + ex.getMessage()).show();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titreLabel.getScene().getWindow();
        stage.close();
    }
}
