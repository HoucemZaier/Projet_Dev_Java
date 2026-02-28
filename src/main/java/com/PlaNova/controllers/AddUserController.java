package com.PlaNova.controllers;

import com.PlaNova.models.*;
import com.PlaNova.services.ServiceUser;
import com.PlaNova.utils.PasswordValidator;
import com.PlaNova.utils.CinValidator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddUserController implements Initializable {

    @FXML
    private ComboBox<String> userTypeCombo;
    @FXML
    private TextField nomField, prenomField, emailField, paysField, cinField, matriculeField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button chooseImageBtn;
    @FXML
    private Button passwordToggleBtn;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Label lengthReq;
    @FXML
    private Label upperReq;
    @FXML
    private Label numberReq;
    @FXML
    private Label specialReq;

    private ServiceUser serviceUser = new ServiceUser();
    private String selectedImagePath = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hide CIN and Matricule fields initially
        cinField.setVisible(false);
        matriculeField.setVisible(false);

        // Add listener to user type combo
        userTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                toggleFields(newVal);
            }
        });

        // Bind password fields
        if (passwordTextField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        // Add password validation listeners
        if (passwordField != null && lengthReq != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                PasswordValidator.updatePasswordRequirements(newVal, lengthReq, upperReq, numberReq, specialReq);
            });
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        if (passwordField.isVisible()) {
            // Switch to visible text
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordToggleBtn.setText("🙈"); // closed eye
        } else {
            // Switch to hidden password
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordToggleBtn.setText("👁"); // open eye
        }
    }

    private void toggleFields(String userType) {
        cinField.setVisible("Client".equals(userType));
        matriculeField.setVisible("Admin".equals(userType) || "Moderateur".equals(userType));
    }

    @FXML
    private void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) chooseImageBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            // You can add image preview here if needed
            System.out.println("Selected image: " + selectedImagePath);
        }
    }

    @FXML
    private void addUser(ActionEvent event) {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            String userType = userTypeCombo.getValue();
            User user = null;

            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String pays = paysField.getText().trim();
            String matricule = matriculeField.getText().trim();

            switch (userType.toLowerCase()) {
                case "client":
                    String cin = cinField.getText().trim();
                    user = new Client(nom, prenom, email, password, pays, selectedImagePath, cin);
                    break;
                case "admin":
                    user = new Admin(nom, prenom, email, password, pays, selectedImagePath, matricule);
                    break;
                case "moderateur":
                    user = new Moderateur(nom, prenom, email, password, pays, selectedImagePath, matricule);
                    break;
                default:
                    showAlert(Alert.AlertType.WARNING, "Erreur", "Type d'utilisateur non supporté: " + userType);
                    return;
            }

            if (user != null) {
                serviceUser.ajouter(user);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur ajouté avec succès!");
                clearFields();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de l'utilisateur: " + e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        if (userTypeCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez sélectionner le type d'utilisateur");
            return false;
        }

        if (nomField.getText().trim().isEmpty() ||
            prenomField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            passwordField.getText().isEmpty() ||
            paysField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez remplir tous les champs obligatoires");
            return false;
        }

        // Check if email already exists in database
        if (isEmailAlreadyExists(emailField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                     "Cette adresse email est déjà utilisée. Veuillez choisir une autre adresse email.");
            return false;
        }

        // Validate password requirements
        PasswordValidator.ValidationResult passwordResult = PasswordValidator.validatePassword(passwordField.getText());
        if (!passwordResult.isValid()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le mot de passe ne respecte pas toutes les exigences. Veuillez vérifier les critères ci-dessus.");
            return false;
        }

        if ("Client".equals(userTypeCombo.getValue()) && cinField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le CIN est requis pour les clients");
            return false;
        }

        // Validate CIN format for clients
        if ("Client".equals(userTypeCombo.getValue()) && !cinField.getText().trim().isEmpty()) {
            String cin = cinField.getText().trim();
            if (!CinValidator.isValidCin(cin)) {
                String message = CinValidator.getValidationMessage(cin);
                showAlert(Alert.AlertType.WARNING, "Erreur de Validation", message != null ? message : "Format de CIN invalide!");
                return false;
            }
        }

        if ("Admin".equals(userTypeCombo.getValue()) && matriculeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le matricule est requis pour les administrateurs");
            return false;
        }

        if ("Moderateur".equals(userTypeCombo.getValue()) && matriculeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le matricule est requis pour les modérateurs");
            return false;
        }

        // Validate matricule format for Admin and Moderateur
        if ("Admin".equals(userTypeCombo.getValue()) && !isValidAdminMatricule(matriculeField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Le matricule administrateur doit être au format ---AMN------ (avec des chiffres à la place des tirets)!\nExemple: 123AMN123456");
            return false;
        }

        if ("Moderateur".equals(userTypeCombo.getValue()) && !isValidModeratorMatricule(matriculeField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Le matricule modérateur doit être au format ---MOD------ (avec des chiffres à la place des tirets)!\nExemple: 123MOD123456");
            return false;
        }

        // Email validation for Admin and Moderateur - must be @planNova.tn
        if (("Admin".equals(userTypeCombo.getValue()) || "Moderateur".equals(userTypeCombo.getValue()))
                && !emailField.getText().trim().endsWith("@planNova.tn")) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "L'email des administrateurs et modérateurs doit être au format: username@planNova.tn");
            return false;
        }

        // Check if matricule already exists in database
        if (("Admin".equals(userTypeCombo.getValue()) || "Moderateur".equals(userTypeCombo.getValue())) &&
            !matriculeField.getText().trim().isEmpty()) {
            try {
                if (serviceUser.matriculeExists(matriculeField.getText().trim())) {
                    showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le matricule existe déjà dans la base de données");
                    return false;
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de Base de Données", "Échec de la vérification du matricule: " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    private boolean isValidAdminMatricule(String matricule) {
        // Format: 123AMN123456 (3 digits, AMN, 6 digits)
        return matricule.matches("\\d{3}AMN\\d{6}");
    }

    private boolean isValidModeratorMatricule(String matricule) {
        // Format: 123MOD123456 (3 digits, MOD, 6 digits)
        return matricule.matches("\\d{3}MOD\\d{6}");
    }

    private boolean isEmailAlreadyExists(String email) {
        try {
            // Use the findByEmail method to check if email exists
            User existingUser = serviceUser.findByEmail(email);
            return existingUser != null; // If user is found, email already exists
        } catch (SQLException e) {
            // Log error and show alert
            System.err.println("Erreur lors de la vérification de l'email: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de Base de Données",
                     "Erreur lors de la vérification de l'email. Veuillez réessayer.");
            return true; // Return true to be safe - prevent account creation if we can't verify
        }
    }

    private void clearFields() {
        userTypeCombo.setValue(null);
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        paysField.clear();
        cinField.clear();
        matriculeField.clear();
        selectedImagePath = "";
        cinField.setVisible(false);
        matriculeField.setVisible(false);
        // Reset field visibility based on current selection
        if (userTypeCombo.getValue() != null) {
            toggleFields(userTypeCombo.getValue());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
