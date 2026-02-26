package Services;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Simple HTTP server for ngrok tunnel - Configurable OAuth callback
 */
public class NgrokOAuthCallbackServer {

    private final HttpServer server;
    private CompletableFuture<OAuthResult> resultFuture;
    private final int port;

    public NgrokOAuthCallbackServer() throws Exception {
        this(8080); // Default port
    }

    public NgrokOAuthCallbackServer(int port) throws Exception {
        this.port = port;
        // Create simple HTTP server for ngrok tunnel on specified port
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);

        server.createContext("/auth/facebook/callback", new FacebookCallbackHandler());
        server.createContext("/auth/google/callback", new GoogleCallbackHandler());
        server.setExecutor(null);

        System.out.println("🌐 ngrok OAuth callback server created on port " + port);
    }


    public int getPort() {
        return port;
    }

    public void start() {
        server.start();
        System.out.println("✅ ngrok OAuth callback server STARTED on http://127.0.0.1:" + port);
        System.out.println("🔗 IMPORTANT: Run ngrok with the SAME port: ngrok http " + port + " --scheme=https");
        System.out.println("📝 Add the ngrok HTTPS URL to your OAuth providers");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("ngrok OAuth callback server stopped");
        }
    }

    public synchronized CompletableFuture<OAuthResult> waitForCallback() {
        // Cancel any existing future
        if (resultFuture != null && !resultFuture.isDone()) {
            System.out.println("🔄 Cancelling previous callback future");
            resultFuture.cancel(true);
        }

        // Create new future
        resultFuture = new CompletableFuture<>();
        System.out.println("🔄 New callback future created");

        return resultFuture;
    }

    /**
     * Safely complete the result future to prevent 502 errors
     */
    private synchronized void completeResultFuture(OAuthResult result) {
        if (resultFuture != null && !resultFuture.isDone()) {
            System.out.println("🔄 Completing callback future with result: " + result.provider + " - success: " + result.success);
            resultFuture.complete(result);
        } else {
            System.out.println("⚠️ Cannot complete future - already done or null");
        }
    }

    private class FacebookCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            System.out.println("📨 Facebook callback received via ngrok - Query: " + query);

            Map<String, String> params = parseQuery(query);
            String code = params.get("code");
            String error = params.get("error");
            String state = params.get("state");

            String response;
            if (error != null) {
                System.out.println("❌ Facebook returned error: " + error);
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                          "<h1 style='color: red;'>❌ Erreur Facebook</h1>" +
                          "<p>Erreur: " + error + "</p>" +
                          "<p>Vous pouvez fermer cette fenêtre.</p></body></html>";
                if (resultFuture != null) {
                    completeResultFuture(new OAuthResult(false, "facebook", error, null, null, null));
                }
            } else if (code != null) {
                System.out.println("✅ Facebook returned authorization code via ngrok: " + code.substring(0, Math.min(10, code.length())) + "...");
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #4267B2; color: white;'>" +
                          "<h1 style='color: #ffffff;'>✅ Authentification Facebook Réussie!</h1>" +
                          "<p style='font-size: 18px;'>Code d'autorisation reçu via ngrok.</p>" +
                          "<p style='font-size: 16px;'>Retour à l'application en cours...</p>" +
                          "<script>setTimeout(function(){ window.close(); }, 2000);</script></body></html>";
                if (resultFuture != null) {
                    completeResultFuture(new OAuthResult(true, "facebook", null, code, state, null));
                }
            } else {
                System.out.println("❌ No code or error in Facebook callback");
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                          "<h1>❌ Erreur</h1><p>Paramètres d'authentification manquants</p></body></html>";
                if (resultFuture != null) {
                    completeResultFuture(new OAuthResult(false, "facebook", "missing_params", null, null, null));
                }
            }

            sendResponse(exchange, response);
        }
    }

    private class GoogleCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            try {
                String query = exchange.getRequestURI().getQuery();
                System.out.println("📨 Google callback received via ngrok - Query: " + query);
                System.out.println("🔍 Request URI: " + exchange.getRequestURI().toString());
                System.out.println("🔍 Request method: " + exchange.getRequestMethod());

                Map<String, String> params = parseQuery(query);
                String code = params.get("code");
                String error = params.get("error");
                String state = params.get("state");

                System.out.println("📋 Parsed parameters:");
                System.out.println("   - code: " + (code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null"));
                System.out.println("   - error: " + error);
                System.out.println("   - state: " + state);

                if (error != null) {
                    System.err.println("❌ Google OAuth error received: " + error);
                    response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                              "<h1 style='color: red;'>❌ Erreur Google</h1>" +
                              "<p>Erreur: " + error + "</p>" +
                              "<p><a href='javascript:window.close()'>Fermer cette fenêtre</a></p>" +
                              "</body></html>";

                    if (resultFuture != null) {
                        System.out.println("🔄 Completing future with Google error: " + error);
                        completeResultFuture(new OAuthResult(false, "google", error, null, null, null));
                    }
                } else if (code != null) {
                    System.out.println("✅ Google authorization code received successfully");
                    response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px; background: linear-gradient(135deg, #4285F4, #34A853); color: white;'>" +
                              "<h1>✅ Authentification Google Réussie!</h1>" +
                              "<p>Code d'autorisation reçu via ngrok.</p>" +
                              "<p>Retour à PlaNova en cours...</p>" +
                              "<script>" +
                              "setTimeout(function(){ " +
                              "  try { window.close(); } catch(e) { " +
                              "    document.body.innerHTML += '<p><a href=\"javascript:window.close()\">Cliquez ici pour fermer</a></p>'; " +
                              "  } " +
                              "}, 2000);" +
                              "</script></body></html>";

                    if (resultFuture != null) {
                        System.out.println("🔄 Completing future with Google success");
                        completeResultFuture(new OAuthResult(true, "google", null, code, state, null));
                    }
                } else {
                    System.err.println("❌ Google callback missing required parameters");
                    response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                              "<h1 style='color: orange;'>⚠️ Paramètres Manquants</h1>" +
                              "<p>Aucun code d'autorisation ou erreur reçu de Google.</p>" +
                              "<p>Query string: " + (query != null ? query : "null") + "</p>" +
                              "<p><a href='javascript:window.close()'>Fermer cette fenêtre</a></p>" +
                              "</body></html>";

                    if (resultFuture != null) {
                        System.out.println("🔄 Completing future with missing parameters error");
                        completeResultFuture(new OAuthResult(false, "google", "missing_params", null, null, null));
                    }
                }

                System.out.println("📤 Sending Google callback response");
                sendResponse(exchange, response);
                System.out.println("✅ Google callback response sent successfully");

            } catch (Exception e) {
                System.err.println("💥 CRITICAL ERROR in Google callback handler: " + e.getMessage());
                e.printStackTrace();

                // Create emergency error response
                try {
                    response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                              "<h1 style='color: red;'>💥 Erreur Serveur</h1>" +
                              "<p>Erreur interne du serveur OAuth.</p>" +
                              "<p>Erreur: " + e.getMessage() + "</p>" +
                              "<p><a href='javascript:window.close()'>Fermer cette fenêtre</a></p>" +
                              "</body></html>";

                    sendResponse(exchange, response);

                    if (resultFuture != null) {
                        System.out.println("🔄 Completing future with server error");
                        completeResultFuture(new OAuthResult(false, "google", "server_error: " + e.getMessage(), null, null, null));
                    }
                } catch (IOException ioEx) {
                    System.err.println("💥 DOUBLE ERROR: Could not send error response: " + ioEx.getMessage());
                }
            }
        }
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        try {
            System.out.println("📤 Preparing to send response (length: " + response.length() + ")");

            // Set headers first
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");

            // Convert response to bytes
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            System.out.println("📤 Response bytes length: " + responseBytes.length);

            // Send response headers
            exchange.sendResponseHeaders(200, responseBytes.length);
            System.out.println("📤 Response headers sent successfully");

            // Send response body
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.flush();
            os.close();
            System.out.println("📤 Response body sent and stream closed successfully");

        } catch (Exception e) {
            System.err.println("💥 ERROR in sendResponse: " + e.getMessage());
            e.printStackTrace();

            // Try to send a minimal error response
            try {
                String errorResponse = "<html><body><h1>Server Error</h1><p>" + e.getMessage() + "</p></body></html>";
                byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);

                if (!exchange.getResponseHeaders().isEmpty()) {
                    // Headers already sent, can't recover
                    System.err.println("💥 Cannot recover - headers already sent");
                    throw e;
                }

                exchange.sendResponseHeaders(500, errorBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(errorBytes);
                os.close();
                System.err.println("📤 Error response sent");

            } catch (Exception secondaryError) {
                System.err.println("💥 CRITICAL: Could not send error response: " + secondaryError.getMessage());
                throw new IOException("Failed to send response", e);
            }
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    public static class OAuthResult {
        public final boolean success;
        public final String provider;
        public final String error;
        public final String code;
        public final String state;
        public final String accessToken;

        public OAuthResult(boolean success, String provider, String error, String code, String state, String accessToken) {
            this.success = success;
            this.provider = provider;
            this.error = error;
            this.code = code;
            this.state = state;
            this.accessToken = accessToken;
        }
    }
}
