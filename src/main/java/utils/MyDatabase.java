package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private final String URL = "jdbc:mysql://localhost:3306/pijava?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private Connection connection;
    private static MyDatabase instance;

    // Constructeur privé pour le Singleton
    private MyDatabase() {
        try {
            // Charger explicitement le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL chargé avec succès !");

            // Établir la connexion
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion réussie !!");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Driver MySQL non trouvé - " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Échec de connexion !!");
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
            // Vérifier si la connexion existe et n'est pas fermée
            if (connection == null || connection.isClosed()) {
                System.out.println("Connexion fermée, tentative de reconnexion...");

                // Recharger le driver si nécessaire
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Établir une nouvelle connexion
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Reconnexion réussie !!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Driver MySQL non trouvé lors de la reconnexion - " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la reconnexion: " + e.getMessage());
        }

        return connection;
    }

    // Méthode pour vérifier l'état de la connexion
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // Méthode pour fermer proprement la connexion (à appeler à la fin de l'application)
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion fermée avec succès.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}