package Models;

import java.time.LocalDateTime;

public class Notification {
    private int notificationId;
    private int targetUserId; // Admin who should see this
    private String type; // GUIDE_APPLICATION, GENERAL, etc.
    private String title;
    private String message;
    private String relatedData; // JSON or link data
    private boolean isRead;
    private LocalDateTime createdAt;

    // Constructors
    public Notification() {}

    public Notification(int targetUserId, String type, String title, String message, String relatedData) {
        this.targetUserId = targetUserId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedData = relatedData;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(int targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedData() {
        return relatedData;
    }

    public void setRelatedData(String relatedData) {
        this.relatedData = relatedData;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", targetUserId=" + targetUserId +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
