package controllers;

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
    private void onHotelClicked(ActionEvent event) {
        loadView("/listhotel.fxml");
    }

    @FXML
    private void onChambreClicked(ActionEvent event) {
        loadView("/listchambre.fxml");
    }

    @FXML
    private void onStatistiquesClicked(ActionEvent event) {
        loadView("/statistiques.fxml");
    }

    @FXML
    private void handleOverview(ActionEvent event) {
        if (chartContainer == null) return;

        // On vide juste le conteneur pour revenir à l'état initial du dashboard
        chartContainer.getChildren().clear();
    }



    private void loadView(String fxmlPath) {
        if (chartContainer == null) {
            System.err.println("❌ Erreur : 'chartContainer' est null.");
            return;
        }

        try {
            URL fxmlLocation = getClass().getResource(fxmlPath);
            if (fxmlLocation == null) {
                throw new IOException("Fichier FXML introuvable : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent view = loader.load();
            chartContainer.getChildren().setAll(view);
            HBox.setHgrow(view, javafx.scene.layout.Priority.ALWAYS);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la vue : " + e.getMessage());
            e.printStackTrace();
        }
    }
}