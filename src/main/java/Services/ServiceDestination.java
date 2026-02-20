package Services;

import Models.Destination;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDestination {

    private final Connection connection;

    public ServiceDestination() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Récupérer tous les noms de destinations pour le ComboBox
     */
    public List<String> getAllDestinationNames() throws SQLException {
        List<String> destinations = new ArrayList<>();
        String sql = "SELECT nom_destination FROM destination";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                destinations.add(rs.getString("nom_destination"));
            }
        }
        return destinations;
    }
}