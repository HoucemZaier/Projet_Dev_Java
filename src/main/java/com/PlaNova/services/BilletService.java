package com.PlaNova.services;

import com.PlaNova.models.Billet;
import com.PlaNova.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BilletService implements IService<Billet> {
    private final Connection connection;

    public BilletService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    private void checkConnection() throws SQLDataException {
        if (connection == null) {
            throw new SQLDataException("Database connection is not available. Please check your MySQL server.");
        }
    }

    @Override
    public void add(Billet b) throws SQLDataException {
        checkConnection();
        String sql = "INSERT INTO billet (db, idv, num_place, id_destination, id_transport_pub, id_transport_priv) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, b.getDb());
            ps.setString(2, b.getIdv());
            ps.setString(3, b.getNumPlace());
            ps.setInt(4, b.getIdDestination());
            if (b.getIdTransportPub() > 0) {
                ps.setInt(5, b.getIdTransportPub());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if (b.getIdTransportPriv() > 0) {
                ps.setInt(6, b.getIdTransportPriv());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void delete(Billet b) throws SQLDataException {
        checkConnection();
        String sql = "DELETE FROM billet WHERE id_billet = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, b.getIdBillet());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void modify(Billet b) throws SQLDataException {
        checkConnection();
        String sql = "UPDATE billet SET db = ?, idv = ?, num_place = ?, id_destination = ?, id_transport_pub = ?, id_transport_priv = ? WHERE id_billet = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, b.getDb());
            ps.setString(2, b.getIdv());
            ps.setString(3, b.getNumPlace());
            ps.setInt(4, b.getIdDestination());
            if (b.getIdTransportPub() > 0) {
                ps.setInt(5, b.getIdTransportPub());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if (b.getIdTransportPriv() > 0) {
                ps.setInt(6, b.getIdTransportPriv());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, b.getIdBillet());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public List<Billet> show() throws SQLDataException {
        checkConnection();
        List<Billet> list = new ArrayList<>();
        String sql = "SELECT * FROM billet";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Billet b = new Billet(
                        rs.getInt("id_billet"),
                        rs.getString("db"),
                        rs.getString("idv"),
                        rs.getString("num_place"),
                        rs.getInt("id_destination"),
                        rs.getInt("id_transport_pub"),
                        rs.getInt("id_transport_priv"));
                list.add(b);
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return list;
    }

    public List<Billet> search(String keyword) throws SQLDataException {
        checkConnection();
        List<Billet> list = new ArrayList<>();
        String sql = "SELECT * FROM billet WHERE db LIKE ? OR idv LIKE ? OR num_place LIKE ? OR CAST(id_billet AS CHAR) LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String param = "%" + keyword + "%";
            ps.setString(1, param);
            ps.setString(2, param);
            ps.setString(3, param);
            ps.setString(4, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Billet b = new Billet(
                        rs.getInt("id_billet"),
                        rs.getString("db"),
                        rs.getString("idv"),
                        rs.getString("num_place"),
                        rs.getInt("id_destination"),
                        rs.getInt("id_transport_pub"),
                        rs.getInt("id_transport_priv"));
                list.add(b);
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return list;
    }
}
