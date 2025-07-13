package it.unical.trenical.client;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import it.unical.trenical.grpc.train.*;
import it.unical.trenical.grpc.common.Train;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client per interagire con il servizio gRPC dei treni.
 * Fornisce metodi per cercare treni, ottenere dettagli e orari.
 */
public class TrainClient {
    private static final Logger logger = Logger.getLogger(TrainClient.class.getName());

    private final ManagedChannel channel;
    private final TrainServiceGrpc.TrainServiceBlockingStub blockingStub;

    /**
     * Costruttore che inizializza la connessione al server gRPC.
     * 
     * @param host Indirizzo del server
     * @param port Porta del server
     */
    public TrainClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Connessione non sicura per semplicità
                .build());
    }

    /**
     * Costruttore che accetta un canale già configurato.
     * Utile per i test o per configurazioni avanzate.
     * 
     * @param channel Canale gRPC già configurato
     */
    public TrainClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = TrainServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Chiude la connessione al server.
     * 
     * @throws InterruptedException Se l'attesa viene interrotta
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Ottiene la lista di tutti i treni o di un treno specifico.
     * 
     * @param trainId ID del treno (0 per ottenere tutti i treni)
     * @return Lista di treni
     */
    public List<Train> getTrains(int trainId) {
        logger.info("Richiesta informazioni sui treni (ID: " + trainId + ")");
        TrainRequest request = TrainRequest.newBuilder()
                .setId(trainId)
                .build();

        TrainResponse response;
        try {
            response = blockingStub.getTrains(request);
            logger.info("Ricevuti " + response.getTrainsCount() + " treni");
            return response.getTrainsList();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "Errore RPC: {0}", e.getStatus());
            throw e;
        }
    }

    /**
     * Cerca treni in base a criteri specifici.
     * 
     * @param departureStation Stazione di partenza
     * @param arrivalStation Stazione di arrivo
     * @param date Data del viaggio (formato YYYY-MM-DD)
     * @param timeFrom Orario minimo di partenza (opzionale)
     * @param timeTo Orario massimo di partenza (opzionale)
     * @param trainType Tipo di treno (opzionale)
     * @return Lista di treni che soddisfano i criteri
     */
    public List<Train> searchTrains(
            String departureStation, 
            String arrivalStation, 
            Timestamp date,
            Timestamp timeFrom,
            Timestamp timeTo,
            String trainType) {

        logger.info("Ricerca treni da " + departureStation + " a " + arrivalStation + " il " + date + " tipo: " + trainType);

        SearchTrainRequest.Builder requestBuilder = SearchTrainRequest.newBuilder()
                .setDepartureStation(departureStation)
                .setArrivalStation(arrivalStation)
                .setDate(date);

        if (timeFrom != null && (timeFrom.getSeconds() != 0 || timeFrom.getNanos() != 0)) {
            requestBuilder.setTimeFrom(timeFrom);
        }

        if (timeTo != null && (timeTo.getSeconds() != 0 || timeTo.getNanos() != 0)) {
            requestBuilder.setTimeTo(timeTo);
        }
        if (trainType != null && !trainType.equalsIgnoreCase("Tutti")) {
            requestBuilder.setTrainType(trainType);
        }

        TrainResponse response;
        try {
            response = blockingStub.searchTrains(requestBuilder.build());
            logger.info("Trovati " + response.getTrainsCount() + " treni");
            return response.getTrainsList();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "Errore RPC: {0}", e.getStatus());
            throw e;
        }
    }

    /**
     * Ottiene i dettagli di un treno specifico per data e ora.
     *
     * @param trainId ID del treno
     * @param dateTime Data e ora del viaggio (LocalDateTime)
     * @return Dettagli del treno
     */
    public TrainDetailsResponse getTrainDetails(int trainId, LocalDateTime dateTime) {
        logger.info("Richiesta dettagli del treno (ID: " + trainId + ", data/ora: " + dateTime + ")");
        Timestamp dateTimestamp = null;
        if (dateTime != null) {
            java.time.Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
            dateTimestamp = Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();
        }
        TrainDetailsRequest.Builder requestBuilder = TrainDetailsRequest.newBuilder()
                .setTrainId(trainId);
        if (dateTimestamp != null) {
            requestBuilder.setDate(dateTimestamp);
        }
        TrainDetailsResponse response;
        try {
            response = blockingStub.getTrainDetails(requestBuilder.build());
            logger.info("Ricevuti dettagli del treno");
            return response;
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "Errore RPC: {0}", e.getStatus());
            throw e;
        }
    }

    /**
     * Cerca stazioni in base a un criterio di ricerca.
     *
     * @param request Richiesta di ricerca stazioni
     * @return Risposta con le stazioni trovate
     */
    public SearchStationResponse searchStations(@NotNull SearchStationRequest request) {
        logger.info("Ricerca stazioni con query: " + request.getQuery());
        try {
            SearchStationResponse response = blockingStub.searchStations(request);
            logger.info("Trovate " + response.getStationsCount() + " stazioni");
            return response;
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "Errore RPC: {0}", e.getStatus());
            throw e;
        }
    }

    /**
     * Metodo main per test.
     */
    public static void main(String[] args) throws Exception {
        TrainClient client = new TrainClient("localhost", 9090);
        try {
            // Esempio di utilizzo
            client.getTrains(0);
        } finally {
            client.shutdown();
        }
    }
}
