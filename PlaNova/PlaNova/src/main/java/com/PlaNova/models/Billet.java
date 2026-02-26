package com.PlaNova.models;

public class Billet {
    private int idBillet;
    private String db;
    private String idv;
    private String numPlace;
    private int idDestination;
    private int idTransportPub;
    private int idTransportPriv;

    public Billet() {}

    public Billet(int idBillet, String db, String idv, String numPlace, int idDestination, int idTransportPub, int idTransportPriv) {
        this.idBillet = idBillet;
        this.db = db;
        this.idv = idv;
        this.numPlace = numPlace;
        this.idDestination = idDestination;
        this.idTransportPub = idTransportPub;
        this.idTransportPriv = idTransportPriv;
    }

    public Billet(String db, String idv, String numPlace, int idDestination, int idTransportPub, int idTransportPriv) {
        this.db = db;
        this.idv = idv;
        this.numPlace = numPlace;
        this.idDestination = idDestination;
        this.idTransportPub = idTransportPub;
        this.idTransportPriv = idTransportPriv;
    }

    public int getIdBillet() { return idBillet; }
    public void setIdBillet(int idBillet) { this.idBillet = idBillet; }

    public String getDb() { return db; }
    public void setDb(String db) { this.db = db; }

    public String getIdv() { return idv; }
    public void setIdv(String idv) { this.idv = idv; }

    public String getNumPlace() { return numPlace; }
    public void setNumPlace(String numPlace) { this.numPlace = numPlace; }

    public int getIdDestination() { return idDestination; }
    public void setIdDestination(int idDestination) { this.idDestination = idDestination; }

    public int getIdTransportPub() { return idTransportPub; }
    public void setIdTransportPub(int idTransportPub) { this.idTransportPub = idTransportPub; }

    public int getIdTransportPriv() { return idTransportPriv; }
    public void setIdTransportPriv(int idTransportPriv) { this.idTransportPriv = idTransportPriv; }

    @Override
    public String toString() {
        return "Billet{" + "id=" + idBillet + ", db=" + db + ", idv=" + idv + ", place=" + numPlace + '}';
    }
}
