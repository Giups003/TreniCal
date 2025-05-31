package it.unical.trenical.client.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class TrainSearchResult {
    private int trainId;
    private String trainName;
    private String departureStation;
    private String arrivalStation;
    private LocalDate date;
    private LocalTime time;
    private int availableSeats;

    public TrainSearchResult(int trainId, String trainName, String departureStation, String arrivalStation, LocalDate date, LocalTime time, int availableSeats) {
        this.trainId = trainId;
        this.trainName = trainName;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.date = date;
        this.time = time;
        this.availableSeats = availableSeats;
    }

    // Getter
    public int getTrainId() { return trainId; }
    public String getTrainName() { return trainName; }
    public String getDepartureStation() { return departureStation; }
    public String getArrivalStation() { return arrivalStation; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public int getAvailableSeats() { return availableSeats; }

    // Setter
    public void setTrainId(int trainId) { this.trainId = trainId; }
    public void setTrainName(String trainName) { this.trainName = trainName; }
    public void setDepartureStation(String departureStation) { this.departureStation = departureStation; }
    public void setArrivalStation(String arrivalStation) { this.arrivalStation = arrivalStation; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTime(LocalTime time) { this.time = time; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    @Override
    public String toString() {
        return "TrainSearchResult{" +
                "trainId=" + trainId +
                "trainName='" + trainName + '\'' +
                ", departureStation='" + departureStation + '\'' +
                ", arrivalStation='" + arrivalStation + '\'' +
                ", date=" + date +
                ", time=" + time +
                ", availableSeats=" + availableSeats +
                '}';
    }
}