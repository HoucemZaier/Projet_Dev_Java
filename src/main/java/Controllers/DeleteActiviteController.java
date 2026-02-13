package Controllers;

import Models.Activite;
import Services.ServiceActivite;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.SQLDataException;

public class DeleteActiviteController {

    private Activite activite;   // activité à supprimer
    private ServiceActivite service = new ServiceActivite();

    // Méthode appelée depuis la liste pour injecter l'activité
    public void setActivite(Activite activite) {
        this.activite = activite;
    }

    @FXML
    private void handleDelete() {
        if (activite != null) {
            try {
                service.supprimer(activite.getIdActivite());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Activité supprimée avec succès !");
                alert.showAndWait();

                closeWindow();

            } catch (SQLDataException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Impossible de supprimer l'activité");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }


    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage)
                javafx.stage.Stage.getWindows()
                        .filtered(window -> window.isShowing())
                        .get(0);
        stage.close();
    }
}
