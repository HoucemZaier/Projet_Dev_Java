package Controllers;

import Models.*;
import Services.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
    private Button chooseImageBtn;
    @FXML
    private ImageView imagePreview;

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
                case "guide":
                    user = new Guide(nom, prenom, email, password, pays, selectedImagePath);
                    break;
            }

            if (user != null) {
                serviceUser.ajouter(user);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully!");
                clearFields();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add user: " + e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        if (userTypeCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select user type");
            return false;
        }

        if (nomField.getText().trim().isEmpty() ||
            prenomField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            passwordField.getText().isEmpty() ||
            paysField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields");
            return false;
        }

        if ("Client".equals(userTypeCombo.getValue()) && cinField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "CIN is required for Client");
            return false;
        }

        if ("Admin".equals(userTypeCombo.getValue()) && matriculeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Matricule is required for Admin");
            return false;
        }

        if ("Moderateur".equals(userTypeCombo.getValue()) && matriculeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Matricule is required for Moderateur");
            return false;
        }

        // Email validation for Admin and Moderateur - must be @planNova.tn
        if (("Admin".equals(userTypeCombo.getValue()) || "Moderateur".equals(userTypeCombo.getValue()))
                && !emailField.getText().trim().endsWith("@planNova.tn")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Admin and Moderateur email must be in the format: username@planNova.tn");
            return false;
        }

        // Check if matricule already exists in database
        if (("Admin".equals(userTypeCombo.getValue()) || "Moderateur".equals(userTypeCombo.getValue())) &&
            !matriculeField.getText().trim().isEmpty()) {
            try {
                if (serviceUser.matriculeExists(matriculeField.getText().trim())) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Matricule already exists in database");
                    return false;
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to check matricule: " + e.getMessage());
                return false;
            }
        }

        return true;
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
