package Models;

public class Client extends User {
    private int id_client ;
    private String cin;

    public Client(String testNom, String testPrenom, String mail, String password, String tunisie, String s) {
        super();
    }

    public Client(int idUtilisateur, String nom, String prenom, String email,
                  String motDePasse, String pays, String imageurl, String cin) {
        super(idUtilisateur, nom, prenom, email, motDePasse, pays, imageurl);
        this.cin = cin;
    }

    public Client(String nom, String prenom, String email, String motDePasse,
                  String pays, String imageurl, String cin) {
        super(nom, prenom, email, motDePasse, pays, imageurl);
        this.cin = cin;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }
    public int getId_client() {
        return id_client;
    }
    public void setId_client(int id_client) {
        this.id_client = id_client;
    }

    @Override
    public String toString() {
        return "Client{" +
                "cin='" + cin + '\'' +
                ", " + super.toString() +
                '}';
    }
}