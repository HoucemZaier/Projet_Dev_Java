package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Hotel;
import services.ServiceHotel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLDataException;

public class ajouterHotelController {

    @FXML
    private TextField nomHotelField;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField villeField;

    @FXML
    private ComboBox<Integer> etoilesComboBox;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField imagePathField;

    @FXML
    private Label errorLabel;

    @FXML
    private VBox errorContainer;

    @FXML
    private Button annulerBtn;

    @FXML
    private Button ajouterBtn;

    private ServiceHotel serviceHotel;
    private String imagePathInProject;

    @FXML
    public void initialize() {
        serviceHotel = new ServiceHotel();

        // Initialiser la ComboBox des étoiles
        initializeEtoilesComboBox();

        // Initialiser les validateurs
        initializeValidators();

        // Configurer la validation du formulaire
        setupFormValidation();

        // Masquer le conteneur d'erreur
        errorContainer.setVisible(false);
    }

    private void initializeEtoilesComboBox() {
        etoilesComboBox.getItems().addAll(1, 2, 3, 4, 5);
        etoilesComboBox.setValue(3);

        // Personnaliser l'affichage des étoiles
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
    }

    private void initializeValidators() {
        // Validation en temps réel pour le nom
        nomHotelField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.length() < 3) {
                setFieldErrorStyle(nomHotelField);
            } else {
                clearFieldErrorStyle(nomHotelField);
            }
        });

        // Validation en temps réel pour la ville
        villeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("^[a-zA-ZÀ-ÿ\\s-]+$")) {
                setFieldErrorStyle(villeField);
            } else {
                clearFieldErrorStyle(villeField);
            }
        });

        // Validation en temps réel pour la description
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.length() < 10) {
                setFieldErrorStyle(descriptionArea);
            } else {
                clearFieldErrorStyle(descriptionArea);
            }
        });
    }

    private void setupFormValidation() {
        // Écouter tous les champs pour activer/désactiver le bouton Ajouter
        javafx.beans.value.ChangeListener<String> textFieldListener = (observable, oldValue, newValue) -> {
            validateForm();
        };

        nomHotelField.textProperty().addListener(textFieldListener);
        adresseField.textProperty().addListener(textFieldListener);
        villeField.textProperty().addListener(textFieldListener);
        descriptionArea.textProperty().addListener(textFieldListener);

        // Pour la ComboBox des étoiles
        etoilesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        // Désactiver le bouton Ajouter initialement
        ajouterBtn.setDisable(true);
    }

    private void validateForm() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        // Validation du nom
        String nom = nomHotelField.getText().trim();
        if (nom.isEmpty() || nom.length() < 3) {
            isValid = false;
            setFieldErrorStyle(nomHotelField);
            errors.append("• Le nom doit contenir au moins 3 caractères\n");
        } else {
            clearFieldErrorStyle(nomHotelField);
        }

        // Validation de l'adresse
        String adresse = adresseField.getText().trim();
        if (adresse.isEmpty()) {
            isValid = false;
            setFieldErrorStyle(adresseField);
            errors.append("• L'adresse est obligatoire\n");
        } else {
            clearFieldErrorStyle(adresseField);
        }

        // Validation de la ville
        String ville = villeField.getText().trim();
        if (ville.isEmpty() || !ville.matches("^[a-zA-ZÀ-ÿ\\s-]+$")) {
            isValid = false;
            setFieldErrorStyle(villeField);
            errors.append("• La ville ne doit contenir que des lettres, espaces et tirets\n");
        } else {
            clearFieldErrorStyle(villeField);
        }

        // Validation des étoiles
        if (etoilesComboBox.getValue() == null) {
            isValid = false;
            setFieldErrorStyle(etoilesComboBox);
            errors.append("• Le nombre d'étoiles est obligatoire\n");
        } else {
            clearFieldErrorStyle(etoilesComboBox);
        }

        // Validation de la description
        String description = descriptionArea.getText().trim();
        if (description.isEmpty() || description.length() < 10) {
            isValid = false;
            setFieldErrorStyle(descriptionArea);
            errors.append("• La description doit contenir au moins 10 caractères\n");
        } else {
            clearFieldErrorStyle(descriptionArea);
        }

        // Mettre à jour le bouton Ajouter
        ajouterBtn.setDisable(!isValid);

        // Afficher/masquer les erreurs
        if (!isValid) {
            errorLabel.setText(errors.toString());
            errorContainer.setVisible(true);
        } else {
            errorContainer.setVisible(false);
        }
    }

    @FXML
    private void handleParcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image d'hôtel");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Images (*.png, *.jpg, *.jpeg, *.gif, *.bmp)",
                "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Vérifier la taille du fichier (max 5MB)
                long fileSize = selectedFile.length();
                if (fileSize > 5 * 1024 * 1024) {
                    showAlert("Fichier trop volumineux",
                            "L'image ne doit pas dépasser 5MB.",
                            Alert.AlertType.WARNING);
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

                String fileName = "hotel_" + System.currentTimeMillis() + fileExtension;
                File destinationFile = new File(imagesDir, fileName);

                // Copier l'image dans le projet
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Stocker le chemin relatif
                imagePathInProject = "images/hotels/" + fileName;
                imagePathField.setText(originalName);
                imagePathField.setStyle(imagePathField.getStyle() +
                        "-fx-border-color: #10b981 !important; " +
                        "-fx-background-color: #f0f9ff !important;");

                errorContainer.setVisible(false);

            } catch (Exception e) {
                showAlert("Erreur",
                        "Impossible de charger l'image : " + e.getMessage(),
                        Alert.AlertType.ERROR);
                imagePathInProject = null;
                imagePathField.clear();
            }
        }
    }

    @FXML
    private void handleAjouterHotel() {
        // Validation finale avant soumission
        if (!validateFormFinal()) {
            return;
        }

        try {
            // Créer l'objet Hotel
            Hotel hotel = new Hotel();
            hotel.setNomHotel(nomHotelField.getText().trim());
            hotel.setAdresse(adresseField.getText().trim());
            hotel.setVille(villeField.getText().trim());
            hotel.setNombreEtoile(etoilesComboBox.getValue());
            hotel.setDescription(descriptionArea.getText().trim());
            hotel.setImage(imagePathInProject != null ? imagePathInProject : "");

            // Afficher les informations pour débogage
            System.out.println("=== Ajout d'un hôtel ===");
            System.out.println("Nom: " + hotel.getNomHotel());
            System.out.println("Adresse: " + hotel.getAdresse());
            System.out.println("Ville: " + hotel.getVille());
            System.out.println("Étoiles: " + hotel.getNombreEtoile());

            // Désactiver le bouton pendant l'opération
            ajouterBtn.setDisable(true);
            ajouterBtn.setText("Ajout en cours...");

            // Simuler un délai pour l'UX
            new Thread(() -> {
                try {
                    Thread.sleep(500);

                    // Ajouter à la base de données
                    serviceHotel.ajouter(hotel);

                    Platform.runLater(() -> {
                        showSuccessAlert();
                        resetForm();
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        ajouterBtn.setDisable(false);
                        ajouterBtn.setText("Ajouter");
                        showAlert("Erreur",
                                "Erreur lors de l'ajout de l'hôtel : " + e.getMessage(),
                                Alert.AlertType.ERROR);
                    });
                }
            }).start();

        } catch (Exception e) {
            showAlert("Erreur",
                    "Une erreur est survenue : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            ajouterBtn.setDisable(false);
            ajouterBtn.setText("Ajouter");
        }
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("✓ Hôtel ajouté avec succès !");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #10b981; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8;"
        );

        ajouterBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold;"
        );
        ajouterBtn.setText("✓ Ajouté !");

        alert.showAndWait();
    }

    private void resetForm() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    clearForm();
                    ajouterBtn.setText("Ajouter");
                    ajouterBtn.setStyle(
                            "-fx-background-color: linear-gradient(to right, #0DA2E7, #0b91d1); " +
                                    "-fx-text-fill: white; " +
                                    "-fx-background-radius: 8; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-font-weight: bold;"
                    );
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private boolean validateFormFinal() {
        validateForm();

        if (errorContainer.isVisible()) {
            errorContainer.setStyle(errorContainer.getStyle() +
                    "-fx-border-color: #ef4444; " +
                    "-fx-background-color: #fef2f2;");
            animateErrorShake();
            return false;
        }

        return true;
    }

    private void animateErrorShake() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(0),
                        new javafx.animation.KeyValue(errorContainer.translateXProperty(), 0)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(100),
                        new javafx.animation.KeyValue(errorContainer.translateXProperty(), 10)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                        new javafx.animation.KeyValue(errorContainer.translateXProperty(), -10)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                        new javafx.animation.KeyValue(errorContainer.translateXProperty(), 10)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(400),
                        new javafx.animation.KeyValue(errorContainer.translateXProperty(), 0))
        );
        timeline.play();
    }

    @FXML
    private void handleAnnuler() {
        boolean hasData = !nomHotelField.getText().trim().isEmpty() ||
                !adresseField.getText().trim().isEmpty() ||
                !villeField.getText().trim().isEmpty() ||
                !descriptionArea.getText().trim().isEmpty() ||
                imagePathInProject != null;

        if (hasData) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation d'annulation");
            alert.setHeaderText("Données non enregistrées");
            alert.setContentText("Le formulaire contient des données non enregistrées. Voulez-vous vraiment annuler ?");

            ButtonType yesButton = new ButtonType("Oui, annuler", ButtonBar.ButtonData.OK_DONE);
            ButtonType noButton = new ButtonType("Non, continuer", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, noButton);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #f59e0b; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 8;"
            );

            alert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    closeWindow();
                }
            });
        } else {
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) nomHotelField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        String style = "-fx-background-color: white; -fx-border-color: ";

        switch (type) {
            case ERROR:
                style += "#ef4444;";
                break;
            case INFORMATION:
                style += "#0DA2E7;";
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

    private void clearForm() {
        nomHotelField.clear();
        adresseField.clear();
        villeField.clear();
        etoilesComboBox.setValue(3);
        descriptionArea.clear();
        imagePathField.clear();

        imagePathInProject = null;
        errorContainer.setVisible(false);

        // Réinitialiser les styles
        clearFieldErrorStyle(nomHotelField);
        clearFieldErrorStyle(adresseField);
        clearFieldErrorStyle(villeField);
        clearFieldErrorStyle(etoilesComboBox);
        clearFieldErrorStyle(descriptionArea);
        clearFieldErrorStyle(imagePathField);

        // Désactiver le bouton Ajouter
        ajouterBtn.setDisable(true);
    }

    private void setFieldErrorStyle(Control control) {
        control.setStyle(control.getStyle() +
                "-fx-border-color: #ef4444 !important; " +
                "-fx-border-width: 1.5 !important;");
    }

    private void clearFieldErrorStyle(Control control) {
        String baseStyle = "-fx-background-radius: 8; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1.5;";

        if (control instanceof TextField) {
            control.setStyle(baseStyle + " -fx-padding: 0 10;");
        } else if (control instanceof ComboBox) {
            control.setStyle(baseStyle + " -fx-padding: 0 10;");
        } else if (control instanceof TextArea) {
            control.setStyle(baseStyle + " -fx-padding: 10;");
        }
    }
}