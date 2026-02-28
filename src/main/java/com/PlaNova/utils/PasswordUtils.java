package com.PlaNova.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and validation using BCrypt
 */
public class PasswordUtils {
    
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * Hash a password using BCrypt
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public static String hashPassword(String plainPassword) {
        // Use BCrypt via Spring Security or similar
        // For standalone JavaFX, we'll use SHA-256 with salt as a fallback
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            // Hash the password with salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(plainPassword.getBytes());
            
            // Combine salt and hashed password
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            // Return Base64 encoded string with salt prefix
            return "$SHA256$" + Base64.getEncoder().encodeToString(combined);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a hashed password
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the hashed password to verify against
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            if (hashedPassword.startsWith("$SHA256$")) {
                // Decode the stored password
                String encoded = hashedPassword.substring(8);
                byte[] combined = Base64.getDecoder().decode(encoded);
                
                // Extract salt (first 16 bytes)
                byte[] salt = new byte[16];
                System.arraycopy(combined, 0, salt, 0, 16);
                
                // Extract hashed password (remaining bytes)
                byte[] storedHash = new byte[combined.length - 16];
                System.arraycopy(combined, 16, storedHash, 0, combined.length - 16);
                
                // Hash the input password with the same salt
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(salt);
                byte[] computedHash = md.digest(plainPassword.getBytes());
                
                // Compare the hashes
                return MessageDigest.isEqual(storedHash, computedHash);
            } else {
                // Legacy plain text comparison (for backward compatibility)
                return plainPassword.equals(hashedPassword);
            }
            
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate password strength
     * @param password the password to validate
     * @return true if password meets requirements (8+ chars, uppercase, number, special char)
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
    
    /**
     * Check if password needs to be migrated (not hashed)
     * @param password the password to check
     * @return true if password is plain text and needs migration
     */
    public static boolean needsMigration(String password) {
        return password != null && !password.startsWith("$SHA256$") && !password.startsWith("$2a$");
    }
}