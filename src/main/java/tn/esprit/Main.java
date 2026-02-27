package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        // ✅ لازم قبل launch باش WebView ما يتعاركش على نفس folder
        System.setProperty("javafx.userDataDir",
                System.getProperty("java.io.tmpdir") + "/webview-" + System.nanoTime());

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Accueil - Dashboard");
        stage.show();
    }
}