package com.PlaNova.services;

import com.PlaNova.models.Hotel;
import com.PlaNova.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceHotel implements IService<Hotel> {
    private Connection connection;

    public ServiceHotel() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Hotel hotel) throws SQLDataException {
        String sql = "INSERT INTO hotel (nom_hotel, adresse, ville, nombre_etoile, descescription, image, id_destination) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, hotel.getNomHotel());
            ps.setString(2, hotel.getAdresse());
            ps.setString(3, hotel.getVille());
            ps.setInt(4, hotel.getNombreEtoile());
            ps.setString(5, hotel.getDescription());
            ps.setString(6, hotel.getImage());
            ps.setInt(7, hotel.getIdDestination());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'hôtel: " + e.getMessage());
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void supprimer(Hotel hotel) throws SQLDataException {
        supprimer(hotel.getIdHotel());
    }

    public void supprimer(int idHotel) throws SQLDataException {
        String deleteChambresSql = "DELETE FROM chambre WHERE id_hotel = ?";
        String deleteHotelSql = "DELETE FROM hotel WHERE id_hotel = ?";

        try {
            // First, delete all chambres associated with this hotel
            PreparedStatement psChambres = connection.prepareStatement(deleteChambresSql);
            psChambres.setInt(1, idHotel);
            psChambres.executeUpdate();

            // Then, delete the hotel itself
            PreparedStatement psHotel = connection.prepareStatement(deleteHotelSql);
            psHotel.setInt(1, idHotel);
            psHotel.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'hôtel: " + e.getMessage());
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void modifier(Hotel hotel) throws SQLDataException {
        String sql = "UPDATE hotel SET nom_hotel=?, adresse=?, ville=?, nombre_etoile=?, descescription=?, image=?, id_destination=? "
                + "WHERE id_hotel=?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, hotel.getNomHotel());
            ps.setString(2, hotel.getAdresse());
            ps.setString(3, hotel.getVille());
            ps.setInt(4, hotel.getNombreEtoile());
            ps.setString(5, hotel.getDescription());
            ps.setString(6, hotel.getImage());
            ps.setInt(7, hotel.getIdDestination());
            ps.setInt(8, hotel.getIdHotel());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'hôtel: " + e.getMessage());
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public List<Hotel> recuperer() throws SQLDataException {
        String sql = "SELECT * FROM hotel";
        List<Hotel> hotels = new ArrayList<>();

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Hotel h = mapResultSetToHotel(rs);
                hotels.add(h);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des hôtels: " + e.getMessage());
            throw new SQLDataException(e.getMessage());
        }
        return hotels;
    }

    public Hotel getById(int id) throws SQLDataException {
        String sql = "SELECT * FROM hotel WHERE id_hotel = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToHotel(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'hôtel par ID: " + e.getMessage());
            throw new SQLDataException(e.getMessage());
        }
        return null;
    }

    private Hotel mapResultSetToHotel(ResultSet rs) throws SQLException {
        Hotel h = new Hotel();
        h.setIdHotel(rs.getInt("id_hotel"));
        h.setNomHotel(rs.getString("nom_hotel"));
        h.setAdresse(rs.getString("adresse"));
        h.setVille(rs.getString("ville"));
        h.setNombreEtoile(rs.getInt("nombre_etoile"));
        h.setDescription(rs.getString("descescription"));
        h.setImage(rs.getString("image"));
        h.setIdDestination(rs.getInt("id_destination"));
        return h;
    }
}