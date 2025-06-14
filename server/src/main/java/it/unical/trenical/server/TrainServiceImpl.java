package it.unical.trenical.server;

    import com.google.protobuf.Timestamp;
    import io.grpc.Status;
    import io.grpc.stub.StreamObserver;
    import it.unical.trenical.grpc.common.Station;
    import it.unical.trenical.grpc.common.Train;
    import it.unical.trenical.grpc.train.*;
    import java.time.*;
    import java.util.List;

    public class TrainServiceImpl extends TrainServiceGrpc.TrainServiceImplBase {
        private final DataStore dataStore;

        public TrainServiceImpl() {
            this.dataStore = DataStore.getInstance();
        }

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
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Errore interno: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void searchTrains(SearchTrainRequest request, StreamObserver<TrainResponse> responseObserver) {
            try {
                String departureStation = request.getDepartureStation();
                String arrivalStation = request.getArrivalStation();
                String trainType = request.getTrainType(); // Leggi la tipologia

                // Estrai la data dal timestamp, se presente
                String date = null;
                if (request.hasDate()) {
                    Timestamp timestamp = request.getDate();
                    // Converti il timestamp in formato data utilizzabile
                    LocalDateTime dateTime = LocalDateTime.ofEpochSecond(
                            timestamp.getSeconds(), timestamp.getNanos(), ZoneOffset.UTC);
                    date = dateTime.toLocalDate().toString(); // formato YYYY-MM-DD
                }

                int limit = 20; // valore predefinito

                List<Train> trains = dataStore.searchTrains(departureStation, arrivalStation, date, trainType, limit);

                TrainResponse response = TrainResponse.newBuilder()
                        .addAllTrains(trains)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Errore interno: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void getTrainDetails(TrainDetailsRequest request, StreamObserver<TrainDetailsResponse> responseObserver) {
            try {
                int trainId = request.getTrainId();
                Train train = null;
                // Se la data è presente nella richiesta, cerca il treno per ID e data
                if (request.hasDate()) {
                    Timestamp dateTs = request.getDate();
                    LocalDate date = Instant.ofEpochSecond(dateTs.getSeconds())
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    // Cerca il treno per ID e data
                    List<Train> trainsForDay = dataStore.generateTrainsForDay(null, null, date);
                    train = trainsForDay.stream()
                            .filter(t -> t.getId() == trainId)
                            .findFirst()
                            .orElse(null);
                } else {
                    // Cerca solo per ID (comportamento legacy)
                    train = dataStore.getTrainById(trainId);
                }

                if (train == null) {
                    responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Treno con ID " + trainId + " non trovato per la data richiesta")
                            .asRuntimeException());
                    return;
                }

                int seatsAvailable = dataStore.getAvailableSeats(trainId);
                boolean isAvailable = seatsAvailable > 0;

                TrainDetailsResponse response = TrainDetailsResponse.newBuilder()
                        .setTrain(train)
                        .setAvailable(isAvailable)
                        .setSeatsAvailable(seatsAvailable)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Errore interno: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void getTrains(TrainRequest request, StreamObserver<TrainResponse> responseObserver) {
            try {
                int trainId = request.getId();
                List<Train> trains;

                if (trainId > 0) {
                    // Cerca un treno specifico per ID
                    Train train = dataStore.getTrainById(trainId);
                    trains = train != null ? List.of(train) : List.of();
                } else {
                    // Ottieni tutti i treni
                    trains = dataStore.getAllTrains();
                }

                TrainResponse response = TrainResponse.newBuilder()
                        .addAllTrains(trains)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Errore interno: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        @Override
        public void getTrainSchedule(ScheduleRequest request, StreamObserver<ScheduleResponse> responseObserver) {
            try {
                // La ScheduleRequest non ha trainId, ma solo station e date
                // Quindi questa funzione deve restituire tutte le partenze/arrivi per la stazione e data richieste
                String stationName = request.getStation();
                Timestamp date = request.hasDate() ? request.getDate() : null;
                List<ScheduleEntry> departures = new java.util.ArrayList<>();
                List<ScheduleEntry> arrivals = new java.util.ArrayList<>();
                // Per ogni treno, controlla se parte o arriva dalla stazione richiesta
                for (Train train : dataStore.getAllTrains()) {
                    // Trova la rotta che corrisponde alle stazioni di partenza e arrivo del treno
                    Route route = dataStore.getAllRoutes().stream()
                        .filter(r -> {
                            Station depStation = dataStore.getStationById(r.getDepartureStationId());
                            Station arrStation = dataStore.getStationById(r.getArrivalStationId());
                            return depStation != null && arrStation != null && 
                                   depStation.getName().equalsIgnoreCase(train.getDepartureStation()) &&
                                   arrStation.getName().equalsIgnoreCase(train.getArrivalStation());
                        })
                        .findFirst().orElse(null);
                    if (route == null) {
                        // Se non troviamo una rotta corrispondente, usiamo direttamente i dati del treno
                        ScheduleEntry.Builder entry = ScheduleEntry.newBuilder()
                            .setTrainId(train.getId())
                            .setTrainName(train.getName())
                            .setTime(train.getDepartureTime())
                            .setDestination(train.getArrivalStation())
                            .setStatus(TrainStatus.ON_TIME);

                        if (train.getDepartureStation().equalsIgnoreCase(stationName)) {
                            departures.add(entry.build());
                        }
                        if (train.getArrivalStation().equalsIgnoreCase(stationName)) {
                            entry.setDestination(train.getDepartureStation());
                            entry.setTime(train.getArrivalTime());
                            arrivals.add(entry.build());
                        }
                        continue;
                    }
                    Station depStation = dataStore.getStationById(route.getDepartureStationId());
                    Station arrStation = dataStore.getStationById(route.getArrivalStationId());
                    if (depStation != null && depStation.getName().equalsIgnoreCase(stationName)) {
                        // Partenza
                        ScheduleEntry.Builder entry = ScheduleEntry.newBuilder()
                            .setTrainId(train.getId())
                            .setTrainName(train.getName())
                            .setTime(Timestamp.newBuilder().setSeconds(parseTimeToEpoch(route.getDepartureTime(), date)).build())
                            .setDestination(arrStation != null ? arrStation.getName() : "?")
                            .setStatus(TrainStatus.ON_TIME);
                        departures.add(entry.build());
                    }
                    if (arrStation != null && arrStation.getName().equalsIgnoreCase(stationName)) {
                        // Arrivo
                        ScheduleEntry.Builder entry = ScheduleEntry.newBuilder()
                            .setTrainId(train.getId())
                            .setTrainName(train.getName())
                            .setTime(Timestamp.newBuilder().setSeconds(parseTimeToEpoch(route.getArrivalTime(), date)).build())
                            .setDestination(depStation != null ? depStation.getName() : "?")
                            .setStatus(TrainStatus.ON_TIME);
                        arrivals.add(entry.build());
                    }
                }
                ScheduleResponse response = ScheduleResponse.newBuilder()
                    .addAllDepartures(departures)
                    .addAllArrivals(arrivals)
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Errore interno: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        private long parseTimeToEpoch(String time, Timestamp date) {
            // time formato HH:mm, date è la data del giorno
            if (date == null || time == null || time.isEmpty()) return 0L;
            try {
                LocalDate localDate = Instant.ofEpochSecond(date.getSeconds()).atZone(ZoneId.systemDefault()).toLocalDate();
                LocalTime localTime = LocalTime.parse(time);
                LocalDateTime dateTime = LocalDateTime.of(localDate, localTime);
                return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
            } catch (Exception e) {
                System.err.println("Errore nel parsing del tempo: " + e.getMessage() + " per il valore: '" + time + "'");
                return 0L;
            }
        }

        @Override
        public void getTrainStops(GetTrainStopsRequest request, StreamObserver<GetTrainStopsResponse> responseObserver) {
            try {
                int trainId = request.getTrainId();
                Train train = dataStore.getTrainById(trainId);
                if (train == null) {
                    responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Treno con ID " + trainId + " non trovato")
                            .asRuntimeException());
                    return;
                }
                Route route = dataStore.getAllRoutes().stream()
                        .filter(r -> r.getId() == trainId)
                        .findFirst().orElse(null);
                List<Stop> stops = new java.util.ArrayList<>();
                if (route != null) {
                    // Fermata di partenza
                    Stop.Builder dep = Stop.newBuilder()
                        .setId(1)
                        .setTrainId(trainId)
                        .setStationId(route.getDepartureStationId())
                        .setDepartureTime(parseTimeToTimestamp(route.getDepartureTime()))
                        .setNote("Partenza");
                    stops.add(dep.build());
                    // Fermata di arrivo
                    Stop.Builder arr = Stop.newBuilder()
                        .setId(2)
                        .setTrainId(trainId)
                        .setStationId(route.getArrivalStationId())
                        .setArrivalTime(parseTimeToTimestamp(route.getArrivalTime()))
                        .setNote("Arrivo");
                    stops.add(arr.build());
                }
                GetTrainStopsResponse response = GetTrainStopsResponse.newBuilder()
                        .addAllStops(stops)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Errore interno: " + e.getMessage())
                        .asRuntimeException());
            }
        }

        private Timestamp parseTimeToTimestamp(String time) {
            if (time == null || time.isEmpty()) return Timestamp.getDefaultInstance();
            try {
                LocalTime localTime = LocalTime.parse(time);
                LocalDate today = LocalDate.now();
                LocalDateTime dateTime = LocalDateTime.of(today, localTime);
                return Timestamp.newBuilder().setSeconds(dateTime.toEpochSecond(ZoneOffset.UTC)).build();
            } catch (Exception e) {
                System.err.println("Errore nel parsing del tempo: " + e.getMessage() + " per il valore: '" + time + "'");
                return Timestamp.getDefaultInstance();
            }
        }

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
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Errore interno: " + e.getMessage())
                        .asRuntimeException());
            }
        }
    }
