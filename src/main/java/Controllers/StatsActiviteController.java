package Controllers;

import Models.Activite;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StatsActiviteController implements Initializable {

    @FXML private BarChart<String, Number> barChartPrix;
    @FXML private PieChart pieChartLieu;
    @FXML private Button btnReset;



    private List<Activite> activites;

    public void setActivites(List<Activite> activites) {
        this.activites = activites;
        afficherStats();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnReset.setOnAction(e -> resetCharts());
    }

    private void afficherStats() {
        if (activites == null || activites.isEmpty()) return;

        // Total pourcentage
        double totalPrix = activites.stream().mapToDouble(Activite::getPrix).sum();

        // ================== BarChart ==================
        XYChart.Series<String, Number> seriesPrix = new XYChart.Series<>();
        activites.forEach(a -> seriesPrix.getData().add(new XYChart.Data<>(a.getNom(), a.getPrix())));
        barChartPrix.getData().clear();
        barChartPrix.getData().add(seriesPrix);

        // Tooltips et clic interactif pour barChart
        barChartPrix.getData().forEach(s -> s.getData().forEach(d -> {
            d.getNode().setOpacity(1.0);
            double percent = d.getYValue().doubleValue() * 100 / totalPrix;
            Tooltip.install(d.getNode(),
                    new Tooltip(d.getXValue() + " : " + d.getYValue() + " DT (" + String.format("%.1f", percent) + "%)"));
            d.getNode().setOnMouseClicked(ev ->
                    barChartPrix.getData().forEach(ss -> ss.getData().forEach(dd -> dd.getNode().setOpacity(dd == d ? 1.0 : 0.3)))
            );
            d.getNode().setOnMouseEntered(ev -> {
                d.getNode().setScaleX(1.05);
                d.getNode().setScaleY(1.05);
            });
            d.getNode().setOnMouseExited(ev -> {
                d.getNode().setScaleX(1.0);
                d.getNode().setScaleY(1.0);
            });
        }));

        // ================== PieChart ==================
        Map<String, Long> countByLieu = activites.stream()
                .collect(Collectors.groupingBy(Activite::getLieu, Collectors.counting()));

        pieChartLieu.setData(FXCollections.observableArrayList(
                countByLieu.entrySet().stream()
                        .map(e -> {
                            PieChart.Data data = new PieChart.Data(e.getKey(), e.getValue());
                            double pourcentage = e.getValue() * 100.0 / activites.size();
                            data.setName(e.getKey() + " (" + String.format("%.1f", pourcentage) + "%)");
                            return data;
                        }).collect(Collectors.toList())
        ));

        pieChartLieu.getData().forEach(data -> {
            data.getNode().setOpacity(1.0);
            Tooltip.install(data.getNode(), new Tooltip(data.getName() + "\nNb activités: " + (int) data.getPieValue()));
            data.getNode().setOnMouseClicked(ev -> pieChartLieu.getData().forEach(d -> d.getNode().setOpacity(d == data ? 1.0 : 0.3)));
            data.getNode().setOnMouseEntered(ev -> {
                data.getNode().setScaleX(1.05);
                data.getNode().setScaleY(1.05);
            });
            data.getNode().setOnMouseExited(ev -> {
                data.getNode().setScaleX(1.0);
                data.getNode().setScaleY(1.0);
            });
        });
    }

    // Réinitialiser les graphiques
    private void resetCharts() {
        barChartPrix.getData().forEach(s -> s.getData().forEach(d -> d.getNode().setOpacity(1.0)));
        pieChartLieu.getData().forEach(d -> d.getNode().setOpacity(1.0));
    }
}