package Models;

import java.sql.Date;

public class Excursion {

    private int idExcursion;
    private String titre;
    private int idDestination;
    private Date dateDepart;
    private Date dateRetour;
    private double prix;
    private int nbPlaces;
    private String statut;
    private String nomDestination;

    // ✅ NOUVEAU : Géolocalisation OpenStreetMap
    private Double latitude;
    private Double longitude;

    public Excursion() {}

    public Excursion(int idExcursion, String titre, int idDestination,
                     Date dateDepart, Date dateRetour,
                     double prix, int nbPlaces, String statut) {
        this.idExcursion = idExcursion;
        this.titre = titre;
        this.idDestination = idDestination;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.prix = prix;
        this.nbPlaces = nbPlaces;
        this.statut = statut;
    }

    // ========= Getters & Setters existants =========

    public int getIdExcursion() { return idExcursion; }
    public void setIdExcursion(int idExcursion) { this.idExcursion = idExcursion; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public int getIdDestination() { return idDestination; }
    public void setIdDestination(int idDestination) { this.idDestination = idDestination; }

    public String getNomDestination() { return nomDestination; }
    public void setNomDestination(String nomDestination) { this.nomDestination = nomDestination; }

    public Date getDateDepart() { return dateDepart; }
    public void setDateDepart(Date dateDepart) { this.dateDepart = dateDepart; }

    public Date getDateRetour() { return dateRetour; }
    public void setDateRetour(Date dateRetour) { this.dateRetour = dateRetour; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    // ========= NOUVEAU : Géolocalisation =========

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    /** Vérifie si la position GPS est définie */
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    @Override
    public String toString() {
        return "Excursion{" +
                "idExcursion=" + idExcursion +
                ", titre='" + titre + '\'' +
                ", idDestination=" + idDestination +
                ", dateDepart=" + dateDepart +
                ", dateRetour=" + dateRetour +
                ", prix=" + prix +
                ", nbPlaces=" + nbPlaces +
                ", statut='" + statut + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
