package com.PlaNova.services;

import com.PlaNova.models.TransportPrive;
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
        props.put("mail.mime.charset", "utf-8");
        props.put("mail.mime.multipart.allowempty", "true");
        return props;
    }

    public boolean sendLocationConfirmation(String clientEmail, TransportPrive transport, LocalDate dateLocation) {
        if (!configService.isConfigValid()) {
            System.err.println("Configuration email invalide.");
            return false;
        }

        try {
            Session session = createSession();
            Message message = createMessage(session, clientEmail, transport, dateLocation);
            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            return false;
        }
    }

    private Session createSession() {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        configService.getEmailUsername(),
                        configService.getEmailPassword());
            }
        });
    }

    private Message createMessage(Session session, String clientEmail, TransportPrive transport, LocalDate dateLocation)
            throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(configService.getFromEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(clientEmail));
        message.setSubject("Confirmation de Location - PlaNova Transport");

        String htmlContent = buildEmailContent(transport, dateLocation);
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        return message;
    }

    private String buildEmailContent(TransportPrive transport, LocalDate dateLocation) {
        String formattedDate = dateLocation
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH));
        return "<html><body><h2>Confirmation de Location</h2>" +
                "<p>Vous avez loué: " + transport.getMarque() + "</p>" +
                "<p>Prix: " + transport.getPrix_lac() + " DT</p>" +
                "<p>Date: " + formattedDate + "</p></body></html>";
    }
}
