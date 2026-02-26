package com.PlaNova.services;

import java.io.*;
import java.util.Properties;

public class EmailConfigService {
    private static final String CONFIG_FILE = "email_config.properties";
    private Properties properties;

    public EmailConfigService() {
        loadConfig();
    }

    private void loadConfig() {
        properties = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading email config: " + e.getMessage());
                setDefaultConfig();
            }
        } else {
            setDefaultConfig();
            saveConfig();
        }
    }

    private void setDefaultConfig() {
        properties.setProperty("smtp.host", "smtp.gmail.com");
        properties.setProperty("smtp.port", "587");
        properties.setProperty("email.username", "houcem.engineering@gmail.com");
        properties.setProperty("email.password", "iyft zvux oxvk zedcz");
        properties.setProperty("email.from", "PlaNova Transport <houcem.engineering@gmail.com>");
    }

    public void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Email Config");
        } catch (IOException e) {
            System.err.println("Error saving email config: " + e.getMessage());
        }
    }

    public String getEmailUsername() {
        return properties.getProperty("email.username");
    }

    public String getEmailPassword() {
        return properties.getProperty("email.password");
    }

    public String getSmtpHost() {
        return properties.getProperty("smtp.host");
    }

    public int getSmtpPort() {
        return Integer.parseInt(properties.getProperty("smtp.port", "587"));
    }

    public String getFromEmail() {
        return properties.getProperty("email.from");
    }

    public boolean isConfigValid() {
        String username = getEmailUsername();
        String password = getEmailPassword();
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }
}
