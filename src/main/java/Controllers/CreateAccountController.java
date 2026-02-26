package Controllers;

import Models.*;
import Services.ServiceUser;
import utils.PasswordValidator;
import utils.CinValidator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CreateAccountController implements Initializable {

    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private TextField cinField;
    @FXML private TextField matriculeField;
    @FXML private TextField matriculeModField;
    @FXML private Button imageButton;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private Button passwordToggleBtn;
    @FXML private Button confirmPasswordToggleBtn;
    @FXML private TextField paysField;
    @FXML private Button createAccountBtn;
    @FXML private Button backToLoginBtn;
    @FXML private Label lengthReq;
    @FXML private Label upperReq;
    @FXML private Label numberReq;
    @FXML private Label specialReq;

    private File selectedImageFile;
    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize combo box with user types
        if (userTypeComboBox != null) {
            // Items are already populated from FXML, no need to add them again
            // userTypeComboBox.getItems().addAll("Client", "Admin", "Guide", "Moderateur");

            // Initialize combo box listener for dynamic fields
            userTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                updateDynamicFields(newVal);
            });
        }

        // Bind password fields
        if (passwordTextField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
        if (confirmPasswordTextField != null) {
            confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        }

        // Add password validation listeners
        if (passwordField != null && lengthReq != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                PasswordValidator.updatePasswordRequirements(newVal, lengthReq, upperReq, numberReq, specialReq);
            });
        }

        // Force buttons to be visible - add a delay to ensure FXML is fully loaded
        Platform.runLater(() -> {
            forceButtonsVisible();
        });

        System.out.println("CreateAccountController initialized successfully");
    }

    private void forceButtonsVisible() {
        System.out.println("=== Forcing buttons visibility ===");

        if (createAccountBtn != null) {
            createAccountBtn.setVisible(true);
            createAccountBtn.setManaged(true);
            createAccountBtn.setDisable(false);
            createAccountBtn.toFront();
            System.out.println("Create button forced visible - Text: " + createAccountBtn.getText());
        } else {
            System.err.println("ERROR: createAccountBtn is NULL!");
        }

        if (backToLoginBtn != null) {
            backToLoginBtn.setVisible(true);
            backToLoginBtn.setManaged(true);
            backToLoginBtn.setDisable(false);
            backToLoginBtn.toFront();
            System.out.println("Back button forced visible - Text: " + backToLoginBtn.getText());
        } else {
            System.err.println("ERROR: backToLoginBtn is NULL!");
        }

        System.out.println("=== Buttons visibility check complete ===");
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

    @FXML
    private void toggleConfirmPasswordVisibility(ActionEvent event) {
        if (confirmPasswordField.isVisible()) {
            // Switch to visible text
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordToggleBtn.setText("🙈"); // closed eye
        } else {
            // Switch to hidden password
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordToggleBtn.setText("👁"); // open eye
        }
    }

    private void updateDynamicFields(String userType) {
        if (userType == null) {
            cinField.setVisible(false);
            matriculeField.setVisible(false);
            matriculeModField.setVisible(false);
            return;
        }

        switch (userType) {
            case "Client":
                cinField.setVisible(true);
                matriculeField.setVisible(false);
                matriculeModField.setVisible(false);
                break;
            case "Admin":
                cinField.setVisible(false);
                matriculeField.setVisible(true);
                matriculeModField.setVisible(false);
                break;
            case "Moderateur":
                cinField.setVisible(false);
                matriculeField.setVisible(false);
                matriculeModField.setVisible(true);
                break;
            default:
                // Hide all fields for unknown types
                cinField.setVisible(false);
                matriculeField.setVisible(false);
                matriculeModField.setVisible(false);
                break;
        }
    }

    @FXML
    void handleImportImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) createAccountBtn.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            System.out.println("Selected image: " + selectedImageFile.getAbsolutePath());
        }
    }

    @FXML
    void handleCreateAccount(ActionEvent event) {
        String userType = userTypeComboBox.getValue();
        String cin = cinField.getText().trim();
        String matricule = matriculeField.getText().trim();
        String matriculeMod = matriculeModField.getText().trim();
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String pays = paysField.getText().trim();

        // Validation
        if (userType == null || nom.isEmpty() || prenom.isEmpty() ||
                email.isEmpty() || password.isEmpty() || pays.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez remplir tous les champs obligatoires!");
            return;
        }

        // Password requirements validation
        PasswordValidator.ValidationResult passwordResult = PasswordValidator.validatePassword(password);
        if (!passwordResult.isValid()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le mot de passe ne respecte pas toutes les exigences. Veuillez vérifier les critères ci-dessus.");
            return;
        }

        // Confirm password validation
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Les mots de passe ne correspondent pas!");
            return;
        }

        // Type-specific validation
        if ("Client".equals(userType) && cin.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le CIN est requis pour les clients!");
            return;
        }

        // Validate CIN format for clients
        if ("Client".equals(userType) && !CinValidator.isValidCin(cin)) {
            String message = CinValidator.getValidationMessage(cin);
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", message != null ? message : "Format de CIN invalide!");
            return;
        }
        if ("Admin".equals(userType) && matricule.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le matricule est requis pour les administrateurs!");
            return;
        }
        if ("Moderateur".equals(userType) && matriculeMod.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le matricule est requis pour les modérateurs!");
            return;
        }

        // Validate matricule format for Admin and Moderateur
        if ("Admin".equals(userType) && !isValidAdminMatricule(matricule)) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Le matricule administrateur doit être au format ---AMN------ (avec des chiffres à la place des tirets)!\nExemple: 123AMN123456");
            return;
        }

        if ("Moderateur".equals(userType) && !isValidModeratorMatricule(matriculeMod)) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Le matricule modérateur doit être au format ---MOD------ (avec des chiffres à la place des tirets)!\nExemple: 123MOD123456");
            return;
        }

        // Validate if matricule exists in database
        if ("Admin".equals(userType) && !matriculeExistsInDatabase(matricule)) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Ce matricule administrateur n'existe pas dans la base de données. Veuillez contacter un administrateur.");
            return;
        }

        if ("Moderateur".equals(userType) && !matriculeExistsInDatabase(matriculeMod)) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Ce matricule modérateur n'existe pas dans la base de données. Veuillez contacter un administrateur.");
            return;
        }

        // Email validation
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez saisir une adresse email valide!");
            return;
        }

        // Check if email already exists in database
        if (isEmailAlreadyExists(email)) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                     "Cette adresse email est déjà utilisée. Veuillez choisir une autre adresse email.");
            return;
        }

        // Email validation for Admin and Moderateur - must be @planNova.tn
        if (("Admin".equals(userType) || "Moderateur".equals(userType)) && !email.endsWith("@planNova.tn")) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "L'email des administrateurs et modérateurs doit être au format: username@planNova.tn");
            return;
        }

        try {
            // Create appropriate user object based on type (using inheritance)
            String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : "";
            User newUser;

            switch (userType) {
                case "Client":
                    newUser = new Client(nom, prenom, email, password, pays, imagePath, cin);
                    break;
                case "Admin":
                    newUser = new Admin(nom, prenom, email, password, pays, imagePath, matricule);
                    break;
                case "Moderateur":
                    newUser = new Moderateur(nom, prenom, email, password, pays, imagePath, matriculeMod);
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Erreur de Type", "Type d'utilisateur non supporté: " + userType);
                    return;
            }

            // Save to database using ServiceUser
            serviceUser.ajouter(newUser);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé avec succès!");

            // Navigate back to login
            handleBackToLogin(event);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create account: " + e.getMessage());
        }
    }

    private boolean isValidAdminMatricule(String matricule) {
        // Format: 123AMN123456 (3 digits, AMN, 6 digits)
        // Example: 123AMN123456
        return matricule.matches("\\d{3}AMN\\d{6}");
    }

    private boolean isValidModeratorMatricule(String matricule) {
        // Format: 123MOD123456 (3 digits, MOD, 6 digits)
        // Example: 123MOD123456
        return matricule.matches("\\d{3}MOD\\d{6}");
    }

    private boolean matriculeExistsInDatabase(String matricule) {
        try {
            return serviceUser.matriculeExists(matricule);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Base de Données", "Échec de la validation du matricule: " + e.getMessage());
            return false;
        }
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

    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backToLoginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - Login");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to return to login: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleImageUpload(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            System.out.println("Selected image: " + selectedImageFile.getAbsolutePath());
            // You can add code here to display the selected image in an ImageView if needed
        }
    }
}
