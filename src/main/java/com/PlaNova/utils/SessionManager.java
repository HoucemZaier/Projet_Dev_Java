package com.PlaNova.utils;

import com.PlaNova.models.ReservationDTO;

public class SessionManager {
    private static ReservationDTO currentReservation = new ReservationDTO();

    public static ReservationDTO getCurrentReservation() {
        return currentReservation;
    }

    public static void clearSession() {
        currentReservation = new ReservationDTO();
    }
}
