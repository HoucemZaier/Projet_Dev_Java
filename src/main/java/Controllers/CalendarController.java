package Controllers;

import Models.Activite;
import Models.Excursion;
import Services.ServiceActivite;
import Services.ServiceExcursion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.sql.SQLDataException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class CalendarController implements Initializable {

    @FXML private WebView calendarWebView;
    @FXML private Label statusLabel;

    private final ServiceActivite serviceActivite = new ServiceActivite();
    private final ServiceExcursion serviceExcursion = new ServiceExcursion();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCalendar();
    }

    private void loadCalendar() {
        try {
            StringBuilder events = new StringBuilder("[");
            boolean first = true;

            // ==============================
            // ACTIVITES (BLEU)
            // ==============================
            List<Activite> activites = serviceActivite.recuperer();
            for (Activite a : activites) {
                if (a.getDateActivite() == null) continue;
                if (!first) events.append(",");
                first = false;

                String heure = a.getHeureActivite() != null
                        ? a.getHeureActivite().toString().substring(0, 5)
                        : "09:00";

                events.append("{")
                        .append("id:'a").append(a.getIdActivite()).append("',")
                        .append("title:'🎯 ").append(escape(a.getNom())).append("',")
                        .append("start:'").append(a.getDateActivite()).append("T").append(heure).append("',")
                        .append("backgroundColor:'#0DA2E7',")
                        .append("borderColor:'#0284c7',")
                        .append("textColor:'white',")
                        .append("extendedProps:{")
                        .append("type:'activite',")
                        .append("description:'").append(escape(a.getDescription())).append("',")
                        .append("lieu:'").append(escape(a.getLieu())).append("',")
                        .append("prix:'").append(a.getPrix()).append(" DT',")
                        .append("heure:'").append(heure).append("'")
                        .append("}}");
            }

            // ==============================
            // EXCURSIONS (VERT / MULTI-JOUR)
            // ==============================
            List<Excursion> excursions = serviceExcursion.recuperer();
            for (Excursion e : excursions) {
                if (e.getDateDepart() == null) continue;
                if (!first) events.append(",");
                first = false;

                LocalDate dateDepart = e.getDateDepart().toLocalDate();
                LocalDate dateFin = e.getDateRetour() != null
                        ? e.getDateRetour().toLocalDate().plusDays(1)
                        : dateDepart.plusDays(1);

                String couleur = switch (e.getStatut() == null ? "" : e.getStatut().toLowerCase()) {
                    case "ouverte" -> "#10b981";
                    case "complète" -> "#f59e0b";
                    case "annulée" -> "#ef4444";
                    default -> "#64748b";
                };

                events.append("{")
                        .append("id:'e").append(e.getIdExcursion()).append("',")
                        .append("title:'🗺️ ").append(escape(e.getTitre())).append("',")
                        .append("start:'").append(dateDepart).append("',")
                        .append("end:'").append(dateFin).append("',")
                        .append("allDay:true,")
                        .append("backgroundColor:'").append(couleur).append("',")
                        .append("borderColor:'").append(couleur).append("',")
                        .append("textColor:'white',")
                        .append("extendedProps:{")
                        .append("type:'excursion',")
                        .append("destination:'").append(escape(e.getNomDestination())).append("',")
                        .append("prix:'").append(e.getPrix()).append(" DT',")
                        .append("places:'").append(e.getNbPlaces()).append("',")
                        .append("statut:'").append(escape(e.getStatut())).append("'")
                        .append("}}");
            }

            events.append("]");

            String html = buildCalendarHtml(events.toString());
            WebEngine engine = calendarWebView.getEngine();
            engine.loadContent(html);

            statusLabel.setText("✅ Calendrier chargé — "
                    + activites.size() + " activité(s) | "
                    + excursions.size() + " excursion(s)");

        } catch (SQLDataException ex) {
            statusLabel.setText("❌ Erreur : " + ex.getMessage());
        }
    }

    private String buildCalendarHtml(String eventsJson) {
        return String.format("""
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.10/index.global.min.css"/>
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.10/index.global.min.js"></script>
</head>
<body>
<div id="calendar"></div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    var calendar = new FullCalendar.Calendar(
        document.getElementById('calendar'),
        {
            initialView: 'dayGridMonth',
            locale: 'fr',
            events: %s
        }
    );
    calendar.render();
});
</script>
</body>
</html>
""", eventsJson);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("'", "\\'")
                .replace("\"", "&quot;")
                .replace("\n", " ");
    }

    @FXML
    private void handleRefresh() {
        statusLabel.setText("🔄 Rechargement...");
        loadCalendar();
    }
}