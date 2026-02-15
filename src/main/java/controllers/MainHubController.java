package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainHubController implements Initializable {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Button btnHotels;

    @FXML
    private Button btnChambres;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnStats;

    @FXML
    private Label pageTitle;

    // Ces labels ne sont pas dans le FXML, on ne les déclare pas avec @FXML
    // On va les créer dynamiquement dans le dashboard
    private Label hotelCountLabel;
    private Label chambreCountLabel;
    private ListView<String> recentActivityList;

    private Parent hotelsView;
    private Parent chambresView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger les vues
        loadViews();

        // Configurer les boutons
        setupButtons();

        // Afficher le dashboard par défaut
        showDashboard();
    }

    private void loadViews() {
        try {
            // Charger la vue des hôtels
            hotelsView = FXMLLoader.load(getClass().getResource("/listHotel.fxml"));

            // Charger la vue des chambres
            chambresView = FXMLLoader.load(getClass().getResource("/listChambre.fxml"));

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger les vues: " + e.getMessage());
        }
    }

    private void setupButtons() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";
        String activeStyle = "-fx-background-color: #e2e8f0; -fx-text-fill: #2A3B4C; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-border-width: 0 0 0 4; -fx-border-color: #2A3B4C;";

        btnDashboard.setStyle(defaultStyle);
        btnHotels.setStyle(defaultStyle);
        btnChambres.setStyle(defaultStyle);
        btnStats.setStyle(defaultStyle);
    }

    private void setActiveButton(Button activeButton) {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";
        String activeStyle = "-fx-background-color: #e2e8f0; -fx-text-fill: #2A3B4C; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-border-width: 0 0 0 4; -fx-border-color: #2A3B4C;";

        btnDashboard.setStyle(defaultStyle);
        btnHotels.setStyle(defaultStyle);
        btnChambres.setStyle(defaultStyle);
        btnStats.setStyle(defaultStyle);

        activeButton.setStyle(activeStyle);
    }

    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        pageTitle.setText("Tableau de bord");

        // Créer un dashboard simple sans utiliser les @FXML
        VBox dashboard = createDashboardView();
        mainContainer.setCenter(dashboard);
    }

    @FXML
    private void showHotels() {
        setActiveButton(btnHotels);
        pageTitle.setText("Gestion des hôtels");
        mainContainer.setCenter(hotelsView);
    }

    @FXML
    private void showChambres() {
        setActiveButton(btnChambres);
        pageTitle.setText("Gestion des chambres");
        mainContainer.setCenter(chambresView);
    }

    @FXML
    private void showStats() {
        setActiveButton(btnStats);
        pageTitle.setText("Statistiques");

        // Créer une vue stats simple
        VBox statsView = createStatsView();
        mainContainer.setCenter(statsView);
    }

    private VBox createDashboardView() {
        VBox dashboard = new VBox(20);
        dashboard.setStyle("-fx-padding: 30; -fx-background-color: #f8fafd;");

        // Titre du dashboard
        Label welcomeLabel = new Label("Bienvenue dans votre espace de gestion");
        welcomeLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Cartes de statistiques
        HBox statsCards = new HBox(20);
        statsCards.setStyle("-fx-padding: 20 0;");

        // Simuler des statistiques
        int totalHotels = 12;
        int totalChambres = 48;
        int disponibles = 32;

        // Carte Hôtels
        VBox hotelCard = createStatCard(
                "Hôtels",
                String.valueOf(totalHotels),
                "🏨",
                "#3A4C5D",
                "Voir les hôtels",
                e -> showHotels()
        );

        // Carte Chambres
        VBox chambreCard = createStatCard(
                "Chambres",
                String.valueOf(totalChambres),
                "🛏️",
                "#10b981",
                "Voir les chambres",
                e -> showChambres()
        );

        // Carte Disponibles
        VBox dispoCard = createStatCard(
                "Disponibles",
                String.valueOf(disponibles),
                "✓",
                "#f59e0b",
                null,
                null
        );

        statsCards.getChildren().addAll(hotelCard, chambreCard, dispoCard);

        // Section activité récente
        VBox activitySection = new VBox(15);
        activitySection.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #eef2f6; -fx-border-width: 1;");

        Label activityTitle = new Label("Activité récente");
        activityTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        ListView<String> activityList = new ListView<>();
        activityList.setPrefHeight(150);
        activityList.setStyle("-fx-background-color: transparent; -fx-border-color: #eef2f6; -fx-border-radius: 8;");

        // Simuler des activités
        activityList.getItems().addAll(
                "🏨 Hôtel 'Royal Palace' ajouté",
                "🛏️ Chambre #204 modifiée",
                "🏨 Hôtel 'Beach Resort' supprimé",
                "🛏️ Nouvelle chambre #105 ajoutée"
        );

        activitySection.getChildren().addAll(activityTitle, activityList);

        dashboard.getChildren().addAll(welcomeLabel, statsCards, activitySection);

        return dashboard;
    }

    private VBox createStatCard(String title, String value, String icon, String color, String buttonText, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #eef2f6; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPrefWidth(200);

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b;");

        header.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(header, valueLabel);

        if (buttonText != null && action != null) {
            Button viewButton = new Button(buttonText);
            viewButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-border-color: " + color + "; -fx-border-radius: 6; -fx-padding: 8; -fx-cursor: hand;");
            viewButton.setMaxWidth(Double.MAX_VALUE);
            viewButton.setOnAction(action);
            card.getChildren().add(viewButton);
        }

        return card;
    }

    private VBox createStatsView() {
        VBox statsView = new VBox(20);
        statsView.setStyle("-fx-padding: 30; -fx-background-color: #f8fafd;");

        Label statsTitle = new Label("Statistiques détaillées");
        statsTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Grille de statistiques
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 12;");

        // Ligne 1
        statsGrid.add(createStatBox("Total Hôtels", "12", "#3A4C5D"), 0, 0);
        statsGrid.add(createStatBox("Total Chambres", "48", "#10b981"), 1, 0);

        // Ligne 2
        statsGrid.add(createStatBox("Chambres Disponibles", "32", "#f59e0b"), 0, 1);
        statsGrid.add(createStatBox("Taux d'occupation", "67%", "#3b82f6"), 1, 1);

        statsView.getChildren().addAll(statsTitle, statsGrid);

        return statsView;
    }

    private VBox createStatBox(String label, String value, String color) {
        VBox box = new VBox(5);
        box.setStyle("-fx-padding: 15; -fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #eef2f6; -fx-border-width: 1;");
        box.setPrefWidth(250);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b;");

        box.getChildren().addAll(valueLabel, labelLabel);
        return box;
    }

    @FXML
    private void handleAjouterHotel() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ajouterHotel.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter un hôtel");
            stage.setScene(new Scene(root, 700, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouterChambre() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ajouterChambre.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ajouter une chambre");
            stage.setScene(new Scene(root, 700, 550));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        showDashboard();
        showAlert("Information", "Données actualisées");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}