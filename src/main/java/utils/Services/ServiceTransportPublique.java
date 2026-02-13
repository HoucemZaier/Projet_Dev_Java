package utils.Services;

import Models.TransportPublique;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTransportPublique implements Iservice <TransportPublique> {
    private Connection connection;

    public ServiceTransportPublique() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(TransportPublique transportPublique) throws SQLDataException {
        String sql = "INSERT INTO transport_publique (type, tarif, horraire, image) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, transportPublique.getType());
            preparedStatement.setDouble(2, transportPublique.getTarif());
            preparedStatement.setString(3, transportPublique.getHoraire());
            preparedStatement.setString(4, transportPublique.getImage_path());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(TransportPublique transportPublique) {
        String sql = "DELETE FROM transport_publique WHERE id_transport_pub = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, transportPublique.getId_transport_pub());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(TransportPublique transportPublique) {
        String sql = "UPDATE transport_publique SET tarif = ?, horraire = ? WHERE id_transport_pub = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, transportPublique.getTarif());
            preparedStatement.setString(2, transportPublique.getHoraire());
            preparedStatement.setInt(3, transportPublique.getId_transport_pub());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<TransportPublique> recuperer() throws SQLDataException {
        String sql = "SELECT * FROM transport_publique";
        List<TransportPublique> transportList = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            transportList = new ArrayList<>();
            while (rs.next()) {
                TransportPublique tp = new TransportPublique();
                tp.setId_transport_pub(rs.getInt("id_transport_pub"));
                tp.setType(rs.getString("type"));
                tp.setTarif(rs.getDouble("tarif"));
                // la colonne se nomme 'horraire' dans la base
                tp.setHoraire(rs.getString("horraire"));
                // et la colonne image est 'image'
                tp.setImage_path(rs.getString("image"));
                transportList.add(tp);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return transportList;
    }
}