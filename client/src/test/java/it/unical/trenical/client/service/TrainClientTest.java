package it.unical.trenical.client.service;

import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import org.junit.jupiter.api.*;

import it.unical.trenical.grpc.train.TrainServiceGrpc;
import it.unical.trenical.grpc.train.TrainServiceProto;
import it.unical.trenical.grpc.common.Train;

import static org.junit.jupiter.api.Assertions.*;

class TrainClientTest {
    private Server server;
    private ManagedChannel channel;

    // Implementazione finta del servizio per i test
    static class FakeTrainServiceImpl extends TrainServiceGrpc.TrainServiceImplBase {
        @Override
        public void searchTrains(TrainServiceProto.SearchTrainRequest request,
                                 io.grpc.stub.StreamObserver<TrainServiceProto.TrainResponse> responseObserver) {
            TrainServiceProto.TrainResponse.Builder respBuilder = TrainServiceProto.TrainResponse.newBuilder();
            // Aggiungi un treno finto solo se le stazioni sono valorizzate
            if (!request.getDepartureStation().isEmpty() && !request.getArrivalStation().isEmpty()) {
                respBuilder.addTrains(Train.newBuilder()
                        .setId(1)
                        .setDepartureStation(request.getDepartureStation())
                        .setArrivalStation(request.getArrivalStation())
                        .build());
            }
            responseObserver.onNext(respBuilder.build());
            responseObserver.onCompleted();
        }
    }

    @BeforeEach
    void setup() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder
                .forName(serverName).directExecutor()
                .addService(new FakeTrainServiceImpl())
                .build().start();
        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    }

    @AfterEach
    void teardown() throws Exception {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void testSearchTrainsSuccess() {
        TrainServiceGrpc.TrainServiceBlockingStub stub = TrainServiceGrpc.newBlockingStub(channel);

        TrainServiceProto.SearchTrainRequest req = TrainServiceProto.SearchTrainRequest.newBuilder()
                .setDepartureStation("Cosenza").setArrivalStation("Roma").build();

        TrainServiceProto.TrainResponse resp = stub.searchTrains(req);

        assertNotNull(resp, "La risposta non deve essere null");
        assertFalse(resp.getTrainsList().isEmpty(), "La lista dei treni non deve essere vuota");
        Train train = resp.getTrainsList().get(0);
        assertEquals("Cosenza", train.getDepartureStation(), "La stazione di partenza deve corrispondere");
        assertEquals("Roma", train.getArrivalStation(), "La stazione di arrivo deve corrispondere");
    }

    @Test
    void testSearchTrainsNoResults() {
        TrainServiceGrpc.TrainServiceBlockingStub stub = TrainServiceGrpc.newBlockingStub(channel);

        TrainServiceProto.SearchTrainRequest req = TrainServiceProto.SearchTrainRequest.newBuilder()
                .setDepartureStation("").setArrivalStation("").build();

        TrainServiceProto.TrainResponse resp = stub.searchTrains(req);

        assertNotNull(resp, "La risposta non deve essere null");
        assertTrue(resp.getTrainsList().isEmpty(), "La lista dei treni deve essere vuota se i parametri sono vuoti");
    }
}
