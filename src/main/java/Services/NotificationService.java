package Services;

import Models.User;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple notification service that stores notifications in files
 * This avoids the need for additional database tables
 */
public class NotificationService {

    private static final String NOTIFICATIONS_DIR = "data/notifications/";
    private static final String ADMIN_NOTIFICATIONS_FILE = NOTIFICATIONS_DIR + "admin_notifications.txt";

    public NotificationService() {
        createNotificationsDirectory();
    }

    /**
     * Send a guide application notification to admin
     */
    public void sendGuideApplicationNotification(User client, String cvLink) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String notification = String.format(
                "[%s] 🎯 NOUVELLE DEMANDE GUIDE\n" +
                "Client: %s %s (ID: %d)\n" +
                "Email: %s\n" +
                "CV: %s\n" +
                "Status: EN_ATTENTE\n" +
                "---\n",
                timestamp,
                client.getPrenom(),
                client.getNom(),
                client.getIdUtilisateur(),
                client.getEmail(),
                cvLink
            );

            // Append to admin notifications file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ADMIN_NOTIFICATIONS_FILE, true))) {
                writer.write(notification);
                writer.newLine();
            }

            System.out.println("📢 Notification envoyée aux administrateurs pour: " + client.getPrenom() + " " + client.getNom());

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
    }

    /**
     * Get count of unread notifications for admin dashboard
     */
    public int getUnreadNotificationsCount() {
        try {
            List<String> applications = getPendingGuideApplications();
            return applications.size();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du calcul des notifications: " + e.getMessage());
            return 0; // No notifications on error
        }
    }

    /**
     * Get all pending guide applications for admin review
     */
    public List<String> getPendingGuideApplications() {
        List<String> applications = new ArrayList<>();
        try {
            List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(ADMIN_NOTIFICATIONS_FILE));
            StringBuilder currentApp = new StringBuilder();

            for (String line : lines) {
                if (line.startsWith("[")) {
                    // Start of new notification
                    if (currentApp.length() > 0 &&
                        currentApp.toString().contains("EN_ATTENTE") &&
                        !currentApp.toString().contains("STATUS: TRAITÉ")) {
                        applications.add(currentApp.toString());
                    }
                    currentApp = new StringBuilder();
                }
                currentApp.append(line).append("\n");
            }

            // Don't forget the last one
            if (currentApp.length() > 0 &&
                currentApp.toString().contains("EN_ATTENTE") &&
                !currentApp.toString().contains("STATUS: TRAITÉ")) {
                applications.add(currentApp.toString());
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la lecture des notifications: " + e.getMessage());
        }

        return applications;
    }

    /**
     * Mark a guide application as approved or rejected and COMPLETELY REMOVE from pending list
     */
    public void updateApplicationStatus(String clientEmail, String newStatus, String adminNote) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(ADMIN_NOTIFICATIONS_FILE);

            if (!java.nio.file.Files.exists(filePath)) {
                System.out.println("⚠️ Fichier de notifications non trouvé: " + ADMIN_NOTIFICATIONS_FILE);
                return;
            }

            List<String> lines = java.nio.file.Files.readAllLines(filePath);
            List<String> updatedLines = new ArrayList<>();
            boolean applicationProcessed = false;
            boolean inTargetNotification = false;
            String currentNotificationStart = "";

            System.out.println("📂 Traitement du fichier avec " + lines.size() + " lignes pour email: " + clientEmail);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                // Détection du début d'une nouvelle notification
                if (line.startsWith("[")) {
                    currentNotificationStart = line;
                    inTargetNotification = false;

                    // Regarder en avant pour voir si cette notification contient l'email du client
                    boolean isTargetNotification = false;
                    for (int j = i + 1; j < lines.size() && !lines.get(j).startsWith("[") && !lines.get(j).equals("---"); j++) {
                        if (lines.get(j).contains(clientEmail) && lines.get(j).contains("Email:")) {
                            // Vérifier aussi que c'est bien une demande en attente
                            for (int k = i; k < Math.min(lines.size(), j + 5); k++) {
                                if (lines.get(k).contains("Status: EN_ATTENTE")) {
                                    isTargetNotification = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }

                    if (isTargetNotification) {
                        System.out.println("🗑️ Notification trouvée à supprimer: " + currentNotificationStart);
                        inTargetNotification = true;
                        applicationProcessed = true;

                        // Créer notification client
                        if ("REJETÉ".equals(newStatus)) {
                            createClientNotification(clientEmail, "DEMANDE_GUIDE_REJETÉE", adminNote);
                        } else if ("APPROUVÉ".equals(newStatus)) {
                            createClientNotification(clientEmail, "DEMANDE_GUIDE_APPROUVÉE", adminNote);
                        }

                        // Ne pas ajouter cette ligne ni les suivantes jusqu'à "---"
                        continue;
                    }
                }

                // Si on est dans la notification à supprimer, ignorer toutes les lignes jusqu'à "---"
                if (inTargetNotification) {
                    if (line.equals("---")) {
                        inTargetNotification = false; // Fin de la notification à supprimer
                        // Ne pas ajouter la ligne "---" non plus
                        continue;
                    }
                    // Ignorer toutes les lignes de la notification
                    continue;
                }

                // Ajouter les lignes qui ne font pas partie de la notification supprimée
                updatedLines.add(line);
            }

            // Écrire le fichier mis à jour
            java.nio.file.Files.write(filePath, updatedLines, java.nio.charset.StandardCharsets.UTF_8);

            if (applicationProcessed) {
                System.out.println("✅ Notification SUPPRIMÉE avec succès pour: " + clientEmail + " -> " + newStatus);
                System.out.println("📊 Fichier mis à jour: " + updatedLines.size() + " lignes restantes");
            } else {
                System.out.println("⚠️ Aucune notification trouvée pour: " + clientEmail);
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create notification for client about their guide application status
     */
    private void createClientNotification(String clientEmail, String type, String adminNote) {
        try {
            String clientNotificationsFile = NOTIFICATIONS_DIR + "client_notifications.txt";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String message = "";
            String icon = "";

            if ("DEMANDE_GUIDE_REJETÉE".equals(type)) {
                icon = "❌";
                message = String.format(
                    "[%s] %s DEMANDE GUIDE REJETÉE\n" +
                    "Email: %s\n" +
                    "Status: Votre demande pour devenir guide a été rejetée\n" +
                    "Raison: %s\n" +
                    "Vous pouvez soumettre une nouvelle demande après avoir corrigé les points mentionnés.\n" +
                    "---\n",
                    timestamp, icon, clientEmail, adminNote
                );
            } else if ("DEMANDE_GUIDE_APPROUVÉE".equals(type)) {
                icon = "✅";
                message = String.format(
                    "[%s] %s DEMANDE GUIDE APPROUVÉE\n" +
                    "Email: %s\n" +
                    "Status: Félicitations! Votre demande pour devenir guide a été approuvée\n" +
                    "Note: %s\n" +
                    "Votre compte a été converti en compte Guide. Vous pouvez maintenant accéder aux fonctionnalités de guide.\n" +
                    "---\n",
                    timestamp, icon, clientEmail, adminNote
                );
            }

            // Append to client notifications file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(clientNotificationsFile, true))) {
                writer.write(message);
                writer.newLine();
            }

            System.out.println("📬 Notification client créée pour: " + clientEmail);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la création de notification client: " + e.getMessage());
        }
    }

    /**
     * Get pending notifications for a specific client email
     */
    public List<String> getClientNotifications(String clientEmail) {
        List<String> clientNotifications = new ArrayList<>();
        try {
            String clientNotificationsFile = NOTIFICATIONS_DIR + "client_notifications.txt";
            java.nio.file.Path filePath = java.nio.file.Paths.get(clientNotificationsFile);

            if (!java.nio.file.Files.exists(filePath)) {
                return clientNotifications; // Return empty list if file doesn't exist
            }

            List<String> lines = java.nio.file.Files.readAllLines(filePath);
            StringBuilder currentNotification = new StringBuilder();

            for (String line : lines) {
                if (line.startsWith("[")) {
                    // Start of new notification
                    if (currentNotification.length() > 0 && currentNotification.toString().contains("Email: " + clientEmail)) {
                        clientNotifications.add(currentNotification.toString());
                    }
                    currentNotification = new StringBuilder();
                }
                currentNotification.append(line).append("\n");
            }

            // Don't forget the last one
            if (currentNotification.length() > 0 && currentNotification.toString().contains("Email: " + clientEmail)) {
                clientNotifications.add(currentNotification.toString());
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la lecture des notifications client: " + e.getMessage());
        }

        return clientNotifications;
    }

    /**
     * Mark client notification as read
     */
    public void markClientNotificationAsRead(String clientEmail, String notificationContent) {
        try {
            String clientNotificationsFile = NOTIFICATIONS_DIR + "client_notifications.txt";
            java.nio.file.Path filePath = java.nio.file.Paths.get(clientNotificationsFile);

            if (!java.nio.file.Files.exists(filePath)) {
                return;
            }

            List<String> lines = java.nio.file.Files.readAllLines(filePath);
            List<String> updatedLines = new ArrayList<>();
            boolean inTargetNotification = false;

            for (String line : lines) {
                if (line.startsWith("[") && inTargetNotification) {
                    inTargetNotification = false;
                }

                if (line.startsWith("[") && !inTargetNotification) {
                    // Check if this is the notification we want to mark as read
                    if (notificationContent.contains(line)) {
                        inTargetNotification = true;
                        updatedLines.add(line + " [LU]");
                    } else {
                        updatedLines.add(line);
                    }
                } else if (inTargetNotification && line.equals("---")) {
                    updatedLines.add("Lu le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    updatedLines.add(line);
                    inTargetNotification = false;
                } else {
                    updatedLines.add(line);
                }
            }

            java.nio.file.Files.write(filePath, updatedLines);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du marquage de notification comme lue: " + e.getMessage());
        }
    }

    /**
     * Create notifications directory if it doesn't exist
     */
    private void createNotificationsDirectory() {
        try {
            java.nio.file.Path notificationsPath = java.nio.file.Paths.get(NOTIFICATIONS_DIR);
            if (!java.nio.file.Files.exists(notificationsPath)) {
                java.nio.file.Files.createDirectories(notificationsPath);
                System.out.println("📁 Dossier de notifications créé: " + NOTIFICATIONS_DIR);
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la création du dossier de notifications: " + e.getMessage());
        }
    }

    /**
     * Check if a client has a pending guide application
     */
    public boolean hasClientPendingGuideApplication(String clientEmail) {
        try {
            List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(ADMIN_NOTIFICATIONS_FILE));
            StringBuilder currentNotification = new StringBuilder();

            for (String line : lines) {
                if (line.startsWith("[")) {
                    // Check previous notification
                    if (currentNotification.length() > 0) {
                        String notification = currentNotification.toString();
                        if (notification.contains(clientEmail) &&
                            notification.contains("EN_ATTENTE") &&
                            notification.contains("NOUVELLE DEMANDE GUIDE")) {
                            return true;
                        }
                    }
                    currentNotification = new StringBuilder();
                }
                currentNotification.append(line).append("\n");
            }

            // Check last notification
            if (currentNotification.length() > 0) {
                String notification = currentNotification.toString();
                if (notification.contains(clientEmail) &&
                    notification.contains("EN_ATTENTE") &&
                    notification.contains("NOUVELLE DEMANDE GUIDE")) {
                    return true;
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la vérification des demandes en attente: " + e.getMessage());
        }

        return false;
    }

    /**
     * Clear all notifications (for testing)
     */
    public void clearAllNotifications() {
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(ADMIN_NOTIFICATIONS_FILE));
            System.out.println("🧹 Notifications supprimées");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la suppression: " + e.getMessage());
        }
    }

    /**
     * Debug method to print notification file content
     */
    public void debugPrintNotifications() {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(ADMIN_NOTIFICATIONS_FILE);
            if (!java.nio.file.Files.exists(filePath)) {
                System.out.println("�� DEBUG: Fichier de notifications n'existe pas: " + ADMIN_NOTIFICATIONS_FILE);
                return;
            }

            List<String> lines = java.nio.file.Files.readAllLines(filePath);
            System.out.println("🔍 DEBUG: Contenu du fichier de notifications (" + lines.size() + " lignes):");
            System.out.println("═".repeat(50));
            for (int i = 0; i < lines.size(); i++) {
                System.out.printf("%3d: %s\n", i + 1, lines.get(i));
            }
            System.out.println("═".repeat(50));

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la lecture pour débogage: " + e.getMessage());
        }
    }

    /**
     * Send cloud storage notification to admin
     */
    public void sendCloudStorageNotification(User user, String fileName, String cloudLink) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String notification = String.format(
                "[%s] ☁️ NOUVEAU FICHIER CLOUD\n" +
                "Utilisateur: %s %s (ID: %d)\n" +
                "Email: %s\n" +
                "Fichier: %s\n" +
                "Lien: %s\n" +
                "Status: NOUVEAU\n" +
                "---\n",
                timestamp,
                user.getPrenom(),
                user.getNom(),
                user.getIdUtilisateur(),
                user.getEmail(),
                fileName,
                cloudLink
            );

            // Append to admin notifications file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ADMIN_NOTIFICATIONS_FILE, true))) {
                writer.write(notification);
                writer.newLine();
            }

            System.out.println("☁️ Notification cloud storage envoyée pour: " + user.getPrenom() + " " + user.getNom());

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification cloud: " + e.getMessage());
        }
    }

    /**
     * Send general notification to admin
     */
    public void sendGeneralNotification(String title, String message) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String notification = String.format(
                "[%s] 📢 %s\n" +
                "Message: %s\n" +
                "Status: NOUVEAU\n" +
                "---\n",
                timestamp,
                title,
                message
            );

            // Append to admin notifications file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ADMIN_NOTIFICATIONS_FILE, true))) {
                writer.write(notification);
                writer.newLine();
            }

            System.out.println("📢 Notification générale envoyée: " + title);

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification générale: " + e.getMessage());
        }
    }
}
