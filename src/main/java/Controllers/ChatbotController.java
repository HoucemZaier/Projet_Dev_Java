package Controllers;

import Models.Activite;
import Models.Excursion;
import Services.GroqService;
import Services.ServiceActivite;
import Services.ServiceExcursion;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Chatbot IA Planova — Connaît toutes les activités et excursions en base
 * Propulsé par Groq LLaMA 3.1 8B
 */
public class ChatbotController implements Initializable {

    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatContainer;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Label statusLabel;
    @FXML private Button clearButton;

    private final GroqService groqService = new GroqService();
    private final ServiceExcursion serviceExcursion = new ServiceExcursion();
    private final ServiceActivite serviceActivite = new ServiceActivite();

    // Historique conversation pour mémoire du chatbot
    private final List<String[]> conversationHistory = new ArrayList<>();
    private String systemContext = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        buildSystemContext();
        showWelcomeMessage();

        // Envoyer avec Entrée
        messageField.setOnAction(e -> handleSend());

        // Auto-scroll vers le bas
        chatContainer.heightProperty().addListener((obs, old, newH) ->
                chatScrollPane.setVvalue(1.0));
    }

    /**
     * Construit le contexte système avec toutes les données de la DB
     */
    private void buildSystemContext() {
        StringBuilder ctx = new StringBuilder();
        ctx.append("Tu es PlanovaBot, l'assistant intelligent de la plateforme de tourisme PlaNova. ");
        ctx.append("Tu connais toutes les données en temps réel de la plateforme. ");
        ctx.append("Réponds toujours en français, de façon concise, utile et chaleureuse. ");
        ctx.append("Utilise des emojis pertinents pour rendre tes réponses plus agréables.\n\n");

        // Données des excursions
        ctx.append("=== EXCURSIONS DISPONIBLES ===\n");
        try {
            List<Excursion> excursions = serviceExcursion.recuperer();
            if (excursions.isEmpty()) {
                ctx.append("Aucune excursion disponible pour le moment.\n");
            } else {
                for (Excursion e : excursions) {
                    ctx.append("• [ID:").append(e.getIdExcursion()).append("] ")
                            .append(e.getTitre())
                            .append(" | Destination: ").append(e.getNomDestination())
                            .append(" | Du ").append(e.getDateDepart()).append(" au ").append(e.getDateRetour())
                            .append(" | Prix: ").append(e.getPrix()).append(" DT")
                            .append(" | Places: ").append(e.getNbPlaces())
                            .append(" | Statut: ").append(e.getStatut());
                    if (e.hasLocation()) {
                        ctx.append(" | GPS: (").append(e.getLatitude()).append(", ").append(e.getLongitude()).append(")");
                    }
                    ctx.append("\n");
                }
            }
        } catch (SQLDataException ex) {
            ctx.append("(Erreur chargement excursions)\n");
        }

        // Données des activités
        ctx.append("\n=== ACTIVITÉS DISPONIBLES ===\n");
        try {
            List<Activite> activites = serviceActivite.recuperer();
            if (activites.isEmpty()) {
                ctx.append("Aucune activité disponible pour le moment.\n");
            } else {
                for (Activite a : activites) {
                    ctx.append("• [ID:").append(a.getIdActivite()).append("] ")
                            .append(a.getNom())
                            .append(" | ").append(a.getDescription())
                            .append(" | Date: ").append(a.getDateActivite())
                            .append(" à ").append(a.getHeureActivite())
                            .append(" | Lieu: ").append(a.getLieu())
                            .append(" | Prix: ").append(a.getPrix()).append(" DT")
                            .append(" | Excursion ID: ").append(a.getIdExcursion())
                            .append("\n");
                }
            }
        } catch (SQLDataException ex) {
            ctx.append("(Erreur chargement activités)\n");
        }

        ctx.append("\nTu peux répondre à des questions sur les prix, les dates, les disponibilités, ");
        ctx.append("les recommandations, les destinations, les activités etc. ");
        ctx.append("Si on te demande quelque chose hors de ta connaissance touristique, redirige poliment.");

        systemContext = ctx.toString();
        statusLabel.setText("✅ Base de données chargée — PlanovaBot prêt");
    }

    private void showWelcomeMessage() {
        addBotMessage("Bonjour ! 👋 Je suis **PlanovaBot**, votre assistant intelligent.\n\n" +
                "Je connais toutes les excursions et activités disponibles sur PlaNova. Vous pouvez me demander :\n" +
                "• 🗺️ Les excursions disponibles et leurs détails\n" +
                "• 🎯 Les activités par destination ou prix\n" +
                "• 💡 Des recommandations personnalisées\n" +
                "• 📅 Les disponibilités et dates\n\n" +
                "Comment puis-je vous aider aujourd'hui ?");
    }

    @FXML
    private void handleSend() {
        String userMsg = messageField.getText().trim();
        if (userMsg.isEmpty()) return;

        messageField.clear();
        sendButton.setDisable(true);
        statusLabel.setText("⏳ PlanovaBot réfléchit...");

        // Afficher message utilisateur
        addUserMessage(userMsg);

        // Construire historique JSON
        StringBuilder historyJson = new StringBuilder();
        for (int i = 0; i < conversationHistory.size(); i++) {
            String[] msg = conversationHistory.get(i);
            if (i > 0) historyJson.append(",");
            historyJson.append("{\"role\":\"").append(msg[0]).append("\",\"content\":\"")
                    .append(msg[1].replace("\"", "\\\"").replace("\n", "\\n"))
                    .append("\"}");
        }

        // Appel API en thread séparé
        String finalUserMsg = userMsg;
        new Thread(() -> {
            try {
                String response = groqService.chat(systemContext, finalUserMsg, historyJson.toString());

                // Sauvegarder dans l'historique
                conversationHistory.add(new String[]{"user", finalUserMsg});
                conversationHistory.add(new String[]{"assistant", response});
                // Limiter à 10 échanges pour éviter dépassement de tokens
                while (conversationHistory.size() > 20) {
                    conversationHistory.remove(0);
                    conversationHistory.remove(0);
                }

                Platform.runLater(() -> {
                    addBotMessage(response);
                    sendButton.setDisable(false);
                    statusLabel.setText("✅ PlanovaBot prêt");
                    messageField.requestFocus();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    addBotMessage("❌ Erreur de connexion : " + ex.getMessage());
                    sendButton.setDisable(false);
                    statusLabel.setText("❌ Erreur");
                });
            }
        }).start();
    }

    private void addUserMessage(String text) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_RIGHT);
        container.setPadding(new Insets(5, 15, 5, 50));

        VBox bubble = new VBox(4);
        bubble.setStyle(
                "-fx-background-color: linear-gradient(to right, #10b981, #0DA2E7);" +
                        "-fx-background-radius: 18 18 4 18;" +
                        "-fx-padding: 10 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(13,162,231,0.3), 8, 0, 0, 3);"
        );
        bubble.setMaxWidth(420);

        Label msgLabel = new Label(text);
        msgLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-wrap-text: true;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(400);

        bubble.getChildren().add(msgLabel);
        container.getChildren().add(bubble);
        chatContainer.getChildren().add(container);
    }

    private void addBotMessage(String text) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(5, 50, 5, 15));

        // Avatar bot
        Circle avatar = new Circle(18);
        avatar.setFill(Color.web("#1e293b"));
        Label avatarLabel = new Label("🤖");
        avatarLabel.setStyle("-fx-font-size: 14px;");
        StackPane avatarPane = new StackPane(avatar, avatarLabel);
        avatarPane.setPrefSize(36, 36);

        VBox bubble = new VBox(4);
        bubble.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18 18 18 4;" +
                        "-fx-padding: 12 16;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 18 18 18 4;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);"
        );
        bubble.setMaxWidth(500);

        Label msgLabel = new Label(text);
        msgLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 13px; -fx-wrap-text: true;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(480);

        bubble.getChildren().add(msgLabel);
        container.getChildren().addAll(avatarPane, bubble);
        chatContainer.getChildren().add(container);
    }

    @FXML
    private void handleClear() {
        chatContainer.getChildren().clear();
        conversationHistory.clear();
        showWelcomeMessage();
        statusLabel.setText("✅ Conversation effacée — PlanovaBot prêt");
    }

    @FXML
    private void handleRefreshData() {
        statusLabel.setText("🔄 Rechargement des données...");
        buildSystemContext();
    }
}
