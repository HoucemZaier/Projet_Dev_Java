package com.PlaNova.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AiService {
    private static final String API_KEY = "AIzaSyD0nnZd_n-Qwl2kXCLgDcNGX3XXN-qaZFc";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-pro:generateContent?key="
            + API_KEY;

    private final HttpClient client;
    private final Gson gson;

    public AiService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public CompletableFuture<String> getTravelPlan(String userInput, String destinationData, String weatherInfo) {
        String prompt = "You are a professional travel assistant for the PlaNova app. " +
                "The user says: \"" + userInput + "\". " +
                "Here is the destination data we have in our database: " + destinationData + ". " +
                "Current Weather Context: " + weatherInfo + ". " +
                "1. Identify the best matching destination from the data. " +
                "2. Create an exciting itinerary for the exact duration the user requested (default to 3 days). " +
                "3. MANDATORY: Start your response by explicitly mentioning the current weather and temperature provided in the context. "
                +
                "4. EXPLAIN how the weather influenced your specific activity choices (e.g., 'Since it is currently sunny, I recommend...'). "
                +
                "5. Adjust activities specifically for this weather (indoor if rainy/windy, outdoor if sunny). " +
                "6. Format with clear headings (Markdown style). Keep under 600 words.";

        JsonObject body = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject part = new JsonObject();
        JsonArray partsArray = new JsonArray();

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        partsArray.add(textPart);

        part.add("parts", partsArray);
        contents.add(part);
        body.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                        return jsonResponse.getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();
                    } else {
                        return "Error: " + response.statusCode() + " " + response.body();
                    }
                });
    }

    public CompletableFuture<String> transcribeAudio(java.io.File audioFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] fileContent = java.nio.file.Files.readAllBytes(audioFile.toPath());
                String base64Audio = java.util.Base64.getEncoder().encodeToString(fileContent);

                JsonObject body = new JsonObject();
                JsonArray contents = new JsonArray();
                JsonObject part = new JsonObject();
                JsonArray partsArray = new JsonArray();

                JsonObject textPart = new JsonObject();
                textPart.addProperty("text", "Transcribe this audio clip. Only return the transcription.");
                partsArray.add(textPart);

                JsonObject audioPart = new JsonObject();
                JsonObject inlineData = new JsonObject();
                inlineData.addProperty("mime_type", "audio/wav");
                inlineData.addProperty("data", base64Audio);
                audioPart.add("inline_data", inlineData);
                partsArray.add(audioPart);

                part.add("parts", partsArray);
                contents.add(part);
                body.add("contents", contents);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GEMINI_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                    return jsonResponse.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                } else {
                    return "Error: " + response.statusCode();
                }
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        });
    }
}
