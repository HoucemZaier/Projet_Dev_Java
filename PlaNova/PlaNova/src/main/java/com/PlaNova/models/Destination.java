package com.PlaNova.models;
import com.PlaNova.utils.MyDatabase;

import java.time.LocalDate;
import java.util.UUID;

public class Destination {
    private int idDestination;
    private String nomDestination;
    private String pays;
    private LocalDate dateArrivee;
    private LocalDate dateDep;
    private String image;

    public Destination() {}

    public Destination(int idDestination, String nomDestination, String pays, LocalDate dateArrivee, LocalDate dateDep, String image) {
        this.idDestination = idDestination;
        this.nomDestination = nomDestination;
        this.pays = pays;
        this.dateArrivee = dateArrivee;
        this.dateDep = dateDep;
        this.image = image;
    }


    public Destination(String nomDestination, String pays, LocalDate dateArrivee, LocalDate dateDep, String image) {
        this.nomDestination = nomDestination;
        this.pays = pays;
        this.dateArrivee = dateArrivee;
        this.dateDep = dateDep;
        this.image = image;
    }

    public int getIdDestination() { return idDestination; }
    public void setIdDestination(int idDestination) { this.idDestination = idDestination; }

    public String getNomDestination() { return nomDestination; }
    public void setNomDestination(String nomDestination) { this.nomDestination = nomDestination; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public LocalDate getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDate dateArrivee) { this.dateArrivee = dateArrivee; }

    public LocalDate getDateDep() { return dateDep; }
    public void setDateDep(LocalDate dateDep) { this.dateDep = dateDep; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return "Destination{" + "nom=" + nomDestination + ", pays=" + pays + '}';
    }
}