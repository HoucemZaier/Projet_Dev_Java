package com.PlaNova.models;

public class TransportPublique {
    private int id_transport_pub;
    private String type;
    private double tarif;
    private String horaire;
    private String image_path;
    private int id_destination;

    public TransportPublique() {
    }

    public TransportPublique(int id_transport_pub, String type, double tarif, String horaire, String image_path,
            int id_destination) {
        this.id_transport_pub = id_transport_pub;
        this.type = type;
        this.tarif = tarif;
        this.horaire = horaire;
        this.image_path = image_path;
        this.id_destination = id_destination;
    }

    public int getId_transport_pub() {
        return id_transport_pub;
    }

    public void setId_transport_pub(int id_transport_pub) {
        this.id_transport_pub = id_transport_pub;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getTarif() {
        return tarif;
    }

    public void setTarif(double tarif) {
        this.tarif = tarif;
    }

    public String getHoraire() {
        return horaire;
    }

    public void setHoraire(String horaire) {
        this.horaire = horaire;
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
}
