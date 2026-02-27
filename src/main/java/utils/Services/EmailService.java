package utils.Services;

import Models.TransportPrive;
import utils.Services.EmailConfigService;

import javax.mail.*;
import javax.mail.internet.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    private final EmailConfigService configService;
    private final Properties properties;

    public EmailService() {
        this.configService = new EmailConfigService();
        this.properties = createProperties();
    }

    private Properties createProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", configService.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(configService.getSmtpPort()));
        props.put("mail.smtp.ssl.trust", configService.getSmtpHost());
        // Éviter les problèmes de ContentHandler
        props.put("mail.mime.charset", "utf-8");
        props.put("mail.mime.multipart.allowempty", "true");
        return props;
    }

    /**
     * Envoie un email de confirmation de location
     */
    public boolean sendLocationConfirmation(String clientEmail, TransportPrive transport, LocalDate dateLocation) {
        if (!configService.isConfigValid()) {
            System.err.println("Configuration email invalide. Veuillez configurer les identifiants email.");
            return false;
        }

        try {
            Session session = createSession();
            Message message = createMessage(session, clientEmail, transport, dateLocation);
            
            // Envoyer l'email
            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + clientEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Session createSession() {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    configService.getEmailUsername(), 
                    configService.getEmailPassword()
                );
            }
        });
    }

    private Message createMessage(Session session, String clientEmail, TransportPrive transport, LocalDate dateLocation) 
            throws MessagingException {
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(configService.getFromEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(clientEmail));
        
        // Sujet de l'email
        message.setSubject("Confirmation de Location - PlaNova Transport");
        
        // Contenu HTML de l'email
        String htmlContent = buildEmailContent(transport, dateLocation);
        
        // Créer le contenu avec MimeMultipart pour éviter les erreurs
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
        
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        
        message.setContent(multipart);
        
        return message;
    }

    private String buildEmailContent(TransportPrive transport, LocalDate dateLocation) {
        String formattedDate = dateLocation.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH));
        
        return "<!DOCTYPE html>" +
                "<html lang='fr'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Confirmation de Location - PlaNova</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                "        .container { max-width: 600px; margin: 20px auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }" +
                "        .header { background-color: #667eea; color: white; padding: 30px; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: 300; }" +
                "        .header p { margin: 10px 0 0 0; opacity: 0.9; }" +
                "        .content { padding: 40px 30px; }" +
                "        .vehicle-info { background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; border-radius: 5px; }" +
                "        .vehicle-info h3 { color: #2d3748; margin: 0 0 15px 0; font-size: 20px; }" +
                "        .info-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #e2e8f0; }" +
                "        .info-row:last-child { border-bottom: none; }" +
                "        .info-label { color: #718096; font-weight: 500; }" +
                "        .info-value { color: #2d3748; font-weight: bold; }" +
                "        .confirmation { background-color: #e6fffa; border: 1px solid #38b2ac; padding: 20px; margin: 25px 0; border-radius: 8px; text-align: center; }" +
                "        .confirmation h4 { color: #2c7a7b; margin: 0 0 10px 0; font-size: 18px; }" +
                "        .confirmation p { color: #2d3748; margin: 0; font-size: 16px; }" +
                "        .footer { background-color: #2d3748; color: white; text-align: center; padding: 20px; font-size: 12px; }" +
                "        .logo { font-size: 24px; margin-bottom: 10px; }" +
                "        .highlight { color: #667eea; font-weight: bold; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <div class='logo'>🚗 PlaNova Transport</div>" +
                "            <h1>Confirmation de Location</h1>" +
                "            <p>Merci pour votre confiance</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2>🎉 Location Confirmée !</h2>" +
                "            <p>Nous sommes ravis de vous informer que votre location de véhicule a été confirmée avec succès.</p>" +
                "            <div class='vehicle-info'>" +
                "                <h3>🚗 Informations du Véhicule</h3>" +
                "                <div class='info-row'>" +
                "                    <span class='info-label'>Marque:</span>" +
                "                    <span class='info-value'>" + transport.getMarque() + "</span>" +
                "                </div>" +
                "                <div class='info-row'>" +
                "                    <span class='info-label'>Prix de location:</span>" +
                "                    <span class='info-value highlight'>" + String.format("%.2f DT", transport.getPrix_loc()) + "</span>" +
                "                </div>" +
                "                <div class='info-row'>" +
                "                    <span class='info-label'>Date de location:</span>" +
                "                    <span class='info-value'>" + formattedDate + "</span>" +
                "                </div>" +
                "                <div class='info-row'>" +
                "                    <span class='info-label'>État:</span>" +
                "                    <span class='info-value'>Indisponible</span>" +
                "                </div>" +
                "            </div>" +
                "            <div class='confirmation'>" +
                "                <h4>✅ Location Confirmée</h4>" +
                "                <p>Vous avez loué la voiture <strong>" + transport.getMarque() + "</strong> au prix de <strong>" + 
                String.format("%.2f DT", transport.getPrix_loc()) + "</strong></p>" +
                "                <p><em>Date de location: " + formattedDate + "</em></p>" +
                "            </div>" +
                "            <h3>📋 Prochaines étapes</h3>" +
                "            <ul>" +
                "                <li>Présentez-vous à notre agence avec une pièce d'identité valide</li>" +
                "                <li>Le paiement s'effectuera au moment de la récupération du véhicule</li>" +
                "                <li>Une vérification de l'état du véhicule sera effectuée</li>" +
                "            </ul>" +
                "            <p><strong>Pour toute question, contactez-nous:</strong></p>" +
                "            <p>📧 Email: contact@planova.tn<br>" +
                "           🕐 Horaires: Lun-Ven 8h-18h, Sam 9h-13h</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <div class='logo'>🚗 PlaNova Transport</div>" +
                "            <p>Votre partenaire de confiance pour toutes vos locations de véhicules</p>" +
                "            <p>&copy; 2026 PlaNova Transport. All rights reserved.</p>" +
                "            <p><em>All right reserved by PlaNova</em></p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Teste la connexion au serveur email
     */
    public boolean testConnection() {
        if (!configService.isConfigValid()) {
            return false;
        }

        try {
            Session session = createSession();
            Transport transport = session.getTransport("smtp");
            transport.connect(
                configService.getSmtpHost(), 
                configService.getEmailUsername(), 
                configService.getEmailPassword()
            );
            transport.close();
            return true;
        } catch (Exception e) {
            System.err.println("Erreur de connexion email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si la configuration email est valide
     */
    public boolean isConfigurationValid() {
        return configService.isConfigValid();
    }
}
