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
 * Simple HTTP server for ngrok tunnel - Facebook OAuth callback
 */
public class NgrokOAuthCallbackServer {

    private final HttpServer server;
    private CompletableFuture<OAuthResult> resultFuture;
    private final int port = 8080;

    public NgrokOAuthCallbackServer() throws Exception {
        // Create simple HTTP server for ngrok tunnel
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);

        server.createContext("/auth/facebook/callback", new FacebookCallbackHandler());
        server.createContext("/auth/google/callback", new GoogleCallbackHandler());
        server.setExecutor(null);

        System.out.println("ngrok OAuth callback server created on port " + port);
    }

    public int getPort() {
        return port;
    }

    public void start() {
        server.start();
        System.out.println("ngrok OAuth callback server started on http://127.0.0.1:" + port);
        System.out.println("🌐 NEXT STEP: Start ngrok with: ngrok http " + port + " --scheme=https");
        System.out.println("📝 Then add the ngrok HTTPS URL to your Facebook app redirect URIs");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("ngrok OAuth callback server stopped");
        }
    }

    public CompletableFuture<OAuthResult> waitForCallback() {
        if (resultFuture != null && !resultFuture.isDone()) {
            resultFuture.cancel(true);
        }
        resultFuture = new CompletableFuture<>();
        return resultFuture;
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
                    resultFuture.complete(new OAuthResult(false, "facebook", error, null, null, null));
                }
            } else if (code != null) {
                System.out.println("✅ Facebook returned authorization code via ngrok: " + code.substring(0, Math.min(10, code.length())) + "...");
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #4267B2; color: white;'>" +
                          "<h1 style='color: #ffffff;'>✅ Authentification Facebook Réussie!</h1>" +
                          "<p style='font-size: 18px;'>Code d'autorisation reçu via ngrok.</p>" +
                          "<p style='font-size: 16px;'>Retour à l'application en cours...</p>" +
                          "<script>setTimeout(function(){ window.close(); }, 2000);</script></body></html>";
                if (resultFuture != null) {
                    resultFuture.complete(new OAuthResult(true, "facebook", null, code, state, null));
                }
            } else {
                System.out.println("❌ No code or error in Facebook callback");
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                          "<h1>❌ Erreur</h1><p>Paramètres d'authentification manquants</p></body></html>";
                if (resultFuture != null) {
                    resultFuture.complete(new OAuthResult(false, "facebook", "missing_params", null, null, null));
                }
            }

            sendResponse(exchange, response);
        }
    }

    private class GoogleCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            System.out.println("📨 Google callback received via ngrok - Query: " + query);

            Map<String, String> params = parseQuery(query);
            String code = params.get("code");
            String error = params.get("error");
            String state = params.get("state");

            String response;
            if (error != null) {
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                          "<h1 style='color: red;'>❌ Erreur Google</h1>" +
                          "<p>Erreur: " + error + "</p></body></html>";
                if (resultFuture != null) {
                    resultFuture.complete(new OAuthResult(false, "google", error, null, null, null));
                }
            } else if (code != null) {
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #4285F4; color: white;'>" +
                          "<h1>✅ Authentification Google Réussie!</h1>" +
                          "<p>Code d'autorisation reçu via ngrok.</p>" +
                          "<script>setTimeout(function(){ window.close(); }, 2000);</script></body></html>";
                if (resultFuture != null) {
                    resultFuture.complete(new OAuthResult(true, "google", null, code, state, null));
                }
            } else {
                response = "<html><body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>" +
                          "<h1>❌ Erreur</h1><p>Paramètres manquants</p></body></html>";
                if (resultFuture != null) {
                    resultFuture.complete(new OAuthResult(false, "google", "missing_params", null, null, null));
                }
            }

            sendResponse(exchange, response);
        }
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
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
