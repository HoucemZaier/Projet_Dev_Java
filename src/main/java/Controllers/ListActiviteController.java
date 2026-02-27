package Controllers;

import Models.Activite;
import Models.Notification;
import Services.NotificationService;
import Services.ServiceActivite;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.ResourceBundle;

public class ListActiviteController implements Initializable {

    @FXML private VBox containerActivites;
    @FXML private TextField txtRecherche;
    @FXML private Button btnTriPrix;
    @FXML private Button btnNotifications;
    @FXML private Label searchResultLabel;
    @FXML private Pagination pagination;

    private final ServiceActivite service = new ServiceActivite();
    private ObservableList<Activite> list;
    private ObservableList<Activite> favoris = FXCollections.observableArrayList();
    private boolean triCroissant = true;
    private final int ITEMS_PER_PAGE = 5;
    private ObservableList<Activite> filteredList = FXCollections.observableArrayList();

    // Debounce pour recherche AJAX
    private PauseTransition searchDebounce;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadData();
        pagination.setPageFactory(this::createPage);

        // ✅ RECHERCHE AJAX TEMPS RÉEL avec debounce 250ms
        searchDebounce = new PauseTransition(Duration.millis(250));
        searchDebounce.setOnFinished(e -> filtrerActivites(txtRecherche.getText()));

        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDebounce.stop();
            searchDebounce.playFromStart();
        });

        updateNotificationBadge();
    }

    // ===== NOTIFICATIONS =====

    private void addNotification(String message) {
        NotificationService.getInstance().addNotification(message);
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        int unread = NotificationService.getInstance().getUnreadCount();
        btnNotifications.setText("🔔 (" + unread + ")");
    }

    @FXML
    private void handleNotifications() {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa;");

        ObservableList<Notification> notifications = NotificationService.getInstance().getNotifications();
        if (notifications.isEmpty()) {
            Label empty = new Label("Aucune notification.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-style:italic; -fx-font-size:14px;");
            root.getChildren().add(empty);
        } else {
            for (Notification item : notifications) {
                HBox card = new HBox(10);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color:white; -fx-padding:15; -fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#e2e8f0; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06), 8,0,0,3);");

                FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.BELL);
                icon.setFill(javafx.scene.paint.Color.web("#0DA2E7")); icon.setSize("20px");

                VBox textBox = new VBox(3);
                Label message = new Label(item.getMessage());
                message.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a; -fx-font-size:14px;");
                Label badge = new Label(item.isRead() ? "" : "NOUVEAU");
                badge.setStyle("-fx-font-size:11px; -fx-text-fill:white; -fx-background-color:#ef4444; -fx-padding:2 6 2 6; -fx-background-radius:8;");
                textBox.getChildren().addAll(message, badge);

                Button btnDel = new Button();
                FontAwesomeIconView delIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                delIcon.setFill(javafx.scene.paint.Color.web("#e74c3c")); delIcon.setSize("16px");
                btnDel.setGraphic(delIcon);
                btnDel.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
                btnDel.setOnAction(e -> {
                    NotificationService.getInstance().removeNotification(item);
                    root.getChildren().remove(card);
                    updateNotificationBadge();
                });

                HBox.setHgrow(textBox, Priority.ALWAYS);
                card.getChildren().addAll(icon, textBox, btnDel);
                root.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-padding:5;");
        stage.setScene(new Scene(scroll, 420, 500));
        stage.setTitle("Notifications");
        stage.show();

        NotificationService.getInstance().markAllAsRead();
        updateNotificationBadge();
    }

    // ===== DONNÉES =====

    private void loadData() {
        try {
            list = FXCollections.observableArrayList(service.recuperer());
            filteredList.setAll(list);
            updatePagination();
            pagination.setPageFactory(this::createPage);
            if (searchResultLabel != null)
                searchResultLabel.setText("✅ " + list.size() + " activité(s) chargée(s)");
        } catch (SQLDataException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filtrerActivites(String keyword) {
        filteredList.clear();
        if (keyword == null) keyword = "";
        String lk = keyword.toLowerCase();

        for (Activite a : list) {
            boolean matchNom = a.getNom() != null && a.getNom().toLowerCase().contains(lk);
            boolean matchLieu = a.getLieu() != null && a.getLieu().toLowerCase().contains(lk);
            boolean matchDesc = a.getDescription() != null && a.getDescription().toLowerCase().contains(lk);
            if (matchNom || matchLieu || matchDesc) filteredList.add(a);
        }

        // ✅ Feedback visuel temps réel
        if (searchResultLabel != null) {
            if (filteredList.isEmpty()) {
                searchResultLabel.setText("🔍 Aucun résultat pour \"" + keyword + "\"");
                searchResultLabel.setStyle("-fx-text-fill: #ef4444;");
            } else {
                searchResultLabel.setText("✅ " + filteredList.size() + " activité(s) trouvée(s)");
                searchResultLabel.setStyle("-fx-text-fill: #10b981;");
            }
        }

        updatePagination();
        pagination.setPageFactory(this::createPage);
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.setCurrentPageIndex(0);
    }

    private VBox createPage(int pageIndex) {
        VBox pageBox = new VBox(15);
        pageBox.setStyle("-fx-padding:20 35;");

        int from = pageIndex * ITEMS_PER_PAGE;
        int to = Math.min(from + ITEMS_PER_PAGE, filteredList.size());

        if (filteredList.isEmpty()) {
            Label noResult = new Label("🔍 Aucune activité trouvée.");
            noResult.setStyle("-fx-text-fill:#ef4444; -fx-font-style:italic;");
            pageBox.getChildren().add(noResult);
            return pageBox;
        }

        for (int i = from; i < to; i++) pageBox.getChildren().add(createCard(filteredList.get(i)));
        return pageBox;
    }

    // ===== TRI =====

    @FXML
    private void trierPrixDynamic() {
        if (triCroissant) {
            FXCollections.sort(list, (a, b) -> Double.compare(a.getPrix(), b.getPrix()));
            btnTriPrix.setText("Trier par prix ⬇️");
        } else {
            FXCollections.sort(list, (a, b) -> Double.compare(b.getPrix(), a.getPrix()));
            btnTriPrix.setText("Trier par prix ⬆️");
        }
        triCroissant = !triCroissant;
        filtrerActivites(txtRecherche.getText());
    }

    // ===== CARTE ACTIVITÉ =====

    private HBox createCard(Activite activite) {
        VBox info = new VBox(8);
        Label nom = new Label(activite.getNom());
        nom.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label description = new Label(activite.getDescription());
        description.setStyle("-fx-text-fill:#475569;");
        Label summary = new Label(
                "📅 " + activite.getDateActivite() +
                        " ⏰ " + activite.getHeureActivite() +
                        " | 📍 " + activite.getLieu() +
                        " | 💰 " + activite.getPrix() + " DT");
        summary.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        info.getChildren().addAll(nom, description, summary);

        FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.EYE);
        iconView.setFill(javafx.scene.paint.Color.web("#3498db")); iconView.setSize("18px");
        FontAwesomeIconView iconEdit = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        iconEdit.setFill(javafx.scene.paint.Color.web("#f39c12")); iconEdit.setSize("18px");
        FontAwesomeIconView iconDelete = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelete.setFill(javafx.scene.paint.Color.web("#e74c3c")); iconDelete.setSize("18px");
        FontAwesomeIconView iconShare = new FontAwesomeIconView(FontAwesomeIcon.SHARE);
        iconShare.setFill(javafx.scene.paint.Color.web("#0DA2E7")); iconShare.setSize("18px");
        FontAwesomeIconView iconFav = new FontAwesomeIconView(FontAwesomeIcon.STAR);
        iconFav.setFill(favoris.contains(activite) ? javafx.scene.paint.Color.GOLD : javafx.scene.paint.Color.LIGHTGRAY);
        iconFav.setSize("18px");

        Button btnView = new Button(); btnView.setGraphic(iconView);
        Button btnEdit = new Button(); btnEdit.setGraphic(iconEdit);
        Button btnDelete = new Button(); btnDelete.setGraphic(iconDelete);
        Button btnShare = new Button(); btnShare.setGraphic(iconShare);
        Button btnFav = new Button(); btnFav.setGraphic(iconFav);
        btnFav.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");
        btnShare.setStyle("-fx-background-color:transparent; -fx-cursor:hand;");

        String baseStyle = "-fx-cursor:hand; -fx-min-width:38; -fx-min-height:38; -fx-background-radius:50; -fx-background-color:#f8f9fa; -fx-border-color:#eee; -fx-border-radius:50; -fx-border-width:1;";
        btnView.setStyle(baseStyle); btnEdit.setStyle(baseStyle); btnDelete.setStyle(baseStyle);

        btnView.setOnMouseEntered(e -> btnView.setStyle(baseStyle + "-fx-background-color:#e1f5fe;"));
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(baseStyle + "-fx-background-color:#fff3e0;"));
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(baseStyle + "-fx-background-color:#ffebee;"));
        btnView.setOnMouseExited(e -> btnView.setStyle(baseStyle));
        btnEdit.setOnMouseExited(e -> btnEdit.setStyle(baseStyle));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle(baseStyle));

        btnFav.setOnAction(e -> {
            if (favoris.contains(activite)) {
                favoris.remove(activite); iconFav.setFill(javafx.scene.paint.Color.LIGHTGRAY);
                addNotification("Retiré des favoris : " + activite.getNom());
            } else {
                favoris.add(activite); iconFav.setFill(javafx.scene.paint.Color.GOLD);
                addNotification("Ajouté aux favoris : " + activite.getNom());
            }
        });

        btnView.setOnAction(e -> {
            String d = "Nom : " + activite.getNom() + "\nDescription : " + activite.getDescription() +
                    "\nDate : " + activite.getDateActivite() + "\nHeure : " + activite.getHeureActivite() +
                    "\nLieu : " + activite.getLieu() + "\nPrix : " + activite.getPrix() + " DT";
            showAlert("Détails de l'activité", d, Alert.AlertType.INFORMATION);
        });

        btnEdit.setOnAction(e -> ouvrirUpdate(activite));
        btnDelete.setOnAction(e -> supprimerActivite(activite));

        btnShare.setOnAction(e -> {
            String contenu = "Activité : " + activite.getNom() + "\nDate : " + activite.getDateActivite() +
                    " à " + activite.getHeureActivite() + "\nLieu : " + activite.getLieu() + "\nPrix : " + activite.getPrix() + " DT";
            ClipboardContent content = new ClipboardContent();
            content.putString(contenu);
            Clipboard.getSystemClipboard().setContent(content);
            showAlert("Succès", "Informations copiées.", Alert.AlertType.INFORMATION);
        });

        HBox actions = new HBox(12, btnView, btnEdit, btnDelete, btnShare, btnFav);
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

    private void supprimerActivite(Activite activite) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation"); confirm.setHeaderText("Suppression");
        confirm.setContentText("Supprimer l'activité : " + activite.getNom() + " ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    service.supprimer(activite.getIdActivite());
                    addNotification("Activité supprimée : " + activite.getNom());
                    loadData();
                    showAlert("Succès", "Activité supprimée !", Alert.AlertType.INFORMATION);
                } catch (SQLDataException e) {
                    showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void ouvrirUpdate(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateActivite.fxml"));
            Parent root = loader.load();
            UpdateActiviteController ctrl = loader.getController();
            ctrl.setActivite(activite);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Activité");
            stage.show();
            stage.setOnHiding(ev -> { loadData(); addNotification("Activité modifiée : " + activite.getNom()); });
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la modification.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAjouterActivite() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterActivite.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Activité");
            stage.show();
            stage.setOnHiding(ev -> { loadData(); addNotification("Nouvelle activité ajoutée"); });
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'ajout.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFavoris() {
        if (favoris.isEmpty()) { showAlert("Favoris", "Aucun favori.", Alert.AlertType.INFORMATION); return; }
        containerActivites.getChildren().clear();
        for (Activite a : favoris) containerActivites.getChildren().add(createCard(a));
    }

    @FXML
    private void afficherStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatsActivite.fxml"));
            Parent root = loader.load();
            StatsActiviteController ctrl = loader.getController();
            ctrl.setActivites(list);
            Stage stage = new Stage();
            stage.setTitle("Dashboard Activités");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir les stats.", Alert.AlertType.ERROR);
        }
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
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le chatbot : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /** ✅ NOUVEAU : Ouvre le calendrier */
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
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le calendrier : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }
}
