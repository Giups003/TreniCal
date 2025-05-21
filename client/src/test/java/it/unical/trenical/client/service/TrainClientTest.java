package it.unical.trenical.client.service;

import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc
import io.grpc.ManagedChannel;
import io.grpc.Server;
import org.junit.jupiter.api.*;

import it.unical.trenical.grpc.train.TrainServiceGrpc;
import it.unical.trenical.grpc.train.TrainServiceProto;

import static org.junit.jupiter.api.Assertions.*;

class TrainClientTest {
    private Server server;
    private ManagedChannel channel;

    @BeforeEach
    void setup() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder
                .forName(serverName).directExecutor()
                .addService(new FakeTrainServiceImpl()) // tua implementazione finta/mock
                .build().start();
        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    }

    @AfterEach
    void teardown() throws Exception {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void testSearchTrains() {
        // Crea lo stub per chiamare il servizio
        TrainServiceGrpc.TrainServiceBlockingStub stub = TrainServiceGrpc.newBlockingStub(channel);

        // Crea la richiesta di ricerca
        TrainServiceProto.SearchTrainRequest req = TrainServiceProto.SearchTrainRequest.newBuilder()
                .setDepartureStation("Cosenza").setArrivalStation("Roma").build();

        // Chiama il servizio e ottieni la risposta
        TrainServiceProto.TrainResponse resp = stub.searchTrains(req);

        // Verifica che la risposta contenga almeno un treno
        assertFalse(resp.getTrainsList().isEmpty());
    }
}