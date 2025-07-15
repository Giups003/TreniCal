package it.unical.trenical.client.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Modello dati per risultati ricerca treni.
 * Utilizzato per il trasferimento dati tra servizi gRPC e UI client.
 */
public class TrainSearchResult {
    private final int trainId;
    private final String trainName;
    private final String departureStation;
    private final String arrivalStation;
    private final LocalDate date;
    private final LocalTime time;
    private final int availableSeats;

    public TrainSearchResult(int trainId, String trainName, String departureStation, String arrivalStation, LocalDate date, LocalTime time, int availableSeats) {
        this.trainId = trainId;
        this.trainName = trainName;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.date = date;
        this.time = time;
        this.availableSeats = availableSeats;
    }

    public int getTrainId() { return trainId; }
    public String getTrainName() { return trainName; }
    public String getDepartureStation() { return departureStation; }
    public String getArrivalStation() { return arrivalStation; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public int getAvailableSeats() { return availableSeats; }
}