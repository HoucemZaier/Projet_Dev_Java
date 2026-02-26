package com.PlaNova.services;

import com.PlaNova.models.Destination;
import com.PlaNova.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DestinationService implements Iservice<Destination> {
    private final Connection connection;

    public DestinationService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    private void checkConnection() throws SQLDataException {
        if (connection == null) {
            throw new SQLDataException("Database connection is not available. Please check your MySQL server.");
        }
    }

    @Override
    public void add(Destination d) throws SQLDataException {
        checkConnection();
        String sql = "INSERT INTO destination (nom_destination, pays, datearrivee, datedep, image) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, d.getNomDestination());
            ps.setString(2, d.getPays());
            ps.setDate(3, Date.valueOf(d.getDateArrivee()));
            ps.setDate(4, Date.valueOf(d.getDateDep()));
            ps.setString(5, d.getImage());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void delete(Destination d) throws SQLDataException {
        checkConnection();
        String sql = "DELETE FROM destination WHERE id_destination = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, d.getIdDestination());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void modify(Destination d) throws SQLDataException {
        checkConnection();
        String sql = "UPDATE destination SET nom_destination = ?, pays = ?, datearrivee = ?, datedep = ?, image = ? WHERE id_destination = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, d.getNomDestination());
            ps.setString(2, d.getPays());
            ps.setDate(3, Date.valueOf(d.getDateArrivee()));
            ps.setDate(4, Date.valueOf(d.getDateDep()));
            ps.setString(5, d.getImage());
            ps.setInt(6, d.getIdDestination());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public List<Destination> show() throws SQLDataException {
        checkConnection();
        List<Destination> list = new ArrayList<>();
        String sql = "SELECT * FROM destination";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Destination d = new Destination(
                    rs.getInt("id_destination"),
                    rs.getString("nom_destination"),
                    rs.getString("pays"),
                    rs.getDate("datearrivee").toLocalDate(),
                    rs.getDate("datedep").toLocalDate(),
                    rs.getString("image")
                );
                list.add(d);
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return list;
    }

    public List<Destination> search(String keyword) throws SQLDataException {
        checkConnection();
        List<Destination> list = new ArrayList<>();
        String sql = "SELECT * FROM destination WHERE nom_destination LIKE ? OR pays LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String param = "%" + keyword + "%";
            ps.setString(1, param);
            ps.setString(2, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Destination d = new Destination(
                    rs.getInt("id_destination"),
                    rs.getString("nom_destination"),
                    rs.getString("pays"),
                    rs.getDate("datearrivee").toLocalDate(),
                    rs.getDate("datedep").toLocalDate(),
                    rs.getString("image")
                );
                list.add(d);
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return list;
    }
}
