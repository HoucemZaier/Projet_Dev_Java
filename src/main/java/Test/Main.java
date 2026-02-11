package Test;

import Models.*;
import Services.ServiceUser;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ServiceUser serviceUser = new ServiceUser();

        System.out.println("========================================");
        System.out.println("   TEST GESTION DES UTILISATEURS");
        System.out.println("========================================\n");

        try {
            // ==================== TEST 1: AJOUTER UN ADMIN ====================
            System.out.println(">>> TEST 1: AJOUT D'UN ADMIN");
            Admin admin = new Admin("Dupont", "Jean", "jean.dupont@email.com",
                    "password123", "France", "jean.jpg", "ADM001");
            serviceUser.ajouter(admin);
            System.out.println("Admin ajoute avec ID: " + admin.getIdUtilisateur());
            System.out.println();

            // ==================== TEST 2: AJOUTER UN CLIENT ====================
            System.out.println(">>> TEST 2: AJOUT D'UN CLIENT");
            Client client = new Client("Martin", "Sophie", "sophie.martin@email.com",
                    "password123", "Tunisie", "sophie.jpg", "12345678");
            serviceUser.ajouter(client);
            System.out.println("Client ajoute avec ID: " + client.getIdUtilisateur());
            System.out.println();

            // ==================== TEST 3: AJOUTER UN GUIDE ====================
            System.out.println(">>> TEST 3: AJOUT D'UN GUIDE");
            Guide guide = new Guide("Bernard", "Pierre", "pierre.bernard@email.com",
                    "password123", "France", "pierre.jpg");
            serviceUser.ajouter(guide);
            System.out.println("Guide ajoute avec ID: " + guide.getIdUtilisateur());
            System.out.println();

            // ==================== TEST 4: AJOUTER UN MODERATEUR ====================
            System.out.println(">>> TEST 4: AJOUT D'UN MODERATEUR");
            Moderateur moderateur = new Moderateur("Leroy", "Marie", "marie.leroy@email.com",
                    "password123", "Belgique", "marie.jpg");
            serviceUser.ajouter(moderateur);
            System.out.println("Moderateur ajoute avec ID: " + moderateur.getIdUtilisateur());
            System.out.println();

            // ==================== TEST 5: AFFICHER TOUS LES UTILISATEURS ====================
            System.out.println(">>> TEST 5: LISTE DE TOUS LES UTILISATEURS");
            List<User> users = serviceUser.recuperer();
            System.out.println("Nombre total: " + users.size());
            for (User u : users) {
                System.out.println("  - " + u.getNom() + " " + u.getPrenom());
            }
            System.out.println();

            // ==================== TEST 6: RÉCUPÉRER PAR ID ====================
            System.out.println(">>> TEST 6: RECUPERATION PAR ID");
            User userById = serviceUser.recupererParId(1);
            if (userById != null) {
                System.out.println("Utilisateur trouve: " + userById.getNom() + " " + userById.getPrenom());
            } else {
                System.out.println("Aucun utilisateur trouve");
            }
            System.out.println();

            // ==================== TEST 7: AUTHENTIFICATION ====================
            System.out.println(">>> TEST 7: AUTHENTIFICATION");
            User userConnecte = serviceUser.authenticate("jean.dupont@email.com", "password123");
            if (userConnecte != null) {
                System.out.println("Connexion reussie!");
                System.out.println("Nom: " + userConnecte.getNom() + " " + userConnecte.getPrenom());
            } else {
                System.out.println("Echec de connexion");
            }
            System.out.println();

            // ==================== TEST 8: RÉCUPÉRER PAR TYPE ====================
            System.out.println(">>> TEST 8: LISTE DES CLIENTS");
            List<User> clients = serviceUser.recupererParType("client");
            System.out.println("Nombre de clients: " + clients.size());
            for (User c : clients) {
                System.out.println("  - " + c.getNom() + " " + c.getPrenom());
            }
            System.out.println();

            System.out.println(">>> LISTE DES GUIDES");
            List<User> guides = serviceUser.recupererParType("guide");
            System.out.println("Nombre de guides: " + guides.size());
            for (User g : guides) {
                System.out.println("  - " + g.getNom() + " " + g.getPrenom());
            }
            System.out.println();

            // ==================== TEST 9: MODIFIER UN UTILISATEUR ====================
            System.out.println(">>> TEST 9: MODIFICATION");
            if (userConnecte != null) {
                userConnecte.setPays("Suisse");
                serviceUser.modifier(userConnecte);
                System.out.println("Utilisateur modifie avec succes");
            }
            System.out.println();

            // ==================== TEST 10: SUPPRIMER UN UTILISATEUR ====================
            System.out.println(">>> TEST 10: SUPPRESSION");
            System.out.println("Test de suppression desactive");
            // serviceUser.supprimer(guide.getIdUtilisateur());
            System.out.println();

            System.out.println("========================================");
            System.out.println("TOUS LES TESTS TERMINES");
            System.out.println("========================================");

        } catch (SQLException e) {
            System.err.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}