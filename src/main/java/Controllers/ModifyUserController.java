package Controllers;

import Models.*;
import Services.ServiceUser;
import utils.PasswordValidator;
import utils.CinValidator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    private TextField passwordTextField;
    @FXML
    private Button chooseImageBtn, passwordToggleBtn;
    @FXML
    private VBox passwordRequirementsBox;
    @FXML
    private Label lengthReq;
    @FXML
    private Label upperReq;
    @FXML
    private Label numberReq;
    @FXML
    private Label specialReq;

    private ServiceUser serviceUser = new ServiceUser();
    private User currentUser;
    private String selectedImagePath = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initially hide CIN and Matricule fields
        if (cinField != null) cinField.setVisible(false);
        if (matriculeField != null) matriculeField.setVisible(false);

        // Make user type field completely read-only
        if (userTypeField != null) {
            userTypeField.setEditable(false);
            userTypeField.setMouseTransparent(true);
            userTypeField.setFocusTraversable(false);
            userTypeField.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.8;");
        }

        // Bind password fields - add null checks
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        // Add password validation listeners - add null checks
        if (passwordField != null && lengthReq != null && upperReq != null &&
            numberReq != null && specialReq != null && passwordRequirementsBox != null) {

            // Initially hide requirements box
            passwordRequirementsBox.setVisible(false);
            passwordRequirementsBox.setManaged(false);

            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    // Show requirements box when user starts typing
                    passwordRequirementsBox.setVisible(true);
                    passwordRequirementsBox.setManaged(true);
                    PasswordValidator.updatePasswordRequirements(newVal, lengthReq, upperReq, numberReq, specialReq);
                } else {
                    // Hide requirements box when field is empty
                    passwordRequirementsBox.setVisible(false);
                    passwordRequirementsBox.setManaged(false);
                }
            });
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        populateFields();
    }

    private void populateFields() {
        if (currentUser != null) {
            if (nomField != null) nomField.setText(currentUser.getNom());
            if (prenomField != null) prenomField.setText(currentUser.getPrenom());
            if (emailField != null) emailField.setText(currentUser.getEmail());

            // Don't populate password field with hashed password - leave it empty for new password entry
            if (passwordField != null) {
                passwordField.setText("");
                passwordField.setPromptText("Entrez un nouveau mot de passe (laisser vide pour conserver l'actuel)");
            }

            if (paysField != null) paysField.setText(currentUser.getPays());
            selectedImagePath = currentUser.getImageurl();

            // Set user type and show specific fields
            if (currentUser instanceof Client) {
                if (userTypeField != null) userTypeField.setText("Client");
                if (cinField != null) {
                    cinField.setText(((Client) currentUser).getCin());
                    cinField.setVisible(true);
                }
            } else if (currentUser instanceof Admin) {
                if (userTypeField != null) userTypeField.setText("Admin");
                if (matriculeField != null) {
                    matriculeField.setText(((Admin) currentUser).getMatricule());
                    matriculeField.setVisible(true);
                }
            } else if (currentUser instanceof Moderateur) {
                if (userTypeField != null) userTypeField.setText("Moderateur");
            } else if (currentUser instanceof Guide) {
                if (userTypeField != null) userTypeField.setText("Guide");
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

            // Only update password if a new one is provided
            String newPassword = passwordField.getText();
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                // Validate password requirements if a new password is provided
                PasswordValidator.ValidationResult passwordResult = PasswordValidator.validatePassword(newPassword);
                if (!passwordResult.isValid()) {
                    showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le mot de passe ne respecte pas toutes les exigences. Veuillez vérifier les critères ci-dessus.");
                    return;
                }
                currentUser.setMotDePasse(newPassword.trim());
            } else {
                // Keep the existing password by setting it to null (ServiceUser will handle this)
                currentUser.setMotDePasse(null);
            }

            currentUser.setPays(paysField.getText().trim());
            currentUser.setImageurl(selectedImagePath);

            // Update specific fields
            if (currentUser instanceof Client) {
                ((Client) currentUser).setCin(cinField.getText().trim());
            } else if (currentUser instanceof Admin) {
                ((Admin) currentUser).setMatricule(matriculeField.getText().trim());
            }

            serviceUser.modifier(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur mis à jour avec succès!");
            cancel(event); // Close window

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour de l'utilisateur: " + e.getMessage());
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        if (passwordField != null && passwordTextField != null && passwordToggleBtn != null) {
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
            paysField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Veuillez remplir tous les champs obligatoires (le mot de passe est optionnel)");
            return false;
        }

        // Check if email is changed and if new email already exists
        String newEmail = emailField.getText().trim();
        String currentEmail = currentUser.getEmail();
        if (!newEmail.equals(currentEmail) && isEmailAlreadyExists(newEmail)) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                     "Cette adresse email est déjà utilisée par un autre utilisateur. Veuillez choisir une autre adresse email.");
            return false;
        }

        if (currentUser instanceof Client && cinField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le CIN est requis pour les clients");
            return false;
        }

        // Validate CIN format for clients
        if (currentUser instanceof Client && !cinField.getText().trim().isEmpty()) {
            String cin = cinField.getText().trim();
            if (!CinValidator.isValidCin(cin)) {
                String message = CinValidator.getValidationMessage(cin);
                showAlert(Alert.AlertType.WARNING, "Erreur de Validation", message != null ? message : "Format de CIN invalide!");
                return false;
            }
        }

        if (currentUser instanceof Admin && matriculeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le matricule est requis pour les administrateurs");
            return false;
        }

        // Validate Admin matricule format
        if (currentUser instanceof Admin && !isValidAdminMatricule(matriculeField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Le matricule administrateur doit être au format ---AMN------ (avec des chiffres à la place des tirets)!\nExemple: 123AMN123456");
            return false;
        }

        // Validate Moderateur matricule format (if they have matricule field)
        if (currentUser instanceof Moderateur && matriculeField.isVisible() &&
            !matriculeField.getText().trim().isEmpty() && !isValidModeratorMatricule(matriculeField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Le matricule modérateur doit être au format ---MOD------ (avec des chiffres à la place des tirets)!\nExemple: 123MOD123456");
            return false;
        }

        // Validate if matricule exists in database (only for Admin)
        if (currentUser instanceof Admin && !matriculeExistsInDatabase(matriculeField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation",
                "Ce matricule administrateur n'existe pas dans la base de données. Veuillez contacter un administrateur.");
            return false;
        }

        // Email validation for Admin and Moderateur - must be @planNova.tn
        if ((currentUser instanceof Admin || currentUser instanceof Moderateur)
                && !emailField.getText().trim().endsWith("@planNova.tn")) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "L'email des administrateurs et modérateurs doit être au format: username@planNova.tn");
            return false;
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
            return true; // Return true to be safe - prevent modification if we can't verify
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
