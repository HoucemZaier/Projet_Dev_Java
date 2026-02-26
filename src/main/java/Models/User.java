package Models;

public class User {
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String pays;
    private String imageurl;
    private int status; // 0 = active, 1 = blocked
    private boolean twoFactorEnabled; // Two-factor authentication enabled
    private String faceModelData; // Base64 encoded face recognition model
    private String totpSecretKey; // Secret key for TOTP (Microsoft Authenticator)

    // Constructeur vide
    public User() {
    }

    // Constructeur complet avec status et 2FA
    public User(int idUtilisateur, String nom, String prenom, String email,
                String motDePasse, String pays, String imageurl, int status,
                boolean twoFactorEnabled, String faceModelData, String totpSecretKey) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.pays = pays;
        this.imageurl = imageurl;
        this.status = status;
        this.twoFactorEnabled = twoFactorEnabled;
        this.faceModelData = faceModelData;
        this.totpSecretKey = totpSecretKey;
    }

    // Constructeur complet avec status et 2FA (sans TOTP pour compatibilité)
    public User(int idUtilisateur, String nom, String prenom, String email,
                String motDePasse, String pays, String imageurl, int status,
                boolean twoFactorEnabled, String faceModelData) {
        this(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl, status, twoFactorEnabled, faceModelData, null);
    }

    // Constructeur complet avec status (pour compatibilité)
    public User(int idUtilisateur, String nom, String prenom, String email,
                String motDePasse, String pays, String imageurl, int status) {
        this(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl, status, false, null);
    }

    // Constructeur complet sans status (pour compatibilité)
    public User(int idUtilisateur, String nom, String prenom, String email,
                String motDePasse, String pays, String imageurl) {
        this(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl, 0); // default active
    }

    // Constructeur sans ID (pour insertion)
    public User(String nom, String prenom, String email, String motDePasse,
                String pays, String imageurl) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.pays = pays;
        this.imageurl = imageurl;
        this.status = 0; // default active
        this.twoFactorEnabled = false; // default disabled
        this.faceModelData = null;
        this.totpSecretKey = null;
    }

    // Getters et Setters
    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isBlocked() {
        return status == 1;
    }

    public boolean isActive() {
        return status == 0;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getFaceModelData() {
        return faceModelData;
    }

    public void setFaceModelData(String faceModelData) {
        this.faceModelData = faceModelData;
    }

    public String getTotpSecretKey() {
        return totpSecretKey;
    }

    public void setTotpSecretKey(String totpSecretKey) {
        this.totpSecretKey = totpSecretKey;
    }

    /**
     * Check if user has TOTP (Microsoft Authenticator) enabled
     */
    public boolean isTotpEnabled() {
        return totpSecretKey != null && !totpSecretKey.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "User{" +
                "idUtilisateur=" + idUtilisateur +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", pays='" + pays + '\'' +
                '}';
    }
}