package com.PlaNova.controllers;

import com.itextpdf.text.DocumentException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import com.PlaNova.models.Chambre;
import com.PlaNova.models.Hotel;
import com.PlaNova.services.PdfExportService;
import com.PlaNova.services.ServiceChambre;
import com.PlaNova.services.ServiceHotel;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.sql.SQLDataException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatistiquesController {

    // Constants pour les couleurs
    private static final class ChartColors {
        static final String[] PIE_COLORS = { "#10b981", "#ef4444", "#f59e0b", "#3b82f6", "#8b5cf6" };
        static final String[] BAR_COLORS = { "#10b981", "#0DA2E7", "#f59e0b" };
        static final String[] TYPE_COLORS = { "#0DA2E7", "#8b5cf6", "#10b981", "#f59e0b", "#ef4444" };
        static final String PRIMARY_COLOR = "#2A3B4C";
    }

    // Constants pour les en-têtes de tableau
    private static final class TableHeaders {
        static final String[] HEADERS = { "Hôtel", "Étoiles", "Chambres", "Disponibles", "Occupées", "Taux",
                "Prix Moy." };
        static final double[] WIDTHS = { 220, 80, 80, 90, 80, 100, 100 };
    }

    // ===== KPI Labels =====
    @FXML
    private Label totalHotelsLabel;
    @FXML
    private Label totalChambresLabel;
    @FXML
    private Label tauxOccupationLabel;
    @FXML
    private Label prixMoyenLabel;
    @FXML
    private Label disponiblesLabel;
    @FXML
    private Label occupeesLabel;
    @FXML
    private Label revenusEstimesLabel;

    // ===== Charts =====
    @FXML
    private BarChart<String, Number> chambresParHotelChart;
    @FXML
    private PieChart statutChart;
    @FXML
    private BarChart<String, Number> prixParHotelChart;
    @FXML
    private BarChart<String, Number> typeChambresChart;

    // ===== Table detail hôtels =====
    @FXML
    private VBox tableContainer;

    // ===== Progress indicators =====
    @FXML
    private ProgressBar occupationBar;
    @FXML
    private Label occupationPctLabel;

    // ===== Buttons =====
    @FXML
    private Button exportPdfBtn;
    @FXML
    private Button refreshBtn;

    private ServiceHotel serviceHotel;
    private ServiceChambre serviceChambre;
    private List<Hotel> hotels;
    private List<Chambre> chambres;

    @FXML
    public void initialize() {
        serviceHotel = new ServiceHotel();
        serviceChambre = new ServiceChambre();

        FontIcon pdfIcon = new FontIcon("fas-file-pdf");
        pdfIcon.setIconSize(14);
        pdfIcon.setIconColor(Color.WHITE);
        exportPdfBtn.setGraphic(pdfIcon);

        FontIcon refreshIcon = new FontIcon("fas-sync-alt");
        refreshIcon.setIconSize(14);
        refreshIcon.setIconColor(Color.WHITE);
        refreshBtn.setGraphic(refreshIcon);

        loadData();
    }

    private void loadData() {
        try {
            hotels = serviceHotel.recuperer();
            chambres = serviceChambre.recuperer();

            updateKPIs();
            updateCharts();
            updateTableDetail();
            animateEntrance();

        } catch (SQLDataException e) {
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateKPIs() {
        int nbHotels = hotels.size();
        int nbChambres = chambres.size();
        long disponibles = chambres.stream().filter(c -> "Disponible".equalsIgnoreCase(c.getStatutChambre())).count();
        long occupees = chambres.stream().filter(c -> "Occupée".equalsIgnoreCase(c.getStatutChambre())).count();
        double tauxOccupation = nbChambres > 0 ? (double) occupees / nbChambres * 100 : 0;
        double prixMoyen = chambres.stream().mapToDouble(Chambre::getPrixChambre).average().orElse(0);
        double revenusEstimes = chambres.stream()
                .filter(c -> "Occupée".equalsIgnoreCase(c.getStatutChambre()))
                .mapToDouble(Chambre::getPrixChambre).sum();

        totalHotelsLabel.setText(String.valueOf(nbHotels));
        totalChambresLabel.setText(String.valueOf(nbChambres));
        disponiblesLabel.setText(String.valueOf(disponibles));
        occupeesLabel.setText(String.valueOf(occupees));
        tauxOccupationLabel.setText(String.format("%.1f%%", tauxOccupation));
        prixMoyenLabel.setText(String.format("%.0f TND", prixMoyen));
        revenusEstimesLabel.setText(String.format("%.0f TND", revenusEstimes));

        if (occupationBar != null) {
            occupationBar.setProgress(tauxOccupation / 100.0);
            occupationPctLabel.setText(String.format("%.1f%%", tauxOccupation));

            String barStyle;
            if (tauxOccupation >= 80) {
                barStyle = "-fx-accent: #ef4444;";
            } else if (tauxOccupation >= 50) {
                barStyle = "-fx-accent: #f59e0b;";
            } else {
                barStyle = "-fx-accent: #10b981;";
            }
            occupationBar.setStyle(barStyle);
        }
    }

    private void updateCharts() {
        updateChambresParHotelChart();
        updateStatutPieChart();
        updatePrixParHotelChart();
        updateTypeChambresChart();
    }

    private void updateChambresParHotelChart() {
        if (chambresParHotelChart == null)
            return;

        chambresParHotelChart.getData().clear();
        chambresParHotelChart.setAnimated(true);

        Map<Integer, Long> countByHotel = chambres.stream()
                .collect(Collectors.groupingBy(Chambre::getIdHotel, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nb Chambres");

        for (Hotel hotel : hotels) {
            long count = countByHotel.getOrDefault(hotel.getIdHotel(), 0L);
            String shortName = hotel.getNomHotel().length() > 15
                    ? hotel.getNomHotel().substring(0, 13) + "..."
                    : hotel.getNomHotel();
            series.getData().add(new XYChart.Data<>(shortName, count));
        }

        chambresParHotelChart.getData().add(series);

        Platform.runLater(() -> {
            series.getData().forEach(data -> {
                if (data.getNode() != null) {
                    data.getNode().setStyle(
                            "-fx-bar-fill: " + ChartColors.PRIMARY_COLOR + "; -fx-background-radius: 4 4 0 0;");
                }
            });
        });
    }

    private void updateStatutPieChart() {
        if (statutChart == null)
            return;

        statutChart.getData().clear();

        Map<String, Long> byStatut = chambres.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStatutChambre() != null ? c.getStatutChambre() : "Inconnu",
                        Collectors.counting()));

        byStatut.forEach(
                (statut, count) -> statutChart.getData().add(new PieChart.Data(statut + " (" + count + ")", count)));

        Platform.runLater(() -> {
            for (int i = 0; i < statutChart.getData().size(); i++) {
                PieChart.Data data = statutChart.getData().get(i);
                if (data.getNode() != null && i < ChartColors.PIE_COLORS.length) {
                    data.getNode().setStyle("-fx-pie-color: " + ChartColors.PIE_COLORS[i] + ";");
                }
            }
        });
    }

    private void updatePrixParHotelChart() {
        if (prixParHotelChart == null)
            return;

        prixParHotelChart.getData().clear();

        Map<Integer, List<Chambre>> byHotel = chambres.stream()
                .collect(Collectors.groupingBy(Chambre::getIdHotel));

        XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("Prix Min");
        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Prix Max");
        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Prix Moyen");

        for (Hotel hotel : hotels) {
            List<Chambre> hChambres = byHotel.getOrDefault(hotel.getIdHotel(), List.of());
            if (hChambres.isEmpty())
                continue;

            String shortName = hotel.getNomHotel().length() > 12
                    ? hotel.getNomHotel().substring(0, 10) + "..."
                    : hotel.getNomHotel();

            double min = hChambres.stream().mapToDouble(Chambre::getPrixChambre).min().orElse(0);
            double max = hChambres.stream().mapToDouble(Chambre::getPrixChambre).max().orElse(0);
            double avg = hChambres.stream().mapToDouble(Chambre::getPrixChambre).average().orElse(0);

            minSeries.getData().add(new XYChart.Data<>(shortName, min));
            maxSeries.getData().add(new XYChart.Data<>(shortName, max));
            avgSeries.getData().add(new XYChart.Data<>(shortName, avg));
        }

        prixParHotelChart.getData().addAll(minSeries, avgSeries, maxSeries);

        Platform.runLater(() -> {
            int si = 0;
            for (XYChart.Series<String, Number> s : prixParHotelChart.getData()) {
                final String color = ChartColors.BAR_COLORS[Math.min(si, ChartColors.BAR_COLORS.length - 1)];
                s.getData().forEach(d -> {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-radius: 4 4 0 0;");
                    }
                });
                si++;
            }
        });
    }

    private void updateTypeChambresChart() {
        if (typeChambresChart == null)
            return;

        typeChambresChart.getData().clear();

        Map<String, Long> byType = chambres.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getTypeChambre() != null ? c.getTypeChambre() : "Inconnu",
                        Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Chambres");

        byType.forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));

        typeChambresChart.getData().add(series);

        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> d = series.getData().get(i);
                if (d.getNode() != null) {
                    final String color = ChartColors.TYPE_COLORS[i % ChartColors.TYPE_COLORS.length];
                    d.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-radius: 4 4 0 0;");
                }
            }
        });
    }

    private void updateTableDetail() {
        if (tableContainer == null)
            return;
        tableContainer.getChildren().clear();

        Map<Integer, List<Chambre>> byHotel = chambres.stream()
                .collect(Collectors.groupingBy(Chambre::getIdHotel));

        HBox header = createTableRowHeader();
        tableContainer.getChildren().add(header);

        boolean alt = false;
        for (Hotel hotel : hotels) {
            List<Chambre> hChambres = byHotel.getOrDefault(hotel.getIdHotel(), List.of());
            long disponibles = hChambres.stream().filter(c -> "Disponible".equalsIgnoreCase(c.getStatutChambre()))
                    .count();
            long occupees = hChambres.stream().filter(c -> "Occupée".equalsIgnoreCase(c.getStatutChambre())).count();
            double avgPrix = hChambres.stream().mapToDouble(Chambre::getPrixChambre).average().orElse(0);
            double taux = hChambres.isEmpty() ? 0 : (double) occupees / hChambres.size() * 100;

            HBox row = createTableRow(hotel, hChambres.size(), disponibles, occupees, avgPrix, taux, alt);
            tableContainer.getChildren().add(row);
            alt = !alt;
        }

        if (hotels.isEmpty()) {
            Label empty = new Label("Aucun hôtel enregistré");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-padding: 20;");
            tableContainer.getChildren().add(empty);
        }
    }

    private HBox createTableRowHeader() {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: #2A3B4C; -fx-padding: 12 15; -fx-background-radius: 8 8 0 0;");
        row.setSpacing(0);

        for (int i = 0; i < TableHeaders.HEADERS.length; i++) {
            Label lbl = new Label(TableHeaders.HEADERS[i]);
            lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
            lbl.setPrefWidth(TableHeaders.WIDTHS[i]);
            row.getChildren().add(lbl);
        }
        return row;
    }

    private HBox createTableRow(Hotel hotel, int total, long disponibles, long occupees,
            double avgPrix, double taux, boolean alt) {
        HBox row = new HBox();
        String bgColor = alt ? "#f8fafc" : "white";
        row.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 11 15; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox nameBox = new VBox(2);
        nameBox.setPrefWidth(220);
        Label nomLbl = new Label(hotel.getNomHotel());
        nomLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1e293b;");
        Label villeLbl = new Label(hotel.getVille());
        villeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        nameBox.getChildren().addAll(nomLbl, villeLbl);

        Label starsLbl = new Label("★".repeat(hotel.getNombreEtoile()));
        starsLbl.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px;");
        starsLbl.setPrefWidth(80);

        Label totalLbl = createStatLabel(String.valueOf(total), "#1e293b", 80);
        Label dispLbl = createStatLabel(String.valueOf(disponibles), "#10b981", 90);
        Label occLbl = createStatLabel(String.valueOf(occupees), "#ef4444", 80);

        VBox tauxBox = new VBox(3);
        tauxBox.setPrefWidth(100);
        ProgressBar bar = new ProgressBar(taux / 100.0);
        bar.setPrefWidth(80);
        bar.setPrefHeight(8);
        String barColor = taux >= 80 ? "#ef4444" : taux >= 50 ? "#f59e0b" : "#10b981";
        bar.setStyle("-fx-accent: " + barColor + ";");
        Label tauxLbl = new Label(String.format("%.0f%%", taux));
        tauxLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        tauxBox.getChildren().addAll(bar, tauxLbl);

        Label prixLbl = createStatLabel(String.format("%.0f TND", avgPrix), "#0DA2E7", 100);

        row.getChildren().addAll(nameBox, starsLbl, totalLbl, dispLbl, occLbl, tauxBox, prixLbl);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #e8f4fd; -fx-padding: 11 15; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 11 15; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"));

        return row;
    }

    private Label createStatLabel(String text, String color, double width) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        lbl.setPrefWidth(width);
        return lbl;
    }

    private void animateEntrance() {
        FadeTransition ft = new FadeTransition(Duration.millis(400));
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        if (tableContainer != null) {
            ft.setNode(tableContainer);
            ft.play();
        }
    }

    @FXML
    private void handleExportPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.setInitialFileName("rapport_statistiques_hotels.pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(exportPdfBtn.getScene().getWindow());

        if (file != null) {
            try {
                exportPdfBtn.setDisable(true);
                exportPdfBtn.setText("Génération...");

                PdfExportService.exporterRapportStatistiques(file.getAbsolutePath(), hotels, chambres);

                exportPdfBtn.setDisable(false);
                exportPdfBtn.setText("Exporter PDF");

                showAlert("✅ Succès",
                        "Rapport PDF généré avec succès !\n📄 Fichier: " + file.getName(),
                        Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                exportPdfBtn.setDisable(false);
                exportPdfBtn.setText("Exporter PDF");
                showAlert("Erreur", "Impossible de générer le PDF: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}