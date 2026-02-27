package services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import models.Chambre;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour la génération de QR codes des chambres
 * Chaque QR code contient toutes les informations de la chambre
 */
public class QRCodeService {

    private static final int QR_SIZE = 150;

    /**
     * Génère un QR code pour une chambre avec ses informations complètes
     * @param chambre La chambre pour laquelle générer le QR code
     * @param nomHotel Le nom de l'hôtel associé
     * @return Image JavaFX du QR code
     */
    public static Image genererQRCodeChambre(Chambre chambre, String nomHotel) {
        String contenu = buildQRContent(chambre, nomHotel);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = writer.encode(contenu, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
            BufferedImage bufferedImage = matrixToImage(bitMatrix);

            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (WriterException e) {
            System.err.println("Erreur génération QR Code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Construit le contenu textuel du QR code (format lisible)
     */
    private static String buildQRContent(Chambre chambre, String nomHotel) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CHAMBRE INFO ===\n");
        sb.append("Hôtel: ").append(nomHotel != null ? nomHotel : "N/A").append("\n");
        sb.append("Type: ").append(chambre.getTypeChambre()).append("\n");
        sb.append("Capacité: ").append(chambre.getCapacite()).append(" pers.\n");
        sb.append("Prix: ").append(String.format("%.2f TND/nuit", chambre.getPrixChambre())).append("\n");
        sb.append("Statut: ").append(chambre.getStatutChambre()).append("\n");

        if (chambre.getEquipement() != null && !chambre.getEquipement().isEmpty()) {
            sb.append("Équipements: ").append(chambre.getEquipement()).append("\n");
        }

        if (chambre.getDescription() != null && !chambre.getDescription().isEmpty()) {
            sb.append("Description: ").append(chambre.getDescription()).append("\n");
        }

        sb.append("===================");
        return sb.toString();
    }

    /**
     * Convertit une BitMatrix en BufferedImage avec style personnalisé
     */
    private static BufferedImage matrixToImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Couleurs : fond blanc, modules bleu foncé
        int darkColor = new Color(42, 59, 76).getRGB();   // #2A3B4C
        int lightColor = Color.WHITE.getRGB();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? darkColor : lightColor);
            }
        }

        return image;
    }

    /**
     * Génère un QR code de taille personnalisée
     */
    public static Image genererQRCodeChambre(Chambre chambre, String nomHotel, int size) {
        String contenu = buildQRContent(chambre, nomHotel);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = writer.encode(contenu, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage bufferedImage = matrixToImage(bitMatrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (WriterException e) {
            return null;
        }
    }
}
