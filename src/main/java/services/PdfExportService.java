package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import models.Chambre;
import models.Hotel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour l'export PDF des statistiques hôtels et chambres
 * Génère un rapport professionnel complet
 */
public class PdfExportService {

    // Couleurs Planova
    private static final BaseColor COLOR_PRIMARY = new BaseColor(42, 59, 76);    // #2A3B4C
    private static final BaseColor COLOR_ACCENT  = new BaseColor(13, 162, 231);  // #0DA2E7
    private static final BaseColor COLOR_SUCCESS = new BaseColor(16, 185, 129);  // #10b981
    private static final BaseColor COLOR_DANGER  = new BaseColor(239, 68, 68);   // #ef4444
    private static final BaseColor COLOR_WARNING = new BaseColor(245, 158, 11);  // #f59e0b
    private static final BaseColor COLOR_LIGHT   = new BaseColor(248, 250, 252); // #f8fafc
    private static final BaseColor COLOR_BORDER  = new BaseColor(226, 232, 240); // #e2e8f0

    /**
     * Exporte le rapport complet des statistiques hôtels & chambres
     */
    public static void exporterRapportStatistiques(
            String filePath,
            List<Hotel> hotels,
            List<Chambre> chambres) throws DocumentException, IOException {

        Document document = new Document(PageSize.A4, 40, 40, 60, 40);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Event pour header/footer sur chaque page
        writer.setPageEvent(new HeaderFooterPageEvent());

        document.open();

        // ===== PAGE 1 : Titre + Résumé Exécutif =====
        addTitrePrincipal(document);
        addInfoGeneration(document);
        addResumExecutif(document, hotels, chambres);
        addStatistiquesGlobales(document, hotels, chambres);

        document.newPage();

        // ===== PAGE 2 : Liste des Hôtels avec leurs Chambres =====
        addTitreSection(document, "📋 Inventaire Détaillé", COLOR_PRIMARY);
        addListeHotelsAvecChambres(document, hotels, chambres);

        document.newPage();

        // ===== PAGE 3 : Analyse Statistique =====
        addTitreSection(document, "📊 Analyse & Répartitions", COLOR_ACCENT);
        addAnalyseChambresParType(document, chambres);
        addAnalyseStatuts(document, chambres);
        addAnalysePrixParHotel(document, hotels, chambres);

        document.close();
    }

    private static void addTitrePrincipal(Document doc) throws DocumentException {
        // Bloc titre avec fond coloré
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(COLOR_PRIMARY);
        titleCell.setPadding(25);
        titleCell.setBorder(Rectangle.NO_BORDER);

        // Logo / Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, BaseColor.WHITE);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 13, new BaseColor(200, 210, 220));

        Paragraph mainTitle = new Paragraph("🏨 PLANOVA", titleFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);

        Paragraph subtitle = new Paragraph("Rapport de Statistiques — Hôtels & Chambres", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(5);

        titleCell.addElement(mainTitle);
        titleCell.addElement(subtitle);
        titleTable.addCell(titleCell);

        doc.add(titleTable);
        doc.add(Chunk.NEWLINE);
    }

    private static void addInfoGeneration(Document doc) throws DocumentException {
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(100, 116, 139));
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        Paragraph info = new Paragraph("Rapport généré le " + dateStr + " • Système de gestion Planova", infoFont);
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(15);
        doc.add(info);
    }

    private static void addResumExecutif(Document doc, List<Hotel> hotels, List<Chambre> chambres)
            throws DocumentException {

        int totalHotels = hotels.size();
        int totalChambres = chambres.size();
        long disponibles = chambres.stream().filter(c -> "Disponible".equalsIgnoreCase(c.getStatutChambre())).count();
        long occupees = chambres.stream().filter(c -> "Occupée".equalsIgnoreCase(c.getStatutChambre())).count();
        double tauxOccupation = totalChambres > 0 ? (double) occupees / totalChambres * 100 : 0;
        double prixMoyen = chambres.stream().mapToDouble(Chambre::getPrixChambre).average().orElse(0);

        // Tableau des KPI cards
        PdfPTable kpiTable = new PdfPTable(4);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingBefore(10);
        kpiTable.setSpacingAfter(15);

        addKpiCard(kpiTable, "Hôtels", String.valueOf(totalHotels), COLOR_ACCENT);
        addKpiCard(kpiTable, "Chambres Total", String.valueOf(totalChambres), COLOR_PRIMARY);
        addKpiCard(kpiTable, "Disponibles", String.valueOf(disponibles), COLOR_SUCCESS);
        addKpiCard(kpiTable, "Taux Occupation", String.format("%.1f%%", tauxOccupation), COLOR_WARNING);

        doc.add(kpiTable);

        PdfPTable kpiTable2 = new PdfPTable(3);
        kpiTable2.setWidthPercentage(75);
        kpiTable2.setHorizontalAlignment(Element.ALIGN_CENTER);
        kpiTable2.setSpacingAfter(20);

        addKpiCard(kpiTable2, "Chambres Occupées", String.valueOf(occupees), COLOR_DANGER);
        addKpiCard(kpiTable2, "Prix Moyen/Nuit", String.format("%.0f TND", prixMoyen), COLOR_ACCENT);
        addKpiCard(kpiTable2, "Moy. Chambres/Hôtel",
                String.format("%.1f", totalHotels > 0 ? (double) totalChambres / totalHotels : 0), COLOR_PRIMARY);

        doc.add(kpiTable2);
    }

    private static void addKpiCard(PdfPTable table, String label, String value, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(color);
        cell.setBorderWidth(2);
        cell.setBackgroundColor(new BaseColor(
                Math.min(255, color.getRed() + 200),
                Math.min(255, color.getGreen() + 200),
                Math.min(255, color.getBlue() + 200)));

        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, color);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(71, 85, 105));

        Paragraph val = new Paragraph(value, valueFont);
        val.setAlignment(Element.ALIGN_CENTER);
        Paragraph lbl = new Paragraph(label, labelFont);
        lbl.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(val);
        cell.addElement(lbl);
        table.addCell(cell);
    }

    private static void addStatistiquesGlobales(Document doc, List<Hotel> hotels, List<Chambre> chambres)
            throws DocumentException {

        // Distribution par étoiles
        addTitreSection(doc, "⭐ Distribution des Hôtels par Étoiles", COLOR_PRIMARY);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(10);

        // Header
        addTableHeader(table, new String[]{"Classement", "Nombre d'hôtels", "Répartition"});

        for (int i = 5; i >= 1; i--) {
            final int etoiles = i;
            long count = hotels.stream().filter(h -> h.getNombreEtoile() == etoiles).count();
            if (count == 0) continue;

            String stars = "⭐".repeat(etoiles);
            double pct = hotels.size() > 0 ? (double) count / hotels.size() * 100 : 0;

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_PRIMARY);
            addTableRow(table, new String[]{stars + " (" + etoiles + " étoiles)", String.valueOf(count),
                    String.format("%.1f%%", pct)}, cellFont, false);
        }
        doc.add(table);
    }

    private static void addListeHotelsAvecChambres(Document doc, List<Hotel> hotels, List<Chambre> chambres)
            throws DocumentException {

        Map<Integer, List<Chambre>> chambresByHotel = chambres.stream()
                .collect(Collectors.groupingBy(Chambre::getIdHotel));

        for (Hotel hotel : hotels) {
            List<Chambre> hotelChambres = chambresByHotel.getOrDefault(hotel.getIdHotel(), List.of());

            // Bloc hôtel
            PdfPTable hotelBlock = new PdfPTable(1);
            hotelBlock.setWidthPercentage(100);
            hotelBlock.setSpacingBefore(12);
            hotelBlock.setSpacingAfter(5);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(COLOR_PRIMARY);
            headerCell.setPadding(10);
            headerCell.setBorder(Rectangle.NO_BORDER);

            Font hotelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            Font hotelInfo = FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(180, 200, 220));

            String stars = "★".repeat(hotel.getNombreEtoile());
            Paragraph hotelTitle = new Paragraph(hotel.getNomHotel() + "  " + stars, hotelFont);
            Paragraph hotelSubtitle = new Paragraph(hotel.getAdresse() + " • " + hotel.getVille() +
                    " • " + hotelChambres.size() + " chambre(s)", hotelInfo);

            headerCell.addElement(hotelTitle);
            headerCell.addElement(hotelSubtitle);
            hotelBlock.addCell(headerCell);
            doc.add(hotelBlock);

            if (hotelChambres.isEmpty()) {
                Font emptyFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new BaseColor(100, 116, 139));
                doc.add(new Paragraph("   Aucune chambre enregistrée pour cet hôtel.", emptyFont));
            } else {
                // Tableau des chambres
                PdfPTable chambresTable = new PdfPTable(6);
                chambresTable.setWidthPercentage(100);
                float[] widths = {0.8f, 1.5f, 1f, 1f, 1.2f, 2f};
                chambresTable.setWidths(widths);

                addTableHeader(chambresTable, new String[]{"ID", "Type", "Capacité", "Prix/Nuit", "Statut", "Équipements"});

                boolean alt = false;
                for (Chambre c : hotelChambres) {
                    Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_PRIMARY);

                    // Couleur du statut
                    BaseColor statutColor = COLOR_PRIMARY;
                    switch (c.getStatutChambre() != null ? c.getStatutChambre().toLowerCase() : "") {
                        case "disponible": statutColor = COLOR_SUCCESS; break;
                        case "occupée": statutColor = COLOR_DANGER; break;
                        case "en maintenance": statutColor = COLOR_WARNING; break;
                    }

                    PdfPCell[] cells = {
                            createCell(c.getTypeChambre(), rowFont, alt),
                            createCell(c.getCapacite() + " pers.", rowFont, alt),
                            createCell(String.format("%.0f TND", c.getPrixChambre()), rowFont, alt),
                            createStatusCell(c.getStatutChambre(), statutColor),
                            createCell(c.getEquipement() != null ? c.getEquipement() : "-", rowFont, alt)
                    };

                    for (PdfPCell cell : cells) chambresTable.addCell(cell);
                    alt = !alt;
                }
                doc.add(chambresTable);
            }
        }
    }

    private static void addAnalyseChambresParType(Document doc, List<Chambre> chambres) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
        Paragraph titre = new Paragraph("Répartition des Chambres par Type", sectionFont);
        titre.setSpacingBefore(10);
        titre.setSpacingAfter(8);
        doc.add(titre);

        Map<String, Long> parType = chambres.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getTypeChambre() != null ? c.getTypeChambre() : "Inconnu",
                        Collectors.counting()));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(85);
        table.setSpacingAfter(15);
        addTableHeader(table, new String[]{"Type de Chambre", "Nombre", "% du total", "Prix moyen"});

        int total = chambres.size();
        for (Map.Entry<String, Long> entry : parType.entrySet()) {
            String type = entry.getKey();
            long count = entry.getValue();
            double pct = total > 0 ? (double) count / total * 100 : 0;
            double avgPrice = chambres.stream()
                    .filter(c -> type.equals(c.getTypeChambre()))
                    .mapToDouble(Chambre::getPrixChambre)
                    .average().orElse(0);

            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_PRIMARY);
            addTableRow(table, new String[]{
                    type, String.valueOf(count),
                    String.format("%.1f%%", pct), String.format("%.0f TND", avgPrice)
            }, rowFont, false);
        }
        doc.add(table);
    }

    private static void addAnalyseStatuts(Document doc, List<Chambre> chambres) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
        Paragraph titre = new Paragraph("Analyse des Statuts d'Occupation", sectionFont);
        titre.setSpacingBefore(5);
        titre.setSpacingAfter(8);
        doc.add(titre);

        Map<String, Long> parStatut = chambres.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStatutChambre() != null ? c.getStatutChambre() : "Inconnu",
                        Collectors.counting()));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(65);
        table.setSpacingAfter(15);
        addTableHeader(table, new String[]{"Statut", "Chambres", "Pourcentage"});

        int total = chambres.size();
        for (Map.Entry<String, Long> entry : parStatut.entrySet()) {
            long count = entry.getValue();
            double pct = total > 0 ? (double) count / total * 100 : 0;

            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_PRIMARY);
            addTableRow(table, new String[]{
                    entry.getKey(), String.valueOf(count), String.format("%.1f%%", pct)
            }, rowFont, false);
        }
        doc.add(table);
    }

    private static void addAnalysePrixParHotel(Document doc, List<Hotel> hotels, List<Chambre> chambres)
            throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_PRIMARY);
        Paragraph titre = new Paragraph("Analyse des Prix par Hôtel", sectionFont);
        titre.setSpacingBefore(5);
        titre.setSpacingAfter(8);
        doc.add(titre);

        Map<Integer, List<Chambre>> byHotel = chambres.stream()
                .collect(Collectors.groupingBy(Chambre::getIdHotel));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);
        addTableHeader(table, new String[]{"Hôtel", "Étoiles", "Nb Chambres", "Prix Min", "Prix Max"});

        boolean alt = false;
        for (Hotel hotel : hotels) {
            List<Chambre> hChambres = byHotel.getOrDefault(hotel.getIdHotel(), List.of());
            double minPrix = hChambres.stream().mapToDouble(Chambre::getPrixChambre).min().orElse(0);
            double maxPrix = hChambres.stream().mapToDouble(Chambre::getPrixChambre).max().orElse(0);
            String stars = "★".repeat(hotel.getNombreEtoile());

            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_PRIMARY);
            addTableRow(table, new String[]{
                    hotel.getNomHotel(), stars, String.valueOf(hChambres.size()),
                    String.format("%.0f TND", minPrix), String.format("%.0f TND", maxPrix)
            }, rowFont, alt);
            alt = !alt;
        }
        doc.add(table);
    }

    // ===== HELPERS =====

    private static void addTitreSection(Document doc, String titre, BaseColor color) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, color);
        Paragraph p = new Paragraph(titre, font);
        p.setSpacingBefore(15);
        p.setSpacingAfter(10);

        // Ligne décorative
        LineSeparator line = new LineSeparator(1, 100, color, Element.ALIGN_LEFT, -2);
        doc.add(p);
        doc.add(line);
        doc.add(Chunk.NEWLINE);
    }

    private static void addTableHeader(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(8);
            cell.setBorderColor(COLOR_ACCENT);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private static void addTableRow(PdfPTable table, String[] values, Font font, boolean alternate) {
        BaseColor bg = alternate ? COLOR_LIGHT : BaseColor.WHITE;
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "-", font));
            cell.setBackgroundColor(bg);
            cell.setPadding(7);
            cell.setBorderColor(COLOR_BORDER);
            table.addCell(cell);
        }
    }

    private static PdfPCell createCell(String text, Font font, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
        cell.setBackgroundColor(alternate ? COLOR_LIGHT : BaseColor.WHITE);
        cell.setPadding(7);
        cell.setBorderColor(COLOR_BORDER);
        return cell;
    }

    private static PdfPCell createStatusCell(String statut, BaseColor color) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, color);
        PdfPCell cell = new PdfPCell(new Phrase(statut != null ? statut : "-", font));
        cell.setPadding(7);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBackgroundColor(new BaseColor(
                Math.min(255, color.getRed() + 190),
                Math.min(255, color.getGreen() + 190),
                Math.min(255, color.getBlue() + 190)));
        return cell;
    }

    /**
     * Page event pour ajouter header/footer sur chaque page
     */
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(148, 163, 184));

            // Footer gauche
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("© Planova — Gestion Hôtelière", footerFont),
                    document.leftMargin(), 30, 0);

            // Footer droite - numéro de page
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Page " + writer.getPageNumber(), footerFont),
                    document.right(), 30, 0);

            // Ligne de séparation footer
            cb.setColorStroke(new BaseColor(226, 232, 240));
            cb.setLineWidth(0.5f);
            cb.moveTo(document.leftMargin(), 38);
            cb.lineTo(document.right(), 38);
            cb.stroke();
        }
    }
}
