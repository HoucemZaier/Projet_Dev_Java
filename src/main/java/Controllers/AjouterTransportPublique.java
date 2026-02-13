package Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Services.ServiceTransportPublique;
import Models.TransportPublique;

public class AjouterTransportPublique implements Initializable {

    @FXML
    private ComboBox<String> typeTransportCombo;

    @FXML
    private TextField tarifField;

    @FXML
    private TextField horaireField;

    @FXML
    private VBox uploadArea;

    private String selectedImagePath; // chemin de l'image choisie

    @FXML
    // fonction d'upload d'image
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.setInitialDirectory(new File("src/main/resources/images"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Window owner = (uploadArea != null && uploadArea.getScene() != null) ? uploadArea.getScene().getWindow() : null;
        File selectedFile = fileChooser.showOpenDialog(owner);
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();

            // Création et affichage de l'image dans la VBox
            Image image = new Image(selectedFile.toURI().toString());
            ImageView imageView = new ImageView(image);

            // Ajuster la taille pour éviter que l'image soit trop grande
            imageView.setFitWidth(200);
            imageView.setPreserveRatio(true);

            // Nettoyer la VBox avant d’ajouter la nouvelle image
            uploadArea.getChildren().clear();
            uploadArea.getChildren().add(imageView);

            showAlert(Alert.AlertType.INFORMATION, "Image sélectionnée", "Chemin : " + selectedImagePath);
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucune image", "Veuillez sélectionner une image.");
        }
    }

    @FXML
    private void handleSaveTransport() {
        try {
            // Vérification ComboBox
            String type = typeTransportCombo.getValue();
            if (type == null || type.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un type de transport.");
                return;
            }

            // Vérification tarif
            double tarif;
            try {
                tarif = Double.parseDouble(tarifField.getText());
                if (tarif <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le tarif doit être un nombre positif.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le tarif doit être un nombre valide.");
                return;
            }

            // Vérification horaire
            String horaire = horaireField.getText();
            if (!isValidHoraire(horaire)) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'horaire doit respecter le format HH:mm - HH:mm et être logique.");
                return;
            }

            // Vérification image
            if (selectedImagePath == null || selectedImagePath.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez uploader une image.");
                return;
            }

            // Création de l'objet TransportPublique en utilisant le constructeur par défaut + setters
            TransportPublique transport = new TransportPublique();
            transport.setType(type);
            transport.setTarif(tarif);
            transport.setHoraire(horaire);
            transport.setImage_path(selectedImagePath);

            // Sauvegarde via ServiceTransportPublique
            ServiceTransportPublique service = new ServiceTransportPublique();
            service.ajouter(transport);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Transport ajouté avec succès !");
            clearForm();

        } catch (SQLDataException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    // Vérification format horaire
    private boolean isValidHoraire(String horaire) {
        Pattern pattern = Pattern.compile("^(\\d{2}:\\d{2})\\s-\\s(\\d{2}:\\d{2})$");
        Matcher matcher = pattern.matcher(horaire);
        if (!matcher.matches()) return false;

        String start = matcher.group(1);
        String end = matcher.group(2);

        try {
            java.time.LocalTime startTime = java.time.LocalTime.parse(start);
            java.time.LocalTime endTime = java.time.LocalTime.parse(end);

            // l'heure de début doit être avant l'heure de fin
            return startTime.isBefore(endTime);
        } catch (Exception e) {
            return false;
        }
    }

    // Affichage d'alerte
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Réinitialiser le formulaire
    private void clearForm() {
        typeTransportCombo.setValue(null);
        tarifField.clear();
        horaireField.clear();
        selectedImagePath = null;
        uploadArea.getChildren().clear(); // vider la zone image aussi
    }

    // Ajout de la méthode initialize requise par Initializable
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Si le ComboBox est lié via FXML, on le peuple avec quelques valeurs par défaut
        if (typeTransportCombo != null) {
            typeTransportCombo.setItems(FXCollections.observableArrayList(
                    "Bus", "Metro", "Taxi"
            ));
        }
    }
}
