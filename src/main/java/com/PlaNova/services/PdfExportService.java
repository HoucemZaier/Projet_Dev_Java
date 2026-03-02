package com.PlaNova.services;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfExportService {

    /**
     * Exports the travel plan text to a PDF file.
     * 
     * @param title    The title of the itinerary
     * @param planText The AI-generated plan text
     * @param file     The file location to save
     */
    public void exportItinerary(String title, String planText, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Title Font
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);

        // Body Font
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        // Split planText by lines to handle simple formatting
        String[] lines = planText.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("###") || line.trim().startsWith("##") || line.trim().startsWith("#")) {
                // Header style for markdown headers
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                Paragraph h = new Paragraph(line.replaceAll("#+", "").trim(), headerFont);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                document.add(h);
            } else {
                Paragraph p = new Paragraph(line, bodyFont);
                p.setSpacingAfter(2);
                document.add(p);
            }
        }

        document.close();
    }

    public void exportBilletPdf(String title, String planText, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // 1. COLORS & FONTS
        Color mainBlue = new Color(30, 145, 214);
        Color textDark = new Color(51, 51, 51);
        
        Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, mainBlue);
        Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, mainBlue);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, textDark);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

        // 2. HEADER
        Paragraph header = new Paragraph("PLANONA | TRAVEL BILLET", h1);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(20);
        document.add(header);

        // 3. MAIN TICKET TABLE (Modern Layout)
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        // Parse PlanText into Map for easy access
        java.util.Map<String, String> data = new java.util.HashMap<>();
        for (String line : planText.split("\n")) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                data.put(parts[0].trim(), parts[1].trim());
            }
        }

        // Add Data Cells
        addTicketCell(table, "DEPARTURE", data.getOrDefault("Departure", "N/A"), labelFont, valueFont);
        addTicketCell(table, "ARRIVAL", data.getOrDefault("Arrival", "N/A"), labelFont, valueFont);
        
        addTicketCell(table, "SEAT", data.getOrDefault("Seat Number", "N/A"), labelFont, valueFont);
        addTicketCell(table, "DESTINATION", data.getOrDefault("Destination", "N/A"), labelFont, valueFont);
        
        addTicketCell(table, "TRANSPORT", data.getOrDefault("Transport Info", "N/A"), labelFont, valueFont);
        addTicketCell(table, "ACTIVITY", data.getOrDefault("Activity", "N/A"), labelFont, valueFont);

        document.add(table);

        // 4. PRICE SECTION
        Paragraph priceLine = new Paragraph("--------------------------------------------------------------------------------", smallFont);
        priceLine.setAlignment(Element.ALIGN_CENTER);
        document.add(priceLine);

        Paragraph total = new Paragraph("TOTAL PAID: " + data.getOrDefault("Total Price", "0.00 EUR"), h2);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingAfter(10);
        document.add(total);

        Paragraph status = new Paragraph("STATUS: " + data.getOrDefault("Status", "PAID"), valueFont);
        status.setAlignment(Element.ALIGN_RIGHT);
        status.setSpacingAfter(30);
        document.add(status);

        // 5. QR CODE & LOGO (Bottom)
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        
        // Add QR placeholder
        try {
            java.net.URL imageUrl = getClass().getResource("/images/qrcode.png");
            if (imageUrl != null) {
                Image img = Image.getInstance(imageUrl);
                img.scaleToFit(100, 100);
                PdfPCell qrCell = new PdfPCell(img);
                qrCell.setBorder(PdfPCell.NO_BORDER);
                qrCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                footerTable.addCell(qrCell);
            } else {
                footerTable.addCell("");
            }
        } catch (Exception e) { footerTable.addCell(""); }

        // Add Date & Info
        PdfPCell infoCell = new PdfPCell(new Paragraph("Generated on: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()) + "\nThank you for choosing PlaNova!", smallFont));
        infoCell.setBorder(PdfPCell.NO_BORDER);
        infoCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footerTable.addCell(infoCell);

        document.add(footerTable);

        document.close();
    }

    private void addTicketCell(PdfPTable table, String label, String value, Font lFont, Font vFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorder(PdfPCell.BOX);
        cell.setBorderColor(new Color(220, 220, 220));
        cell.setBackgroundColor(new Color(250, 252, 255));
        
        Paragraph pLabel = new Paragraph(label, lFont);
        Paragraph pValue = new Paragraph(value != null ? value : "N/A", vFont);
        
        cell.addElement(pLabel);
        cell.addElement(pValue);
        table.addCell(cell);
    }
}
