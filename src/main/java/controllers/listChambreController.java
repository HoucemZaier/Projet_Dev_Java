package controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Chambre;
import models.Hotel;
import org.kordamp.ikonli.javafx.FontIcon;
import services.QRCodeService;
import services.ServiceChambre;
import services.ServiceHotel;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class listChambreController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> hotelFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> statutFilter;
    @FXML private Label totalChambresLabel;
    @FXML private Label disponiblesLabel;
    @FXML private Label occupeesLabel;
    @FXML private Label prixMoyenLabel;
    @FXML private FlowPane chambresContainer;
    @FXML private Label statsLabel;
    @FXML private Label pageLabel;
    @FXML private Button ajouterBtn;

    private ServiceChambre serviceChambre;
    private ServiceHotel serviceHotel;
    private ObservableList<Chambre> chambresList = FXCollections.observableArrayList();
    private ObservableList<Chambre> filteredList = FXCollections.observableArrayList();
    private List<Hotel> hotelsList;

    private int currentPage = 1;
    private final int ITEMS_PER_PAGE = 9; // 3 colonnes x 3 lignes
    private int totalPages = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceChambre = new ServiceChambre();
        serviceHotel = new ServiceHotel();

        initializeFilters();
        loadChambres();
        setupSearchListeners();
    }

    private void initializeFilters() {
        try {
            hotelsList = serviceHotel.recuperer();
            ObservableList<String> hotelNames = FXCollections.observableArrayList("Tous les hôtels");
            hotelNames.addAll(hotelsList.stream().map(Hotel::getNomHotel).collect(Collectors.toList()));
            hotelFilter.setItems(hotelNames);
            hotelFilter.getSelectionModel().selectFirst();
        } catch (SQLDataException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les hôtels: " + e.getMessage());
        }

        typeFilter.setItems(FXCollections.observableArrayList(
                "Tous les types", "Simple", "Double", "Suite", "Familiale", "Deluxe"));
        typeFilter.getSelectionModel().selectFirst();

        statutFilter.setItems(FXCollections.observableArrayList(
                "Tous les statuts", "Disponible", "Occupée", "En maintenance", "Réservée"));
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
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        hotelFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        statutFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    @FXML
    private void handleRechercher() { applyFilters(); }

    private void applyFilters() {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String selectedHotel = hotelFilter.getValue();
        String selectedType = typeFilter.getValue();
        String selectedStatut = statutFilter.getValue();

        List<Chambre> filtered = chambresList.stream()
                .filter(c -> {
                    if (!searchText.isEmpty()) {
                        return (c.getTypeChambre() + " " + c.getIdChambre()).toLowerCase().contains(searchText);
                    }
                    return true;
                })
                .filter(c -> {
                    if (selectedHotel != null && !selectedHotel.equals("Tous les hôtels")) {
                        String nomHotel = getNomHotel(c.getIdHotel());
                        return selectedHotel.equals(nomHotel);
                    }
                    return true;
                })
                .filter(c -> {
                    if (selectedType != null && !selectedType.equals("Tous les types")) {
                        return selectedType.equalsIgnoreCase(c.getTypeChambre());
                    }
                    return true;
                })
                .filter(c -> {
                    if (selectedStatut != null && !selectedStatut.equals("Tous les statuts")) {
                        return selectedStatut.equalsIgnoreCase(c.getStatutChambre());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
        currentPage = 1;
        updatePagination();
        displayCurrentPage();
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        pageLabel.setText(currentPage + " / " + totalPages);

        int start = (currentPage - 1) * ITEMS_PER_PAGE + 1;
        int end = Math.min(currentPage * ITEMS_PER_PAGE, filteredList.size());
        if (filteredList.isEmpty()) {
            statsLabel.setText("Aucun résultat");
        } else {
            statsLabel.setText(String.format("%d–%d de %d chambre(s)", start, end, filteredList.size()));
        }
    }

    private void displayCurrentPage() {
        chambresContainer.getChildren().clear();

        int start = (currentPage - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredList.size());

        for (int i = start; i < end; i++) {
            Chambre chambre = filteredList.get(i);
            String nomHotel = getNomHotel(chambre.getIdHotel());
            VBox card = createChambreCard(chambre, nomHotel);

            // Animation d'apparition
            card.setOpacity(0);
            chambresContainer.getChildren().add(card);
            FadeTransition ft = new FadeTransition(Duration.millis(250 + (i - start) * 60), card);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        }

        if (filteredList.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label icon = new Label("🏨");
            icon.setStyle("-fx-font-size: 40px;");
            Label msg = new Label("Aucune chambre trouvée");
            msg.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            empty.getChildren().addAll(icon, msg);
            chambresContainer.getChildren().add(empty);
        }
    }

    /**
     * Crée une carte chambre avec QR Code
     */
    private VBox createChambreCard(Chambre chambre, String nomHotel) {
        VBox card = new VBox(0);
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 12, 0, 0, 4);"
        );
        card.setCursor(javafx.scene.Cursor.HAND);

        // ===== BANDE DE COULEUR STATUT =====
        String statutColor = getStatutColor(chambre.getStatutChambre());
        Rectangle colorBar = new Rectangle(320, 5);
        colorBar.setFill(Color.web(statutColor));
        colorBar.setArcWidth(14); colorBar.setArcHeight(14);

        // ===== CONTENU PRINCIPAL =====
        HBox content = new HBox(12);
        content.setPadding(new Insets(14, 14, 10, 14));
        content.setAlignment(Pos.TOP_LEFT);

        // -- Infos chambre (gauche) --
        VBox infoBox = new VBox(6);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Header : Type + ID
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label typeLbl = new Label(chambre.getTypeChambre());
        typeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");


        // Hôtel
        Label hotelLbl = new Label("🏨 " + (nomHotel != null ? nomHotel : "N/A"));
        hotelLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        hotelLbl.setWrapText(true);
        hotelLbl.setMaxWidth(170);

        // Prix
        Label prixLbl = new Label(String.format("%.0f TND / nuit", chambre.getPrixChambre()));
        prixLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2A3B4C;");

        // Capacité + Équipements
        HBox capRow = new HBox(6);
        capRow.setAlignment(Pos.CENTER_LEFT);
        Label capLbl = new Label("👥 " + chambre.getCapacite() + " pers.");
        capLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        capRow.getChildren().add(capLbl);

        if (chambre.getEquipement() != null && !chambre.getEquipement().isEmpty()) {
            String firstEquip = chambre.getEquipement().split(",")[0].trim();
            Label equipLbl = new Label("• " + firstEquip);
            equipLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
            capRow.getChildren().add(equipLbl);
        }

        // Badge statut
        HBox statutBadge = new HBox(5);
        statutBadge.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4);
        dot.setFill(Color.web(statutColor));
        Label statutLbl = new Label(chambre.getStatutChambre());
        statutLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + statutColor + ";");
        statutBadge.getChildren().addAll(dot, statutLbl);

        infoBox.getChildren().addAll(titleRow, hotelLbl, prixLbl, capRow, statutBadge);

        // -- QR Code (droite) --
        VBox qrBox = new VBox(5);
        qrBox.setAlignment(Pos.TOP_CENTER);
        qrBox.setMinWidth(115);
        qrBox.setMaxWidth(115);

        // Généré à 300px pour une qualité maximale, affiché à 110px → scannable
        Image qrImage = QRCodeService.genererQRCodeChambre(chambre, nomHotel, 300);
        if (qrImage != null) {
            ImageView qrView = new ImageView(qrImage);
            qrView.setFitWidth(110);
            qrView.setFitHeight(110);
            qrView.setPreserveRatio(true);
            qrView.setSmooth(false); // Pixels nets, pas de flou
            qrView.setStyle(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
            );
            // Fond blanc avec bordure pour délimiter le QR
            VBox qrWrapper = new VBox();
            qrWrapper.setAlignment(Pos.CENTER);
            qrWrapper.setPadding(new Insets(4));
            qrWrapper.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 6;" +
                            "-fx-border-color: #e2e8f0;" +
                            "-fx-border-radius: 6;" +
                            "-fx-border-width: 1.5;"
            );
            qrWrapper.getChildren().add(qrView);

            Label qrLabel = new Label("📷 Scanner");
            qrLabel.setStyle("-fx-font-size: 8px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
            qrBox.getChildren().addAll(qrWrapper, qrLabel);
        } else {
            Label noQr = new Label("QR\nN/A");
            noQr.setStyle("-fx-font-size: 9px; -fx-text-fill: #94a3b8; -fx-text-alignment: center;");
            qrBox.getChildren().add(noQr);
        }

        content.getChildren().addAll(infoBox, qrBox);

        // ===== SÉPARATEUR =====
        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 0;");

        // ===== ACTIONS =====
        HBox actions = new HBox(8);
        actions.setPadding(new Insets(8, 14, 12, 14));
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Bouton Voir
        Button viewBtn = createActionButton("fas-eye", "#10b981", "Voir");
        viewBtn.setOnAction(e -> handleVoirDetails(chambre));

        // Bouton Modifier
        Button editBtn = createActionButton("fas-edit", "#0DA2E7", "Modifier");
        editBtn.setOnAction(e -> handleModifierChambre(chambre));

        // Bouton Supprimer
        Button deleteBtn = createActionButton("fas-trash", "#ef4444", "");
        deleteBtn.setOnAction(e -> handleSupprimerChambre(chambre));

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        card.getChildren().addAll(colorBar, content, sep, actions);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(13,162,231,0.2), 18, 0, 0, 6);" +
                        "-fx-translate-y: -2;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 12, 0, 0, 4);"));

        return card;
    }

    private Button createActionButton(String iconCode, String color, String text) {
        Button btn = new Button(text);
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(12);
        icon.setIconColor(Color.web(color));
        btn.setGraphic(icon);
        btn.setStyle(
                "-fx-background-color: " + color + "15;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-background-radius: 7;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 10;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 7;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 10;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + "15;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-background-radius: 7;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 10;"));
        return btn;
    }

    private String getStatutColor(String statut) {
        if (statut == null) return "#94a3b8";
        return switch (statut.toLowerCase()) {
            case "disponible"     -> "#10b981";
            case "occupée"        -> "#ef4444";
            case "en maintenance" -> "#f59e0b";
            case "réservée"       -> "#3b82f6";
            default -> "#94a3b8";
        };
    }

    private String getNomHotel(int idHotel) {
        if (hotelsList == null) return "N/A";
        return hotelsList.stream()
                .filter(h -> h.getIdHotel() == idHotel)
                .map(Hotel::getNomHotel)
                .findFirst()
                .orElse("Hôtel inconnu");
    }

    private void updateStatistics() {
        int total = chambresList.size();
        long disponibles = chambresList.stream().filter(c -> "Disponible".equalsIgnoreCase(c.getStatutChambre())).count();
        long occupees = chambresList.stream().filter(c -> "Occupée".equalsIgnoreCase(c.getStatutChambre())).count();
        double prixMoyen = chambresList.stream().mapToDouble(Chambre::getPrixChambre).average().orElse(0.0);

        totalChambresLabel.setText(String.valueOf(total));
        disponiblesLabel.setText(String.valueOf(disponibles));
        occupeesLabel.setText(String.valueOf(occupees));
        prixMoyenLabel.setText(String.format("%.0f TND", prixMoyen));
    }

    @FXML
    private void handleAjouterChambre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterChambre.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une chambre");
            stage.setScene(new Scene(root, 700, 550));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
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
            modifierChambreController controller = loader.getController();
            controller.setChambre(chambre);
            Stage stage = new Stage();
            stage.setTitle("Modifier la chambre");
            stage.setScene(new Scene(root, 750, 580));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
            loadChambres();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la chambre: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimerChambre(Chambre chambre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la chambre");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la chambre #" + chambre.getIdChambre() + " ?");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    serviceChambre.supprimer(chambre);
                    loadChambres();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Chambre supprimée avec succès!");
                } catch (SQLDataException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression impossible: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleVoirDetails(Chambre chambre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailsChambre.fxml"));
            Parent root = loader.load();
            detailsChambreController controller = loader.getController();
            controller.setChambre(chambre);
            Stage stage = new Stage();
            stage.setTitle("Détails de la chambre #" + chambre.getIdChambre());
            stage.setScene(new Scene(root, 600, 450));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails: " + e.getMessage());
        }
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) { currentPage--; updatePagination(); displayCurrentPage(); }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) { currentPage++; updatePagination(); displayCurrentPage(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
