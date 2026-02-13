package services;

import Modeles.Posts;
import Services.PostService;
import Utils.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.Connection;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class POSTSERVICETEST {

    static PostService service;
    static Connection cnx;

    @BeforeAll
    static void setup() {
        cnx = DataSource.getInstance().getConnection();
        service = new PostService();
    }

    @Test
    @Order(1)
    void testAjouter() {

        Posts p = new Posts(0, "Test JUnit", "Description du test", 99.99, "Technique", 1);
        service.ajouter(p);

        List<Posts> list = service.afficherTout();
        Assertions.assertFalse(list.isEmpty(), "La liste ne doit pas être vide après l'ajout d'un post.");
    }

    @Test
    @Order(2)
    void testAfficherTout() {
        List<Posts> list = service.afficherTout();
        Assertions.assertTrue(list.size() > 0, "La liste doit contenir au moins le post ajouté précédemment.");
        System.out.println("✅ Nombre de posts récupérés : " + list.size());
    }

    @Test
    @Order(3)
    void testModifier() {
        List<Posts> list = service.afficherTout();
        Posts p = list.get(list.size() - 1);

        String nouveauNom = "Titre Modifié JUnit";
        p.setNomPost(nouveauNom);
        p.setPrix(150.0);

        service.modifier(p);

        List<Posts> updatedList = service.afficherTout();
        Posts updatedPost = updatedList.stream()
                .filter(post -> post.getIdPost() == p.getIdPost())
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(updatedPost);
        Assertions.assertEquals(nouveauNom, updatedPost.getNomPost(), "Le nom du post doit être mis à jour en base de données.");
    }

    @Test
    @Order(4)
    void testSupprimer() {
        List<Posts> list = service.afficherTout();
        Assertions.assertFalse(list.isEmpty(), "Il doit y avoir un post à supprimer.");

        int idASupprimer = list.get(list.size() - 1).getIdPost();
        service.supprimer(idASupprimer);

        List<Posts> afterDelete = service.afficherTout();
        boolean exists = afterDelete.stream().anyMatch(p -> p.getIdPost() == idASupprimer);

        Assertions.assertFalse(exists, "Le post doit être absent de la liste après suppression.");
    }
}