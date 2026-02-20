package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.Properties;
import java.util.ResourceBundle;

public class ListExcursionController implements Initializable {

    @FXML private VBox containerExcursions;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> statutFilter;

    private final ServiceExcursion service = new ServiceExcursion();
    private ObservableList<Excursion> list = FXCollections.observableArrayList();
    private ObservableList<Excursion> favoris = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statutFilter.getItems().addAll("Tous", "Ouverte", "Annulée", "Complète");
        statutFilter.setValue("Tous");

        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerExcursions());
        statutFilter.valueProperty().addListener((obs, oldVal, newVal) -> filtrerExcursions());

        loadData();
    }

    private void filtrerExcursions() {
        containerExcursions.getChildren().clear();
        String keyword = txtRecherche.getText() == null ? "" : txtRecherche.getText().toLowerCase().trim();
        String statut = statutFilter.getValue();

        for (Excursion e : list) {
            String titre = e.getTitre() == null ? "" : e.getTitre().toLowerCase();
            String destination = e.getNomDestination() == null ? "" : e.getNomDestination().toLowerCase();
            String statutExcursion = e.getStatut() == null ? "" : e.getStatut();

            boolean matchesKeyword = titre.contains(keyword) || destination.contains(keyword);
            boolean matchesStatut = "Tous".equalsIgnoreCase(statut) || statutExcursion.equalsIgnoreCase(statut);

            if (matchesKeyword && matchesStatut) {
                containerExcursions.getChildren().add(createCard(e));
            }
        }

        if (containerExcursions.getChildren().isEmpty()) {
            Label noResult = new Label("Aucune excursion trouvée.");
            noResult.setStyle("-fx-text-fill:#ef4444; -fx-font-style:italic;");
            containerExcursions.getChildren().add(noResult);
        }
    }

    private void loadData() {
        try {
            list = FXCollections.observableArrayList(service.recuperer());
            filtrerExcursions();
        } catch (SQLDataException e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private HBox createCard(Excursion e) {
        VBox info = new VBox(8);

        Label titre = new Label(e.getTitre());
        titre.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        Label details = new Label(
                "📍 " + e.getNomDestination() +
                        " | 📅 " + e.getDateDepart() + " → " + e.getDateRetour() +
                        " | 💰 " + e.getPrix() + " DT" +
                        " | 🪑 " + e.getNbPlaces() + " place(s)" +
                        " | " + e.getStatut()
        );
        details.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");

        info.getChildren().addAll(titre, details);

        FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.EYE);
        iconView.setFill(Color.web("#3498db")); iconView.setSize("18px");

        FontAwesomeIconView iconEdit = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        iconEdit.setFill(Color.web("#f39c12")); iconEdit.setSize("18px");

        FontAwesomeIconView iconDelete = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        iconDelete.setFill(Color.web("#e74c3c")); iconDelete.setSize("18px");

        FontAwesomeIconView iconShare = new FontAwesomeIconView(FontAwesomeIcon.SHARE);
        iconShare.setFill(Color.web("#0DA2E7")); iconShare.setSize("18px");

        FontAwesomeIconView iconFav = new FontAwesomeIconView(FontAwesomeIcon.STAR);
        iconFav.setFill(favoris.contains(e) ? Color.GOLD : Color.LIGHTGRAY); iconFav.setSize("18px");

        FontAwesomeIconView iconEmail = new FontAwesomeIconView(FontAwesomeIcon.ENVELOPE);
        iconEmail.setFill(Color.web("#10b981")); iconEmail.setSize("18px");

        Button btnView = new Button(); btnView.setGraphic(iconView);
        Button btnEdit = new Button(); btnEdit.setGraphic(iconEdit);
        Button btnDelete = new Button(); btnDelete.setGraphic(iconDelete);
        Button btnFav = new Button(); btnFav.setGraphic(iconFav);
        Button btnEmail = new Button(); btnEmail.setGraphic(iconEmail);

        String baseStyle = "-fx-cursor: hand; -fx-min-width: 38; -fx-min-height: 38; -fx-background-radius: 50; -fx-background-color: #f8f9fa; -fx-border-color: #eee; -fx-border-radius: 50; -fx-border-width: 1;";
        btnView.setStyle(baseStyle); btnEdit.setStyle(baseStyle); btnDelete.setStyle(baseStyle); btnEmail.setStyle(baseStyle);
        btnFav.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        btnView.setOnAction(ev -> {
            StringBuilder detailsStr = new StringBuilder();
            detailsStr.append("Titre : ").append(e.getTitre()).append("\n");
            detailsStr.append("Destination : ").append(e.getNomDestination()).append("\n");
            detailsStr.append("Dates : ").append(e.getDateDepart()).append(" → ").append(e.getDateRetour()).append("\n");
            detailsStr.append("Prix : ").append(e.getPrix()).append(" DT\n");
            detailsStr.append("Nombre de places : ").append(e.getNbPlaces()).append("\n");
            detailsStr.append("Statut : ").append(e.getStatut());
            showAlert("Détails de l'excursion", detailsStr.toString(), Alert.AlertType.INFORMATION);
        });

        btnEdit.setOnAction(ev -> ouvrirUpdate(e));
        btnDelete.setOnAction(ev -> supprimerExcursion(e));
        btnFav.setOnAction(ev -> {
            if (favoris.contains(e)) {
                favoris.remove(e);
                ((FontAwesomeIconView) btnFav.getGraphic()).setFill(Color.LIGHTGRAY);
            } else {
                favoris.add(e);
                ((FontAwesomeIconView) btnFav.getGraphic()).setFill(Color.GOLD);
            }
        });
        btnEmail.setOnAction(ev -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Envoyer Email");
            dialog.setHeaderText("Entrer l'adresse email du destinataire");
            dialog.showAndWait().ifPresent(email -> envoyerEmail(email, e));
        });

        btnView.setOnMouseEntered(e1 -> btnView.setStyle(baseStyle + "-fx-background-color: #e1f5fe;"));
        btnEdit.setOnMouseEntered(e1 -> btnEdit.setStyle(baseStyle + "-fx-background-color: #fff3e0;"));
        btnDelete.setOnMouseEntered(e1 -> btnDelete.setStyle(baseStyle + "-fx-background-color: #ffebee;"));
        btnView.setOnMouseExited(e1 -> btnView.setStyle(baseStyle));
        btnEdit.setOnMouseExited(e1 -> btnEdit.setStyle(baseStyle));
        btnDelete.setOnMouseExited(e1 -> btnDelete.setStyle(baseStyle));

        HBox actions = new HBox(12, btnView, btnEdit, btnDelete, btnEmail, btnFav);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox card = new HBox(40, info, actions);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("""
            -fx-background-color:white;
            -fx-padding:20;
            -fx-background-radius:12;
            -fx-border-radius:12;
            -fx-border-color:#e2e8f0;
            -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06),10,0,0,4);
        """);
        HBox.setHgrow(info, Priority.ALWAYS);

        return card;
    }

    private void supprimerExcursion(Excursion e) {
        String statut = e.getStatut() == null ? "" : e.getStatut().trim();
        if (statut.equalsIgnoreCase("Ouverte")) {
            showAlert("Erreur", "Impossible de supprimer une excursion ouverte !", Alert.AlertType.ERROR);
            return;
        }
        try {
            service.supprimer(e.getIdExcursion());
            loadData();
            showAlert("Succès", "Excursion supprimée.", Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void ouvrirUpdate(Excursion e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateExcursion.fxml"));
            Parent root = loader.load();
            UpdateExcursionController controller = loader.getController();
            controller.setExcursion(e);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHiding(event -> loadData());
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAjouterExcursion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/createExcursion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Excursion");
            stage.show();
            stage.setOnHiding(event -> loadData());
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFavoris() {
        if (favoris.isEmpty()) {
            showAlert("Favoris", "Aucun favori.", Alert.AlertType.INFORMATION);
            return;
        }
        containerExcursions.getChildren().clear();
        for (Excursion e : favoris) {
            containerExcursions.getChildren().add(createCard(e));
        }
    }

    @FXML
    private void handleNotifications() {
        showAlert("Notifications", "Utilisez l'icône cloche pour envoyer un email.", Alert.AlertType.INFORMATION);
    }

    private void envoyerEmail(String destinataire, Excursion e) {
        final String emailExpediteur = "saida.dridi18@gmail.com";
        final String motDePasse = "hmgx vjoj hsir pqgy"; // attention à sécuriser

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailExpediteur, motDePasse);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailExpediteur));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Nouvelle Excursion Disponible ✈️");
            message.setText(
                    "Titre : " + e.getTitre() +
                            "\nDestination : " + e.getNomDestination() +
                            "\nDates : " + e.getDateDepart() + " → " + e.getDateRetour() +
                            "\nPrix : " + e.getPrix() + " DT"
            );

            Transport.send(message);
            showAlert("Succès", "Email envoyé avec succès !", Alert.AlertType.INFORMATION);

        } catch (MessagingException ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible d'envoyer l'email.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}