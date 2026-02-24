package Services;

import Models.*;
import java.sql.SQLException;
import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

/**
 * Social media authentication service using ngrok tunnel
 */
public class SocialAuthService {

    private final ServiceUser serviceUser;
    private final HttpClient httpClient;
    private NgrokOAuthCallbackServer ngrokServer;
    private String ngrokUrl = null;

    // Facebook credentials
    private static final String FACEBOOK_APP_ID = "925022893757906";
    private static final String FACEBOOK_APP_SECRET = "e93e7c5a45f1c523b353a70bdaf5b5b9";

    public SocialAuthService() {
        this.serviceUser = new ServiceUser();
        this.httpClient = HttpClient.newHttpClient();
        
        try {
            this.ngrokServer = new NgrokOAuthCallbackServer();
            this.ngrokServer.start();
            
            System.out.println("🚀 NGROK SETUP REQUIRED:");
            System.out.println("1. Open a new terminal/command prompt");
            System.out.println("2. Run: ngrok http 8080 --scheme=https");
            System.out.println("3. Copy the HTTPS URL (e.g., https://abc123.ngrok.io)");
            System.out.println("4. Enter it below:");
            System.out.print("Enter ngrok HTTPS URL: ");
            
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            if (input.startsWith("https://") && input.contains("ngrok")) {
                this.ngrokUrl = input;
                System.out.println("✅ ngrok URL set: " + ngrokUrl);
                System.out.println("📝 Add this to Facebook app redirect URIs: " + ngrokUrl + "/auth/facebook/callback");
                System.out.println("");
                System.out.println("🎯 NEXT STEPS:");
                System.out.println("1. Go to: https://developers.facebook.com/apps/" + FACEBOOK_APP_ID + "/fb-login/settings/");
                System.out.println("2. Add this redirect URI: " + ngrokUrl + "/auth/facebook/callback");
                System.out.println("3. Save changes in Facebook");
                System.out.println("4. Click the Facebook login button in your app");
                System.out.println("5. Success! 🎉");
                System.out.println("");
            } else {
                System.out.println("❌ Invalid ngrok URL.");
            }
            
        } catch (Exception e) {
            System.err.println("Error starting ngrok server: " + e.getMessage());
            this.ngrokServer = null;
        }
    }

    public boolean isAvailable() {
        return ngrokServer != null && ngrokUrl != null;
    }

    public String getServerInfo() {
        if (isAvailable()) {
            return "ngrok tunnel: " + ngrokUrl + " -> localhost:8080";
        }
        return "ngrok not configured";
    }

    public void openFacebookAuth() throws Exception {
        if (!isAvailable()) {
            throw new Exception("ngrok n'est pas configuré. Veuillez démarrer ngrok et redémarrer l'application.");
        }

        String redirectUri = ngrokUrl + "/auth/facebook/callback";
        String authUrl = String.format(
            "https://www.facebook.com/v18.0/dialog/oauth?client_id=%s&redirect_uri=%s&scope=public_profile,email&response_type=code&state=facebook_auth",
            FACEBOOK_APP_ID,
            java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8)
        );

        System.out.println("🌐 Opening Facebook OAuth URL via ngrok: " + authUrl);
        System.out.println("📍 ngrok Redirect URI: " + redirectUri);

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
            System.out.println("✅ Facebook OAuth opened - ngrok tunnel active");
            System.out.println("⏳ Waiting for ngrok callback...");
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
            } else {
                java.util.concurrent.CompletableFuture<NgrokOAuthCallbackServer.OAuthResult> future = new java.util.concurrent.CompletableFuture<>();
                future.complete(new NgrokOAuthCallbackServer.OAuthResult(false, provider, "unknown_provider", null, null, null));
                return future;
            }

            java.util.concurrent.CompletableFuture<NgrokOAuthCallbackServer.OAuthResult> callbackFuture = ngrokServer.waitForCallback();

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

            User existingUser = serviceUser.findByEmail(userInfo.email);

            if (existingUser != null) {
                if (existingUser.isBlocked()) {
                    throw new SQLException("COMPTE_BLOQUE:Votre compte a été bloqué par l'administrateur.");
                }

                if (existingUser instanceof Client) {
                    return (Client) existingUser;
                } else {
                    throw new Exception("Un compte avec cet email existe déjà mais n'est pas un compte client.");
                }
            } else {
                String firstName = userInfo.firstName != null ? userInfo.firstName : extractFirstName(userInfo.name);
                String lastName = userInfo.lastName != null ? userInfo.lastName : extractLastName(userInfo.name);
                String tempPassword = "FB_" + System.currentTimeMillis();
                String pays = "Non spécifié";
                String cin = generateTemporaryCin();
                String imageUrl = userInfo.pictureUrl != null ? userInfo.pictureUrl : "https://img.icons8.com/color/96/facebook.png";
                
                Client newClient = new Client(lastName, firstName, userInfo.email, tempPassword, pays, imageUrl, cin);
                newClient.setStatus(0);

                try {
                    serviceUser.ajouter(newClient);
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
            "https://graph.facebook.com/v18.0/me?fields=id,name,email,first_name,last_name,picture&access_token=%s",
            accessToken
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
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
        String searchPattern = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) return null;
        
        startIndex += searchPattern.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;
        
        return json.substring(startIndex, endIndex);
    }

    private String extractJsonSection(String json, String key) {
        String searchPattern = "\"" + key + "\":{";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) return null;
        
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
            return json.substring(startIndex, currentIndex);
        }
        
        return null;
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "User";
        String[] parts = fullName.trim().split(" ");
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "User";
        String[] parts = fullName.trim().split(" ");
        return parts.length > 1 ? parts[parts.length - 1] : "User";
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
}
