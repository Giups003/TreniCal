package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.ticket.*;
import it.unical.trenical.grpc.ticket.PurchaseTicketRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

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

            // Se la richiesta non contiene un metodo di pagamento o è esplicitamente marcata come solo prezzo,
            // viene trattata come simulazione per calcolo prezzo
            if (request.getPaymentMethod().isEmpty() ||
                "SOLO_PREZZO".equals(request.getPaymentMethod())) {

                double price = priceCalculator.calculateTicketPrice(
                        request.getDepartureStation(),
                        request.getArrivalStation(),
                        request.getServiceClass(),
                        request.getTravelDate(),
                        request.getPromoCode(),
                        request.getTrainType()
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
            // Passa la data/ora corretta a decrementSeat
            LocalDateTime travelDateTime = null;
            if (request.hasTravelDate()) {
                com.google.protobuf.Timestamp ts = request.getTravelDate();
                travelDateTime = Instant.ofEpochSecond(ts.getSeconds())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            boolean seatsOk = dataStore.checkAvailableSeats(trainId, travelDateTime, seatsRequested);
            if (!seatsOk) {
                PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Posti esauriti per questo treno o richiesta superiore ai posti disponibili.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Calcola il prezzo del biglietto una sola volta prima del ciclo
            double price = priceCalculator.calculateTicketPrice(
                    request.getDepartureStation(),
                    request.getArrivalStation(),
                    request.getServiceClass(),
                    request.getTravelDate(),
                    request.getPromoCode() != null ? request.getPromoCode().toUpperCase() : "",
                    request.getTrainType()
            );
            // Crea e salva un biglietto per ogni posto richiesto
            List<Ticket> createdTickets = new ArrayList<>();
            // Trova tutti i posti già occupati per il treno
            Set<Integer> occupiedSeats = new HashSet<>();
            for (Ticket t : dataStore.getAllTickets()) {
                if (t.getTrainId() == trainId) {
                    try { occupiedSeats.add(Integer.parseInt(t.getSeat())); } catch (Exception ignored) {}
                }
            }
            int seatNumber = 1;
            for (int i = 0; i < seatsRequested; i++) {
                // Trova il primo posto libero
                while (occupiedSeats.contains(seatNumber)) {
                    seatNumber++;
                }
                String ticketId = UUID.randomUUID().toString();
                Ticket ticket = Ticket.newBuilder()
                        .setId(ticketId)
                        .setTrainId(request.getTrainId())
                        .setPassengerName(request.getPassengerName())
                        .setDepartureStation(request.getDepartureStation())
                        .setArrivalStation(request.getArrivalStation())
                        .setTravelDate(request.getTravelDate())
                        .setServiceClass(request.getServiceClass())
                        .setPrice(price)
                        .setSeat(String.valueOf(seatNumber))
                        .setPurchaseDate(
                            Timestamp.newBuilder()
                                .setSeconds(Instant.now().getEpochSecond())
                                .build()
                        )
                        .build();
                dataStore.addTicket(ticket);
                createdTickets.add(ticket);
                occupiedSeats.add(seatNumber);
                seatNumber++;
            }

            // Prepara e invia la risposta al client (tutti i biglietti acquistati)
            Ticket firstTicket = createdTickets.get(0);
            PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                    .setSuccess(true)
                    .setTicketId(firstTicket.getId())
                    .setMessage("Biglietti acquistati con successo!")
                    .setPrice(price)
                    .setTicket(firstTicket)
                    .addAllTickets(createdTickets)
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

            Ticket.Builder updatedTicket = ticket.toBuilder();
            String newDeparture = isValidField(request.getNewDepartureStation()) ? request.getNewDepartureStation() : ticket.getDepartureStation();
            String newArrival = isValidField(request.getNewArrivalStation()) ? request.getNewArrivalStation() : ticket.getArrivalStation();
            String newServiceClass = isValidField(request.getNewServiceClass()) ? request.getNewServiceClass() : ticket.getServiceClass();
            Timestamp newDate = request.hasNewTravelDate() ? request.getNewTravelDate() : ticket.getTravelDate();
            Timestamp newTime = request.hasNewTravelTime() ? request.getNewTravelTime() : ticket.getTravelTime();

            // Determina il nuovo trainId (se non specificato, usa quello attuale)
            int newTrainId = request.getTrainId() != 0 ? request.getTrainId() : ticket.getTrainId();

            // Libera il vecchio posto (rimuovendo il biglietto)
            dataStore.deleteTicket(ticket.getId());
            // Trova tutti i posti occupati sul nuovo treno/data/orario
            Set<Integer> occupiedSeats = new HashSet<>();
            for (Ticket t : dataStore.getAllTickets()) {
                if (t.getTrainId() == newTrainId && t.hasTravelDate() && t.getTravelDate().equals(newDate) && t.hasTravelTime() && t.getTravelTime().equals(newTime)) {
                    try { occupiedSeats.add(Integer.parseInt(t.getSeat())); } catch (Exception ignored) {}
                }
            }
            // Assegna il primo posto libero
            int seatNumber = 1;
            while (occupiedSeats.contains(seatNumber)) {
                seatNumber++;
            }
            updatedTicket.setSeat(String.valueOf(seatNumber));
            // Aggiorna i campi modificati
            if (isValidField(request.getNewDepartureStation())) {
                updatedTicket.setDepartureStation(request.getNewDepartureStation());
            }
            if (isValidField(request.getNewArrivalStation())) {
                updatedTicket.setArrivalStation(request.getNewArrivalStation());
            }
            if (request.hasNewTravelDate()) {
                updatedTicket.setTravelDate(newDate);
            }
            if (request.hasNewTravelTime()) {
                updatedTicket.setTravelTime(newTime);
            }
            if (request.getTrainId() != 0 && request.getTrainId() != ticket.getTrainId()) {
                updatedTicket.setTrainId(request.getTrainId());
            }
            if (isValidField(request.getNewServiceClass())) {
                updatedTicket.setServiceClass(request.getNewServiceClass());
            }

            // Controlla se almeno un campo è effettivamente cambiato
            boolean changedDeparture = isValidField(request.getNewDepartureStation()) && !request.getNewDepartureStation().equals(ticket.getDepartureStation());
            boolean changedArrival = isValidField(request.getNewArrivalStation()) && !request.getNewArrivalStation().equals(ticket.getArrivalStation());
            boolean changedServiceClass = isValidField(request.getNewServiceClass()) && !request.getNewServiceClass().equals(ticket.getServiceClass());
            boolean changedDate = request.hasNewTravelDate() && !request.getNewTravelDate().equals(ticket.getTravelDate());
            boolean changedTime = request.hasNewTravelTime() && !request.getNewTravelTime().equals(ticket.getTravelTime());
            boolean changedTrain = request.getTrainId() != 0 && request.getTrainId() != ticket.getTrainId();
            boolean modified = changedDeparture || changedArrival || changedServiceClass || changedDate || changedTime || changedTrain;
            if (!modified) {
                sendOperationResponse(false, "Nessuna modifica effettuata!", responseObserver);
                return;
            }

            // Ricalcola il prezzo e applica eventuale penale/differenza
            double oldPrice = ticket.getPrice();
            double newPrice = priceCalculator.calculateTicketPrice(
                    newDeparture,
                    newArrival,
                    newServiceClass,
                    newDate,
                    "", // promoCode vuoto, nessun codice promo durante modifica
                    request.getTrainType()
            );
            double penale = 0.0;
            double diff = 0.0;
            double finalPrice = oldPrice;
            boolean classChanged = !ticket.getServiceClass().equals(newServiceClass);
            boolean dateChanged = !ticket.getTravelDate().equals(newDate) || !ticket.getTravelTime().equals(newTime);
            // Se la data/orario è oggi e nessun campo è cambiato, nessun sovrapprezzo
            LocalDate today = LocalDate.now();
            LocalDate ticketDate = Instant.ofEpochSecond(ticket.getTravelDate().getSeconds()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate newDateLocal = Instant.ofEpochSecond(newDate.getSeconds()).atZone(ZoneId.systemDefault()).toLocalDate();
            if (ticketDate.equals(today) && newDateLocal.equals(today) &&
                !classChanged && !dateChanged && !changedDeparture && !changedArrival && !changedTrain) {
                penale = 0.0;
                diff = 0.0;
                finalPrice = oldPrice;
            } else {
                if (classChanged) {
                    penale = oldPrice * 0.10; // 10% se cambia classe
                } else if (dateChanged) {
                    penale = oldPrice * 0.05; // 5% se cambia solo data/orario
                }
                if (newPrice > oldPrice) {
                    diff = newPrice - oldPrice;
                }
                finalPrice = oldPrice + penale + diff;
            }
            updatedTicket.setPrice(finalPrice);

            dataStore.deleteTicket(ticket.getId());
            dataStore.addTicket(updatedTicket.build());
            // Risposta dettagliata con breakdown prezzi
            String msg = String.format(
                "Biglietto modificato con successo! Prezzo precedente: %.2f €, Prezzo nuovo: %.2f €, Penale: %.2f €, Differenza tariffaria: %.2f €, Totale da pagare: %.2f €",
                oldPrice, newPrice, penale, diff, finalPrice
            );
            OperationResponse response = OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage(msg)
                    .setOldPrice(oldPrice)
                    .setNewPrice(newPrice)
                    .setPenalty(penale)
                    .setTariffDiff(diff)
                    .setTotal(finalPrice)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
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
            // Aggiorna lo stato del biglietto a "Annullato" invece di cancellarlo
            Ticket annullato = ticket.toBuilder().setStatus("Annullato").build();
            dataStore.deleteTicket(ticketId);
            dataStore.addTicket(annullato);
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

    /**
     * Gestisce la richiesta di elenco dei biglietti.
     * Restituisce solo i biglietti dell'utente richiesto o tutti se non è specificato un passeggero.
     * @param request Richiesta contenente i criteri di ricerca (es. nome passeggero).
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void listTickets(ListTicketsRequest request, StreamObserver<ListTicketsResponse> responseObserver) {
        try {
            String passengerName = request.getPassengerName();
            List<Ticket> allTickets = dataStore.getAllTickets();
            List<Ticket> filteredTickets = new ArrayList<>();
            List<Ticket> toUpdate = new ArrayList<>();
            long now = System.currentTimeMillis() / 1000L;
            for (Ticket t : allTickets) {
                if (t.hasTravelDate() && (t.getStatus() == null || (!t.getStatus().equalsIgnoreCase("Annullato") && !t.getStatus().equalsIgnoreCase("Scaduto")))) {
                    long travelEpoch = t.getTravelDate().getSeconds();
                    if (travelEpoch < now) {
                        // Aggiorna lo stato a "Scaduto"
                        Ticket updated = t.toBuilder().setStatus("Scaduto").build();
                        toUpdate.add(updated);
                        t = updated;
                    }
                }
                if (passengerName != null && !passengerName.isEmpty()) {
                    if (t.getPassengerName().equals(passengerName)) {
                        filteredTickets.add(t);
                    }
                } else {
                    filteredTickets.add(t);
                }
            }
            // Aggiorna i biglietti scaduti nel DataStore
            for (Ticket t : toUpdate) {
                dataStore.deleteTicket(t.getId());
                dataStore.addTicket(t);
            }
            ListTicketsResponse response = ListTicketsResponse.newBuilder()
                    .addAllTickets(filteredTickets)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Errore durante il recupero dei biglietti: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void clearAllTickets(ClearAllTicketsRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            dataStore.clearAllTickets();
            OperationResponse response = OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Tutti i biglietti sono stati eliminati.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            OperationResponse response = OperationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Errore durante l'eliminazione dei biglietti: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * Gestisce la richiesta del prezzo di un biglietto senza acquistarlo.
     *
     * @param request          Richiesta contenente i dettagli del biglietto.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void getTicketPrice(GetTicketPriceRequest request, StreamObserver<GetTicketPriceResponse> responseObserver) {
        try {
            double price = priceCalculator.calculateTicketPrice(
                request.getDepartureStation(),
                request.getArrivalStation(),
                request.getServiceClass(),
                request.getTravelDate(),
                request.getPromoCode() != null ? request.getPromoCode().toUpperCase() : "",
                request.getTrainType()
            );
            GetTicketPriceResponse response = GetTicketPriceResponse.newBuilder()
                .setPrice(price)
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Errore durante il calcolo del prezzo: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * Gestisce la richiesta di recupero di un biglietto per ID.
     *
     * @param request          Richiesta contenente l'ID del biglietto da recuperare.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void getTicket(GetTicketRequest request, StreamObserver<GetTicketResponse> responseObserver) {
        try {
            String ticketId = request.getTicketId();
            if (ticketId == null || ticketId.isEmpty()) {
                responseObserver.onNext(GetTicketResponse.newBuilder().build());
                responseObserver.onCompleted();
                return;
            }
            Ticket ticket = dataStore.getTicketById(ticketId);
            if (ticket == null) {
                responseObserver.onNext(GetTicketResponse.newBuilder().build());
                responseObserver.onCompleted();
                return;
            }
            GetTicketResponse response = GetTicketResponse.newBuilder()
                    .setTicket(ticket)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Errore durante il recupero del biglietto: " + e.getMessage()).asRuntimeException()
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


    private void sendOperationResponse(boolean success, String message, StreamObserver<OperationResponse> responseObserver) {
        OperationResponse response = OperationResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

