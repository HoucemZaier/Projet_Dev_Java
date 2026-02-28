package com.PlaNova.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import com.PlaNova.models.Chambre;
import java.util.List;

public class detailsChambreController {

    @FXML
    private Label chambreNumberLabel;
    @FXML
    private Label chambreTypeLabel;
    @FXML
    private Circle statutIndicator;
    @FXML
    private Label statutLabel;
    @FXML
    private Label hotelNameLabel;
    @FXML
    private Label hotelIdLabel;
    @FXML
    private Label typeChambreLabel;
    @FXML
    private Label capaciteLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private Label statutDetailLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label idChambreLabel;

    // FlowPane pour afficher les équipements
    @FXML
    private FlowPane equipementContainer;

    private Chambre chambre;

    public void setChambre(Chambre chambre) {
        this.chambre = chambre;
        afficherDetailsChambre();
    }

    private void afficherDetailsChambre() {
        if (chambre != null) {
            // En-tête
            chambreNumberLabel.setText("Chambre N° " + chambre.getIdChambre());
            chambreTypeLabel.setText(chambre.getTypeChambre());

            // Statut
            statutLabel.setText(chambre.getStatutChambre());
            statutDetailLabel.setText(chambre.getStatutChambre());

            // Couleur du statut
            String statut = chambre.getStatutChambre().toLowerCase();
            if (statut.contains("disponible")) {
                statutIndicator.setFill(Color.web("#10b981")); // Vert
            } else if (statut.contains("occup") || statut.contains("occupé")) {
                statutIndicator.setFill(Color.web("#ef4444")); // Rouge
            } else if (statut.contains("réserv") || statut.contains("reserv")) {
                statutIndicator.setFill(Color.web("#f59e0b")); // Orange
            } else {
                statutIndicator.setFill(Color.web("#94a3b8")); // Gris
            }

            // Informations hôtel
            hotelNameLabel.setText(chambre.getNomHotel() != null ? chambre.getNomHotel() : "Hôtel non spécifié");
            hotelIdLabel.setText(String.valueOf(chambre.getIdHotel()));

            // Détails chambre
            typeChambreLabel.setText(chambre.getTypeChambre() != null ? chambre.getTypeChambre() : "Non spécifié");

            String capaciteText = chambre.getCapacite() + " personne" + (chambre.getCapacite() > 1 ? "s" : "");
            capaciteLabel.setText(capaciteText);

            prixLabel.setText(String.format("%.2f €", chambre.getPrixChambre()));

            // Équipements - VERSION CORRIGÉE
            afficherEquipements();

            // Description
            if (chambre.getDescription() != null && !chambre.getDescription().trim().isEmpty()) {
                descriptionLabel.setText(chambre.getDescription());
            } else {
                descriptionLabel.setText("Aucune description disponible");
                descriptionLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            }

            // ID Chambre
            idChambreLabel.setText(String.valueOf(chambre.getIdChambre()));
        }
    }
//
//    private void afficherEquipements() {
//        if (equipementContainer != null) {
//            // VIDER le conteneur avant d'ajouter les nouveaux équipements
//            equipementContainer.getChildren().clear();
//
//            // Récupérer la liste des équipements
//            List<String> equipementList = chambre.getEquipementList();
//
//            // AFFICHER dans la console pour voir ce qu'on récupère
//            System.out.println("=== DÉBOGAGE ÉQUIPEMENTS ===");
//            System.out.println("Équipements bruts: '" + chambre.getEquipement() + "'");
//            System.out.println("Liste d'équipements: " + equipementList);
//            System.out.println("Nombre d'équipements: " + (equipementList != null ? equipementList.size() : 0));
//
//            // Vérifier si la liste n'est pas vide
//            if (equipementList == null || equipementList.isEmpty()) {
//                // Afficher un message si aucun équipement
//                Label emptyLabel = new Label("Aucun équipement");
//                emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 5;");
//                equipementContainer.getChildren().add(emptyLabel);
//                System.out.println("Aucun équipement à afficher");
//            } else {
//                // BOUCLE - Ajouter CHAQUE équipement individuellement
//                int compteur = 0;
//                for (String equip : equipementList) {
//                    if (equip != null && !equip.trim().isEmpty()) {
//                        compteur++;
//
//                        // Créer un label pour CET équipement spécifique
//                        Label equipLabel = new Label(equip.trim());
//                        equipLabel.setStyle(
//                                "-fx-background-color: #e9ecef; " +
//                                        "-fx-padding: 8 15; " +
//                                        "-fx-background-radius: 20; " +
//                                        "-fx-text-fill: #2c3e50; " +
//                                        "-fx-font-size: 13px; " +
//                                        "-fx-font-weight: 500;"
//                        );
//
//                        // AJOUTER CE label au conteneur
//                        equipementContainer.getChildren().add(equipLabel);
//
//                        // Afficher dans la console pour déboguer
//                        System.out.println("✓ Équipement #" + compteur + " ajouté: " + equip.trim());
//                    }
//                }
//
//                // Afficher le nombre total d'équipements ajoutés
//                System.out.println("Total des équipements ajoutés: " + compteur);
//                System.out.println("=== FIN DÉBOGAGE ===");
//            }
//        }
//    }
private void afficherEquipements() {
    if (equipementContainer != null) {
        // VIDER le conteneur avant d'ajouter les nouveaux équipements
        equipementContainer.getChildren().clear();

        // Récupérer la liste des équipements sélectionnés
        List<String> equipementList = chambre.getEquipementsSelectionnes();

        // AFFICHER dans la console pour voir ce qu'on récupère
        System.out.println("=== DÉBOGAGE ÉQUIPEMENTS SÉLECTIONNÉS ===");
        System.out.println("Équipements bruts: '" + chambre.getEquipement() + "'");
        System.out.println("Liste d'équipements sélectionnés: " + equipementList);
        System.out.println("Nombre d'équipements sélectionnés: " + (equipementList != null ? equipementList.size() : 0));

        // Vérifier si la liste n'est pas vide
        if (equipementList == null || equipementList.isEmpty()) {
            // Afficher un message si aucun équipement sélectionné
            Label emptyLabel = new Label("Aucun équipement sélectionné");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 5;");
            equipementContainer.getChildren().add(emptyLabel);
            System.out.println("Aucun équipement sélectionné à afficher");
        } else {
            // BOUCLE - Ajouter CHAQUE équipement sélectionné individuellement
            int compteur = 0;
            for (String equip : equipementList) {
                if (equip != null && !equip.trim().isEmpty()) {
                    compteur++;

                    // Créer un label pour CET équipement spécifique
                    Label equipLabel = new Label(equip.trim());
                    equipLabel.setStyle(
                            "-fx-background-color: #e9ecef; " +
                                    "-fx-padding: 8 15; " +
                                    "-fx-background-radius: 20; " +
                                    "-fx-text-fill: #2c3e50; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-font-weight: 500;"
                    );

                    // AJOUTER CE label au conteneur
                    equipementContainer.getChildren().add(equipLabel);

                    // Afficher dans la console pour déboguer
                    System.out.println("✓ Équipement sélectionné #" + compteur + " ajouté: " + equip.trim());
                }
            }

            // Afficher le nombre total d'équipements sélectionnés ajoutés
            System.out.println("Total des équipements sélectionnés ajoutés: " + compteur);
            System.out.println("=== FIN DÉBOGAGE ===");
        }
    }
}
    @FXML
    private void handleClose() {
        Stage stage = (Stage) chambreNumberLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}