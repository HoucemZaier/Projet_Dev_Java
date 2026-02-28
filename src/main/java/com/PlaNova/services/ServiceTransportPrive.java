package com.PlaNova.services;

import com.PlaNova.models.TransportPrive;
import com.PlaNova.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTransportPrive implements IService<TransportPrive> {
    private Connection connection;

    public ServiceTransportPrive() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(TransportPrive transportPrive) throws SQLDataException {
        String sql = "INSERT INTO transport_prive (marque, etat, complement, prix_Lac, image, id_destination) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, transportPrive.getMarque());
            preparedStatement.setString(2, transportPrive.getEtat());
            preparedStatement.setString(3, transportPrive.getComplement());
            preparedStatement.setDouble(4, transportPrive.getPrix_lac());
            preparedStatement.setString(5, transportPrive.getImage_path());
            preparedStatement.setInt(6, transportPrive.getId_destination());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void delete(TransportPrive transportPrive) throws SQLDataException {
        String sql = "DELETE FROM transport_prive WHERE id_transport_priv = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transportPrive.getId_transport_priv());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modify(TransportPrive transportPrive) throws SQLDataException {
        String sql = "UPDATE transport_prive SET marque = ?, etat = ?, complement = ?, prix_Lac = ?, image = ?, id_destination = ? WHERE id_transport_priv = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, transportPrive.getMarque());
            preparedStatement.setString(2, transportPrive.getEtat());
            preparedStatement.setString(3, transportPrive.getComplement());
            preparedStatement.setDouble(4, transportPrive.getPrix_lac());
            preparedStatement.setString(5, transportPrive.getImage_path());
            preparedStatement.setInt(6, transportPrive.getId_destination());
            preparedStatement.setInt(7, transportPrive.getId_transport_priv());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<TransportPrive> show() throws SQLDataException {
        String sql = "SELECT * FROM transport_prive";
        List<TransportPrive> transportList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                TransportPrive tp = new TransportPrive();
                tp.setId_transport_priv(rs.getInt("id_transport_priv"));
                tp.setMarque(rs.getString("marque"));
                tp.setEtat(rs.getString("etat"));
                tp.setComplement(rs.getString("complement"));
                tp.setPrix_lac(rs.getDouble("prix_Lac"));
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
