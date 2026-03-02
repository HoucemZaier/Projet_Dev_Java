package com.PlaNova;

import com.PlaNova.models.Destination;
import com.PlaNova.services.DestinationService;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class DbTest {
    public static void main(String[] args) {
        try {
            DestinationService ds = new DestinationService();
            List<Destination> list = ds.show();
            Collections.sort(list, Comparator.comparing(Destination::getNomDestination));
            System.out.println("TOTAL_ROWS: " + list.size());
            for (int i = 0; i < 30 && i < list.size(); i++) {
                Destination d = list.get(i);
                System.out.println(
                        "ROW " + (i + 1) + " ID:" + d.getIdDestination() + " NAME:[" + d.getNomDestination() + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
