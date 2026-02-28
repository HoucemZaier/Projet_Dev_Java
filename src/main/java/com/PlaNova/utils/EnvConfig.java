package com.PlaNova.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the .env file located at the project root and exposes
 * each key through {@link #get(String)}.
 *
 * Usage:
 * String key = EnvConfig.get("STRIPE_SECRET_KEY");
 */
public class EnvConfig {

    private static final Map<String, String> ENV = new HashMap<>();

    static {
        // Resolve project root: when running from Maven / IDE the working directory is
        // the project root (where pom.xml lives). We walk up from the class-path root
        // just in case the JVM is started from a sub-directory.
        File envFile = findEnvFile();
        if (envFile != null && envFile.exists()) {
            load(envFile);
        } else {
            System.err.println("[EnvConfig] WARNING: .env file not found. "
                    + "Expected at: " + new File(".env").getAbsolutePath());
        }
    }

    /** Returns the value for the given key, or {@code null} if not set. */
    public static String get(String key) {
        // System properties / OS env vars override the .env file
        String sysProp = System.getProperty(key);
        if (sysProp != null)
            return sysProp;

        String osEnv = System.getenv(key);
        if (osEnv != null)
            return osEnv;

        return ENV.get(key);
    }

    /** Like {@link #get(String)} but returns {@code defaultValue} when missing. */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }

    // -------------------------------------------------------------------------

    private static File findEnvFile() {
        // Try current working directory first
        File f = new File(".env");
        if (f.exists())
            return f;

        // Walk up up to 4 levels (useful when tests run from a sub-directory)
        File dir = new File(".").getAbsoluteFile().getParentFile();
        for (int i = 0; i < 4 && dir != null; i++) {
            f = new File(dir, ".env");
            if (f.exists())
                return f;
            dir = dir.getParentFile();
        }
        return null;
    }

    private static void load(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip blank lines and comments
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                int idx = line.indexOf('=');
                if (idx < 1)
                    continue;

                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                ENV.put(key, value);
            }
            System.out.println("[EnvConfig] Loaded " + ENV.size() + " variables from " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[EnvConfig] Failed to read .env: " + e.getMessage());
        }
    }
}
