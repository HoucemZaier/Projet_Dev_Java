package com.PlaNova.services;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class WeatherService {
    private static final String API_KEY = "API_KEY";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?units=metric&appid="
            + API_KEY + "&q=";

    private final HttpClient client;
    private final Gson gson;

    public WeatherService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    /**
     * 
     * 
     * @param city 
     * @return 
     */
    public CompletableFuture<String> getWeather(String city) {
        if (city == null || city.trim().isEmpty()) {
            return CompletableFuture.completedFuture("No weather data available.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + city.replace(" ", "%20")))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                        double temp = jsonResponse.getAsJsonObject("main").get("temp").getAsDouble();
                        String description = jsonResponse.getAsJsonArray("weather").get(0).getAsJsonObject()
                                .get("description").getAsString();
                        return String.format("Current weather in %s: %.1f°C, %s.", city, temp, description);
                    } else {
                        return "Weather data unavailable for " + city;
                    }
                }).exceptionally(ex -> "Weather service error: " + ex.getMessage());
    }
}
