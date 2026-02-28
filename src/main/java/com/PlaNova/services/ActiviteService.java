package com.PlaNova.services;

import com.PlaNova.models.Activite;
import com.PlaNova.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService {
    private final Connection connection;

    public ActiviteService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public List<Activite> getActivitesByDestination(int destinationId) throws SQLDataException {
        if (connection == null) {
            throw new SQLDataException("Database connection is not available.");
        }

        List<Activite> list = new ArrayList<>();
        String sql = "SELECT * FROM activite WHERE id_destination = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, destinationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Activite a = new Activite();
                    a.setIdActivite(rs.getInt("id_activite"));
                    a.setNom(rs.getString("nom"));
                    a.setDescription(rs.getString("description"));
                    a.setDateActivite(rs.getDate("date_activite").toLocalDate());
                    a.setHeureActivite(rs.getTime("heure_activite").toLocalTime());
                    a.setLieu(rs.getString("lieu"));
                    a.setPrix(rs.getDouble("prix"));
                    a.setIdExcursion(rs.getInt("id_excursion"));
                    a.setIdDestination(rs.getInt("id_destination"));

                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return list;
    }
}
