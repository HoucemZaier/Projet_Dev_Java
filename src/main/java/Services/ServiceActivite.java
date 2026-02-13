package Services;

import Models.Activite;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceActivite implements Iservice<Activite> {

    private final Connection connection;

    public ServiceActivite() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Activite a) throws SQLDataException {
        String sql = "INSERT INTO Activite (nom, description, date_activite, heure_activite, lieu, prix, id_excursion, id_destination) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setDate(3, a.getDateActivite());
            ps.setTime(4, a.getHeureActivite());
            ps.setString(5, a.getLieu());
            ps.setDouble(6, a.getPrix());
            ps.setInt(7, a.getIdExcursion());
            ps.setInt(8, a.getIdDestination());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de l'ajout de l'activité : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) throws SQLDataException {
        String sql = "DELETE FROM Activite WHERE id_activite = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de la suppression : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Activite a) throws SQLDataException {
        String sql = "UPDATE Activite SET nom=?, description=?, date_activite=?, heure_activite=?, lieu=?, prix=?, id_excursion=?, id_destination=? " +
                "WHERE id_activite=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setDate(3, a.getDateActivite());
            ps.setTime(4, a.getHeureActivite());
            ps.setString(5, a.getLieu());
            ps.setDouble(6, a.getPrix());
            ps.setInt(7, a.getIdExcursion());
            ps.setInt(8, a.getIdDestination());
            ps.setInt(9, a.getIdActivite());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de la modification : " + ex.getMessage());
        }
    }

    @Override
    public List<Activite> recuperer() throws SQLDataException {
        List<Activite> list = new ArrayList<>();
        String sql = "SELECT * FROM Activite";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Activite a = new Activite();
                a.setIdActivite(rs.getInt("id_activite"));
                a.setNom(rs.getString("nom"));
                a.setDescription(rs.getString("description"));
                a.setDateActivite(rs.getDate("date_activite"));
                a.setHeureActivite(rs.getTime("heure_activite"));
                a.setLieu(rs.getString("lieu"));
                a.setPrix(rs.getDouble("prix"));
                a.setIdExcursion(rs.getInt("id_excursion"));
                a.setIdDestination(rs.getInt("id_destination"));
                list.add(a);
            }

        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de la récupération des activités : " + ex.getMessage());
        }
        return list;
    }

    // ----------------------
    // Méthodes pour les ComboBox
    // ----------------------

    public List<Integer> getAllExcursionIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_excursion FROM Excursion"; // table excursion
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getInt("id_excursion"));
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération des excursions : " + ex.getMessage());
        }
        return ids;
    }

    public List<Integer> getAllDestinationIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_destination FROM Destination"; // table destination
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getInt("id_destination"));
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération des destinations : " + ex.getMessage());
        }
        return ids;
    }
}
