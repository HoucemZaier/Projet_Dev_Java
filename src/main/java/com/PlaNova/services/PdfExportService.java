package com.PlaNova.services;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import com.lowagie.text.Image;

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

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);

        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        String[] lines = planText.split("\n");
        for (String line : lines) {
            Paragraph p = new Paragraph(line, bodyFont);
            p.setSpacingAfter(2);
            document.add(p);
        }

        try {
            java.net.URL imageUrl = getClass().getResource("/images/qrcode.png");
            if (imageUrl != null) {
                Image img = Image.getInstance(imageUrl);
                img.setAlignment(Image.ALIGN_CENTER);
                img.scaleToFit(150, 150);
                document.add(img);
            } else {
                System.err.println("QR Code image not found!");
            }
        } catch (Exception e) {
            System.err.println("Could not add QR Code to PDF: " + e.getMessage());
        }

        document.close();
    }
}
