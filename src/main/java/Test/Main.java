package Test;

import Models.Activite;
import Models.Excursion;
import Services.ServiceActivite;
import Services.ServiceExcursion;

import java.sql.Date;
import java.sql.SQLDataException;

public class Main {
    public static void main(String[] args) {

        ServiceExcursion serviceExcursion = new ServiceExcursion();

        try {
        serviceExcursion.ajouter( new Excursion(0, "Excursion Plage", "Hammamet",
                Date.valueOf("2026-03-01"), Date.valueOf("2026-03-03"),
                250.0, 30, "ouverte"));
        System.out.println(serviceExcursion.recuperer());
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }
}