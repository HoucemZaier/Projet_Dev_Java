
package Test;

import Services.NotificationService;

public class TestNotificationDeletion {
    public static void main(String[] args) {
        NotificationService notificationService = new NotificationService();

        System.out.println("🧪 Test de suppression de notification");

        // Afficher le contenu actuel
        System.out.println("\n📋 Avant suppression:");
        notificationService.debugPrintNotifications();

        // Tester la suppression
        System.out.println("\n🗑️ Test de suppression pour ihebdhib22@gmail.com...");
        notificationService.updateApplicationStatus("ihebdhib22@gmail.com", "REJETÉ", "Test de suppression");

        // Afficher le contenu après
        System.out.println("\n📋 Après suppression:");
        notificationService.debugPrintNotifications();

        // Tester le comptage
        int count = notificationService.getUnreadNotificationsCount();
        System.out.println("\n📊 Nombre de notifications en attente: " + count);
    }
}
