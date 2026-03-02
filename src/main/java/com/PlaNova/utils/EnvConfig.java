package com.PlaNova.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads key-value pairs from a {@code .env} file located at the project root.
 * Falls back to system environment variables when a key is not found in the file.
 */
public class EnvConfig {

    private static final Map<String, String> envVars = new HashMap<>();

    static {
        loadEnvFile();
    }

    private static void loadEnvFile() {
        // Try multiple locations: project root, working directory
        Path envPath = findEnvFile();
        if (envPath != null && Files.exists(envPath)) {
            try (BufferedReader reader = Files.newBufferedReader(envPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // Skip comments and empty lines
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int idx = line.indexOf('=');
                    if (idx > 0) {
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        // Remove surrounding quotes if present
                        if (value.length() >= 2
                                && ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'")))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        envVars.put(key, value);
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not read .env file: " + e.getMessage());
            }
        } else {
            System.err.println("Warning: .env file not found. Using system environment variables.");
        }
    }

    private static Path findEnvFile() {
        // Check current working directory
        Path cwd = Paths.get(".env");
        if (Files.exists(cwd)) return cwd;

        // Check project root relative to classpath (common Maven layout)
        Path projectRoot = Paths.get(System.getProperty("user.dir"), ".env");
        if (Files.exists(projectRoot)) return projectRoot;

        return null;
    }

    /**
     * Returns the value for the given key.
     * Checks the .env file first, then falls back to system environment variables.
     */
    public static String get(String key) {
        String value = envVars.get(key);
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }
        return value;
    }

    /**
     * Returns the value for the given key, or the default if not found.
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
