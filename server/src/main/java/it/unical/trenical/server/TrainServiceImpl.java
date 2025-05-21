package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.train.TrainServiceGrpc.*;
import it.unical.trenical.grpc.train.TrainServiceProto.*;
import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.train.TrainServiceProto.TrainStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Implementazione del servizio gRPC per la gestione dei treni.
 * Fornisce funzionalità per la ricerca e la visualizzazione dei dettagli dei treni.
 */
public class TrainServiceImpl extends TrainServiceImplBase {

    // Database mock dei treni
    private final Map<Integer, Train> trainDatabase = new HashMap<>();

    // Database mock delle fermate dei treni
    private final Map<Integer, List<Stop>> trainStopsDatabase = new HashMap<>();

    // Database mock della disponibilità dei treni
    private final Map<Integer, Map<String, Integer>> trainAvailabilityDatabase = new HashMap<>();

    /**
     * Costruttore che inizializza i database mock.
     */
    public TrainServiceImpl() {
        initializeTrainDatabase();
        initializeTrainStopsDatabase();
        initializeTrainAvailabilityDatabase();
    }

    /**
     * Ottiene informazioni sui treni.
     * 
     * @param request Richiesta contenente l'ID del treno (opzionale).
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void getTrains(TrainRequest request, StreamObserver<TrainResponse> responseObserver) {
        try {
            List<Train> trains = new ArrayList<>();

            // Se è specificato un ID, restituisce solo quel treno
            if (request.getId() > 0) {
                Train train = trainDatabase.get(request.getId());
                if (train != null) {
                    trains.add(train);
                }
            } else {
                // Altrimenti restituisce tutti i treni
                trains.addAll(trainDatabase.values());
            }

            TrainResponse response = TrainResponse.newBuilder()
                    .addAllTrains(trains)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore durante il recupero dei treni: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Cerca treni in base a criteri specifici.
     * 
     * @param request Richiesta contenente i criteri di ricerca.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void searchTrains(SearchTrainRequest request, StreamObserver<TrainResponse> responseObserver) {
        try {
            // Valida la richiesta
            if (!isValidField(request.getDepartureStation()) ||
                !isValidField(request.getArrivalStation()) ||
                !isValidField(request.getDate())) {

                TrainResponse response = TrainResponse.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Filtra i treni in base ai criteri di ricerca
            List<Train> matchingTrains = trainDatabase.values().stream()
                    .filter(train ->
                            train.getDepartureStation().equalsIgnoreCase(request.getDepartureStation()) &&
                            train.getArrivalStation().equalsIgnoreCase(request.getArrivalStation()))
                    .collect(Collectors.toList());

            // Filtra ulteriormente per orario se specificato
            if (isValidField(request.getTimeFrom()) || isValidField(request.getTimeTo())) {
                matchingTrains = filterTrainsByTime(matchingTrains, request.getTimeFrom(), request.getTimeTo());
            }

            TrainResponse response = TrainResponse.newBuilder()
                    .addAllTrains(matchingTrains)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore durante la ricerca dei treni: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Ottiene dettagli di un treno specifico.
     * 
     * @param request Richiesta contenente l'ID del treno.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void getTrainDetails(TrainDetailsRequest request, StreamObserver<TrainDetailsResponse> responseObserver) {
        try {
            int trainId = request.getTrainId();
            Train train = trainDatabase.get(trainId);

            if (train == null) {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Treno con ID " + trainId + " non trovato")
                                .asRuntimeException()
                );
                return;
            }

            // Ottiene le fermate del treno
            List<Stop> stops = trainStopsDatabase.getOrDefault(trainId, new ArrayList<>());

            // Ottiene la disponibilità del treno (per la data corrente)
            String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            int seatsAvailable = getTrainAvailability(trainId, currentDate);
            boolean available = seatsAvailable > 0;

            TrainDetailsResponse response = TrainDetailsResponse.newBuilder()
                    .setTrain(train)
                    .addAllStops(stops)
                    .setAvailable(available)
                    .setSeatsAvailable(seatsAvailable)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore durante il recupero dei dettagli del treno: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Ottiene gli orari dei treni per una specifica stazione.
     * 
     * @param request Richiesta contenente la stazione e la data.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void getTrainSchedule(ScheduleRequest request, StreamObserver<ScheduleResponse> responseObserver) {
        try {
            // Valida la richiesta
            if (!isValidField(request.getStation()) || !isValidField(request.getDate())) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Stazione e data sono campi obbligatori")
                                .asRuntimeException()
                );
                return;
            }

            String station = request.getStation();

            // Trova le partenze (treni che partono dalla stazione specificata)
            List<ScheduleEntry> departures = findDepartures(station);

            // Trova gli arrivi (treni che arrivano alla stazione specificata)
            List<ScheduleEntry> arrivals = findArrivals(station);

            ScheduleResponse response = ScheduleResponse.newBuilder()
                    .addAllDepartures(departures)
                    .addAllArrivals(arrivals)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore durante il recupero degli orari: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Filtra i treni in base all'orario di partenza.
     * 
     * @param trains Lista di treni da filtrare
     * @param timeFrom Orario minimo di partenza (opzionale)
     * @param timeTo Orario massimo di partenza (opzionale)
     * @return Lista filtrata di treni
     */
    private List<Train> filterTrainsByTime(List<Train> trains, Timestamp timeFrom, Timestamp timeTo) {
            LocalTime minTime = null;
            LocalTime maxTime = null;

            try {
                if (isValidField(timeFrom)) {
                    minTime = LocalTime.ofSecondOfDay(timeFrom.getSeconds() % 86400);
                }
                if (isValidField(timeTo)) {
                    maxTime = LocalTime.ofSecondOfDay(timeTo.getSeconds() % 86400);
                }
            } catch (Exception e) {
                return trains; // In caso di errore di parsing, restituisce la lista non filtrata
            }

            List<Train> filteredTrains = new ArrayList<>();

            for (Train train : trains) {
                try {
                    LocalTime departureTime = LocalTime.ofSecondOfDay(train.getDepartureTime().getSeconds() % 86400);
                    boolean matchesMinTime = minTime == null || !departureTime.isBefore(minTime);
                    boolean matchesMaxTime = maxTime == null || !departureTime.isAfter(maxTime);

                    if (matchesMinTime && matchesMaxTime) {
                        filteredTrains.add(train);
                    }
                } catch (DateTimeParseException e) {
                    // Ignora i treni con orario non valido
                }
            }

            return filteredTrains;
        }

    /**
     * Trova le partenze per una stazione specifica.
     * 
     * @param station Nome della stazione
     * @return Lista di voci dell'orario per le partenze
     */
    private List<ScheduleEntry> findDepartures(String station) {
        List<ScheduleEntry> departures = new ArrayList<>();

        for (Map.Entry<Integer, Train> entry : trainDatabase.entrySet()) {
            Train train = entry.getValue();

            if (train.getDepartureStation().equalsIgnoreCase(station)) {
                ScheduleEntry scheduleEntry = ScheduleEntry.newBuilder()
                        .setTrainId(entry.getKey())
                        .setTrainName(train.getName())
                        .setTime(train.getDepartureTime())
                        .setDestination(train.getArrivalStation())
                        .setPlatform(1 + (entry.getKey() % 10)) // Binario mock
                        .setStatus(TrainStatus.ON_TIME) // Stato mock
                        .build();

                departures.add(scheduleEntry);
            }
        }

        return departures;
    }

    /**
     * Trova gli arrivi per una stazione specifica.
     * 
     * @param station Nome della stazione
     * @return Lista di voci dell'orario per gli arrivi
     */
    private List<ScheduleEntry> findArrivals(String station) {
        List<ScheduleEntry> arrivals = new ArrayList<>();

        for (Map.Entry<Integer, Train> entry : trainDatabase.entrySet()) {
            Train train = entry.getValue();

            if (train.getArrivalStation().equalsIgnoreCase(station)) {
                ScheduleEntry scheduleEntry = ScheduleEntry.newBuilder()
                        .setTrainId(entry.getKey())
                        .setTrainName(train.getName())
                        .setTime(train.getArrivalTime())
                        .setDestination(train.getDepartureStation()) // Per gli arrivi, la destinazione è la stazione di partenza
                        .setPlatform(1 + (entry.getKey() % 10)) // Binario mock
                        .setStatus(TrainStatus.ON_TIME) // Stato mock
                        .build();

                arrivals.add(scheduleEntry);
            }
        }

        return arrivals;
    }

    /**
     * Ottiene la disponibilità di posti per un treno in una data specifica.
     * 
     * @param trainId ID del treno
     * @param date Data in formato DD-MM-YYYY
     * @return Numero di posti disponibili
     */
    private int getTrainAvailability(int trainId, String date) {
        Map<String, Integer> dateAvailability = trainAvailabilityDatabase.get(trainId);

        if (dateAvailability == null) {
            return 0;
        }

        return dateAvailability.getOrDefault(date, 0);
    }

    /**
     * Verifica se un campo stringa è valido (non nullo e non vuoto).
     * 
     * @param field Campo stringa da verificare
     * @return true se il campo è valido, false altrimenti
     */
    private boolean isValidField(String field) {
        return field != null && !field.trim().isEmpty();
    }

    /**
     * Verifica se un Timestamp è valido (non nullo e non rappresenta un timestamp vuoto).
     *
     * @param timestamp Timestamp da verificare.
     * @return true se il timestamp è valido, false altrimenti.
     */
    private boolean isValidField(com.google.protobuf.Timestamp timestamp) {
        return timestamp != null && (timestamp.getSeconds() != 0 || timestamp.getNanos() != 0);
    }

    /**
     * Inizializza il database mock dei treni.
     */
    private void initializeTrainDatabase() {
        // Frecciarossa Roma-Milano
        trainDatabase.put(1001, Train.newBuilder()
                .setId(1001)
                .setName("Frecciarossa 9612")
                .setDepartureStation("Roma Termini")
                .setArrivalStation("Milano Centrale")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(8, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(11, 0).toSecondOfDay())
                        .build())
                .build());

        // Frecciarossa Milano-Roma
        trainDatabase.put(1002, Train.newBuilder()
                .setId(1002)
                .setName("Frecciarossa 9613")
                .setDepartureStation("Milano Centrale")
                .setArrivalStation("Roma Termini")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(9, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(12, 0).toSecondOfDay())
                        .build())
                .build());

        // Intercity Roma-Napoli
        trainDatabase.put(2001, Train.newBuilder()
                .setId(2001)
                .setName("Intercity 583")
                .setDepartureStation("Roma Termini")
                .setArrivalStation("Napoli Centrale")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(10, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(11, 30).toSecondOfDay())
                        .build())
                .build());

        // Regionale Firenze-Bologna
        trainDatabase.put(3001, Train.newBuilder()
                .setId(3001)
                .setName("Regionale 11852")
                .setDepartureStation("Firenze S.M.N.")
                .setArrivalStation("Bologna Centrale")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(12, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(12, 45).toSecondOfDay())
                        .build())
                .build());

        // Frecciabianca Torino-Venezia
        trainDatabase.put(4001, Train.newBuilder()
                .setId(4001)
                .setName("Frecciabianca 8512")
                .setDepartureStation("Torino Porta Nuova")
                .setArrivalStation("Venezia S. Lucia")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(14, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(16, 30).toSecondOfDay())
                        .build())
                .build());
    }

    /**
     * Inizializza il database mock delle fermate dei treni.
     */
    private void initializeTrainStopsDatabase() {
        // Fermate del Frecciarossa Roma-Milano
        List<Stop> stops1001 = new ArrayList<>();
        stops1001.add(Stop.newBuilder()
                .setStation("Roma Termini")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(8, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(11, 0).toSecondOfDay())
                        .build())
                .setPlatform(5)
                .build());
        stops1001.add(Stop.newBuilder()
                .setStation("Firenze S.M.N.")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(9, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(9, 15).toSecondOfDay())
                        .build())
                .setPlatform(8)
                .build());
        stops1001.add(Stop.newBuilder()
                .setStation("Bologna Centrale")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(9, 45).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(9, 40).toSecondOfDay())
                        .build())
                .setPlatform(3)
                .build());
        stops1001.add(Stop.newBuilder()
                .setStation("Milano Centrale")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(0, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(11, 0).toSecondOfDay())
                        .build())
                .setPlatform(12)
                .build());
        trainStopsDatabase.put(1001, stops1001);

        // Fermate del Frecciarossa Milano-Roma
        List<Stop> stops1002 = new ArrayList<>();
        stops1002.add(Stop.newBuilder()
                .setStation("Milano Centrale")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(9, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(0, 30).toSecondOfDay())
                        .build())
                .setPlatform(14)
                .build());
        stops1002.add(Stop.newBuilder()
                .setStation("Bologna Centrale")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(10, 15).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(10, 10).toSecondOfDay())
                        .build())
                .setPlatform(4)
                .build());
        stops1002.add(Stop.newBuilder()
                .setStation("Firenze S.M.N.")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(10, 45).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(10, 40).toSecondOfDay())
                        .build())
                .setPlatform(9)
                .build());
        stops1002.add(Stop.newBuilder()
                .setStation("Roma Termini")
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(0, 0).toSecondOfDay())
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(LocalTime.of(12, 0).toSecondOfDay())
                        .build())
                .setPlatform(6)
                .build());
        trainStopsDatabase.put(1002, stops1002);

        // Altre fermate possono essere aggiunte secondo necessità
    }

    /**
     * Inizializza il database mock della disponibilità dei treni.
     */
    private void initializeTrainAvailabilityDatabase() {
        // Data corrente per i test
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String nextWeek = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Disponibilità per il treno 1001
        Map<String, Integer> availability1001 = new HashMap<>();
        availability1001.put(today, 45);
        availability1001.put(tomorrow, 120);
        availability1001.put(nextWeek, 200);
        trainAvailabilityDatabase.put(1001, availability1001);

        // Disponibilità per il treno 1002
        Map<String, Integer> availability1002 = new HashMap<>();
        availability1002.put(today, 30);
        availability1002.put(tomorrow, 100);
        availability1002.put(nextWeek, 180);
        trainAvailabilityDatabase.put(1002, availability1002);

        // Disponibilità per il treno 2001
        Map<String, Integer> availability2001 = new HashMap<>();
        availability2001.put(today, 80);
        availability2001.put(tomorrow, 150);
        availability2001.put(nextWeek, 220);
        trainAvailabilityDatabase.put(2001, availability2001);

        // Altre disponibilità possono essere aggiunte secondo necessità
    }
}
