package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.SQLDataException;
import java.util.List;

public class ListExcursionController {

    @FXML private TableView<Excursion> tableExcursion;
    @FXML private TableColumn<Excursion, Integer> idColumn;
    @FXML private TableColumn<Excursion, String> titreColumn;
    @FXML private TableColumn<Excursion, String> destinationColumn;
    @FXML private TableColumn<Excursion, String> dateDepartColumn;
    @FXML private TableColumn<Excursion, String> dateRetourColumn;
    @FXML private TableColumn<Excursion, Double> prixColumn;
    @FXML private TableColumn<Excursion, Integer> nbPlacesColumn;
    @FXML private TableColumn<Excursion, String> statutColumn;
    @FXML private TableColumn<Excursion, Void> actionColumn; // colonne pour les boutons

    @FXML private TextField txtRecherche;

    private final ServiceExcursion service = new ServiceExcursion();
    private ObservableList<Excursion> list;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdExcursion()).asObject());
        titreColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitre()));
        destinationColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDestination()));
        dateDepartColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getDateDepart())));
        dateRetourColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getDateRetour())));
        prixColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrix()).asObject());
        nbPlacesColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getNbPlaces()).asObject());
        statutColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut()));

        loadData();
        addButtonsToTable();
    }

    private void loadData() {
        try {
            List<Excursion> excursions = service.recuperer();
            list = FXCollections.observableArrayList(excursions);
            tableExcursion.setItems(list);
        } catch (SQLDataException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void addButtonsToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnUpdate = new Button("✏");
            private final Button btnDelete = new Button("🗑");

            {
                btnUpdate.setStyle("-fx-background-color:#f59e0b; -fx-text-fill:white;");
                btnDelete.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white;");
                btnUpdate.setTooltip(new Tooltip("Modifier"));
                btnDelete.setTooltip(new Tooltip("Supprimer"));

                btnUpdate.setOnAction(event -> {
                    Excursion e = getTableView().getItems().get(getIndex());
                    ouvrirUpdate(e);
                });

                btnDelete.setOnAction(event -> {
                    Excursion e = getTableView().getItems().get(getIndex());
                    supprimerExcursion(e);
                });
            }

            private final HBox pane = new HBox(5, btnUpdate, btnDelete);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
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
                    list.remove(e);
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
            ex.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtRecherche.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            tableExcursion.setItems(list);
            return;
        }

        ObservableList<Excursion> filteredList = FXCollections.observableArrayList();
        for (Excursion e : list) {
            if (e.getTitre().toLowerCase().contains(keyword) ||
                    e.getDestination().toLowerCase().contains(keyword)) {
                filteredList.add(e);
            }
        }
        tableExcursion.setItems(filteredList);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
