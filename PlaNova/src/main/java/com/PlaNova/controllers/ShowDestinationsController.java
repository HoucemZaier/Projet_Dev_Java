package com.PlaNova.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ShowDestinationsController {
    @FXML
    private  Label id;
    @FXML
    private Label tfdestination;
    @FXML
    private Label tfcountry;
    @FXML
    private Label tfdatedep;
    @FXML
    private Label tfdatea;
    @FXML
    private Label image;


    public void setid(int id){
        this.id.setText(String.valueOf(id));
    }
    public void settfdestination(String tfdestination){
        this.tfdestination.setText(tfdestination);
    }
    public void settfcountry(String tfcountry){
        this.tfcountry.setText(tfcountry);
    }
    public void settfdatedep(String tfdatedep){
        this.tfdatedep.setText(tfdatedep);
    }
    public void settfdatea(String tfdatea){
        this.tfdatea.setText(tfdatea);
    }


}
