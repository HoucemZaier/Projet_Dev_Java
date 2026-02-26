package utils;

import Models.User;

/**
 * Singleton class to manage the current user session
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUserType() {
        if (currentUser == null) return null;
        return currentUser.getClass().getSimpleName();
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser instanceof Models.Admin;
    }

    public boolean isModerator() {
        return currentUser != null && currentUser instanceof Models.Moderateur;
    }

    public boolean isGuide() {
        return currentUser != null && currentUser instanceof Models.Guide;
    }

    public boolean isClient() {
        return currentUser != null && currentUser instanceof Models.Client;
    }

    public boolean canAccessDashboard() {
        // Admin, Moderateur, and Guide can access dashboard
        return isAdmin() || isModerator() || isGuide();
    }

    public boolean canAccessUserManagement() {
        // Only Admin can access user management
        return isAdmin();
    }
}

