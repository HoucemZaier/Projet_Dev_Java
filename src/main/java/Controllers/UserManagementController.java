package Controllers;

import Models.*;
import Services.ServiceUser;
import javafx.util.Duration;
import utils.UserSession;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.animation.KeyValue;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private Button overviewBtn;
    @FXML private Button transportPubliqueBtn;
    @FXML private Button transportPriveeBtn;
    @FXML
    private VBox userContainer;
    @FXML
    private TextField searchField;
    @FXML
    private TextField headerSearchField;
    @FXML
    private ComboBox<String> countryFilter;
    @FXML
    private Label totalUsersLabel, activeUsersLabel, blockedUsersLabel;
    @FXML
    private HBox headerSection;
    @FXML
    private HBox actionsBar;

    private ServiceUser serviceUser = new ServiceUser();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Validate all FXML fields are properly injected
            if (userContainer == null) {
                throw new RuntimeException("userContainer not injected by FXML");
            }
            if (totalUsersLabel == null || activeUsersLabel == null || blockedUsersLabel == null) {
                throw new RuntimeException("Label fields not injected by FXML");
            }
            if (countryFilter == null) {
                throw new RuntimeException("countryFilter not injected by FXML");
            }

            // Configure the stage to be resizable and allow maximizing
            Platform.runLater(() -> {
                try {
                    Stage stage = (Stage) userContainer.getScene().getWindow();
                    if (stage != null) {
                        stage.setResizable(true);
                        stage.setMaximized(true); // Allow full screen for better visibility
                        stage.setMinWidth(1100);
                        stage.setMinHeight(700);
                    }
                } catch (Exception e) {
                    System.err.println("Could not configure stage: " + e.getMessage());
                }
            });

            // Check user role and hide elements for Moderateur
            if (UserSession.getInstance().isModerator()) {
                // Hide header and actions for Moderateur
                if (headerSection != null) {
                    headerSection.setVisible(false);
                    headerSection.setManaged(false);
                }
                if (actionsBar != null) {
                    actionsBar.setVisible(false);
                    actionsBar.setManaged(false);
                }
            }

            loadUsers();
            setupFilters();
            updateStats();
            setupUserContainer();
        } catch (Exception e) {
            System.err.println("Error during dashboard initialization: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'Initialisation",
                "Échec de l'initialisation du tableau de bord: " + e.getMessage());
        }
    }

    private void setupUserContainer() {
        userContainer.getChildren().clear();
        User currentLoggedUser = UserSession.getInstance().getCurrentUser();

        for (User user : userList) {
            // Skip showing the current logged-in admin's card to prevent self-modification
            if (currentLoggedUser != null &&
                currentLoggedUser instanceof Admin &&
                user.getIdUtilisateur() == currentLoggedUser.getIdUtilisateur()) {
                System.out.println("Skipping current admin user from list: " + user.getNom() + " " + user.getPrenom());
                continue; // Skip adding this card
            }

            userContainer.getChildren().add(createUserCard(user));
        }
    }
    
    @FXML
    private void handleOverview(ActionEvent event) {
        navigateToDashboard();
    }
    
    @FXML
    private void handleTransportPublique(ActionEvent event) {
        // Navigate to Transport Publique management
        navigateTo("/gestionTransport.fxml", "Transport Publique Management");
    }

    @FXML
    private void handleTransportPrivee(ActionEvent event) {
        // Navigate to Transport Privée management
        navigateTo("/gestionTransport.fxml", "Transport Privée Management");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Show professional confirmation dialog before logout
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de Déconnexion");
        alert.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        alert.setContentText("Vous serez redirigé vers l'écran de connexion et perdrez votre session actuelle.");

        // Add custom professional styling to the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/login.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog");

        // Customize button text and styling
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        okButton.setText("Se déconnecter");
        okButton.getStyleClass().add("logout-confirm-btn");

        cancelButton.setText("Annuler");
        cancelButton.getStyleClass().add("logout-cancel-btn");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Clear user session
                UserSession.getInstance().logout();

                // Navigate back to login screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Planova - Connexion");
                stage.centerOnScreen();
                stage.setResizable(false);

                System.out.println("Utilisateur déconnecté avec succès");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la déconnexion: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleDashbordManagement(ActionEvent event) {
        // Check if current user is Admin
        navigateTo("dashboard.fxml", "User Management");
    }
    private void navigateTo(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // If navigating to dashbord, set the user context
            if (fxmlFile.equals("/dashboard.fxml")) {
                UserManagementController controller = loader.getController();
                if (controller != null && currentUser != null) {
                    controller.setCurrentUser(currentUser);
                }
            }

            // Get the stage from any button
            Stage stage = (Stage) overviewBtn.getScene().getWindow();

            // Add transition animation
            Parent currentRoot = stage.getScene().getRoot();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            fadeOut.setOnFinished(e -> {
                stage.setScene(new Scene(root));
                stage.setTitle("PlaNova - " + title);

                // Allow full screen and resizing for better visibility
                stage.setResizable(true);
                stage.setMaximized(true);
                stage.setMinWidth(1100);
                stage.setMinHeight(700);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });

            fadeOut.play();

            System.out.println("Navigated to: " + title);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", "Échec de la navigation vers " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createUserCard(User user) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setUserData(user); // Store user reference for later retrieval

        // Avatar with image from database
        StackPane avatarContainer = new StackPane();
        avatarContainer.getStyleClass().add("user-avatar");
        avatarContainer.setPrefSize(50, 50);
        avatarContainer.setMaxSize(50, 50);
        avatarContainer.setMinSize(50, 50);

        Circle avatar = new Circle(25);

        // Try to load user image from database
        if (user.getImageurl() != null && !user.getImageurl().isEmpty()) {
            try {
                // Load image from URL/path stored in database
                Image userImage = new Image(user.getImageurl(), 50, 50, true, true);
                ImageView imageView = new ImageView(userImage);
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);

                // Create circular clip for the image
                Circle clip = new Circle(25);
                imageView.setClip(clip);

                avatarContainer.getChildren().add(imageView);
            } catch (Exception e) {
                System.err.println("Failed to load user image: " + user.getImageurl() + " - " + e.getMessage());
                // Fallback to colored circle
                avatar.setFill(getAvatarColor(user));
                avatarContainer.getChildren().add(avatar);
            }
        } else {
            // No image in database, use colored circle with initials
            avatar.setFill(getAvatarColor(user));

            // Add user initials
            String initials = (user.getNom().charAt(0) + "" + user.getPrenom().charAt(0)).toUpperCase();
            Label initialsLabel = new Label(initials);
            initialsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

            avatarContainer.getChildren().addAll(avatar, initialsLabel);
        }

        // User info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        Label countryLabel = new Label("Pays: " + user.getPays());
        countryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");

        // Status label
        Label statusLabel = new Label(user.isBlocked() ? "Bloqué" : "Actif");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
        if (user.isBlocked()) {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
        } else {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;");
        }

        infoBox.getChildren().addAll(nameLabel, emailLabel, countryLabel, statusLabel);

        // Role badge
        Label roleLabel = new Label(getUserType(user));
        roleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 5 10 5 10; -fx-background-radius: 12;");

        if (user instanceof Client) {
            roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #27ae60; -fx-text-fill: white;");
        } else if (user instanceof Moderateur) {
            roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #e67e22; -fx-text-fill: white;");
        } else if (user instanceof Guide) {
            roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #9b59b6; -fx-text-fill: white;");
        } else {
            roleLabel.setStyle(roleLabel.getStyle() + "-fx-background-color: #34495e; -fx-text-fill: white;");
        }

        // Action buttons (Edit, Block/Unblock, and Delete) - beautiful icon-only buttons
        HBox actionBox = new HBox(8);
        actionBox.getStyleClass().add("action-buttons-container");

        // Enhanced Edit Button with professional white icon
        Button modifyCardBtn = new Button();
        ImageView editIcon = new ImageView(new Image("https://img.icons8.com/ios-filled/50/ffffff/edit.png"));
        editIcon.setFitWidth(16);
        editIcon.setFitHeight(16);
        modifyCardBtn.setGraphic(editIcon);
        modifyCardBtn.getStyleClass().addAll("edit-btn", "action-btn-square");
        modifyCardBtn.setOnAction(event -> {
            // Add click feedback animation
            addClickFeedback(modifyCardBtn, "#3b82f6");
            openModifyUserWindow(user);
        });
        addButtonAnimation(modifyCardBtn);

        // Block/Unblock Button with professional icon
        Button blockUnblockBtn = new Button();
        ImageView blockIcon;
        if (user.isBlocked()) {
            // Unblock icon
            blockIcon = new ImageView(new Image("https://img.icons8.com/ios-filled/50/ffffff/unlock.png"));
            blockUnblockBtn.getStyleClass().addAll("unblock-btn", "action-btn-square");
            blockUnblockBtn.setOnAction(event -> {
                addClickFeedback(blockUnblockBtn, "#10b981");
                showUnblockConfirmation(user);
            });
        } else {
            // Block icon
            blockIcon = new ImageView(new Image("https://img.icons8.com/ios-filled/50/ffffff/lock.png"));
            blockUnblockBtn.getStyleClass().addAll("block-btn", "action-btn-square");
            blockUnblockBtn.setOnAction(event -> {
                addClickFeedback(blockUnblockBtn, "#f59e0b");
                showBlockConfirmation(user);
            });
        }
        blockIcon.setFitWidth(16);
        blockIcon.setFitHeight(16);
        blockUnblockBtn.setGraphic(blockIcon);
        addButtonAnimation(blockUnblockBtn);

        // Enhanced Delete Button with professional white icon
        Button deleteCardBtn = new Button();
        ImageView deleteIcon = new ImageView(new Image("https://img.icons8.com/ios-filled/50/ffffff/trash.png"));
        deleteIcon.setFitWidth(16);
        deleteIcon.setFitHeight(16);
        deleteCardBtn.setGraphic(deleteIcon);
        deleteCardBtn.getStyleClass().addAll("delete-btn", "action-btn-square");
        deleteCardBtn.setOnAction(event -> {
            // Add click feedback animation
            addClickFeedback(deleteCardBtn, "#ef4444");
            openDeleteConfirmation(user);
        });
        addButtonAnimation(deleteCardBtn);

        actionBox.getChildren().addAll(modifyCardBtn, blockUnblockBtn, deleteCardBtn);

        // Add all elements to card
        card.getChildren().addAll(avatarContainer, infoBox, roleLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.getChildren().add(actionBox);

        // Click handler - show user info with animations
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                // Single click: Select user and show UserInfo
                selectCard(card, user);
                showUserInfo(user);
            }
        });

        // Hover effect
        card.setOnMouseEntered(event -> {
            if (!card.getStyle().contains("-fx-border-color")) {
                card.setStyle(card.getStyle() + "-fx-opacity: 0.9;");
            }
        });

        card.setOnMouseExited(event -> {
            if (!card.getStyle().contains("-fx-border-color")) {
                card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");
            }
        });

        return card;
    }

    private void addButtonAnimation(Button btn) {
        // Enhanced hover animation with smooth scaling and rotation
        btn.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), btn);
            scaleUp.setToX(1.15);
            scaleUp.setToY(1.15);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition rotate = new RotateTransition(Duration.millis(200), btn);
            rotate.setToAngle(5);
            rotate.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition hoverAnimation = new ParallelTransition(scaleUp, rotate);
            hoverAnimation.play();

            // Add subtle pulse
            addSubtlePulse(btn);
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), btn);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition resetRotate = new RotateTransition(Duration.millis(200), btn);
            resetRotate.setToAngle(0);
            resetRotate.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition exitAnimation = new ParallelTransition(scaleDown, resetRotate);
            exitAnimation.play();

            // Remove pulse
            removeSubtlePulse(btn);
        });

        // Simple press animation
        btn.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(100), btn);
            press.setToX(0.9);
            press.setToY(0.9);
            press.setInterpolator(Interpolator.EASE_IN);
            press.play();
        });

        btn.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(150), btn);
            release.setToX(1.15); // Return to hover state
            release.setToY(1.15);
            release.setInterpolator(Interpolator.EASE_OUT);
            release.play();
        });
    }

    // Simplified pulse effect
    private void addSubtlePulse(Button btn) {
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(btn.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(1000),
                new KeyValue(btn.opacityProperty(), 0.8, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(2000),
                new KeyValue(btn.opacityProperty(), 1.0, Interpolator.EASE_BOTH))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
        btn.setUserData(pulse);
    }

    // Remove pulse effect
    private void removeSubtlePulse(Button btn) {
        Object userData = btn.getUserData();
        if (userData instanceof Timeline) {
            Timeline pulse = (Timeline) userData;
            pulse.stop();

            // Smooth return to normal opacity
            Timeline resetOpacity = new Timeline(
                new KeyFrame(Duration.millis(200),
                    new KeyValue(btn.opacityProperty(), 1.0, Interpolator.EASE_OUT))
            );
            resetOpacity.play();

            btn.setUserData(null);
        }
    }

    private void selectCard(HBox card, User user) {
        // Remove previous selection style from all cards
        if (userContainer != null) {
            for (javafx.scene.Node node : userContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox otherCard = (HBox) node;
                    otherCard.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");
                }
            }
        }

        // Apply selection style to clicked card
        card.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #3498db; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.3), 5, 0, 0, 2); -fx-cursor: hand;");

        System.out.println("Selected user: " + user.getNom() + " " + user.getPrenom());
    }

    private void openModifyUserWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifyUser.fxml"));
            Parent root = loader.load();

            ModifyUserController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Modify User");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh list after modifying
            loadUsers();
            updateStats();
            setupUserContainer(); // Refresh UI

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ouverture de la fenêtre de modification utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openDeleteConfirmation(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/deleteConfirmation.fxml"));
            Parent root = loader.load();

            DeleteConfirmationController controller = loader.getController();
            controller.setUser(user);
            controller.setOnDeleteConfirmed(() -> {
                Platform.runLater(() -> {
                    loadUsers();
                    updateStats();
                    setupUserContainer(); // Refresh UI
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès!");
                });
            });

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/login.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setResizable(false);

            // Add fade-in animation
            stage.setOnShown(event -> {
                FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(300));
                fadeIn.setNode(root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            stage.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ouverture de la confirmation de suppression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private javafx.scene.paint.Color getAvatarColor(User user) {
        // Generate consistent color based on user type
        if (user instanceof Client) {
            return javafx.scene.paint.Color.web("#27ae60");
        } else if (user instanceof Admin) {
            return javafx.scene.paint.Color.web("#3498db");
        } else if (user instanceof Moderateur) {
            return javafx.scene.paint.Color.web("#e67e22");
        } else if (user instanceof Guide) {
            return javafx.scene.paint.Color.web("#9b59b6");
        }
        return javafx.scene.paint.Color.LIGHTGRAY;
    }

    private void showUserInfo(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/userInfo.fxml"));
            Parent root = loader.load();

            UserInfoController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("User Information");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Add fade-in animation when stage shows
            stage.setOnShown(event -> {
                FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(400));
                fadeIn.setNode(root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ouverture des informations utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try {
            System.out.println("Attempting to load users from database...");
            List<User> users = serviceUser.recuperer();
            if (users == null) {
                System.err.println("Service returned null user list");
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Échec de la récupération des utilisateurs: le service a renvoyé null");
                return;
            }

            // Filter out the current admin user from the list
            User currentLoggedUser = UserSession.getInstance().getCurrentUser();
            if (currentLoggedUser != null && currentLoggedUser instanceof Admin) {
                users.removeIf(user -> user.getIdUtilisateur() == currentLoggedUser.getIdUtilisateur());
                System.out.println("Filtered out current admin user from the list");
            }

            System.out.println("Successfully loaded " + users.size() + " users (excluding current admin)");
            userList.setAll(users);
        } catch (SQLException e) {
            System.err.println("SQL Error loading users: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Base de Données", "Échec du chargement des utilisateurs: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue lors du chargement des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        try {
            // Country filter setup (you can populate with actual countries)
            if (countryFilter != null) {
                countryFilter.setItems(FXCollections.observableArrayList("All", "Tunisia", "France", "USA"));
                countryFilter.setValue("All");
            } else {
                System.err.println("Warning: countryFilter is null");
            }

            // Search functionality
            if (searchField != null) {
                searchField.textProperty().addListener((obs, oldText, newText) -> {
                    filterUsers(newText, countryFilter != null ? countryFilter.getValue() : "All");
                });
            } else {
                System.err.println("Warning: searchField is null");
            }

            if (countryFilter != null) {
                countryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
                    filterUsers(searchField != null ? searchField.getText() : "", newVal);
                });
            }
        } catch (Exception e) {
            System.err.println("Error setting up filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterUsers(String searchText, String country) {
        try {
            List<User> allUsers = serviceUser.recuperer();
            ObservableList<User> filteredList = FXCollections.observableArrayList();
            User currentLoggedUser = UserSession.getInstance().getCurrentUser();

            for (User user : allUsers) {
                // Skip the current logged-in admin from appearing in the filtered list
                if (currentLoggedUser != null &&
                    currentLoggedUser instanceof Admin &&
                    user.getIdUtilisateur() == currentLoggedUser.getIdUtilisateur()) {
                    continue; // Skip this user
                }

                boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    user.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                    user.getPrenom().toLowerCase().contains(searchText.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(searchText.toLowerCase());

                boolean matchesCountry = "All".equals(country) || country.equals(user.getPays());

                if (matchesSearch && matchesCountry) {
                    filteredList.add(user);
                }
            }

            userList.setAll(filteredList);
            setupUserContainer(); // Refresh the UI after filtering
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du filtrage des utilisateurs: " + e.getMessage());
        }
    }

    private void updateStats() {
        try {
            int totalUsers = userList.size();
            int activeUsers = serviceUser.countActiveUsers();
            int blockedUsers = serviceUser.countBlockedUsers();

            totalUsersLabel.setText(String.valueOf(totalUsers));
            activeUsersLabel.setText(String.valueOf(activeUsers));
            blockedUsersLabel.setText(String.valueOf(blockedUsers));
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour des statistiques : " + e.getMessage());
        }
    }

    private void showBlockConfirmation(User user) {
        // Create custom professional confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("");

        // Remove default styling and add custom
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());
        dialogPane.getStyleClass().add("professional-confirmation-dialog");

        // Create custom content
        VBox content = new VBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(30, 40, 30, 40));

        // Warning icon with animation
        StackPane iconContainer = new StackPane();
        ImageView warningIcon = new ImageView(new Image("https://img.icons8.com/fluency/96/warning-shield.png"));
        warningIcon.setFitWidth(80);
        warningIcon.setFitHeight(80);
        iconContainer.getChildren().add(warningIcon);

        // Add pulse animation to icon
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(warningIcon.scaleXProperty(), 1.0),
                new KeyValue(warningIcon.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(1000),
                new KeyValue(warningIcon.scaleXProperty(), 1.1, Interpolator.EASE_BOTH),
                new KeyValue(warningIcon.scaleYProperty(), 1.1, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.millis(2000),
                new KeyValue(warningIcon.scaleXProperty(), 1.0, Interpolator.EASE_BOTH),
                new KeyValue(warningIcon.scaleYProperty(), 1.0, Interpolator.EASE_BOTH)
            )
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        // Title
        Label titleLabel = new Label("Confirmer le Blocage");
        titleLabel.getStyleClass().add("dialog-title");

        // User info with avatar
        HBox userInfo = new HBox(15);
        userInfo.setAlignment(javafx.geometry.Pos.CENTER);

        // User avatar
        Circle avatar = new Circle(30);
        avatar.setFill(getAvatarColor(user));
        String initials = (user.getNom().charAt(0) + "" + user.getPrenom().charAt(0)).toUpperCase();
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane avatarContainer = new StackPane();
        avatarContainer.getChildren().addAll(avatar, initialsLabel);

        // User details
        VBox userDetails = new VBox(5);
        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.getStyleClass().add("dialog-user-name");
        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("dialog-user-email");
        userDetails.getChildren().addAll(nameLabel, emailLabel);

        userInfo.getChildren().addAll(avatarContainer, userDetails);

        // Message
        Label messageLabel = new Label("Êtes-vous sûr de vouloir bloquer cet utilisateur ?\n\nL'utilisateur ne pourra plus se connecter à son compte jusqu'à ce qu'il soit débloqué.");
        messageLabel.getStyleClass().add("dialog-message");
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        content.getChildren().addAll(iconContainer, titleLabel, userInfo, messageLabel);
        dialogPane.setContent(content);

        // Custom buttons
        ButtonType blockButtonType = new ButtonType("Bloquer l'Utilisateur", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(blockButtonType, cancelButtonType);

        // Style the buttons
        Button blockBtn = (Button) dialogPane.lookupButton(blockButtonType);
        Button cancelBtn = (Button) dialogPane.lookupButton(cancelButtonType);

        blockBtn.getStyleClass().addAll("dialog-confirm-btn", "block-confirm-btn");
        cancelBtn.getStyleClass().addAll("dialog-cancel-btn");

        // Add button animations
        addDialogButtonAnimation(blockBtn, "#f59e0b");
        addDialogButtonAnimation(cancelBtn, "#6b7280");

        // Add entrance animation
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        if (stage != null) {
            stage.setOnShowing(e -> {
                // Fade in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), dialogPane);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                // Scale animation
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), dialogPane);
                scaleIn.setFromX(0.8);
                scaleIn.setFromY(0.8);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);
                scaleIn.setInterpolator(Interpolator.EASE_OUT);

                ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn);
                entrance.play();
            });
        }

        Optional<ButtonType> result = dialog.showAndWait();
        pulse.stop(); // Stop the pulse animation

        if (result.isPresent() && result.get() == blockButtonType) {
            try {
                if (serviceUser.blockUser(user.getIdUtilisateur())) {
                    // Update user status in the list
                    user.setStatus(1);
                    // Refresh the user container to update display
                    setupUserContainer();
                    updateStats();
                    showSuccessDialog("Utilisateur Bloqué",
                                    "L'utilisateur " + user.getNom() + " " + user.getPrenom() + " a été bloqué avec succès.");
                } else {
                    showErrorDialog("Erreur", "Échec du blocage de l'utilisateur.");
                }
            } catch (SQLException e) {
                showErrorDialog("Erreur Base de Données",
                              "Erreur lors du blocage : " + e.getMessage());
            }
        }
    }

    private void showUnblockConfirmation(User user) {
        // Create custom professional confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("");

        // Remove default styling and add custom
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());
        dialogPane.getStyleClass().add("professional-confirmation-dialog");

        // Create custom content
        VBox content = new VBox(20);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(30, 40, 30, 40));

        // Success icon with animation
        StackPane iconContainer = new StackPane();
        ImageView successIcon = new ImageView(new Image("https://img.icons8.com/fluency/96/unlock.png"));
        successIcon.setFitWidth(80);
        successIcon.setFitHeight(80);
        iconContainer.getChildren().add(successIcon);

        // Add rotation animation to icon
        RotateTransition rotate = new RotateTransition(Duration.millis(2000), successIcon);
        rotate.setByAngle(360);
        rotate.setInterpolator(Interpolator.EASE_BOTH);
        rotate.setCycleCount(Timeline.INDEFINITE);
        rotate.play();

        // Title
        Label titleLabel = new Label("Confirmer le Déblocage");
        titleLabel.getStyleClass().add("dialog-title");

        // User info with avatar
        HBox userInfo = new HBox(15);
        userInfo.setAlignment(javafx.geometry.Pos.CENTER);

        // User avatar
        Circle avatar = new Circle(30);
        avatar.setFill(getAvatarColor(user));
        String initials = (user.getNom().charAt(0) + "" + user.getPrenom().charAt(0)).toUpperCase();
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane avatarContainer = new StackPane();
        avatarContainer.getChildren().addAll(avatar, initialsLabel);

        // User details
        VBox userDetails = new VBox(5);
        Label nameLabel = new Label(user.getNom() + " " + user.getPrenom());
        nameLabel.getStyleClass().add("dialog-user-name");
        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("dialog-user-email");
        userDetails.getChildren().addAll(nameLabel, emailLabel);

        userInfo.getChildren().addAll(avatarContainer, userDetails);

        // Message
        Label messageLabel = new Label("Êtes-vous sûr de vouloir débloquer cet utilisateur ?\n\nL'utilisateur pourra de nouveau se connecter à son compte normalement.");
        messageLabel.getStyleClass().add("dialog-message");
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        content.getChildren().addAll(iconContainer, titleLabel, userInfo, messageLabel);
        dialogPane.setContent(content);

        // Custom buttons
        ButtonType unblockButtonType = new ButtonType("Débloquer l'Utilisateur", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(unblockButtonType, cancelButtonType);

        // Style the buttons
        Button unblockBtn = (Button) dialogPane.lookupButton(unblockButtonType);
        Button cancelBtn = (Button) dialogPane.lookupButton(cancelButtonType);

        unblockBtn.getStyleClass().addAll("dialog-confirm-btn", "unblock-confirm-btn");
        cancelBtn.getStyleClass().addAll("dialog-cancel-btn");

        // Add button animations
        addDialogButtonAnimation(unblockBtn, "#10b981");
        addDialogButtonAnimation(cancelBtn, "#6b7280");

        // Add entrance animation
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        if (stage != null) {
            stage.setOnShowing(e -> {
                // Fade in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), dialogPane);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                // Scale animation
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), dialogPane);
                scaleIn.setFromX(0.8);
                scaleIn.setFromY(0.8);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);
                scaleIn.setInterpolator(Interpolator.EASE_OUT);

                ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn);
                entrance.play();
            });
        }

        Optional<ButtonType> result = dialog.showAndWait();
        rotate.stop(); // Stop the rotation animation

        if (result.isPresent() && result.get() == unblockButtonType) {
            try {
                if (serviceUser.unblockUser(user.getIdUtilisateur())) {
                    // Update user status in the list
                    user.setStatus(0);
                    // Refresh the user container to update display
                    setupUserContainer();
                    updateStats();
                    showSuccessDialog("Utilisateur Débloqué",
                                    "L'utilisateur " + user.getNom() + " " + user.getPrenom() + " a été débloqué avec succès.");
                } else {
                    showErrorDialog("Erreur", "Échec du déblocage de l'utilisateur.");
                }
            } catch (SQLException e) {
                showErrorDialog("Erreur Base de Données",
                              "Erreur lors du déblocage : " + e.getMessage());
            }
        }
    }

    @FXML
    public void addNewUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New User");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh list after adding
            loadUsers();
            updateStats();
            setupUserContainer(); // Refresh UI

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ouverture de la fenêtre d'ajout d'utilisateur: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getUserType(User user) {
        if (user instanceof Client) return "Client";
        if (user instanceof Admin) return "Admin";
        if (user instanceof Moderateur) return "Moderateur";
        if (user instanceof Guide) return "Guide";
        return "User";
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();

            dashboardController controller = loader.getController();
            if (controller != null) {
                controller.setCurrentUser(UserSession.getInstance().getCurrentUser());
            }

            Stage stage = (Stage) userContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PlaNova - Dashboard");

            // Allow full screen and resizing for better visibility
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setMinWidth(1100);
            stage.setMinHeight(700);

            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", "Échec de la navigation vers le tableau de bord: " + e.getMessage());
        }
    }

    // Simplified click feedback animation
    private void addClickFeedback(Button btn, String accentColor) {
        // Simple but elegant click animation
        Timeline clickEffect = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(btn.scaleXProperty(), 1.15),
                new KeyValue(btn.scaleYProperty(), 1.15),
                new KeyValue(btn.opacityProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(100),
                new KeyValue(btn.scaleXProperty(), 0.9, Interpolator.EASE_OUT),
                new KeyValue(btn.scaleYProperty(), 0.9, Interpolator.EASE_OUT),
                new KeyValue(btn.opacityProperty(), 0.8, Interpolator.EASE_OUT)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(btn.scaleXProperty(), 1.15, Interpolator.EASE_OUT),
                new KeyValue(btn.scaleYProperty(), 1.15, Interpolator.EASE_OUT),
                new KeyValue(btn.opacityProperty(), 1.0, Interpolator.EASE_OUT)
            )
        );

        // Add a gentle rotation
        RotateTransition clickRotate = new RotateTransition(Duration.millis(300), btn);
        clickRotate.setByAngle(15);
        clickRotate.setInterpolator(Interpolator.EASE_OUT);
        clickRotate.setAutoReverse(true);
        clickRotate.setCycleCount(2);

        // Play both animations
        ParallelTransition clickAnimation = new ParallelTransition(clickEffect, clickRotate);
        clickAnimation.play();
    }

    // Professional dialog button animations
    private void addDialogButtonAnimation(Button btn, String baseColor) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), btn);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);
            scaleUp.play();

            // Add glow effect
            btn.setStyle(btn.getStyle() + "-fx-effect: dropshadow(gaussian, " + baseColor + ", 10, 0.3, 0, 0);");
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), btn);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.setInterpolator(Interpolator.EASE_OUT);
            scaleDown.play();

            // Remove glow effect
            btn.setStyle(btn.getStyle().replaceAll("-fx-effect: dropshadow\\([^;]*\\);", ""));
        });

        btn.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(100), btn);
            press.setToX(0.95);
            press.setToY(0.95);
            press.play();
        });

        btn.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(100), btn);
            release.setToX(1.05);
            release.setToY(1.05);
            release.play();
        });
    }

    // Professional success dialog
    private void showSuccessDialog(String title, String message) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());
        dialogPane.getStyleClass().add("success-dialog");

        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(25, 35, 25, 35));

        // Success icon with animation
        ImageView successIcon = new ImageView(new Image("https://img.icons8.com/fluency/64/checkmark.png"));
        successIcon.setFitWidth(60);
        successIcon.setFitHeight(60);

        // Bounce animation for success icon
        ScaleTransition bounce = new ScaleTransition(Duration.millis(600), successIcon);
        bounce.setFromX(0.3);
        bounce.setFromY(0.3);
        bounce.setToX(1.0);
        bounce.setToY(1.0);
        bounce.setInterpolator(Interpolator.SPLINE(0.68, -0.55, 0.265, 1.55));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("success-dialog-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("success-dialog-message");
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        content.getChildren().addAll(successIcon, titleLabel, messageLabel);
        dialogPane.setContent(content);

        ButtonType okButtonType = new ButtonType("Parfait", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButtonType);

        Button okBtn = (Button) dialogPane.lookupButton(okButtonType);
        okBtn.getStyleClass().add("success-dialog-btn");
        addDialogButtonAnimation(okBtn, "#10b981");

        // Show with animation
        Platform.runLater(() -> {
            bounce.play();
            dialog.showAndWait();
        });
    }

    // Professional error dialog
    private void showErrorDialog(String title, String message) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());
        dialogPane.getStyleClass().add("error-dialog");

        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(25, 35, 25, 35));

        // Error icon with animation
        ImageView errorIcon = new ImageView(new Image("https://img.icons8.com/fluency/64/error.png"));
        errorIcon.setFitWidth(60);
        errorIcon.setFitHeight(60);

        // Shake animation for error icon
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), errorIcon);
        shake.setFromX(-10);
        shake.setToX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("error-dialog-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("error-dialog-message");
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        content.getChildren().addAll(errorIcon, titleLabel, messageLabel);
        dialogPane.setContent(content);

        ButtonType okButtonType = new ButtonType("Compris", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButtonType);

        Button okBtn = (Button) dialogPane.lookupButton(okButtonType);
        okBtn.getStyleClass().add("error-dialog-btn");
        addDialogButtonAnimation(okBtn, "#ef4444");

        // Show with animation
        Platform.runLater(() -> {
            shake.play();
            dialog.showAndWait();
        });
    }
}
