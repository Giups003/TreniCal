package it.unical.trenical.server;

import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.train.TrainServiceGrpc;
import it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest;
import it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse;
import it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest;
import it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest;
import it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse;
import it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest;
import it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse;
import it.unical.trenical.grpc.train.TrainServiceProto.Stop;
import it.unical.trenical.grpc.train.TrainServiceProto.ScheduleEntry;

/**
 * Implementazione del servizio gRPC per la gestione dei treni.
 * Fornisce funzionalità per la ricerca e la visualizzazione dei dettagli dei treni.
 */
public class TrainServiceImplementation extends TrainServiceGrpc.TrainServiceImplBase {

    /**
     * Ottiene informazioni sui treni.
     * 
     * @param request Richiesta contenente l'ID del treno (opzionale).
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void getTrains(TrainRequest request, StreamObserver<TrainResponse> responseObserver) {
        // Implementazione mock
        TrainResponse response = TrainResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Cerca treni in base a criteri specifici.
     * 
     * @param request Richiesta contenente i criteri di ricerca.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void searchTrains(SearchTrainRequest request, StreamObserver<TrainResponse> responseObserver) {
        // Implementazione mock
        TrainResponse response = TrainResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Ottiene dettagli di un treno specifico.
     * 
     * @param request Richiesta contenente l'ID del treno.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void getTrainDetails(TrainDetailsRequest request, StreamObserver<TrainDetailsResponse> responseObserver) {
        // Implementazione mock
        TrainDetailsResponse response = TrainDetailsResponse.newBuilder()
                .setAvailable(true)
                .setSeatsAvailable(100)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Ottiene gli orari dei treni per una specifica stazione.
     * 
     * @param request Richiesta contenente la stazione e la data.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void getTrainSchedule(ScheduleRequest request, StreamObserver<ScheduleResponse> responseObserver) {
        // Implementazione mock
        ScheduleResponse response = ScheduleResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
}
