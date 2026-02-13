package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

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

    private final ServiceExcursion service = new ServiceExcursion();

    @FXML
    public void initialize() {
        loadTable();
    }

    private void loadTable() {
        try {
            List<Excursion> list = service.recuperer();
            tableExcursion.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    void ouvrirCreate(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/createExcursion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Excursion");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void ouvrirUpdate() throws Exception {
        Excursion selected = tableExcursion.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/updateExcursion.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        UpdateExcursionController controller = loader.getController();
        controller.setExcursion(selected);
        stage.showAndWait();
        loadTable();
    }

    @FXML
    private void handleDelete() {
        Excursion selected = tableExcursion.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            service.supprimer(selected.getIdExcursion());
            loadTable();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }
}
