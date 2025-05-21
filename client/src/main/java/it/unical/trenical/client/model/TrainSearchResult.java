package it.unical.trenical.client.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class TrainSearchResult {
    private String trainName;
    private String departureStation;
    private String arrivalStation;
    private LocalDate date;
    private LocalTime time;
    private int availableSeats;

    public TrainSearchResult(String trainName, String departureStation, String arrivalStation, LocalDate date, LocalTime time, int availableSeats) {
        this.trainName = trainName;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.date = date;
        this.time = time;
        this.availableSeats = availableSeats;
    }

    public String getTrainName() { return trainName; }
    public String getDepartureStation() { return departureStation; }
    public String getArrivalStation() { return arrivalStation; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public int getAvailableSeats() { return availableSeats; }
}