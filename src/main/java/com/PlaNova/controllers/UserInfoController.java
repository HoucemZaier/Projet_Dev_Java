package com.PlaNova.controllers;

import com.PlaNova.models.User;
import com.PlaNova.models.Client;
import com.PlaNova.models.Admin;
import com.PlaNova.models.Moderateur;
import com.PlaNova.models.Guide;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class UserInfoController implements Initializable {

    @FXML
    private ImageView userImageView;
    @FXML
    private Label nameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label countryLabel;
    @FXML
    private Label cinLabel;
    @FXML
    private Label matriculeLabel;

    private User user;

    public void setUser(User user) {
        this.user = user;
        loadUserInfo();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add fade-in animation when window opens
        if (userImageView != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(500));
            fade.setNode(userImageView);
            fade.setFromValue(0.3);
            fade.setToValue(1.0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(500));
            scale.setNode(userImageView);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.0);
            scale.setToY(1.0);

            fade.play();
            scale.play();
        }
    }

    private void loadUserInfo() {
        if (user != null) {
            // Set name
            nameLabel.setText(user.getNom() + " " + user.getPrenom());

            // Set email
            emailLabel.setText(user.getEmail());

            // Set country
            countryLabel.setText(user.getPays() != null ? user.getPays() : "N/A");

            // Set role and type-specific info
            String role = getUserType(user);
            roleLabel.setText(role);

            // Set CIN if client
            if (user instanceof Client) {
                Client client = (Client) user;
                cinLabel.setText(client.getCin() != null ? client.getCin() : "N/A");
                matriculeLabel.setText("N/A");
            }
            // Set Matricule if admin or moderateur
            else if (user instanceof Admin) {
                Admin admin = (Admin) user;
                matriculeLabel.setText(admin.getMatricule() != null ? admin.getMatricule() : "N/A");
                cinLabel.setText("N/A");
            }
            else if (user instanceof Moderateur) {
                Moderateur moderateur = (Moderateur) user;
                matriculeLabel.setText(moderateur.getMatricule() != null ? moderateur.getMatricule() : "N/A");
                cinLabel.setText("N/A");
            }
            // Other types
            else {
                cinLabel.setText("N/A");
                matriculeLabel.setText("N/A");
            }

            // Load user image if available
            if (user.getImageurl() != null && !user.getImageurl().isEmpty()) {
                try {
                    Image image = new Image(user.getImageurl());
                    userImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Failed to load user image: " + e.getMessage());
                }
            }
        }
    }

    private String getUserType(User user) {
        if (user instanceof Client) return "Client";
        if (user instanceof Admin) return "Admin";
        if (user instanceof Guide) return "Guide";
        if (user instanceof Moderateur) return "Moderateur";
        return "User";
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}

