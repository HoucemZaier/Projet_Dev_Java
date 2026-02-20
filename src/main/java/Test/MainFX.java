package Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFX extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainFX.class.getName());

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Ouvrir uniquement les interfaces d'affichage (listes)
            openWindow("/AffichierPrive.fxml", "Liste Transports Privés");
            openWindow("/affichierPublique.fxml", "Liste Transports Publics");
<<<<<<< HEAD
=======
            openWindow("/AfficheClientPrive.fxml", "Liste Transports Privés (Client)");
>>>>>>> f98421b (feat: ajouter les pages parti client pour transportPrivé)

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossible de charger les interfaces d'affichage", e);
            // Quitter proprement, l'exception est déjà loggée
            throw e;
        }
    }

    /**
     * Ouvre une fenêtre JavaFX pour un FXML donné, avec un titre.
     */
    private void openWindow(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
