package com.PlaNova.utils;

/**
 * Utility class for CIN validation
 * CIN must be numeric and exactly 8 characters long
 */
public class CinValidator {

    /**
     * Validates CIN format
     * @param cin the CIN to validate
     * @return true if valid (numeric and 8 characters), false otherwise
     */
    public static boolean isValidCin(String cin) {
        if (cin == null || cin.trim().isEmpty()) {
            return false;
        }

        cin = cin.trim();

        // Check if exactly 8 characters
        if (cin.length() != 8) {
            return false;
        }

        // Check if all characters are numeric
        return cin.matches("\\d{8}");
    }

    /**
     * Gets validation error message for CIN
     * @param cin the CIN to validate
     * @return error message if invalid, null if valid
     */
    public static String getValidationMessage(String cin) {
        if (cin == null || cin.trim().isEmpty()) {
            return "Le CIN est requis";
        }

        cin = cin.trim();

        if (cin.length() != 8) {
            return "Le CIN doit contenir exactement 8 caractères";
        }

        if (!cin.matches("\\d{8}")) {
            return "Le CIN doit contenir uniquement des chiffres";
        }

        return null; // Valid
    }
}
