package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OSRMService {
    private static final String OSRM_BASE_URL = "https://router.project-osrm.org";
    private final HttpClient httpClient;
    private final Gson gson;

    public OSRMService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public ItineraryResponse calculateRoute(double startLat, double startLon, double endLat, double endLon, String transportMode) {
        try {
            String profile = getOSRMProfile(transportMode);
            String url = String.format("%s/route/v1/%s/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson&steps=true",
                    OSRM_BASE_URL, profile, startLon, startLat, endLon, endLat);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                throw new RuntimeException("OSRM API error: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to calculate route", e);
        }
    }

    private String getOSRMProfile(String transportMode) {
        if (transportMode == null) return "driving";
        
        String mode = transportMode.toLowerCase();
        if (mode.contains("taxi") || mode.contains("car") || mode.contains("voiture")) {
            return "driving";
        } else if (mode.contains("bus") || mode.contains("marche") || mode.contains("walk")) {
            return "foot";
        } else {
            return "driving";
        }
    }

    private ItineraryResponse parseResponse(String responseBody) {
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        JsonArray routes = json.getAsJsonArray("routes");
        
        if (routes.size() == 0) {
            throw new RuntimeException("No route found");
        }

        JsonObject route = routes.get(0).getAsJsonObject();
        double distance = route.get("distance").getAsDouble();
        double duration = route.get("duration").getAsDouble();
        
        JsonObject geometry = route.getAsJsonObject("geometry");
        JsonArray coordinates = geometry.getAsJsonArray("coordinates");

        ItineraryResponse response = new ItineraryResponse();
        response.setDistance(distance);
        response.setDuration(duration);
        response.setCoordinates(coordinates.toString());
        
        return response;
    }

    public static class ItineraryResponse {
        private double distance;
        private double duration;
        private String coordinates;

        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }
        
        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }
        
        public String getCoordinates() { return coordinates; }
        public void setCoordinates(String coordinates) { this.coordinates = coordinates; }
        
        public String getFormattedDistance() {
            if (distance < 1000) {
                return String.format("%.0f m", distance);
            } else {
                return String.format("%.2f km", distance / 1000);
            }
        }
        
        public String getFormattedDuration() {
            int minutes = (int) (duration / 60);
            if (minutes < 60) {
                return String.format("%d min", minutes);
            } else {
                int hours = minutes / 60;
                int remainingMinutes = minutes % 60;
                return String.format("%d h %d min", hours, remainingMinutes);
            }
        }
    }
}
