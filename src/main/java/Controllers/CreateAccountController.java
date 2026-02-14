package Controllers;

import Models.*;
import Services.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class CreateAccountController {

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
    @FXML private TextField paysField;
    @FXML private Button createAccountBtn;
    @FXML private Button backToLoginBtn;

    private File selectedImageFile;
    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    private void initialize() {
        // Initialize combo box with user types
        if (userTypeComboBox != null) {
            // Items are already populated from FXML, no need to add them again
            // userTypeComboBox.getItems().addAll("Client", "Admin", "Guide", "Moderateur");

            // Initialize combo box listener for dynamic fields
            userTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                updateDynamicFields(newVal);
            });
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
            case "Guide":
                cinField.setVisible(false);
                matriculeField.setVisible(false);
                matriculeModField.setVisible(false);
                break;
            case "Moderateur":
                cinField.setVisible(false);
                matriculeField.setVisible(false);
                matriculeModField.setVisible(true);
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
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields!");
            return;
        }

        // Confirm password validation
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Passwords do not match!");
            return;
        }

        // Type-specific validation
        if ("Client".equals(userType) && cin.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "CIN is required for Client!");
            return;
        }
        if ("Admin".equals(userType) && matricule.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Matricule is required for Admin!");
            return;
        }
        if ("Moderateur".equals(userType) && matriculeMod.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Matricule is required for Moderateur!");
            return;
        }

        // Validate matricule format for Admin and Moderateur
        if ("Admin".equals(userType) && !isValidAdminMatricule(matricule)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                "Admin matricule must be in format ---AMN------ (with numbers instead of dashes)!");
            return;
        }

        if ("Moderateur".equals(userType) && !isValidModeratorMatricule(matriculeMod)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                "Moderateur matricule must be in format ---MOD------ (with numbers instead of dashes)!");
            return;
        }

        // Validate if matricule exists in database
        if ("Admin".equals(userType) && !matriculeExistsInDatabase(matricule)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                "This Admin matricule does not exist in the database. Please contact an administrator.");
            return;
        }

        if ("Moderateur".equals(userType) && !matriculeExistsInDatabase(matriculeMod)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                "This Moderateur matricule does not exist in the database. Please contact an administrator.");
            return;
        }

        // Email validation
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid email address!");
            return;
        }

        // Email validation for Admin and Moderateur - must be @planNova.tn
        if (("Admin".equals(userType) || "Moderateur".equals(userType)) && !email.endsWith("@planNova.tn")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Admin and Moderateur email must be in the format: username@planNova.tn");
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
                case "Guide":
                    newUser = new Guide(nom, prenom, email, password, pays, imagePath);
                    break;
                case "Moderateur":
                    newUser = new Moderateur(nom, prenom, email, password, pays, imagePath, matriculeMod);
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid user type selected!");
                    return;
            }

            // Save to database using ServiceUser
            serviceUser.ajouter(newUser);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");

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
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to validate matricule: " + e.getMessage());
            return false;
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
