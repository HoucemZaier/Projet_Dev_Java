package Services;

import Models.*;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {
    private Connection connection;

    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
        if (connection == null) {
            System.err.println("ERREUR CRITIQUE: Impossible de créer une connexion à la base de données!");
            System.err.println("Vérifiez votre configuration de base de données dans MyDatabase.java");
        }
    }

    @Override
    public void ajouter(User user) throws SQLException {
        // 1. Insérer dans la table utilisateur
        String sqlUser = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, pays, imageurl) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getMotDePasse());
            pstmt.setString(5, user.getPays());
            pstmt.setString(6, user.getImageurl());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    user.setIdUtilisateur(userId);

                    // 2. Insérer dans la table spécifique selon le type
                    ajouterTypeSpecifique(user);

                    System.out.println("Utilisateur ajouté avec succès ! ID: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
            throw e;
        }
    }

    private void ajouterTypeSpecifique(User user) throws SQLException {
        if (user instanceof Admin) {
            String sql = "INSERT INTO admin (id_admin, matricule) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, user.getIdUtilisateur());
            pstmt.setString(2, ((Admin) user).getMatricule());
            pstmt.executeUpdate();
            ((Admin) user).setId_admin(user.getIdUtilisateur());

        } else if (user instanceof Client) {
            String sql = "INSERT INTO client (id_client, cin) VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, user.getIdUtilisateur());
            pstmt.setString(2, ((Client) user).getCin());
            pstmt.executeUpdate();
            ((Client) user).setId_client(user.getIdUtilisateur());

        } else if (user instanceof Guide) {
            String sql = "INSERT INTO guide (id_guide) VALUES (?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, user.getIdUtilisateur());
            pstmt.executeUpdate();
            ((Guide) user).setId_guide(user.getIdUtilisateur());

        } else if (user instanceof Moderateur) {
            // Si vous avez une table moderateur
            ((Moderateur) user).setId_moderateur(user.getIdUtilisateur());
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        try {
            // First, delete from child tables based on user type
            supprimerTypeSpecifique(id);

            // Then delete from parent table
            String sql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Utilisateur supprimé avec succès !");
            } else {
                System.out.println("Aucun utilisateur trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            throw e;
        }
    }

    private void supprimerTypeSpecifique(int id) throws SQLException {
        // Delete from admin table if exists
        String sqlAdmin = "DELETE FROM admin WHERE id_admin = ?";
        PreparedStatement pstmtAdmin = connection.prepareStatement(sqlAdmin);
        pstmtAdmin.setInt(1, id);
        pstmtAdmin.executeUpdate();

        // Delete from client table if exists
        String sqlClient = "DELETE FROM client WHERE id_client = ?";
        PreparedStatement pstmtClient = connection.prepareStatement(sqlClient);
        pstmtClient.setInt(1, id);
        pstmtClient.executeUpdate();

        // Delete from guide table if exists
        String sqlGuide = "DELETE FROM guide WHERE id_guide = ?";
        PreparedStatement pstmtGuide = connection.prepareStatement(sqlGuide);
        pstmtGuide.setInt(1, id);
        pstmtGuide.executeUpdate();

        // Note: Moderateur doesn't have a specific table, so no deletion needed
    }

    @Override
    public void modifier(User user) throws SQLException {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, pays = ?, imageurl = ? WHERE id_utilisateur = ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getMotDePasse());
            pstmt.setString(5, user.getPays());
            pstmt.setString(6, user.getImageurl());
            pstmt.setInt(7, user.getIdUtilisateur());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                modifierTypeSpecifique(user);
                System.out.println("Utilisateur modifié avec succès !");
            } else {
                System.out.println("Aucun utilisateur trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
            throw e;
        }
    }

    private void modifierTypeSpecifique(User user) throws SQLException {
        if (user instanceof Admin) {
            String sql = "UPDATE admin SET matricule = ? WHERE id_admin = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, ((Admin) user).getMatricule());
            pstmt.setInt(2, user.getIdUtilisateur());
            pstmt.executeUpdate();

        } else if (user instanceof Client) {
            String sql = "UPDATE client SET cin = ? WHERE id_client = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, ((Client) user).getCin());
            pstmt.setInt(2, user.getIdUtilisateur());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<User> recuperer() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not available. Check your database configuration.");
        }

        String sql = "SELECT * FROM utilisateur";
        List<User> userList = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                User user = createUserFromResultSet(rs);
                userList.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération : " + e.getMessage());
            throw e;
        }

        return userList;
    }

    @Override
    public User recupererParId(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération par ID : " + e.getMessage());
            throw e;
        }

        return null;
    }

    // Méthode supplémentaire : Authentification
    public User authenticate(String email, String motDePasse) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not available. Check your database configuration.");
        }

        String sql = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, motDePasse);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
            throw e;
        }

        return null;
    }

    // Méthode supplémentaire : Récupérer par type
    public List<User> recupererParType(String type) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE type_utilisateur = ?";
        List<User> userList = new ArrayList<>();

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = createUserFromResultSet(rs);
                userList.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération par type : " + e.getMessage());
            throw e;
        }

        return userList;
    }

    // Créer un objet User à partir du ResultSet en utilisant le polymorphisme (instanceof)
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = null;

        int id = rs.getInt("id_utilisateur");
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String email = rs.getString("email");
        String motDePasse = rs.getString("mot_de_passe");
        String pays = rs.getString("pays");
        String imageurl = rs.getString("imageurl");

        // Utiliser le polymorphisme pour déterminer le type en vérifiant les tables spécifiques
        if (isAdmin(id)) {
            String matriculeAdmin = getMatriculeAdmin(id);
            Admin admin = new Admin(id, nom, prenom, email, motDePasse, pays, imageurl, matriculeAdmin);
            admin.setId_admin(id);
            user = admin;
        } else if (isClient(id)) {
            String cin = getCinClient(id);
            Client client = new Client(id, nom, prenom, email, motDePasse, pays, imageurl, cin);
            client.setId_client(id);
            user = client;
        } else if (isGuide(id)) {
            Guide guide = new Guide(id, nom, prenom, email, motDePasse, pays, imageurl);
            guide.setId_guide(id);
            user = guide;
        } else if (isModerateur(id)) {
            Moderateur moderateur = new Moderateur(id, nom, prenom, email, motDePasse, pays, imageurl);
            moderateur.setId_moderateur(id);
            user = moderateur;
        } else {
            // Type inconnu, créer un User générique
            user = new User(id, nom, prenom, email, motDePasse, pays, imageurl);
        }

        return user;
    }

    private String getMatriculeAdmin(int idAdmin) throws SQLException {
        String sql = "SELECT matricule FROM admin WHERE id_admin = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, idAdmin);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getString("matricule");
        }
        return null;
    }

    private String getCinClient(int idClient) throws SQLException {
        String sql = "SELECT cin FROM client WHERE id_client = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, idClient);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getString("cin");
        }
        return null;
    }

    // Méthodes helper pour déterminer le type d'utilisateur en utilisant le polymorphisme (vérification des tables)
    private boolean isAdmin(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM admin WHERE id_admin = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    private boolean isClient(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM client WHERE id_client = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    private boolean isGuide(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM guide WHERE id_guide = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    private boolean isModerateur(int id) throws SQLException {
        // Pour Moderateur, on peut vérifier s'il n'est dans aucune autre table spécifique
        // ou si vous avez une table moderateur, vérifiez-la
        return !isAdmin(id) && !isClient(id) && !isGuide(id);
    }
}
