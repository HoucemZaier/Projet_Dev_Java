package Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Service for Microsoft Authenticator TOTP (Time-based One-Time Password) integration
 */
public class TOTPService {

    private static final String ISSUER = "PlaNova";
    private final GoogleAuthenticator gAuth;

    public TOTPService() {
        this.gAuth = new GoogleAuthenticator();
    }

    /**
     * Generate a secret key for TOTP
     * @return the secret key as a string
     */
    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * Generate QR code for Microsoft Authenticator setup
     * @param userEmail user's email
     * @param secretKey the secret key
     * @return JavaFX Image of QR code
     */
    public Image generateQRCodeImage(String userEmail, String secretKey) {
        try {
            // Generate the QR code URL for Microsoft Authenticator manually
            String qrCodeUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                ISSUER, userEmail, secretKey, ISSUER
            );

            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 200, 200);

            // Convert to PNG byte array and then to JavaFX Image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] imageBytes = outputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

            return new Image(inputStream);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du QR code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verify TOTP code entered by user
     * @param secretKey the user's secret key
     * @param userCode the 6-digit code from Microsoft Authenticator
     * @return true if code is valid
     */
    public boolean verifyCode(String secretKey, int userCode) {
        try {
            return gAuth.authorize(secretKey, userCode);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification du code TOTP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the current TOTP code for testing purposes (admin only)
     * @param secretKey the secret key
     * @return current TOTP code
     */
    public int getCurrentCode(String secretKey) {
        try {
            return gAuth.getTotpPassword(secretKey);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du code TOTP: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Check if TOTP is properly configured for a user
     * @param secretKey the secret key to check
     * @return true if valid configuration
     */
    public boolean isValidSecretKey(String secretKey) {
        try {
            return secretKey != null && !secretKey.trim().isEmpty() &&
                   secretKey.length() >= 16 && // Minimum key length
                   getCurrentCode(secretKey) != -1;
        } catch (Exception e) {
            return false;
        }
    }
}
