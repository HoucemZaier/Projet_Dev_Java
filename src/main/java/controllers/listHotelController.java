package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Hotel;
import services.ServiceHotel;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;

public class listHotelController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> villeFilter;

    @FXML
    private ComboBox<String> etoilesFilter;

    @FXML
    private VBox hotelsContainer;

    @FXML
    private Label statsLabel;

    @FXML
    private Label pageLabel;

    @FXML
    private Button ajouterBtn;

    @FXML
    private Label lastUpdateLabel;

    private ServiceHotel serviceHotel;
    private List<Hotel> allHotels;
    private List<Hotel> filteredHotels;
    private int currentPage = 1;
    private final int ITEMS_PER_PAGE = 10;

    @FXML
    public void initialize() {
        serviceHotel = new ServiceHotel();

        // Initialiser les filtres
        initializeFilters();

        // Charger les données
        loadHotels();

        // Écouter la recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterHotels();
        });

        // Écouter les filtres
        villeFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterHotels();
        });

        etoilesFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterHotels();
        });

        // Initialiser le label de dernière mise à jour
        if (lastUpdateLabel != null) {
            updateLastUpdateLabel();
        }
    }

    private void initializeFilters() {
        // Filtre des villes (chargé dynamiquement)
        villeFilter.getItems().addAll("Toutes les villes");

        // Filtre des étoiles
        etoilesFilter.getItems().addAll(
                "Toutes les étoiles",
                "⭐☆☆☆☆ (1)",
                "⭐⭐☆☆☆ (2)",
                "⭐⭐⭐☆☆ (3)",
                "⭐⭐⭐⭐☆ (4)",
                "⭐⭐⭐⭐⭐ (5)"
        );
        etoilesFilter.setValue("Toutes les étoiles");
    }

    private void loadHotels() {
        try {
            // Récupérer tous les hôtels
            allHotels = serviceHotel.recuperer();
            System.out.println("Hôtels chargés: " + allHotels.size()); // Debug

            // Mettre à jour le filtre des villes
            updateVilleFilter();

            // Réinitialiser la page courante
            currentPage = 1;

            // Filtrer les hôtels
            filterHotels();

            // Mettre à jour le label de dernière mise à jour
            updateLastUpdateLabel();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les hôtels : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateLastUpdateLabel() {
        if (lastUpdateLabel != null) {
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            lastUpdateLabel.setText("Dernière mise à jour: " + now.format(formatter));
        }
    }

    private void updateVilleFilter() {
        List<String> villes = allHotels.stream()
                .map(Hotel::getVille)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String currentSelection = villeFilter.getValue();

        villeFilter.getItems().clear();
        villeFilter.getItems().add("Toutes les villes");
        villeFilter.getItems().addAll(villes);

        // Restaurer la sélection précédente si possible
        if (currentSelection != null && villeFilter.getItems().contains(currentSelection)) {
            villeFilter.setValue(currentSelection);
        } else {
            villeFilter.setValue("Toutes les villes");
        }
    }

    private void filterHotels() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedVille = villeFilter.getValue();
        String selectedEtoiles = etoilesFilter.getValue();

        filteredHotels = allHotels.stream()
                .filter(hotel -> {
                    // Filtre par recherche
                    if (!searchText.isEmpty()) {
                        return hotel.getNomHotel().toLowerCase().contains(searchText) ||
                                hotel.getVille().toLowerCase().contains(searchText) ||
                                hotel.getAdresse().toLowerCase().contains(searchText);
                    }
                    return true;
                })
                .filter(hotel -> {
                    // Filtre par ville
                    if (selectedVille != null && !selectedVille.equals("Toutes les villes")) {
                        return hotel.getVille().equals(selectedVille);
                    }
                    return true;
                })
                .filter(hotel -> {
                    // Filtre par étoiles
                    if (selectedEtoiles != null && !selectedEtoiles.equals("Toutes les étoiles")) {
                        int etoiles = getEtoilesFromFilter(selectedEtoiles);
                        return hotel.getNombreEtoile() == etoiles;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        System.out.println("Hôtels filtrés: " + filteredHotels.size()); // Debug

        // Ajuster la page courante si nécessaire
        int maxPage = (int) Math.ceil((double) filteredHotels.size() / ITEMS_PER_PAGE);
        if (currentPage > maxPage && maxPage > 0) {
            currentPage = maxPage;
        } else if (filteredHotels.isEmpty()) {
            currentPage = 1;
        }

        // Afficher les hôtels filtrés
        displayHotels();
        updateStats();
        updatePagination();
    }

    private int getEtoilesFromFilter(String filter) {
        switch (filter) {
            case "⭐☆☆☆☆ (1)": return 1;
            case "⭐⭐☆☆☆ (2)": return 2;
            case "⭐⭐⭐☆☆ (3)": return 3;
            case "⭐⭐⭐⭐☆ (4)": return 4;
            case "⭐⭐⭐⭐⭐ (5)": return 5;
            default: return 0;
        }
    }

    private void displayHotels() {
        hotelsContainer.getChildren().clear();

        if (filteredHotels == null || filteredHotels.isEmpty()) {
            // Afficher un message si aucun hôtel
            HBox emptyRow = createEmptyRow();
            hotelsContainer.getChildren().add(emptyRow);
            return;
        }

        // Calculer les indices pour la pagination
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredHotels.size());

        System.out.println("Affichage page " + currentPage + ": indices " + startIndex + " à " + endIndex); // Debug

        for (int i = startIndex; i < endIndex; i++) {
            Hotel hotel = filteredHotels.get(i);
            HBox row = createHotelRow(hotel, i % 2 == 0);
            hotelsContainer.getChildren().add(row);
        }
    }

    private HBox createEmptyRow() {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 1.5 1.5 1.5; -fx-padding: 20; -fx-alignment: CENTER;");
        row.setPrefHeight(60);

        Label message = new Label("Aucun hôtel trouvé");
        message.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");

        row.getChildren().add(message);
        return row;
    }

    /*private HBox createHotelRow(Hotel hotel, boolean evenRow) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: " + (evenRow ? "white" : "#f8fafc") + "; " +
                "-fx-border-color: #e2e8f0; " +
                "-fx-border-width: 0 1.5 1.5 1.5; " +
                "-fx-padding: 15;");
        row.setPrefHeight(70);

        // Colonne ID
        HBox idCell = new HBox();
        idCell.setPrefWidth(60);
        idCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label idLabel = new Label(String.valueOf(hotel.getIdHotel()));
        idLabel.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        idCell.getChildren().add(idLabel);

        // Colonne Nom (avec adresse)
        HBox nomCell = new HBox();
        nomCell.setPrefWidth(250);
        nomCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox nomVBox = new VBox(3);
        Label nomLabel = new Label(hotel.getNomHotel());
        nomLabel.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 13;");
        Label adresseLabel = new Label(hotel.getAdresse());
        adresseLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        nomVBox.getChildren().addAll(nomLabel, adresseLabel);
        nomCell.getChildren().add(nomVBox);

        // Colonne Ville
        HBox villeCell = new HBox();
        villeCell.setPrefWidth(100);
        villeCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label villeLabel = new Label(hotel.getVille());
        villeLabel.setStyle("-fx-text-fill: #334155;");
        villeCell.getChildren().add(villeLabel);

        // Colonne Étoiles
        HBox etoilesCell = new HBox();
        etoilesCell.setPrefWidth(100);
        etoilesCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label etoilesLabel = new Label(getStarsString(hotel.getNombreEtoile()));
        etoilesLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 14;");
        etoilesCell.getChildren().add(etoilesLabel);

        // Colonne Image
        HBox imageCell = new HBox();
        imageCell.setPrefWidth(100);
        imageCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label imageLabel = new Label(hotel.getImage() != null && !hotel.getImage().isEmpty() ? "📷" : "Aucune");
        imageLabel.setStyle("-fx-text-fill: #64748b;");
        imageCell.getChildren().add(imageLabel);

        // Colonne Actions
        HBox actionsCell = new HBox();
        actionsCell.setPrefWidth(100);
        actionsCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionsCell.setSpacing(10);

        // Bouton Modifier
        Button editBtn = createIconButton("✏️", "#0DA2E7");
        editBtn.setOnAction(e -> handleModifierHotel(hotel));

        // Bouton Supprimer
        Button deleteBtn = createIconButton("🗑️", "#ef4444");
        deleteBtn.setOnAction(e -> handleSupprimerHotel(hotel));

        actionsCell.getChildren().addAll(editBtn, deleteBtn);

        // Ajouter toutes les colonnes à la ligne
        row.getChildren().addAll(idCell, nomCell, villeCell, etoilesCell, imageCell, actionsCell);

        // Effet de survol
        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-background-color: #f1f5f9; " +
                    "-fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 0 1.5 1.5 1.5; " +
                    "-fx-padding: 15;");
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-background-color: " + (evenRow ? "white" : "#f8fafc") + "; " +
                    "-fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 0 1.5 1.5 1.5; " +
                    "-fx-padding: 15;");
        });

        return row;
    }*/


    private HBox createHotelRow(Hotel hotel, boolean evenRow) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: " + (evenRow ? "white" : "#f8fafc") + ";" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-width: 0 1.5 1.5 1.5;" +
                "-fx-padding: 15;");
        row.setPrefHeight(70);

        // ===== Colonne ID =====
        HBox idCell = new HBox();
        idCell.setPrefWidth(60);
        idCell.setAlignment(Pos.CENTER_LEFT);
        Label idLabel = new Label(String.valueOf(hotel.getIdHotel()));
        idLabel.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        idCell.getChildren().add(idLabel);

        // ===== Colonne Nom + Adresse =====
        HBox nomCell = new HBox();
        nomCell.setPrefWidth(250);
        nomCell.setAlignment(Pos.CENTER_LEFT);
        VBox nomVBox = new VBox(3);
        Label nomLabel = new Label(hotel.getNomHotel());
        nomLabel.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 13;");
        Label adresseLabel = new Label(hotel.getAdresse());
        adresseLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        nomVBox.getChildren().addAll(nomLabel, adresseLabel);
        nomCell.getChildren().add(nomVBox);

        // ===== Colonne Ville =====
        HBox villeCell = new HBox();
        villeCell.setPrefWidth(100);
        villeCell.setAlignment(Pos.CENTER_LEFT);
        Label villeLabel = new Label(hotel.getVille());
        villeLabel.setStyle("-fx-text-fill: #334155;");
        villeCell.getChildren().add(villeLabel);

        // ===== Colonne Étoiles =====
        HBox etoilesCell = new HBox();
        etoilesCell.setPrefWidth(100);
        etoilesCell.setAlignment(Pos.CENTER_LEFT);
        Label etoilesLabel = new Label(getStarsString(hotel.getNombreEtoile()));
        etoilesLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 14;");
        etoilesCell.getChildren().add(etoilesLabel);

        // ===== Colonne Image =====
        HBox imageCell = new HBox();
        imageCell.setPrefWidth(100);
        imageCell.setAlignment(Pos.CENTER_LEFT);
        Label imageLabel = new Label(hotel.getImage() != null && !hotel.getImage().isEmpty() ? "📷" : "Aucune");
        imageLabel.setStyle("-fx-text-fill: #64748b;");
        imageCell.getChildren().add(imageLabel);

        // ===== Colonne Actions avec FontAwesome =====
        HBox actionsCell = new HBox(10);
        actionsCell.setPrefWidth(120);
        actionsCell.setAlignment(Pos.CENTER_LEFT);

        // Bouton Modifier
        Button editBtn = new Button();
        FontIcon editIcon = new FontIcon("fas-edit"); // Font Awesome edit
        editIcon.setIconSize(14);
        editIcon.setIconColor(Color.WHITE);
        editBtn.setGraphic(editIcon);
        editBtn.setStyle(
                "-fx-background-color: #0DA2E7;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );
        editBtn.setOnAction(e -> handleModifierHotel(hotel));
        editBtn.setOnMouseEntered(e -> editBtn.setOpacity(0.85));
        editBtn.setOnMouseExited(e -> editBtn.setOpacity(1));

        // Bouton Supprimer
        Button deleteBtn = new Button();
        FontIcon deleteIcon = new FontIcon("fas-trash"); // Font Awesome trash
        deleteIcon.setIconSize(14);
        deleteIcon.setIconColor(Color.WHITE);
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setStyle(
                "-fx-background-color: #ef4444;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );
        deleteBtn.setOnAction(e -> handleSupprimerHotel(hotel));
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setOpacity(0.85));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setOpacity(1));

        actionsCell.getChildren().addAll(editBtn, deleteBtn);

        // ===== Ajouter toutes les colonnes à la ligne =====
        row.getChildren().addAll(idCell, nomCell, villeCell, etoilesCell, imageCell, actionsCell);

        // ===== Effet de survol de la ligne =====
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #f1f5f9;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 0 1.5 1.5 1.5;" +
                        "-fx-padding: 15;"
        ));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-background-color: " + (evenRow ? "white" : "#f8fafc") + ";" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 0 1.5 1.5 1.5;" +
                        "-fx-padding: 15;"
        ));

        return row;
    }

    private Button createIconButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color +
                "; -fx-font-size: 14; -fx-cursor: hand; -fx-padding: 5;");
        button.setPrefSize(30, 30);

        // Effet de survol
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: " + color + "15; -fx-text-fill: " + color +
                    "; -fx-font-size: 14; -fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 4;");
        });
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color +
                    "; -fx-font-size: 14; -fx-cursor: hand; -fx-padding: 5;");
        });

        return button;
    }

    private String getStarsString(int etoiles) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < etoiles ? "⭐" : "☆");
        }
        return stars.toString();
    }

    private void updateStats() {
        if (allHotels == null || filteredHotels == null) {
            statsLabel.setText("Chargement...");
            return;
        }

        int total = allHotels.size();
        int filtered = filteredHotels.size();

        String statsText;
        if (total == filtered) {
            statsText = String.format("Total: %d hôtel%s", total, total > 1 ? "s" : "");
        } else {
            statsText = String.format("Affichés: %d/%d hôtel%s", filtered, total, total > 1 ? "s" : "");
        }

        statsLabel.setText(statsText);
    }

    private void updatePagination() {
        if (filteredHotels == null || filteredHotels.isEmpty()) {
            pageLabel.setText("Page 0/0");
            return;
        }

        int totalPages = (int) Math.ceil((double) filteredHotels.size() / ITEMS_PER_PAGE);
        pageLabel.setText(String.format("Page %d/%d", currentPage, totalPages));
    }

    @FXML
    private void handleRechercher() {
        filterHotels();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            displayHotels();
            updatePagination();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) filteredHotels.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages) {
            currentPage++;
            displayHotels();
            updatePagination();
        }
    }

    @FXML
    private void handleAjouterHotel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterHotel.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un hôtel");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setResizable(false);

            // Rafraîchir la liste après la fermeture
            stage.setOnHidden(e -> {
                System.out.println("Fermeture de la fenêtre d'ajout, rechargement...");
                loadHotels();
            });

            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleModifierHotel(Hotel hotel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierhotel.fxml"));
            Parent root = loader.load();

            modifierHotelController controller = loader.getController();
            controller.setHotel(hotel);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'hôtel : " + hotel.getNomHotel());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setResizable(false);

            // Rafraîchir la liste après la fermeture
            stage.setOnHidden(e -> {
                System.out.println("Fermeture de la fenêtre de modification, rechargement...");
                loadHotels();
            });

            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // NOUVELLE MÉTHODE DE SUPPRESSION SIMPLIFIÉE
    @FXML
    private void handleSupprimerHotel(Hotel hotel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'hôtel");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'hôtel \"" + hotel.getNomHotel() + "\" ?");

        // Personnaliser le style de l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #ef4444; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8;"
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceHotel.supprimer(hotel);
                    loadHotels(); // Recharger la liste après suppression
                    showAlert("Succès", "Hôtel \"" + hotel.getNomHotel() + "\" supprimé avec succès!", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Erreur", "Impossible de supprimer l'hôtel: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        String style = "-fx-background-color: white; -fx-border-color: ";

        switch (type) {
            case ERROR:
                style += "#ef4444;";
                break;
            case INFORMATION:
                style += "#10b981;";
                break;
            case WARNING:
                style += "#f59e0b;";
                break;
            default:
                style += "#cbd5e1;";
        }

        style += " -fx-border-width: 2; -fx-border-radius: 8;";
        dialogPane.setStyle(style);

        alert.showAndWait();
    }
}