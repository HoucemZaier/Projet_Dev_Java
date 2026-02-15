package models;

public class Chambre
{
    private int idChambre;
    private String typeChambre;
    private int capacite;
    private double prixChambre;
    private String statutChambre;
    private int idHotel;
    private String nomHotel;
    private String description;  // Nouveau champ
    private String equipement;   // Nouveau champ

    public Chambre()
    {
    }

    public Chambre(int idChambre, String typeChambre, int capacite,
                   double prixChambre, String statutChambre, int idHotel,
                   String description, String equipement)  // Nouveaux paramètres
    {
        this.idChambre = idChambre;
        this.typeChambre = typeChambre;
        this.capacite = capacite;
        this.prixChambre = prixChambre;
        this.statutChambre = statutChambre;
        this.idHotel = idHotel;
        this.description = description;
        this.equipement = equipement;
    }

    // Getters et Setters pour les nouveaux champs
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getEquipement()
    {
        return equipement;
    }

    public void setEquipement(String equipement)
    {
        this.equipement = equipement;
    }

    // Getters et Setters existants
    public int getIdChambre()
    {
        return idChambre;
    }

    public void setIdChambre(int idChambre)
    {
        this.idChambre = idChambre;
    }

    public String getTypeChambre()
    {
        return typeChambre;
    }

    public void setTypeChambre(String typeChambre)
    {
        this.typeChambre = typeChambre;
    }

    public int getCapacite()
    {
        return capacite;
    }

    public void setCapacite(int capacite)
    {
        this.capacite = capacite;
    }

    public double getPrixChambre()
    {
        return prixChambre;
    }

    public void setPrixChambre(double prixChambre)
    {
        this.prixChambre = prixChambre;
    }

    public String getStatutChambre()
    {
        return statutChambre;
    }

    public void setStatutChambre(String statutChambre)
    {
        this.statutChambre = statutChambre;
    }

    public int getIdHotel()
    {
        return idHotel;
    }

    public void setIdHotel(int idHotel)
    {
        this.idHotel = idHotel;
    }

    public String getNomHotel()
    {
        return nomHotel;
    }

    public void setNomHotel(String nomHotel)
    {
        this.nomHotel = nomHotel;
    }

    @Override
    public String toString()
    {
        return "Chambre{" +
                "idChambre=" + idChambre +
                ", typeChambre='" + typeChambre + '\'' +
                ", capacite=" + capacite +
                ", prixChambre=" + prixChambre +
                ", statutChambre='" + statutChambre + '\'' +
                ", idHotel=" + idHotel +
                ", description='" + description + '\'' +
                ", equipement='" + equipement + '\'' +
                '}';
    }
}