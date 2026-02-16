package utils;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PasswordUtilsTest {

    @Test
    @Order(1)
    @DisplayName("Test hashage et vérification du mot de passe")
    void testPasswordHashingAndVerification() {
        String plainPassword = "TestPassword123!";

        // Hasher le mot de passe
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);

        // Vérifications
        assertNotNull(hashedPassword, "Le mot de passe hashé ne doit pas être null");
        assertNotEquals(plainPassword, hashedPassword, "Le mot de passe hashé doit être différent du mot de passe en clair");
        assertTrue(hashedPassword.length() > plainPassword.length(), "Le mot de passe hashé doit être plus long");

        // Vérifier que le mot de passe peut être vérifié
        assertTrue(PasswordUtils.verifyPassword(plainPassword, hashedPassword),
                  "La vérification du mot de passe correct doit réussir");
    }

    @Test
    @Order(2)
    @DisplayName("Test vérification avec mauvais mot de passe")
    void testPasswordVerificationWithWrongPassword() {
        String correctPassword = "TestPassword123!";
        String wrongPassword = "WrongPassword456@";

        String hashedPassword = PasswordUtils.hashPassword(correctPassword);

        // La vérification avec un mauvais mot de passe doit échouer
        assertFalse(PasswordUtils.verifyPassword(wrongPassword, hashedPassword),
                   "La vérification avec un mauvais mot de passe doit échouer");
    }

    @Test
    @Order(3)
    @DisplayName("Test avec mot de passe null")
    void testPasswordUtilsWithNull() {
        // Test hashage avec null
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtils.hashPassword(null);
        }, "Le hashage d'un mot de passe null doit lever une exception");

        // Test vérification avec mot de passe null
        String hashedPassword = PasswordUtils.hashPassword("test");
        assertFalse(PasswordUtils.verifyPassword(null, hashedPassword),
                   "La vérification avec un mot de passe null doit échouer");
    }

    @Test
    @Order(4)
    @DisplayName("Test avec hash null")
    void testPasswordVerificationWithNullHash() {
        String plainPassword = "TestPassword123!";

        // La vérification avec un hash null doit échouer
        assertFalse(PasswordUtils.verifyPassword(plainPassword, null),
                   "La vérification avec un hash null doit échouer");
    }

    @Test
    @Order(5)
    @DisplayName("Test avec mot de passe vide")
    void testPasswordUtilsWithEmptyPassword() {
        String emptyPassword = "";

        // Le hashage d'un mot de passe vide doit fonctionner
        String hashedPassword = PasswordUtils.hashPassword(emptyPassword);
        assertNotNull(hashedPassword, "Le hashage d'un mot de passe vide doit fonctionner");

        // La vérification doit aussi fonctionner
        assertTrue(PasswordUtils.verifyPassword(emptyPassword, hashedPassword),
                  "La vérification d'un mot de passe vide hashé doit fonctionner");
    }

    @Test
    @Order(6)
    @DisplayName("Test consistance du hashage")
    void testHashingConsistency() {
        String password = "TestPassword123!";

        // Hasher le même mot de passe plusieurs fois
        String hash1 = PasswordUtils.hashPassword(password);
        String hash2 = PasswordUtils.hashPassword(password);
        String hash3 = PasswordUtils.hashPassword(password);

        // Les hash doivent être différents (à cause du salt)
        assertNotEquals(hash1, hash2, "Les hash successifs doivent être différents");
        assertNotEquals(hash2, hash3, "Les hash successifs doivent être différents");
        assertNotEquals(hash1, hash3, "Les hash successifs doivent être différents");

        // Mais tous doivent pouvoir être vérifiés avec le mot de passe original
        assertTrue(PasswordUtils.verifyPassword(password, hash1), "Hash1 doit être vérifiable");
        assertTrue(PasswordUtils.verifyPassword(password, hash2), "Hash2 doit être vérifiable");
        assertTrue(PasswordUtils.verifyPassword(password, hash3), "Hash3 doit être vérifiable");
    }

    @Test
    @Order(7)
    @DisplayName("Test avec différents types de caractères")
    void testPasswordUtilsWithDifferentCharacters() {
        String[] passwords = {
            "SimplePassword",
            "MotDePasseAvecAccents àéèùç",
            "密码中文",
            "پاس ورڈ عربی",
            "пароль русский",
            "🔐🔑🗝️ emoji password! 🚀",
            "Spëcîål Chârãctërs! 123",
            "UPPERCASE_PASSWORD_123!",
            "lowercase_password_456@"
        };

        for (String password : passwords) {
            String hashedPassword = PasswordUtils.hashPassword(password);

            assertNotNull(hashedPassword, "Le hash ne doit pas être null pour: " + password);
            assertTrue(PasswordUtils.verifyPassword(password, hashedPassword),
                      "La vérification doit réussir pour: " + password);
            assertFalse(PasswordUtils.verifyPassword(password + "_wrong", hashedPassword),
                       "La vérification avec un mauvais mot de passe doit échouer pour: " + password);
        }
    }

    @Test
    @Order(8)
    @DisplayName("Test performance du hashage")
    void testHashingPerformance() {
        String password = "TestPassword123!";

        // Mesurer le temps de hashage
        long startTime = System.currentTimeMillis();
        String hashedPassword = PasswordUtils.hashPassword(password);
        long hashTime = System.currentTimeMillis() - startTime;

        // Mesurer le temps de vérification
        startTime = System.currentTimeMillis();
        boolean verified = PasswordUtils.verifyPassword(password, hashedPassword);
        long verifyTime = System.currentTimeMillis() - startTime;

        // Vérifications
        assertTrue(verified, "La vérification doit réussir");
        assertTrue(hashTime < 5000, "Le hashage ne doit pas prendre plus de 5 secondes");
        assertTrue(verifyTime < 5000, "La vérification ne doit pas prendre plus de 5 secondes");

        System.out.println("Temps de hashage: " + hashTime + "ms");
        System.out.println("Temps de vérification: " + verifyTime + "ms");
    }

    @Test
    @Order(9)
    @DisplayName("Test sécurité - résistance aux attaques par force brute")
    void testBruteForceResistance() {
        String password = "TestPassword123!";
        String hashedPassword = PasswordUtils.hashPassword(password);

        // Tester plusieurs mots de passe similaires
        String[] similarPasswords = {
            "TestPassword123@",
            "testPassword123!",
            "TestPassword124!",
            "TestPasswort123!",
            "TestPassword123",
            "TestPassword12!",
            "estPassword123!"
        };

        for (String similarPassword : similarPasswords) {
            assertFalse(PasswordUtils.verifyPassword(similarPassword, hashedPassword),
                       "Un mot de passe similaire ne doit pas être accepté: " + similarPassword);
        }
    }

    @Test
    @Order(10)
    @DisplayName("Test avec hash malformé")
    void testVerificationWithMalformedHash() {
        String password = "TestPassword123!";

        String[] malformedHashes = {
            "",
            "invalidhash",
            "tooshort",
            "$2b$10$invalid",
            "notahash"
        };

        for (String malformedHash : malformedHashes) {
            assertFalse(PasswordUtils.verifyPassword(password, malformedHash),
                       "La vérification avec un hash malformé doit échouer: " + malformedHash);
        }
    }
}
