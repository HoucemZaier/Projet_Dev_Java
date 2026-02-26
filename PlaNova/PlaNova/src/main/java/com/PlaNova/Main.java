package com.PlaNova;

import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        System.setProperty("glass.gtk.uiScale", "1.0");
        System.setProperty("glass.gtk.uiScale.enabled", "false");
        System.setProperty("prism.allowhidpi", "false");
        System.setProperty("sun.java2d.uiScale", "1.0");

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Destinations.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 800);

        primaryStage.setTitle("PlaNova - Travel Management");
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(800);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
}
