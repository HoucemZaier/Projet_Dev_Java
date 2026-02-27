package Services;

import Models.Excursion;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class EmailService {
    public static void sendExcursionEmail(String destinataire, String htmlContent) throws MessagingException {
        final String emailExpediteur = "saida.dridi18@gmail.com";
        final String motDePasse = "hmgx vjoj hsir pqgy";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailExpediteur, motDePasse);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailExpediteur));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
        message.setSubject("Nouvelle Excursion Disponible ✈️");

        // Important : mettre le contenu en HTML
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        Transport.send(message);
    }

}