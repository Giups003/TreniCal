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
     * @param request          Richiesta contenente i dettagli del biglietto da acquistare.
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

            boolean isPriceCheck = request.getPromoCode() != null && !request.getPromoCode().isEmpty() && request.getSeats() == 1 && request.getPassengerName() != null && !request.getPassengerName().isEmpty();
            if (isPriceCheck) {
                double price = priceCalculator.calculateTicketPrice(
                        request.getDepartureStation(),
                        request.getArrivalStation(),
                        request.getServiceClass(),
                        request.getTravelDate(),
                        request.getPromoCode()
                );
                PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                        .setSuccess(true)
                        .setPrice(price)
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
     * @param request          Richiesta contenente i dettagli della modifica.
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
                // Penale variabile: 10% del prezzo originale se cambia classe, altrimenti 5% se cambia solo data/orario
                double penale = 0.0;
                if (!ticket.getServiceClass().equals(newServiceClass)) {
                    penale = oldPrice * 0.10; // 10% se cambia classe
                    msg += String.format(" (penale cambio classe: %.2f euro)", penale);
                } else if (!ticket.getTravelDate().equals(newDate)) {
                    penale = oldPrice * 0.05; // 5% se cambia solo data/orario
                    msg += String.format(" (penale cambio data/orario: %.2f euro)", penale);
                }
                if (newPrice > oldPrice) {
                    double diff = newPrice - oldPrice;
                    finalPrice = newPrice + penale;
                    msg += String.format(" (differenza tariffaria: %.2f euro)", diff);
                } else {
                    finalPrice = newPrice + penale;
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
     * Gestisce la richiesta di annullamento di un biglietto.
     * @param request Richiesta contenente l'ID del biglietto da annullare.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void cancelTicket(CancelTicketRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            String ticketId = request.getTicketId();
            System.out.println("[cancelTicket] Richiesta annullamento per biglietto ID: " + ticketId);
            if (ticketId == null || ticketId.isEmpty()) {
                OperationResponse response = OperationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("ID biglietto non valido!")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }
            Ticket ticket = dataStore.getTicketById(ticketId);
            if (ticket == null) {
                OperationResponse response = OperationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Biglietto non trovato!")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }
            dataStore.deleteTicket(ticketId);
            OperationResponse response = OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Biglietto annullato con successo.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore durante l'annullamento del biglietto: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    private boolean isValidPurchaseRequest(PurchaseTicketRequest request) {
        return request != null &&
                request.getTrainId() > 0 &&
                request.getPassengerName() != null && !request.getPassengerName().isEmpty() &&
                request.getDepartureStation() != null && !request.getDepartureStation().isEmpty() &&
                request.getArrivalStation() != null && !request.getArrivalStation().isEmpty() &&
                request.getServiceClass() != null && !request.getServiceClass().isEmpty() &&
                request.getTravelDate() != null;
    }

    private boolean isValidField(String s) {
        return s != null && !s.isEmpty();
    }
    private boolean isValidField(com.google.protobuf.Timestamp t) {
        return t != null && t.getSeconds() > 0;
    }

    private void sendOperationResponse(boolean success, String message, StreamObserver<OperationResponse> responseObserver) {
        OperationResponse response = OperationResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
