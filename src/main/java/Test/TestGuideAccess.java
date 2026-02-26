package Test;

import Models.Guide;
import Models.Client;
import utils.UserSession;

public class TestGuideAccess {
    public static void main(String[] args) {
        System.out.println("🧪 Test d'accès au dashboard pour les guides");

        // Test avec un Client
        Client client = new Client("TestNom", "TestPrenom", "client@test.com", "password", "Tunisie", "");
        UserSession.getInstance().setCurrentUser(client);

        System.out.println("\n👤 Utilisateur Client:");
        System.out.println("Type: " + UserSession.getInstance().getCurrentUserType());
        System.out.println("isClient(): " + UserSession.getInstance().isClient());
        System.out.println("canAccessDashboard(): " + UserSession.getInstance().canAccessDashboard());

        // Test avec un Guide
        Guide guide = new Guide("GuideNom", "GuidePrenom", "guide@test.com", "password", "Tunisie", "");
        UserSession.getInstance().setCurrentUser(guide);

        System.out.println("\n🎯 Utilisateur Guide:");
        System.out.println("Type: " + UserSession.getInstance().getCurrentUserType());
        System.out.println("isGuide(): " + UserSession.getInstance().isGuide());
        System.out.println("canAccessDashboard(): " + UserSession.getInstance().canAccessDashboard());

        if (UserSession.getInstance().canAccessDashboard()) {
            System.out.println("✅ Le guide PEUT accéder au dashboard !");
        } else {
            System.out.println("❌ Le guide NE PEUT PAS accéder au dashboard !");
        }

        UserSession.getInstance().logout();
    }
}
