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

    // Constructeur vide
    public User() {
    }

    // Constructeur complet avec status
    public User(int idUtilisateur, String nom, String prenom, String email,
                String motDePasse, String pays, String imageurl, int status) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.pays = pays;
        this.imageurl = imageurl;
        this.status = status;
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