package Services;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void ajouter(T entity) throws SQLException;
    void supprimer(int id) throws SQLException;
    void modifier(T entity) throws SQLException;
    List<T> recuperer() throws SQLException;
    T recupererParId(int id) throws SQLException;
}