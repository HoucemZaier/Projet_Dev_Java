package com.PlaNova.controllers;

import com.PlaNova.models.TransportPublique;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import com.PlaNova.services.ServiceTransportPublique;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class affichierPublique implements Initializable {

    @FXML
    private ListView<TransportPublique> listTransportPublique;

    @FXML
    private Button btnAjouter;

    @FXML
    private ComboBox<String> comboRechercheType;

    @FXML
    private ComboBox<String> comboTri;

    private final ServiceTransportPublique service = new ServiceTransportPublique();

    // Liste brute venant de la base
    private final ObservableList<TransportPublique> transportData = FXCollections.observableArrayList();

    // Vues filtrée / triée de la liste
    private FilteredList<TransportPublique> filteredData;
    private SortedList<TransportPublique> sortedData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        setupFilteringAndSorting();
        loadTransportsFromDatabase();

        if (btnAjouter != null) {
            btnAjouter.setOnAction(this::handleAjouterTransport);
        }
    }

    private void configureListView() {
        if (listTransportPublique == null)
            return;

        listTransportPublique.setCellFactory(
                (Callback<ListView<TransportPublique>, ListCell<TransportPublique>>) listView -> new ListCell<TransportPublique>() {

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

                        btnEdit.setStyle(
                                "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-cursor: hand;");
                        btnDelete.setStyle(
                                "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-cursor: hand;");

                        actions.getChildren().addAll(btnEdit, btnDelete);

                        card.setStyle(
                                "-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 12; -fx-alignment: CENTER_LEFT;");
                        card.getChildren().addAll(
                                imageView,
                                infos,
                                lblTarif,
                                new javafx.scene.layout.Region() {
                                    {
                                        HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS);
                                    }
                                },
                                actions);
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
                                Image image = loadImage(imagePath);
                                if (image != null && !image.isError()) {
                                    imageView.setImage(image);
                                } else {
                                    // Image par défaut pour transport public
                                    imageView.setImage(loadDefaultTransportImage(item.getType()));
                                }
                            } else {
                                // Image par défaut selon le type de transport
                                imageView.setImage(loadDefaultTransportImage(item.getType()));
                            }

                            btnEdit.setOnAction(e -> {
                                openEditDialog(item);
                                reloadTable();
                            });

                            btnDelete.setOnAction(e -> handleDeleteTransport(item));

                            setGraphic(card);
                            setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");
                        }
                    }
                });
    }

    /**
     * Initialise la FilteredList / SortedList et relie les ComboBox.
     */
    private void setupFilteringAndSorting() {
        // Vues dérivées autour de la liste principale
        filteredData = new FilteredList<>(transportData, t -> true);
        sortedData = new SortedList<>(filteredData);

        if (listTransportPublique != null) {
            listTransportPublique.setItems(sortedData);
        }

        if (comboRechercheType != null) {
            comboRechercheType.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> applyFiltersAndSorting());
        }

        if (comboTri != null) {
            comboTri.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> applyFiltersAndSorting());
        }
    }

    private void loadTransportsFromDatabase() {
        try {
            List<TransportPublique> list = service.recuperer();
            transportData.setAll(list); // on recharge la liste brute
            applyFiltersAndSorting(); // on applique le filtre/tri courant
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger les transports publics : " + e.getMessage());
        }
    }

    /**
     * Applique le filtrage par type et le tri par tarif suivant les ComboBox.
     */
    private void applyFiltersAndSorting() {
        if (filteredData == null) {
            return;
        }

        String selectedType = comboRechercheType != null
                ? comboRechercheType.getSelectionModel().getSelectedItem()
                : null;

        String selectedTri = comboTri != null
                ? comboTri.getSelectionModel().getSelectedItem()
                : null;

        // ---- Filtrage par type ----
        filteredData.setPredicate(tp -> {
            if (tp == null) {
                return false;
            }

            // Aucun filtre, ou "Tous" sélectionné -> tous les transports affichés
            if (selectedType == null || selectedType.isEmpty()) {
                return true;
            }
            String normalizedSelectedRaw = selectedType.trim().toLowerCase();
            if (normalizedSelectedRaw.equals("tous") || normalizedSelectedRaw.equals("tous les types")) {
                return true;
            }

            String normalizedSelected = normalizeType(selectedType);
            String normalizedType = normalizeType(tp.getType());

            // Si le type filtré (bus / taxi / metro) correspond au type du transport
            return normalizedSelected.equalsIgnoreCase(normalizedType);
        });

        // ---- Tri par tarif (du moins cher au plus cher) ----
        if (sortedData != null) {
            if (selectedTri != null && selectedTri.equalsIgnoreCase("Tarif")) {
                sortedData.setComparator(Comparator.comparingDouble(TransportPublique::getTarif));
            } else {
                // Pas de tri spécifique
                sortedData.setComparator(null);
            }
        }
    }

    /**
     * Normalise une valeur de type pour ignorer emojis / majuscules / accents.
     */
    private String normalizeType(String value) {
        if (value == null) {
            return "";
        }
        String v = value.toLowerCase();

        if (v.contains("bus")) {
            return "bus";
        }
        if (v.contains("taxi")) {
            return "taxi";
        }
        if (v.contains("metro") || v.contains("métro")) {
            return "metro";
        }
        return v.trim();
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
            try {
                service.supprimer(transport);
                reloadTable();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le transport: " + e.getMessage());
            }
        }
    }

    private void openEditDialog(TransportPublique transport) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/transport/editpublique.fxml"));
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
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
        }
    }

    private void handleAjouterTransport(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/transport/ajoutertransportpublique.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Transport Public");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            reloadTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre d'ajout : " + e.getMessage());
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
     * Charge une image par défaut selon le type de transport public
     */
    private Image loadDefaultTransportImage(String type) {
        if (type == null) {
            return loadDefaultImage("bus3.png");
        }

        String normalizedType = type.toLowerCase();
        if (normalizedType.contains("bus")) {
            return loadDefaultImage("bus3.png");
        } else if (normalizedType.contains("taxi")) {
            return loadDefaultImage("taxi1.png");
        } else if (normalizedType.contains("metro") || normalizedType.contains("métro")) {
            return loadDefaultImage("metro1.png");
        }

        // Image par défaut
        return loadDefaultImage("bus3.png");
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