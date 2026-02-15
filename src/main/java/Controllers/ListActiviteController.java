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

    private final ServiceActivite service = new ServiceActivite();
    private ObservableList<Activite> list;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadData();
    }

    private void loadData() {
        try {
            containerActivites.getChildren().clear();
            list = FXCollections.observableArrayList(service.recuperer());

            for (Activite activite : list) {
                containerActivites.getChildren().add(createCard(activite));
            }

        } catch (SQLDataException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private HBox createCard(Activite activite) {

        // Informations
        VBox info = new VBox(8);
        Label nom = new Label(activite.getNom());
        nom.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label description = new Label(activite.getDescription());
        description.setStyle("-fx-text-fill:#475569;");
        Label details = new Label("📅 " + activite.getDateActivite() + " | 📍 " + activite.getLieu() + " | 💰 " + activite.getPrix() + " DT");
        details.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        info.getChildren().addAll(nom, description, details);

        // BOUTONS STYLE FORUMCONTROLLER
        FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.EYE);
        iconView.setFill(javafx.scene.paint.Color.web("#3498db"));
        iconView.setSize("18px");

        FontAwesomeIconView iconEdit = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        iconEdit.setFill(javafx.scene.paint.Color.web("#f39c12"));
        iconEdit.setSize("18px");

        FontAwesomeIconView iconDelete = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelete.setFill(javafx.scene.paint.Color.web("#e74c3c"));
        iconDelete.setSize("18px");

        Button btnView = new Button(); btnView.setGraphic(iconView);
        Button btnEdit = new Button(); btnEdit.setGraphic(iconEdit);
        Button btnDelete = new Button(); btnDelete.setGraphic(iconDelete);

        String baseStyle = "-fx-cursor: hand; -fx-min-width: 38; -fx-min-height: 38; " +
                "-fx-background-radius: 50; -fx-background-color: #f8f9fa; " +
                "-fx-border-color: #eee; -fx-border-radius: 50; -fx-border-width: 1;";

        btnView.setStyle(baseStyle);
        btnEdit.setStyle(baseStyle);
        btnDelete.setStyle(baseStyle);

        btnView.setOnMouseEntered(e -> btnView.setStyle(baseStyle + "-fx-background-color: #e1f5fe;"));
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(baseStyle + "-fx-background-color: #fff3e0;"));
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(baseStyle + "-fx-background-color: #ffebee;"));

        btnView.setOnMouseExited(e -> btnView.setStyle(baseStyle));
        btnEdit.setOnMouseExited(e -> btnEdit.setStyle(baseStyle));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle(baseStyle));

        // Actions au clic
        btnView.setOnAction(e -> showAlert("Détails", activite.getDescription(), Alert.AlertType.INFORMATION));
        btnEdit.setOnAction(e -> ouvrirUpdate(activite));
        btnDelete.setOnAction(e -> supprimerActivite(activite));

        HBox actions = new HBox(12, btnView, btnEdit, btnDelete);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Carte
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
    private void handleRechercher() {
        String keyword = txtRecherche.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        containerActivites.getChildren().clear();

        for (Activite a : list) {
            if (a.getNom().toLowerCase().contains(keyword)
                    || a.getLieu().toLowerCase().contains(keyword)
                    || a.getDescription().toLowerCase().contains(keyword)) {

                containerActivites.getChildren().add(createCard(a));
            }
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
