package Controllers;

import Models.Excursion;
import Services.EmailService;
import Services.ServiceExcursion;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.ResourceBundle;

public class ListExcursionController implements Initializable {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> statutFilter;
    @FXML private Pagination pagination;
    @FXML private Label searchResultLabel;

    // === Données ===
    private final ServiceExcursion service = new ServiceExcursion();
    private ObservableList<Excursion> list = FXCollections.observableArrayList();
    private ObservableList<Excursion> favoris = FXCollections.observableArrayList();
    private ObservableList<Excursion> filteredList = FXCollections.observableArrayList();
    private final int ITEMS_PER_PAGE = 5;

    // Debounce timer pour recherche AJAX temps réel
    private PauseTransition searchDebounce;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statutFilter.getItems().addAll("Tous", "Ouverte", "Annulée", "Complète");
        statutFilter.setValue("Tous");

        // ✅ RECHERCHE AJAX TEMPS RÉEL avec debounce 250ms
        searchDebounce = new PauseTransition(Duration.millis(250));
        searchDebounce.setOnFinished(e -> filtrerExcursions());

        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDebounce.stop();
            searchDebounce.playFromStart();
        });

        statutFilter.valueProperty().addListener((obs, oldVal, newVal) -> filtrerExcursions());

        pagination.setPageFactory(this::createPage);

        loadData();
    }

    // ===== RECHERCHE & FILTRES =====
    private void filtrerExcursions() {
        filteredList.clear();
        String keyword = txtRecherche.getText() == null ? "" : txtRecherche.getText().toLowerCase().trim();
        String statut = statutFilter.getValue();

        for (Excursion e : list) {
            String titre = e.getTitre() == null ? "" : e.getTitre().toLowerCase();
            String dest = e.getNomDestination() == null ? "" : e.getNomDestination().toLowerCase();
            String statutEx = e.getStatut() == null ? "" : e.getStatut();

            boolean matchKw = keyword.isEmpty() || titre.contains(keyword) || dest.contains(keyword);
            boolean matchStat = "Tous".equalsIgnoreCase(statut) || statutEx.equalsIgnoreCase(statut);

            if (matchKw && matchStat) filteredList.add(e);
        }

        // Feedback temps réel
        if (searchResultLabel != null) {
            if (filteredList.isEmpty()) {
                searchResultLabel.setText("🔍 Aucun résultat pour \"" + txtRecherche.getText() + "\"");
                searchResultLabel.setStyle("-fx-text-fill: #ef4444;");
            } else {
                searchResultLabel.setText("✅ " + filteredList.size() + " excursion(s) trouvée(s)");
                searchResultLabel.setStyle("-fx-text-fill: #10b981;");
            }
        }

        updatePagination();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);

        int current = pagination.getCurrentPageIndex();
        if (current >= pageCount) current = 0;
        pagination.setCurrentPageIndex(current);

        pagination.setPageFactory(this::createPage);
    }

    private VBox createPage(int pageIndex) {
        VBox pageBox = new VBox(15);
        pageBox.setFillWidth(true);

        int from = pageIndex * ITEMS_PER_PAGE;
        int to = Math.min(from + ITEMS_PER_PAGE, filteredList.size());

        if (filteredList.isEmpty()) {
            Label noResult = new Label("🔍 Aucune excursion trouvée.");
            noResult.setStyle("-fx-text-fill:#ef4444; -fx-font-style:italic; -fx-font-size:14px;");
            pageBox.getChildren().add(noResult);
            return pageBox;
        }

        for (int i = from; i < to; i++) {
            pageBox.getChildren().add(createCard(filteredList.get(i)));
        }

        return pageBox;
    }

    private void loadData() {
        try {
            list = FXCollections.observableArrayList(service.recuperer());
            filteredList = FXCollections.observableArrayList(list);
            if (searchResultLabel != null) searchResultLabel.setText("✅ " + list.size() + " excursion(s) chargée(s)");
            updatePagination();
        } catch (SQLDataException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ===== CRÉATION CARTE =====
    private HBox createCard(Excursion e) {
        VBox info = new VBox(8);

        Label titre = new Label(e.getTitre());
        titre.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        // Indicateur GPS
        String gpsTag = e.hasLocation() ? " 📍" : "";

        Label details = new Label(
                "📍 " + e.getNomDestination() + " | 📅 " + e.getDateDepart() + " → " + e.getDateRetour() + " | 💰 " + e.getPrix() + " DT" +
                        " | 🪑 " + e.getNbPlaces() + " place(s)" + " | " + e.getStatut() + gpsTag
        );
        details.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");

        info.getChildren().addAll(titre, details);

        // BOUTONS ACTIONS
        FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.EYE);
        iconView.setFill(Color.web("#3498db"));
        iconView.setSize("18px");

        FontAwesomeIconView iconEdit = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        iconEdit.setFill(Color.web("#f39c12"));
        iconEdit.setSize("18px");

        FontAwesomeIconView iconDelete = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelete.setFill(Color.web("#e74c3c"));
        iconDelete.setSize("18px");

        FontAwesomeIconView iconEmail = new FontAwesomeIconView(FontAwesomeIcon.ENVELOPE);
        iconEmail.setFill(Color.web("#10b981"));
        iconEmail.setSize("18px");

        FontAwesomeIconView iconFav = new FontAwesomeIconView(FontAwesomeIcon.STAR);
        iconFav.setFill(favoris.contains(e) ? Color.GOLD : Color.LIGHTGRAY);
        iconFav.setSize("18px");

        // ✅ NOUVEAU : Bouton Carte OSM
        Button btnMap = new Button("🗺️ Carte");
        btnMap.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-background-radius:8; -fx-cursor:hand; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:5 10;");
        btnMap.setVisible(e.hasLocation());
        btnMap.setOnAction(ev -> ouvrirCarteExcursion(e));

        Button btnView = new Button();
        btnView.setGraphic(iconView);

        Button btnEdit = new Button();
        btnEdit.setGraphic(iconEdit);

        Button btnDelete = new Button();
        btnDelete.setGraphic(iconDelete);

        Button btnFav = new Button();
        btnFav.setGraphic(iconFav);

        Button btnEmail = new Button();
        btnEmail.setGraphic(iconEmail);

        String baseStyle = "-fx-cursor:hand; -fx-min-width:38; -fx-min-height:38; -fx-background-radius:50; -fx-background-color:#f8f9fa; -fx-border-color:#eee; -fx-border-radius:50; -fx-border-width:1;";
        btnView.setStyle(baseStyle);
        btnEdit.setStyle(baseStyle);
        btnDelete.setStyle(baseStyle);
        btnEmail.setStyle(baseStyle);
        btnFav.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

        // Hover
        btnView.setOnMouseEntered(ev -> btnView.setStyle(baseStyle + "-fx-background-color:#e1f5fe;"));
        btnEdit.setOnMouseEntered(ev -> btnEdit.setStyle(baseStyle + "-fx-background-color:#fff3e0;"));
        btnDelete.setOnMouseEntered(ev -> btnDelete.setStyle(baseStyle + "-fx-background-color:#ffebee;"));

        btnView.setOnMouseExited(ev -> btnView.setStyle(baseStyle));
        btnEdit.setOnMouseExited(ev -> btnEdit.setStyle(baseStyle));
        btnDelete.setOnMouseExited(ev -> btnDelete.setStyle(baseStyle));

        // Actions
        btnView.setOnAction(ev -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Titre : ").append(e.getTitre()).append("\n");
            sb.append("Destination : ").append(e.getNomDestination()).append("\n");
            sb.append("Dates : ").append(e.getDateDepart()).append(" → ").append(e.getDateRetour()).append("\n");
            sb.append("Prix : ").append(e.getPrix()).append(" DT\n");
            sb.append("Places : ").append(e.getNbPlaces()).append("\n");
            sb.append("Statut : ").append(e.getStatut());
            if (e.hasLocation()) sb.append("\n📍 GPS : (").append(e.getLatitude()).append(", ").append(e.getLongitude()).append(")");
            showAlert("Détails de l'excursion", sb.toString(), Alert.AlertType.INFORMATION);
        });

        btnEdit.setOnAction(ev -> ouvrirUpdate(e));

        btnDelete.setOnAction(ev -> supprimerExcursion(e));

        btnFav.setOnAction(ev -> {
            if (favoris.contains(e)) {
                favoris.remove(e);
                ((FontAwesomeIconView) btnFav.getGraphic()).setFill(Color.LIGHTGRAY);
            } else {
                favoris.add(e);
                ((FontAwesomeIconView) btnFav.getGraphic()).setFill(Color.GOLD);
            }
        });

        btnEmail.setOnAction(ev -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Envoyer Email");
            dialog.setHeaderText("Adresse email du destinataire");
            dialog.showAndWait().ifPresent(email -> {
                try {
                    EmailService.sendExcursionEmail(email, ListExcursionController.buildEmailContent(e));
                    showAlert("Succès", "Email envoyé !", Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    showAlert("Erreur", "Impossible d'envoyer l'email.", Alert.AlertType.ERROR);
                }
            });
        });

        HBox actions = new HBox(10, btnMap, btnView, btnEdit, btnDelete, btnEmail, btnFav);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox card = new HBox(40, info, actions);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("""
            -fx-background-color:white;
            -fx-padding:20;
            -fx-background-radius:12;
            -fx-border-radius:12;
            -fx-border-color:#e2e8f0;
            -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06),10,0,0,4);
            """);
        HBox.setHgrow(info, Priority.ALWAYS);

        return card;
    }

    // ===== ACTIONS =====

    /**
     * ✅ NOUVEAU : Ouvre la carte OSM pour voir la position de l'excursion
     */
    private void ouvrirCarteExcursion(Excursion e) {
        try {
            Stage stage = new Stage();
            stage.setTitle("📍 " + e.getTitre() + " — Position sur la carte");
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox root = new VBox(0);
            root.setStyle("-fx-background-color:#1e293b;");

            // Header
            HBox header = new HBox(10);
            header.setStyle("-fx-padding:12 18; -fx-background-color:#1e293b;");

            Label titleLbl = new Label("🗺️ " + e.getTitre() + " — " + e.getNomDestination());
            titleLbl.setStyle("-fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold;");

            Label coordLbl = new Label(String.format("GPS : %.5f, %.5f", e.getLatitude(), e.getLongitude()));
            coordLbl.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            header.getChildren().addAll(titleLbl, spacer, coordLbl);

            // Carte Leaflet centrée sur l'excursion
            WebView webView = new WebView();
            webView.setPrefSize(700, 480);

            WebEngine engine = webView.getEngine();
            engine.setJavaScriptEnabled(true);

            engine.setUserAgent(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 " +
                            "ExcursionApp/1.0 (motaz@tunis; contact: motaz@example.com)"
            );

            engine.setOnError(ev -> System.out.println("WEB ERROR: " + ev.getMessage()));
            engine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
                if (newEx != null) {
                    System.out.println("LOAD EXCEPTION: " + newEx.getMessage());
                    newEx.printStackTrace();
                }
            });

            String html = buildMapViewHtml(e);
            engine.loadContent(html);

            engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    Platform.runLater(() -> {
                        // Forçage initial
                        engine.executeScript(
                                "function forceRedraw() { if (map) { map.invalidateSize(true); map._onResize(); } }" +
                                        "setTimeout(forceRedraw, 200);" +
                                        "setTimeout(forceRedraw, 600);" +
                                        "setTimeout(forceRedraw, 1200);" +
                                        "setTimeout(forceRedraw, 2000);"
                        );

                        webView.requestFocus();
                    });
                }
            });

            // Listener sur taille
            webView.widthProperty().addListener((obs, old, newVal) -> {
                if (engine.getDocument() != null) {
                    engine.executeScript("if (map) map.invalidateSize();");
                }
            });
            webView.heightProperty().addListener((obs, old, newVal) -> {
                if (engine.getDocument() != null) {
                    engine.executeScript("if (map) map.invalidateSize();");
                }
            });

            root.getChildren().addAll(header, webView);

            stage.setScene(new Scene(root, 700, 530));
            stage.show();
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'ouvrir la carte : " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String buildMapViewHtml(Excursion e) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.8.0/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.8.0/dist/leaflet.js'></script>" +
                "<style>*{margin:0;padding:0;}#map{width:100%;height:480px;}</style></head><body>" +
                "<div id='map'></div><script>" +
                "var map=L.map('map').setView([" + e.getLatitude() + "," + e.getLongitude() + "],10);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png',{" +
                "attribution:'Données &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> — Tuiles &copy; <a href=\"https://www.openstreetmap.fr\">OpenStreetMap France</a>'" +
                "}).addTo(map);" +
                "var marker=L.marker([" + e.getLatitude() + "," + e.getLongitude() + "]).addTo(map);" +
                "marker.bindPopup('<b>🗺️ " + e.getTitre().replace("'", "\\'") + "</b><br>📍 " + (e.getNomDestination() != null ? e.getNomDestination().replace("'","\\'"): "") + "<br>📅 " + e.getDateDepart() + " → " + e.getDateRetour() + "<br>💰 " + e.getPrix() + " DT | " + e.getStatut() + "').openPopup();" +
                "</script></body></html>";
    }

    private void supprimerExcursion(Excursion e) {
        String statut = e.getStatut() == null ? "" : e.getStatut().trim();
        if (statut.equalsIgnoreCase("Ouverte")) {
            showAlert("Erreur", "Impossible de supprimer une excursion ouverte !", Alert.AlertType.ERROR);
            return;
        }

        try {
            service.supprimer(e.getIdExcursion());
            loadData();
            showAlert("Succès", "Excursion supprimée.", Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void ouvrirUpdate(Excursion e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateExcursion.fxml"));
            Parent root = loader.load();
            UpdateExcursionController ctrl = loader.getController();
            ctrl.setExcursion(e);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Excursion");
            stage.show();

            stage.setOnHiding(ev -> loadData());
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAjouterExcursion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/createExcursion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Excursion");
            stage.show();

            stage.setOnHiding(ev -> loadData());
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFavoris() {
        if (favoris.isEmpty()) {
            showAlert("Favoris", "Aucun favori.", Alert.AlertType.INFORMATION);
            return;
        }

        filteredList = FXCollections.observableArrayList(favoris);
        updatePagination();
        if (searchResultLabel != null) searchResultLabel.setText("⭐ " + favoris.size() + " favori(s)");
    }

    /** ✅ NOUVEAU : Ouvre le chatbot IA */
    @FXML
    private void handleChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatbot.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("🤖 PlanovaBot — Assistant IA");
            stage.setResizable(true);
            stage.show();
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'ouvrir le chatbot : " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /** ✅ NOUVEAU : Ouvre le calendrier interactif */
    @FXML
    private void handleCalendar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/calendar.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("📅 Calendrier Planova");
            stage.setResizable(true);
            stage.show();
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'ouvrir le calendrier : " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleNotifications() {
        showAlert("Notifications", "Utilisez l'icône cloche pour envoyer un email.", Alert.AlertType.INFORMATION);
    }

    // ===== EMAIL =====
    public static String buildEmailContent(Excursion excursion) {
        return "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'><style>" +
                "body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0;}" +
                ".container{max-width:650px;margin:30px auto;background:white;border-radius:12px;overflow:hidden;}" +
                ".header{background:linear-gradient(135deg,#10b981,#0DA2E7);color:white;padding:40px;text-align:center;}" +
                ".content{padding:35px;}.card{background:#f8fafc;border-radius:10px;padding:20px;}" +
                ".row{display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #e2e8f0;}" +
                ".label{color:#64748b;}.value{font-weight:bold;color:#1e293b;}" +
                ".footer{background:#1e293b;color:white;text-align:center;padding:20px;font-size:13px;}" +
                "</style></head><body><div class='container'>" +
                "<div class='header'><h1>🌍 PlaNova Excursion</h1><p>Une nouvelle aventure vous attend ✨</p></div>" +
                "<div class='content'><div class='card'>" +
                "<div class='row'><span class='label'>Excursion</span><span class='value'>" + excursion.getTitre() + "</span></div>" +
                "<div class='row'><span class='label'>Destination</span><span class='value'>" + excursion.getNomDestination() + "</span></div>" +
                "<div class='row'><span class='label'>Dates</span><span class='value'>" + excursion.getDateDepart() + " → " + excursion.getDateRetour() + "</span></div>" +
                "<div class='row'><span class='label'>Prix</span><span class='value'>" + excursion.getPrix() + " DT</span></div>" +
                "<div class='row'><span class='label'>Places</span><span class='value'>" + excursion.getNbPlaces() + "</span></div>" +
                "<div class='row'><span class='label'>Statut</span><span class='value'>" + excursion.getStatut() + "</span></div>" +
                (excursion.hasLocation() ? "<div class='row'><span class='label'>📍 GPS</span><span class='value'>" + excursion.getLatitude() + ", " + excursion.getLongitude() + "</span></div>" : "") +
                "</div></div>" +
                "<div class='footer'><p>© 2026 PlaNova - Tous droits réservés</p></div></div></body></html>";
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}