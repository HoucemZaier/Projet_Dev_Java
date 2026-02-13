package utils.Services;

import Models.TransportPrive;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTransportPrive implements Iservice<TransportPrive> {
    private Connection connection;

    public ServiceTransportPrive() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(TransportPrive transportPrive) throws SQLDataException {
        // insertion selon la structure actuelle de la table (sans type_carburant)
        String sql = "INSERT INTO transport_prive (marque, etat, prix_loc, image) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, transportPrive.getMarque());
            preparedStatement.setString(2, transportPrive.getEtat());
            preparedStatement.setDouble(3, transportPrive.getPrix_loc());
            preparedStatement.setString(4, transportPrive.getImage_path());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(TransportPrive transportPrive) throws SQLDataException {
        String sql = "DELETE FROM transport_prive WHERE id_transport_priv = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, transportPrive.getId_transport_priv());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(TransportPrive transportPrive) throws SQLDataException {
        String sql = "UPDATE transport_prive SET etat = ?, prix_loc = ? WHERE id_transport_priv = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, transportPrive.getEtat());
            preparedStatement.setDouble(2, transportPrive.getPrix_loc());
            preparedStatement.setInt(3, transportPrive.getId_transport_priv());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<TransportPrive> recuperer() throws SQLDataException {
        String sql = "SELECT * FROM transport_prive";
        List<TransportPrive> transportList = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            transportList = new ArrayList<>();
            while (rs.next()) {
                TransportPrive tp = new TransportPrive();
                tp.setId_transport_priv(rs.getInt("id_transport_priv"));
                tp.setMarque(rs.getString("marque"));
                tp.setEtat(rs.getString("etat"));
                // la table ne contient pas la colonne type_carburant, on laisse la valeur Java à null
                tp.setPrix_loc(rs.getDouble("prix_loc"));
                tp.setImage_path(rs.getString("image"));
                transportList.add(tp);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return transportList;
    }
}