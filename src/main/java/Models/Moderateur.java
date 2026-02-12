package Models;

public class Moderateur extends User {
    private int id_moderateur;
    private String matricule;

    public Moderateur() {
        super();
    }

    public Moderateur(String nom, String prenom, String email, String motDePasse,
                      String pays, String imageurl, String matricule) {
        super(nom, prenom, email, motDePasse, pays, imageurl);
        this.matricule = matricule;
    }

    public Moderateur(int idUtilisateur, String nom, String prenom, String email,
                      String motDePasse, String pays, String imageurl, String matricule) {
        super(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl);
        this.id_moderateur = idUtilisateur;
        this.matricule = matricule;
    }

    public int getId_moderateur() {
        return id_moderateur;
    }

    public void setId_moderateur(int id_moderateur) {
        this.id_moderateur = id_moderateur;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    @Override
    public String toString() {
        return "Moderateur{" +
                "id_moderateur=" + id_moderateur +
                ", matricule='" + matricule + '\'' +
                ", " + super.toString() +
                '}';
    }
}