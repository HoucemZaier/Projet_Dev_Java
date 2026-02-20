package Services;

import Models.Excursion;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceExcursion implements Iservice<Excursion> {

    private final Connection connection;

    public ServiceExcursion() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // Méthode pour récupérer l'id de la destination à partir du nom
    public int getIdDestinationByName(String nomDestination) throws SQLDataException {
        String sql = "SELECT id_destination FROM destination WHERE nom_destination = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomDestination);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_destination");
            } else {
                throw new SQLDataException("Destination introuvable : " + nomDestination);
            }
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de la récupération de l'id de destination : " + ex.getMessage());
        }
    }

    // Nouvelle méthode pour ajouter avec nom de destination
    public void ajouter(Excursion e, String nomDestination) throws SQLDataException {
        int idDest = getIdDestinationByName(nomDestination); // convertir nom -> id

        String sql = "INSERT INTO excursion (titre, id_destination, date_depart, date_retour, prix, nb_places, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setInt(2, idDest);
            ps.setDate(3, e.getDateDepart());
            ps.setDate(4, e.getDateRetour());
            ps.setDouble(5, e.getPrix());
            ps.setInt(6, e.getNbPlaces());
            ps.setString(7, e.getStatut());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de l'ajout de l'excursion : " + ex.getMessage());
        }
    }

    // Implémentation obligatoire pour interface (peut lancer exception)
    @Override
    public void ajouter(Excursion e) throws SQLDataException {
        throw new UnsupportedOperationException("Utilisez ajouter(Excursion e, String nomDestination) pour spécifier la destination");
    }

    @Override
    public void modifier(Excursion e) throws SQLDataException {
        if (e.getNomDestination() == null || e.getNomDestination().isEmpty()) {
            throw new SQLDataException("Le nom de la destination est requis pour modifier l'excursion !");
        }

        int idDest = getIdDestinationByName(e.getNomDestination());

        String sql = "UPDATE excursion SET titre=?, id_destination=?, date_depart=?, date_retour=?, prix=?, nb_places=?, statut=? " +
                "WHERE id_excursion=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setInt(2, idDest);
            ps.setDate(3, e.getDateDepart());
            ps.setDate(4, e.getDateRetour());
            ps.setDouble(5, e.getPrix());
            ps.setInt(6, e.getNbPlaces());
            ps.setString(7, e.getStatut());
            ps.setInt(8, e.getIdExcursion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de la modification de l'excursion : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) throws SQLDataException {
        String sql = "DELETE FROM excursion WHERE id_excursion = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de la suppression de l'excursion : " + ex.getMessage());
        }
    }

    @Override
    public List<Excursion> recuperer() throws SQLDataException {
        List<Excursion> list = new ArrayList<>();
        String sql = "SELECT e.*, d.nom_destination FROM excursion e " +
                "LEFT JOIN destination d ON e.id_destination = d.id_destination";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Excursion e = new Excursion();
                e.setIdExcursion(rs.getInt("id_excursion"));
                e.setTitre(rs.getString("titre"));
                e.setIdDestination(rs.getInt("id_destination"));
                e.setNomDestination(rs.getString("nom_destination")); // <-- ici nom de la destination
                e.setDateDepart(rs.getDate("date_depart"));
                e.setDateRetour(rs.getDate("date_retour"));
                e.setPrix(rs.getDouble("prix"));
                e.setNbPlaces(rs.getInt("nb_places"));
                e.setStatut(rs.getString("statut"));
                list.add(e);
            }

        } catch (SQLException ex) {
            throw new SQLDataException("Erreur lors de la récupération des excursions : " + ex.getMessage());
        }
        return list;
    }
}