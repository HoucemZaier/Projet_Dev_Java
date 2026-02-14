package Controllers;

import Models.*;
import Services.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifyUserController implements Initializable {

    @FXML
    private TextField userTypeField, nomField, prenomField, emailField, paysField, cinField, matriculeField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button chooseImageBtn;

    private ServiceUser serviceUser = new ServiceUser();
    private User currentUser;
    private String selectedImagePath = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initially hide CIN and Matricule fields
        cinField.setVisible(false);
        matriculeField.setVisible(false);
    }

    public void setUser(User user) {
        this.currentUser = user;
        populateFields();
    }

    private void populateFields() {
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            passwordField.setText(currentUser.getMotDePasse());
            paysField.setText(currentUser.getPays());
            selectedImagePath = currentUser.getImageurl();

            // Set user type and show specific fields
            if (currentUser instanceof Client) {
                userTypeField.setText("Client");
                cinField.setText(((Client) currentUser).getCin());
                cinField.setVisible(true);
            } else if (currentUser instanceof Admin) {
                userTypeField.setText("Admin");
                matriculeField.setText(((Admin) currentUser).getMatricule());
                matriculeField.setVisible(true);
            } else if (currentUser instanceof Moderateur) {
                userTypeField.setText("Moderateur");
            } else if (currentUser instanceof Guide) {
                userTypeField.setText("Guide");
            }
        }
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
            System.out.println("Selected image: " + selectedImagePath);
        }
    }

    @FXML
    private void updateUser(ActionEvent event) {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            // Update user object
            currentUser.setNom(nomField.getText().trim());
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setMotDePasse(passwordField.getText());
            currentUser.setPays(paysField.getText().trim());
            currentUser.setImageurl(selectedImagePath);

            // Update specific fields
            if (currentUser instanceof Client) {
                ((Client) currentUser).setCin(cinField.getText().trim());
            } else if (currentUser instanceof Admin) {
                ((Admin) currentUser).setMatricule(matriculeField.getText().trim());
            }

            serviceUser.modifier(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
            cancel(event); // Close window

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + e.getMessage());
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        if (nomField.getText().trim().isEmpty() ||
            prenomField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            passwordField.getText().isEmpty() ||
            paysField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields");
            return false;
        }

        if (currentUser instanceof Client && cinField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "CIN is required for Client");
            return false;
        }

        if (currentUser instanceof Admin && matriculeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Matricule is required for Admin");
            return false;
        }

        // Email validation for Admin and Moderateur - must be @planNova.tn
        if ((currentUser instanceof Admin || currentUser instanceof Moderateur)
                && !emailField.getText().trim().endsWith("@planNova.tn")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Admin and Moderateur email must be in the format: username@planNova.tn");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
