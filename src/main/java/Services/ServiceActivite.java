package Services;

import Models.Activite;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceActivite implements Iservice<Activite> {

    private Connection connection;

    public ServiceActivite() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Activite a) {
        String sql = "INSERT INTO Activite (nom, description, date_activite, heure_activite, lieu, prix, id_excursion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setDate(3, a.getDateActivite());
            ps.setTime(4, a.getHeureActivite());
            ps.setString(5, a.getLieu());
            ps.setDouble(6, a.getPrix());
            ps.setInt(7, a.getIdExcursion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void supprimer(Activite a) {
        String sql = "DELETE FROM Activite WHERE id_activite = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, a.getIdActivite());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void modifier(Activite a) {
        String sql = "UPDATE Activite SET nom=?, description=?, date_activite=?, heure_activite=?, lieu=?, prix=?, id_excursion=? " +
                "WHERE id_activite=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setDate(3, a.getDateActivite());
            ps.setTime(4, a.getHeureActivite());
            ps.setString(5, a.getLieu());
            ps.setDouble(6, a.getPrix());
            ps.setInt(7, a.getIdExcursion());
            ps.setInt(8, a.getIdActivite());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public List<Activite> recuperer() {
        List<Activite> list = new ArrayList<>();
        String sql = "SELECT * FROM Activite";

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

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
                list.add(a);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return list;
    }
}
