package Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Services.SocialAuthService;

public class mainfx extends Application {

    public static void main(String[] args) {
        // Add shutdown hook to properly cleanup OAuth server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔄 Application shutting down, cleaning up OAuth server...");
            SocialAuthService.shutdown();
        }));

        // 🔧 PORT SYNCHRONIZATION FIX: Force OAuth server to use port 8080
        try {
            System.out.println("🔧 PORT FIX: Ensuring OAuth server runs on port 8080 (matching ngrok)");

            // CRITICAL: Force the OAuth server to use port 8080 to match ngrok tunnel
            SocialAuthService service = SocialAuthService.getInstance(8080); // Force port 8080

            // Your current ngrok URL from the screenshot
            String yourNgrokUrl = "https://trimly-unmetallurgical-jerry.ngrok-free.dev";

            // Set manual ngrok URL to ensure consistency
            SocialAuthService.setManualNgrokUrl(yourNgrokUrl);

            System.out.println("✅ PORT SYNCHRONIZATION COMPLETE:");
            System.out.println("   🔌 OAuth server port: 8080");
            System.out.println("   🌐 ngrok tunnel: " + yourNgrokUrl + " → localhost:8080");
            System.out.println("   📍 Google redirect URI: " + yourNgrokUrl + "/auth/google/callback");
            System.out.println("   📍 Facebook redirect URI: " + yourNgrokUrl + "/auth/facebook/callback");

        } catch (Exception e) {
            System.err.println("❌ CRITICAL: Could not start OAuth server on port 8080!");
            System.err.println("❌ Error: " + e.getMessage());
            System.err.println("💡 SOLUTION: Make sure port 8080 is available:");
            System.err.println("   1. Close any other applications using port 8080");
            System.err.println("   2. Run: netstat -ano | findstr :8080");
            System.err.println("   3. Kill any processes using port 8080");
            System.err.println("   4. Restart this application");
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the login scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("PlaNova - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // Add close request handler to cleanup OAuth server
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("🔄 Window closing, cleaning up OAuth server...");
                SocialAuthService.shutdown();
            });

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading login scene: " + e.getMessage());
        }
    }
}
