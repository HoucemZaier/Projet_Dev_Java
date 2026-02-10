package Services;

import Models.Excursion;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceExcursion implements Iservice <Excursion> {
    private Connection connection;
    public ServiceExcursion() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Excursion e) throws SQLDataException{
        String sql = "INSERT INTO Excursion (titre, destination, date_depart, date_retour, prix, nb_places, statut) " +
                " VALUES ('"
                + e.getTitre() + "', '"
                + e.getDestination() + "', '"
                + e.getDateDepart() + "', '"
                + e.getDateRetour() + "', '"
                + e.getPrix() + "', '"
                + e.getNbPlaces() + "', '"
                + e.getStatut() + "')";

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException q) {
            System.out.println(q.getMessage());
        }
    }
    @Override
    public void supprimer(Excursion e) {
        String sql = "DELETE FROM Excursion WHERE id_excursion = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, e.getIdExcursion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void modifier(Excursion e) {
        String sql = "UPDATE Excursion SET titre=?, destination=?, date_depart=?, date_retour=?, prix=?, nb_places=?, statut=? " +
                "WHERE id_excursion=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDestination());
            ps.setDate(3, e.getDateDepart());
            ps.setDate(4, e.getDateRetour());
            ps.setDouble(5, e.getPrix());
            ps.setInt(6, e.getNbPlaces());
            ps.setString(7, e.getStatut());
            ps.setInt(8, e.getIdExcursion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public List<Excursion> recuperer() {
        List<Excursion> list = new ArrayList<>();
        String sql = "SELECT * FROM Excursion";

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Excursion e = new Excursion();
                e.setIdExcursion(rs.getInt("id_excursion"));
                e.setTitre(rs.getString("titre"));
                e.setDestination(rs.getString("destination"));
                e.setDateDepart(rs.getDate("date_depart"));
                e.setDateRetour(rs.getDate("date_retour"));
                e.setPrix(rs.getDouble("prix"));
                e.setNbPlaces(rs.getInt("nb_places"));
                e.setStatut(rs.getString("statut"));
                list.add(e);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return list;
    }
}
