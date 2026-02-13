package Models;

import java.sql.Date;
import java.sql.Time;

public class Activite {

    private int idActivite;
    private String nom;
    private String description;
    private Date dateActivite;
    private Time heureActivite;
    private String lieu;
    private double prix;
    private int idExcursion;    // clé étrangère vers Excursion
    private int idDestination;  // clé étrangère vers Destination (nouveau)

    // Constructeur vide
    public Activite() {
    }

    // Constructeur complet
    public Activite(int idActivite, String nom, String description,
                    Date dateActivite, Time heureActivite,
                    String lieu, double prix, int idExcursion,
                    int idDestination) {
        this.idActivite = idActivite;
        this.nom = nom;
        this.description = description;
        this.dateActivite = dateActivite;
        this.heureActivite = heureActivite;
        this.lieu = lieu;
        this.prix = prix;
        this.idExcursion = idExcursion;
        this.idDestination = idDestination;
    }

    // Getters & Setters
    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateActivite() {
        return dateActivite;
    }

    public void setDateActivite(Date dateActivite) {
        this.dateActivite = dateActivite;
    }

    public Time getHeureActivite() {
        return heureActivite;
    }

    public void setHeureActivite(Time heureActivite) {
        this.heureActivite = heureActivite;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getIdExcursion() {
        return idExcursion;
    }

    public void setIdExcursion(int idExcursion) {
        this.idExcursion = idExcursion;
    }

    public int getIdDestination() {
        return idDestination;
    }

    public void setIdDestination(int idDestination) {
        this.idDestination = idDestination;
    }

    @Override
    public String toString() {
        return "Activite{" +
                "idActivite=" + idActivite +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", dateActivite=" + dateActivite +
                ", heureActivite=" + heureActivite +
                ", lieu='" + lieu + '\'' +
                ", prix=" + prix +
                ", idExcursion=" + idExcursion +
                ", idDestination=" + idDestination +
                '}';
    }
}