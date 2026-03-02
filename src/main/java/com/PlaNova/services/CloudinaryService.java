package com.PlaNova.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.UUID;

import com.PlaNova.utils.EnvConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Uploads images to Cloudinary using unsigned upload presets.
 * Uses Java's built-in HttpClient + existing Gson — no extra dependencies.
 *
 * FREE TIER SETUP (takes 2 minutes):
 * 1. Create a free account at https://cloudinary.com/users/register_free
 * 2. Go to Settings > Upload > Upload Presets
 * 3. Click "Add upload preset", set Signing Mode to "Unsigned", save it
 * 4. Copy your Cloud Name from the Dashboard
 * 5. Set the values in your .env file:
 *      CLOUDINARY_CLOUD_NAME   — your cloud name (e.g., "dxyz1abc")
 *      CLOUDINARY_UPLOAD_PRESET — your unsigned preset name (e.g., "planova_unsigned")
 */
public class CloudinaryService {

    private static final String CLOUD_NAME = EnvConfig.get("CLOUDINARY_CLOUD_NAME");

    private static final String UPLOAD_PRESET = EnvConfig.get("CLOUDINARY_UPLOAD_PRESET");

    private static final String UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    private final HttpClient httpClient;

    public CloudinaryService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Uploads an image file to Cloudinary and returns the HTTPS URL.
     *
     * @param file the local image file to upload
     * @return the secure Cloudinary URL (e.g., https://res.cloudinary.com/…/image.jpg)
     * @throws IOException          if the upload fails
     * @throws InterruptedException if the request is interrupted
     */
    public String uploadImage(File file) throws IOException, InterruptedException {
        if (CLOUD_NAME == null || CLOUD_NAME.isEmpty() || UPLOAD_PRESET == null || UPLOAD_PRESET.isEmpty()) {
            throw new IOException(
                    "Cloudinary is not configured. " +
                    "Edit CloudinaryService.java with your cloud name and upload preset.");
        }

        String boundary = "----PlaNovaBoundary" + UUID.randomUUID();

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) contentType = "application/octet-stream";

        // Build multipart/form-data body
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        writeField(body, boundary, "upload_preset", UPLOAD_PRESET);
        writeField(body, boundary, "folder", "planova");

        // File part
        body.write(("--" + boundary + "\r\n").getBytes());
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\""
                + file.getName() + "\"\r\n").getBytes());
        body.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes());
        body.write(fileBytes);
        body.write("\r\n".getBytes());

        // Closing boundary
        body.write(("--" + boundary + "--\r\n").getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPLOAD_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return json.get("secure_url").getAsString();
        } else {
            throw new IOException("Cloudinary upload failed (HTTP " + response.statusCode() + "): "
                    + response.body());
        }
    }

    /**
     * Checks whether Cloudinary credentials have been configured.
     */
    public boolean isConfigured() {
        return CLOUD_NAME != null && !CLOUD_NAME.isEmpty()
                && UPLOAD_PRESET != null && !UPLOAD_PRESET.isEmpty();
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private void writeField(ByteArrayOutputStream out, String boundary,
                            String name, String value) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        out.write((value + "\r\n").getBytes());
    }
}
