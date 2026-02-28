package com.PlaNova.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * OAuth Configuration Manager - Loads credentials from secure properties file
 * This keeps sensitive credentials out of source code and GitHub
 */
public class OAuthConfig {

    private static final String CONFIG_FILE = "/oauth.properties";
    private static OAuthConfig instance;
    private Properties properties;

    private OAuthConfig() {
        loadProperties();
    }

    public static synchronized OAuthConfig getInstance() {
        if (instance == null) {
            instance = new OAuthConfig();
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();

        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("❌ OAuth configuration file not found: " + CONFIG_FILE);
                System.err.println("💡 Please create src/main/resources/oauth.properties with your OAuth credentials");
                System.err.println("💡 Use oauth.properties.template as a reference");
                throw new RuntimeException("OAuth configuration file not found");
            }

            properties.load(input);
            System.out.println("✅ OAuth configuration loaded successfully");

        } catch (IOException e) {
            System.err.println("❌ Error loading OAuth configuration: " + e.getMessage());
            throw new RuntimeException("Failed to load OAuth configuration", e);
        }
    }

    // Facebook Configuration
    public String getFacebookAppId() {
        return getProperty("facebook.app.id", "FACEBOOK_APP_ID_NOT_CONFIGURED");
    }

    public String getFacebookAppSecret() {
        return getProperty("facebook.app.secret", "FACEBOOK_APP_SECRET_NOT_CONFIGURED");
    }

    // Google Configuration
    public String getGoogleClientId() {
        return getProperty("google.client.id", "GOOGLE_CLIENT_ID_NOT_CONFIGURED");
    }

    public String getGoogleClientSecret() {
        return getProperty("google.client.secret", "GOOGLE_CLIENT_SECRET_NOT_CONFIGURED");
    }

    public String getGoogleScope() {
        return getProperty("google.scope", "openid email profile");
    }

    private String getProperty(String key, String defaultValue) {
        // Map property key to Env variable name (e.g. facebook.app.id ->
        // FACEBOOK_APP_ID)
        String envKey = key.replace('.', '_').toUpperCase();
        String value = EnvConfig.get(envKey, properties.getProperty(key));

        if (value == null || value.trim().isEmpty() || value.startsWith("YOUR_")) {
            System.err.println("⚠️ OAuth property not configured: " + key);
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Validate that all required OAuth credentials are configured
     */
    public void validateConfiguration() {
        boolean isValid = true;

        if (getFacebookAppId().startsWith("FACEBOOK_APP_ID")) {
            System.err.println("❌ Facebook App ID not configured");
            isValid = false;
        }

        if (getFacebookAppSecret().startsWith("FACEBOOK_APP_SECRET")) {
            System.err.println("❌ Facebook App Secret not configured");
            isValid = false;
        }

        if (getGoogleClientId().startsWith("GOOGLE_CLIENT_ID")) {
            System.err.println("❌ Google Client ID not configured");
            isValid = false;
        }

        if (getGoogleClientSecret().startsWith("GOOGLE_CLIENT_SECRET")) {
            System.err.println("❌ Google Client Secret not configured");
            isValid = false;
        }

        if (!isValid) {
            throw new RuntimeException("OAuth configuration is incomplete. Please check oauth.properties file.");
        }

        System.out.println("✅ All OAuth credentials are configured properly");
    }
}
