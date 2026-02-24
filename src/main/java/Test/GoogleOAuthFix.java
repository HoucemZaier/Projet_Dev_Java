package Test;

import Services.SocialAuthService;

/**
 * Quick fix utility to manually set your ngrok URL for immediate testing
 */
public class GoogleOAuthFix {

    public static void main(String[] args) {
        System.out.println("🔧 Google OAuth Quick Fix Utility");
        System.out.println("================================");

        // Your current ngrok URL from the screenshot
        String yourNgrokUrl = "https://trimly-unmetallurgical-jerry.ngrok-free.dev";

        try {
            // Get the SocialAuthService instance
            SocialAuthService service = SocialAuthService.getInstance();

            // Manually set the ngrok URL
            SocialAuthService.setManualNgrokUrl(yourNgrokUrl);

            // Display the configuration
            service.displayCurrentConfig();

            System.out.println("\n✅ QUICK FIX APPLIED!");
            System.out.println("📋 What was set:");
            System.out.println("   🌐 ngrok URL: " + yourNgrokUrl);
            System.out.println("   📍 Google redirect URI: " + yourNgrokUrl + "/auth/google/callback");
            System.out.println("   📍 Facebook redirect URI: " + yourNgrokUrl + "/auth/facebook/callback");

            System.out.println("\n🚨 IMPORTANT:");
            System.out.println("1. Make sure this EXACT URI is in your Google Cloud Console:");
            System.out.println("   " + yourNgrokUrl + "/auth/google/callback");
            System.out.println("2. Now try the Google authentication in your app");
            System.out.println("3. If the URL changes, update yourNgrokUrl in this file and run again");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
