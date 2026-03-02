package com.PlaNova.services;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.PlaNova.utils.EnvConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AiService {
    private static final String API_KEY        = EnvConfig.get("GROQ_API_KEY");
    private static final String GROQ_CHAT_URL  = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_AUDIO_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
    private static final String TEXT_MODEL     = "llama-3.3-70b-versatile";
    private static final String VISION_MODEL   = "meta-llama/llama-4-scout-17b-16e-instruct";
    private static final String AUDIO_MODEL    = "whisper-large-v3";

    private final HttpClient client;
    private final Gson gson;

    public AiService() {
        this.client = HttpClient.newHttpClient();
        this.gson   = new Gson();
    }

    private String authHeader() {
        return "Bearer " + API_KEY;
    }

    /** Extracts the assistant message content from a Groq /chat/completions response. */
    private String extractText(String responseBody) {
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }

    public CompletableFuture<String> getTravelPlan(String userInput, String destinationData, String weatherInfo) {
        String prompt = "You are a professional travel assistant for the PlaNova app. " +
                "The user says: \"" + userInput + "\". " +
                "Here is the destination data we have in our database: " + destinationData + ". " +
                "Current Weather Context: " + weatherInfo + ". " +
                "1. Identify the best matching destination from the data. " +
                "2. Create an exciting itinerary for the exact duration the user requested (default to 3 days). " +
                "3. MANDATORY: Start your response by explicitly mentioning the current weather and temperature provided in the context. " +
                "4. EXPLAIN how the weather influenced your specific activity choices (e.g., 'Since it is currently sunny, I recommend...'). " +
                "5. Adjust activities specifically for this weather (indoor if rainy/windy, outdoor if sunny). " +
                "6. Format with clear headings (Markdown style). Keep under 600 words.";

        JsonObject body = new JsonObject();
        body.addProperty("model", TEXT_MODEL);
        JsonArray messages = new JsonArray();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);
        messages.add(userMsg);
        body.add("messages", messages);

        final String bodyJson = body.toString();
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GROQ_CHAT_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", authHeader())
                        .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) return extractText(response.body());
                return "Error: " + response.statusCode() + " " + response.body();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        });
    }

    public CompletableFuture<String> transcribeAudio(File audioFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] fileBytes = Files.readAllBytes(audioFile.toPath());
                String fileName  = audioFile.getName();
                String boundary  = UUID.randomUUID().toString().replace("-", "");

                byte[] partHeader = ("--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                        "Content-Type: audio/wav\r\n\r\n").getBytes();
                byte[] modelPart  = ("\r\n--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"model\"\r\n\r\n" +
                        AUDIO_MODEL + "\r\n").getBytes();
                byte[] footer     = ("--" + boundary + "--\r\n").getBytes();

                byte[] multipart = new byte[partHeader.length + fileBytes.length + modelPart.length + footer.length];
                int pos = 0;
                System.arraycopy(partHeader, 0, multipart, pos, partHeader.length); pos += partHeader.length;
                System.arraycopy(fileBytes,  0, multipart, pos, fileBytes.length);  pos += fileBytes.length;
                System.arraycopy(modelPart,  0, multipart, pos, modelPart.length);  pos += modelPart.length;
                System.arraycopy(footer,     0, multipart, pos, footer.length);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GROQ_AUDIO_URL))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .header("Authorization", authHeader())
                        .POST(HttpRequest.BodyPublishers.ofByteArray(multipart))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    return json.get("text").getAsString();
                }
                return "Error: " + response.statusCode() + " " + response.body();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        });
    }

    public CompletableFuture<String> identifyImage(File imageFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                String base64     = Base64.getEncoder().encodeToString(imageBytes);

                String fileName = imageFile.getName().toLowerCase();
                String mimeType = "image/jpeg";
                if (fileName.endsWith(".png"))  mimeType = "image/png";
                else if (fileName.endsWith(".gif"))  mimeType = "image/gif";
                else if (fileName.endsWith(".bmp"))  mimeType = "image/bmp";
                else if (fileName.endsWith(".webp")) mimeType = "image/webp";

                String prompt = "Identify the place, landmark, city, or travel destination shown in this image. " +
                        "Return ONLY a JSON object with two fields: " +
                        "\"name\" (the destination/landmark name) and \"country\" (the country it is in). " +
                        "Example: {\"name\":\"Eiffel Tower\",\"country\":\"France\"} " +
                        "If you cannot identify a specific place, try your best guess based on the landscape, architecture, or scenery. " +
                        "Return ONLY the JSON, no markdown, no explanation.";

                JsonObject body = new JsonObject();
                body.addProperty("model", VISION_MODEL);
                JsonArray messages = new JsonArray();
                JsonObject userMsg = new JsonObject();
                userMsg.addProperty("role", "user");

                JsonArray contentArray = new JsonArray();
                JsonObject textPart = new JsonObject();
                textPart.addProperty("type", "text");
                textPart.addProperty("text", prompt);
                contentArray.add(textPart);

                JsonObject imagePart = new JsonObject();
                imagePart.addProperty("type", "image_url");
                JsonObject imageUrl = new JsonObject();
                imageUrl.addProperty("url", "data:" + mimeType + ";base64," + base64);
                imagePart.add("image_url", imageUrl);
                contentArray.add(imagePart);

                userMsg.add("content", contentArray);
                messages.add(userMsg);
                body.add("messages", messages);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GROQ_CHAT_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", authHeader())
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String text = extractText(response.body()).trim();
                    if (text.startsWith("```")) {
                        text = text.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
                    }
                    return text;
                }
                return "{\"error\":\"API returned " + response.statusCode() + "\"}";
            } catch (Exception e) {
                return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
            }
        });
    }
}
