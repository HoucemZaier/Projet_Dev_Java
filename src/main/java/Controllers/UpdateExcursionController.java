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
import java.util.ResourceBundle;

public class UpdateExcursionController implements Initializable {

    @FXML private TextField idField;
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

    @FXML private Button updateButton;
    @FXML private Button cancelButton;
    @FXML private Label messageLabel;

    // ✅ NOUVEAU : Carte
    @FXML private WebView mapWebView;
    @FXML private Label latLabel;
    @FXML private Label lngLabel;
    @FXML private Label mapHintLabel;

    private double selectedLat = 0;
    private double selectedLng = 0;
    private boolean locationSelected = false;

    private final ServiceExcursion serviceExcursion = new ServiceExcursion();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statutComboBox.setItems(FXCollections.observableArrayList("ouverte", "complète", "annulée"));

        try {
            destinationComboBox.setItems(FXCollections.observableArrayList(serviceExcursion.getAllDestinationNames()));
        } catch (Exception e) {
            showError("Erreur destinations : " + e.getMessage());
        }

        dateDepartPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) setDisable(true);
            }
        });

        dateRetourPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate depart = dateDepartPicker.getValue() != null ? dateDepartPicker.getValue() : LocalDate.now();
                if (date.isBefore(depart)) {
                    setDisable(true);
                    setStyle("-fx-background-color:#f0f0f0;");
                }
            }
        });

        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (dateRetourPicker.getValue() != null && newVal != null && dateRetourPicker.getValue().isBefore(newVal)) dateRetourPicker.setValue(newVal);
        });

        setupListeners();
        setupUpdateButtonBinding();
    }

    public void setExcursion(Excursion e) {
        if (e == null) return;

        idField.setText(String.valueOf(e.getIdExcursion()));
        titreField.setText(e.getTitre());
        destinationComboBox.setValue(e.getNomDestination());
        dateDepartPicker.setValue(e.getDateDepart().toLocalDate());
        dateRetourPicker.setValue(e.getDateRetour().toLocalDate());
        prixField.setText(String.valueOf(e.getPrix()));
        nbPlacesField.setText(String.valueOf(e.getNbPlaces()));
        statutComboBox.setValue(e.getStatut());

        // Initialiser la carte avec la position existante ou Tunis par défaut
        double lat = e.getLatitude() != null ? e.getLatitude() : 36.8065;
        double lng = e.getLongitude() != null ? e.getLongitude() : 10.1815;
        boolean hasExistingMarker = e.hasLocation();

        initMap(lat, lng, hasExistingMarker);

        if (hasExistingMarker) {
            selectedLat = lat;
            selectedLng = lng;
            locationSelected = true;
            latLabel.setText(String.format("%.6f", lat));
            lngLabel.setText(String.format("%.6f", lng));
            if (mapHintLabel != null) {
                mapHintLabel.setText("✅ Position enregistrée — cliquez pour changer");
                mapHintLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px;");
            }
        }
    }

    private void initMap(double defaultLat, double defaultLng, boolean hasMarker) {
        if (mapWebView == null) return;

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

        String html = buildLeafletHtml(defaultLat, defaultLng, hasMarker);
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
                    mapHintLabel.setText("✅ Position mise à jour !");
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
        String markerJs = hasMarker ? "currentMarker = L.marker([" + lat + "," + lng + "]).addTo(map);" +
                "currentMarker.bindPopup('📍 Position actuelle').openPopup();" : "var currentMarker = null;";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'/>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
                "<style>" +
                "html, body { margin:0; padding:0; height:100%; width:100%; }" +
                "#map { width:100%; height:100%; cursor:crosshair; }" +
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
            mapHintLabel.setText("Cliquez sur la carte pour sélectionner la position");
            mapHintLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        }

        initMap(36.8065, 10.1815, false);
    }

    private void setupListeners() {
        titreField.textProperty().addListener((obs, o, n) -> {
            if (n.trim().isEmpty()) {
                titreError.setText("Obligatoire");
                titreError.setVisible(true);
            } else if (!n.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                titreError.setText("Lettres uniquement");
                titreError.setVisible(true);
            } else {
                titreError.setVisible(false);
            }
        });

        destinationComboBox.valueProperty().addListener((obs, o, n) -> destinationError.setVisible(n == null || n.trim().isEmpty()));

        prixField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*(\\.\\d{0,2})?")) prixField.setText(o);
            prixError.setVisible(n.trim().isEmpty() || !isDoubleValid(n));
        });

        nbPlacesField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) nbPlacesField.setText(o);
            nbPlacesError.setVisible(n.trim().isEmpty() || !isIntValid(n));
        });

        statutComboBox.valueProperty().addListener((obs, o, n) -> statutError.setVisible(n == null));
        dateDepartPicker.valueProperty().addListener((obs, o, n) -> dateDepartError.setVisible(n == null));
        dateRetourPicker.valueProperty().addListener((obs, o, n) -> dateRetourError.setVisible(n == null));
    }

    private void setupUpdateButtonBinding() {
        updateButton.disableProperty().bind(
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
    private void handleUpdate() {
        if (!validateInputs()) return;

        try {
            Excursion e = new Excursion();
            e.setIdExcursion(Integer.parseInt(idField.getText()));
            e.setTitre(titreField.getText().trim());
            e.setNomDestination(destinationComboBox.getValue());
            e.setDateDepart(Date.valueOf(dateDepartPicker.getValue()));
            e.setDateRetour(Date.valueOf(dateRetourPicker.getValue()));
            e.setPrix(Double.parseDouble(prixField.getText()));
            e.setNbPlaces(Integer.parseInt(nbPlacesField.getText()));
            e.setStatut(statutComboBox.getValue());

            if (locationSelected) {
                e.setLatitude(selectedLat);
                e.setLongitude(selectedLng);
            }

            serviceExcursion.modifier(e);
            showSuccess("Excursion mise à jour !" + (locationSelected ? " 📍" : ""));

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
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

        if (dateRetourPicker.getValue() == null) {
            dateRetourError.setVisible(true);
            valid = false;
        }

        if (!isDoubleValid(prixField.getText())) {
            prixError.setVisible(true);
            valid = false;
        }

        if (!isIntValid(nbPlacesField.getText())) {
            nbPlacesError.setVisible(true);
            valid = false;
        }

        if (statutComboBox.getValue() == null) {
            statutError.setVisible(true);
            valid = false;
        }

        return valid;
    }

    private boolean isDoubleValid(String s) {
        try {
            return Double.parseDouble(s) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isIntValid(String s) {
        try {
            return Integer.parseInt(s) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void showError(String m) {
        messageLabel.setText("❌ " + m);
        messageLabel.setStyle("-fx-text-fill:#ef4444; -fx-font-weight:bold;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String m) {
        messageLabel.setText("✓ " + m);
        messageLabel.setStyle("-fx-text-fill:#10b981; -fx-font-weight:bold;");
        messageLabel.setVisible(true);
    }
}