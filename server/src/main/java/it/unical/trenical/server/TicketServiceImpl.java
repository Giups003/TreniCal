package it.unical.trenical.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.ticket.*;
import it.unical.trenical.grpc.ticket.PurchaseTicketRequest;

import java.util.UUID;

/**
 * Implementazione del servizio gRPC per la gestione dei biglietti.
 * Fornisce funzionalità per l'acquisto e la modifica dei biglietti.
 */
public class TicketServiceImpl extends TicketServiceGrpc.TicketServiceImplBase {

    // Calcolatore di prezzi
    private final PriceCalculator priceCalculator;
    // DataStore per la persistenza reale
    private final DataStore dataStore;

    /**
     * Costruttore che inizializza il servizio con un calcolatore di prezzi.
     */
    public TicketServiceImpl() {
        this.priceCalculator = new PriceCalculator();
        this.dataStore = DataStore.getInstance();
    }

    /**
     * Costruttore che accetta un calcolatore di prezzi esterno (utile per i test).
     *
     * @param priceCalculator Calcolatore di prezzi da utilizzare
     */
    public TicketServiceImpl(PriceCalculator priceCalculator) {
        this.priceCalculator = priceCalculator;
        this.dataStore = DataStore.getInstance();
    }

    /**
     * Gestisce la richiesta di acquisto di un biglietto.
     * 
     * @param request Richiesta contenente i dettagli del biglietto da acquistare.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void purchaseTicket(PurchaseTicketRequest request, StreamObserver<PurchaseTicketResponse> responseObserver) {
        try {
            // Valida la richiesta
            if (!isValidPurchaseRequest(request)) {
                PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Richiesta non valida. Assicurati di fornire tutti i campi obbligatori.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Gestione posti disponibili
            DataStore dataStore = this.dataStore;
            int trainId = request.getTrainId();
            int seatsRequested = request.getSeats();
            if (seatsRequested <= 0) seatsRequested = 1;
            boolean seatsOk = dataStore.decrementSeat(trainId, seatsRequested);
            if (!seatsOk) {
                PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Posti esauriti per questo treno o richiesta superiore ai posti disponibili.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Genera un ID univoco per il biglietto
            String ticketId = UUID.randomUUID().toString();

            // Calcola il prezzo del biglietto usando il calcolatore di prezzi
            double price = priceCalculator.calculateTicketPrice(
                    request.getDepartureStation(),
                    request.getArrivalStation(),
                    request.getServiceClass(),
                    request.getTravelDate(),
                    request.getPromoCode()
            );

            // Crea il biglietto
            Ticket ticket = Ticket.newBuilder()
                    .setId(ticketId)
                    .setTrainId(request.getTrainId())
                    .setPassengerName(request.getPassengerName())
                    .setDepartureStation(request.getDepartureStation())
                    .setArrivalStation(request.getArrivalStation())
                    .setTravelDate(request.getTravelDate())
                    .setServiceClass(request.getServiceClass())
                    .setPrice(price)
                    .setSeat(String.valueOf(seatsRequested))
                    .build();

            // Salva il biglietto reale nel DataStore
            dataStore.addTicket(ticket);

            // Prepara e invia la risposta al client
            PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                    .setSuccess(true)
                    .setTicketId(ticketId)
                    .setMessage("Biglietto acquistato con successo!")
                    .setPrice(price)
                    .setTicket(ticket)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Gestisce eventuali errori imprevisti
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore interno: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Gestisce la richiesta di modifica di un biglietto.
     * 
     * @param request Richiesta contenente i dettagli della modifica.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void modifyTicket(ModifyTicketRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            // Valida la richiesta
            if (!isValidField(request.getTicketId())) {
                sendOperationResponse(false, "ID biglietto non valido!", responseObserver);
                return;
            }

            // Recupera il biglietto reale dal DataStore
            Ticket ticket = dataStore.getTicketById(request.getTicketId());
            if (ticket == null) {
                sendOperationResponse(false, "Biglietto non trovato!", responseObserver);
                return;
            }

            boolean modified = false;
            Ticket.Builder updatedTicket = ticket.toBuilder();
            String newDeparture = isValidField(request.getNewDepartureStation()) ? request.getNewDepartureStation() : ticket.getDepartureStation();
            String newArrival = isValidField(request.getNewArrivalStation()) ? request.getNewArrivalStation() : ticket.getArrivalStation();
            String newServiceClass = isValidField(request.getNewServiceClass()) ? request.getNewServiceClass() : ticket.getServiceClass();
            com.google.protobuf.Timestamp newDate = isValidField(request.getNewDate()) ? request.getNewDate() : ticket.getTravelDate();

            // Applica le modifiche solo a data, orario, classe di servizio
            if (isValidField(request.getNewDepartureStation())) {
                updatedTicket.setDepartureStation(request.getNewDepartureStation());
                modified = true;
            }
            if (isValidField(request.getNewArrivalStation())) {
                updatedTicket.setArrivalStation(request.getNewArrivalStation());
                modified = true;
            }
            if (isValidField(request.getNewServiceClass())) {
                updatedTicket.setServiceClass(request.getNewServiceClass());
                modified = true;
            }
            if (isValidField(request.getNewDate())) {
                updatedTicket.setTravelDate(request.getNewDate());
                modified = true;
            }

            // Ricalcola il prezzo e applica eventuale penale/differenza
            if (modified) {
                double oldPrice = ticket.getPrice();
                double newPrice = priceCalculator.calculateTicketPrice(
                        newDeparture,
                        newArrival,
                        newServiceClass,
                        newDate,
                        ""
                );
                double finalPrice = newPrice;
                String msg = "Biglietto modificato con successo!";
                // Esempio: applica penale fissa di 5 euro se la classe cambia
                if (!ticket.getServiceClass().equals(newServiceClass)) {
                    finalPrice += 5.0;
                    msg += " (penale cambio classe: 5 euro)";
                } else if (newPrice > oldPrice) {
                    double diff = newPrice - oldPrice;
                    finalPrice = newPrice;
                    msg += String.format(" (differenza tariffaria: %.2f euro)", diff);
                }
                updatedTicket.setPrice(finalPrice);
                dataStore.deleteTicket(ticket.getId());
                dataStore.addTicket(updatedTicket.build());
                sendOperationResponse(true, msg, responseObserver);
            } else {
                sendOperationResponse(false, "Nessuna modifica effettuata!", responseObserver);
            }
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore durante la modifica del biglietto: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    /**
     * Metodo di supporto per inviare una risposta generica di operazione.
     *
     * @param success Stato di successo dell'operazione.
     * @param message Messaggio da inviare al client.
     * @param responseObserver Stream per inviare la risposta.
     */
    private void sendOperationResponse(boolean success, String message, StreamObserver<OperationResponse> responseObserver) {
        OperationResponse response = OperationResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Verifica se un campo è valido (non nullo e non vuoto).
     *
     * @param field Campo da verificare
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
     * Verifica se una richiesta di acquisto biglietto è valida.
     *
     * @param request Richiesta da validare
     * @return true se la richiesta è valida, false altrimenti
     */
    private boolean isValidPurchaseRequest(PurchaseTicketRequest request) {
        return request.getTrainId() != 0 &&
               isValidField(request.getPassengerName()) &&
               isValidField(request.getDepartureStation()) &&
               isValidField(request.getArrivalStation()) &&
               isValidField(request.getTravelDate()) &&
               isValidField(request.getServiceClass());
    }
}
