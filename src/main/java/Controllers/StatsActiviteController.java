package Controllers;

import Models.Activite;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StatsActiviteController implements Initializable {

    @FXML private BarChart<String, Number> barChartPrix;
    @FXML private PieChart pieChartLieu;

    private List<Activite> activites;

    public void setActivites(List<Activite> activites) {
        this.activites = activites;
        afficherStats();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Les données seront injectées via setActivites
    }

    private void afficherStats() {
        if (activites == null || activites.isEmpty()) return;

        // 🔹 BarChart : prix par activité
        XYChart.Series<String, Number> seriesPrix = new XYChart.Series<>();
        for (Activite a : activites) {
            seriesPrix.getData().add(new XYChart.Data<>(a.getNom(), a.getPrix()));
        }
        barChartPrix.getData().clear();
        barChartPrix.getData().add(seriesPrix);

        // 🔹 PieChart : répartition par lieu
        Map<String, Long> countByLieu = activites.stream()
                .collect(Collectors.groupingBy(Activite::getLieu, Collectors.counting()));

        PieChart.Data[] pieData = countByLieu.entrySet().stream()
                .map(e -> new PieChart.Data(e.getKey(), e.getValue()))
                .toArray(PieChart.Data[]::new);

        pieChartLieu.setData(FXCollections.observableArrayList(pieData));
    }
}
