package services;

import models.Hotel;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHotel implements Iservice<Hotel>
{
    private   Connection connection;

    public ServiceHotel()
    {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Hotel hotel) throws SQLDataException
    {
        String sql = "INSERT INTO hotel (nom_hotel, adresse, ville, nombre_etoile, description, image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, hotel.getNomHotel());
            ps.setString(2, hotel.getAdresse());
            ps.setString(3, hotel.getVille());
            ps.setInt(4, hotel.getNombreEtoile());
            ps.setString(5, hotel.getDescription());
            ps.setString(6, hotel.getImage());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Hotel hotel) throws SQLDataException
    {
        String sql = "DELETE FROM hotel WHERE id_hotel = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, hotel.getIdHotel());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Ajoutez cette méthode pour supporter la suppression par ID
    public void supprimer(int idHotel) throws SQLDataException
    {
        String sql = "DELETE FROM hotel WHERE id_hotel = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idHotel);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Hotel hotel) throws SQLDataException
    {
        String sql = "UPDATE hotel SET nom_hotel=?, adresse=?, ville=?, nombre_etoile=?, description=?, image=? "
                + "WHERE id_hotel=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, hotel.getNomHotel());
            ps.setString(2, hotel.getAdresse());
            ps.setString(3, hotel.getVille());
            ps.setInt(4, hotel.getNombreEtoile());
            ps.setString(5, hotel.getDescription());
            ps.setString(6, hotel.getImage());
            ps.setInt(7, hotel.getIdHotel());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Hotel> recuperer() throws SQLDataException
    {
        String sql = "SELECT * FROM hotel";
        List<Hotel> hotels = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Hotel h = new Hotel();
                h.setIdHotel(rs.getInt("id_hotel"));
                h.setNomHotel(rs.getString("nom_hotel"));
                h.setAdresse(rs.getString("adresse"));
                h.setVille(rs.getString("ville"));
                h.setNombreEtoile(rs.getInt("nombre_etoile"));
                h.setDescription(rs.getString("description"));
                h.setImage(rs.getString("image"));

                hotels.add(h);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return hotels;
    }

    // Méthode pour récupérer un hôtel par son ID
    public Hotel getById(int id) throws SQLDataException
    {
        String sql = "SELECT * FROM hotel WHERE id_hotel = ?";
        Hotel hotel = null;

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                hotel = new Hotel();
                hotel.setIdHotel(rs.getInt("id_hotel"));
                hotel.setNomHotel(rs.getString("nom_hotel"));
                hotel.setAdresse(rs.getString("adresse"));
                hotel.setVille(rs.getString("ville"));
                hotel.setNombreEtoile(rs.getInt("nombre_etoile"));
                hotel.setDescription(rs.getString("description"));
                hotel.setImage(rs.getString("image"));

            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return hotel;
    }
}