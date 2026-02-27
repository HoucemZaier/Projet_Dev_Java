package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Hotel;
import services.ServiceHotel;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class modifierHotelController {

    @FXML private TextField nomHotelField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private ComboBox<Integer> etoilesComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField imagePathField;

    // Labels d'erreur pour chaque champ
    @FXML private Label nomErrorLabel;
    @FXML private Label adresseErrorLabel;
    @FXML private Label villeErrorLabel;
    @FXML private Label etoilesErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label imageErrorLabel;

    @FXML private Button annulerBtn;
    @FXML private Button modifierBtn;

    private ServiceHotel serviceHotel;
    private Hotel hotel;
    private String imagePathInProject;

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        populateFields();
    }

    @FXML
    public void initialize() {
        serviceHotel = new ServiceHotel();

        // Initialiser la ComboBox avec les étoiles de 1 à 5
        etoilesComboBox.getItems().addAll(1, 2, 3, 4, 5);

        // Configurer le rendu des étoiles
        etoilesComboBox.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("★".repeat(item) + " (" + item + " étoiles)");
                    setStyle("-fx-font-size: 13px; -fx-padding: 8 10;");
                }
            }
        });

        etoilesComboBox.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sélectionner le nombre d'étoiles");
                } else {
                    setText("★".repeat(item) + " (" + item + " étoiles)");
                }
            }
        });

        // Initialiser les labels d'erreur (invisibles par défaut)
        initializeErrorLabels();

        // Validation en temps réel
        setupRealTimeValidation();

        // Écouter les changements pour activer/désactiver le bouton
        setupFormValidation();

        // Désactiver le bouton Modifier initialement
        modifierBtn.setDisable(true);
    }

    private void initializeErrorLabels() {
        // Créer les labels d'erreur s'ils ne sont pas définis dans le FXML
        if (nomErrorLabel == null) {
            nomErrorLabel = createErrorLabel();
        }
        if (adresseErrorLabel == null) {
            adresseErrorLabel = createErrorLabel();
        }
        if (villeErrorLabel == null) {
            villeErrorLabel = createErrorLabel();
        }
        if (etoilesErrorLabel == null) {
            etoilesErrorLabel = createErrorLabel();
        }
        if (descriptionErrorLabel == null) {
            descriptionErrorLabel = createErrorLabel();
        }
        if (imageErrorLabel == null) {
            imageErrorLabel = createErrorLabel();
        }

        // Masquer tous les labels d'erreur
        hideAllErrorLabels();
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 5 0 0 15; -fx-wrap-text: true;");
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private void hideAllErrorLabels() {
        nomErrorLabel.setVisible(false);
        nomErrorLabel.setManaged(false);
        adresseErrorLabel.setVisible(false);
        adresseErrorLabel.setManaged(false);
        villeErrorLabel.setVisible(false);
        villeErrorLabel.setManaged(false);
        etoilesErrorLabel.setVisible(false);
        etoilesErrorLabel.setManaged(false);
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);
        imageErrorLabel.setVisible(false);
        imageErrorLabel.setManaged(false);
    }

    private void populateFields() {
        if (hotel != null) {
            nomHotelField.setText(hotel.getNomHotel());
            adresseField.setText(hotel.getAdresse());
            villeField.setText(hotel.getVille());
            etoilesComboBox.setValue(hotel.getNombreEtoile());
            descriptionArea.setText(hotel.getDescription());

            // Afficher le nom du fichier image si présent
            if (hotel.getImage() != null && !hotel.getImage().isEmpty()) {
                String[] parts = hotel.getImage().split("/");
                imagePathField.setText(parts.length > 0 ? parts[parts.length - 1] : hotel.getImage());
                imagePathInProject = hotel.getImage();
            } else {
                imagePathField.setText("Aucune image");
            }

            // Activer la validation après peuplement
            validateForm();
        }
    }

    private void setupRealTimeValidation() {
        // Validation du nom
        nomHotelField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNomField();
            validateForm();
        });


            // Validation de l'adresse
        adresseField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateAdresseField();
            validateForm();
        });

        // Validation de la ville
        villeField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateVilleField();
            validateForm();
        });

        // Validation de la description
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDescriptionField();
            validateForm();
        });

        // Validation des étoiles
        etoilesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateEtoilesField();
            validateForm();
        });
    }
    private void validateNomField() {
        String nom = nomHotelField.getText().trim();

        if (nom.isEmpty()) {
            showFieldError(nomHotelField, nomErrorLabel, "Le nom de l'hôtel est obligatoire");
            return;
        }

        // Vérification des chiffres en PRIORITÉ (première validation après le vide)
        if (nom.matches(".*\\d.*")) {
            showFieldError(nomHotelField, nomErrorLabel, "Le nom ne doit pas contenir de chiffres");
            return;
        }

        if (nom.length() < 5) {
            showFieldError(nomHotelField, nomErrorLabel, "Le nom doit contenir au moins 5 caractères");
            return;
        }

        if (nom.length() > 100) {
            showFieldError(nomHotelField, nomErrorLabel, "Le nom ne peut pas dépasser 100 caractères");
            return;
        }

        // Vérification qu'il n'y a pas de caractères spéciaux non autorisés
        if (nom.matches(".*[^a-zA-ZÀ-ÿ\\s'-].*")) {
            showFieldError(nomHotelField, nomErrorLabel,
                    "Le nom ne doit contenir que des lettres, espaces, apostrophes et tirets");
            return;
        }

        // Vérification du format strict
        if (!nom.matches("^[a-zA-ZÀ-ÿ]+(?:[ '-][a-zA-ZÀ-ÿ]+)*$")) {
            showFieldError(nomHotelField, nomErrorLabel,
                    "Format invalide. Le nom doit:\n" +
                            "• Commencer par une lettre\n" +
                            "• Ne contenir que des lettres, espaces, tirets ou apostrophes\n" +
                            "• Ne pas avoir d'espaces ou tirets consécutifs\n" +
                            "• Ne pas finir par un espace ou tiret");
            return;
        }

        // Vérification des mots trop courts
        String[] mots = nom.split("[ '-]");
        for (String mot : mots) {
            if (mot.length() < 2) {
                showFieldError(nomHotelField, nomErrorLabel,
                        "Chaque mot doit contenir au moins 2 lettres");
                return;
            }
        }

        hideFieldError(nomHotelField, nomErrorLabel);
    }
    private void validateAdresseField() {
        String adresse = adresseField.getText().trim();

        if (adresse.isEmpty()) {
            showFieldError(adresseField, adresseErrorLabel, "L'adresse est obligatoire");
        } else if (adresse.length() < 8) {
            showFieldError(adresseField, adresseErrorLabel, "L'adresse est trop courte (minimum 8 caractères)");
        } else if (!isValidAddressFormat(adresse)) {
            showFieldError(adresseField, adresseErrorLabel,
                    "Format d'adresse invalide. L'adresse doit contenir:\n" +
                            "• Un numéro (ex: 15, 123)\n" +
                            "• Un nom de rue (ex: Rue de Paris)\n" +
                            "Exemple: 123 Avenue des Champs-Élysées");
        } else {
            hideFieldError(adresseField, adresseErrorLabel);
        }
    }

    private boolean isValidAddressFormat(String adresse) {
        // Vérifier la présence d'un numéro (au moins un chiffre)
        boolean hasNumber = adresse.matches(".*\\d+.*");
        if (!hasNumber) return false;

        // Vérifier la présence de lettres (nom de rue)
        boolean hasLetters = adresse.matches(".*[a-zA-ZÀ-ÿ].*");
        if (!hasLetters) return false;

        // Vérifier les caractères autorisés
        boolean validCharacters = adresse.matches("^[a-zA-ZÀ-ÿ0-9\\s,.'-]+$");
        if (!validCharacters) return false;

        // Vérifier que ça commence par un numéro ou une combinaison numéro+lettre
        boolean startsCorrectly = adresse.matches("^\\d+.*") || adresse.matches("^\\d+[a-zA-Z].*");
        if (!startsCorrectly) return false;

        return true;
    }

    private void validateVilleField() {
        String ville = villeField.getText().trim();
        if (ville.isEmpty()) {
            showFieldError(villeField, villeErrorLabel, "La ville est obligatoire");
        } else if (!ville.matches("^[a-zA-ZÀ-ÿ\\s-]+$")) {
            showFieldError(villeField, villeErrorLabel, "La ville ne doit contenir que des lettres, espaces et tirets");
        } else {
            hideFieldError(villeField, villeErrorLabel);
        }
    }

    private void validateEtoilesField() {
        if (etoilesComboBox.getValue() == null) {
            showFieldError(etoilesComboBox, etoilesErrorLabel, "Le nombre d'étoiles est obligatoire");
        } else {
            hideFieldError(etoilesComboBox, etoilesErrorLabel);
        }
    }

    private void validateDescriptionField() {
        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            showFieldError(descriptionArea, descriptionErrorLabel, "La description est obligatoire");
        } else if (description.length() < 10) {
            showFieldError(descriptionArea, descriptionErrorLabel, "La description doit contenir au moins 10 caractères");
        } else {
            hideFieldError(descriptionArea, descriptionErrorLabel);
        }
    }

    private void showFieldError(Control field, Label errorLabel, String message) {
        setFieldErrorStyle(field);
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 5 0 0 15; -fx-wrap-text: true;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideFieldError(Control field, Label errorLabel) {
        clearFieldErrorStyle(field);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setupFormValidation() {
        // Écouter tous les champs pour activer/désactiver le bouton Modifier
        javafx.beans.value.ChangeListener<String> textFieldListener = (observable, oldValue, newValue) -> {
            validateForm();
        };

        nomHotelField.textProperty().addListener(textFieldListener);
        adresseField.textProperty().addListener(textFieldListener);
        villeField.textProperty().addListener(textFieldListener);
        descriptionArea.textProperty().addListener(textFieldListener);

        etoilesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });
    }

    @FXML
    private void handleParcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une nouvelle image");

        // Filtres pour les images
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Images (*.png, *.jpg, *.jpeg, *.gif, *.bmp)",
                "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        // Définir le répertoire par défaut
        File userPicturesDir = new File(System.getProperty("user.home"), "Pictures");
        if (userPicturesDir.exists()) {
            fileChooser.setInitialDirectory(userPicturesDir);
        }

        File selectedFile = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Vérifier la taille du fichier (max 5MB)
                long fileSize = selectedFile.length();
                if (fileSize > 5 * 1024 * 1024) {
                    showImageError("L'image ne doit pas dépasser 5MB.");
                    return;
                }

                // Créer le dossier resources/images/hotels s'il n'existe pas
                File imagesDir = new File("src/main/resources/images/hotels");
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }

                // Générer un nom de fichier unique
                String originalName = selectedFile.getName();
                String fileExtension = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileExtension = originalName.substring(dotIndex);
                }

                String fileName = "hotel_" + hotel.getIdHotel() + "_" + System.currentTimeMillis() + fileExtension;
                File destinationFile = new File(imagesDir, fileName);

                // Copier l'image dans le projet
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Stocker le chemin relatif
                imagePathInProject = "images/hotels/" + fileName;
                imagePathField.setText(originalName);

                // Masquer l'erreur d'image si elle était affichée
                hideImageError();

            } catch (Exception e) {
                showImageError("Impossible de charger l'image : " + e.getMessage());
            }
        }
    }

    private void showImageError(String message) {
        imageErrorLabel.setText(message);
        imageErrorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 5 0 0 15; -fx-wrap-text: true;");
        imageErrorLabel.setVisible(true);
        imageErrorLabel.setManaged(true);
        setFieldErrorStyle(imagePathField);
    }

    private void hideImageError() {
        imageErrorLabel.setVisible(false);
        imageErrorLabel.setManaged(false);
        clearFieldErrorStyle(imagePathField);
    }

    @FXML
    private void handleModifierHotel() {
        // Validation finale avant soumission
        if (!validateFormFinal()) {
            return;
        }

        try {
            // Mettre à jour l'objet Hotel
            hotel.setNomHotel(nomHotelField.getText().trim());
            hotel.setAdresse(adresseField.getText().trim());
            hotel.setVille(villeField.getText().trim());
            hotel.setNombreEtoile(etoilesComboBox.getValue());
            hotel.setDescription(descriptionArea.getText().trim());

            // Ne mettre à jour l'image que si une nouvelle a été sélectionnée
            if (imagePathInProject != null && !imagePathInProject.equals(hotel.getImage())) {
                hotel.setImage(imagePathInProject);
            }

            // Désactiver le bouton pendant l'opération
            modifierBtn.setDisable(true);
            modifierBtn.setText("Modification en cours...");

            // Simuler un délai pour l'UX
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Petit délai pour l'effet visuel

                    // Mettre à jour dans la base de données
                    serviceHotel.modifier(hotel);

                    // Revenir sur le thread JavaFX
                    javafx.application.Platform.runLater(() -> {
                        // Afficher message de succès stylé
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Succès");
                        alert.setHeaderText(null);
                        alert.setContentText("✓ Hôtel modifié avec succès !");

                        // Personnaliser le style
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.setStyle(
                                "-fx-background-color: white; " +
                                        "-fx-border-color: #10b981; " +
                                        "-fx-border-width: 2; " +
                                        "-fx-border-radius: 8;"
                        );

                        // Ajouter un effet de succès au bouton
                        modifierBtn.setStyle(
                                "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                                        "-fx-text-fill: white; " +
                                        "-fx-background-radius: 8; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-font-weight: bold;"
                        );
                        modifierBtn.setText("✓ Modifié !");

                        alert.showAndWait();

                        // Fermer la fenêtre après un délai
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                javafx.application.Platform.runLater(() -> {
                                    Stage stage = (Stage) modifierBtn.getScene().getWindow();
                                    stage.close();
                                });
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();

                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        modifierBtn.setDisable(false);
                        modifierBtn.setText("Modifier");
                        showAlert("Erreur",
                                "Erreur lors de la modification de l'hôtel : " + e.getMessage(),
                                Alert.AlertType.ERROR);
                    });
                }
            }).start();

        } catch (Exception e) {
            showAlert("Erreur",
                    "Une erreur est survenue : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            modifierBtn.setDisable(false);
            modifierBtn.setText("Modifier");
        }
    }

    private void validateForm() {
        boolean isValid = true;

        // Valider chaque champ individuellement
        validateNomField();
        validateAdresseField();
        validateVilleField();
        validateEtoilesField();
        validateDescriptionField();

        // Vérifier si des erreurs sont présentes
        if (nomErrorLabel.isVisible() || adresseErrorLabel.isVisible() ||
                villeErrorLabel.isVisible() || etoilesErrorLabel.isVisible() ||
                descriptionErrorLabel.isVisible() || imageErrorLabel.isVisible()) {
            isValid = false;
        }

        // Mettre à jour le bouton Modifier
        modifierBtn.setDisable(!isValid);
    }

    private boolean validateFormFinal() {
        validateForm();

        if (modifierBtn.isDisable()) {
            // Animer le premier champ en erreur pour attirer l'attention
            if (nomErrorLabel.isVisible()) {
                animateFieldError(nomHotelField);
            } else if (adresseErrorLabel.isVisible()) {
                animateFieldError(adresseField);
            } else if (villeErrorLabel.isVisible()) {
                animateFieldError(villeField);
            } else if (etoilesErrorLabel.isVisible()) {
                animateFieldError(etoilesComboBox);
            } else if (descriptionErrorLabel.isVisible()) {
                animateFieldError(descriptionArea);
            } else if (imageErrorLabel.isVisible()) {
                animateFieldError(imagePathField);
            }
            return false;
        }

        return true;
    }

    private void animateFieldError(Control field) {
        // Simple animation de shake
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(0),
                        new javafx.animation.KeyValue(field.translateXProperty(), 0)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(100),
                        new javafx.animation.KeyValue(field.translateXProperty(), 10)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                        new javafx.animation.KeyValue(field.translateXProperty(), -10)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                        new javafx.animation.KeyValue(field.translateXProperty(), 10)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(400),
                        new javafx.animation.KeyValue(field.translateXProperty(), 0))
        );
        timeline.play();
    }

    private void setFieldErrorStyle(Control control) {
        String baseStyle = control instanceof TextField ?
                "-fx-background-radius: 10; -fx-background-color: #f1f5f9; -fx-border-color: transparent; -fx-border-radius: 10; -fx-padding: 0 15;" :
                control instanceof ComboBox ?
                        "-fx-background-radius: 10; -fx-background-color: #f1f5f9; -fx-border-color: transparent; -fx-border-radius: 10; -fx-padding: 0 15;" :
                        "-fx-background-radius: 10; -fx-background-color: #f1f5f9; -fx-border-color: transparent; -fx-border-radius: 10; -fx-padding: 15;";

        control.setStyle(baseStyle + " -fx-border-color: #ef4444 !important; -fx-border-width: 2 !important;");
    }

    private void clearFieldErrorStyle(Control control) {
        if (control instanceof TextField) {
            control.setStyle("-fx-background-radius: 10; -fx-background-color: #f1f5f9; -fx-border-color: transparent; -fx-border-radius: 10; -fx-padding: 0 15;");
        } else if (control instanceof ComboBox) {
            control.setStyle("-fx-background-radius: 10; -fx-background-color: #f1f5f9; -fx-border-color: transparent; -fx-border-radius: 10; -fx-padding: 0 15;");
        } else if (control instanceof TextArea) {
            control.setStyle("-fx-background-radius: 10; -fx-background-color: #f1f5f9; -fx-border-color: transparent; -fx-border-radius: 10; -fx-padding: 15;");
        }
    }

    @FXML
    private void handleAnnuler() {
        // Vérifier si des modifications ont été faites
        boolean hasChanges = hasFormChanges();

        if (hasChanges) {
            // Demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation d'annulation");
            alert.setHeaderText("Modifications non enregistrées");
            alert.setContentText("Vous avez des modifications non enregistrées. Voulez-vous vraiment annuler ?");

            // Personnaliser les boutons
            ButtonType yesButton = new ButtonType("Oui, annuler", ButtonBar.ButtonData.OK_DONE);
            ButtonType noButton = new ButtonType("Non, continuer", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, noButton);

            // Style de l'alerte
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #f59e0b; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 8;"
            );

            // Attendre la réponse
            alert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    closeWindow();
                }
            });
        } else {
            closeWindow();
        }
    }

    private boolean hasFormChanges() {
        if (hotel == null) return false;

        // Vérifier chaque champ pour détecter des modifications
        if (!nomHotelField.getText().trim().equals(hotel.getNomHotel())) return true;
        if (!adresseField.getText().trim().equals(hotel.getAdresse())) return true;
        if (!villeField.getText().trim().equals(hotel.getVille())) return true;
        if (etoilesComboBox.getValue() != hotel.getNombreEtoile()) return true;
        if (!descriptionArea.getText().trim().equals(hotel.getDescription())) return true;

        // Vérifier si une nouvelle image a été sélectionnée
        if (imagePathInProject != null && !imagePathInProject.equals(hotel.getImage())) {
            return true;
        }

        return false;
    }

    private void closeWindow() {
        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Personnaliser le style selon le type
        DialogPane dialogPane = alert.getDialogPane();
        String style = "-fx-background-color: white; -fx-border-color: ";

        switch (type) {
            case ERROR:
                style += "#ef4444;";
                break;
            case INFORMATION:
                style += "#10b981;";
                break;
            case WARNING:
                style += "#f59e0b;";
                break;
            default:
                style += "#cbd5e1;";
        }

        style += " -fx-border-width: 2; -fx-border-radius: 8;";
        dialogPane.setStyle(style);

        alert.showAndWait();
    }

    @FXML
    private void handleSupprimerImage() {
        if (imagePathInProject != null && !imagePathInProject.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer l'image");
            alert.setHeaderText("Confirmation");
            alert.setContentText("Voulez-vous vraiment supprimer l'image actuelle ?");

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #ef4444; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 8;"
            );

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    imagePathInProject = "";
                    imagePathField.setText("Aucune image");
                    clearFieldErrorStyle(imagePathField);
                    hideImageError();
                }
            });
        }
    }
}