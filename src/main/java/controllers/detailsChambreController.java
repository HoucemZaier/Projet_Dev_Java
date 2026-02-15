package controllers;

import models.Chambre;
import models.Hotel;
import services.ServiceChambre;
import services.ServiceHotel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.List;
import java.util.ResourceBundle;

public class detailsChambreController implements Initializable {

    @FXML
    private Label chambreNumberLabel;
    @FXML
    private Label chambreTitleLabel;
    @FXML
    private Circle statutIndicator;
    @FXML
    private Label statutLabel;

    // Informations générales
    @FXML
    private Label hotelNameLabel;
    @FXML
    private Label hotelAddressLabel;
    @FXML
    private Label hotelPhoneLabel;
    @FXML
    private Label hotelEmailLabel;
    @FXML
    private Label chambreTypeLabel;
    @FXML
    private Label capaciteLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private Label etageLabel;
    @FXML
    private Label vueLabel;

    // Équipements
    @FXML
    private HBox wifiBox;
    @FXML
    private Label wifiLabel;
    @FXML
    private HBox tvBox;
    @FXML
    private Label tvLabel;
    @FXML
    private HBox climatisationBox;
    @FXML
    private Label climatisationLabel;
    @FXML
    private HBox miniBarBox;
    @FXML
    private Label miniBarLabel;
    @FXML
    private HBox balconBox;
    @FXML
    private Label balconLabel;
    @FXML
    private HBox coffreBox;
    @FXML
    private Label coffreLabel;
    @FXML
    private HBox telephoneBox;
    @FXML
    private Label telephoneLabel;
    @FXML
    private HBox chauffageBox;
    @FXML
    private Label chauffageLabel;

    // Statistiques et dates
    @FXML
    private Label dateCreationLabel;
    @FXML
    private Label derniereReservationLabel;
    @FXML
    private Label totalReservationsLabel;
    @FXML
    private Label chiffreAffaireLabel;

    // Description
    @FXML
    private Label descriptionLabel;

    // Notes
    @FXML
    private Label noteLabel;
    @FXML
    private Label avisLabel;

    private Chambre chambre;
    private ServiceHotel serviceHotel;
    private ServiceChambre serviceChambre;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceHotel = new ServiceHotel();
        serviceChambre = new ServiceChambre();
    }

    /**
     * Méthode pour recevoir la chambre à afficher
     */
    public void setChambre(Chambre chambre) {
        this.chambre = chambre;
        loadChambreDetails();
        loadHotelDetails();
        loadStatistics();
    }

    private void loadChambreDetails() {
        if (chambre != null) {
            // En-tête
            chambreNumberLabel.setText("Chambre N° " + chambre.getIdChambre());
            chambreTitleLabel.setText(chambre.getTypeChambre() + " - " + chambre.getStatutChambre());

            // Statut avec indicateur coloré
            statutLabel.setText(chambre.getStatutChambre());
            switch (chambre.getStatutChambre().toLowerCase()) {
                case "disponible":
                    statutIndicator.setFill(Color.web("#10b981"));
                    break;
                case "occupée":
                    statutIndicator.setFill(Color.web("#ef4444"));
                    break;
                case "en maintenance":
                    statutIndicator.setFill(Color.web("#f59e0b"));
                    break;
                case "réservée":
                    statutIndicator.setFill(Color.web("#3b82f6"));
                    break;
                default:
                    statutIndicator.setFill(Color.web("#64748b"));
            }

            // Informations générales
            chambreTypeLabel.setText(chambre.getTypeChambre());
            capaciteLabel.setText(chambre.getCapacite() + " personne" + (chambre.getCapacite() > 1 ? "s" : ""));
            prixLabel.setText(String.format("%.2f € / nuit", chambre.getPrixChambre()));

            // Informations supplémentaires (simulées - à adapter selon votre DB)
            etageLabel.setText("2ème étage");
            vueLabel.setText("Vue sur mer");

            // Équipements (simulés - à adapter selon votre DB)
            setEquipementStatus(wifiBox, wifiLabel, true);
            setEquipementStatus(tvBox, tvLabel, true);
            setEquipementStatus(climatisationBox, climatisationLabel, true);
            setEquipementStatus(miniBarBox, miniBarLabel, Math.random() > 0.5);
            setEquipementStatus(balconBox, balconLabel, Math.random() > 0.7);
            setEquipementStatus(coffreBox, coffreLabel, Math.random() > 0.6);
            setEquipementStatus(telephoneBox, telephoneLabel, true);
            setEquipementStatus(chauffageBox, chauffageLabel, true);

            // Description
            descriptionLabel.setText("Chambre " + chambre.getTypeChambre().toLowerCase() +
                    " spacieuse et confortable, idéale pour " +
                    (chambre.getCapacite() == 1 ? "une personne" :
                            (chambre.getCapacite() == 2 ? "un couple" : "une famille")) +
                    ". Équipée de tout le confort moderne pour un séjour agréable.");

            // Notes
            noteLabel.setText("4.5");
            avisLabel.setText("(128 avis)");
        }
    }

    private void loadHotelDetails() {
        try {
            List<Hotel> hotels = serviceHotel.recuperer();
            hotels.stream()
                    .filter(h -> h.getIdHotel() == chambre.getIdHotel())
                    .findFirst()
                    .ifPresent(hotel -> {
                        hotelNameLabel.setText(hotel.getNomHotel());
                        hotelAddressLabel.setText(hotel.getAdresse() + ", " + hotel.getIdHotel());
                        hotelPhoneLabel.setText("+33 1 23 45 67 89"); // À remplacer par les vraies données
                        hotelEmailLabel.setText("contact@" + hotel.getNomHotel().toLowerCase().replace(" ", "") + ".com");
                    });
        } catch (SQLDataException e) {
            hotelNameLabel.setText("Hôtel inconnu");
            hotelAddressLabel.setText("Adresse non disponible");
        }
    }

    private void loadStatistics() {
        // Statistiques simulées - à adapter avec vos vraies données
        dateCreationLabel.setText("15/01/2024");
        derniereReservationLabel.setText("12/02/2024");
        totalReservationsLabel.setText("24 réservations");

        // Calcul du chiffre d'affaires simulé
        double ca = chambre.getPrixChambre() * 24; // 24 réservations
        chiffreAffaireLabel.setText(String.format("%.2f €", ca));
    }

    private void setEquipementStatus(HBox container, Label label, boolean disponible) {
        if (disponible) {
            container.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 20; -fx-padding: 5 15;");
            label.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: 600;");
        } else {
            container.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 20; -fx-padding: 5 15;");
            label.setStyle("-fx-text-fill: #c62828; -fx-font-weight: 600;");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) chambreNumberLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleModifier() {
        // Ouvrir la fenêtre de modification
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/modifierChambre.fxml")
            );
            javafx.scene.Parent root = loader.load();

            modifierChambreController controller = loader.getController();
            controller.setChambre(chambre);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Modifier la chambre");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir les détails après modification
            loadChambreDetails();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleReserver() {
        // Logique pour réserver la chambre
        if ("Disponible".equalsIgnoreCase(chambre.getStatutChambre())) {
            showAlert("Réservation", "Fonctionnalité de réservation à implémenter");
        } else {
            showAlert("Réservation", "Cette chambre n'est pas disponible pour le moment");
        }
    }

    @FXML
    private void handleImprimer() {
        // Logique pour imprimer les détails
        showAlert("Impression", "Fonctionnalité d'impression à implémenter");
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}