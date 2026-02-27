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
        if (connection == null) {
            throw new RuntimeException("Connexion à la base de données impossible !");
        }
    }

    /** Récupère tous les noms de destinations triés */
    public List<String> getAllDestinationNames() throws SQLException {
        List<String> destinations = new ArrayList<>();
        String sql = "SELECT nom_destination FROM destination ORDER BY nom_destination ASC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String nom = rs.getString("nom_destination");
                if (nom != null && !nom.isBlank()) destinations.add(nom.trim());
            }
        }
        return destinations;
    }

    /** Récupère l'ID d'une destination à partir de son nom */
    public int getIdDestinationByName(String nomDestination) throws SQLDataException {
        if (nomDestination == null || nomDestination.isBlank())
            throw new SQLDataException("Le nom de la destination ne peut pas être vide.");

        String sql = "SELECT id_destination FROM destination WHERE LOWER(TRIM(nom_destination)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomDestination);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_destination");
                else throw new SQLDataException("Destination introuvable : " + nomDestination);
            }
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur récupération id destination : " + ex.getMessage());
        }
    }

    /** Ajouter une excursion avec nom de destination + coordonnées GPS */
    public void ajouter(Excursion e, String nomDestination) throws SQLDataException {
        int idDest = getIdDestinationByName(nomDestination);
        String sql = "INSERT INTO excursion (titre, id_destination, date_depart, date_retour, prix, nb_places, statut, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setInt(2, idDest);
            ps.setDate(3, e.getDateDepart());
            ps.setDate(4, e.getDateRetour());
            ps.setDouble(5, e.getPrix());
            ps.setInt(6, e.getNbPlaces());
            ps.setString(7, e.getStatut());
            if (e.getLatitude() != null)  ps.setDouble(8, e.getLatitude());
            else ps.setNull(8, Types.DOUBLE);
            if (e.getLongitude() != null) ps.setDouble(9, e.getLongitude());
            else ps.setNull(9, Types.DOUBLE);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur ajout excursion : " + ex.getMessage());
        }
    }

    @Override
    public void ajouter(Excursion e) throws SQLDataException {
        throw new UnsupportedOperationException(
                "Utilisez ajouter(Excursion e, String nomDestination) pour spécifier la destination");
    }

    @Override
    public void modifier(Excursion e) throws SQLDataException {
        if (e.getNomDestination() == null || e.getNomDestination().isBlank())
            throw new SQLDataException("Le nom de la destination est requis pour modifier l'excursion !");

        int idDest = getIdDestinationByName(e.getNomDestination());

        String sql = "UPDATE excursion SET titre=?, id_destination=?, date_depart=?, date_retour=?, " +
                "prix=?, nb_places=?, statut=?, latitude=?, longitude=? WHERE id_excursion=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setInt(2, idDest);
            ps.setDate(3, e.getDateDepart());
            ps.setDate(4, e.getDateRetour());
            ps.setDouble(5, e.getPrix());
            ps.setInt(6, e.getNbPlaces());
            ps.setString(7, e.getStatut());
            if (e.getLatitude() != null)  ps.setDouble(8, e.getLatitude());
            else ps.setNull(8, Types.DOUBLE);
            if (e.getLongitude() != null) ps.setDouble(9, e.getLongitude());
            else ps.setNull(9, Types.DOUBLE);
            ps.setInt(10, e.getIdExcursion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur modification excursion : " + ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) throws SQLDataException {
        String sql = "DELETE FROM excursion WHERE id_excursion=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur suppression excursion : " + ex.getMessage());
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
                e.setNomDestination(rs.getString("nom_destination"));
                e.setDateDepart(rs.getDate("date_depart"));
                e.setDateRetour(rs.getDate("date_retour"));
                e.setPrix(rs.getDouble("prix"));
                e.setNbPlaces(rs.getInt("nb_places"));
                e.setStatut(rs.getString("statut"));
                // ✅ NOUVEAU : lat/lng
                double lat = rs.getDouble("latitude");
                double lng = rs.getDouble("longitude");
                if (!rs.wasNull()) { e.setLatitude(lat); e.setLongitude(lng); }
                list.add(e);
            }
        } catch (SQLException ex) {
            throw new SQLDataException("Erreur récupération excursions : " + ex.getMessage());
        }
        return list;
    }
}
