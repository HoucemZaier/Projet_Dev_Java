package com.PlaNova;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class TestImage extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            java.net.URL url = getClass().getResource("/images/logo.png");
            if (url == null) System.out.println("URL IS NULL");
            else {
                System.out.println("URL IS: " + url);
                Image img = new Image(url.toExternalForm());
                System.out.println("Image error: " + img.isError());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
