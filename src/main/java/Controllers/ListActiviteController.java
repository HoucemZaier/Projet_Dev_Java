package Controllers;

import Models.Activite;
import Services.ServiceActivite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.ResourceBundle;

public class ListActiviteController implements Initializable {

    @FXML private TableView<Activite> tableActivite;
    @FXML private TableColumn<Activite, Integer> colId;
    @FXML private TableColumn<Activite, String> colNom;
    @FXML private TableColumn<Activite, String> colDescription;
    @FXML private TableColumn<Activite, String> colDate;
    @FXML private TableColumn<Activite, String> colHeure;
    @FXML private TableColumn<Activite, String> colLieu;
    @FXML private TableColumn<Activite, Double> colPrix;
    @FXML private TableColumn<Activite, Integer> colExcursion;
    @FXML private TableColumn<Activite, Integer> colDestination;
    @FXML private TableColumn<Activite, Void> colActions;

    @FXML private TextField txtRecherche; // champ texte pour la recherche

    private final ServiceActivite service = new ServiceActivite();
    private ObservableList<Activite> list;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdActivite()).asObject());
        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
        colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDateActivite() != null ? data.getValue().getDateActivite().toString() : ""
                )
        );
        colHeure.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getHeureActivite() != null ? data.getValue().getHeureActivite().toString() : ""
                )
        );
        colLieu.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLieu()));
        colPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrix()).asObject());
        colExcursion.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdExcursion()).asObject());
        colDestination.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdDestination()).asObject());

        loadData();
        addButtonsToTable();
    }

    private void loadData() {
        try {
            list = FXCollections.observableArrayList(service.recuperer());
            tableActivite.setItems(list);
        } catch (SQLDataException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void addButtonsToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnUpdate = new Button("✏");
            private final Button btnDelete = new Button("🗑");

            {
                btnUpdate.setStyle("-fx-background-color:#f59e0b; -fx-text-fill:white;");
                btnDelete.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white;");
                btnUpdate.setTooltip(new Tooltip("Modifier"));
                btnDelete.setTooltip(new Tooltip("Supprimer"));

                btnUpdate.setOnAction(event -> {
                    Activite a = getTableView().getItems().get(getIndex());
                    ouvrirUpdate(a);
                });

                btnDelete.setOnAction(event -> {
                    Activite a = getTableView().getItems().get(getIndex());
                    supprimerActivite(a);
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

    private void supprimerActivite(Activite activite) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Suppression");
        confirm.setContentText("Supprimer l'activité : " + activite.getNom() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(activite.getIdActivite());
                    list.remove(activite);
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
            e.printStackTrace();
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

            stage.setOnHiding(event -> loadData()); // rafraîchir après ajout
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtRecherche.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            tableActivite.setItems(list);
            return;
        }

        ObservableList<Activite> filteredList = FXCollections.observableArrayList();

        for (Activite a : list) {
            if (a.getNom().toLowerCase().contains(keyword) ||
                    a.getLieu().toLowerCase().contains(keyword) ||
                    a.getDescription().toLowerCase().contains(keyword)) {
                filteredList.add(a);
            }
        }

        tableActivite.setItems(filteredList);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
