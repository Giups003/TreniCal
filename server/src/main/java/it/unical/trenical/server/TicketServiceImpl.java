package it.unical.trenical.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.ticket.*;
import it.unical.trenical.grpc.ticket.PurchaseTicketRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementazione del servizio gRPC per la gestione dei biglietti.
 * Fornisce funzionalità per l'acquisto e la modifica dei biglietti.
 */
public class TicketServiceImpl extends TicketServiceGrpc.TicketServiceImplBase {

    // Mock database per i biglietti
    private final Map<String, Ticket> ticketDatabase = new HashMap<>();

    // Calcolatore di prezzi
    private final PriceCalculator priceCalculator;

    /**
     * Costruttore che inizializza il servizio con un calcolatore di prezzi.
     */
    public TicketServiceImpl() {
        this.priceCalculator = new PriceCalculator();
    }

    /**
     * Costruttore che accetta un calcolatore di prezzi esterno (utile per i test).
     * 
     * @param priceCalculator Calcolatore di prezzi da utilizzare
     */
    public TicketServiceImpl(PriceCalculator priceCalculator) {
        this.priceCalculator = priceCalculator;
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

            // Crea e salva il biglietto nel database mock
            Ticket ticket = Ticket.newBuilder()
                    .setId(ticketId)
                    .setTrainId(request.getTrainId())
                    .setPassengerName(request.getPassengerName())
                    .setDepartureStation(request.getDepartureStation())
                    .setArrivalStation(request.getArrivalStation())
                    .setTravelDate(request.getTravelDate())
                    .setServiceClass(request.getServiceClass())
                    .setPrice(price)
                    .build();

            ticketDatabase.put(ticketId, ticket);

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
                            .withDescription("Errore durante l'acquisto del biglietto: " + e.getMessage())
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

            // Cerca il biglietto nel database
            Ticket ticket = ticketDatabase.get(request.getTicketId());

            // Verifica se il biglietto esiste
            if (ticket == null) {
                sendOperationResponse(false, "Biglietto non trovato!", responseObserver);
                return;
            }

            // Verifica se ci sono campi da modificare
            if (!isValidField(request.getNewDate()) &&
                !isValidField(request.getNewTime()) && 
                !isValidField(request.getNewServiceClass())) {
                sendOperationResponse(false, "Nessun campo da modificare specificato!", responseObserver);
                return;
            }

            // Modifica i dettagli del biglietto solo se forniti nella richiesta
            Ticket.Builder updatedTicket = ticket.toBuilder();
            boolean modified = false;

            if (isValidField(request.getNewDate())) {
                updatedTicket.setTravelDate(request.getNewDate());
                modified = true;
            }

            if (isValidField(request.getNewServiceClass())) {
                updatedTicket.setServiceClass(request.getNewServiceClass());

                // Ricalcola il prezzo se la classe di servizio è cambiata
                double newPrice = priceCalculator.calculateTicketPrice(
                        ticket.getDepartureStation(),
                        ticket.getArrivalStation(),
                        request.getNewServiceClass(),
                        ticket.getTravelDate(),
                        "" // Nessun codice promo per le modifiche
                );
                updatedTicket.setPrice(newPrice);
                modified = true;
            }

            if (!modified) {
                sendOperationResponse(false, "Nessuna modifica effettuata!", responseObserver);
                return;
            }

            // Aggiorna il database con il biglietto modificato
            ticketDatabase.put(ticket.getId(), updatedTicket.build());

            // Prepara e invia la risposta al client
            sendOperationResponse(true, "Biglietto modificato con successo!", responseObserver);
        } catch (Exception e) {
            // Gestisce eventuali errori imprevisti
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
     * Verifica se un campo Timestamp è valido (non nullo e non zero).
     *
     * @param timestamp Campo Timestamp da verificare
     * @return true se il campo Timestamp è valido, false altrimenti
     */
    private boolean isValidTimestamp(com.google.protobuf.Timestamp timestamp) {
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

    /**
     * Ottiene il database dei biglietti (utile per i test).
     * 
     * @return Map contenente i biglietti
     */
    public Map<String, Ticket> getTicketDatabase() {
        return ticketDatabase;
    }
}
