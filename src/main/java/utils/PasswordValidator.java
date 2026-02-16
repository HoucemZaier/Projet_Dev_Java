package utils;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Utility class for password validation with visual feedback
 */
public class PasswordValidator {

    public static class ValidationResult {
        private boolean hasMinLength;
        private boolean hasUppercase;
        private boolean hasNumber;
        private boolean hasSpecialChar;

        public ValidationResult(boolean hasMinLength, boolean hasUppercase, boolean hasNumber, boolean hasSpecialChar) {
            this.hasMinLength = hasMinLength;
            this.hasUppercase = hasUppercase;
            this.hasNumber = hasNumber;
            this.hasSpecialChar = hasSpecialChar;
        }

        public boolean isValid() {
            return hasMinLength && hasUppercase && hasNumber && hasSpecialChar;
        }

        // Getters
        public boolean hasMinLength() { return hasMinLength; }
        public boolean hasUppercase() { return hasUppercase; }
        public boolean hasNumber() { return hasNumber; }
        public boolean hasSpecialChar() { return hasSpecialChar; }
    }

    /**
     * Validates password and returns detailed result
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null) password = "";

        boolean hasMinLength = password.length() >= 8;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return new ValidationResult(hasMinLength, hasUppercase, hasNumber, hasSpecialChar);
    }

    /**
     * Updates visual indicators based on password validation
     */
    public static void updatePasswordRequirements(String password, Label lengthReq, Label upperReq,
                                                 Label numberReq, Label specialReq) {
        ValidationResult result = validatePassword(password);

        // Update length requirement
        if (result.hasMinLength()) {
            lengthReq.setText("✓ 8+ caractères");
            lengthReq.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            lengthReq.setText("• 8+ caractères");
            lengthReq.setStyle("-fx-text-fill: #6c757d;");
        }

        // Update uppercase requirement
        if (result.hasUppercase()) {
            upperReq.setText("✓ Majuscule");
            upperReq.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            upperReq.setText("• Majuscule");
            upperReq.setStyle("-fx-text-fill: #6c757d;");
        }

        // Update number requirement
        if (result.hasNumber()) {
            numberReq.setText("✓ Chiffre");
            numberReq.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            numberReq.setText("• Chiffre");
            numberReq.setStyle("-fx-text-fill: #6c757d;");
        }

        // Update special character requirement
        if (result.hasSpecialChar()) {
            specialReq.setText("✓ Caractère spécial");
            specialReq.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            specialReq.setText("• Caractère spécial");
            specialReq.setStyle("-fx-text-fill: #6c757d;");
        }
    }

    /**
     * Gets password strength as a percentage
     */
    public static int getPasswordStrength(String password) {
        ValidationResult result = validatePassword(password);
        int strength = 0;

        if (result.hasMinLength()) strength += 25;
        if (result.hasUppercase()) strength += 25;
        if (result.hasNumber()) strength += 25;
        if (result.hasSpecialChar()) strength += 25;

        return strength;
    }

    /**
     * Gets password strength color
     */
    public static String getPasswordStrengthColor(int strength) {
        if (strength < 25) return "#dc3545"; // Red
        else if (strength < 50) return "#fd7e14"; // Orange
        else if (strength < 75) return "#ffc107"; // Yellow
        else if (strength < 100) return "#17a2b8"; // Blue
        else return "#28a745"; // Green
    }

    /**
     * Gets password strength text
     */
    public static String getPasswordStrengthText(int strength) {
        if (strength < 25) return "Très faible";
        else if (strength < 50) return "Faible";
        else if (strength < 75) return "Moyen";
        else if (strength < 100) return "Fort";
        else return "Très fort";
    }
}
