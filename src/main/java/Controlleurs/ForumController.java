package Controlleurs;

import Modeles.Fourms;
import Modeles.Posts;
import Services.FourmsService;
import Utils.DataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import java.io.IOException;
import java.sql.*;
import java.util.List;

public class ForumController {

    @FXML private TextField txtNom;
    @FXML private TextField txtNbParticipant;
    @FXML private TextField txtCommentaire;
    @FXML private ComboBox<String> comboPoste;
    @FXML private ListView<Fourms> list;
    @FXML private Button btnAnnuler;
    private int currentForumId;
    private final FourmsService fs = new FourmsService();

    private final String DB_URL = "jdbc:mysql://localhost:3306/pidev";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    @FXML
    private void handleOpenAddWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interface/Gestion Fourms_Posts/AjoutFourms.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Créer une Nouvelle Fourms");
            stage.setScene(new Scene(root));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors du chargement de la fenêtre d'ajout : " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    public void initialize() {
        if (comboPoste != null) {
            loadPostesNames();
        }

        if (list != null) {
            afficherForums();
        }
        if (btnAnnuler != null) {
            btnAnnuler.setOnAction(e -> {
                Stage stage = (Stage) btnAnnuler.getScene().getWindow();
                stage.close();
            });
        }
    }

    private void loadPostesNames() {
        ObservableList<String> options = FXCollections.observableArrayList();
        String query = "SELECT nomPost FROM post";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                options.add(rs.getString("nomPost"));
            }
            comboPoste.setItems(options);

        } catch (SQLException e) {
            System.err.println("Erreur de chargement des postes : " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            if (txtNom.getText().isEmpty() || comboPoste.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir le nom et choisir un poste.");
                return;
            }

            int idPosteSelectionne = getIdPosteParNom(comboPoste.getValue());

            Fourms nouveauForum = new Fourms();
            nouveauForum.setNom(txtNom.getText());
            nouveauForum.setNbparticipant(Integer.parseInt(txtNbParticipant.getText()));
            nouveauForum.setCommentaire(txtCommentaire.getText());
            nouveauForum.setIdposte(idPosteSelectionne);

            fs.ajouter(nouveauForum);

            showAlert(Alert.AlertType.ERROR, "Succès", "Le forum a été ajouté avec succès !");
            viderChamps();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Le nombre de participants doit être un chiffre.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    private int getIdPosteParNom(String nomPost) {
        String query = "SELECT idPost  FROM post WHERE nomPost = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nomPost);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idPost");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération ID poste : " + e.getMessage());
        }
        return -1;
    }

    private void viderChamps() {
        txtNom.clear();
        txtNbParticipant.clear();
        txtCommentaire.clear();
        comboPoste.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType error, String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void afficherForums() {
        if (list == null) return;

        List<Fourms> forums = fs.afficherTout();
        if (forums != null) {
            list.getItems().setAll(forums);
        }

        list.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        list.setCellFactory(lv -> new ListCell<Fourms>() {
            @Override
            protected void updateItem(Fourms forum, boolean empty) {
                super.updateItem(forum, empty);
                if (empty || forum == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox card = new VBox(10);
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-padding: 15;");

                    HBox header = new HBox();
                    header.setAlignment(Pos.CENTER_LEFT);

                    Label title = new Label(forum.getNom());
                    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label participants = new Label(forum.getNbparticipant() + " Participants");
                    participants.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-padding: 5 10; -fx-background-radius: 10; -fx-font-weight: bold;");

                    header.getChildren().addAll(title, spacer, participants);

                    Label commentaire = new Label(forum.getCommentaire());
                    commentaire.setWrapText(true);
                    commentaire.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

                    Label posteInfo = new Label("📍 Poste rattaché: " + getNomPosteById(forum.getIdposte()));
                    posteInfo.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

                    HBox actions = new HBox(12);
                    actions.setAlignment(Pos.CENTER_RIGHT);

                    FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                    iconView.setFill(javafx.scene.paint.Color.web("#3498db"));
                    iconView.setSize("18px");

                    FontAwesomeIconView iconEdit = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                    iconEdit.setFill(javafx.scene.paint.Color.web("#f39c12"));
                    iconEdit.setSize("18px");

                    FontAwesomeIconView iconDelete = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                    iconDelete.setFill(javafx.scene.paint.Color.web("#e74c3c"));
                    iconDelete.setSize("18px");

                    Button btnView = new Button(); btnView.setGraphic(iconView);
                    Button btnEdit = new Button(); btnEdit.setGraphic(iconEdit);
                    Button btnDelete = new Button(); btnDelete.setGraphic(iconDelete);

                    String baseStyle = "-fx-cursor: hand; -fx-min-width: 38; -fx-min-height: 38; " +
                            "-fx-background-radius: 50; -fx-background-color: #f8f9fa; " +
                            "-fx-border-color: #eee; -fx-border-radius: 50; -fx-border-width: 1;";

                    btnView.setStyle(baseStyle);
                    btnEdit.setStyle(baseStyle);
                    btnDelete.setStyle(baseStyle);

                    btnView.setOnMouseEntered(e -> btnView.setStyle(baseStyle + "-fx-background-color: #e1f5fe;"));
                    btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(baseStyle + "-fx-background-color: #fff3e0;"));
                    btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(baseStyle + "-fx-background-color: #ffebee;"));

                    btnView.setOnMouseExited(e -> btnView.setStyle(baseStyle));
                    btnEdit.setOnMouseExited(e -> btnEdit.setStyle(baseStyle));
                    btnDelete.setOnMouseExited(e -> btnDelete.setStyle(baseStyle));


                    btnView.setOnAction(e -> showDetails(forum));

                    btnEdit.setOnAction(e -> openEditWindow(forum));

                    btnDelete.setOnAction(e -> {
                        fs.supprimer(forum.getId_forum());
                        afficherForums();
                    });

                    actions.getChildren().addAll(btnView, btnEdit, btnDelete);
                    card.getChildren().addAll(header, commentaire, posteInfo, actions);

                    setGraphic(card);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-background-color: transparent; -fx-padding: 10;");
                }
            }
        });
    }
    private String getNomPosteById(int idposte) {
        String query = "SELECT nomPost FROM post WHERE idPost = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idposte);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("nomPost");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération nom poste : " + e.getMessage());
        }
        return "Poste inconnu";
    }

    private void showDetails(Fourms forum) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Consultation du Forum");
        alert.setHeaderText("Détails de : " + forum.getNom());

        alert.setContentText(
                "Nombre de participants : " + forum.getNbparticipant() +
                        "\n\nCommentaire :\n" + forum.getCommentaire()
        );

        alert.getDialogPane().setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;"
        );

        alert.showAndWait();
    }

    private void openEditWindow(Fourms forum) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/interface/Gestion Fourms_Posts/Edit_fourm.fxml")
            );

            Parent root = loader.load();

            EditFourmsController controller = loader.getController();
            controller.initData(forum);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Forum");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnHiding(e -> afficherForums());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void annulerAction() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}
