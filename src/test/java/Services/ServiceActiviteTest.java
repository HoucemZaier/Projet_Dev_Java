package Services;

import Models.Activite;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceActiviteTest {

    static ServiceActivite service;

    @BeforeAll
    static void setup() {
        service = new ServiceActivite();
    }

    @Test
    @Order(1)
    void testAjouterActivite() {
        try {
            // Création d'une activité de test
            Activite activiteTest = new Activite(
                    0,                              // id_activite (auto-incrémenté)
                    "Natation",                     // nom
                    "Cours de natation pour débutants", // description
                    Date.valueOf("2026-03-01"),     // date_activite
                    java.sql.Time.valueOf("09:00:00"), // heure_activite
                    "Plage Nord",                   // lieu
                    50.0,                           // prix
                    1,                              // id_excursion (doit exister dans la table Excursion)
                    1                               // id_destination (doit exister dans la table Destination)
            );

            // Ajout via le service
            service.ajouter(activiteTest);

            // Récupération de toutes les activités
            List<Activite> activites = service.recuperer();

            // Assertions
            assertFalse(activites.isEmpty(), "La liste ne doit pas être vide après ajout");
            assertTrue(
                    activites.stream().anyMatch(a -> a.getNom().equals("Natation")),
                    "L'activité ajoutée doit exister dans la liste"
            );

        } catch (SQLException e) {
            e.printStackTrace();
            Assertions.fail("Erreur SQL lors de l'ajout de l'activité : " + e.getMessage());
        }
    }
}
