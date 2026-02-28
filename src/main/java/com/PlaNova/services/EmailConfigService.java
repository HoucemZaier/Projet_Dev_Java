package com.PlaNova.services;

import com.PlaNova.utils.EnvConfig;
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
        return EnvConfig.get("EMAIL_USERNAME", properties.getProperty("email.username"));
    }

    public String getEmailPassword() {
        return EnvConfig.get("EMAIL_PASSWORD", properties.getProperty("email.password"));
    }

    public String getSmtpHost() {
        return EnvConfig.get("SMTP_HOST", properties.getProperty("smtp.host", "smtp.gmail.com"));
    }

    public int getSmtpPort() {
        String port = EnvConfig.get("SMTP_PORT", properties.getProperty("smtp.port", "587"));
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return 587;
        }
    }

    public String getFromEmail() {
        return EnvConfig.get("EMAIL_FROM",
                properties.getProperty("email.from", "PlaNova Transport <houcem.engineering@gmail.com>"));
    }

    public boolean isConfigValid() {
        String username = getEmailUsername();
        String password = getEmailPassword();
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }

    public void setEmailUsername(String username) {
        properties.setProperty("email.username", username);
    }

    public void setEmailPassword(String password) {
        properties.setProperty("email.password", password);
    }

    public void printConfig() {
        System.out.println("Email Config: " + properties.toString());
    }
}
