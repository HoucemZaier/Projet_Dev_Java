package Models;

public class Moderateur extends User {
    private int id_moderateur;

    public Moderateur() {
        super();
    }

    public Moderateur(int idUtilisateur, String nom, String prenom, String email,
                      String motDePasse, String pays, String imageurl) {
        super(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl);
        this.id_moderateur = idUtilisateur;
    }

    public Moderateur(String nom, String prenom, String email, String motDePasse,
                      String pays, String imageurl) {
        super(nom, prenom, email, motDePasse, pays, imageurl);
    }

    public int getId_moderateur() {
        return id_moderateur;
    }

    public void setId_moderateur(int id_moderateur) {
        this.id_moderateur = id_moderateur;
    }

    @Override
    public String toString() {
        return "Moderateur{" +
                "id_moderateur=" + id_moderateur +
                ", " + super.toString() +
                '}';
    }
}