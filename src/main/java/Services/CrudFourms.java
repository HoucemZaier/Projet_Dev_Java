package Services;

import Modeles.Fourms;
import java.util.List;

public interface CrudFourms {
    void ajouter(Fourms forum);

    void supprimer(int id_forum);

    void modifier(Fourms forum);

    List<Fourms> afficherTout();
}
