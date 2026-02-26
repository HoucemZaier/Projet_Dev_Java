package com.PlaNova.models;

import java.time.LocalDate;

public class Reservation {
    private int idReservation;
    private int idUtilisateur;
    private int idDestination;
    private Integer idHotel; // Nullable
    private Integer idChambre; // Nullable
    private String transportType;
    private Integer idTransport; // Nullable
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double prixTotal;
    private String status;

    public Reservation() {
    }

    public Reservation(int idReservation, int idUtilisateur, int idDestination, Integer idHotel, Integer idChambre,
            String transportType, Integer idTransport, LocalDate dateDebut, LocalDate dateFin,
            double prixTotal, String status) {
        this.idReservation = idReservation;
        this.idUtilisateur = idUtilisateur;
        this.idDestination = idDestination;
        this.idHotel = idHotel;
        this.idChambre = idChambre;
        this.transportType = transportType;
        this.idTransport = idTransport;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prixTotal = prixTotal;
        this.status = status;
    }

    public Reservation(int idUtilisateur, int idDestination, Integer idHotel, Integer idChambre,
            String transportType, Integer idTransport, LocalDate dateDebut, LocalDate dateFin,
            double prixTotal, String status) {
        this.idUtilisateur = idUtilisateur;
        this.idDestination = idDestination;
        this.idHotel = idHotel;
        this.idChambre = idChambre;
        this.transportType = transportType;
        this.idTransport = idTransport;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prixTotal = prixTotal;
        this.status = status;
    }

    // Getters and Setters
    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdDestination() {
        return idDestination;
    }

    public void setIdDestination(int idDestination) {
        this.idDestination = idDestination;
    }

    public Integer getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(Integer idHotel) {
        this.idHotel = idHotel;
    }

    public Integer getIdChambre() {
        return idChambre;
    }

    public void setIdChambre(Integer idChambre) {
        this.idChambre = idChambre;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public Integer getIdTransport() {
        return idTransport;
    }

    public void setIdTransport(Integer idTransport) {
        this.idTransport = idTransport;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", idUtilisateur=" + idUtilisateur +
                ", idDestination=" + idDestination +
                ", prixTotal=" + prixTotal +
                ", status='" + status + '\'' +
                '}';
    }
}
