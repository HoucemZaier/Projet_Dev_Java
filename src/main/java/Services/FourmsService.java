package Services;

import Modeles.Fourms;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FourmsService implements CrudFourms {

    private Connection con = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Fourms forum) {
        String sql = "INSERT INTO forum (nom, nbparticipant, commentaire, idposte) VALUES (?, ?, ?, ?)";

        try (Connection con = DataSource.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, forum.getNom());
            stmt.setInt(2, forum.getNbparticipant());
            stmt.setString(3, forum.getCommentaire());
            stmt.setInt(4, forum.getIdposte());
            stmt.executeUpdate();

            System.out.println("Forum ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id_forum) {
        String sql = "DELETE FROM forum WHERE id_forum = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id_forum);
            stmt.executeUpdate();
            System.out.println("Forum supprimé !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Fourms forum) {
        String sql = "UPDATE forum SET nom = ?, nbparticipant = ?, commentaire = ?, idposte = ? WHERE id_forum = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, forum.getNom());
            stmt.setInt(2, forum.getNbparticipant());
            stmt.setString(3, forum.getCommentaire());
            stmt.setInt(4, forum.getIdposte());
            stmt.setInt(5, forum.getId_forum());
            stmt.executeUpdate();
            System.out.println("Forum modifié !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Fourms> afficherTout() {
        List<Fourms> forums = new ArrayList<>();
        String sql = "SELECT * FROM forum";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Fourms f = new Fourms(
                        rs.getInt("id_forum"),
                        rs.getString("nom"),
                        rs.getInt("nbparticipant"),
                        rs.getString("commentaire"),
                        rs.getInt("idposte")
                );
                forums.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return forums;
    }
}
