package com.PlaNova.controllers;

import com.PlaNova.models.ReservationDTO;
import com.PlaNova.models.TransportPrive;
import com.PlaNova.models.TransportPublique;
import com.PlaNova.services.ServiceTransportPrive;
import com.PlaNova.services.ServiceTransportPublique;
import com.PlaNova.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TransportationController {

    @FXML
    private FlowPane transportFlowPane;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private ComboBox<String> typeComboBox;

    private ServiceTransportPrive privateService = new ServiceTransportPrive();
    private ServiceTransportPublique publicService = new ServiceTransportPublique();

    private ObservableList<TransportPrive> privateList = FXCollections.observableArrayList();
    private ObservableList<TransportPublique> publicList = FXCollections.observableArrayList();

    private boolean showPrivate = true;

    @FXML
    public void initialize() {
        setupFilterControls();
        loadData();
        updateDisplay();

        searchField.textProperty().addListener((obs, oldV, newV) -> updateDisplay());
        sortComboBox.valueProperty().addListener((obs, oldV, newV) -> updateDisplay());
        typeComboBox.valueProperty().addListener((obs, oldV, newV) -> updateDisplay());
    }

    private void setupFilterControls() {
        sortComboBox.setItems(
                FXCollections.observableArrayList("Price: Low to High", "Price: High to Low", "Alphabetical"));
        sortComboBox.setValue("Alphabetical");

        typeComboBox.setItems(FXCollections.observableArrayList("All", "Bus", "Taxi", "Train", "Ferry", "Avion",
                "Bateau", "Vaporetto", "Navette"));
        typeComboBox.setValue("All");
        typeComboBox.setVisible(!showPrivate);
    }

    private void loadData() {
        try {
            privateList.setAll(privateService.show());
            publicList.setAll(publicService.show());
        } catch (SQLDataException e) {
            System.err.println("Failed to load transport data: " + e.getMessage());
        }
    }

    private void updateDisplay() {
        transportFlowPane.getChildren().clear();
        String search = searchField.getText().toLowerCase();

        ReservationDTO sessionPanier = SessionManager.getCurrentReservation();
        Integer targetDestId = sessionPanier.getDestinationId();

        if (showPrivate) {
            List<TransportPrive> filtered = privateList.stream()
                    .filter(tp -> tp.getMarque().toLowerCase().contains(search))
                    .filter(tp -> targetDestId == null || targetDestId == 0
                            || tp.getId_destination() == targetDestId.intValue())
                    .sorted(getPrivateComparator())
                    .collect(Collectors.toList());

            for (TransportPrive tp : filtered) {
                transportFlowPane.getChildren().add(createPrivateCard(tp));
            }
        } else {
            String typeFilter = typeComboBox.getValue();
            List<TransportPublique> filtered = publicList.stream()
                    .filter(tp -> tp.getType().toLowerCase().contains(search))
                    .filter(tp -> typeFilter.equals("All") || tp.getType().equalsIgnoreCase(typeFilter))
                    .filter(tp -> targetDestId == null || targetDestId == 0
                            || tp.getId_destination() == targetDestId.intValue())
                    .sorted(getPublicComparator())
                    .collect(Collectors.toList());

            for (TransportPublique tp : filtered) {
                transportFlowPane.getChildren().add(createPublicCard(tp));
            }
        }
    }

    private Comparator<TransportPrive> getPrivateComparator() {
        String sort = sortComboBox.getValue();
        if (sort.equals("Price: Low to High"))
            return Comparator.comparingDouble(TransportPrive::getPrix_lac);
        if (sort.equals("Price: High to Low"))
            return (a, b) -> Double.compare(b.getPrix_lac(), a.getPrix_lac());
        return Comparator.comparing(TransportPrive::getMarque);
    }

    private Comparator<TransportPublique> getPublicComparator() {
        String sort = sortComboBox.getValue();
        if (sort.equals("Price: Low to High"))
            return Comparator.comparingDouble(TransportPublique::getTarif);
        if (sort.equals("Price: High to Low"))
            return (a, b) -> Double.compare(b.getTarif(), a.getTarif());
        return Comparator.comparing(TransportPublique::getType);
    }

    private VBox createPrivateCard(TransportPrive tp) {
        VBox card = new VBox(12);
        card.getStyleClass().add("nature-card");
        card.setPrefWidth(280);
        card.setPadding(new javafx.geometry.Insets(15));

        StackPane imageHolder = new StackPane();
        imageHolder.setPrefHeight(150);
        imageHolder.getStyleClass().add("card-image");

        ImageView iv = new ImageView();
        try {
            if (tp.getImage_path() != null && !tp.getImage_path().isEmpty()) {
                iv.setImage(new Image(tp.getImage_path()));
            } else {
                iv.setImage(new Image(getClass().getResource("/images/logo.PNG").toExternalForm()));
            }
        } catch (Exception e) {
            iv.setImage(new Image(getClass().getResource("/images/logo.PNG").toExternalForm()));
        }
        iv.setFitWidth(250);
        iv.setFitHeight(140);
        iv.setPreserveRatio(false);
        Rectangle clip = new Rectangle(250, 140);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        iv.setClip(clip);
        imageHolder.getChildren().add(iv);

        Label title = new Label(tp.getMarque());
        title.getStyleClass().add("card-title");

        Label status = new Label("Etat: " + tp.getEtat());
        status.getStyleClass().add("card-info");

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(-2);
        Label price = new Label(String.format("%.2f DT", tp.getPrix_lac()));
        price.getStyleClass().add("card-price");
        Label unit = new Label("per day");
        unit.setStyle("-fx-font-size: 10px; -fx-text-fill: #718096;");
        priceBox.getChildren().addAll(price, unit);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button selectBtn = new Button("Select");
        selectBtn.getStyleClass().add("glass-button");
        selectBtn.setOnAction(e -> selectPrivateTransport(tp));

        footer.getChildren().addAll(priceBox, spacer, selectBtn);
        card.getChildren().addAll(imageHolder, title, status, footer);

        return card;
    }

    private VBox createPublicCard(TransportPublique tp) {
        VBox card = new VBox(12);
        card.getStyleClass().add("nature-card");
        card.setPrefWidth(280);
        card.setPadding(new javafx.geometry.Insets(15));

        StackPane imageHolder = new StackPane();
        imageHolder.setPrefHeight(150);
        imageHolder.getStyleClass().add("card-image");

        ImageView iv = new ImageView();
        try {
            if (tp.getImage_path() != null && !tp.getImage_path().isEmpty()) {
                iv.setImage(new Image(tp.getImage_path()));
            } else {
                iv.setImage(new Image(getClass().getResource("/images/logo.PNG").toExternalForm()));
            }
        } catch (Exception e) {
            iv.setImage(new Image(getClass().getResource("/images/logo.PNG").toExternalForm()));
        }
        iv.setFitWidth(250);
        iv.setFitHeight(140);
        iv.setPreserveRatio(false);
        Rectangle clip = new Rectangle(250, 140);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        iv.setClip(clip);
        imageHolder.getChildren().add(iv);

        Label title = new Label(tp.getType());
        title.getStyleClass().add("card-title");

        Label schedule = new Label(tp.getHoraire());
        schedule.getStyleClass().add("card-info");
        schedule.setWrapText(true);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = new VBox(-2);
        Label price = new Label(String.format("%.2f DT", tp.getTarif()));
        price.getStyleClass().add("card-price");
        Label unit = new Label("ticket");
        unit.setStyle("-fx-font-size: 10px; -fx-text-fill: #718096;");
        priceBox.getChildren().addAll(price, unit);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button selectBtn = new Button("Select");
        selectBtn.getStyleClass().add("glass-button");
        selectBtn.setOnAction(e -> selectPublicTransport(tp));

        footer.getChildren().addAll(priceBox, spacer, selectBtn);
        card.getChildren().addAll(imageHolder, title, schedule, footer);

        return card;
    }

    private void selectPrivateTransport(TransportPrive tp) {
        ReservationDTO sessionPanier = SessionManager.getCurrentReservation();
        sessionPanier.setTransportType("prive");
        sessionPanier.setTransportId(tp.getId_transport_priv());
        sessionPanier.setTransportCost(tp.getPrix_lac());
        switchScene("/ui/explore.fxml");
    }

    private void selectPublicTransport(TransportPublique tp) {
        ReservationDTO sessionPanier = SessionManager.getCurrentReservation();
        sessionPanier.setTransportType("public");
        sessionPanier.setTransportId(tp.getId_transport_pub());
        sessionPanier.setTransportCost(tp.getTarif());
        switchScene("/ui/explore.fxml");
    }

    @FXML
    void showPrivateView() {
        showPrivate = true;
        typeComboBox.setVisible(false);
        updateDisplay();
    }

    @FXML
    void showPublicView() {
        showPrivate = false;
        typeComboBox.setVisible(true);
        updateDisplay();
    }

    @FXML
    void navigateToExplore() {
        switchScene("/ui/explore.fxml");
    }

    private void switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            transportFlowPane.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}
