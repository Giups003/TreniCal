package it.unical.trenical.client.service;

import com.google.protobuf.Timestamp;
import it.unical.trenical.grpc.train.*;
import it.unical.trenical.grpc.common.Train;
import io.grpc.stub.StreamObserver;

/**
 * Implementazione fittizia del servizio TrainService per i test.
 */
public class FakeTrainServiceImpl extends TrainServiceGrpc.TrainServiceImplBase {

    @Override
    public void searchTrains(SearchTrainRequest request,
                             StreamObserver<TrainResponse> responseObserver) {
        // Crea una risposta fittizia con dati di test
        Train mockTrain = Train.newBuilder()
                .setId(1)
                .setName("Freccia Rossa Test")
                .setDepartureStation(request.getDepartureStation())
                .setArrivalStation(request.getArrivalStation())
                .setDepartureTime(Timestamp.newBuilder()
                        .setSeconds(System.currentTimeMillis() / 1000)
                        .build())
                .setArrivalTime(Timestamp.newBuilder()
                        .setSeconds(System.currentTimeMillis() / 1000 + 3600) // 1 ora dopo
                        .build())
                .build();

        TrainResponse response = TrainResponse.newBuilder()
                .addTrains(mockTrain)
                .build();

        // Invia la risposta e completa la chiamata
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}