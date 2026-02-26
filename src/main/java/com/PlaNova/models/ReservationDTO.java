package com.PlaNova.models;

import java.time.LocalDate;

public class ReservationDTO {
    // Data from Database
    private int destinationId;
    private String destinationName;
    private int hotelId;
    private double hotelPricePerNight;
    private Integer roomId;
    private String roomType;

    // Data from UI Selection (Client Input)
    private LocalDate startDate;
    private LocalDate endDate;
    private String transportType; // e.g., "Train", "Plane"
    private double transportCost;
    private Integer transportId;

    public ReservationDTO() {
    }

    public Integer getTransportId() {
        return transportId;
    }

    public void setTransportId(Integer transportId) {
        this.transportId = transportId;
    }

    // The "Panier" Calculation
    public double calculateTotal() {
        if (startDate == null || endDate == null)
            return 0.0;
        long days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate));
        return (hotelPricePerNight * days) + transportCost;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public double getHotelPricePerNight() {
        return hotelPricePerNight;
    }

    public void setHotelPricePerNight(double hotelPricePerNight) {
        this.hotelPricePerNight = hotelPricePerNight;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public double getTransportCost() {
        return transportCost;
    }

    public void setTransportCost(double transportCost) {
        this.transportCost = transportCost;
    }
}
