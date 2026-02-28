package com.PlaNova.models;

public class TransportPrive {
    private int id_transport_priv;
    private String marque;
    private String etat;
    private String complement;
    private double prix_lac;
    private String image_path;
    private int id_destination;

    public TransportPrive() {
    }

    public TransportPrive(int id_transport_priv, String marque, String etat, String complement, double prix_lac,
            String image_path, int id_destination) {
        this.id_transport_priv = id_transport_priv;
        this.marque = marque;
        this.etat = etat;
        this.complement = complement;
        this.prix_lac = prix_lac;
        this.image_path = image_path;
        this.id_destination = id_destination;
    }

    public int getId_transport_priv() {
        return id_transport_priv;
    }

    public void setId_transport_priv(int id_transport_priv) {
        this.id_transport_priv = id_transport_priv;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public double getPrix_lac() {
        return prix_lac;
    }

    public void setPrix_lac(double prix_lac) {
        this.prix_lac = prix_lac;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public int getId_destination() {
        return id_destination;
    }

    public void setId_destination(int id_destination) {
        this.id_destination = id_destination;
    }

    // Backwards Compatibility Wrappers
    public double getPrix_loc() {
        return this.prix_lac;
    }

    public void setPrix_loc(double prix_loc) {
        this.prix_lac = prix_loc;
    }
}
