package com.PlaNova.models;

public class User {
    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String pays;
    private String imageurl;
    private int status; // 0 = active, 1 = blocked
    private boolean twoFactorEnabled;
    private String faceModelData;
    private String totpSecretKey;
    private String typeUtilisateur;

    public User() {
    }

    // Constructor missing typeUtilisateur (used by subclasses likely)
    public User(int idUtilisateur, String nom, String prenom, String email,
            String motDePasse, String pays, String imageurl) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.pays = pays;
        this.imageurl = imageurl;
    }

    public User(String nom, String prenom, String email, String motDePasse,
            String pays, String imageurl) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.pays = pays;
        this.imageurl = imageurl;
    }

    public User(int idUtilisateur, String nom, String prenom, String email,
            String motDePasse, String pays, String imageurl, String typeUtilisateur) {
        this(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl);
        this.typeUtilisateur = typeUtilisateur;
    }

    // Full constructor
    public User(int idUtilisateur, String nom, String prenom, String email,
            String motDePasse, String pays, String imageurl, int status,
            boolean twoFactorEnabled, String faceModelData, String totpSecretKey, String typeUtilisateur) {
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
        this.typeUtilisateur = typeUtilisateur;
    }

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

    public void setBlocked(boolean blocked) {
        this.status = blocked ? 1 : 0;
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

    public String getTypeUtilisateur() {
        return typeUtilisateur;
    }

    public void setTypeUtilisateur(String typeUtilisateur) {
        this.typeUtilisateur = typeUtilisateur;
    }

    public boolean isTotpEnabled() {
        return totpSecretKey != null && !totpSecretKey.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + idUtilisateur +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", type='" + typeUtilisateur + '\'' +
                '}';
    }
}