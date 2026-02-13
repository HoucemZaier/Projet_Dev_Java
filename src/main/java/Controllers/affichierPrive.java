package Controllers;

import Models.TransportPrive;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.Services.ServiceTransportPrive;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLDataException;
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
    private TableView<TransportPrive> tableTransportPrive;

    @FXML
    private TableColumn<TransportPrive, Integer> colNumero;

    @FXML
    private TableColumn<TransportPrive, String> colNom;

    @FXML
    private TableColumn<TransportPrive, String> colImage;

    @FXML
    private TableColumn<TransportPrive, String> colEtat;

    @FXML
    private TableColumn<TransportPrive, Double> colPrix;

    @FXML
    private TableColumn<TransportPrive, Void> colActions;

    private final ServiceTransportPrive service = new ServiceTransportPrive();
    private final ObservableList<TransportPrive> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser la ComboBox de tri par prix
        if (triPrixComboBox != null && triPrixComboBox.getItems().isEmpty()) {
            triPrixComboBox.setItems(FXCollections.observableArrayList(
                    "Prix croissant",
                    "Prix décroissant"
            ));

            triPrixComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    trierParPrix(newVal);
                }
            });
        }

        // Configurer colonnes de base
        if (colNumero != null) colNumero.setCellValueFactory(new PropertyValueFactory<>("id_transport_priv"));
        if (colNom != null) colNom.setCellValueFactory(new PropertyValueFactory<>("marque"));
        if (colEtat != null) colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        if (colPrix != null) colPrix.setCellValueFactory(new PropertyValueFactory<>("prix_loc"));

        // Colonne image : affiche la miniature de la voiture à partir de image_path
        if (colImage != null) {
            colImage.setCellValueFactory(new PropertyValueFactory<>("image_path"));
            colImage.setCellFactory(column -> new TableCell<TransportPrive, String>() {
                private final ImageView imageView = new ImageView();

                {
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                }

                @Override
                protected void updateItem(String imagePath, boolean empty) {
                    super.updateItem(imagePath, empty);
                    if (empty || imagePath == null || imagePath.isEmpty()) {
                        setGraphic(null);
                    } else {
                        File file = new File(imagePath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString(), 80, 50, true, true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
        }

        // Colonne actions (Modifier / Supprimer)
        if (colActions != null) {
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button editButton = new Button("Modifier");
                private final Button deleteButton = new Button("Supprimer");
                private final HBox pane = new HBox(8, editButton, deleteButton);

                {
                    // Styles des boutons : bleu pour Modifier, rouge pour Supprimer
                    editButton.setStyle(
                            "-fx-background-color: #2563eb;" +  // bleu
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 999;"
                    );
                    deleteButton.setStyle(
                            "-fx-background-color: #ef4444;" +  // rouge
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 999;"
                    );

                    editButton.setOnAction(event -> {
                        TransportPrive tp = getTableView().getItems().get(getIndex());
                        openEditDialog(tp);
                    });

                    deleteButton.setOnAction(event -> {
                        TransportPrive tp = getTableView().getItems().get(getIndex());
                        handleDelete(tp);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(pane);
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

        // Attacher la liste au TableView
        if (tableTransportPrive != null) {
            tableTransportPrive.setItems(data);
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
     * Trie la liste des transports privés selon le prix de location.
     */
    private void trierParPrix(String critere) {
        if (critere == null || data.isEmpty()) {
            return;
        }

        FXCollections.sort(data, (a, b) -> {
            int cmp = Double.compare(a.getPrix_loc(), b.getPrix_loc());
            return "Prix décroissant".equals(critere) ? -cmp : cmp;
        });
    }

    private void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterTransportPrive.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editPrive.fxml"));
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
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
            } catch (SQLDataException e) {
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
}