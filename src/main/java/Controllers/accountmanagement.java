package Controllers;

import Models.User;
import Services.ServiceUser;
import utils.PasswordUtils;
import utils.UserSession;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class accountmanagement implements Initializable {

    @FXML private Button saveChangesBtn;
    @FXML private Button cancelBtn;
    @FXML private Button updatePasswordBtn;
    @FXML private Button closeBtn;
    @FXML private Button minimizeBtn;
    @FXML private Label lengthReq;
    @FXML private Label upperReq;
    @FXML private Label numberReq;
    @FXML private Label specialReq;

    @FXML private VBox accountCard;
    @FXML private VBox securityCard;
    @FXML private HBox headerBox;
    @FXML private StackPane mainContentArea;
    @FXML private VBox slidingPanel;
    @FXML private Button togglePanelBtn;

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

    private User currentUser;
    private ServiceUser serviceUser;
    private boolean isPanelVisible = false;
    private Runnable profileUpdateCallback;

    /**
     * Sets a callback to be executed when the profile is updated
     * @param callback The callback to execute
     */
    public void setProfileUpdateCallback(Runnable callback) {
        this.profileUpdateCallback = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceUser = new ServiceUser();

        // Load user data from session
        loadUserData();

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

            System.out.println("User data loaded: " + currentUser.getNom() + " " + currentUser.getPrenom());
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

            showAlert(Alert.AlertType.INFORMATION, "Success", "Account information updated successfully!");

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

        if (!PasswordUtils.isPasswordStrong(newPassword)) {
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
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully!");

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
}