package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.common.Station;
import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.train.*;
import java.time.*;
import java.util.List;

/**
 * Servizio gRPC per la gestione dei treni e delle stazioni del sistema TreniCal.
 * Fornisce funzionalità per ricerca stazioni, ricerca treni, dettagli treni e orari.
 */
public class TrainServiceImpl extends TrainServiceGrpc.TrainServiceImplBase {

    /** Istanza singleton del DataStore per accesso ai dati */
    private final DataStore dataStore;

        public TrainServiceImpl() {
            this.dataStore = DataStore.getInstance();
        }

    // ==================== RICERCA STAZIONI ====================

    /**
     * Cerca stazioni per nome con limite di risultati.
     *
     * @param request richiesta contenente query di ricerca e limite
     * @param responseObserver observer per inviare la risposta
     */
    @Override
    public void searchStations(SearchStationRequest request, StreamObserver<SearchStationResponse> responseObserver) {
        try {
            String query = request.getQuery();
            int limit = request.getLimit();

            List<Station> stations = dataStore.searchStations(query, limit);

            SearchStationResponse response = SearchStationResponse.newBuilder()
                    .addAllStations(stations)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            handleException(e, "Errore nella ricerca stazioni", responseObserver);
        }
    }

    // ==================== RICERCA TRENI ====================

    /**
     * Cerca treni per tratta, data e tipo con filtri opzionali.
     *
     * @param request richiesta contenente parametri di ricerca
     * @param responseObserver observer per inviare la risposta
     */
    @Override
    public void searchTrains(SearchTrainRequest request, StreamObserver<TrainResponse> responseObserver) {
        try {
            String departureStation = request.getDepartureStation();
            String arrivalStation = request.getArrivalStation();
            String trainType = request.getTrainType();

            // Conversione timestamp in data utilizzabile
            String date = null;
            if (request.hasDate()) {
                date = convertTimestampToDateString(request.getDate());
            }

            int limit = 20; // Limite predefinito risultati

            List<Train> trains = dataStore.searchTrains(departureStation, arrivalStation, date, trainType, limit);

            TrainResponse response = TrainResponse.newBuilder()
                    .addAllTrains(trains)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            handleException(e, "Errore nella ricerca treni", responseObserver);
        }
    }

    // ==================== DETTAGLI TRENI ====================

    /**
     * Recupera i dettagli completi di un treno specifico per una data.
     * Include informazioni su disponibilità posti.
     *
     * @param request richiesta contenente ID treno e data
     * @param responseObserver observer per inviare la risposta
     */
    @Override
    public void getTrainDetails(TrainDetailsRequest request, StreamObserver<TrainDetailsResponse> responseObserver) {
        try {
            int trainId = request.getTrainId();
            Train train = null;
            LocalDateTime travelDateTime = null;

            // Ricerca treno per ID e data se specificata
            if (request.hasDate()) {
                travelDateTime = convertTimestampToLocalDateTime(request.getDate());
                LocalDate date = travelDateTime.toLocalDate();

                // Genera treni per il giorno e filtra per ID
                List<Train> trainsForDay = dataStore.generateTrainsForDay(null, null, date);
                train = trainsForDay.stream()
                        .filter(t -> t.getId() == trainId)
                        .findFirst()
                        .orElse(null);
            }

            if (train == null) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Treno con ID " + trainId + " non trovato per la data richiesta")
                        .asRuntimeException());
                return;
            }

            // Calcolo disponibilità posti
            int seatsAvailable = (travelDateTime != null)
                ? dataStore.getAvailableSeats(trainId, travelDateTime)
                : 0;
            boolean isAvailable = seatsAvailable > 0;

            TrainDetailsResponse response = TrainDetailsResponse.newBuilder()
                    .setTrain(train)
                    .setAvailable(isAvailable)
                    .setSeatsAvailable(seatsAvailable)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            handleException(e, "Errore nel recupero dettagli treno", responseObserver);
        }
    }

    // ==================== LISTA TRENI ====================

    /**
     * Recupera tutti i treni disponibili o un treno specifico per ID.
     *
     * @param request richiesta contenente ID treno (opzionale)
     * @param responseObserver observer per inviare la risposta
     */
    @Override
    public void getTrains(TrainRequest request, StreamObserver<TrainResponse> responseObserver) {
        try {
            int trainId = request.getId();
            List<Train> trains;

            if (trainId > 0) {
                // Ricerca treno specifico per ID (implementazione da completare)
                trains = List.of();
            } else {
                // Recupera tutti i treni disponibili
                trains = dataStore.getAllTrains();
            }

            TrainResponse response = TrainResponse.newBuilder()
                    .addAllTrains(trains)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            handleException(e, "Errore nel recupero lista treni", responseObserver);
        }
    }

    // ==================== ORARI STAZIONE ====================

    /**
     * Recupera gli orari di partenza e arrivo per una stazione specifica in una data.
     *
     * @param request richiesta contenente stazione e data
     * @param responseObserver observer per inviare la risposta
     */
    @Override
    public void getTrainSchedule(ScheduleRequest request, StreamObserver<ScheduleResponse> responseObserver) {
        try {
            String stationName = request.getStation();
            Timestamp date = request.hasDate() ? request.getDate() : null;

            List<ScheduleEntry> departures = new java.util.ArrayList<>();
            List<ScheduleEntry> arrivals = new java.util.ArrayList<>();

            // Analizza tutti i treni per trovare partenze e arrivi dalla stazione
            for (Train train : dataStore.getAllTrains()) {
                processTrainForSchedule(train, stationName, date, departures, arrivals);
            }

            ScheduleResponse response = ScheduleResponse.newBuilder()
                    .addAllDepartures(departures)
                    .addAllArrivals(arrivals)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            handleException(e, "Errore nel recupero orari stazione", responseObserver);
        }
    }

    // ==================== LISTA ROTTE ====================

    /**
     * Recupera tutte le rotte disponibili nel sistema.
     *
     * @param request richiesta (vuota)
     * @param responseObserver observer per inviare la risposta
     */
    @Override
    public void listRoutes(ListRoutesRequest request, StreamObserver<ListRoutesResponse> responseObserver) {
        try {
            List<Route> routes = dataStore.getAllRoutes();

            ListRoutesResponse response = ListRoutesResponse.newBuilder()
                    .addAllRoutes(routes)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            handleException(e, "Errore nel recupero rotte", responseObserver);
        }
    }

    // ==================== METODI PRIVATI DI SUPPORTO ====================

    /**
     * Elabora un treno per generare le entry di orario per una stazione specifica.
     *
     * @param train treno da elaborare
     * @param stationName nome della stazione di interesse
     * @param date data di riferimento
     * @param departures lista delle partenze da popolare
     * @param arrivals lista degli arrivi da popolare
     */
    private void processTrainForSchedule(Train train, String stationName, Timestamp date,
                                       List<ScheduleEntry> departures, List<ScheduleEntry> arrivals) {

        // Trova la rotta corrispondente al treno
        Route route = dataStore.getAllRoutes().stream()
                .filter(r -> isRouteMatchingTrain(r, train))
                .findFirst()
                .orElse(null);

        if (route == null) {
            // Usa direttamente i dati del treno se non c'è rotta corrispondente
            processTrainDirectly(train, stationName, departures, arrivals);
            return;
        }

        // Elabora partenze e arrivi usando la rotta
        processTrainWithRoute(train, route, stationName, date, departures, arrivals);
    }

    /**
     * Verifica se una rotta corrisponde a un treno specifico.
     */
    private boolean isRouteMatchingTrain(Route route, Train train) {
        Station depStation = dataStore.getStationById(route.getDepartureStationId());
        Station arrStation = dataStore.getStationById(route.getArrivalStationId());

        return depStation != null && arrStation != null &&
               depStation.getName().equalsIgnoreCase(train.getDepartureStation()) &&
               arrStation.getName().equalsIgnoreCase(train.getArrivalStation());
    }

    /**
     * Elabora un treno usando direttamente i suoi dati (senza rotta).
     */
    private void processTrainDirectly(Train train, String stationName,
                                    List<ScheduleEntry> departures, List<ScheduleEntry> arrivals) {

        ScheduleEntry.Builder entryBuilder = ScheduleEntry.newBuilder()
                .setTrainId(train.getId())
                .setTrainName(train.getName())
                .setStatus(TrainStatus.ON_TIME);

        // Partenza dalla stazione
        if (train.getDepartureStation().equalsIgnoreCase(stationName)) {
            ScheduleEntry departure = entryBuilder
                    .setTime(train.getDepartureTime())
                    .setDestination(train.getArrivalStation())
                    .build();
            departures.add(departure);
        }

        // Arrivo alla stazione
        if (train.getArrivalStation().equalsIgnoreCase(stationName)) {
            ScheduleEntry arrival = entryBuilder
                    .setTime(train.getArrivalTime())
                    .setDestination(train.getDepartureStation())
                    .build();
            arrivals.add(arrival);
        }
    }

    /**
     * Elabora un treno usando una rotta specifica.
     */
    private void processTrainWithRoute(Train train, Route route, String stationName, Timestamp date,
                                     List<ScheduleEntry> departures, List<ScheduleEntry> arrivals) {

        Station depStation = dataStore.getStationById(route.getDepartureStationId());
        Station arrStation = dataStore.getStationById(route.getArrivalStationId());

        // Partenza dalla stazione
        if (depStation != null && depStation.getName().equalsIgnoreCase(stationName)) {
            ScheduleEntry departure = ScheduleEntry.newBuilder()
                    .setTrainId(train.getId())
                    .setTrainName(train.getName())
                    .setTime(Timestamp.newBuilder()
                            .setSeconds(parseTimeToEpoch(route.getDepartureTime(), date))
                            .build())
                    .setDestination(arrStation != null ? arrStation.getName() : "Destinazione sconosciuta")
                    .setStatus(TrainStatus.ON_TIME)
                    .build();
            departures.add(departure);
        }

        // Arrivo alla stazione
        if (arrStation != null && arrStation.getName().equalsIgnoreCase(stationName)) {
            ScheduleEntry arrival = ScheduleEntry.newBuilder()
                    .setTrainId(train.getId())
                    .setTrainName(train.getName())
                    .setTime(Timestamp.newBuilder()
                            .setSeconds(parseTimeToEpoch(route.getArrivalTime(), date))
                            .build())
                    .setDestination(depStation != null ? depStation.getName() : "Origine sconosciuta")
                    .setStatus(TrainStatus.ON_TIME)
                    .build();
            arrivals.add(arrival);
        }
    }

    /**
     * Converte una stringa tempo (HH:mm) e una data in timestamp epoch.
     *
     * @param time orario in formato HH:mm
     * @param date data di riferimento
     * @return timestamp in secondi dall'epoch
     */
    private long parseTimeToEpoch(String time, Timestamp date) {
        if (date == null || time == null || time.isEmpty()) {
            return 0L;
        }

        try {
            LocalDate localDate = Instant.ofEpochSecond(date.getSeconds())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalTime localTime = LocalTime.parse(time);
            LocalDateTime dateTime = LocalDateTime.of(localDate, localTime);

            return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();

        } catch (Exception e) {
            System.err.println("Errore nel parsing del tempo: " + e.getMessage() +
                             " per il valore: '" + time + "'");
            return 0L;
        }
    }

    /**
     * Converte un Timestamp protobuf in stringa data formato YYYY-MM-DD.
     */
    private String convertTimestampToDateString(Timestamp timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(
                timestamp.getSeconds(), timestamp.getNanos(), ZoneOffset.UTC);
        return dateTime.toLocalDate().toString();
    }

    /**
     * Converte un Timestamp protobuf in LocalDateTime.
     */
    private LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * Gestisce le eccezioni in modo uniforme per tutti i servizi.
     */
    private void handleException(Exception e, String context, StreamObserver<?> responseObserver) {
        String errorMessage = context + ": " + e.getMessage();
        responseObserver.onError(Status.INTERNAL
                .withDescription(errorMessage)
                .asRuntimeException());
    }
}
