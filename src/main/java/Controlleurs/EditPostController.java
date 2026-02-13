package Controlleurs;

import Modeles.Posts;
import Services.PostService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditPostController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtPrix;

    @FXML
    private TextField txtCat;

    private Posts currentPost;
    private PostService postService = new PostService();

    public void setPost(Posts post) {
        this.currentPost = post;

        txtNom.setText(post.getNomPost());
        txtDescription.setText(post.getDescription());
        txtPrix.setText(String.valueOf(post.getPrix()));
        txtCat.setText(post.getTypePost());
    }

    @FXML
    private void updatePost() {
        try {
            String nom = txtNom.getText();
            String description = txtDescription.getText();
            String prixText = txtPrix.getText();
            String type = txtCat.getText();

            if (nom.isEmpty() || description.isEmpty() || prixText.isEmpty() || type.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs manquants");
                alert.setHeaderText(null);
                alert.setContentText("❌ Veuillez remplir tous les champs !");
                alert.showAndWait();
                return;
            }

            double prix = Double.parseDouble(prixText);

            currentPost.setNomPost(nom);
            currentPost.setDescription(description);
            currentPost.setPrix(prix);
            currentPost.setTypePost(type);

            postService.modifier(currentPost);

            Stage stage = (Stage) txtNom.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Le prix doit être un nombre !");
            alert.showAndWait();
        }
    }
}
