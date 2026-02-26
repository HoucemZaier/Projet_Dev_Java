package Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Simple file upload service that simulates cloud storage
 * Files are stored locally and a "cloud link" is generated
 */
public class FileUploadService {

    private static final String UPLOAD_DIR = "uploads/cv/";
    private static final String BASE_URL = "https://plannova-storage.com/cv/";

    public FileUploadService() {
        // Create upload directory if it doesn't exist
        createUploadDirectory();
    }

    /**
     * Upload CV file and return cloud storage link
     */
    public String uploadCV(File cvFile, String clientName) throws IOException {
        if (cvFile == null || !cvFile.exists()) {
            throw new IOException("Fichier CV non valide");
        }

        // Validate file type
        String fileName = cvFile.getName().toLowerCase();
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".doc") && !fileName.endsWith(".docx")) {
            throw new IOException("Type de fichier non supporté. Veuillez utiliser PDF, DOC ou DOCX.");
        }

        // Validate file size (max 5MB)
        long fileSizeInMB = cvFile.length() / (1024 * 1024);
        if (fileSizeInMB > 5) {
            throw new IOException("Le fichier CV dépasse la taille maximale de 5MB.");
        }

        // Generate unique filename
        String fileExtension = getFileExtension(cvFile.getName());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String newFileName = String.format("CV_%s_%s_%s.%s",
            sanitizeFileName(clientName), timestamp, uniqueId, fileExtension);

        // Copy file to upload directory
        Path targetPath = Paths.get(UPLOAD_DIR + newFileName);
        Files.copy(cvFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Generate "cloud" link
        String cloudLink = BASE_URL + newFileName;

        System.out.println("✅ CV uploadé avec succès: " + newFileName);
        System.out.println("🔗 Lien cloud: " + cloudLink);

        return cloudLink;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndexOf + 1);
    }

    /**
     * Sanitize filename to remove special characters
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Create upload directory if it doesn't exist
     */
    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("📁 Dossier d'upload créé: " + UPLOAD_DIR);
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la création du dossier d'upload: " + e.getMessage());
        }
    }

    /**
     * Check if file exists in cloud storage
     */
    public boolean fileExists(String cloudLink) {
        if (cloudLink == null || !cloudLink.startsWith(BASE_URL)) {
            return false;
        }

        String fileName = cloudLink.substring(BASE_URL.length());
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        return Files.exists(filePath);
    }

    /**
     * Get local file path from cloud link (for admin viewing)
     */
    public String getLocalFilePath(String cloudLink) {
        if (cloudLink == null || !cloudLink.startsWith(BASE_URL)) {
            return null;
        }

        String fileName = cloudLink.substring(BASE_URL.length());
        return UPLOAD_DIR + fileName;
    }
}
