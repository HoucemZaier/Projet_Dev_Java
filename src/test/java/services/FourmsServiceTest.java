package services;

import Modeles.Fourms;
import Services.FourmsService;
import Utils.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.Connection;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class FourmsServiceTest {

    static FourmsService service;
    static Connection cnx;

    @BeforeAll
    static void setup() {
        cnx = DataSource.getInstance().getConnection();
        service = new FourmsService();
    }

    @Test
    @Order(1)
    void testAjouter() {
        // Création d'un forum de test
        Fourms f = new Fourms(0, "Forum JUnit", 10, "Description test", 1); // idposte = 1 par défaut
        service.ajouter(f);

        // Vérifier qu'il a été ajouté
        List<Fourms> list = service.afficherTout();
        Assertions.assertFalse(list.isEmpty(), "La liste ne doit pas être vide après l'ajout.");
        System.out.println("✅ Forum ajouté, total forums : " + list.size());
    }

    @Test
    @Order(2)
    void testAfficherTout() {
        List<Fourms> list = service.afficherTout();
        Assertions.assertTrue(list.size() > 0, "La liste doit contenir au moins le forum ajouté précédemment.");
        System.out.println("✅ Nombre de forums récupérés : " + list.size());
    }

    @Test
    @Order(3)
    void testModifier() {
        List<Fourms> list = service.afficherTout();
        Fourms f = list.get(list.size() - 1); // prendre le dernier ajouté

        String nouveauNom = "Forum Modifié JUnit";
        f.setNom(nouveauNom);
        f.setNbparticipant(20);
        f.setCommentaire("Description modifiée");

        service.modifier(f);

        // Vérification
        List<Fourms> updatedList = service.afficherTout();
        Fourms updatedForum = updatedList.stream()
                .filter(forums -> forums.getId_forum() == f.getId_forum())
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(updatedForum);
        Assertions.assertEquals(nouveauNom, updatedForum.getNom(), "Le nom du forum doit être mis à jour.");
        Assertions.assertEquals(20, updatedForum.getNbparticipant(), "Le nombre de participants doit être mis à jour.");
        System.out.println("✅ Forum modifié avec succès !");
    }

    @Test
    @Order(4)
    void testSupprimer() {
        List<Fourms> list = service.afficherTout();
        Assertions.assertFalse(list.isEmpty(), "Il doit y avoir un forum à supprimer.");

        int idASupprimer = list.get(list.size() - 1).getId_forum();
        service.supprimer(idASupprimer);

        List<Fourms> afterDelete = service.afficherTout();
        boolean exists = afterDelete.stream().anyMatch(f -> f.getId_forum() == idASupprimer);

        Assertions.assertFalse(exists, "Le forum doit être absent après suppression.");
        System.out.println("✅ Forum supprimé avec succès !");
    }
}
