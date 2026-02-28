package com.PlaNova.controllers;

import com.PlaNova.models.TransportPrive;
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
import javafx.scene.control.ComboBox;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.PlaNova.services.ServiceTransportPrive;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class affichierPrive implements Initializable {

    @FXML
    // ComboBox pour trier les voitures selon le prix de location
    private ComboBox<String> triPrixComboBox;

    @FXML
    private ComboBox<String> etatComboBox;

    @FXML
    private Button ajouterButton;

    @FXML
    private ListView<TransportPrive> listTransportPrive;

    private final ServiceTransportPrive service = new ServiceTransportPrive();
    private final ObservableList<TransportPrive> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser / brancher la ComboBox de tri par prix
        if (triPrixComboBox != null) {
            // Si aucun item n'est défini dans le FXML, on les ajoute en Java
            if (triPrixComboBox.getItems().isEmpty()) {
                triPrixComboBox.setItems(FXCollections.observableArrayList(
                        "Prix Moins Cher",
                        "Prix Plus Cher"));
            }

            // Listener sur le choix sélectionné
            triPrixComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    trierParPrix(newVal);
                }
            });
        }

        // Configurer ListView avec cellules type carte
        if (listTransportPrive != null) {
            listTransportPrive.setCellFactory(
                    (Callback<ListView<TransportPrive>, ListCell<TransportPrive>>) listView -> new ListCell<TransportPrive>() {
                        private final HBox card = new HBox(12);
                        private final ImageView imageView = new ImageView();
                        private final VBox infos = new VBox(4);
                        private final Label lblMarque = new Label();
                        private final Label lblEtat = new Label();
                        private final Label lblPrix = new Label();
                        private final HBox actions = new HBox(8);
                        private final Button btnEdit = new Button("Modifier");
                        private final Button btnDelete = new Button("Supprimer");

                        {
                            imageView.setFitWidth(80);
                            imageView.setFitHeight(60);
                            imageView.setPreserveRatio(true);
                            lblMarque.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                            lblEtat.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
                            lblPrix.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
                            infos.getChildren().addAll(lblMarque, lblEtat);
                            btnEdit.setStyle(
                                    "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-cursor: hand;");
                            btnDelete.setStyle(
                                    "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-cursor: hand;");
                            actions.getChildren().addAll(btnEdit, btnDelete);
                            card.setStyle(
                                    "-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 12; -fx-alignment: CENTER_LEFT;");
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
                                    Image image = loadImage(imagePath);
                                    if (image != null && !image.isError()) {
                                        imageView.setImage(image);
                                    } else {
                                        // Image par défaut pour transport privé
                                        imageView.setImage(loadDefaultCarImage());
                                    }
                                } else {
                                    // Image par défaut pour transport privé
                                    imageView.setImage(loadDefaultCarImage());
                                }
                                btnEdit.setOnAction(e -> openEditDialog(item));
                                btnDelete.setOnAction(e -> handleDelete(item));
                                setGraphic(card);
                                setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
                            }
                        }
                    });
        }

        // Bouton "Ajouter une voiture"
        if (ajouterButton != null) {
            ajouterButton.setOnAction(this::handleAjouter);
        }

        // Charger données depuis la base
        loadData();

        // Attacher la liste au ListView
        if (listTransportPrive != null) {
            listTransportPrive.setItems(data);
        }
    }

    private void loadData() {
        try {
            List<TransportPrive> list = service.recuperer();
            data.setAll(list != null ? list : List.of());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors du chargement des données", e.getMessage());
        }
    }

    /**
     * Trie la liste des transports privés selon le prix de location.
     * "Prix Moins Cher" -> tri croissant
     * "Prix Plus Cher" -> tri décroissant
     */
    private void trierParPrix(String critere) {
        if (critere == null || data.isEmpty()) {
            return;
        }

        boolean triMoinsCher = "Prix Moins Cher".equalsIgnoreCase(critere);
        boolean triPlusCher = "Prix Plus Cher".equalsIgnoreCase(critere);

        if (!triMoinsCher && !triPlusCher) {
            // Autre valeur => on ne fait rien
            return;
        }

        FXCollections.sort(data, (a, b) -> {
            int cmp = Double.compare(a.getPrix_loc(), b.getPrix_loc());
            return triPlusCher ? -cmp : cmp;
        });
    }

    private void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/transport/ajoutertransportprive.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Transport Privé");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recharger la liste après ajout
            loadData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout : " + e.getMessage());
        }
    }

    private void openEditDialog(TransportPrive tp) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/transport/editprive.fxml"));
            Parent root = loader.load();

            editPrive controller = loader.getController();
            if (controller != null) {
                controller.setTransport(tp);
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier un Transport Privé");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recharger après modification
            loadData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
        }
    }

    private void handleDelete(TransportPrive tp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le transport");
        confirm.setContentText("Voulez-vous vraiment supprimer l'élément id=" + tp.getId_transport_priv() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(tp);
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Supprimé", "Transport supprimé avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Charge une image depuis le chemin (fichier local ou ressource)
     */
    private Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        imagePath = imagePath.trim();

        try {
            // Essayer de charger comme ressource d'abord (pour les images dans resources/)
            if (!imagePath.startsWith("/") && !imagePath.startsWith("file:") &&
                    !imagePath.startsWith("http") && !imagePath.startsWith("https")) {

                // Si le chemin ne commence pas par /images/, l'ajouter
                String resourcePath = imagePath.startsWith("images/") ? "/" + imagePath : "/images/" + imagePath;

                URL resourceUrl = getClass().getResource(resourcePath);
                if (resourceUrl != null) {
                    return new Image(resourceUrl.toExternalForm(), 80, 60, true, true);
                }
            }

            // Essayer de charger comme fichier local
            File file = new File(imagePath);
            if (file.exists()) {
                return new Image(file.toURI().toString(), 80, 60, true, true);
            }

            // Essayer avec le préfixe file:
            if (!imagePath.startsWith("file:")) {
                String fileUrl = "file:" + imagePath.replace("\\", "/");
                Image img = new Image(fileUrl, 80, 60, true, true);
                if (!img.isError()) {
                    return img;
                }
            }

            // Dernier essai : charger directement le chemin
            return new Image(imagePath, 80, 60, true, true);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Charge une image par défaut pour les voitures de transport privé
     */
    private Image loadDefaultCarImage() {
        // Utiliser une image de voiture par défaut
        return loadDefaultImage("toyota 1.png");
    }

    /**
     * Charge une image par défaut depuis les ressources
     */
    private Image loadDefaultImage(String imageName) {
        try {
            URL imageUrl = getClass().getResource("/images/" + imageName);
            if (imageUrl != null) {
                return new Image(imageUrl.toExternalForm(), 80, 60, true, true);
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger l'image par défaut: " + imageName);
        }
        return null;
    }
}