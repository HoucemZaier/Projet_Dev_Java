package Models;

import java.sql.Date;
import java.util.List;

public class Excursion {

    private int idExcursion;
    private String titre;
    private String destination;
    private Date dateDepart;
    private Date dateRetour;
    private double prix;
    private int nbPlaces;
    private String statut;

    public Excursion() {
    }

    public Excursion(int idExcursion, String titre, String destination,
                     Date dateDepart, Date dateRetour,
                     double prix, int nbPlaces, String statut) {
        this.idExcursion = idExcursion;
        this.titre = titre;
        this.destination = destination;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.prix = prix;
        this.nbPlaces = nbPlaces;
        this.statut = statut;
    }

    // Getters & Setters

    public int getIdExcursion() {
        return idExcursion;
    }

    public void setIdExcursion(int idExcursion) {
        this.idExcursion = idExcursion;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(Date dateDepart) {
        this.dateDepart = dateDepart;
    }

    public Date getDateRetour() {
        return dateRetour;
    }

    public void setDateRetour(Date dateRetour) {
        this.dateRetour = dateRetour;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Excursion{" +
                "idExcursion=" + idExcursion +
                ", titre='" + titre + '\'' +
                ", destination='" + destination + '\'' +
                ", dateDepart=" + dateDepart +
                ", dateRetour=" + dateRetour +
                ", prix=" + prix +
                ", nbPlaces=" + nbPlaces +
                ", statut='" + statut + '\'' +
                '}';
    }
}
