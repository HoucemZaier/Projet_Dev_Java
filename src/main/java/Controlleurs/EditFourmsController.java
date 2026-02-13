package Controlleurs;

import Modeles.Fourms;
import Services.FourmsService;
import Utils.DataSource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EditFourmsController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtNbParticipant;

    @FXML
    private TextField txtCommentaire;

    @FXML
    private ComboBox<String> comboPoste; // ComboBox pour sélectionner un post

    private Fourms currentForum;
    private FourmsService forumService = new FourmsService();

    // Connexion à la BD
    private Connection cnx = DataSource.getInstance().getConnection();

    @FXML
    public void initData(Fourms forum) {
        this.currentForum = forum;
        txtNom.setText(forum.getNom());
        txtNbParticipant.setText(String.valueOf(forum.getNbparticipant()));
        txtCommentaire.setText(forum.getCommentaire());

        loadPostes();

        if (forum.getIdposte() != 0) {
            String nomPost = getNomById(forum.getIdposte());
            if (nomPost != null) comboPoste.setValue(nomPost);
        }
    }

    private void loadPostes() {
        List<String> postes = getAllNoms();
        comboPoste.getItems().clear();
        comboPoste.getItems().addAll(postes);
    }

    @FXML
    private void annulerAction() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleModifier(ActionEvent event) {
        try {
            if (txtNom.getText().isEmpty() || txtNbParticipant.getText().isEmpty()
                    || txtCommentaire.getText().isEmpty() || comboPoste.getValue() == null) {
                showAlert("Champs incomplets", "Veuillez remplir tous les champs.");
                return;
            }

            currentForum.setNom(txtNom.getText());
            currentForum.setNbparticipant(Integer.parseInt(txtNbParticipant.getText()));
            currentForum.setCommentaire(txtCommentaire.getText());

            int idPostSelectionne = getIdByNom(comboPoste.getValue());
            currentForum.setIdposte(idPostSelectionne);

            forumService.modifier(currentForum);

            Stage stage = (Stage) txtNom.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le nombre de participants doit être un entier.");
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de la modification.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public List<String> getAllNoms() {
        List<String> noms = new ArrayList<>();
        try {
            String req = "SELECT nomPost FROM post";
            PreparedStatement ps = cnx.prepareStatement(req);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                noms.add(rs.getString("nomPost"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noms;
    }

    public int getIdByNom(String nom) {
        try {
            String req = "SELECT idPost FROM post WHERE nomPost = ?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("idPost");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getNomById(int id) {
        try {
            String req = "SELECT nomPost FROM post WHERE idPost = ?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nomPost");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
