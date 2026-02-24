package Services;

import Models.*;
import java.sql.SQLException;
import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

/**
 * Social media authentication service using ngrok tunnel with automatic detection
 * Singleton implementation to prevent port binding conflicts
 */
public class SocialAuthService {

    // Singleton instance management
    private static SocialAuthService instance;
    private static NgrokOAuthCallbackServer sharedServer;
    private static boolean serverInitialized = false;
    private static int configuredPort = 8080; // Default port, can be changed

    private final ServiceUser serviceUser;
    private final HttpClient httpClient;
    private String ngrokUrl = null;

    // Facebook credentials
    private static final String FACEBOOK_APP_ID = "925022893757906";
    private static final String FACEBOOK_APP_SECRET = "e93e7c5a45f1c523b353a70bdaf5b5b9";

    // Google OAuth credentials - FULLY CONFIGURED ✅
    // Client ID and Secret from Google Cloud Console
    private static final String GOOGLE_CLIENT_ID = "342453768411-1go1q6gkb5v15tjls2hgsofq64uhaajh.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "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";
    private static final String GOOGLE_SCOPE = "openid email profile";

    /**
     * Get singleton instance with default port 8080
     */
    public static synchronized SocialAuthService getInstance() {
        return getInstance(8080);
    }

    /**
     * Get singleton instance with custom port - useful for avoiding conflicts
     */
    public static synchronized SocialAuthService getInstance(int port) {
        if (instance == null || configuredPort != port) {
            if (instance != null && configuredPort != port) {
                // Port changed, shutdown old server and create new one
                shutdown();
            }
            configuredPort = port;
            instance = new SocialAuthService(port);
        }
        return instance;
    }

    private SocialAuthService(int port) {
        this.serviceUser = new ServiceUser();
        this.httpClient = HttpClient.newHttpClient();

        // Initialize server only once per port
        synchronized (SocialAuthService.class) {
            if (!serverInitialized || configuredPort != port) {
                try {
                    if (sharedServer != null) {
                        sharedServer.stop(); // Stop old server if exists
                    }

                    sharedServer = new NgrokOAuthCallbackServer(port);
                    sharedServer.start();

                    System.out.println("🚀 OAUTH SERVER STARTED ON PORT " + port + ":");
                    System.out.println("✅ Local server: http://127.0.0.1:" + port);
                    System.out.println("🔗 Start ngrok: ngrok http " + port + " --scheme=https");
                    System.out.println("📋 CRITICAL: Update redirect URIs in OAuth providers:");
                    System.out.println("   📘 Facebook: https://YOUR_NGROK_URL.ngrok.io/auth/facebook/callback");
                    System.out.println("   📕 Google: https://YOUR_NGROK_URL.ngrok.io/auth/google/callback");

                    serverInitialized = true;
                    configuredPort = port;
                    this.ngrokUrl = "NGROK_READY";

                } catch (java.net.BindException e) {
                    System.err.println("❌ PORT " + port + " CONFLICT DETECTED!");
                    System.err.println("💡 SOLUTIONS:");
                    System.err.println("   1. Use different port: SocialAuthService.getInstance(8081)");
                    System.err.println("   2. Close application using port " + port);
                    System.err.println("   3. Run: netstat -ano | findstr :" + port);
                    System.err.println("   4. Kill the process or restart computer");
                    sharedServer = null;
                    this.ngrokUrl = null;
                    serverInitialized = false;
                } catch (Exception e) {
                    System.err.println("❌ Error starting OAuth server on port " + port + ": " + e.getMessage());
                    sharedServer = null;
                    this.ngrokUrl = null;
                    serverInitialized = false;
                }
            } else {
                // Server already initialized on same port, just reference it
                this.ngrokUrl = "NGROK_READY";
                System.out.println("🔄 Using existing OAuth server on port " + configuredPort);
            }
        }
    }

    /**
     * Get current configured port
     */
    public static int getConfiguredPort() {
        return configuredPort;
    }


    /**
     * Automatically detect ngrok tunnel URL by querying the ngrok API
     */
    private String detectNgrokUrl() {
        try {
            // ngrok exposes its API on localhost:4040 by default
            String ngrokApiUrl = "http://127.0.0.1:4040/api/tunnels";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ngrokApiUrl))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // Parse the JSON response to find HTTPS tunnel
                String httpsUrl = extractHttpsUrlFromNgrokResponse(responseBody);

                if (httpsUrl != null && httpsUrl.contains("ngrok")) {
                    return httpsUrl;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not detect ngrok automatically: " + e.getMessage());
        }

        return null;
    }

    /**
     * Extract HTTPS URL from ngrok API response
     */
    private String extractHttpsUrlFromNgrokResponse(String json) {
        try {
            System.out.println("🔍 Parsing ngrok API response for HTTPS tunnels...");
            // Look for HTTPS tunnel in the JSON response
            String[] tunnels = json.split("\"public_url\":");

            for (String tunnel : tunnels) {
                if (tunnel.contains("https://") && (tunnel.contains("ngrok") || tunnel.contains("ngrok-free.dev"))) {
                    int start = tunnel.indexOf("\"https://");
                    if (start != -1) {
                        start += 1; // Skip the opening quote
                        int end = tunnel.indexOf("\"", start);
                        if (end != -1) {
                            String url = tunnel.substring(start, end);
                            System.out.println("🌐 Found HTTPS tunnel: " + url);

                            // Check if this tunnel points to our configured port
                            if (tunnel.contains(":" + configuredPort) || tunnel.contains("localhost:" + configuredPort)) {
                                System.out.println("✅ Verified tunnel for port " + configuredPort + ": " + url);
                                return url;
                            } else {
                                System.out.println("⚠️ Tunnel found but port not explicitly mentioned, checking if it's the right one...");
                                // For ngrok-free.dev domains, port might not be shown in the tunnel info
                                // Let's assume it's correct if it's the only HTTPS tunnel
                                if (url.contains("ngrok-free.dev") || url.contains("ngrok.io")) {
                                    System.out.println("✅ Using ngrok tunnel (assuming correct port): " + url);
                                    return url;
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("❌ No suitable HTTPS tunnel found in ngrok response");
            System.out.println("📄 Raw ngrok response: " + json.substring(0, Math.min(500, json.length())) + "...");
        } catch (Exception e) {
            System.err.println("❌ Error parsing ngrok response: " + e.getMessage());
        }

        return null;
    }

    public boolean isAvailable() {
        return sharedServer != null && ngrokUrl != null;
    }

    public String getServerInfo() {
        if (isAvailable()) {
            return "OAuth server running on port 8080 - ngrok ready";
        }
        return "OAuth server not available - port 8080 conflict";
    }

    /**
     * Get the redirect URI for OAuth - will be set when ngrok is detected
     */
    public String getRedirectUri() {
        // Try to detect current ngrok URL
        String currentNgrokUrl = detectCurrentNgrokUrl();
        if (currentNgrokUrl != null) {
            return currentNgrokUrl + "/auth/facebook/callback";
        }
        return "https://YOUR_NGROK_URL.ngrok.io/auth/facebook/callback";
    }

    /**
     * Try to detect ngrok URL dynamically when starting OAuth
     */
    private String detectCurrentNgrokUrl() {
        try {
            String ngrokApiUrl = "http://127.0.0.1:4040/api/tunnels";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ngrokApiUrl))
                    .timeout(java.time.Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                return extractHttpsUrlFromNgrokResponse(responseBody);
            }
        } catch (Exception e) {
            // Silently fail - ngrok might not be running yet
        }
        return null;
    }

    public void openFacebookAuth() throws Exception {
        if (!isAvailable()) {
            throw new Exception("Le serveur OAuth n'est pas disponible. Port 8080 pourrait être occupé.");
        }

        // Try to detect ngrok URL dynamically
        String currentNgrokUrl = detectCurrentNgrokUrl();
        if (currentNgrokUrl == null) {
            throw new Exception("ngrok n'est pas détecté. Assurez-vous de démarrer ngrok avec: ngrok http 8080 --scheme=https");
        }

        // Update the ngrok URL
        this.ngrokUrl = currentNgrokUrl;

        String redirectUri = ngrokUrl + "/auth/facebook/callback";
        String authUrl = String.format(
            "https://www.facebook.com/v18.0/dialog/oauth?client_id=%s&redirect_uri=%s&scope=public_profile,email&response_type=code&state=facebook_auth",
            FACEBOOK_APP_ID,
            java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8)
        );

        System.out.println("🌐 ngrok URL detected: " + ngrokUrl);
        System.out.println("🌐 Opening Facebook OAuth URL: " + authUrl);
        System.out.println("📍 Redirect URI: " + redirectUri);

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
            System.out.println("✅ Facebook OAuth opened - waiting for callback...");
        } else {
            throw new Exception("Impossible d'ouvrir le navigateur pour l'authentification");
        }
    }

    public void openGoogleAuth() throws Exception {
        if (!isAvailable()) {
            throw new Exception("Le serveur OAuth n'est pas disponible. Port " + configuredPort + " pourrait être occupé.");
        }

        // Try to detect ngrok URL dynamically
        String currentNgrokUrl = detectCurrentNgrokUrl();
        if (currentNgrokUrl == null) {
            throw new Exception("❌ ngrok n'est pas détecté!\n\n" +
                               "🔧 ÉTAPES REQUISES:\n" +
                               "1. Démarrez ngrok: ngrok http " + configuredPort + " --scheme=https\n" +
                               "2. Copiez l'URL HTTPS (ex: https://abc123.ngrok.io)\n" +
                               "3. Ajoutez à Google Cloud Console:\n" +
                               "   https://VOTRE_URL_NGROK.ngrok.io/auth/google/callback\n" +
                               "4. Réessayez l'authentification");
        }

        // Update the ngrok URL
        this.ngrokUrl = currentNgrokUrl;

        String redirectUri = ngrokUrl + "/auth/google/callback";
        String authUrl = String.format(
            "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&scope=%s&response_type=code&state=google_auth&access_type=offline",
            GOOGLE_CLIENT_ID,
            java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8),
            java.net.URLEncoder.encode(GOOGLE_SCOPE, java.nio.charset.StandardCharsets.UTF_8)
        );

        System.out.println("🌐 ngrok URL détecté: " + ngrokUrl);
        System.out.println("🔗 Port configuré: " + configuredPort);
        System.out.println("📍 URI de redirection: " + redirectUri);
        System.out.println("🌐 Opening Google OAuth URL: " + authUrl);
        System.out.println("");
        System.out.println("🚨 IMPORTANT: Vérifiez que cette URI est dans Google Cloud Console:");
        System.out.println("   " + redirectUri);

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
            System.out.println("✅ Google OAuth ouvert - en attente du callback...");
        } else {
            throw new Exception("Impossible d'ouvrir le navigateur pour l'authentification");
        }
    }

    public java.util.concurrent.CompletableFuture<NgrokOAuthCallbackServer.OAuthResult> startOAuth(String provider) {
        if (!isAvailable()) {
            java.util.concurrent.CompletableFuture<NgrokOAuthCallbackServer.OAuthResult> future = new java.util.concurrent.CompletableFuture<>();
            future.complete(new NgrokOAuthCallbackServer.OAuthResult(false, provider, "ngrok_not_configured", null, null, null));
            return future;
        }

        try {
            System.out.println("🚀 Starting OAuth via ngrok for provider: " + provider);
            System.out.println("🌐 ngrok tunnel: " + getServerInfo());

            if ("facebook".equals(provider)) {
                openFacebookAuth();
            } else if ("google".equals(provider)) {
                openGoogleAuth();
            } else {
                java.util.concurrent.CompletableFuture<NgrokOAuthCallbackServer.OAuthResult> future = new java.util.concurrent.CompletableFuture<>();
                future.complete(new NgrokOAuthCallbackServer.OAuthResult(false, provider, "unknown_provider", null, null, null));
                return future;
            }

            java.util.concurrent.CompletableFuture<NgrokOAuthCallbackServer.OAuthResult> callbackFuture = sharedServer.waitForCallback();

            java.util.concurrent.CompletableFuture<NgrokOAuthCallbackServer.OAuthResult> timeoutFuture = new java.util.concurrent.CompletableFuture<>();
            java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);

            scheduler.schedule(() -> {
                if (!callbackFuture.isDone()) {
                    System.out.println("⏰ ngrok OAuth timeout reached (30 seconds)");
                    timeoutFuture.complete(new NgrokOAuthCallbackServer.OAuthResult(false, provider, "timeout", null, null, null));
                }
                scheduler.shutdown();
            }, 30, java.util.concurrent.TimeUnit.SECONDS);

            return java.util.concurrent.CompletableFuture.anyOf(callbackFuture, timeoutFuture)
                .thenApply(result -> (NgrokOAuthCallbackServer.OAuthResult) result);

        } catch (Exception e) {
            System.out.println("💥 Exception starting ngrok OAuth: " + e.getMessage());
            java.util.concurrent.CompletableFuture<NgrokOAuthCallbackServer.OAuthResult> future = new java.util.concurrent.CompletableFuture<>();
            future.complete(new NgrokOAuthCallbackServer.OAuthResult(false, provider, e.getMessage(), null, null, null));
            return future;
        }
    }

    public Client authenticateWithFacebookCode(String authorizationCode) throws Exception {        
        try {
            if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
                throw new Exception("Code d'autorisation Facebook vide ou null");
            }
            
            String accessToken = exchangeFacebookCodeForToken(authorizationCode);
            FacebookUserInfo userInfo = getFacebookUserInfo(accessToken);

            if (userInfo.email == null || userInfo.email.isEmpty()) {
                throw new Exception("Impossible de récupérer l'adresse email depuis Facebook.");
            }

            // Check if user exists with this email
            User existingUser = serviceUser.findByEmail(userInfo.email);

            if (existingUser != null) {
                // User exists, check if blocked
                if (existingUser.isBlocked()) {
                    throw new SQLException("COMPTE_BLOQUE:Votre compte a été bloqué par l'administrateur.");
                }

                // Check if it's a client account
                if (existingUser instanceof Client) {
                    return (Client) existingUser;
                } else {
                    throw new Exception("Un compte avec cet email existe déjà mais n'est pas un compte client.");
                }
            } else {
                // User doesn't exist, create new client account with minimal info
                String firstName = userInfo.firstName != null && !userInfo.firstName.trim().isEmpty()
                    ? userInfo.firstName.trim()
                    : extractFirstName(userInfo.name);

                String lastName = userInfo.lastName != null && !userInfo.lastName.trim().isEmpty()
                    ? userInfo.lastName.trim()
                    : extractLastName(userInfo.name);

                // Ensure we have valid names
                if (firstName == null || firstName.trim().isEmpty()) firstName = "User";
                if (lastName == null || lastName.trim().isEmpty()) lastName = "Facebook";

                // Use minimal data for new account
                String tempPassword = "FB_" + System.currentTimeMillis();
                String pays = "Non spécifié";
                String cin = generateTemporaryCin();

                // Use Facebook profile image if available
                String imageUrl = "user.png"; // Default fallback
                if (userInfo.pictureUrl != null && !userInfo.pictureUrl.isEmpty()) {
                    // Check if the URL is reasonable length for database storage
                    if (userInfo.pictureUrl.length() <= 255) {
                        imageUrl = userInfo.pictureUrl;
                    } else {
                        // URL too long, try to get a shorter version
                        System.out.println("⚠️ Facebook profile image URL too long, using default");
                    }
                }

                Client newClient = new Client(lastName, firstName, userInfo.email, tempPassword, pays, imageUrl, cin);
                newClient.setStatus(0);

                try {
                    serviceUser.ajouter(newClient);
                    System.out.println("✅ New Facebook user created: " + userInfo.email);
                    System.out.println("📸 Profile image: " + imageUrl);
                    return newClient;
                } catch (SQLException e) {
                    if (e.getMessage() != null && (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("unique"))) {
                        throw new Exception("Un compte avec cet email existe déjà.");
                    }
                    throw new Exception("Erreur lors de la création du compte: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Erreur d'authentification Facebook: " + e.getMessage());
        }
    }

    private String exchangeFacebookCodeForToken(String code) throws Exception {
        String redirectUri = ngrokUrl + "/auth/facebook/callback";

        String tokenUrl = String.format(
            "https://graph.facebook.com/v18.0/oauth/access_token?client_id=%s&client_secret=%s&redirect_uri=%s&code=%s",
            FACEBOOK_APP_ID,
            FACEBOOK_APP_SECRET,
            java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8),
            code
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            String accessToken = extractJsonValue(responseBody, "access_token");
            if (accessToken == null || accessToken.isEmpty()) {
                throw new Exception("Token d'accès non trouvé dans la réponse Facebook: " + responseBody);
            }
            return accessToken;
        } else {
            throw new Exception("Erreur lors de l'échange du code d'autorisation Facebook: " + response.statusCode() + " - " + response.body());
        }
    }

    private FacebookUserInfo getFacebookUserInfo(String accessToken) throws Exception {
        String apiUrl = String.format(
            "https://graph.facebook.com/v18.0/me?fields=id,name,email,first_name,last_name,picture.type(large)&access_token=%s",
            accessToken
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("📱 Facebook API response received");
            return parseJsonToFacebookUser(response.body());
        } else {
            throw new Exception("Erreur lors de la récupération des données utilisateur Facebook: " + response.statusCode());
        }
    }

    private FacebookUserInfo parseJsonToFacebookUser(String json) {
        FacebookUserInfo userInfo = new FacebookUserInfo();
        
        userInfo.id = extractJsonValue(json, "id");
        userInfo.name = extractJsonValue(json, "name");
        userInfo.email = extractJsonValue(json, "email");
        userInfo.firstName = extractJsonValue(json, "first_name");
        userInfo.lastName = extractJsonValue(json, "last_name");
        
        String pictureSection = extractJsonSection(json, "picture");
        if (pictureSection != null) {
            String dataSection = extractJsonSection(pictureSection, "data");
            if (dataSection != null) {
                userInfo.pictureUrl = extractJsonValue(dataSection, "url");
            }
        }
        
        return userInfo;
    }

    private String extractJsonValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        // Try quoted string first: "key":"value"
        String searchPattern = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex != -1) {
            startIndex += searchPattern.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex != -1) {
                String value = json.substring(startIndex, endIndex);
                return decodeUnicodeEscapes(value);
            }
        }

        // Try unquoted value: "key":value (for numbers, booleans, etc.)
        searchPattern = "\"" + key + "\":";
        startIndex = json.indexOf(searchPattern);
        if (startIndex != -1) {
            startIndex += searchPattern.length();

            // Skip whitespace
            while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
                startIndex++;
            }

            // Find the end of the value (comma, closing brace, or end of string)
            int endIndex = startIndex;
            while (endIndex < json.length()) {
                char c = json.charAt(endIndex);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    break;
                }
                endIndex++;
            }

            if (endIndex > startIndex) {
                String value = json.substring(startIndex, endIndex).trim();
                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return decodeUnicodeEscapes(value);
            }
        }

        System.err.println("❌ Could not extract '" + key + "' from JSON: " + json.substring(0, Math.min(200, json.length())) + "...");
        return null;
    }

    /**
     * Decode Unicode escape sequences in strings
     */
    private String decodeUnicodeEscapes(String input) {
        if (input == null || !input.contains("\\u")) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (i < input.length() - 5 && input.substring(i, i + 2).equals("\\u")) {
                try {
                    String hexCode = input.substring(i + 2, i + 6);
                    int charCode = Integer.parseInt(hexCode, 16);
                    result.append((char) charCode);
                    i += 6;
                } catch (NumberFormatException e) {
                    // Not a valid Unicode escape, just append the character
                    result.append(input.charAt(i));
                    i++;
                }
            } else {
                result.append(input.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    private String extractJsonSection(String json, String key) {
        String searchPattern = "\"" + key + "\":{";
        int startIndex = json.indexOf(searchPattern);
        int endIndex = -1;
        if (startIndex != -1) {
            startIndex += searchPattern.length() - 1;
            int braceCount = 1;
            int currentIndex = startIndex + 1;

            while (currentIndex < json.length() && braceCount > 0) {
                char c = json.charAt(currentIndex);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                currentIndex++;
            }

            if (braceCount == 0) {
                endIndex = currentIndex;
            }
        }

        if (endIndex != -1) {
            return json.substring(startIndex, endIndex);
        }
        
        return null;
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "User";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "User";
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "Facebook";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 && !parts[parts.length - 1].isEmpty() ? parts[parts.length - 1] : "Facebook";
    }

    private String generateTemporaryCin() {
        return "TEMP_" + System.currentTimeMillis() % 100000000L;
    }

    private static class FacebookUserInfo {
        String id;
        String name;
        String email;
        String firstName;
        String lastName;
        String pictureUrl;
    }

    private static class GoogleUserInfo {
        String id;
        String email;
        String name;
        String givenName;
        String familyName;
        String picture;
        String locale;
    }

    public Client authenticateWithGoogleCode(String authorizationCode) throws Exception {
        try {
            if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
                throw new Exception("Code d'autorisation Google vide ou null");
            }

            String accessToken = exchangeGoogleCodeForToken(authorizationCode);
            GoogleUserInfo userInfo = getGoogleUserInfo(accessToken);

            if (userInfo.email == null || userInfo.email.isEmpty()) {
                throw new Exception("Impossible de récupérer l'adresse email depuis Google.");
            }

            // Check if user exists with this email
            User existingUser = serviceUser.findByEmail(userInfo.email);

            if (existingUser != null) {
                // User exists, check if blocked
                if (existingUser.isBlocked()) {
                    throw new SQLException("COMPTE_BLOQUE:Votre compte a été bloqué par l'administrateur.");
                }

                // Check if it's a client account
                if (existingUser instanceof Client) {
                    return (Client) existingUser;
                } else {
                    throw new Exception("Un compte avec cet email existe déjà mais n'est pas un compte client.");
                }
            } else {
                // User doesn't exist, create new client account with Google data
                String firstName = userInfo.givenName != null && !userInfo.givenName.trim().isEmpty()
                    ? userInfo.givenName.trim()
                    : extractFirstName(userInfo.name);

                String lastName = userInfo.familyName != null && !userInfo.familyName.trim().isEmpty()
                    ? userInfo.familyName.trim()
                    : extractLastName(userInfo.name);

                // Ensure we have valid names
                if (firstName == null || firstName.trim().isEmpty()) firstName = "User";
                if (lastName == null || lastName.trim().isEmpty()) lastName = "Google";

                // Use minimal data for new account
                String tempPassword = "GOOGLE_" + System.currentTimeMillis();
                String pays = "Non spécifié";
                String cin = generateTemporaryCin();

                // Use Google profile image if available
                String imageUrl = "user.png"; // Default fallback
                if (userInfo.picture != null && !userInfo.picture.isEmpty()) {
                    // Check if the URL is reasonable length for database storage
                    if (userInfo.picture.length() <= 255) {
                        imageUrl = userInfo.picture;
                    } else {
                        // URL too long, try to get a shorter version
                        System.out.println("⚠️ Google profile image URL too long, using default");
                    }
                }

                Client newClient = new Client(lastName, firstName, userInfo.email, tempPassword, pays, imageUrl, cin);
                newClient.setStatus(0);

                try {
                    serviceUser.ajouter(newClient);
                    System.out.println("✅ New Google user created: " + userInfo.email);
                    System.out.println("📸 Profile image: " + imageUrl);
                    return newClient;
                } catch (SQLException e) {
                    if (e.getMessage() != null && (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("unique"))) {
                        throw new Exception("Un compte avec cet email existe déjà.");
                    }
                    throw new Exception("Erreur lors de la création du compte: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Erreur d'authentification Google: " + e.getMessage());
        }
    }

    private String exchangeGoogleCodeForToken(String code) throws Exception {
        String redirectUri = ngrokUrl + "/auth/google/callback";

        String tokenUrl = "https://oauth2.googleapis.com/token";

        String postData = String.format(
            "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s",
            java.net.URLEncoder.encode(GOOGLE_CLIENT_ID, java.nio.charset.StandardCharsets.UTF_8),
            java.net.URLEncoder.encode(GOOGLE_CLIENT_SECRET, java.nio.charset.StandardCharsets.UTF_8),
            java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8),
            java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8)
        );

        System.out.println("🔄 Exchanging Google authorization code for access token...");
        System.out.println("📍 Token URL: " + tokenUrl);
        System.out.println("🔗 Redirect URI: " + redirectUri);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📨 Google token response status: " + response.statusCode());
        System.out.println("📄 Google token response body: " + response.body());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            String accessToken = extractJsonValue(responseBody, "access_token");
            if (accessToken == null || accessToken.isEmpty()) {
                throw new Exception("Token d'accès non trouvé dans la réponse Google: " + responseBody);
            }
            System.out.println("✅ Access token extracted successfully");
            return accessToken;
        } else {
            String errorBody = response.body();
            System.err.println("❌ Google token exchange failed with status " + response.statusCode());
            System.err.println("❌ Error response: " + errorBody);

            // Check for common error types
            if (errorBody.contains("redirect_uri_mismatch")) {
                String currentNgrokUrl = detectCurrentNgrokUrl();
                String expectedRedirectUri = redirectUri;
                if (currentNgrokUrl != null) {
                    expectedRedirectUri = currentNgrokUrl + "/auth/google/callback";
                }

                throw new Exception("❌ REDIRECT URI MISMATCH ERROR!\n\n" +
                                   "Le problème: Les URIs de redirection ne correspondent pas.\n\n" +
                                   "🔧 SOLUTION IMMÉDIATE:\n" +
                                   "1. Allez sur Google Cloud Console: https://console.cloud.google.com/\n" +
                                   "2. APIs & Services > Credentials\n" +
                                   "3. Cliquez sur votre OAuth Client ID\n" +
                                   "4. Dans 'Authorized redirect URIs', ajoutez exactement:\n" +
                                   "   " + expectedRedirectUri + "\n" +
                                   "5. Cliquez SAVE\n" +
                                   "6. Attendez 1-2 minutes et réessayez\n\n" +
                                   "🌐 Port actuel: " + configuredPort + "\n" +
                                   "📍 URI attendu: " + expectedRedirectUri + "\n\n" +
                                   "💡 ASTUCE: L'URL ngrok change à chaque redémarrage!\n" +
                                   "Vous devez mettre à jour les URIs après chaque redémarrage de ngrok.");
            } else if (errorBody.contains("invalid_client")) {
                throw new Exception("❌ CLIENT GOOGLE INVALIDE!\n\n" +
                                   "Vérifiez votre Client ID et Client Secret dans:\n" +
                                   "Google Cloud Console > APIs & Services > Credentials\n\n" +
                                   "Client ID actuel: " + GOOGLE_CLIENT_ID.substring(0, 20) + "...\n" +
                                   "Assurez-vous qu'ils correspondent exactement à votre projet Google.");
            } else if (errorBody.contains("invalid_grant")) {
                throw new Exception("❌ CODE D'AUTORISATION EXPIRÉ!\n\n" +
                                   "Le code d'autorisation Google a expiré ou est invalide.\n" +
                                   "Réessayez l'authentification Google.");
            } else {
                throw new Exception("Erreur lors de l'échange du code d'autorisation Google: " + response.statusCode() + " - " + errorBody);
            }
        }
    }

    private GoogleUserInfo getGoogleUserInfo(String accessToken) throws Exception {
        String apiUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("📱 Google API response received");
            return parseJsonToGoogleUser(response.body());
        } else {
            throw new Exception("Erreur lors de la récupération des données utilisateur Google: " + response.statusCode());
        }
    }

    private GoogleUserInfo parseJsonToGoogleUser(String json) {
        GoogleUserInfo userInfo = new GoogleUserInfo();

        userInfo.id = extractJsonValue(json, "id");
        userInfo.email = extractJsonValue(json, "email");
        userInfo.name = extractJsonValue(json, "name");
        userInfo.givenName = extractJsonValue(json, "given_name");
        userInfo.familyName = extractJsonValue(json, "family_name");
        userInfo.picture = extractJsonValue(json, "picture");
        userInfo.locale = extractJsonValue(json, "locale");

        return userInfo;
    }

    /**
     * Cleanup method to properly shutdown the OAuth server
     * Call this when the application is closing
     */
    public static synchronized void shutdown() {
        if (sharedServer != null) {
            try {
                sharedServer.stop();
                System.out.println("🛑 OAuth server stopped gracefully");
            } catch (Exception e) {
                System.err.println("Warning: Error stopping OAuth server: " + e.getMessage());
            } finally {
                sharedServer = null;
                serverInitialized = false;
                instance = null;
            }
        }
    }

    /**
     * Switch to a different port if needed (e.g., to avoid conflicts)
     * This will shutdown the current server and start a new one on the specified port
     */
    public static synchronized void switchPort(int newPort) {
        System.out.println("🔄 Switching OAuth server from port " + configuredPort + " to " + newPort);

        // Shutdown current instance
        shutdown();

        // Create new instance with new port
        instance = getInstance(newPort);

        System.out.println("✅ OAuth server now running on port " + newPort);
        System.out.println("⚠️  IMPORTANT: You must now:");
        System.out.println("   1. Restart ngrok: ngrok http " + newPort + " --scheme=https");
        System.out.println("   2. Update redirect URIs in Google/Facebook OAuth settings");
    }

    /**
     * Get current redirect URI for Google OAuth
     */
    public String getGoogleRedirectUri() {
        String currentNgrokUrl = detectCurrentNgrokUrl();
        if (currentNgrokUrl != null) {
            return currentNgrokUrl + "/auth/google/callback";
        }
        return "https://YOUR_NGROK_URL.ngrok.io/auth/google/callback";
    }

    /**
     * Get current redirect URI for Facebook OAuth
     */
    public String getFacebookRedirectUri() {
        String currentNgrokUrl = detectCurrentNgrokUrl();
        if (currentNgrokUrl != null) {
            return currentNgrokUrl + "/auth/facebook/callback";
        }
        return "https://YOUR_NGROK_URL.ngrok.io/auth/facebook/callback";
    }

    /**
     * Display current configuration and redirect URIs
     */
    public void displayCurrentConfig() {
        System.out.println("📊 CURRENT OAUTH CONFIGURATION:");
        System.out.println("🔌 Local port: " + configuredPort);
        System.out.println("🌐 Server status: " + (isAvailable() ? "✅ Running" : "❌ Not available"));
        System.out.println("📍 Google redirect URI: " + getGoogleRedirectUri());
        System.out.println("📍 Facebook redirect URI: " + getFacebookRedirectUri());

        String currentNgrokUrl = detectCurrentNgrokUrl();
        if (currentNgrokUrl != null) {
            System.out.println("✅ ngrok URL detected: " + currentNgrokUrl);
        } else {
            System.out.println("❌ ngrok not detected. Run: ngrok http " + configuredPort + " --scheme=https");
        }
    }

    /**
     * Manually set the ngrok URL if auto-detection fails
     * Use this when you know your ngrok URL but the app can't detect it automatically
     */
    public static void setManualNgrokUrl(String ngrokUrl) {
        if (instance != null) {
            instance.ngrokUrl = ngrokUrl;
            System.out.println("🔧 Manually set ngrok URL to: " + ngrokUrl);
            System.out.println("📍 Google redirect URI: " + ngrokUrl + "/auth/google/callback");
            System.out.println("📍 Facebook redirect URI: " + ngrokUrl + "/auth/facebook/callback");
        }
    }

    /**
     * Get the manually set or auto-detected ngrok URL
     */
    public String getCurrentNgrokUrl() {
        if (ngrokUrl != null && !ngrokUrl.equals("NGROK_READY")) {
            return ngrokUrl;
        }
        return detectCurrentNgrokUrl();
    }
}
