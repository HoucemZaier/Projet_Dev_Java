package Models;

public class Guide extends User {
    private int id_guide;

    public Guide() {
        super();
    }

    public Guide(int idUtilisateur, String nom, String prenom, String email,
                 String motDePasse, String pays, String imageurl) {
        super(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl);
        this.id_guide = idUtilisateur; // id_guide = id_utilisateur
    }

    public Guide(String nom, String prenom, String email, String motDePasse,
                 String pays, String imageurl) {
        super(nom, prenom, email, motDePasse, pays, imageurl);
    }

    // Getter public (vous aviez oublié public)
    public int getId_guide() {
        return id_guide;
    }

    public void setId_guide(int id_guide) {
        this.id_guide = id_guide;
    }

    @Override
    public String toString() {
        return "Guide{" +
                "id_guide=" + id_guide +
                ", " + super.toString() +
                '}';
    }
}