package com.PlaNova.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Activite {
    private int idActivite;
    private String nom;
    private String description;
    private LocalDate dateActivite;
    private LocalTime heureActivite;
    private String lieu;
    private double prix;
    private Integer idExcursion;
    private Integer idDestination;

    public Activite() {
    }

    public Activite(int idActivite, String nom, String description, LocalDate dateActivite, LocalTime heureActivite,
            String lieu, double prix, Integer idExcursion, Integer idDestination) {
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

    public LocalDate getDateActivite() {
        return dateActivite;
    }

    public void setDateActivite(LocalDate dateActivite) {
        this.dateActivite = dateActivite;
    }

    public LocalTime getHeureActivite() {
        return heureActivite;
    }

    public void setHeureActivite(LocalTime heureActivite) {
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

    public Integer getIdExcursion() {
        return idExcursion;
    }

    public void setIdExcursion(Integer idExcursion) {
        this.idExcursion = idExcursion;
    }

    public Integer getIdDestination() {
        return idDestination;
    }

    public void setIdDestination(Integer idDestination) {
        this.idDestination = idDestination;
    }

    @Override
    public String toString() {
        return nom + " (€" + prix + ")";
    }
}
