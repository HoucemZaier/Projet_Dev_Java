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
    @FXML private Button imageButton;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField paysField;
    @FXML private Button createAccountBtn;
    @FXML private Button backToLoginBtn;

    private File selectedImageFile;
    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    private void initialize() {
        // Initialize combo box with user types
        if (userTypeComboBox != null) {
            userTypeComboBox.getItems().addAll("Client", "Admin", "Guide", "Moderateur");

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
            return;
        }

        switch (userType) {
            case "Client":
                cinField.setVisible(true);
                matriculeField.setVisible(false);
                break;
            case "Admin":
                cinField.setVisible(false);
                matriculeField.setVisible(true);
                break;
            case "Guide":
            case "Moderateur":
                cinField.setVisible(false);
                matriculeField.setVisible(false);
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
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String pays = paysField.getText().trim();

        // Validation
        if (userType == null || nom.isEmpty() || prenom.isEmpty() ||
                email.isEmpty() || password.isEmpty() || pays.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields!");
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

        // Email validation
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid email address!");
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
                    newUser = new Moderateur(nom, prenom, email, password, pays, imagePath);
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
