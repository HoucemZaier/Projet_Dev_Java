package Services;

import Modeles.Posts;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService implements CrudPosts {

    private Connection con = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Posts post) {
        String requete = "INSERT INTO post (nomPost, description, prix, typePost, id_utilisateur) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(requete)) {
            ps.setString(1, post.getNomPost());
            ps.setString(2, post.getDescription());
            ps.setDouble(3, post.getPrix());
            ps.setString(4, post.getTypePost());

            if (post.getId_utilisateur() > 0) {
                ps.setInt(5, post.getId_utilisateur());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.executeUpdate();
            System.out.println("✅ Post ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur d'ajout : " + e.getMessage());
        }
    }

    @Override
    public List<Posts> afficherTout() {
        List<Posts> list = new ArrayList<>();
        String requete = "SELECT * FROM post";
        try (Statement ste = con.createStatement();
             ResultSet rs = ste.executeQuery(requete)) {

            while (rs.next()) {
                list.add(new Posts(
                        rs.getInt("idPost"),
                        rs.getString("nomPost"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getString("typePost"),
                        rs.getInt("id_utilisateur")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur de lecture : " + e.getMessage());
        }
        return list;
    }

    @Override
    public void modifier(Posts post) {
        String requete = "UPDATE post SET nomPost=?, description=?, prix=?, typePost=?, id_utilisateur=? WHERE idPost=?";
        try (PreparedStatement ps = con.prepareStatement(requete)) {
            ps.setString(1, post.getNomPost());
            ps.setString(2, post.getDescription());
            ps.setDouble(3, post.getPrix());
            ps.setString(4, post.getTypePost());
            ps.setInt(5, post.getId_utilisateur());
            ps.setInt(6, post.getIdPost());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Post ID " + post.getIdPost() + " mis à jour !");
            } else {
                System.out.println("⚠️ Aucun post trouvé avec l'ID " + post.getIdPost());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur de modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String requete = "DELETE FROM post WHERE idPost = ?";
        try (PreparedStatement ps = con.prepareStatement(requete)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Post " + id + " supprimé !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de suppression : " + e.getMessage());
        }
    }
}