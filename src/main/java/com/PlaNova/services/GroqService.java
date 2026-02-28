package com.PlaNova.services;

import com.PlaNova.utils.EnvConfig;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service Groq API — Génération automatique de descriptions d'hôtels
 * Utilise l'API compatible OpenAI de Groq avec le modèle LLaMA3
 */
public class GroqService {

    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant"; // Groq LLaMA3 8B — rapide et gratuit
    private static final String API_KEY = EnvConfig.get("GROQ_API_KEY");

    private final HttpClient httpClient;

    public GroqService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Génère une description professionnelle d'un hôtel basée sur son nom, sa ville
     * et ses étoiles
     * 
     * @param nomHotel      Le nom de l'hôtel
     * @param ville         La ville où se trouve l'hôtel
     * @param nombreEtoiles Le nombre d'étoiles (1-5)
     * @return Description générée par LLaMA3
     */
    public String genererDescriptionHotel(String nomHotel, String ville, int nombreEtoiles)
            throws IOException, InterruptedException {

        String userPrompt = "Tu es un expert en tourisme et hôtellerie de luxe. " +
                "Génère une description professionnelle et attrayante en français pour cet hôtel :\\n\\n" +
                "Nom: " + nomHotel + "\\n" +
                "Ville: " + ville + "\\n" +
                "Étoiles: " + nombreEtoiles + "\\n\\n" +
                "Consignes strictes:\\n" +
                "- Entre 80 et 120 mots exactement\\n" +
                "- Mentionne la ville " + ville + " et ses attraits touristiques\\n" +
                "- Adapte le ton au standing " + nombreEtoiles + " étoiles\\n" +
                "- Évoque le confort, les services et l'hospitalité\\n" +
                "- Ton chaleureux, professionnel et engageant\\n" +
                "- Réponds UNIQUEMENT avec la description, sans titre ni commentaire.";

        // Construction du JSON body (format OpenAI Chat Completions)
        String jsonBody = "{"
                + "\"model\": \"" + MODEL + "\","
                + "\"messages\": ["
                + "  {\"role\": \"system\", \"content\": \"Tu es un expert en hôtellerie. Tu rédiges uniquement des descriptions d'hôtels professionnelles en français.\"},"
                + "  {\"role\": \"user\", \"content\": \"" + userPrompt + "\"}"
                + "],"
                + "\"max_tokens\": 350,"
                + "\"temperature\": 0.75,"
                + "\"top_p\": 0.9,"
                + "\"stream\": false"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        if (status == 200) {
            return parseResponseContent(response.body());
        } else if (status == 401) {
            throw new IOException("❌ Clé API invalide (401). Vérifiez la clé Groq.");
        } else if (status == 429) {
            throw new IOException("⏳ Limite d'appels atteinte (429). Réessayez dans quelques secondes.");
        } else if (status == 400) {
            throw new IOException("❌ Requête invalide (400): " + response.body());
        } else {
            throw new IOException("❌ Erreur API Groq (HTTP " + status + "): " + response.body());
        }
    }

    /**
     * Parse la réponse JSON OpenAI pour extraire choices[0].message.content
     * Sans dépendance externe (parsing manuel du JSON)
     */
    private String parseResponseContent(String json) {
        // Cherche "content": "..." dans choices[0].message
        String[] markers = { "\"content\": \"", "\"content\":\"" };

        for (String marker : markers) {
            int start = json.indexOf(marker);
            if (start == -1)
                continue;

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
            if (!content.isEmpty())
                return content;
        }

        // Fallback : log pour debug
        System.err.println("⚠️ Réponse Groq brute: " + json.substring(0, Math.min(300, json.length())));
        return "Impossible de parser la réponse. Réessayez.";
    }

    /**
     * Vérifie si la clé API est bien configurée
     */
    public boolean isApiKeyConfigured() {
        return API_KEY != null
                && !API_KEY.trim().isEmpty()
                && !API_KEY.equals("gsk_VOTRE_CLE_API_GROQ_ICI")
                && API_KEY.startsWith("gsk_");
    }

    /**
     * Retourne le modèle utilisé (pour affichage dans l'UI)
     */
    public String getModelName() {
        return MODEL;
    }
}
