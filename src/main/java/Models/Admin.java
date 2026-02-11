package Models;

public class Admin extends User {
    private int id_admin;
    private String matricule;

    public Admin() {
        super();
    }

    public Admin(int idUtilisateur, String nom, String prenom, String email,
                 String motDePasse, String pays, String imageurl, String matricule) {
        super(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl);
        this.id_admin = idUtilisateur;
        this.matricule = matricule;
    }

    public Admin(String nom, String prenom, String email, String motDePasse,
                 String pays, String imageurl, String matricule) {
        super(nom, prenom, email, motDePasse, pays, imageurl);
        this.matricule = matricule;
    }

    public int getId_admin() {
        return id_admin;
    }

    public void setId_admin(int id_admin) {
        this.id_admin = id_admin;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id_admin=" + id_admin +
                ", matricule='" + matricule + '\'' +
                ", " + super.toString() +
                '}';
    }
}