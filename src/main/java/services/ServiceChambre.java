package services;

import models.Chambre;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceChambre implements Iservice<Chambre>
{
    private Connection connection;

    public ServiceChambre()
    {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Chambre chambre) throws SQLDataException
    {
        String sql = "INSERT INTO chambre (type_chambre, capacite, prix_chambre, statut_chambre, id_hotel, description, equipement) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, chambre.getTypeChambre());
            ps.setInt(2, chambre.getCapacite());
            ps.setDouble(3, chambre.getPrixChambre());
            ps.setString(4, chambre.getStatutChambre());
            ps.setInt(5, chambre.getIdHotel());
            ps.setString(6, chambre.getDescription());
            ps.setString(7, chambre.getEquipement());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la chambre: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Chambre chambre) throws SQLDataException
    {
        String sql = "DELETE FROM chambre WHERE id_chambre = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, chambre.getIdChambre());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Chambre chambre) throws SQLDataException
    {
        String sql = "UPDATE chambre SET type_chambre=?, capacite=?, prix_chambre=?, statut_chambre=?, id_hotel=?, description=?, equipement=? "
                + "WHERE id_chambre=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, chambre.getTypeChambre());
            ps.setInt(2, chambre.getCapacite());
            ps.setDouble(3, chambre.getPrixChambre());
            ps.setString(4, chambre.getStatutChambre());
            ps.setInt(5, chambre.getIdHotel());
            ps.setString(6, chambre.getDescription());
            ps.setString(7, chambre.getEquipement());
            ps.setInt(8, chambre.getIdChambre());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @Override
    public List<Chambre> recuperer() throws SQLDataException
    {
        String sql = "SELECT * FROM chambre";
        List<Chambre> chambres = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Chambre c = new Chambre();
                c.setIdChambre(rs.getInt("id_chambre"));
                c.setTypeChambre(rs.getString("type_chambre"));
                c.setCapacite(rs.getInt("capacite"));
                c.setPrixChambre(rs.getDouble("prix_chambre"));
                c.setStatutChambre(rs.getString("statut_chambre"));
                c.setIdHotel(rs.getInt("id_hotel"));
                c.setDescription(rs.getString("description"));
                c.setEquipement(rs.getString("equipement"));
                chambres.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return chambres;
    }

    // Méthode supplémentaire pour récupérer les chambres avec le nom de l'hôtel
    public List<Chambre> recupererAvecNomHotel() throws SQLDataException
    {
        String sql = "SELECT c.*, h.nom_hotel FROM chambre c " +
                "LEFT JOIN hotel h ON c.id_hotel = h.id_hotel";
        List<Chambre> chambres = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Chambre c = new Chambre();
                c.setIdChambre(rs.getInt("id_chambre"));
                c.setTypeChambre(rs.getString("type_chambre"));
                c.setCapacite(rs.getInt("capacite"));
                c.setPrixChambre(rs.getDouble("prix_chambre"));
                c.setStatutChambre(rs.getString("statut_chambre"));
                c.setIdHotel(rs.getInt("id_hotel"));
                c.setDescription(rs.getString("description"));
                c.setEquipement(rs.getString("equipement"));
                c.setNomHotel(rs.getString("nom_hotel"));
                chambres.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return chambres;
    }
}