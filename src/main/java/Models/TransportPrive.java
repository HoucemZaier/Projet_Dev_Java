package Models;

/**
 * Classe TransportPrive représente un moyen de transport privé
 * avec ses attributs : identifiant, marque, état, prix de location et image
 */
public class TransportPrive {

    // ==================== ATTRIBUTS ====================
    private int id_transport_priv;
    private String marque;
    private String etat;
    private double prix_loc;
    private String image_path;
    private String type_carburant;

    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur par défaut
     */
    public TransportPrive() {
    }

    /**
     * Constructeur paramétrisé
     * @param id_transport_priv Identifiant du transport privé
     * @param marque Marque du véhicule
     * @param etat État du véhicule
     * @param prix_loc Prix de location
     * @param image_path Chemin vers l'image du véhicule
     * @param type_carburant Type de carburant du véhicule
     */
    public TransportPrive(int id_transport_priv, String marque, String etat, double prix_loc, String image_path, String type_carburant) {
        this.id_transport_priv = id_transport_priv;
        this.marque = marque;
        this.etat = etat;
        this.prix_loc = prix_loc;
        this.image_path = image_path;
        this.type_carburant = type_carburant;
    }

    // ==================== GETTERS ET SETTERS ====================

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

    public double getPrix_loc() {
        return prix_loc;
    }

    public void setPrix_loc(double prix_loc) {
        this.prix_loc = prix_loc;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getType_carburant() {
        return type_carburant;
    }

    public void setType_carburant(String type_carburant) {
        this.type_carburant = type_carburant;
    }

    // ==================== MÉTHODES ====================

    @Override
    public String toString() {
        return "TransportPrive{" +
                "id_transport_priv=" + id_transport_priv +
                ", marque='" + marque + '\'' +
                ", etat='" + etat + '\'' +
                ", prix_loc=" + prix_loc +
                ", image_path='" + image_path + '\'' +
                ", type_carburant='" + type_carburant + '\'' +
                '}' + '\n';
    }

}
