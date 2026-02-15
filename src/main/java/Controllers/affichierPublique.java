package Controllers;

import Models.TransportPublique;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.util.Callback;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.Services.ServiceTransportPublique;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class affichierPublique implements Initializable {

    @FXML
    private ListView<TransportPublique> listTransportPublique;

    @FXML
    private Button btnAjouter;

    private final ServiceTransportPublique service = new ServiceTransportPublique();
    private ObservableList<TransportPublique> transportData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        loadTransportsFromDatabase();

        if (btnAjouter != null) {
            btnAjouter.setOnAction(this::handleAjouterTransport);
        }
    }

    private void configureListView() {
        if (listTransportPublique == null) return;
        listTransportPublique.setCellFactory((Callback<ListView<TransportPublique>, ListCell<TransportPublique>>) listView -> new ListCell<TransportPublique>() {
            private final HBox card = new HBox(12);
            private final ImageView imageView = new ImageView();
            private final VBox infos = new VBox(4);
            private final Label lblType = new Label();
            private final Label lblHoraire = new Label();
            private final Label lblTarif = new Label();
            private final HBox actions = new HBox(8);
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");

            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
                lblType.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                lblHoraire.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                lblTarif.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
                infos.getChildren().addAll(lblType, lblHoraire);
                btnEdit.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-cursor: hand;");
                actions.getChildren().addAll(btnEdit, btnDelete);
                card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 12; -fx-alignment: CENTER_LEFT;");
                card.getChildren().addAll(imageView, infos, lblTarif, new javafx.scene.layout.Region() {{ HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS); }}, actions);
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
                    btnEdit.setOnAction(e -> { openEditDialog(item); reloadTable(); });
                    btnDelete.setOnAction(e -> handleDeleteTransport(item));
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
                }
            }
        });
    }

    private void loadTransportsFromDatabase() {
        try {
            List<TransportPublique> list = service.recuperer();
            transportData = FXCollections.observableArrayList(list);
            if (listTransportPublique != null) {
                listTransportPublique.setItems(transportData);
            }
        } catch (SQLDataException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Impossible de charger les transports publics : " + e.getMessage());
        }
    }

    private void reloadTable() {
        loadTransportsFromDatabase();
    }

    private void handleDeleteTransport(TransportPublique transport) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer ce transport ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.supprimer(transport);
            reloadTable();
        }
    }

    private void openEditDialog(TransportPublique transport) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editPublique.fxml"));
            Parent root = loader.load();

            editPublique controller = loader.getController();
            if (controller != null) {
                controller.setTransport(transport);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier Transport Public");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
        }
    }

    private void handleAjouterTransport(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterTransportPublique.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Transport Public");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            reloadTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}