package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private static MyDatabase instance;
    private Connection connection;

    // MODIFIEZ CES INFORMATIONS SELON VOTRE CONFIGURATION
    private final String URL = "jdbc:mysql://localhost:3306/pidev";  // ← Changez le nom de la base
    private final String USER = "root";  // ← Votre username MySQL
    private final String PASSWORD = "";  // ← Votre mot de passe MySQL

    private MyDatabase() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établir la connexion
            connection = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("✓ Connexion reussie a la base de donnees!");

        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver MySQL introuvable!");
            System.err.println("Assurez-vous que la dépendance MySQL JDBC est installée");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Erreur de connexion a la base de donnees!");
            System.err.println("Verifiez: URL, USER, PASSWORD");
            System.err.println("URL configurée: " + URL);
            System.err.println("Détails de l'erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Vérifier si la connexion est toujours active
            if (connection == null || connection.isClosed()) {
                System.out.println("Connexion perdue ou fermée, réétablissement...");
                instance = new MyDatabase();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la connexion: " + e.getMessage());
            e.printStackTrace();
        }

        if (connection == null) {
            System.err.println("ERREUR CRITIQUE: Impossible d'établir la connexion à la base de données!");
        }

        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Connexion fermee");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}