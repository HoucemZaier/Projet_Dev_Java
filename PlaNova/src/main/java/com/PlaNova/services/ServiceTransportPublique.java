package com.PlaNova.services;

import com.PlaNova.models.TransportPublique;
import com.PlaNova.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTransportPublique implements Iservice<TransportPublique> {
    private Connection connection;

    public ServiceTransportPublique() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(TransportPublique transportPublique) throws SQLDataException {
        String sql = "INSERT INTO transport_publique (type, tarif, horraire, image, id_destination) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, transportPublique.getType());
            preparedStatement.setDouble(2, transportPublique.getTarif());
            preparedStatement.setString(3, transportPublique.getHoraire());
            preparedStatement.setString(4, transportPublique.getImage_path());
            preparedStatement.setInt(5, transportPublique.getId_destination());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void delete(TransportPublique transportPublique) throws SQLDataException {
        String sql = "DELETE FROM transport_publique WHERE id_transport_pub = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transportPublique.getId_transport_pub());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modify(TransportPublique transportPublique) throws SQLDataException {
        String sql = "UPDATE transport_publique SET type = ?, tarif = ?, horraire = ?, image = ?, id_destination = ? WHERE id_transport_pub = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, transportPublique.getType());
            preparedStatement.setDouble(2, transportPublique.getTarif());
            preparedStatement.setString(3, transportPublique.getHoraire());
            preparedStatement.setString(4, transportPublique.getImage_path());
            preparedStatement.setInt(5, transportPublique.getId_destination());
            preparedStatement.setInt(6, transportPublique.getId_transport_pub());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<TransportPublique> show() throws SQLDataException {
        String sql = "SELECT * FROM transport_publique";
        List<TransportPublique> transportList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                TransportPublique tp = new TransportPublique();
                tp.setId_transport_pub(rs.getInt("id_transport_pub"));
                tp.setType(rs.getString("type"));
                tp.setTarif(rs.getDouble("tarif"));
                tp.setHoraire(rs.getString("horraire"));
                tp.setImage_path(rs.getString("image"));
                tp.setId_destination(rs.getInt("id_destination"));
                transportList.add(tp);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return transportList;
    }
}
