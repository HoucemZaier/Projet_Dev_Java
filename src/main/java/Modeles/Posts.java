package Modeles;

public class Posts {
    private int idPost;
    private String nomPost;
    private String description;
    private double prix;
    private String typePost;
    private int id_utilisateur;


    public Posts() {
    }

    public Posts(int idPost, String nomPost, String description, double prix, String typePost, int id_utilisateur) {
        this.idPost = idPost;
        this.nomPost = nomPost;
        this.description = description;
        this.prix = prix;
        this.typePost = typePost;
        this.id_utilisateur = id_utilisateur;
    }


    public int getIdPost() {
        return idPost;
    }

    public void setIdPost(int idPost) {
        this.idPost = idPost;
    }

    public String getNomPost() {
        return nomPost;
    }

    public void setNomPost(String nomPost) {
        this.nomPost = nomPost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getTypePost() {
        return typePost;
    }

    public void setTypePost(String typePost) {
        this.typePost = typePost;
    }

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    @Override
    public String toString() {
        return "Post{" +
                "idPost=" + idPost +
                ", nomPost='" + nomPost + '\'' +
                ", prix=" + prix +
                ", typePost='" + typePost + '\'' +
                '}';
    }
}