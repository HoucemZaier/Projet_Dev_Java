package Models;

import java.time.LocalDateTime;

public class Notification {

    private String message;
    private LocalDateTime date;
    private boolean read;

    public Notification(String message) {
        this.message = message;
        this.date = LocalDateTime.now();
        this.read = false;
    }

    public String getMessage() { return message; }
    public LocalDateTime getDate() { return date; }
    public boolean isRead() { return read; }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return (read ? "" : "🔴 ") + message +
                "\n🕒 " + date.toLocalTime();
    }
}