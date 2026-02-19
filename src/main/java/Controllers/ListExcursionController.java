package Controllers;

import Models.Excursion;
import Services.ServiceExcursion;
import jakarta.mail.*;
import jakarta.mail.internet.*;


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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

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
        // Ajouter "Tous" pour afficher toutes les excursions
        statutFilter.getItems().addAll("Tous", "Ouverte", "Annulée", "Complète");
        statutFilter.setValue("Tous"); // Valeur par défaut

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
            String destination = e.getDestination() == null ? "" : e.getDestination().toLowerCase();
            String statutExcursion = e.getStatut() == null ? "" : e.getStatut();

            boolean matchesKeyword = titre.contains(keyword) || destination.contains(keyword);

            // Si "Tous" est sélectionné, matchesStatut est toujours vrai
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
        VBox info = new VBox(6);
        Label titre = new Label(e.getTitre());
        titre.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        Label details = new Label(
                "📍 " + e.getDestination() +
                        " | 📅 " + e.getDateDepart() + " → " + e.getDateRetour() +
                        " | 💰 " + e.getPrix() + " DT" +
                        " | 🪑 " + e.getNbPlaces() +
                        " | " + e.getStatut()
        );

        info.getChildren().addAll(titre, details);

        Button btnView = createIconButton(FontAwesomeIcon.EYE, "#3498db");
        Button btnEdit = createIconButton(FontAwesomeIcon.PENCIL, "#f39c12");
        Button btnDelete = createIconButton(FontAwesomeIcon.TRASH, "#e74c3c");
        Button btnShare = createIconButton(FontAwesomeIcon.SHARE, "#0DA2E7");
        Button btnFav = createIconButton(FontAwesomeIcon.STAR, favoris.contains(e) ? "#f59e0b" : "#cbd5e1");
        Button btnNotify = createIconButton(FontAwesomeIcon.BELL, "#10b981");

        // Actions
        btnView.setOnAction(ev -> showAlert("Détails",
                "Titre: " + e.getTitre() +
                        "\nDestination: " + e.getDestination() +
                        "\nDates: " + e.getDateDepart() + " → " + e.getDateRetour() +
                        "\nPrix: " + e.getPrix() + " DT",
                Alert.AlertType.INFORMATION));

        btnEdit.setOnAction(ev -> ouvrirUpdate(e));
        btnDelete.setOnAction(ev -> supprimerExcursion(e));

        btnShare.setOnAction(ev -> {
            String contenu = "Excursion : " + e.getTitre() +
                    "\nDestination : " + e.getDestination() +
                    "\nDates : " + e.getDateDepart() + " → " + e.getDateRetour() +
                    "\nPrix : " + e.getPrix() + " DT";
            ClipboardContent content = new ClipboardContent();
            content.putString(contenu);
            Clipboard.getSystemClipboard().setContent(content);
            showAlert("Succès", "Informations copiées.\nCollez-les dans WhatsApp ou Email.", Alert.AlertType.INFORMATION);
        });

        btnFav.setOnAction(ev -> {
            if (favoris.contains(e)) {
                favoris.remove(e);
                ((FontAwesomeIconView) btnFav.getGraphic()).setFill(Color.web("#cbd5e1"));
            } else {
                favoris.add(e);
                ((FontAwesomeIconView) btnFav.getGraphic()).setFill(Color.web("#f59e0b"));
            }
        });

        btnNotify.setOnAction(ev -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Envoyer Email");
            dialog.setHeaderText("Entrer l'email du destinataire");
            dialog.showAndWait().ifPresent(email -> envoyerEmail(email, e));
        });

        HBox actions = new HBox(8, btnView, btnEdit, btnDelete, btnShare, btnFav, btnNotify);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox card = new HBox(40, info, actions);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-padding:15; -fx-border-color:#ddd;");
        HBox.setHgrow(info, Priority.ALWAYS);

        return card;
    }

    private Button createIconButton(FontAwesomeIcon icon, String color) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setFill(Color.web(color));
        iconView.setSize("18px");

        Button btn = new Button();
        btn.setGraphic(iconView);
        btn.setStyle("-fx-background-color:#f8f9fa; -fx-cursor:hand;");
        return btn;
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
        ouvrirUpdate(new Excursion());
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
        final String motDePasse = "hmgx vjoj hsir pqgy";

        // Configuration SMTP pour Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Création de la session avec authentification
        jakarta.mail.Session session = jakarta.mail.Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    @Override
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(emailExpediteur, motDePasse);
                    }
                });

        try {
            // Création du message
            jakarta.mail.Message message = new jakarta.mail.internet.MimeMessage(session);
            message.setFrom(new jakarta.mail.internet.InternetAddress(emailExpediteur));
            message.setRecipients(jakarta.mail.Message.RecipientType.TO,
                    jakarta.mail.internet.InternetAddress.parse(destinataire));
            message.setSubject("Nouvelle Excursion Disponible ✈️");
            message.setText(
                    "Titre : " + e.getTitre() +
                            "\nDestination : " + e.getDestination() +
                            "\nDates : " + e.getDateDepart() + " → " + e.getDateRetour() +
                            "\nPrix : " + e.getPrix() + " DT"
            );

            // Envoi de l'email
            jakarta.mail.Transport.send(message);

            showAlert("Succès", "Email envoyé avec succès !", Alert.AlertType.INFORMATION);

        } catch (jakarta.mail.MessagingException ex) {
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
