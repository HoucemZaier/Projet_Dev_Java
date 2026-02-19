package Controllers;

import Models.Activite;
import Services.ServiceActivite;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.ResourceBundle;

public class ListActiviteController implements Initializable {

    @FXML private VBox containerActivites;
    @FXML private TextField txtRecherche;
    @FXML private Button btnTriPrix;

    private final ServiceActivite service = new ServiceActivite();
    private ObservableList<Activite> list;
    private ObservableList<Activite> favoris = FXCollections.observableArrayList();
    private boolean triCroissant = true; // pour suivre la direction du tri

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadData();

        // 🔹 Recherche en temps réel
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerActivites(newVal));
    }

    private void loadData() {
        try {
            containerActivites.getChildren().clear();
            list = FXCollections.observableArrayList(service.recuperer());
            filtrerActivites(txtRecherche.getText());

        } catch (SQLDataException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filtrerActivites(String keyword) {
        containerActivites.getChildren().clear();
        if (keyword == null) keyword = "";
        String lowerKeyword = keyword.toLowerCase();

        for (Activite a : list) {
            boolean matchesNom = a.getNom() != null && a.getNom().toLowerCase().contains(lowerKeyword);
            boolean matchesLieu = a.getLieu() != null && a.getLieu().toLowerCase().contains(lowerKeyword);
            boolean matchesDescription = a.getDescription() != null && a.getDescription().toLowerCase().contains(lowerKeyword);

            if (matchesNom || matchesLieu || matchesDescription) {
                containerActivites.getChildren().add(createCard(a));
            }
        }

        if (containerActivites.getChildren().isEmpty()) {
            Label noResult = new Label("Aucune activité trouvée pour : " + keyword);
            noResult.setStyle("-fx-text-fill:#ef4444; -fx-font-style:italic;");
            containerActivites.getChildren().add(noResult);
        }
    }

    // 🔹 Tri dynamique par prix avec flèche
    @FXML
    private void trierPrixDynamic() {
        if(triCroissant) {
            FXCollections.sort(list, (a,b) -> Double.compare(a.getPrix(), b.getPrix()));
            btnTriPrix.setText("Trier par prix ⬇️");
        } else {
            FXCollections.sort(list, (a,b) -> Double.compare(b.getPrix(), a.getPrix()));
            btnTriPrix.setText("Trier par prix ⬆️");
        }
        triCroissant = !triCroissant;
        filtrerActivites(txtRecherche.getText());
    }

    private HBox createCard(Activite activite) {
        VBox info = new VBox(8);
        Label nom = new Label(activite.getNom());
        nom.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label description = new Label(activite.getDescription());
        description.setStyle("-fx-text-fill:#475569;");
        Label summary = new Label("📅 " + activite.getDateActivite() + " | 📍 " + activite.getLieu() + " | 💰 " + activite.getPrix() + " DT");
        summary.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        info.getChildren().addAll(nom, description, summary);

        FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.EYE);
        iconView.setFill(javafx.scene.paint.Color.web("#3498db"));
        iconView.setSize("18px");

        FontAwesomeIconView iconEdit = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        iconEdit.setFill(javafx.scene.paint.Color.web("#f39c12"));
        iconEdit.setSize("18px");

        FontAwesomeIconView iconDelete = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelete.setFill(javafx.scene.paint.Color.web("#e74c3c"));
        iconDelete.setSize("18px");

        FontAwesomeIconView iconFav = new FontAwesomeIconView(FontAwesomeIcon.STAR);
        iconFav.setFill(favoris.contains(activite) ? javafx.scene.paint.Color.GOLD : javafx.scene.paint.Color.LIGHTGRAY);
        iconFav.setSize("18px");

        Button btnView = new Button(); btnView.setGraphic(iconView);
        Button btnEdit = new Button(); btnEdit.setGraphic(iconEdit);
        Button btnDelete = new Button(); btnDelete.setGraphic(iconDelete);
        Button btnFav = new Button(); btnFav.setGraphic(iconFav);
        btnFav.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        btnFav.setOnAction(e -> {
            if(favoris.contains(activite)) {
                favoris.remove(activite);
                iconFav.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            } else {
                favoris.add(activite);
                iconFav.setFill(javafx.scene.paint.Color.GOLD);
            }
        });

        String baseStyle = "-fx-cursor: hand; -fx-min-width: 38; -fx-min-height: 38; -fx-background-radius: 50; -fx-background-color: #f8f9fa; -fx-border-color: #eee; -fx-border-radius: 50; -fx-border-width: 1;";
        btnView.setStyle(baseStyle);
        btnEdit.setStyle(baseStyle);
        btnDelete.setStyle(baseStyle);

        btnView.setOnMouseEntered(e -> btnView.setStyle(baseStyle + "-fx-background-color: #e1f5fe;"));
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(baseStyle + "-fx-background-color: #fff3e0;"));
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(baseStyle + "-fx-background-color: #ffebee;"));
        btnView.setOnMouseExited(e -> btnView.setStyle(baseStyle));
        btnEdit.setOnMouseExited(e -> btnEdit.setStyle(baseStyle));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle(baseStyle));

        btnView.setOnAction(e -> {
            StringBuilder details = new StringBuilder();
            details.append("Nom : ").append(activite.getNom()).append("\n");
            details.append("Description : ").append(activite.getDescription()).append("\n");
            details.append("Date : ").append(activite.getDateActivite()).append("\n");
            details.append("Heure : ").append(activite.getHeureActivite()).append("\n");
            details.append("Lieu : ").append(activite.getLieu()).append("\n");
            details.append("Prix : ").append(activite.getPrix()).append(" DT").append("\n");
            details.append("Excursion ID : ").append(activite.getIdExcursion()).append("\n");
            details.append("Destination ID : ").append(activite.getIdDestination());

            showAlert("Détails de l'activité", details.toString(), Alert.AlertType.INFORMATION);
        });
        btnEdit.setOnAction(e -> ouvrirUpdate(activite));
        btnDelete.setOnAction(e -> supprimerActivite(activite));

        HBox actions = new HBox(12, btnView, btnEdit, btnDelete, btnFav);
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

    private void supprimerActivite(Activite activite) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Suppression");
        confirm.setContentText("Supprimer l'activité : " + activite.getNom() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(activite.getIdActivite());
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
            UpdateActiviteController controller = loader.getController();
            controller.setActivite(activite);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Activité");
            stage.show();

            stage.setOnHiding(event -> loadData());

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification.", Alert.AlertType.ERROR);
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

            stage.setOnHiding(event -> loadData());

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void afficherStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatsActivite.fxml"));
            Parent root = loader.load();
            StatsActiviteController controller = loader.getController();
            controller.setActivites(list);

            Stage stage = new Stage();
            stage.setTitle("Dashboard Activités");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le tableau de bord.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
