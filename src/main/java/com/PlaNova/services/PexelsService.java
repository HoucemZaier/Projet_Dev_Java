package com.PlaNova.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.PlaNova.utils.EnvConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PexelsService {

    private static final String API_KEY = EnvConfig.get("PEXELS_API_KEY");
    private static final String SEARCH_URL = "https://api.pexels.com/v1/search";

    private final HttpClient httpClient;

    public PexelsService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<List<String>> searchImages(String query, int perPage) {
        String encodedQuery = query.replace(" ", "%20");
        String url = SEARCH_URL + "?query=" + encodedQuery
                + "&per_page=" + Math.min(perPage, 15)
                + "&orientation=landscape";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", API_KEY)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<String> urls = new ArrayList<>();
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        JsonArray photos = json.getAsJsonArray("photos");
                        for (int i = 0; i < photos.size(); i++) {
                            JsonObject src = photos.get(i).getAsJsonObject()
                                    .getAsJsonObject("src");
                            urls.add(src.get("medium").getAsString());
                        }
                    }
                    return urls;
                });
    }

    public CompletableFuture<String> findBestImage(String query) {
        return searchImages(query, 1)
                .thenApply(urls -> urls.isEmpty() ? null : urls.get(0));
    }
}
