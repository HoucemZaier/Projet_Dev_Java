package com.PlaNova.controllers;

import com.PlaNova.models.Destination;
import com.PlaNova.services.DestinationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

public class AddDestinationController {
    @FXML
    private TextField tfid;
    @FXML
    private TextField tfdestination;
    @FXML
    private TextField tfcountry;
    @FXML
    private DatePicker tfdatedep;
    @FXML
    private DatePicker tfdatea;
    @FXML
    private ImageView imageView;
    @FXML
    private Button uploadButton;


    @FXML
    void addDestination(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("destination added title");
        alert.setContentText("destionation added description");
        DestinationService vs = new DestinationService();
        Destination destination = new Destination(tfdestination.getText(), tfcountry.getText(), tfdatedep.getValue(), tfdatea.getValue(), imageView.getImage() != null ? imageView.getImage().getUrl() : "");
        try {
            vs.add(destination);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void uploadImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            Image selectedImage = new Image(selectedFile.toURI().toString());
            imageView.setImage(selectedImage);
        }
    }
}
