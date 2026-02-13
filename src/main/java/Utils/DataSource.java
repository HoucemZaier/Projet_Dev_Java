package Utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource
{
    final String URL = "jdbc:mysql://localhost:3306/pidev";
    final String USERNAME = "root";
    final String PASSWORD = "";

    private Connection connection;
    private static DataSource instance;

    public DataSource() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion réussie !!");
        } catch (SQLException e) {
            System.out.println("Echec de Connexion !!");
            e.printStackTrace();
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Nouvelle connexion établie !");
            }
        } catch (SQLException e) {
            System.err.println("Erreur de reconnexion : " + e.getMessage());
        }
        return conn;
    }
}
