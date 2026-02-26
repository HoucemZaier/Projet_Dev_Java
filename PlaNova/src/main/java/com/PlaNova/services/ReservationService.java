package com.PlaNova.services;

import com.PlaNova.models.Reservation;
import com.PlaNova.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements Iservice<Reservation> {
    private final Connection connection;

    public ReservationService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    private void checkConnection() throws SQLDataException {
        if (connection == null) {
            throw new SQLDataException("Database connection is not available. Please check your MySQL server.");
        }
    }

    @Override
    public void add(Reservation r) throws SQLDataException {
        checkConnection();
        String sql = "INSERT INTO reservation (id_utilisateur, id_destination, id_hotel, id_chambre, transport_type, id_transport, date_debut, date_fin, prix_total, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getIdUtilisateur());
            ps.setInt(2, r.getIdDestination());
            if (r.getIdHotel() != null) {
                ps.setInt(3, r.getIdHotel());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (r.getIdChambre() != null) {
                ps.setInt(4, r.getIdChambre());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (r.getTransportType() != null) {
                ps.setString(5, r.getTransportType());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            if (r.getIdTransport() != null) {
                ps.setInt(6, r.getIdTransport());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setDate(7, Date.valueOf(r.getDateDebut()));
            ps.setDate(8, Date.valueOf(r.getDateFin()));
            ps.setDouble(9, r.getPrixTotal());
            // Default logic if not provided
            if (r.getStatus() != null && !r.getStatus().isEmpty()) {
                ps.setString(10, r.getStatus());
            } else {
                ps.setString(10, "en_attente");
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setIdReservation(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void delete(Reservation r) throws SQLDataException {
        checkConnection();
        String sql = "DELETE FROM reservation WHERE id_reservation = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, r.getIdReservation());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void modify(Reservation r) throws SQLDataException {
        checkConnection();
        String sql = "UPDATE reservation SET id_utilisateur = ?, id_destination = ?, id_hotel = ?, id_chambre = ?, transport_type = ?, id_transport = ?, date_debut = ?, date_fin = ?, prix_total = ?, status = ? WHERE id_reservation = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, r.getIdUtilisateur());
            ps.setInt(2, r.getIdDestination());
            if (r.getIdHotel() != null) {
                ps.setInt(3, r.getIdHotel());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (r.getIdChambre() != null) {
                ps.setInt(4, r.getIdChambre());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (r.getTransportType() != null) {
                ps.setString(5, r.getTransportType());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            if (r.getIdTransport() != null) {
                ps.setInt(6, r.getIdTransport());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setDate(7, Date.valueOf(r.getDateDebut()));
            ps.setDate(8, Date.valueOf(r.getDateFin()));
            ps.setDouble(9, r.getPrixTotal());
            ps.setString(10, r.getStatus());
            ps.setInt(11, r.getIdReservation());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public List<Reservation> show() throws SQLDataException {
        checkConnection();
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setIdReservation(rs.getInt("id_reservation"));
                r.setIdUtilisateur(rs.getInt("id_utilisateur"));
                r.setIdDestination(rs.getInt("id_destination"));

                int hotelId = rs.getInt("id_hotel");
                if (!rs.wasNull())
                    r.setIdHotel(hotelId);

                int chambreId = rs.getInt("id_chambre");
                if (!rs.wasNull())
                    r.setIdChambre(chambreId);

                r.setTransportType(rs.getString("transport_type"));

                int transportId = rs.getInt("id_transport");
                if (!rs.wasNull())
                    r.setIdTransport(transportId);

                r.setDateDebut(rs.getDate("date_debut").toLocalDate());
                r.setDateFin(rs.getDate("date_fin").toLocalDate());
                r.setPrixTotal(rs.getDouble("prix_total"));
                r.setStatus(rs.getString("status"));

                list.add(r);
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
        return list;
    }
}
