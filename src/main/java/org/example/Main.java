package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Gestion Hôtelière");
        stage.setScene(scene);
        stage.show();
    }
}