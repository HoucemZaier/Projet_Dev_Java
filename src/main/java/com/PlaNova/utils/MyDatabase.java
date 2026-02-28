package com.PlaNova.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private final String URl = EnvConfig.get("DB_URL", "jdbc:mysql://localhost:3306/pidev");
    private final String USERNAME = EnvConfig.get("DB_USERNAME", "root");
    private final String PASSWORD = EnvConfig.get("DB_PASSWORD", "");
    private Connection connection;
    private static MyDatabase instance;

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private MyDatabase() {

        try {
            connection = DriverManager.getConnection(URl, USERNAME, PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}