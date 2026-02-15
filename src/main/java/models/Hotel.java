package models;

public class Hotel
{
    private int idHotel;
    private String nomHotel;
    private String adresse;
    private String ville;
    private int nombreEtoile;
    private String description;
    private String image;


    public Hotel()
    {
    }

    public Hotel(int idHotel, String nomHotel, String adresse, String ville,
                 int nombreEtoile, String description, String image)
    {
        this.idHotel = idHotel;
        this.nomHotel = nomHotel;
        this.adresse = adresse;
        this.ville = ville;
        this.nombreEtoile = nombreEtoile;
        this.description = description;
        this.image = image;

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

    public String getAdresse()
    {
        return adresse;
    }

    public void setAdresse(String adresse)
    {
        this.adresse = adresse;
    }

    public String getVille()
    {
        return ville;
    }

    public void setVille(String ville)
    {
        this.ville = ville;
    }

    public int getNombreEtoile()
    {
        return nombreEtoile;
    }

    public void setNombreEtoile(int nombreEtoile)
    {
        this.nombreEtoile = nombreEtoile;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }




    @Override
    public String toString()
    {
        return "Hotel{" +
                "idHotel=" + idHotel +
                ", nomHotel='" + nomHotel + '\'' +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", nombreEtoile=" + nombreEtoile +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

}
