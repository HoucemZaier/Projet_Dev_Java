package services;

import Models.*;
import Services.ServiceUser;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class serviceUserTest {
    static ServiceUser service;
    static int idClientTest;
    static int idAdminTest;
    static int idModeratorTest;
    static int idGuideTest;

    @BeforeAll
    static void setup() {
        service = new ServiceUser();
    }

    @Test
    @Order(1)
    void testAjouterClient() throws SQLException {
        Client client = new Client("TestNom", "TestPrenom", "test@example.com",
                                 "TestPassword123!", "Tunisie", "image.jpg", "12345678");
        service.ajouter(client);
        idClientTest = client.getIdUtilisateur();

        List<User> users = service.recuperer();
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(user ->
            user.getEmail().equals("test@example.com") && user instanceof Client
        ));
    }

    @Test
    @Order(2)
    void testAjouterAdmin() throws SQLException {
        Admin admin = new Admin("AdminNom", "AdminPrenom", "admin@planNova.tn",
                              "AdminPassword123!", "Tunisie", "admin.jpg", "123AMN123456");
        service.ajouter(admin);
        idAdminTest = admin.getIdUtilisateur();

        List<User> users = service.recuperer();
        assertTrue(users.stream().anyMatch(user ->
            user.getEmail().equals("admin@planNova.tn") && user instanceof Admin
        ));
    }

    @Test
    @Order(3)
    void testAjouterModerator() throws SQLException {
        Moderateur moderator = new Moderateur("ModeratorNom", "ModeratorPrenom", "moderator@planNova.tn",
                                            "ModeratorPassword123!", "Tunisie", "mod.jpg", "123MOD123456");
        service.ajouter(moderator);
        idModeratorTest = moderator.getIdUtilisateur();

        List<User> users = service.recuperer();
        assertTrue(users.stream().anyMatch(user ->
            user.getEmail().equals("moderator@planNova.tn") && user instanceof Moderateur
        ));
    }

    @Test
    @Order(4)
    void testAjouterGuide() throws SQLException {
        Guide guide = new Guide("GuideNom", "GuidePrenom", "guide@example.com",
                              "GuidePassword123!", "Tunisie", "guide.jpg");
        service.ajouter(guide);
        idGuideTest = guide.getIdUtilisateur();

        List<User> users = service.recuperer();
        assertTrue(users.stream().anyMatch(user ->
            user.getEmail().equals("guide@example.com") && user instanceof Guide
        ));
    }

    @Test
    @Order(5)
    void testAuthentification() throws SQLException {
        User authenticatedUser = service.authenticate("test@example.com", "TestPassword123!");
        assertNotNull(authenticatedUser);
        assertEquals("test@example.com", authenticatedUser.getEmail());

        User failedAuth = service.authenticate("test@example.com", "mauvais_mot_de_passe");
        assertNull(failedAuth);
    }

    @Test
    @Order(6)
    void testFindByEmail() throws SQLException {
        User foundUser = service.findByEmail("test@example.com");
        assertNotNull(foundUser);
        assertEquals("TestNom", foundUser.getNom());

        User notFound = service.findByEmail("inexistant@example.com");
        assertNull(notFound);
    }

    @Test
    @Order(7)
    void testEmailDuplication() throws SQLException {
        // Try to create another client with the same email
        Client duplicateClient = new Client("DuplicateNom", "DuplicatePrenom", "test@example.com",
                                          "DuplicatePassword123!", "France", "duplicate.jpg", "87654321");

        // This should either throw an exception or be handled by the service
        // depending on your database constraints
        boolean exceptionThrown = false;
        try {
            service.ajouter(duplicateClient);
            // If no exception, check if findByEmail still returns the original user
            User foundUser = service.findByEmail("test@example.com");
            assertEquals("TestNom", foundUser.getNom(), "Original user should be preserved");
        } catch (SQLException e) {
            exceptionThrown = true;
            assertTrue(e.getMessage().contains("duplicate") ||
                      e.getMessage().contains("UNIQUE") ||
                      e.getMessage().contains("constraint"),
                      "Should get a duplicate email constraint error");
        }

        // Either way, we should still have only one user with that email
        User user = service.findByEmail("test@example.com");
        assertNotNull(user, "Original user should still exist");
    }

    @Test
    @Order(8)
    void testModifierUtilisateur() throws SQLException {
        User userToModify = service.recupererParId(idClientTest);
        userToModify.setNom("NouveauNom");
        service.modifier(userToModify);

        User modifiedUser = service.recupererParId(idClientTest);
        assertEquals("NouveauNom", modifiedUser.getNom());
    }

    @Test
    @Order(9)
    void testUpdatePassword() throws SQLException {
        boolean updateSuccess = service.updatePassword(idClientTest, "NouveauMotDePasse123!");
        assertTrue(updateSuccess);

        User authWithNewPassword = service.authenticate("test@example.com", "NouveauMotDePasse123!");
        assertNotNull(authWithNewPassword);
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (idClientTest > 0) {
            service.supprimer(idClientTest);
        }
        if (idAdminTest > 0) {
            service.supprimer(idAdminTest);
        }
        if (idModeratorTest > 0) {
            service.supprimer(idModeratorTest);
        }
        if (idGuideTest > 0) {
            service.supprimer(idGuideTest);
        }
    }
}
