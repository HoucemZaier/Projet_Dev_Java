package Services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Models.Notification;

public class NotificationService {

    private static NotificationService instance;

    private ObservableList<Notification> notifications =
            FXCollections.observableArrayList();

    private NotificationService() {}

    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public ObservableList<Notification> getNotifications() {
        return notifications;
    }

    public void addNotification(String message) {
        notifications.add(0, new Notification(message));
    }

    public void markAllAsRead() {
        for (Notification n : notifications) {
            n.setRead(true);
        }
    }

    public void removeNotification(Notification notification) {
        notifications.remove(notification);
    }
    public int getUnreadCount() {
        int count = 0;
        for (Notification n : notifications) {
            if (!n.isRead()) count++;
        }
        return count;
    }
}