package com.PlaNova.services;

import com.PlaNova.models.Destination;
import java.sql.SQLDataException;
import java.util.List;

public interface IService<T> {
    default void add(T t) throws SQLDataException {
        try {
            ajouter(t);
        } catch (Exception e) {
            throw new SQLDataException(e);
        }
    }

    default void delete(T t) throws SQLDataException {
        try {
            supprimer(t);
        } catch (Exception e) {
            throw new SQLDataException(e);
        }
    }

    default void modify(T t) throws SQLDataException {
        try {
            modifier(t);
        } catch (Exception e) {
            throw new SQLDataException(e);
        }
    }

    default List<T> show() throws SQLDataException {
        try {
            return recuperer();
        } catch (Exception e) {
            throw new SQLDataException(e);
        }
    }

    // Legacy methods from GIT_Transport IService
    default void ajouter(T entity) throws java.sql.SQLException {
        add(entity);
    }

    default void supprimer(T entity) throws java.sql.SQLException {
        delete(entity);
    }

    default void modifier(T entity) throws java.sql.SQLException {
        modify(entity);
    }

    default List<T> recuperer() throws java.sql.SQLException {
        return show();
    }

    default T recupererParId(int id) throws java.sql.SQLException {
        return null;
    }

    default void supprimer(int id) throws java.sql.SQLException {
    }
}