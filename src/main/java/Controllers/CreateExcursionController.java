package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class CreateExcursionController implements Initializable {

    @FXML private TextField titreField;
    @FXML private Label titreError;

    @FXML private ComboBox<String> destinationComboBox;
    @FXML private Label destinationError;

    @FXML private DatePicker dateDepartPicker;
    @FXML private Label dateDepartError;

    @FXML private DatePicker dateRetourPicker;
    @FXML private Label dateRetourError;

    @FXML private TextField prixField;
    @FXML private Label prixError;

    @FXML private TextField nbPlacesField;
    @FXML private Label nbPlacesError;

    @FXML private ComboBox<String> statutComboBox;
    @FXML private Label statutError;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label messageLabel;

    // 🗺️ Carte OpenStreetMap
    @FXML private WebView mapWebView;
    @FXML private Label latLabel;
    @FXML private Label lngLabel;
    @FXML private Label mapHintLabel;

    private double selectedLat = 0;
    private double selectedLng = 0;
    private boolean locationSelected = false;

    private final ServiceExcursion service = new ServiceExcursion();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statutComboBox.setItems(FXCollections.observableArrayList("ouverte", "complète", "annulée"));

        try {
            List<String> destinations = service.getAllDestinationNames();
            destinationComboBox.setItems(FXCollections.observableArrayList(destinations));
        } catch (Exception ex) {
            showError("Erreur lors du chargement des destinations : " + ex.getMessage());
        }

        // DatePickers — désactiver dates passées
        dateDepartPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        dateRetourPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate depart = dateDepartPicker.getValue() != null ? dateDepartPicker.getValue() : LocalDate.now();
                if (date != null && date.isBefore(depart)) {
                    setDisable(true);
                    setStyle("-fx-background-color:#f0f0f0;");
                }
            }
        });

        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (dateRetourPicker.getValue() != null && newVal != null && dateRetourPicker.getValue().isBefore(newVal)) {
                dateRetourPicker.setValue(newVal);
            }

            dateRetourPicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date != null && date.isBefore(newVal != null ? newVal : LocalDate.now())) {
                        setDisable(true);
                        setStyle("-fx-background-color:#f0f0f0;");
                    }
                }
            });
        });

        // Init map (centré sur Tunis)
        initMap(36.8065, 10.1815);

        setupListeners();
        setupSaveButtonBinding();
    }

    // ================== MAP ==================

    private void initMap(double defaultLat, double defaultLng) {
        WebEngine engine = mapWebView.getEngine();
        engine.setJavaScriptEnabled(true);

        engine.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 " +
                        "ExcursionApp/1.0 (motaz@tunis; contact: motaz@example.com)"
        );

        engine.setOnError(e -> System.out.println("WEB ERROR: " + e.getMessage()));
        engine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
            if (newEx != null) {
                System.out.println("LOAD EXCEPTION: " + newEx.getMessage());
                newEx.printStackTrace();
            }
        });

        String html = buildLeafletHtml(defaultLat, defaultLng, false);
        engine.loadContent(html);

        engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaApp", new MapBridge());

                    // Forçage initial
                    engine.executeScript(
                            "function forceRedraw() { if (map) { map.invalidateSize(true); map._onResize(); } }" +
                                    "setTimeout(forceRedraw, 200);" +
                                    "setTimeout(forceRedraw, 600);" +
                                    "setTimeout(forceRedraw, 1200);" +
                                    "setTimeout(forceRedraw, 2000);"
                    );

                    mapWebView.requestFocus();
                });
            }
        });

        // Listener sur taille (clé pour resize fenêtre)
        mapWebView.widthProperty().addListener((obs, old, newVal) -> {
            if (engine.getDocument() != null) {
                engine.executeScript("if (map) map.invalidateSize();");
            }
        });
        mapWebView.heightProperty().addListener((obs, old, newVal) -> {
            if (engine.getDocument() != null) {
                engine.executeScript("if (map) map.invalidateSize();");
            }
        });
    }

    public class MapBridge {
        public void onLocationSelected(double lat, double lng) {
            Platform.runLater(() -> {
                selectedLat = lat;
                selectedLng = lng;
                locationSelected = true;

                if (latLabel != null) latLabel.setText(String.format("%.6f", lat));
                if (lngLabel != null) lngLabel.setText(String.format("%.6f", lng));

                if (mapHintLabel != null) {
                    mapHintLabel.setText("✅ Position sélectionnée !");
                    mapHintLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
            });
        }

        public void onTileError(String url) {
            Platform.runLater(() -> {
                System.out.println("❌ TILE FAILED: " + url);
                if (mapHintLabel != null) {
                    mapHintLabel.setText("❌ Problème de chargement des tuiles - Vérifiez votre connexion");
                    mapHintLabel.setStyle("-fx-text-fill:#ef4444; -fx-font-weight:bold; -fx-font-size:11px;");
                }
            });
        }
    }

    private String buildLeafletHtml(double lat, double lng, boolean hasMarker) {
        String markerJs = hasMarker ? "currentMarker = L.marker([" + lat + "," + lng + "]).addTo(map);" : "";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'/>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
                "<style>" +
                "html, body { margin:0; padding:0; height:100%; width:100%; }" +
                "#map { width:100%; height:100%; }" +
                "</style>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.8.0/dist/leaflet.css'/>" +
                "</head>" +
                "<body>" +
                "<div id='map'></div>" +
                "<script src='https://unpkg.com/leaflet@1.8.0/dist/leaflet.js'></script>" +
                "<script>" +
                "var map = L.map('map').setView([" + lat + ", " + lng + "], 7);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png', {" +
                "  maxZoom: 20," +
                "  attribution: 'Données &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> — Tuiles &copy; <a href=\"https://www.openstreetmap.fr\">OpenStreetMap France</a>'" +
                "}).on('tileerror', function(e) {" +
                "  console.log('Tile error: ' + e.url);" +
                "  if (window.javaApp) window.javaApp.onTileError(e.url);" +
                "}).addTo(map);" +
                "var currentMarker = null;" +
                markerJs +
                "map.on('click', function(e) {" +
                "  if (currentMarker) map.removeLayer(currentMarker);" +
                "  currentMarker = L.marker([e.latlng.lat, e.latlng.lng]).addTo(map);" +
                "  currentMarker.bindPopup('📍 ' + e.latlng.lat.toFixed(6) + ', ' + e.latlng.lng.toFixed(6)).openPopup();" +
                "  if (window.javaApp) window.javaApp.onLocationSelected(e.latlng.lat, e.latlng.lng);" +
                "});" +
                "</script>" +
                "</body>" +
                "</html>";
    }

    @FXML
    private void handleResetLocation() {
        selectedLat = 0;
        selectedLng = 0;
        locationSelected = false;

        if (latLabel != null) latLabel.setText("—");
        if (lngLabel != null) lngLabel.setText("—");

        if (mapHintLabel != null) {
            mapHintLabel.setText("💡 Facultatif — Cliquez sur la carte pour enregistrer la localisation");
            mapHintLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        }

        initMap(36.8065, 10.1815);
    }

    // ================== FORM ==================

    private void setupListeners() {
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                titreError.setText("Le titre est obligatoire");
                titreError.setVisible(true);
            } else if (!newVal.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                titreError.setText("Lettres uniquement");
                titreError.setVisible(true);
            } else {
                titreError.setVisible(false);
            }
        });

        destinationComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                destinationError.setVisible(newVal == null || newVal.trim().isEmpty()));

        prixField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) prixField.setText(oldVal);
            prixError.setVisible(newVal.trim().isEmpty() || parseDouble(newVal) <= 0);
        });

        nbPlacesField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) nbPlacesField.setText(oldVal);
            nbPlacesError.setVisible(newVal.trim().isEmpty() || parseInt(newVal) <= 0);
        });

        statutComboBox.valueProperty().addListener((obs, o, n) -> statutError.setVisible(n == null));
        dateDepartPicker.valueProperty().addListener((obs, o, n) -> dateDepartError.setVisible(n == null));
        dateRetourPicker.valueProperty().addListener((obs, o, n) -> dateRetourError.setVisible(n == null));
    }

    private void setupSaveButtonBinding() {
        saveButton.disableProperty().bind(
                titreField.textProperty().isEmpty()
                        .or(destinationComboBox.valueProperty().isNull())
                        .or(dateDepartPicker.valueProperty().isNull())
                        .or(dateRetourPicker.valueProperty().isNull())
                        .or(prixField.textProperty().isEmpty())
                        .or(nbPlacesField.textProperty().isEmpty())
                        .or(statutComboBox.valueProperty().isNull())
        );
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) return;

        try {
            Excursion e = new Excursion();
            e.setTitre(titreField.getText().trim());
            e.setDateDepart(Date.valueOf(dateDepartPicker.getValue()));
            e.setDateRetour(Date.valueOf(dateRetourPicker.getValue()));
            e.setPrix(parseDouble(prixField.getText()));
            e.setNbPlaces(parseInt(nbPlacesField.getText()));
            e.setStatut(statutComboBox.getValue());

            if (locationSelected) {
                e.setLatitude(selectedLat);
                e.setLongitude(selectedLng);
            }

            service.ajouter(e, destinationComboBox.getValue());
            showSuccess("Excursion ajoutée avec succès !" + (locationSelected ? " 📍 Position enregistrée." : ""));

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::closeWindow);
                } catch (InterruptedException ignored) {}
            }).start();

        } catch (Exception ex) {
            showError("Erreur : " + ex.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean valid = true;

        if (titreField.getText().trim().isEmpty() || !titreField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) {
            titreError.setVisible(true);
            valid = false;
        }
        if (destinationComboBox.getValue() == null) {
            destinationError.setVisible(true);
            valid = false;
        }
        if (dateDepartPicker.getValue() == null) {
            dateDepartError.setVisible(true);
            valid = false;
        }
        if (dateRetourPicker.getValue() == null ||
                (dateDepartPicker.getValue() != null && dateRetourPicker.getValue().isBefore(dateDepartPicker.getValue()))) {
            dateRetourError.setVisible(true);
            valid = false;
        }
        if (prixField.getText().trim().isEmpty() || parseDouble(prixField.getText()) <= 0) {
            prixError.setVisible(true);
            valid = false;
        }
        if (nbPlacesField.getText().trim().isEmpty() || parseInt(nbPlacesField.getText()) <= 0) {
            nbPlacesError.setVisible(true);
            valid = false;
        }
        if (statutComboBox.getValue() == null) {
            statutError.setVisible(true);
            valid = false;
        }
        return valid;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        messageLabel.setText("❌ " + msg);
        messageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String msg) {
        messageLabel.setText("✓ " + msg);
        messageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
}