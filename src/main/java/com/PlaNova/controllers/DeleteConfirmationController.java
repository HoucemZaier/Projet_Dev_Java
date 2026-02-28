package com.PlaNova.controllers;

import com.PlaNova.models.User;
import com.PlaNova.services.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.lang.Runnable;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;


public class DeleteConfirmationController implements Initializable {

    @FXML
    private Label messageLabel, userDetailsLabel;
    @FXML
    private Button cancelBtn, confirmDeleteBtn;

    private User userToDelete;
    private ServiceUser serviceUser = new ServiceUser();
    private Runnable onDeleteConfirmed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization if needed
    }

    public void setUser(User user) {
        this.userToDelete = user;
        updateUI();
    }

    public void setOnDeleteConfirmed(Runnable callback) {
        this.onDeleteConfirmed = callback;
    }

    private void updateUI() {
        if (userToDelete != null) {
            userDetailsLabel.setText("User: " + userToDelete.getNom() + " " + userToDelete.getPrenom() +
                                   "\nEmail: " + userToDelete.getEmail() +
                                   "\nType: " + getUserType(userToDelete));
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void confirmDelete(ActionEvent event) {
        try {
            serviceUser.supprimer(userToDelete.getIdUtilisateur());

            // Close the confirmation dialog
            Stage stage = (Stage) confirmDeleteBtn.getScene().getWindow();
            stage.close();

            // Execute callback if provided
            if (onDeleteConfirmed != null) {
                onDeleteConfirmed.run();
            }

        } catch (SQLException e) {
            // Handle error - you might want to show an alert here
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }

    private String getUserType(User user) {
        if (user instanceof com.PlaNova.models.Client) return "Client";
        if (user instanceof com.PlaNova.models.Admin) return "Admin";
        if (user instanceof com.PlaNova.models.Moderateur) return "Moderateur";
        if (user instanceof com.PlaNova.models.Guide) return "Guide";
        return "User";
    }
}
