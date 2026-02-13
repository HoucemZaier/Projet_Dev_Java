package Modeles;

public class Fourms {
    private int id_forum;        // correspond à id_forum, clé primaire
    private String nom;          // correspond à nom
    private int nbparticipant;   // correspond à nbparticipant
    private String commentaire;  // correspond à commentaire
    private int idposte;         // correspond à idposte, clé étrangère possible

    // Constructeur vide
    public Fourms() {
    }

    // Constructeur avec tous les champs
    public Fourms(int id_forum, String nom, int nbparticipant, String commentaire, int idposte) {
        this.id_forum = id_forum;
        this.nom = nom;
        this.nbparticipant = nbparticipant;
        this.commentaire = commentaire;
        this.idposte = idposte;
    }

    // Getters et Setters
    public int getId_forum() {
        return id_forum;
    }

    public void setId_forum(int id_forum) {
        this.id_forum = id_forum;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getNbparticipant() {
        return nbparticipant;
    }

    public void setNbparticipant(int nbparticipant) {
        this.nbparticipant = nbparticipant;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getIdposte() {
        return idposte;
    }

    public void setIdposte(int idposte) {
        this.idposte = idposte;
    }

    @Override
    public String toString() {
        return "Fourms{" +
                "id_forum=" + id_forum +
                ", nom='" + nom + '\'' +
                ", nbparticipant=" + nbparticipant +
                ", commentaire='" + commentaire + '\'' +
                ", idposte=" + idposte +
                '}';
    }
}
