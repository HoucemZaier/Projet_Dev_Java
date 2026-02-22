package utils.Services;

/**
 * Utilitaire de test pour la configuration email
 */
public class EmailConfigTest {
    
    public static void main(String[] args) {
        EmailConfigService configService = new EmailConfigService();
        
        System.out.println("=== Test de Configuration Email ===");
        
        // Afficher la configuration actuelle
        configService.printConfig();
        
        // Tester si la configuration est valide
        boolean isValid = configService.isConfigValid();
        System.out.println("Configuration valide: " + isValid);
        
        if (!isValid) {
            System.out.println("\n⚠️  La configuration n'est pas valide !");
            System.out.println("Le mot de passe par défaut 'qhft digu dfpi risg' doit être remplacé");
            System.out.println("Pour résoudre ce problème :");
            System.out.println("1. Lancez l'application");
            System.out.println("2. Allez dans Configuration → Paramètres Email");
            System.out.println("3. Entrez un email Gmail valide");
            System.out.println("4. Entrez un mot de passe d'application Gmail");
            System.out.println("5. Cliquez sur 'Tester la connexion'");
            System.out.println("6. Si le test réussit, cliquez sur 'Sauvegarder'");
        } else {
            System.out.println("\n✅ Configuration email valide !");
        }
        
        // Tester le service email
        EmailService emailService = new EmailService();
        System.out.println("Service email configuration valide: " + emailService.isConfigurationValid());
        
        // Tester la connexion SMTP
        System.out.println("Test de connexion SMTP: " + emailService.testConnection());
    }
}
