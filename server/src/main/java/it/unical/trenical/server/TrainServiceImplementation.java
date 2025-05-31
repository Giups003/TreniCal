package it.unical.trenical.server;

    import com.google.protobuf.Timestamp;
    import io.grpc.Status;
    import io.grpc.stub.StreamObserver;
    import it.unical.trenical.grpc.common.Station;
    import it.unical.trenical.grpc.common.Train;
    import it.unical.trenical.grpc.train.*;
    import it.unical.trenical.server.data.DataStore;

    import java.time.LocalDateTime;
    import java.time.ZoneOffset;
    import java.util.List;

    public class TrainServiceImplementation extends TrainServiceGrpc.TrainServiceImplBase {
        private final DataStore dataStore;

        public TrainServiceImplementation() {
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

                List<Train> trains = dataStore.searchTrains(departureStation, arrivalStation, date, limit);

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
                Train train = dataStore.getTrainById(trainId);

                if (train == null) {
                    responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Treno con ID " + trainId + " non trovato")
                            .asRuntimeException());
                    return;
                }

                // Creiamo una risposta base con il treno trovato
                TrainDetailsResponse response = TrainDetailsResponse.newBuilder()
                        .setTrain(train)
                        .setAvailable(true)
                        .setSeatsAvailable(50) // Valore di esempio
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
            // Implementazione temporanea - restituisce una risposta vuota
            responseObserver.onNext(ScheduleResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void getTrainStops(GetTrainStopsRequest request, StreamObserver<GetTrainStopsResponse> responseObserver) {
            // Implementazione temporanea - restituisce una risposta vuota
            responseObserver.onNext(GetTrainStopsResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
    }