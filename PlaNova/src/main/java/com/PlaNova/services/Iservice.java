package com.PlaNova.services;

import com.PlaNova.models.Destination;
import java.sql.SQLDataException;
import java.util.List;

public interface Iservice <T> {
    void add(T t) throws SQLDataException;

    void delete(T t) throws SQLDataException;

    void modify(T t) throws SQLDataException;

    List<T> show() throws SQLDataException;
}