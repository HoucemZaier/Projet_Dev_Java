
package Controllers;

import Models.TransportPublique;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import utils.OSRMService;
import utils.Services.ServiceTransportPublique;

import java.io.File;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AfficheClientPublique implements Initializable {

    @FXML
    private ListView<TransportPublique> listTransportPublique;

    @FXML
    private ComboBox<String> comboRechercheType;

    @FXML
    private ComboBox<String> comboTri;

    @FXML
    private Button btnVoirItineraire;

    @FXML
    private Button btnClearMap;

    @FXML
    private Button btnAfficherMap;

    @FXML
    private WebView mapWebView;

    private final ServiceTransportPublique service = new ServiceTransportPublique();
    private final OSRMService osrmService = new OSRMService();

    // Liste brute venant de la base
    private final ObservableList<TransportPublique> transportData = FXCollections.observableArrayList();

    // Vues filtrée / triée de la liste
    private FilteredList<TransportPublique> filteredData;
    private SortedList<TransportPublique> sortedData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        setupFilteringAndSorting();
        loadTransportsFromDatabase();
        initializeMap();
        setupButtonHandlers();
    }

    private void configureListView() {
        if (listTransportPublique == null) return;

        listTransportPublique.setCellFactory(
                listView ->
                        new ListCell<TransportPublique>() {

                            private final HBox card = new HBox(12);
                            private final ImageView imageView = new ImageView();
                            private final VBox infos = new VBox(4);
                            private final Label lblType = new Label();
                            private final Label lblHoraire = new Label();
                            private final Label lblTarif = new Label();

                            {
                                imageView.setFitWidth(80);
                                imageView.setFitHeight(60);
                                imageView.setPreserveRatio(true);

                                lblType.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                                lblHoraire.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                                lblTarif.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

                                infos.getChildren().addAll(lblType, lblHoraire);

                                card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 12; -fx-alignment: CENTER_LEFT;");
                                card.getChildren().addAll(
                                        imageView,
                                        infos,
                                        lblTarif,
                                        new javafx.scene.layout.Region() {{
                                            HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS);
                                        }}
                                );
                            }

                            @Override
                            protected void updateItem(TransportPublique item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setGraphic(null);
                                    setStyle("-fx-background-color: transparent;");
                                } else {
                                    lblType.setText(item.getType());
                                    lblHoraire.setText("Horaire : " + (item.getHoraire() != null ? item.getHoraire() : "—"));
                                    lblTarif.setText(String.format("%.2f DT", item.getTarif()));

                                    String imagePath = item.getImage_path();
                                    if (imagePath != null && !imagePath.isEmpty()) {
                                        File file = new File(imagePath);
                                        if (file.exists()) {
                                            imageView.setImage(new Image(file.toURI().toString(), 80, 60, true, true));
                                        } else {
                                            imageView.setImage(null);
                                        }
                                    } else {
                                        imageView.setImage(null);
                                    }

                                    setGraphic(card);
                                    setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
                                }
                            }
                        }
        );
    }

    /**
     * Initialise la FilteredList / SortedList et relie les ComboBox.
     */
    private void setupFilteringAndSorting() {
        // Vues dérivées autour de la liste principale
        filteredData = new FilteredList<>(transportData, t -> true);
        sortedData = new SortedList<>(filteredData);

        if (listTransportPublique != null) {
            listTransportPublique.setItems(sortedData);
        }

        if (comboRechercheType != null) {
            comboRechercheType.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> applyFiltersAndSorting()
            );
        }

        if (comboTri != null) {
            comboTri.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> applyFiltersAndSorting()
            );
        }
    }

    private void loadTransportsFromDatabase() {
        try {
            List<TransportPublique> list = service.recuperer();
            transportData.setAll(list);   // on recharge la liste brute
            applyFiltersAndSorting();     // on applique le filtre/tri courant
        } catch (SQLDataException e) {
            showAlert("Erreur de chargement",
                    "Impossible de charger les transports publics : " + e.getMessage());
        }
    }

    /**
     * Applique le filtrage par type et le tri par tarif suivant les ComboBox.
     */
    private void applyFiltersAndSorting() {
        if (filteredData == null) {
            return;
        }

        String selectedType = comboRechercheType != null
                ? comboRechercheType.getSelectionModel().getSelectedItem()
                : null;

        String selectedTri = comboTri != null
                ? comboTri.getSelectionModel().getSelectedItem()
                : null;

        // ---- Filtrage par type ----
        filteredData.setPredicate(tp -> {
            if (tp == null) {
                return false;
            }

            // Aucun filtre, ou "Tous" sélectionné -> tous les transports affichés
            if (selectedType == null || selectedType.isEmpty()) {
                return true;
            }
            String normalizedSelectedRaw = selectedType.trim().toLowerCase();
            if (normalizedSelectedRaw.equals("tous") || normalizedSelectedRaw.equals("tous les types")) {
                return true;
            }

            String normalizedSelected = normalizeType(selectedType);
            String normalizedType = normalizeType(tp.getType());

            // Si le type filtré (bus / taxi / metro) correspond au type du transport
            return normalizedSelected.equalsIgnoreCase(normalizedType);
        });

        // ---- Tri par tarif (du moins cher au plus cher) ----
        if (sortedData != null) {
            if (selectedTri != null && selectedTri.equalsIgnoreCase("Tarif")) {
                sortedData.setComparator(Comparator.comparingDouble(TransportPublique::getTarif));
            } else {
                // Pas de tri spécifique
                sortedData.setComparator(null);
            }
        }
    }

    /**
     * Normalise une valeur de type pour ignorer emojis / majuscules / accents.
     */
    private String normalizeType(String value) {
        if (value == null) {
            return "";
        }
        String v = value.toLowerCase();

        if (v.contains("bus")) {
            return "bus";
        }
        if (v.contains("taxi")) {
            return "taxi";
        }
        if (v.contains("metro") || v.contains("métro")) {
            return "metro";
        }
        return v.trim();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void initializeMap() {
        try {
            URL mapUrl = getClass().getResource("/map.html");
            if (mapUrl != null) {
                mapWebView.getEngine().load(mapUrl.toExternalForm());
            } else {
                showAlert("Erreur de chargement", "Impossible de charger la carte");
            }
        } catch (Exception e) {
            showAlert("Erreur de carte", "Erreur lors du chargement de la carte: " + e.getMessage());
        }
    }

    private void setupButtonHandlers() {
        btnVoirItineraire.setOnAction(event -> handleVoirItineraire());
        btnClearMap.setOnAction(event -> clearMap());
        btnAfficherMap.setOnAction(event -> handleAfficherMap());
    }

    private void handleVoirItineraire() {
        TransportPublique selectedTransport = listTransportPublique.getSelectionModel().getSelectedItem();
        if (selectedTransport == null) {
            showAlert("Sélection requise", "Veuillez sélectionner un transport dans la liste");
            return;
        }

        String type = selectedTransport.getType();
        if (!isTransportSupported(type)) {
            showAlert("Transport non supporté", "Seuls les bus et taxi sont supportés pour le calcul d'itinéraire");
            return;
        }

        // Coordonnées de test (Tunis centre -> Aéroport Tunis)
        double startLat = 36.8065;
        double startLon = 10.1815;
        double endLat = 36.8483;
        double endLon = 10.2015;

        calculateAndDisplayRoute(startLat, startLon, endLat, endLon, selectedTransport);
    }

    private boolean isTransportSupported(String type) {
        if (type == null) return false;
        String normalizedType = type.toLowerCase();
        return normalizedType.contains("bus") || normalizedType.contains("taxi") || normalizedType.contains("metro");
    }

    private void calculateAndDisplayRoute(double startLat, double startLon, double endLat, double endLon, TransportPublique transport) {
        btnVoirItineraire.setDisable(true);
        btnVoirItineraire.setText("🔄 Calcul...");

        CompletableFuture.supplyAsync(() -> {
            try {
                return osrmService.calculateRoute(startLat, startLon, endLat, endLon, transport.getType());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((result, throwable) -> {
            javafx.application.Platform.runLater(() -> {
                btnVoirItineraire.setDisable(false);
                btnVoirItineraire.setText("🗺️ Voir Itinéraire");

                if (throwable != null) {
                    showAlert("Erreur de calcul", "Erreur lors du calcul de l'itinéraire: " + throwable.getMessage());
                    return;
                }

                displayRouteOnMap(startLat, startLon, endLat, endLon, transport, result);
            });
        });
    }

    private void displayRouteOnMap(double startLat, double startLon, double endLat, double endLon, 
                                 TransportPublique transport, OSRMService.ItineraryResponse route) {
        try {
            String script = String.format(
                "if (window.calculateRouteFromJava) { " +
                "  window.calculateRouteFromJava(%f, %f, %f, %f, '%s', '%.2f'); " +
                "} else { " +
                "  console.log('calculateRouteFromJava not available'); " +
                "}",
                startLat, startLon, endLat, endLon, 
                transport.getType(), transport.getTarif()
            );

            mapWebView.getEngine().executeScript(script);

            // Afficher un résumé
            String summary = String.format(
                "Itinéraire %s:\n" +
                "📏 Distance: %s\n" +
                "⏱️ Durée: %s\n" +
                "💰 Tarif: %.2f DT",
                transport.getType(),
                route.getFormattedDistance(),
                route.getFormattedDuration(),
                transport.getTarif()
            );

            javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            info.setTitle("Itinéraire calculé");
            info.setHeaderText(null);
            info.setContentText(summary);
            info.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur d'affichage", "Erreur lors de l'affichage de l'itinéraire: " + e.getMessage());
        }
    }

    private void clearMap() {
        try {
            mapWebView.getEngine().executeScript("if (window.clearMarkers) { window.clearMarkers(); }");
        } catch (Exception e) {
            // Ignorer les erreurs de script
        }
    }

    private void handleAfficherMap() {
        try {
            // Recharger la carte pour afficher tous les transports
            URL mapUrl = getClass().getResource("/map.html");
            if (mapUrl != null) {
                mapWebView.getEngine().load(mapUrl.toExternalForm());
                
                // Attendre que la carte se charge puis afficher tous les transports
                javafx.application.Platform.runLater(() -> {
                    javafx.util.Duration duration = javafx.util.Duration.seconds(2);
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(duration);
                    pause.setOnFinished(event -> displayAllTransportsOnMap());
                    pause.play();
                });
            } else {
                showAlert("Erreur de chargement", "Impossible de charger la carte");
            }
        } catch (Exception e) {
            showAlert("Erreur de carte", "Erreur lors du chargement de la carte: " + e.getMessage());
        }
    }

    private void displayAllTransportsOnMap() {
        try {
            // Afficher tous les transports sur la carte avec des marqueurs
            for (TransportPublique transport : transportData) {
                if (isTransportSupported(transport.getType())) {
                    // Coordonnées aléatoires pour démonstration (Tunis et environs)
                    double lat = 33.8869 + (Math.random() - 0.5) * 0.5;
                    double lon = 9.5375 + (Math.random() - 0.5) * 0.5;
                    
                    String script = String.format(
                        "if (window.addTransportMarker) { " +
                        "  window.addTransportMarker(%f, %f, '%s', '%.2f'); " +
                        "}",
                        lat, lon, transport.getType(), transport.getTarif()
                    );
                    
                    mapWebView.getEngine().executeScript(script);
                }
            }
            
            showAlert("Carte chargée", "Tous les transports publics (bus, taxi et metro) sont affichés sur la carte");
        } catch (Exception e) {
            // Ignorer les erreurs d'exécution de script
        }
    }
}
