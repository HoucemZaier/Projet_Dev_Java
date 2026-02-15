package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
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

public class ListExcursionController implements Initializable {

    @FXML private VBox containerExcursions;
    @FXML private TextField txtRecherche;

    private final ServiceExcursion service = new ServiceExcursion();
    private ObservableList<Excursion> list;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadData();
    }

    private void loadData() {
        try {
            containerExcursions.getChildren().clear();
            list = FXCollections.observableArrayList(service.recuperer());

            for (Excursion e : list) {
                containerExcursions.getChildren().add(createCard(e));
            }

        } catch (SQLDataException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private HBox createCard(Excursion e) {

        // Informations principales
        VBox info = new VBox(8);
        Label titre = new Label(e.getTitre());
        titre.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        Label destination = new Label("📍 " + e.getDestination());
        destination.setStyle("-fx-text-fill:#475569;");

        Label dates = new Label("📅 " + e.getDateDepart() + " → " + e.getDateRetour());
        dates.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");

        Label prix = new Label("💰 " + e.getPrix() + " DT   |   🪑 " + e.getNbPlaces() + " places   |   " + e.getStatut());
        prix.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");

        info.getChildren().addAll(titre, destination, dates, prix);

        // Boutons avec icônes
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

        btnView.setOnMouseEntered(ev -> btnView.setStyle(baseStyle + "-fx-background-color: #e1f5fe;"));
        btnEdit.setOnMouseEntered(ev -> btnEdit.setStyle(baseStyle + "-fx-background-color: #fff3e0;"));
        btnDelete.setOnMouseEntered(ev -> btnDelete.setStyle(baseStyle + "-fx-background-color: #ffebee;"));

        btnView.setOnMouseExited(ev -> btnView.setStyle(baseStyle));
        btnEdit.setOnMouseExited(ev -> btnEdit.setStyle(baseStyle));
        btnDelete.setOnMouseExited(ev -> btnDelete.setStyle(baseStyle));

        // Actions des boutons
        btnView.setOnAction(ev -> showAlert("Détails",
                "Titre: " + e.getTitre() +
                        "\nDestination: " + e.getDestination() +
                        "\nDates: " + e.getDateDepart() + " → " + e.getDateRetour() +
                        "\nPrix: " + e.getPrix() +
                        "\nNb Places: " + e.getNbPlaces() +
                        "\nStatut: " + e.getStatut(), Alert.AlertType.INFORMATION));

        btnEdit.setOnAction(ev -> ouvrirUpdate(e));
        btnDelete.setOnAction(ev -> supprimerExcursion(e));

        HBox actions = new HBox(12, btnView, btnEdit, btnDelete);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // Carte HBox
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

    private void supprimerExcursion(Excursion e) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Suppression");
        confirm.setContentText("Supprimer l'excursion : " + e.getTitre() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(e.getIdExcursion());
                    loadData();
                    showAlert("Succès", "Excursion supprimée !", Alert.AlertType.INFORMATION);
                } catch (SQLDataException ex) {
                    showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void ouvrirUpdate(Excursion e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateExcursion.fxml"));
            Parent root = loader.load();
            UpdateExcursionController controller = loader.getController();
            controller.setExcursion(e);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Excursion");
            stage.show();

            stage.setOnHiding(event -> loadData());
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification.", Alert.AlertType.ERROR);
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

            stage.setOnHiding(event -> loadData());
        } catch (Exception ex) {
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

        containerExcursions.getChildren().clear();
        for (Excursion e : list) {
            if (e.getTitre().toLowerCase().contains(keyword)
                    || e.getDestination().toLowerCase().contains(keyword)) {
                containerExcursions.getChildren().add(createCard(e));
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
