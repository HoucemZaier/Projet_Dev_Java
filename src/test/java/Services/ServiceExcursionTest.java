package Services;

import Models.Excursion;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceExcursionTest {

    static ServiceExcursion service;
    static Excursion excursionTest;

    @BeforeAll
    static void setup() {
        service = new ServiceExcursion();
    }

    @Test
    @Order(1)
    void testAjouterExcursion() throws SQLException {
        // Création de l'excursion de test
        excursionTest = new Excursion(0, "Excursion Plage", "Hammamet",
                Date.valueOf("2026-03-01"), Date.valueOf("2026-03-03"),
                250.0, 30, "ouverte");

        // Ajout via le service
        service.ajouter(excursionTest);

        // Récupération de toutes les excursions
        List<Excursion> excursions = service.recuperer();

        // Assertions
        assertFalse(excursions.isEmpty(), "La liste ne doit pas être vide après ajout");
        assertTrue(
                excursions.stream().anyMatch(ex -> ex.getTitre().equals("Excursion Plage")),
                "L'excursion ajoutée doit exister dans la liste"
        );
    }


}