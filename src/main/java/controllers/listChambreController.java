package controllers;

import models.Chambre;
import models.Hotel;
import services.ServiceChambre;
import services.ServiceHotel;
import utils.MyDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class listChambreController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> hotelFilter;
    @FXML
    private ComboBox<String> typeFilter;
    @FXML
    private ComboBox<String> statutFilter;
    @FXML
    private Label totalChambresLabel;
    @FXML
    private Label disponiblesLabel;
    @FXML
    private Label occupeesLabel;
    @FXML
    private Label prixMoyenLabel;
    @FXML
    private VBox chambresContainer;
    @FXML
    private Label statsLabel;
    @FXML
    private Label pageLabel;
    @FXML
    private Button ajouterBtn;

    private ServiceChambre serviceChambre;
    private ServiceHotel serviceHotel;
    private ObservableList<Chambre> chambresList = FXCollections.observableArrayList();
    private ObservableList<Chambre> filteredList = FXCollections.observableArrayList();

    // Variables pour la pagination
    private int currentPage = 1;
    private final int ITEMS_PER_PAGE = 5;
    private int totalPages = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceChambre = new ServiceChambre();
        serviceHotel = new ServiceHotel();

        // Initialiser les filtres
        initializeFilters();

        // Charger les données
        loadChambres();

        // Configurer les écouteurs de recherche
        setupSearchListeners();
    }

    private void initializeFilters() {
        // Filtre Hôtel
        try {
            List<Hotel> hotels = serviceHotel.recuperer();
            ObservableList<String> hotelNames = FXCollections.observableArrayList();
            hotelNames.add("Tous les hôtels");
            hotelNames.addAll(hotels.stream()
                    .map(Hotel::getNomHotel)
                    .collect(Collectors.toList()));
            hotelFilter.setItems(hotelNames);
            hotelFilter.getSelectionModel().selectFirst();
        } catch (SQLDataException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les hôtels: " + e.getMessage());
        }

        // Filtre Type de chambre
        ObservableList<String> types = FXCollections.observableArrayList(
                "Tous les types", "Simple", "Double", "Suite", "Familiale", "Deluxe"
        );
        typeFilter.setItems(types);
        typeFilter.getSelectionModel().selectFirst();

        // Filtre Statut
        ObservableList<String> statuts = FXCollections.observableArrayList(
                "Tous les statuts", "Disponible", "Occupée", "En maintenance", "Réservée"
        );
        statutFilter.setItems(statuts);
        statutFilter.getSelectionModel().selectFirst();
    }

    private void loadChambres() {
        try {
            List<Chambre> chambres = serviceChambre.recuperer();
            chambresList.setAll(chambres);
            applyFilters();
            updateStatistics();
        } catch (SQLDataException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les chambres: " + e.getMessage());
        }
    }

    private void setupSearchListeners() {
        // Recherche en temps réel (optionnel)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Filtres
        hotelFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        typeFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        statutFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    @FXML
    private void handleRechercher() {
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedHotel = hotelFilter.getValue();
        String selectedType = typeFilter.getValue();
        String selectedStatut = statutFilter.getValue();

        List<Chambre> filtered = chambresList.stream()
                .filter(chambre -> {
                    // Filtre recherche
                    if (searchText != null && !searchText.isEmpty()) {
                        String chambreInfo = (chambre.getTypeChambre() + " " +
                                chambre.getIdChambre()).toLowerCase();
                        if (!chambreInfo.contains(searchText)) {
                            return false;
                        }
                    }

                    // Filtre hôtel
                    if (selectedHotel != null && !selectedHotel.equals("Tous les hôtels")) {
                        try {
                            Hotel hotel = getHotelById(chambre.getIdHotel());
                            if (hotel == null || !hotel.getNomHotel().equals(selectedHotel)) {
                                return false;
                            }
                        } catch (SQLDataException e) {
                            return false;
                        }
                    }

                    // Filtre type
                    if (selectedType != null && !selectedType.equals("Tous les types")) {
                        if (!chambre.getTypeChambre().equalsIgnoreCase(selectedType)) {
                            return false;
                        }
                    }

                    // Filtre statut
                    if (selectedStatut != null && !selectedStatut.equals("Tous les statuts")) {
                        if (!chambre.getStatutChambre().equalsIgnoreCase(selectedStatut)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
        updatePagination();
        displayCurrentPage();
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        // Ajuster la page courante si nécessaire
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        pageLabel.setText(String.valueOf(currentPage));

        // Mettre à jour les statistiques d'affichage
        int start = (currentPage - 1) * ITEMS_PER_PAGE + 1;
        int end = Math.min(currentPage * ITEMS_PER_PAGE, filteredList.size());
        if (filteredList.isEmpty()) {
            statsLabel.setText("Aucun résultat");
        } else {
            statsLabel.setText(String.format("Affichage %d-%d de %d résultats",
                    start, end, filteredList.size()));
        }
    }

    private void displayCurrentPage() {
        chambresContainer.getChildren().clear();

        int start = (currentPage - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredList.size());

        for (int i = start; i < end; i++) {
            Chambre chambre = filteredList.get(i);
            HBox chambreRow = createChambreRow(chambre, i + 1);
            chambresContainer.getChildren().add(chambreRow);
        }

        // Si la liste est vide, afficher un message
        if (filteredList.isEmpty()) {
            Label emptyLabel = new Label("Aucune chambre trouvée");
            emptyLabel.setStyle("-fx-padding: 40; -fx-text-fill: #64748b; -fx-font-size: 14;");
            chambresContainer.getChildren().add(emptyLabel);
        }
    }

    private HBox createChambreRow(Chambre chambre, int index) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: white; -fx-border-color: #edf2f7; -fx-border-width: 0 0 1 0; -fx-padding: 15 20; -fx-cursor: hand;");

        // Hover effect
        row.setOnMouseEntered(e ->
                row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #edf2f7; -fx-border-width: 0 0 1 0; -fx-padding: 15 20; -fx-cursor: hand;"));
        row.setOnMouseExited(e ->
                row.setStyle("-fx-background-color: white; -fx-border-color: #edf2f7; -fx-border-width: 0 0 1 0; -fx-padding: 15 20; -fx-cursor: hand;"));

        // Numéro
        HBox numBox = new HBox();
        numBox.setPrefWidth(60);
        numBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label numLabel = new Label(String.valueOf(index));
        numLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #334155;");
        numBox.getChildren().add(numLabel);

        // Hôtel
        HBox hotelBox = new HBox();
        hotelBox.setPrefWidth(180);
        hotelBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label hotelLabel = new Label();
        try {
            Hotel hotel = getHotelById(chambre.getIdHotel());
            hotelLabel.setText(hotel != null ? hotel.getNomHotel() : "Hôtel inconnu");
        } catch (SQLDataException e) {
            hotelLabel.setText("Hôtel inconnu");
        }
        hotelLabel.setStyle("-fx-text-fill: #1e293b;");
        hotelBox.getChildren().add(hotelLabel);

        // Numéro de chambre
        HBox numChambreBox = new HBox();
        numChambreBox.setPrefWidth(100);
        numChambreBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label numChambreLabel = new Label("Chambre " + chambre.getIdChambre());
        numChambreLabel.setStyle("-fx-text-fill: #1e293b;");
        numChambreBox.getChildren().add(numChambreLabel);

        // Type
        HBox typeBox = new HBox();
        typeBox.setPrefWidth(120);
        typeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label typeLabel = new Label(chambre.getTypeChambre());
        typeLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: 500;");
        typeBox.getChildren().add(typeLabel);

        // Prix
        HBox prixBox = new HBox();
        prixBox.setPrefWidth(100);
        prixBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label prixLabel = new Label(String.format("%.2f €", chambre.getPrixChambre()));
        prixLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: 600;");
        prixBox.getChildren().add(prixLabel);

        // Statut avec badge coloré
        HBox statutBox = new HBox();
        statutBox.setPrefWidth(100);
        statutBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statutLabel = new Label(chambre.getStatutChambre());
        Circle statutDot = new Circle(6);

        switch (chambre.getStatutChambre().toLowerCase()) {
            case "disponible":
                statutDot.setFill(Color.web("#10b981"));
                break;
            case "occupée":
                statutDot.setFill(Color.web("#ef4444"));
                break;
            case "en maintenance":
                statutDot.setFill(Color.web("#f59e0b"));
                break;
            case "réservée":
                statutDot.setFill(Color.web("#3b82f6"));
                break;
            default:
                statutDot.setFill(Color.web("#64748b"));
        }

        HBox statutContent = new HBox(8);
        statutContent.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        statutContent.getChildren().addAll(statutDot, statutLabel);
        statutBox.getChildren().add(statutContent);

        // Actions
        HBox actionsBox = new HBox();
        actionsBox.setPrefWidth(120);
        actionsBox.setSpacing(10);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: #3A4C5D; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14; -fx-padding: 8 12;");
        editBtn.setOnAction(e -> handleModifierChambre(chambre));

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14; -fx-padding: 8 12;");
        deleteBtn.setOnAction(e -> handleSupprimerChambre(chambre));

        Button viewBtn = new Button("👁");
        viewBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 14; -fx-padding: 8 12;");
        viewBtn.setOnAction(e -> handleVoirDetails(chambre));

        actionsBox.getChildren().addAll(editBtn, deleteBtn, viewBtn);

        // Ajouter tous les éléments à la ligne
        row.getChildren().addAll(numBox, hotelBox, numChambreBox, typeBox, prixBox, statutBox, actionsBox);

        return row;
    }

    private Hotel getHotelById(int idHotel) throws SQLDataException {
        List<Hotel> hotels = serviceHotel.recuperer();
        return hotels.stream()
                .filter(h -> h.getIdHotel() == idHotel)
                .findFirst()
                .orElse(null);
    }

    private void updateStatistics() {
        int total = chambresList.size();
        long disponibles = chambresList.stream()
                .filter(c -> "Disponible".equalsIgnoreCase(c.getStatutChambre()))
                .count();
        long occupees = chambresList.stream()
                .filter(c -> "Occupée".equalsIgnoreCase(c.getStatutChambre()))
                .count();
        double prixMoyen = chambresList.stream()
                .mapToDouble(Chambre::getPrixChambre)
                .average()
                .orElse(0.0);

        totalChambresLabel.setText(String.valueOf(total));
        disponiblesLabel.setText(String.valueOf(disponibles));
        occupeesLabel.setText(String.valueOf(occupees));
        prixMoyenLabel.setText(String.format("%.2f €", prixMoyen));
    }

    @FXML
    private void handleAjouterChambre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterChambre.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une chambre");

            // DÉFINIR LES DIMENSIONS ICI
            stage.setScene(new Scene(root, 700, 550)); // Largeur 700, Hauteur 550

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false); // Optionnel : empêcher le redimensionnement
            stage.showAndWait();

            // Rafraîchir la liste après fermeture
            loadChambres();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleModifierChambre(Chambre chambre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierChambre.fxml"));
            Parent root = loader.load();

            // Passer la chambre au contrôleur de modification
            modifierChambreController controller = loader.getController();
            controller.setChambre(chambre);

            Stage stage = new Stage();
            stage.setTitle("Modifier la chambre");

            // DÉFINIR LES DIMENSIONS ICI
            stage.setScene(new Scene(root, 750, 580)); // Largeur 750, Hauteur 580

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false); // Optionnel : empêcher le redimensionnement
            stage.showAndWait();

            // Rafraîchir la liste après fermeture
            loadChambres();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimerChambre(Chambre chambre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la chambre");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la chambre #" + chambre.getIdChambre() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceChambre.supprimer(chambre);
                    loadChambres();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Chambre supprimée avec succès!");
                } catch (SQLDataException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la chambre: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleVoirDetails(Chambre chambre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailsChambre.fxml"));
            Parent root = loader.load();

            // Passer la chambre au contrôleur de détails
            detailsChambreController controller = loader.getController();
            controller.setChambre(chambre);

            Stage stage = new Stage();
            stage.setTitle("Détails de la chambre");

            // DÉFINIR LES DIMENSIONS ICI
            stage.setScene(new Scene(root, 600, 450)); // Largeur 600, Hauteur 450

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false); // Optionnel : empêcher le redimensionnement
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre des détails: " + e.getMessage());
        }
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
            displayCurrentPage();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
            displayCurrentPage();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    // Exemple d'ajout dans la méthode d'affichage des chambres
    private void afficherDetailsChambre(Chambre chambre) {
        // ... code existant ...

        // Ajouter l'affichage de la description et des équipements
        if (chambre.getDescription() != null && !chambre.getDescription().isEmpty()) {
            // Afficher la description
        }

        if (chambre.getEquipement() != null && !chambre.getEquipement().isEmpty()) {
            // Afficher les équipements
        }
    }
}