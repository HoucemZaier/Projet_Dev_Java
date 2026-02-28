package com.PlaNova.services;

import com.PlaNova.models.Hotel;
import com.PlaNova.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelService {
    private final Connection connection;

    public HotelService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public List<Hotel> getHotelsByDestinationOrVille(int destinationId, String ville) throws SQLDataException {
        if (connection == null) {
            throw new SQLDataException("Database connection is not available.");
        }

        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT * FROM hotel WHERE id_destination = ? OR ville = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setString(2, ville);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Hotel h = new Hotel();
                    h.setIdHotel(rs.getInt("id_hotel"));
                    h.setNomHotel(rs.getString("nom_hotel"));
                    h.setAdresse(rs.getString("adresse"));
                    h.setVille(rs.getString("ville"));
                    h.setNombreEtoile(rs.getInt("nombre_etoile"));
                    h.setDescription(rs.getString("descescription"));
                                                                      
                    h.setImage(rs.getString("image"));
                    h.setIdDestination(rs.getInt("id_destination"));

                    list.add(h);
                }
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return list;
    }
}
