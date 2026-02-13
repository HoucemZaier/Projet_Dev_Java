package Controlleurs;

import Modeles.Posts;
import Services.PostService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;

import java.sql.*;
import java.util.List;

public class PostController {

    @FXML private TextField txtNom;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtPrix;
    @FXML private TextField txtCat;
    @FXML private ComboBox<String> comboUtilisateur;
    @FXML private ListView<Posts> list;

    private PostService postService = new PostService();
    private final String DB_URL = "jdbc:mysql://localhost:3306/pidev";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    private int currentPostId;

    @FXML
    public void initialize() {
        if (comboUtilisateur != null) {
            loadUtilisateurs();
        }
        if (list != null) {
            afficherPosts();
        }
    }


    private void loadUtilisateurs() {
        try (Connection cnx = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nom FROM utilisateur")) {
            comboUtilisateur.getItems().clear();
            while (rs.next()) {
                comboUtilisateur.getItems().add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
        }
    }

    /*public void afficherPosts() {
        if (list == null) return;

        List<Posts> posts = postService.afficherTout();
        if (posts != null) {
            list.getItems().setAll(posts);
        }

        list.setCellFactory(lv -> new ListCell<Posts>() {
            @Override
            protected void updateItem(Posts post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(15);
                    container.setStyle("-fx-padding: 8; -fx-alignment: CENTER_LEFT; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");

                    String nomAuteur = getUserNameById(post.getId_utilisateur());

                    Label lbl = new Label(String.format("%s | %.2f DT | Cat: %s | Par: %s",
                            post.getNomPost(), post.getPrix(), post.getTypePost(), nomAuteur));
                    lbl.setPrefWidth(380);
                    lbl.setStyle("-fx-font-weight: bold;");

                    Button btnEdit = new Button("Modifier");
                    Button btnDelete = new Button("Supprimer");
                    Button btnView = new Button("Voir");

                    btnDelete.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                    btnView.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

                    // ACTION SUPPRIMER
                    btnDelete.setOnAction(event -> {
                        postService.supprimer(post.getIdPost());
                        afficherPosts();
                    });

                    // ACTION MODIFIER (Ouvrir Edit_Post.fxml)
                    btnEdit.setOnAction(event -> openEditWindow(post));

                    // ACTION VOIR
                    btnView.setOnAction(event -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Détails du Post");
                        alert.setHeaderText(post.getNomPost());
                        alert.setContentText(post.getDescription());
                        alert.showAndWait();
                    });

                    container.getChildren().addAll(lbl, btnView, btnEdit, btnDelete);
                    setGraphic(container);
                }
            }
        });
    }*/

    /*public void afficherPosts() {
        if (list == null) return;

        List<Posts> posts = postService.afficherTout();
        if (posts != null) {
            list.getItems().setAll(posts);
        }

        list.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        list.setCellFactory(lv -> new ListCell<Posts>() {
            @Override
            protected void updateItem(Posts post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox card = new VBox(10);
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-padding: 15;");

                    // --- HEADER ---
                    HBox header = new HBox();
                    Label title = new Label(post.getNomPost());
                    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    Label price = new Label(String.format("%.2f DT", post.getPrix()));
                    price.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 5 10; -fx-background-radius: 10; -fx-font-weight: bold;");
                    header.getChildren().addAll(title, spacer, price);

                    // --- BODY ---
                    Label category = new Label("📁 " + post.getTypePost());
                    category.setStyle("-fx-text-fill: #7f8c8d;");
                    Label author = new Label("👤 Posté par: " + getUserNameById(post.getId_utilisateur()));
                    author.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

                    // --- ACTIONS (Utilisation de Symboles Unicode pour éviter le crash FontAwesomeFX) ---
                    HBox actions = new HBox(10);
                    actions.setAlignment(Pos.CENTER_RIGHT);

                    // Utilisation de symboles Unicode robustes (👁, ✎, 🗑)
                    Button btnView = new Button("👁");
                    Button btnEdit = new Button("✎");
                    Button btnDelete = new Button("🗑");

                    String baseStyle = "-fx-font-size: 16px; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-min-width: 40; -fx-min-height: 40;";

                    btnView.setStyle(baseStyle + "-fx-background-color: #3498db;");
                    btnEdit.setStyle(baseStyle + "-fx-background-color: #f1c40f;");
                    btnDelete.setStyle(baseStyle + "-fx-background-color: #e74c3c;");

                    btnDelete.setOnAction(e -> { postService.supprimer(post.getIdPost()); afficherPosts(); });
                    btnEdit.setOnAction(e -> openEditWindow(post));
                    btnView.setOnAction(event -> showDetails(post));

                    actions.getChildren().addAll(btnView, btnEdit, btnDelete);
                    card.getChildren().addAll(header, category, author, actions);

                    setGraphic(card);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-background-color: transparent; -fx-padding: 10;");
                }
            }
        });
    }*/

    public void afficherPosts() {
        if (list == null) return;

        List<Posts> posts = postService.afficherTout();
        if (posts != null) {
            list.getItems().setAll(posts);
        }

        list.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        list.setCellFactory(lv -> new ListCell<Posts>() {
            @Override
            protected void updateItem(Posts post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox card = new VBox(10);
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-padding: 15;");

                    HBox header = new HBox();
                    Label title = new Label(post.getNomPost());
                    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    Label price = new Label(String.format("%.2f DT", post.getPrix()));
                    price.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 5 10; -fx-background-radius: 10; -fx-font-weight: bold;");
                    header.getChildren().addAll(title, spacer, price);

                    Label category = new Label("📁 " + post.getTypePost());
                    category.setStyle("-fx-text-fill: #7f8c8d;");
                    Label author = new Label("👤 Posté par: " + getUserNameById(post.getId_utilisateur()));
                    author.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

                    HBox actions = new HBox(12);
                    actions.setAlignment(Pos.CENTER_RIGHT);

                    FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                    iconView.setFill(javafx.scene.paint.Color.web("#3498db")); // Bleu
                    iconView.setSize("20px");

                    FontAwesomeIconView iconEdit = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                    iconEdit.setFill(javafx.scene.paint.Color.web("#f39c12")); // Orange/Jaune
                    iconEdit.setSize("20px");

                    FontAwesomeIconView iconDelete = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                    iconDelete.setFill(javafx.scene.paint.Color.web("#e74c3c")); // Rouge
                    iconDelete.setSize("20px");

                    Button btnView = new Button(); btnView.setGraphic(iconView);
                    Button btnEdit = new Button(); btnEdit.setGraphic(iconEdit);
                    Button btnDelete = new Button(); btnDelete.setGraphic(iconDelete);

                    String baseStyle = "-fx-cursor: hand; -fx-min-width: 42; -fx-min-height: 42; " +
                            "-fx-background-radius: 50; -fx-background-color: #f8f9fa; " +
                            "-fx-border-color: #eee; -fx-border-radius: 50; -fx-border-width: 1;";

                    btnView.setStyle(baseStyle);
                    btnEdit.setStyle(baseStyle);
                    btnDelete.setStyle(baseStyle);

                    btnView.setOnMouseEntered(e -> btnView.setStyle(baseStyle + "-fx-background-color: #e3f2fd;"));
                    btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(baseStyle + "-fx-background-color: #fff3e0;"));
                    btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(baseStyle + "-fx-background-color: #ffebee;"));

                    javafx.event.EventHandler<javafx.scene.input.MouseEvent> resetStyle = e -> {
                        ((Button)e.getSource()).setStyle(baseStyle);
                    };
                    btnView.setOnMouseExited(resetStyle);
                    btnEdit.setOnMouseExited(resetStyle);
                    btnDelete.setOnMouseExited(resetStyle);

                    btnDelete.setOnAction(e -> { postService.supprimer(post.getIdPost()); afficherPosts(); });
                    btnEdit.setOnAction(e -> openEditWindow(post));
                    btnView.setOnAction(event -> showDetails(post));

                    actions.getChildren().addAll(btnView, btnEdit, btnDelete);
                    card.getChildren().addAll(header, category, author, actions);

                    setGraphic(card);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-background-color: transparent; -fx-padding: 10;");
                }
            }
        });
    }

    private void showDetails(Posts post) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Consultation du Post");
        alert.setHeaderText("Détails de : " + post.getNomPost());

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();

        alert.setContentText(post.getDescription());

        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        alert.showAndWait();
    }


    private void openEditWindow(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interface/Gestion Fourms_Posts/Edit_Post.fxml"));
            Parent root = loader.load();

            PostController controller = loader.getController();
            controller.initData(post);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Post");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnHiding(e -> afficherPosts());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initData(Posts post) {
        this.currentPostId = post.getIdPost();
        if (txtNom != null) txtNom.setText(post.getNomPost());
        if (txtDescription != null) txtDescription.setText(post.getDescription());
        if (txtPrix != null) txtPrix.setText(String.valueOf(post.getPrix()));
        if (txtCat != null) txtCat.setText(post.getTypePost());
        if (comboUtilisateur != null) {
            comboUtilisateur.setValue(getUserNameById(post.getId_utilisateur()));
        }
    }

    @FXML
    private void updatePost() {
        try {
            if (isInputInvalid()) {
                showAlert("Champs incomplets", "Veuillez remplir tous les champs.");
                return;
            }

            Posts post = new Posts();
            post.setIdPost(currentPostId);
            post.setNomPost(txtNom.getText());
            post.setDescription(txtDescription.getText());
            post.setPrix(Double.parseDouble(txtPrix.getText()));
            post.setTypePost(txtCat.getText());
            post.setId_utilisateur(getUserIdByName(comboUtilisateur.getValue()));

            postService.modifier(post);

            txtNom.getScene().getWindow().hide();

        } catch (Exception e) {
            showAlert("Erreur", "Vérifiez le format du prix.");
        }
    }

    @FXML
    private void ajouterPost() {
        try {
            if (isInputInvalid()) {
                showAlert("Erreur", "Champs vides !");
                return;
            }
            Posts post = new Posts();
            post.setNomPost(txtNom.getText());
            post.setDescription(txtDescription.getText());
            post.setPrix(Double.parseDouble(txtPrix.getText()));
            post.setTypePost(txtCat.getText());
            post.setId_utilisateur(getUserIdByName(comboUtilisateur.getValue()));

            postService.ajouter(post);

            if (list != null) afficherPosts();
            else txtNom.getScene().getWindow().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void oncreatePost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interface/Gestion Fourms_Posts/Ajout_Post.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHiding(event -> afficherPosts());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getUserNameById(int id) {
        try (Connection cnx = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cnx.prepareStatement("SELECT nom FROM utilisateur WHERE id_utilisateur = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("nom");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Inconnu";
    }

    private int getUserIdByName(String name) {
        try (Connection cnx = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cnx.prepareStatement("SELECT id_utilisateur FROM utilisateur WHERE nom = ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_utilisateur");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private boolean isInputInvalid() {
        return txtNom == null || txtNom.getText().isEmpty() || txtPrix == null || txtPrix.getText().isEmpty() || comboUtilisateur == null || comboUtilisateur.getValue() == null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void annulerAction() {
        if (txtNom != null) txtNom.clear();
        if (txtDescription != null) txtDescription.clear();
        if (txtPrix != null) txtPrix.clear();
        if (txtCat != null) txtCat.clear();

        if (comboUtilisateur != null) {
            comboUtilisateur.getSelectionModel().clearSelection();
            comboUtilisateur.setValue(null);
        }

        this.currentPostId = 0;

        System.out.println("✅ Formulaire réinitialisé.");
    }
}