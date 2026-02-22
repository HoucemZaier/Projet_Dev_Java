package utils.Services;

import java.io.*;
import java.util.Properties;

/**
 * Service pour gérer la configuration sécurisée des emails
 * Permet de stocker et récupérer les identifiants email de manière sécurisée
 */
public class EmailConfigService {
    
    private static final String CONFIG_FILE = "email_config.properties";
    private Properties properties;
    
    public EmailConfigService() {
        loadConfig();
    }
    
    /**
     * Charge la configuration depuis le fichier
     */
    private void loadConfig() {
        properties = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la configuration email: " + e.getMessage());
                setDefaultConfig();
            }
        } else {
            setDefaultConfig();
            saveConfig();
        }
    }
    
    /**
     * Définit la configuration par défaut
     */
    private void setDefaultConfig() {
        properties.setProperty("smtp.host", "smtp.gmail.com");
        properties.setProperty("smtp.port", "587");
        properties.setProperty("email.username", "houcem.engineering@gmail.com");
        properties.setProperty("email.password", "iyft zvux oxvk zedcz");
        properties.setProperty("email.from", "PlaNova Transport <houcem.engineering@gmail.com>");
    }
    
    /**
     * Sauvegarde la configuration dans le fichier
     */
    public void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Configuration Email PlaNova Transport");
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la configuration email: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour le nom d'utilisateur email
     */
    public void setEmailUsername(String username) {
        properties.setProperty("email.username", username);
        saveConfig();
    }
    
    /**
     * Met à jour le mot de passe email
     */
    public void setEmailPassword(String password) {
        properties.setProperty("email.password", password);
        saveConfig();
    }
    
    /**
     * Récupère le nom d'utilisateur email
     */
    public String getEmailUsername() {
        return properties.getProperty("email.username");
    }
    
    /**
     * Récupère le mot de passe email
     */
    public String getEmailPassword() {
        return properties.getProperty("email.password");
    }
    
    /**
     * Récupère l'hôte SMTP
     */
    public String getSmtpHost() {
        return properties.getProperty("smtp.host");
    }
    
    /**
     * Récupère le port SMTP
     */
    public int getSmtpPort() {
        return Integer.parseInt(properties.getProperty("smtp.port", "587"));
    }
    
    /**
     * Récupère l'adresse email d'envoi
     */
    public String getFromEmail() {
        return properties.getProperty("email.from");
    }
    
    /**
     * Vérifie si la configuration est valide
     */
    public boolean isConfigValid() {
        String username = getEmailUsername();
        String password = getEmailPassword();
        
        return username != null && !username.isEmpty() && 
               password != null && !password.isEmpty() && 
               !password.equals("qhft digu dfpi risg");
    }
    
    /**
     * Affiche la configuration actuelle (pour débogage)
     */
    public void printConfig() {
        System.out.println("=== Configuration Email ===");
        System.out.println("SMTP Host: " + getSmtpHost());
        System.out.println("SMTP Port: " + getSmtpPort());
        System.out.println("Username: " + getEmailUsername());
        System.out.println("Password: " + (getEmailPassword().length() > 0 ? "***" + getEmailPassword().substring(getEmailPassword().length() - 3) : "vide"));
        System.out.println("From: " + getFromEmail());
        System.out.println("Config valide: " + isConfigValid());
        System.out.println("========================");
    }
}
