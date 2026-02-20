package Controllers;

import Models.TransportPrive;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import utils.Services.ServiceTransportPrive;

import java.io.File;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.List;
import java.util.ResourceBundle;

public class AfficheClientPrive implements Initializable {

    @FXML
    private javafx.scene.control.ComboBox<String> triPrixComboBox;

    @FXML
    private ListView<TransportPrive> listTransportPrive;

    private final ServiceTransportPrive service = new ServiceTransportPrive();
    private final ObservableList<TransportPrive> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // --- même logique de TRI que dans affichierPrive.java ---
        if (triPrixComboBox != null) {
            if (triPrixComboBox.getItems().isEmpty()) {
                triPrixComboBox.setItems(FXCollections.observableArrayList(
                        "Prix Moins Cher",
                        "Prix Plus Cher"
                ));
            }

            triPrixComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    trierParPrix(newVal);
                }
            });
        }

        // --- Cellules de la ListView : carte + bouton "Louer Voiture" JAUNE ---
        if (listTransportPrive != null) {
            listTransportPrive.setCellFactory((Callback<ListView<TransportPrive>, ListCell<TransportPrive>>) listView ->
                    new ListCell<TransportPrive>() {
                        private final HBox card = new HBox(12);
                        private final ImageView imageView = new ImageView();
                        private final VBox infos = new VBox(4);
                        private final Label lblMarque = new Label();
                        private final Label lblEtat = new Label();
                        private final Label lblPrix = new Label();
                        private final HBox actions = new HBox(8);
                        private final Button btnLouer = new Button("Louer Voiture");

                        {
                            imageView.setFitWidth(80);
                            imageView.setFitHeight(60);
                            imageView.setPreserveRatio(true);

                            lblMarque.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                            lblEtat.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                            lblPrix.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

                            infos.getChildren().addAll(lblMarque, lblEtat);

                            // Bouton JAUNE "Louer Voiture"
                            btnLouer.setStyle(
                                    "-fx-background-color: #facc15;" +   // jaune
                                            "-fx-text-fill: #1f2937;" +
                                            "-fx-font-size: 12px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-padding: 6 14 6 14;" +
                                            "-fx-background-radius: 999;" +
                                            "-fx-cursor: hand;"
                            );

                            actions.getChildren().add(btnLouer);

                            card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; " +
                                    "-fx-padding: 12; -fx-alignment: CENTER_LEFT;");

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            card.getChildren().addAll(imageView, infos, lblPrix, spacer, actions);
                        }

                        @Override
                        protected void updateItem(TransportPrive item, boolean empty) {
                            super.updateItem(item, empty);

                            if (empty || item == null) {
                                setGraphic(null);
                                setStyle("-fx-background-color: transparent;");
                            } else {
                                lblMarque.setText(item.getMarque());
                                lblEtat.setText("État : " + (item.getEtat() != null ? item.getEtat() : "—"));
                                lblPrix.setText(String.format("%.2f DT", item.getPrix_loc()));

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

                                // Action de location (à adapter selon ton flow)
                                btnLouer.setOnAction(e -> handleLouer(item));

                                setGraphic(card);
                                setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
                            }
                        }
                    });
        }

        // Charger les données et les lier à la ListView
        loadData();
        if (listTransportPrive != null) {
            listTransportPrive.setItems(data);
        }
    }

    private void loadData() {
        try {
            List<TransportPrive> list = service.recuperer();
            data.setAll(list != null ? list : List.of());
        } catch (SQLDataException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors du chargement des données", e.getMessage());
        }
    }

    /**
     * Même logique que dans affichierPrive :
     * "Prix Moins Cher"  -> tri croissant
     * "Prix Plus Cher"   -> tri décroissant
     */
    private void trierParPrix(String critere) {
        if (critere == null || data.isEmpty()) {
            return;
        }

        boolean triMoinsCher = "Prix Moins Cher".equalsIgnoreCase(critere);
        boolean triPlusCher = "Prix Plus Cher".equalsIgnoreCase(critere);

        if (!triMoinsCher && !triPlusCher) {
            return;
        }

        FXCollections.sort(data, (a, b) -> {
            int cmp = Double.compare(a.getPrix_loc(), b.getPrix_loc());
            return triPlusCher ? -cmp : cmp;
        });
    }

    /**
     * Action quand l'utilisateur clique sur "Louer Voiture"
     * -> Ici simple popup, à remplacer par ton flux réel de réservation.
     */
    private void handleLouer(TransportPrive tp) {
        String msg = "Vous avez choisi de louer la voiture : "
                + tp.getMarque()
                + " (prix " + tp.getPrix_loc() + " DT).";
        showAlert(Alert.AlertType.INFORMATION, "Louer voiture", msg);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}