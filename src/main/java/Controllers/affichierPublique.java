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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
    private TableView<TransportPublique> tableWidget;

    @FXML
    private TableColumn<TransportPublique, Integer> colId;

    @FXML
    private TableColumn<TransportPublique, String> colImage;

    @FXML
    private TableColumn<TransportPublique, String> colType;

    @FXML
    private TableColumn<TransportPublique, Double> colTarif;

    @FXML
    private TableColumn<TransportPublique, String> colHoraire;

    @FXML
    private TableColumn<TransportPublique, Void> colActions;

    @FXML
    private Button btnAjouter;

    private final ServiceTransportPublique service = new ServiceTransportPublique();
    private ObservableList<TransportPublique> transportData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        configureActionsColumn();
        loadTransportsFromDatabase();

        if (btnAjouter != null) {
            btnAjouter.setOnAction(this::handleAjouterTransport);
        }
    }

    private void configureTableColumns() {
        if (colId != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("id_transport_pub"));
        }
        if (colType != null) {
            colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (colTarif != null) {
            colTarif.setCellValueFactory(new PropertyValueFactory<>("tarif"));
        }
        if (colHoraire != null) {
            colHoraire.setCellValueFactory(new PropertyValueFactory<>("horaire"));
        }

        if (colImage != null) {
            colImage.setCellValueFactory(new PropertyValueFactory<>("image_path"));
            colImage.setCellFactory(column -> new TableCell<TransportPublique, String>() {
                private final ImageView imageView = new ImageView();

                {
                    imageView.setFitWidth(60);
                    imageView.setFitHeight(40);
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
                            Image image = new Image(file.toURI().toString(), 60, 40, true, true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
        }
    }

    private void configureActionsColumn() {
        if (colActions == null) {
            return;
        }

        colActions.setCellFactory(column -> new TableCell<TransportPublique, Void>() {

            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox container = new HBox(5, btnEdit, btnDelete);

            {
                // Styles des boutons : bleu pour Modifier, rouge pour Supprimer
                btnEdit.setStyle(
                        "-fx-background-color: #2563eb;" +  // bleu
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 999;"
                );
                btnDelete.setStyle(
                        "-fx-background-color: #ef4444;" +  // rouge
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 999;"
                );

                btnEdit.setOnAction(event -> {
                    TransportPublique selected = getTableView().getItems().get(getIndex());
                    if (selected != null) {
                        openEditDialog(selected);
                        reloadTable();
                    }
                });

                btnDelete.setOnAction(event -> {
                    TransportPublique selected = getTableView().getItems().get(getIndex());
                    if (selected != null) {
                        handleDeleteTransport(selected);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void loadTransportsFromDatabase() {
        try {
            List<TransportPublique> list = service.recuperer();
            transportData = FXCollections.observableArrayList(list);
            if (tableWidget != null) {
                tableWidget.setItems(transportData);
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