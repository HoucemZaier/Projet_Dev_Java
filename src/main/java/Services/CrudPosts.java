package Services;

import Modeles.Posts;
import java.util.List;

public interface CrudPosts
{

    void ajouter(Posts post);

    List<Posts> afficherTout();

    void modifier(Posts post);

    void supprimer(int id);
}