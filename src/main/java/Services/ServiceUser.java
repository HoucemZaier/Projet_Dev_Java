package Services;

import Models.*;
import utils.MyDatabase;
import utils.PasswordUtils;

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
        String sqlUser = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, pays, imageurl, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            // Hash password before saving
            pstmt.setString(4, PasswordUtils.hashPassword(user.getMotDePasse()));
            pstmt.setString(5, user.getPays());
            pstmt.setString(6, user.getImageurl());
            pstmt.setInt(7, user.getStatus()); // Add status field

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
            // Insert into employee table with matricule if it has one
            if (((Moderateur) user).getMatricule() != null) {
                String sql = "INSERT INTO employee (id_employee, matricule) VALUES (?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, user.getIdUtilisateur());
                pstmt.setString(2, ((Moderateur) user).getMatricule());
                pstmt.executeUpdate();
            }
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

        // Delete from employee table if exists (for Moderateur)
        String sqlEmployee = "DELETE FROM employee WHERE id_employee = ?";
        PreparedStatement pstmtEmployee = connection.prepareStatement(sqlEmployee);
        pstmtEmployee.setInt(1, id);
        pstmtEmployee.executeUpdate();
    }

    @Override
    public void modifier(User user) throws SQLException {
        String passwordToSave = user.getMotDePasse();
        boolean updatePassword = passwordToSave != null && !passwordToSave.isEmpty();
        if (updatePassword
                && !passwordToSave.startsWith("$2")
                && !passwordToSave.startsWith("$SHA256$")) {
            passwordToSave = PasswordUtils.hashPassword(passwordToSave);
        }

        final String sqlWithPassword = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, pays = ?, imageurl = ?, status = ? WHERE id_utilisateur = ?";
        final String sqlWithoutPassword = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, pays = ?, imageurl = ?, status = ? WHERE id_utilisateur = ?";

        try {
            PreparedStatement pstmt;
            if (updatePassword && passwordToSave != null) {
                pstmt = connection.prepareStatement(sqlWithPassword);
                pstmt.setString(1, user.getNom());
                pstmt.setString(2, user.getPrenom());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, passwordToSave);
                pstmt.setString(5, user.getPays());
                pstmt.setString(6, user.getImageurl());
                pstmt.setInt(7, user.getStatus());
                pstmt.setInt(8, user.getIdUtilisateur());
            } else {
                // Do not update password (e.g. when saving account info only) so we never overwrite with null
                pstmt = connection.prepareStatement(sqlWithoutPassword);
                pstmt.setString(1, user.getNom());
                pstmt.setString(2, user.getPrenom());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getPays());
                pstmt.setString(5, user.getImageurl());
                pstmt.setInt(6, user.getStatus());
                pstmt.setInt(7, user.getIdUtilisateur());
            }

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

    // Méthode supplémentaire : Vérifier si une matricule existe dans la base de données
    public boolean matriculeExists(String matricule) throws SQLException {
        // Check if matricule exists in admin or employee tables
        String sqlAdmin = "SELECT COUNT(*) FROM admin WHERE matricule = ?";
        String sqlEmployee = "SELECT COUNT(*) FROM employee WHERE matricule = ?";

        try {
            // Check in admin table
            PreparedStatement pstmtAdmin = connection.prepareStatement(sqlAdmin);
            pstmtAdmin.setString(1, matricule);
            ResultSet rsAdmin = pstmtAdmin.executeQuery();
            if (rsAdmin.next() && rsAdmin.getInt(1) > 0) {
                return true;
            }

            // Check in employee table
            PreparedStatement pstmtEmp = connection.prepareStatement(sqlEmployee);
            pstmtEmp.setString(1, matricule);
            ResultSet rsEmp = pstmtEmp.executeQuery();
            if (rsEmp.next() && rsEmp.getInt(1) > 0) {
                return true;
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la matricule : " + e.getMessage());
            throw e;
        }
    }

    /** Find user by email only (no password check). TRIM on column so spaces in DB don't block match; case-insensitive. */
    public User findByEmail(String email) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not available. Check your database configuration.");
        }
        String emailClean = email != null ? email.trim() : "";
        if (emailClean.isEmpty()) return null;
        String sql = "SELECT * FROM utilisateur WHERE LOWER(TRIM(email)) = LOWER(?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, emailClean);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return createUserFromResultSet(rs);
        }
        return null;
    }

    // Méthode supplémentaire : Authentification with hashed passwords
    public User authenticate(String email, String motDePasse) throws SQLException {
        User user = findByEmail(email);
        if (user == null) return null;

        // Check if user is blocked
        if (user.isBlocked()) {
            throw new SQLException("COMPTE_BLOQUE:Votre compte a été bloqué par l'administrateur. Veuillez contacter le support.");
        }

        String stored = user.getMotDePasse();
        if (stored == null || stored.isEmpty()) return null;
        if (!PasswordUtils.verifyPassword(motDePasse, stored)) return null;
        return user;
    }

    // Méthode pour bloquer un utilisateur
    public boolean blockUser(int userId) throws SQLException {
        String sql = "UPDATE utilisateur SET status = 1 WHERE id_utilisateur = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utilisateur bloqué avec succès. ID: " + userId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors du blocage de l'utilisateur : " + e.getMessage());
            throw e;
        }
    }

    // Méthode pour débloquer un utilisateur
    public boolean unblockUser(int userId) throws SQLException {
        String sql = "UPDATE utilisateur SET status = 0 WHERE id_utilisateur = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utilisateur débloqué avec succès. ID: " + userId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors du déblocage de l'utilisateur : " + e.getMessage());
            throw e;
        }
    }

    // Méthode pour vérifier si un utilisateur est bloqué
    public boolean isUserBlocked(int userId) throws SQLException {
        String sql = "SELECT status FROM utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("status") == 1;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du statut de l'utilisateur : " + e.getMessage());
            throw e;
        }
    }

    // Méthode pour compter les utilisateurs actifs
    public int countActiveUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE status = 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des utilisateurs actifs : " + e.getMessage());
            throw e;
        }
    }

    // Méthode pour compter les utilisateurs bloqués
    public int countBlockedUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE status = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des utilisateurs bloqués : " + e.getMessage());
            throw e;
        }
    }

    // Méthode supplémentaire : Mettre à jour le mot de passe
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not available. Check your database configuration.");
        }

        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE id_utilisateur = ?";

        try {
            // Hash the new password before storing
            String hashedPassword = PasswordUtils.hashPassword(newPassword);

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Mot de passe mis à jour avec succès pour l'utilisateur ID: " + userId);
                return true;
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du mot de passe : " + e.getMessage());
            throw e;
        }
    }

    // Méthode supplémentaire : Vérifier le mot de passe actuel
    public boolean verifyCurrentPassword(int userId, String currentPassword) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not available. Check your database configuration.");
        }

        String sql = "SELECT mot_de_passe FROM utilisateur WHERE id_utilisateur = ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("mot_de_passe");
                return PasswordUtils.verifyPassword(currentPassword, storedPassword);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du mot de passe : " + e.getMessage());
            throw e;
        }

        return false;
    }

    /** Reset password for a user by email (same email matching as login: LOWER(email) = LOWER(?), trim in Java). */
    public boolean resetPasswordByEmail(String email, String newPlainPassword) throws SQLException {
        if (connection == null || email == null || newPlainPassword == null || newPlainPassword.isEmpty()) {
            return false;
        }
        String emailClean = email.trim();
        if (emailClean.isEmpty()) return false;
        String selectSql = "SELECT id_utilisateur FROM utilisateur WHERE LOWER(TRIM(email)) = LOWER(?)";
        PreparedStatement selectStmt = connection.prepareStatement(selectSql);
        selectStmt.setString(1, emailClean);
        ResultSet rs = selectStmt.executeQuery();
        if (!rs.next()) {
            return false; // no user with this email
        }
        int userId = rs.getInt("id_utilisateur");
        rs.close();
        selectStmt.close();
        // Update password by id
        String updateSql = "UPDATE utilisateur SET mot_de_passe = ? WHERE id_utilisateur = ?";
        String hashed = PasswordUtils.hashPassword(newPlainPassword);
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        updateStmt.setString(1, hashed);
        updateStmt.setInt(2, userId);
        int rows = updateStmt.executeUpdate();
        return rows > 0;
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
        int status = rs.getInt("status"); // Get status from database

        // Utiliser le polymorphisme pour déterminer le type en vérifiant les tables spécifiques
        if (isAdmin(id)) {
            String matriculeAdmin = getMatriculeAdmin(id);
            Admin admin = new Admin(id, nom, prenom, email, motDePasse, pays, imageurl, matriculeAdmin);
            admin.setId_admin(id);
            admin.setStatus(status); // Set status
            user = admin;
        } else if (isClient(id)) {
            String cin = getCinClient(id);
            Client client = new Client(id, nom, prenom, email, motDePasse, pays, imageurl, cin);
            client.setId_client(id);
            client.setStatus(status); // Set status
            user = client;
        } else if (isGuide(id)) {
            Guide guide = new Guide(id, nom, prenom, email, motDePasse, pays, imageurl);
            guide.setId_guide(id);
            guide.setStatus(status); // Set status
            user = guide;
        } else if (isModerateur(id)) {
            String matricule = getMatriculeModerateur(id);
            Moderateur moderateur = new Moderateur(id, nom, prenom, email, motDePasse, pays, imageurl, matricule);
            moderateur.setId_moderateur(id);
            moderateur.setStatus(status); // Set status
            user = moderateur;
        } else {
            // Type inconnu, créer un User générique
            user = new User(id, nom, prenom, email, motDePasse, pays, imageurl, status);
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

    private String getMatriculeModerateur(int idModerateur) throws SQLException {
        String sql = "SELECT matricule FROM employee WHERE id_employee = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, idModerateur);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getString("matricule");
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
        // Check if user exists in employee table (for Moderateur)
        String sql = "SELECT COUNT(*) FROM employee WHERE id_employee = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}
