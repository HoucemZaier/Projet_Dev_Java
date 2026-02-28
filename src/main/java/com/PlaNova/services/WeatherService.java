package com.PlaNova.services;

import com.PlaNova.utils.EnvConfig;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class WeatherService {
    private static final String API_KEY = EnvConfig.get("WEATHER_API_KEY");
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

        String actualCity = extractCityName(city);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + actualCity.replace(" ", "%20")))
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

    private String extractCityName(String destinationName) {
        String lower = destinationName.toLowerCase();
        if (lower.contains("chania"))
            return "Chania";
        if (lower.contains("venise") || lower.contains("venice"))
            return "Venice";
        if (lower.contains("gizeh") || lower.contains("giza"))
            return "Giza";
        if (lower.contains("feroe") || lower.contains("faroe"))
            return "Torshavn";
        if (lower.contains("crete") || lower.contains("heraklion"))
            return "Heraklion";
        if (lower.contains("rethymno"))
            return "Rethymno";
        if (lower.contains("phuket"))
            return "Phuket";
        if (lower.contains("mont blanc") || lower.contains("chamonix"))
            return "Chamonix";
        if (lower.contains("tunis") || lower.contains("ariana"))
            return "Ariana";
        return destinationName.split(" ")[0];
    }
}
