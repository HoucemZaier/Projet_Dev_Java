package controllers;

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

public class modifierHotelController {

    @FXML private TextField nomHotelField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private ComboBox<Integer> etoilesComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField imagePathField;
    @FXML private TextField idDestinationField;
    @FXML private Label errorLabel;
    @FXML private VBox errorContainer;
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

        // Valider que l'ID destination est numérique
        idDestinationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                idDestinationField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Validation en temps réel
        setupRealTimeValidation();

        // Écouter les changements pour activer/désactiver le bouton
        setupFormValidation();

        // Masquer le conteneur d'erreur
        errorContainer.setVisible(false);

        // Désactiver le bouton Modifier initialement
        modifierBtn.setDisable(true);
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
            if (!newValue.isEmpty() && newValue.length() < 3) {
                setFieldErrorStyle(nomHotelField);
            } else {
                clearFieldErrorStyle(nomHotelField);
            }
            validateForm();
        });

        // Validation de la ville
        villeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.matches("^[a-zA-ZÀ-ÿ\\s-]+$")) {
                setFieldErrorStyle(villeField);
            } else {
                clearFieldErrorStyle(villeField);
            }
            validateForm();
        });

        // Validation de la description
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue.length() < 10) {
                setFieldErrorStyle(descriptionArea);
            } else {
                clearFieldErrorStyle(descriptionArea);
            }
            validateForm();
        });

        // Validation de l'ID destination
        idDestinationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    int value = Integer.parseInt(newValue);
                    if (value <= 0) {
                        setFieldErrorStyle(idDestinationField);
                    } else {
                        clearFieldErrorStyle(idDestinationField);
                    }
                } catch (NumberFormatException e) {
                    setFieldErrorStyle(idDestinationField);
                }
            } else {
                clearFieldErrorStyle(idDestinationField);
            }
            validateForm();
        });

        // Validation des étoiles
        etoilesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                setFieldErrorStyle(etoilesComboBox);
            } else {
                clearFieldErrorStyle(etoilesComboBox);
            }
            validateForm();
        });
    }

    private void setupFormValidation() {
        // Écouter tous les champs pour activer/désactiver le bouton Modifier
        javafx.beans.value.ChangeListener<String> textFieldListener = (observable, oldValue, newValue) -> {
            validateForm();
        };

        nomHotelField.textProperty().addListener(textFieldListener);
        adresseField.textProperty().addListener(textFieldListener);
        villeField.textProperty().addListener(textFieldListener);
        idDestinationField.textProperty().addListener(textFieldListener);
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

                String fileName = "hotel_" + hotel.getIdHotel() + "_" + System.currentTimeMillis() + fileExtension;
                File destinationFile = new File(imagesDir, fileName);

                // Copier l'image dans le projet
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Stocker le chemin relatif
                imagePathInProject = "images/hotels/" + fileName;
                imagePathField.setText(originalName);

                // Ajouter une icône de succès visuel
                imagePathField.setStyle(imagePathField.getStyle() +
                        "-fx-border-color: #10b981 !important; " +
                        "-fx-background-color: #f0f9ff !important;");

                errorContainer.setVisible(false);

            } catch (Exception e) {
                showAlert("Erreur",
                        "Impossible de charger l'image : " + e.getMessage(),
                        Alert.AlertType.ERROR);
                imagePathInProject = hotel.getImage(); // Garder l'ancienne image
            }
        }
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

        // Validation de l'ID destination
        String idDest = idDestinationField.getText().trim();
        if (idDest.isEmpty()) {
            isValid = false;
            setFieldErrorStyle(idDestinationField);
            errors.append("• L'ID de destination est obligatoire\n");
        } else {
            try {
                int id = Integer.parseInt(idDest);
                if (id <= 0) {
                    isValid = false;
                    setFieldErrorStyle(idDestinationField);
                    errors.append("• L'ID de destination doit être positif\n");
                } else {
                    clearFieldErrorStyle(idDestinationField);
                }
            } catch (NumberFormatException e) {
                isValid = false;
                setFieldErrorStyle(idDestinationField);
                errors.append("• L'ID de destination doit être un nombre valide\n");
            }
        }

        // Mettre à jour le bouton Modifier
        modifierBtn.setDisable(!isValid);

        // Afficher/masquer les erreurs
        if (!isValid) {
            errorLabel.setText(errors.toString());
            errorContainer.setVisible(true);
        } else {
            errorContainer.setVisible(false);
        }
    }

    private boolean validateFormFinal() {
        validateForm();

        if (errorContainer.isVisible()) {
            // Animer le conteneur d'erreur pour attirer l'attention
            errorContainer.setStyle(errorContainer.getStyle() +
                    "-fx-border-color: #ef4444; " +
                    "-fx-background-color: #fef2f2;");

            // Effet de shake sur le conteneur d'erreur
            animateErrorShake();
            return false;
        }

        return true;
    }

    private void animateErrorShake() {
        // Simple animation de shake
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

    private void setFieldErrorStyle(Control control) {
        String baseStyle = control instanceof TextField ?
                "-fx-background-radius: 8; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 0 10;" :
                control instanceof ComboBox ?
                        "-fx-background-radius: 8; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 0 10;" :
                        "-fx-background-radius: 8; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 10;";

        control.setStyle(baseStyle + " -fx-border-color: #ef4444 !important; -fx-border-width: 1.5 !important;");
    }

    private void clearFieldErrorStyle(Control control) {
        if (control instanceof TextField) {
            control.setStyle("-fx-background-radius: 8; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 0 10;");
        } else if (control instanceof ComboBox) {
            control.setStyle("-fx-background-radius: 8; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 0 10;");
        } else if (control instanceof TextArea) {
            control.setStyle("-fx-background-radius: 8; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 10;");
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
                }
            });
        }
    }
}