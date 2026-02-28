package com.PlaNova;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import atlantafx.base.theme.NordLight;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFX extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainFX.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/explore.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1280, 800);

            primaryStage.setTitle("PlaNova - Travel Management");
            primaryStage.setMinWidth(1280);
            primaryStage.setMinHeight(800);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossible de charger l'interface d'affichage", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        System.setProperty("glass.gtk.uiScale", "1.0");
        System.setProperty("glass.gtk.uiScale.enabled", "false");
        System.setProperty("prism.allowhidpi", "false");
        System.setProperty("sun.java2d.uiScale", "1.0");

        launch(args);
    }
}