package utils;

import Services.SocialAuthService;

/**
 * OAuth Configuration and Troubleshooting Utility
 * Use this tool to manage ports, check configuration, and troubleshoot OAuth issues
 */
public class OAuthTroubleshooter {

    public static void main(String[] args) {
        System.out.println("🔧 PlaNova OAuth Configuration & Troubleshooting Tool");
        System.out.println("==================================================");

        if (args.length == 0) {
            showHelp();
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "status":
                showStatus();
                break;
            case "port":
                if (args.length < 2) {
                    System.err.println("❌ Usage: java OAuthTroubleshooter port <port_number>");
                    return;
                }
                changePort(Integer.parseInt(args[1]));
                break;
            case "config":
                showConfig();
                break;
            case "fix":
                fixCommonIssues();
                break;
            case "help":
            default:
                showHelp();
                break;
        }
    }

    private static void showHelp() {
        System.out.println("📋 Available Commands:");
        System.out.println();
        System.out.println("  status    - Show current OAuth server status");
        System.out.println("  port <n>  - Switch to different port (e.g., port 8081)");
        System.out.println("  config    - Display current configuration and redirect URIs");
        System.out.println("  fix       - Attempt to fix common OAuth issues");
        System.out.println("  help      - Show this help message");
        System.out.println();
        System.out.println("📝 Examples:");
        System.out.println("  java OAuthTroubleshooter status");
        System.out.println("  java OAuthTroubleshooter port 8081");
        System.out.println("  java OAuthTroubleshooter config");
    }

    private static void showStatus() {
        System.out.println("📊 OAuth Server Status:");
        System.out.println("========================");

        try {
            SocialAuthService service = SocialAuthService.getInstance();
            System.out.println("🔌 Current port: " + SocialAuthService.getConfiguredPort());
            System.out.println("🌐 Server available: " + (service.isAvailable() ? "✅ Yes" : "❌ No"));
            System.out.println("📄 Server info: " + service.getServerInfo());
        } catch (Exception e) {
            System.err.println("❌ Error checking status: " + e.getMessage());
        }
    }

    private static void changePort(int newPort) {
        System.out.println("🔄 Attempting to switch to port " + newPort + "...");

        try {
            SocialAuthService.switchPort(newPort);
            System.out.println("✅ Successfully switched to port " + newPort);

            // Show updated configuration
            showConfig();
        } catch (Exception e) {
            System.err.println("❌ Error switching port: " + e.getMessage());
            System.out.println("💡 Try a different port or check if port " + newPort + " is available");
        }
    }

    private static void showConfig() {
        System.out.println("⚙️ Current OAuth Configuration:");
        System.out.println("================================");

        try {
            SocialAuthService service = SocialAuthService.getInstance();
            service.displayCurrentConfig();

            System.out.println();
            System.out.println("🔧 Next Steps:");
            System.out.println("1. Make sure ngrok is running: ngrok http " + SocialAuthService.getConfiguredPort() + " --scheme=https");
            System.out.println("2. Copy the ngrok HTTPS URL");
            System.out.println("3. Add redirect URIs to OAuth providers:");
            System.out.println("   📘 Facebook: https://developers.facebook.com/apps/925022893757906/fb-login/settings/");
            System.out.println("   📕 Google: https://console.cloud.google.com/apis/credentials");
        } catch (Exception e) {
            System.err.println("❌ Error showing configuration: " + e.getMessage());
        }
    }

    private static void fixCommonIssues() {
        System.out.println("🔧 Attempting to fix common OAuth issues...");
        System.out.println("===========================================");

        // Try different ports if 8080 is occupied
        int[] portsToTry = {8080, 8081, 8082, 8083, 8084};

        for (int port : portsToTry) {
            try {
                System.out.println("🔍 Trying port " + port + "...");
                SocialAuthService service = SocialAuthService.getInstance(port);

                if (service.isAvailable()) {
                    System.out.println("✅ Successfully started OAuth server on port " + port);
                    service.displayCurrentConfig();
                    return;
                } else {
                    System.out.println("❌ Port " + port + " not available");
                }
            } catch (Exception e) {
                System.out.println("❌ Port " + port + " failed: " + e.getMessage());
            }
        }

        System.err.println("❌ Could not find an available port. Please:");
        System.err.println("   1. Close other applications using ports 8080-8084");
        System.err.println("   2. Restart your computer");
        System.err.println("   3. Try again");
    }
}
