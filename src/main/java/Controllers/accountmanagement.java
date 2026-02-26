package Controllers;

import Models.User;
import Models.Client;
import Services.ServiceUser;
import Services.NotificationService;
import utils.UserSession;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class accountmanagement implements Initializable {

    @FXML private Button saveChangesBtn;
    @FXML private Button cancelBtn;
    @FXML private Button updatePasswordBtn;
    @FXML private Button becomeGuideBtn;
    @FXML private Button closeBtn;
    @FXML private Button minimizeBtn;
    @FXML private Label lengthReq;
    @FXML private Label upperReq;
    @FXML private Label numberReq;
    @FXML private Label specialReq;
    @FXML private Label roleLabel;

    @FXML private VBox accountCard;
    @FXML private VBox securityCard;
    @FXML private HBox headerBox;
    @FXML private StackPane mainContentArea;
    @FXML private VBox slidingPanel;
    @FXML private Button togglePanelBtn;
    @FXML private StackPane mainContainer;

    // Form fields for account information
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField paysField;
    @FXML private TextField roleField;
    @FXML private Label userInitials;
    @FXML private StackPane avatarStackPane;
    @FXML private Circle userAvatarCircle;
    @FXML private ImageView userAvatarImage;
    @FXML private Text userFullName;
    @FXML private Label userIdLabel;
    @FXML private Label userRoleLabel;

    // Form fields for password change
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // 2FA elements
    @FXML private VBox twoFactorSection;
    @FXML private Label twoFactorStatusLabel;
    @FXML private Label faceIdStatusLabel;
    @FXML private Label totpStatusLabel;
    @FXML private Button enable2FABtn;
    @FXML private Button disable2FABtn;

    private User currentUser;
    private ServiceUser serviceUser;
    private NotificationService notificationService;
    private boolean isPanelVisible = false;
    private Runnable profileUpdateCallback;

    /**
     * Sets a callback to be executed when the profile is updated
     * @param callback The callback to execute
     */
    public void setProfileUpdateCallback(Runnable callback) {
        this.profileUpdateCallback = callback;
    }

    /**
     * Sets the current user for the account management interface
     * @param user The user to set
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            UserSession.getInstance().setCurrentUser(user);
            loadUserData();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceUser = new ServiceUser();
        notificationService = new NotificationService();

        // Load user data from session
        loadUserData();

        // Show become guide button only for clients
        if (becomeGuideBtn != null && currentUser != null) {
            boolean isClient = currentUser instanceof Client;
            becomeGuideBtn.setVisible(isClient);
            becomeGuideBtn.setManaged(isClient);

            if (isClient) {
                checkGuideApplicationStatus();
                addButtonHoverAnimation(becomeGuideBtn);
            }
        }

        setupAnimations();
        setupButtonEffects();
        setupPasswordValidation();
        setupSlidingPanel();
        setupWindowControls();
    }

    private void loadUserData() {
        currentUser = UserSession.getInstance().getCurrentUser();

        // Sync with DB: reload user by id so email/password and other fields match the database
        if (currentUser != null) {
            try {
                User fromDb = serviceUser.recupererParId(currentUser.getIdUtilisateur());
                if (fromDb != null) {
                    currentUser = fromDb;
                    UserSession.getInstance().setCurrentUser(currentUser);
                }
            } catch (Exception e) {
                System.err.println("Could not refresh user from DB: " + e.getMessage());
            }
        }

        if (currentUser != null) {
            // Set form fields from current (DB-synced) user
            if (nomField != null) nomField.setText(currentUser.getNom());
            if (prenomField != null) prenomField.setText(currentUser.getPrenom());
            if (emailField != null) emailField.setText(currentUser.getEmail());
            if (paysField != null) paysField.setText(currentUser.getPays());

            // Set role
            String role = UserSession.getInstance().getCurrentUserType();
            if (roleField != null) roleField.setText(role != null ? role : "User");

            // Client-specific UI adjustments
            boolean isClient = UserSession.getInstance().isClient();
            if (isClient) {
                // Hide role field and label for clients
                if (roleField != null) roleField.setVisible(false);
                if (roleLabel != null) roleLabel.setVisible(false);
            } else {
                // Show role field for non-clients
                if (roleField != null) roleField.setVisible(true);
                if (roleLabel != null) roleLabel.setVisible(true);
            }

            // Set header: full name, ID, role (so it shows current user, not placeholder)
            if (userFullName != null) {
                String fullName = (currentUser.getPrenom() != null ? currentUser.getPrenom() : "") + " " +
                        (currentUser.getNom() != null ? currentUser.getNom() : "");
                userFullName.setText(fullName.trim().isEmpty() ? "User" : fullName.trim());
            }
            if (userIdLabel != null) userIdLabel.setText("ID: " + currentUser.getIdUtilisateur());
            if (userRoleLabel != null) userRoleLabel.setText(role != null ? role : "User");

            // Set user initials for avatar (fallback when no image)
            if (userInitials != null) {
                String initials = (currentUser.getPrenom() != null && !currentUser.getPrenom().isEmpty() ?
                        currentUser.getPrenom().substring(0, 1) : "") +
                        (currentUser.getNom() != null && !currentUser.getNom().isEmpty() ?
                                currentUser.getNom().substring(0, 1) : "");
                userInitials.setText(initials.isEmpty() ? "U" : initials.toUpperCase());
            }

            // Load profile image from database
            refreshProfileImage(currentUser.getImageurl());

            // Show become guide button only for clients (reuse isClient variable)
            if (becomeGuideBtn != null) {
                becomeGuideBtn.setVisible(isClient);
                becomeGuideBtn.setManaged(isClient);

                if (isClient && becomeGuideBtn.getOnMouseEntered() == null) {
                    addButtonHoverAnimation(becomeGuideBtn);
                }
            }

            System.out.println("User data loaded: " + currentUser.getNom() + " " + currentUser.getPrenom());

            // Initialize 2FA section based on user role
            initialize2FASection();
        } else {
            showAlert(Alert.AlertType.WARNING, "No User Session", "No user session found. Please log in again.");
        }
    }

    /** Loads and displays the profile image from path (DB value). Uses proper file URL for Windows. */
    private void refreshProfileImage(String imageUrl) {
        if (userAvatarImage == null || userAvatarCircle == null) return;
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                String path = imageUrl.trim();
                String url = path;
                if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("file:")) {
                    File f = new File(path);
                    url = f.exists() ? f.toURI().toASCIIString() : ("file:" + path.replace("\\", "/"));
                }
                Image img = new Image(url, 80, 80, true, true);
                if (!img.isError()) {
                    userAvatarImage.setImage(img);
                    userAvatarImage.setFitWidth(80);
                    userAvatarImage.setFitHeight(80);
                    userAvatarImage.setPreserveRatio(true);
                    userAvatarImage.setSmooth(true);  // Enable smooth scaling for better quality
                    userAvatarImage.setCache(true);   // Cache for better performance
                    Circle clip = new Circle(40);
                    clip.setCenterX(40);
                    clip.setCenterY(40);
                    userAvatarImage.setClip(clip);
                    userAvatarImage.setVisible(true);
                    if (userInitials != null) userInitials.setVisible(false);
                    // No blue fill so the profile picture is visible; keep only the white border
                    userAvatarCircle.setFill(Color.TRANSPARENT);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Failed to load profile image: " + e.getMessage());
            }
        }
        userAvatarImage.setImage(null);
        userAvatarImage.setVisible(false);
        if (userInitials != null) userInitials.setVisible(true);
        // Blue background when showing initials only
        userAvatarCircle.setFill(Color.web("#4a6fa5"));
    }

    @FXML
    private void handleAvatarClick(MouseEvent event) {
        if (currentUser == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose profile photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) (avatarStackPane != null ? avatarStackPane.getScene().getWindow() : null);
        if (stage == null) return;
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            String absolutePath = file.getAbsolutePath();
            currentUser.setImageurl(absolutePath);
            try {
                serviceUser.modifier(currentUser);
                UserSession.getInstance().setCurrentUser(currentUser);
                refreshProfileImage(absolutePath);

                // Notify dashboard to refresh profile display
                if (profileUpdateCallback != null) {
                    profileUpdateCallback.run();
                }

                showAlert(Alert.AlertType.INFORMATION, "Profile photo", "Profile photo updated successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile photo: " + e.getMessage());
            }
        }
    }

    private void setupWindowControls() {
        if (closeBtn != null) {
            closeBtn.setOnAction(e -> {
                // Animate close
                Stage stage = (Stage) closeBtn.getScene().getWindow();
                animateWindowClose(stage);
            });
        }

        if (minimizeBtn != null) {
            minimizeBtn.setOnAction(e -> {
                Stage stage = (Stage) minimizeBtn.getScene().getWindow();
                stage.setIconified(true);
            });
        }
    }

    private void animateWindowClose(Stage stage) {
        if (stage == null || stage.getScene() == null) return;

        Parent root = stage.getScene().getRoot();

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), root);
        scaleTransition.setToX(0.8);
        scaleTransition.setToY(0.8);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), root);
        fadeTransition.setToValue(0);

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.setOnFinished(e -> stage.close());
        parallelTransition.play();
    }

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user session available.");
            return;
        }

        String nom = nomField != null ? nomField.getText().trim() : "";
        String prenom = prenomField != null ? prenomField.getText().trim() : "";
        String email = emailField != null ? emailField.getText().trim() : "";
        String pays = paysField != null ? paysField.getText().trim() : "";

        // Validation: same constraints as create account
        if (nom.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Nom is required.");
            return;
        }
        if (prenom.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Prénom is required.");
            return;
        }
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email is required.");
            return;
        }
        // Admin and Moderateur must use @planNova.tn
        if ((currentUser instanceof Models.Admin || currentUser instanceof Models.Moderateur)
                && !email.endsWith("@planNova.tn")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email must end with @planNova.tn for Admin and Moderateur.");
            return;
        }
        if (pays.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Pays is required.");
            return;
        }

        try {
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);
            currentUser.setPays(pays);

            // Save to database
            serviceUser.modifier(currentUser);

            // Update session
            UserSession.getInstance().setCurrentUser(currentUser);

            // Refresh header so displayed name updates immediately
            if (userFullName != null) userFullName.setText(prenom + " " + nom);

            // Notify dashboard to refresh profile display
            if (profileUpdateCallback != null) {
                profileUpdateCallback.run();
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Informations du compte mises à jour avec succès!");

            // Animate success
            if (accountCard != null) animateSuccess(accountCard);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdatePassword(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user session available.");
            return;
        }

        String currentPassword = currentPasswordField != null ? currentPasswordField.getText() : "";
        String newPassword = newPasswordField != null ? newPasswordField.getText() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        // Validate current password
        if (currentPassword == null || currentPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter your current password.");
            return;
        }

        // Verify current password
        try {
            if (!serviceUser.verifyCurrentPassword(currentUser.getIdUtilisateur(), currentPassword)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect.");
                return;
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to verify password: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Validate new password
        if (newPassword == null || newPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a new password.");
            return;
        }

        if (!isPasswordStrong(newPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password must be at least 8 characters with uppercase, lowercase, number, and special character.");
            return;
        }

        // Confirm password match
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match.");
            return;
        }

        // Update password
        try {
            boolean success = serviceUser.updatePassword(currentUser.getIdUtilisateur(), newPassword);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe mis à jour avec succès!");

                // Clear password fields
                if (currentPasswordField != null) currentPasswordField.clear();
                if (newPasswordField != null) newPasswordField.clear();
                if (confirmPasswordField != null) confirmPasswordField.clear();

                // Animate success
                if (securityCard != null) animateSuccess(securityCard);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Reload user data to discard changes
        loadUserData();

        // Add shake animation to indicate reset
        if (accountCard != null) {
            ShakeTransition shakeAccount = new ShakeTransition(accountCard);
            shakeAccount.play();
        }
    }

    @FXML
    private void toggleSlidingPanel() {
        if (slidingPanel == null) return;

        double targetX = isPanelVisible ? slidingPanel.getWidth() : 0;

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), slidingPanel);
        slide.setToX(targetX);
        slide.setInterpolator(Interpolator.EASE_BOTH);

        // Rotate toggle button icon
        if (togglePanelBtn != null) {
            RotateTransition rotate = new RotateTransition(Duration.millis(300), togglePanelBtn);
            rotate.setByAngle(isPanelVisible ? -180 : 180);
            rotate.play();
        }

        slide.play();
        isPanelVisible = !isPanelVisible;
    }

    private void setupSlidingPanel() {
        if (slidingPanel != null) {
            // Initially hide the panel
            slidingPanel.setTranslateX(slidingPanel.getWidth());

            // Add hover effect to toggle button
            if (togglePanelBtn != null) {
                togglePanelBtn.setOnAction(e -> toggleSlidingPanel());

                // Pulse animation to draw attention to the panel
                PulseTransition pulse = new PulseTransition(togglePanelBtn);
                pulse.setDelay(Duration.seconds(1));
                pulse.play();
            }
        }
    }

    private void animateSuccess(Region region) {
        if (region == null) return;
        ColorTransition colorTransition = new ColorTransition(region);
        colorTransition.play();
    }

    private void setupAnimations() {
        // Fade in animation for the entire scene
        if (accountCard != null && accountCard.getParent() != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.8), accountCard.getParent());
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.play();
        }

        // Slide up animation for cards
        if (accountCard != null) {
            TranslateTransition slideAccount = new TranslateTransition(Duration.seconds(0.6), accountCard);
            slideAccount.setFromY(50);
            slideAccount.setToY(0);
            slideAccount.setInterpolator(Interpolator.EASE_OUT);
            slideAccount.play();
        }

        if (securityCard != null) {
            TranslateTransition slideSecurity = new TranslateTransition(Duration.seconds(0.6), securityCard);
            slideSecurity.setFromY(50);
            slideSecurity.setToY(0);
            slideSecurity.setInterpolator(Interpolator.EASE_OUT);
            slideSecurity.setDelay(Duration.seconds(0.2));
            slideSecurity.play();
        }

        // Header slide down
        if (headerBox != null) {
            TranslateTransition slideHeader = new TranslateTransition(Duration.seconds(0.5), headerBox);
            slideHeader.setFromY(-100);
            slideHeader.setToY(0);
            slideHeader.setInterpolator(Interpolator.EASE_OUT);
            slideHeader.play();
        }

        // Avatar pulse animation
        if (userAvatarCircle != null) {
            PulseTransition pulseAvatar = new PulseTransition(userAvatarCircle);
            pulseAvatar.setDelay(Duration.seconds(1.5));
            pulseAvatar.play();
        }
    }

    private void setupButtonEffects() {
        // Save Changes button hover effect
        if (saveChangesBtn != null) addHoverEffect(saveChangesBtn, "#4a6fa5", "#5d7fb5");

        // Cancel button hover effect
        if (cancelBtn != null) addHoverEffect(cancelBtn, "transparent", "#fee", "#dc3545", "#c82333");

        // Update Password button hover effect
        if (updatePasswordBtn != null) addHoverEffect(updatePasswordBtn, "#4a6fa5", "#5d7fb5");

        // Button click animations
        if (saveChangesBtn != null) addClickAnimation(saveChangesBtn);
        if (cancelBtn != null) addClickAnimation(cancelBtn);
        if (updatePasswordBtn != null) addClickAnimation(updatePasswordBtn);
    }

    private void addHoverEffect(Button button, String normalColor, String hoverColor) {
        button.setOnMouseEntered(e -> {
            animateButtonColor(button, normalColor, hoverColor);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        button.setOnMouseExited(e -> {
            animateButtonColor(button, hoverColor, normalColor);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void addHoverEffect(Button button, String normalBg, String hoverBg, String normalText, String hoverText) {
        String baseStyle = button.getStyle();
        button.setOnMouseEntered(e -> {
            button.setStyle(baseStyle
                    .replace(normalBg, hoverBg)
                    .replace(normalText, hoverText));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle
                    .replace(hoverBg, normalBg)
                    .replace(hoverText, normalText));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void animateButtonColor(Button button, String fromColor, String toColor) {
        String currentStyle = button.getStyle();
        button.setStyle(currentStyle.replace(fromColor, toColor));
    }

    private void addClickAnimation(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(0.95);
            st.setToY(0.95);
            st.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }


    private void setupPasswordValidation() {
        // Real-time password validation when typing in new password field
        if (newPasswordField != null) {
            newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
                validatePassword(newValue);
            });
        }
    }

    private void validatePassword(String password) {
        // Update password requirement labels
        if (lengthReq != null)
            updateRequirementLabel(lengthReq, password != null && password.length() >= 8);
        if (upperReq != null)
            updateRequirementLabel(upperReq, password != null && password.matches(".*[A-Z].*"));
        if (numberReq != null)
            updateRequirementLabel(numberReq, password != null && password.matches(".*\\d.*"));
        if (specialReq != null)
            updateRequirementLabel(specialReq, password != null && password.matches(".*[!@#$%^&*].*"));
    }

    private void updateRequirementLabel(Label label, boolean met) {
        if (met) {
            label.setStyle("-fx-text-fill: #28a745;");
        } else {
            label.setStyle("-fx-text-fill: #6c757d;");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return password.matches(".*[A-Z].*") && // At least one uppercase
               password.matches(".*[a-z].*") && // At least one lowercase
               password.matches(".*\\d.*") &&   // At least one digit
               password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"); // At least one special char
    }

    // Custom Transition Classes
    class ShakeTransition extends Transition {
        private final Node node;
        private final double startX;

        public ShakeTransition(Node node) {
            this.node = node;
            this.startX = node.getTranslateX();
            setCycleDuration(Duration.millis(500));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        @Override
        protected void interpolate(double frac) {
            double delta = Math.sin(frac * 10 * Math.PI) * 10 * (1 - frac);
            node.setTranslateX(startX + delta);
        }
    }

    class PulseTransition extends Transition {
        private final Node node;
        private final double initialScale;

        public PulseTransition(Node node) {
            this.node = node;
            this.initialScale = node.getScaleX();
            setCycleDuration(Duration.millis(1000));
            setCycleCount(INDEFINITE);
            setAutoReverse(true);
            setInterpolator(Interpolator.EASE_BOTH);
        }

        @Override
        protected void interpolate(double frac) {
            double scale = initialScale + (Math.sin(frac * Math.PI) * 0.1);
            node.setScaleX(scale);
            node.setScaleY(scale);
        }
    }

    class ColorTransition extends Transition {
        private final Region region;
        private final String originalStyle;

        public ColorTransition(Region region) {
            this.region = region;
            this.originalStyle = region.getStyle();
            setCycleDuration(Duration.millis(500));
            setCycleCount(2);
            setAutoReverse(true);
        }

        @Override
        protected void interpolate(double frac) {
            if (frac < 0.5) {
                region.setStyle("-fx-background-color: #d4edda; -fx-background-radius: 20;");
            } else {
                region.setStyle(originalStyle);
            }
        }
    }

    @FXML
    private void handleBecomeGuide(ActionEvent event) {
        try {
            // Load the guide requirements dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guideRequirements.fxml"));
            Parent root = loader.load();

            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Devenir Guide de Voyage - PlaNova");
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // Add fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            dialogStage.setOnShown(e -> fadeIn.play());

            // Show dialog and wait for result
            dialogStage.showAndWait();

            // Refresh user data and check application status after dialog closes
            loadUserData();
            checkGuideApplicationStatus();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de demande de guide: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Inattendue", "Une erreur s'est produite: " + e.getMessage());
        }
    }

    /**
     * Check if client has pending guide application and update button state
     */
    private void checkGuideApplicationStatus() {
        if (currentUser == null || !(currentUser instanceof Client) || becomeGuideBtn == null) {
            return;
        }

        // Run check in background to avoid blocking UI
        javafx.concurrent.Task<Boolean> checkTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return notificationService.hasClientPendingGuideApplication(currentUser.getEmail());
            }
        };

        checkTask.setOnSucceeded(e -> {
            boolean hasPendingApplication = checkTask.getValue();
            javafx.application.Platform.runLater(() -> {
                if (hasPendingApplication) {
                    // Disable button and update styling for pending state
                    becomeGuideBtn.setDisable(true);
                    becomeGuideBtn.setText("⏳ Demande en Attente");
                    becomeGuideBtn.setStyle(
                        "-fx-background-color: #fbbf24; " +
                        "-fx-text-fill: #92400e; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15; " +
                        "-fx-padding: 12 20; " +
                        "-fx-font-size: 13px; " +
                        "-fx-cursor: default;"
                    );

                    // Add tooltip to explain the state
                    Tooltip tooltip = new Tooltip("Votre demande pour devenir guide est en cours de traitement par l'administration");
                    tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #374151; -fx-text-fill: white; -fx-background-radius: 8;");
                    Tooltip.install(becomeGuideBtn, tooltip);
                } else {
                    // Enable button and restore normal styling
                    becomeGuideBtn.setDisable(false);
                    becomeGuideBtn.setText("🎯 Devenir Guide");
                    becomeGuideBtn.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #10b981, #059669); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15; " +
                        "-fx-padding: 12 20; " +
                        "-fx-font-size: 13px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.3), 8, 0, 0, 3);"
                    );

                    // Remove tooltip
                    Tooltip.uninstall(becomeGuideBtn, null);
                }
            });
        });

        checkTask.setOnFailed(e -> {
            System.err.println("❌ Erreur lors de la vérification du statut de demande: " +
                             checkTask.getException().getMessage());
        });

        new Thread(checkTask).start();
    }

    private void addButtonHoverAnimation(Button button) {
        if (button == null) return;

        // Scale animation on hover
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> {
            scaleOut.stop();
            scaleIn.play();
        });

        button.setOnMouseExited(e -> {
            scaleIn.stop();
            scaleOut.play();
        });
    }

    /**
     * Initialize 2FA section visibility and status based on user role
     */
    private void initialize2FASection() {
        if (twoFactorSection == null) return;

        // Check if user is admin, moderator, or client (allow 2FA for these roles)
        String userType = UserSession.getInstance().getCurrentUserType();
        boolean canUse2FA = "Admin".equals(userType) || "Moderateur".equals(userType) || "Client".equals(userType);

        if (canUse2FA) {
            twoFactorSection.setVisible(true);
            twoFactorSection.setManaged(true);

            // Update 2FA status
            update2FAStatus();

            // Add hover animations to 2FA buttons
            if (enable2FABtn != null) {
                addButtonHoverAnimation(enable2FABtn);
            }
            if (disable2FABtn != null) {
                addButtonHoverAnimation(disable2FABtn);
            }
        } else {
            // Hide 2FA section only for guides
            twoFactorSection.setVisible(false);
            twoFactorSection.setManaged(false);
        }
    }

    /**
     * Update 2FA status display
     */
    private void update2FAStatus() {
        if (currentUser != null && twoFactorStatusLabel != null && enable2FABtn != null && disable2FABtn != null) {
            boolean hasFaceId = currentUser.getFaceModelData() != null;
            boolean hasTotp = currentUser.isTotpEnabled();
            boolean is2FAEnabled = currentUser.isTwoFactorEnabled() && (hasFaceId || hasTotp);

            // Debug logging
            System.out.println("=== 2FA Status Update ===");
            System.out.println("User ID: " + currentUser.getIdUtilisateur());
            System.out.println("TOTP Secret Key: " + (currentUser.getTotpSecretKey() != null ? "Present (length=" + currentUser.getTotpSecretKey().length() + ")" : "NULL"));
            System.out.println("Has TOTP: " + hasTotp);
            System.out.println("Has FaceID: " + hasFaceId);
            System.out.println("2FA Enabled Flag: " + currentUser.isTwoFactorEnabled());
            System.out.println("Overall 2FA Status: " + is2FAEnabled);
            System.out.println("========================");

            if (is2FAEnabled) {
                // 2FA is enabled
                twoFactorStatusLabel.setText("Activé");
                twoFactorStatusLabel.setStyle("-fx-background-color: #28a745; -fx-background-radius: 20; -fx-text-fill: white; -fx-padding: 6 15; -fx-font-size: 12px; -fx-font-weight: bold;");

                // Update button text
                enable2FABtn.setText("⚙️ Reconfigurer 2FA");
                disable2FABtn.setVisible(true);
                disable2FABtn.setManaged(true);
            } else {
                // 2FA is disabled
                twoFactorStatusLabel.setText("Désactivé");
                twoFactorStatusLabel.setStyle("-fx-background-color: #dc3545; -fx-background-radius: 20; -fx-text-fill: white; -fx-padding: 6 15; -fx-font-size: 12px; -fx-font-weight: bold;");

                // Update button text
                enable2FABtn.setText("⚙️ Configurer 2FA");
                disable2FABtn.setVisible(false);
                disable2FABtn.setManaged(false);
            }

            // Update individual method status
            if (faceIdStatusLabel != null) {
                if (hasFaceId) {
                    faceIdStatusLabel.setText("✅ Configuré");
                    faceIdStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                } else {
                    faceIdStatusLabel.setText("❌ Non configuré");
                    faceIdStatusLabel.setStyle("-fx-text-fill: #dc3545;");
                }
            }

            if (totpStatusLabel != null) {
                if (hasTotp) {
                    totpStatusLabel.setText("✅ Configuré");
                    totpStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                } else {
                    totpStatusLabel.setText("❌ Non configuré");
                    totpStatusLabel.setStyle("-fx-text-fill: #dc3545;");
                }
            }
        }
    }

    /**
     * Handle setup 2FA button click
     */
    @FXML
    private void handleSetup2FA(ActionEvent event) {
        if (currentUser == null) return;

        try {
            // Load unified 2FA setup dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/twoFactorSetup.fxml"));
            Parent root = loader.load();

            // Get the controller
            TwoFactorSetupController controller = loader.getController();

            // Create and show modal window
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Configuration 2FA - PlaNova");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Center on parent window
            Stage parentStage = (Stage) enable2FABtn.getScene().getWindow();
            stage.initOwner(parentStage);

            // Add close handler to refresh status
            stage.setOnCloseRequest(e -> {
                // Refresh user from database to get updated 2FA settings
                try {
                    User refreshedUser = serviceUser.recupererParId(currentUser.getIdUtilisateur());
                    if (refreshedUser != null) {
                        currentUser = refreshedUser;
                        UserSession.getInstance().setCurrentUser(currentUser);
                        update2FAStatus();

                        // Notify profile update callback if set
                        if (profileUpdateCallback != null) {
                            profileUpdateCallback.run();
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to refresh user data: " + ex.getMessage());
                }
            });

            try {
                stage.getIcons().add(new Image("/logo.PNG"));
            } catch (Exception e) {
                // Icon loading failed, continue without icon
            }

            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir l'interface de configuration 2FA: " + e.getMessage());
        }
    }

    /**
     * Handle disable 2FA button click
     */
    @FXML
    private void handleDisable2FA(ActionEvent event) {
        if (currentUser == null) return;

        // Enhanced confirmation dialog with better styling
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Désactiver 2FA");
        confirmAlert.setHeaderText("🔒 Désactiver l'Authentification à Deux Facteurs");

        String methods = "";
        if (currentUser.getFaceModelData() != null) {
            methods += "• Reconnaissance faciale\n";
        }
        if (currentUser.isTotpEnabled()) {
            methods += "• Microsoft Authenticator\n";
        }

        confirmAlert.setContentText("Êtes-vous sûr de vouloir désactiver l'authentification à deux facteurs?\n\n" +
                "Méthodes actuellement configurées:\n" + methods + "\n" +
                "⚠️ Attention: Cela réduira considérablement la sécurité de votre compte.");

        // Custom buttons
        ButtonType disableButton = new ButtonType("🔓 Désactiver", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(disableButton, cancelButton);

        // Add icon to dialog
        try {
            Stage dialogStage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(new Image("/logo.PNG"));
        } catch (Exception e) {
            // Icon loading failed, continue without icon
        }

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == disableButton) {
                try {
                    // Disable 2FA in database - clear all 2FA data
                    currentUser.setTwoFactorEnabled(false);
                    currentUser.setFaceModelData(null);
                    currentUser.setTotpSecretKey(null);
                    serviceUser.modifier(currentUser);

                    // Update session
                    UserSession.getInstance().setCurrentUser(currentUser);

                    // Update UI
                    update2FAStatus();

                    // Notify profile update callback if set
                    if (profileUpdateCallback != null) {
                        profileUpdateCallback.run();
                    }

                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "2FA Désactivé",
                            "✅ L'authentification à deux facteurs a été complètement désactivée.\n\n" +
                            "Toutes les méthodes d'authentification (reconnaissance faciale et Microsoft Authenticator) " +
                            "ont été supprimées de votre compte.");

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "❌ Erreur lors de la désactivation de 2FA: " + e.getMessage());
                }
            }
        });
    }
}