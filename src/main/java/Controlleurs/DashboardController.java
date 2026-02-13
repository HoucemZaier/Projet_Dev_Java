package Controlleurs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private HBox chartContainer;
    @FXML
    private void onPostClicked(ActionEvent event) {
        if (chartContainer == null) {
            System.err.println("❌ Erreur : 'chartContainer' est null.");
            return;
        }

        try {
            URL fxmlLocation = getClass().getResource("/interface/Gestion Fourms_Posts/Gestions_des_Posts.fxml");

            if (fxmlLocation == null) {
                throw new IOException("Fichier FXML introuvable au chemin spécifié.");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent postsInterface = loader.load();

            chartContainer.getChildren().setAll(postsInterface);
            HBox.setHgrow(postsInterface, javafx.scene.layout.Priority.ALWAYS);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la sous-vue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onFourmClicked(ActionEvent event) {
        if (chartContainer == null) {
            System.err.println("❌ Erreur : 'chartContainer' est null.");
            return;
        }

        try {
            URL fxmlLocation = getClass().getResource("/interface/Gestion Fourms_Posts/Gestions_des_Fourms.fxml");

            if (fxmlLocation == null) {
                throw new IOException("Fichier FXML introuvable au chemin spécifié.");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent postsInterface = loader.load();

            chartContainer.getChildren().setAll(postsInterface);
            HBox.setHgrow(postsInterface, javafx.scene.layout.Priority.ALWAYS);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la sous-vue : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
