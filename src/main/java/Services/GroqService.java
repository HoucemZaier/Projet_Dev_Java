package Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service Groq AI — Chatbot personnalisé Planova
 * Utilisé pour le chatbot activités/excursions
 */
public class GroqService {

    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL    = "llama-3.1-8b-instant";

    private final HttpClient httpClient;

    public GroqService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Envoie un message au chatbot Planova
     * @param systemContext  Contexte système (données DB injectées)
     * @param userMessage    Message de l'utilisateur
     * @param conversationHistory JSON des messages précédents (pour mémoire)
     * @return Réponse du modèle
     */
    public String chat(String systemContext, String userMessage, String conversationHistory)
            throws IOException, InterruptedException {

        String messagesJson = "[" +
                "{\"role\":\"system\",\"content\":" + jsonEscape(systemContext) + "}," +
                conversationHistory +
                (conversationHistory.isEmpty() ? "" : ",") +
                "{\"role\":\"user\",\"content\":" + jsonEscape(userMessage) + "}" +
                "]";

        String jsonBody = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":" + messagesJson + ","
                + "\"max_tokens\":800,"
                + "\"temperature\":0.6,"
                + "\"stream\":false"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseContent(response.body());
        } else if (response.statusCode() == 429) {
            return "⏳ Limite d'appels atteinte. Réessayez dans quelques secondes.";
        } else if (response.statusCode() == 401) {
            return "❌ Clé API invalide. Vérifiez la configuration.";
        } else {
            return "❌ Erreur API (" + response.statusCode() + "). Réessayez.";
        }
    }

    /** Surcharge simple sans historique */
    public String chat(String systemContext, String userMessage)
            throws IOException, InterruptedException {
        return chat(systemContext, userMessage, "");
    }

    /** Escape JSON pour éviter les injections */
    private String jsonEscape(String s) {
        if (s == null) return "\"\"";
        s = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + s + "\"";
    }

    /** Parse choices[0].message.content depuis la réponse JSON */
    private String parseContent(String json) {
        String[] markers = {"\"content\": \"", "\"content\":\""};
        for (String marker : markers) {
            int start = json.indexOf(marker);
            if (start == -1) continue;
            start += marker.length();
            StringBuilder result = new StringBuilder();
            boolean escaped = false;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaped) {
                    switch (c) {
                        case 'n' -> result.append('\n');
                        case 't' -> result.append('\t');
                        case '"' -> result.append('"');
                        case '\\' -> result.append('\\');
                        default -> result.append(c);
                    }
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                } else {
                    result.append(c);
                }
            }
            String content = result.toString().trim();
            if (!content.isEmpty()) return content;
        }
        return "Je n'ai pas pu générer une réponse. Réessayez.";
    }
}
