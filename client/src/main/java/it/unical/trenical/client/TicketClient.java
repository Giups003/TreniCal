package it.unical.trenical.client;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import it.unical.trenical.grpc.ticket.*;

import java.sql.Time;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client per interagire con il servizio gRPC dei biglietti.
 * Fornisce metodi per l'acquisto e la modifica dei biglietti.
 */
public class TicketClient {
    private static final Logger logger = Logger.getLogger(TicketClient.class.getName());

    private final ManagedChannel channel;
    private final TicketServiceGrpc.TicketServiceBlockingStub blockingStub;

    /**
     * Costruttore che inizializza la connessione al server gRPC.
     * 
     * @param host Indirizzo del server
     * @param port Porta del server
     */
    public TicketClient(String host, int port) {
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
    public TicketClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = TicketServiceGrpc.newBlockingStub(channel);
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
     * Acquista un biglietto con i parametri specificati.
     * 
     * @param trainId ID del treno
     * @param passengerName Nome del passeggero
     * @param departureStation Stazione di partenza
     * @param arrivalStation Stazione di arrivo
     * @param travelDate Data di viaggio (formato YYYY-MM-DD)
     * @param serviceClass Classe di servizio (es. "Prima Classe", "Economy")
     * @param promoCode Codice promozionale (opzionale)
     * @return Risposta contenente i dettagli del biglietto acquistato
     * @throws StatusRuntimeException Se si verifica un errore durante la chiamata RPC
     */
    public PurchaseTicketResponse purchaseTicket(
            Integer trainId,
            String passengerName,
            String departureStation,
            String arrivalStation,
            Timestamp travelDate,
            String serviceClass,
            String promoCode) {

        logger.info("Acquisto biglietto per " + passengerName + " da " + departureStation + 
                " a " + arrivalStation + " il " + travelDate);

        // Crea una richiesta di acquisto biglietto
        PurchaseTicketRequest.Builder requestBuilder = PurchaseTicketRequest.newBuilder()
                .setTrainId(trainId)
                .setPassengerName(passengerName)
                .setDepartureStation(departureStation)
                .setArrivalStation(arrivalStation)
                .setTravelDate(travelDate)
                .setServiceClass(serviceClass);

        // Aggiunge il codice promozionale se specificato
        if (promoCode != null && !promoCode.isEmpty()) {
            requestBuilder.setPromoCode(promoCode);
        }

        try {
            // Invia la richiesta al server
            PurchaseTicketResponse response = blockingStub.purchaseTicket(requestBuilder.build());

            // Logga il risultato
            if (response.getSuccess()) {
                logger.info("Biglietto acquistato con successo. ID: " + response.getTicketId() + 
                        ", Prezzo: " + response.getPrice());
            } else {
                logger.warning("Acquisto biglietto fallito: " + response.getMessage());
            }

            return response;
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "Errore RPC durante l'acquisto del biglietto: {0}", e.getStatus());
            throw e;
        }
    }

    /**
     * Versione semplificata del metodo purchaseTicket che usa valori predefiniti.
     * Utile per test rapidi.
     * 
     * @return Risposta contenente i dettagli del biglietto acquistato
     * @throws StatusRuntimeException Se si verifica un errore durante la chiamata RPC
     */
    public PurchaseTicketResponse purchaseTicket() {
        return purchaseTicket(
                1234,
                "Mario Rossi",
                "Roma",
                "Milano",
                Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build(),
                "Economy",
                "PROMO10"
        );
    }

    /**
     * Modifica un biglietto esistente.
     * 
     * @param ticketId ID del biglietto da modificare
     * @param newDate Nuova data di viaggio (opzionale)
     * @param newTime Nuovo orario (opzionale)
     * @param newServiceClass Nuova classe di servizio (opzionale)
     * @return Risposta contenente l'esito dell'operazione
     * @throws StatusRuntimeException Se si verifica un errore durante la chiamata RPC
     */
    public OperationResponse modifyTicket(
            String ticketId,
            Timestamp newDate,
            Timestamp newTime,
            String newServiceClass) {

        logger.info("Modifica biglietto con ID: " + ticketId);

        // Crea una richiesta di modifica biglietto
        ModifyTicketRequest.Builder requestBuilder = ModifyTicketRequest.newBuilder()
                .setTicketId(ticketId);

        // Aggiunge i campi opzionali se specificati
        if (newDate != null && !newDate.equals(Timestamp.getDefaultInstance())) {
            requestBuilder.setNewTravelDate(newDate);
        }
        if (newTime != null && !newTime.equals(Timestamp.getDefaultInstance())) {
            requestBuilder.setNewTravelTime(newTime);
        }
        if (newServiceClass != null && !newServiceClass.isEmpty()) {
            requestBuilder.setNewServiceClass(newServiceClass);
        }

        try {
            // Invia la richiesta al server
            OperationResponse response = blockingStub.modifyTicket(requestBuilder.build());

            // Logga il risultato
            if (response.getSuccess()) {
                logger.info("Biglietto modificato con successo: " + response.getMessage());
            } else {
                logger.warning("Modifica biglietto fallita: " + response.getMessage());
            }

            return response;
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "Errore RPC durante la modifica del biglietto: {0}", e.getStatus());
            throw e;
        }
    }

    /**
     * Metodo main per test.
     */
    public static void main(String[] args) {
        TicketClient client = new TicketClient("localhost", 9090);
        try {
            // Esempio di acquisto biglietto
            PurchaseTicketResponse purchaseResponse = client.purchaseTicket();
            System.out.println("Risposta del server: " + purchaseResponse.getMessage());
            System.out.println("Prezzo: " + purchaseResponse.getPrice());
            System.out.println("ID Biglietto: " + purchaseResponse.getTicketId());

            // Esempio di modifica biglietto
            if (purchaseResponse.getSuccess()) {
                OperationResponse modifyResponse = client.modifyTicket(
                        purchaseResponse.getTicketId(),
                        Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build(),
                        null,
                        "Prima Classe"
                );
                System.out.println("Modifica biglietto: " + modifyResponse.getMessage());
            }
        } finally {
            try {
                client.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interruzione durante la chiusura del client: " + e.getMessage());
            }
        }
    }
}
