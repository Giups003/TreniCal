package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.ticket.*;
import it.unical.trenical.grpc.ticket.PurchaseTicketRequest;
import it.unical.trenical.server.strategy.PriceCalculator;

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
 * PATTERN STRATEGY: Utilizza PriceCalculator che implementa il pattern Strategy
 * per il calcolo dinamico dei prezzi con diverse strategie intercambiabili.
 */
public class TicketServiceImpl extends TicketServiceGrpc.TicketServiceImplBase {

    // PATTERN STRATEGY: Calcolatore di prezzi con strategia intercambiabile
    private final PriceCalculator priceCalculator;
    // DataStore per la persistenza dei dati
    private final DataStore dataStore;

    /**
     * Costruttore che inizializza il servizio con strategia di calcolo prezzi standard.
     * PATTERN STRATEGY: Inizializza con strategia predefinita.
     */
    public TicketServiceImpl() {
        this.priceCalculator = new PriceCalculator(); // Strategia standard di default
        this.dataStore = DataStore.getInstance();

        System.out.println("[STRATEGY PATTERN] TicketServiceImpl inizializzato con strategia: " +
                         priceCalculator.getCurrentStrategyName());
    }

    /**
     * Costruttore che accetta un calcolatore di prezzi esterno (utile per i test).
     * PATTERN STRATEGY: Permette iniezione di dipendenza con strategia specifica.
     *
     * @param priceCalculator Calcolatore di prezzi da utilizzare
     */
    public TicketServiceImpl(PriceCalculator priceCalculator) {
        this.priceCalculator = priceCalculator;
        this.dataStore = DataStore.getInstance();

        System.out.println("[STRATEGY PATTERN] TicketServiceImpl inizializzato con strategia custom: " +
                         priceCalculator.getCurrentStrategyName());
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

            // PATTERN STRATEGY: Determina il tipo di cliente e imposta la strategia appropriata
            String username = request.getPassengerName();
            String customerType = dataStore.getCustomerType(username);

            System.out.println("[STRATEGY PATTERN] Cliente: " + username +
                              ", Tipo: " + customerType);

            // PATTERN STRATEGY: Imposta la strategia appropriata per il tipo di cliente
            priceCalculator.setStrategyByType(customerType);

            // Se la richiesta non contiene un metodo di pagamento o è esplicitamente marcata come solo prezzo,
            // viene trattata come simulazione per calcolo prezzo
            if (request.getPaymentMethod().isEmpty() ||
                "SOLO_PREZZO".equals(request.getPaymentMethod())) {

                // CORREZIONE: Valida il codice promo anche per le simulazioni di prezzo
                String promoCode = request.getPromoCode();
                if (!promoCode.trim().isEmpty()) {
                    if (!priceCalculator.isValidPromoCode(promoCode, customerType)) {
                        PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                                .setSuccess(false)
                                .setMessage("Codice promozionale '" + promoCode + "' non valido o non applicabile per il tuo tipo di account (" + customerType + ").")
                                .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                        return;
                    }
                }

                // PATTERN STRATEGY: Delega il calcolo del prezzo alla strategia corretta per tipo utente
                double price = priceCalculator.calculateTicketPrice(
                        request.getDepartureStation(),
                        request.getArrivalStation(),
                        request.getServiceClass(),
                        request.getTravelDate(),
                        !request.getPromoCode().trim().isEmpty() ? request.getPromoCode().toUpperCase() : "",
                        request.getTrainType(),
                        customerType
                );

                PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                        .setSuccess(true)
                        .setPrice(price)
                        .setMessage("Prezzo calcolato per cliente " + customerType + ": " + priceCalculator.getCurrentStrategyName())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // PATTERN STRATEGY: Validazione codice promo tramite strategia appropriata per tipo utente
            String promoCode = request.getPromoCode();
            if (!promoCode.trim().isEmpty()) {
                if (!priceCalculator.isValidPromoCode(promoCode, customerType)) {
                    PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Codice promozionale '" + promoCode + "' non valido o non applicabile per il tuo tipo di account (" + customerType + ").")
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return;
                }
            }

            // Gestione posti disponibili
            int trainId = request.getTrainId();
            int seatsRequested = request.getSeats();
            if (seatsRequested <= 0) seatsRequested = 1;

            LocalDateTime travelDateTime = null;
            if (request.hasTravelDate()) {
                Timestamp ts = request.getTravelDate();
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

            // PATTERN STRATEGY: Calcola il prezzo finale con la strategia corretta per tipo utente
            double finalPrice = priceCalculator.calculateTicketPrice(
                    request.getDepartureStation(),
                    request.getArrivalStation(),
                    request.getServiceClass(),
                    request.getTravelDate(),
                    !promoCode.trim().isEmpty() ? promoCode.toUpperCase() : "",
                    request.getTrainType(),
                    customerType
            );

            // Crea e salva un biglietto per ogni posto richiesto
            List<Ticket> createdTickets = new ArrayList<>();
            Set<Integer> occupiedSeats = new HashSet<>();

            // Trova tutti i posti già occupati per il treno
            for (Ticket t : dataStore.getAllTickets()) {
                if (t.getTrainId() == trainId) {
                    try {
                        occupiedSeats.add(Integer.parseInt(t.getSeat()));
                    } catch (Exception ignored) {}
                }
            }

            int seatNumber = 1;
            for (int i = 0; i < seatsRequested; i++) {
                // Trova il primo posto libero
                while (occupiedSeats.contains(seatNumber)) {
                    seatNumber++;
                }

                String ticketId = UUID.randomUUID().toString();

                // Pattern BUILDER: utilizzo di Ticket.newBuilder() per costruire oggetti complessi
                Ticket ticket = Ticket.newBuilder()
                        .setId(ticketId)
                        .setTrainId(request.getTrainId())
                        .setPassengerName(request.getPassengerName())
                        .setDepartureStation(request.getDepartureStation())
                        .setArrivalStation(request.getArrivalStation())
                        .setTravelDate(request.getTravelDate())
                        .setServiceClass(request.getServiceClass())
                        .setPrice(finalPrice)
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

            // Prepara e invia la risposta al client
            Ticket firstTicket = createdTickets.get(0);
            String successMessage = String.format(
                "Biglietti acquistati con successo! Strategia applicata: %s. " +
                "Prezzo per biglietto: %.2f €. " +
                (!promoCode.trim().isEmpty() ?
                    "Codice promo '" + promoCode + "' applicato." : "Nessun codice promo applicato."),
                priceCalculator.getCurrentStrategyName(),
                finalPrice
            );

            PurchaseTicketResponse response = PurchaseTicketResponse.newBuilder()
                    .setSuccess(true)
                    .setTicketId(firstTicket.getId())
                    .setMessage(successMessage)
                    .setPrice(finalPrice)
                    .setTicket(firstTicket)
                    .addAllTickets(createdTickets)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            // Gestisce eventuali errori imprevisti
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore interno durante l'acquisto: " + e.getMessage())
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

            boolean classChanged = !ticket.getServiceClass().equals(newServiceClass);
            boolean dateChanged = !ticket.getTravelDate().equals(newDate) || !ticket.getTravelTime().equals(newTime);
            LocalDate today = LocalDate.now();
            LocalDate ticketDate = Instant.ofEpochSecond(ticket.getTravelDate().getSeconds()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate newDateLocal = Instant.ofEpochSecond(newDate.getSeconds()).atZone(ZoneId.systemDefault()).toLocalDate();

            double diff = newPrice - oldPrice;
            double finalPrice;

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
                finalPrice = oldPrice + penale + diff;
            }
            updatedTicket.setPrice(finalPrice);
            // Elimina solo una volta il biglietto originale
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
     * Calcola la penale in base ai giorni rimanenti prima della partenza:
     * - Più di 7 giorni: 0% di penale
     * - 4-7 giorni: 10% di penale
     * - 2-3 giorni: 25% di penale
     * - 1 giorno: 50% di penale
     * - Stesso giorno: 100% di penale (nessun rimborso)
     *
     * @param request Richiesta contenente l'ID del biglietto da annullare.
     * @param responseObserver Stream per inviare la risposta al client.
     */
    @Override
    public void cancelTicket(CancelTicketRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            String ticketId = request.getTicketId();
            System.out.println("[cancelTicket] Richiesta annullamento per biglietto ID: " + ticketId);

            if (ticketId.isEmpty()) {
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

            // Verifica se il biglietto è già annullato o scaduto
            if ("Annullato".equalsIgnoreCase(ticket.getStatus()) || "Scaduto".equalsIgnoreCase(ticket.getStatus())) {
                OperationResponse response = OperationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Il biglietto è già " + ticket.getStatus().toLowerCase() + "!")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Calcola i giorni rimanenti fino alla partenza
            LocalDate today = LocalDate.now();
            LocalDate travelDate = Instant.ofEpochSecond(ticket.getTravelDate().getSeconds())
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            long daysUntilTravel = java.time.temporal.ChronoUnit.DAYS.between(today, travelDate);

            // Se la data di viaggio è nel passato, non permettere l'annullamento
            if (daysUntilTravel < 0) {
                OperationResponse response = OperationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Non è possibile annullare un biglietto per una data passata!")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Calcola la penale in base ai giorni rimanenti
            double originalPrice = ticket.getPrice();
            double penaltyPercentage = calculateCancellationPenalty(daysUntilTravel);
            double penaltyAmount = originalPrice * (penaltyPercentage / 100.0);
            double refundAmount = originalPrice - penaltyAmount;

            // Aggiorna lo stato del biglietto a "Annullato"
            Ticket cancelledTicket = ticket.toBuilder()
                    .setStatus("Annullato")
                    .setPrice(refundAmount) // Imposta il prezzo come importo rimborsato
                    .build();

            dataStore.deleteTicket(ticketId);
            dataStore.addTicket(cancelledTicket);

            // Prepara il messaggio di risposta dettagliato
            String message;
            if (penaltyPercentage == 0) {
                message = String.format(
                    "Biglietto annullato con successo! Rimborso completo di %.2f € (nessuna penale applicata - annullamento con più di 7 giorni di anticipo).",
                    refundAmount
                );
            } else if (penaltyPercentage == 100) {
                message = String.format(
                    "Biglietto annullato. Nessun rimborso disponibile (annullamento nel giorno di partenza). Prezzo originale: %.2f €.",
                    originalPrice
                );
            } else {
                message = String.format(
                    "Biglietto annullato con successo! Prezzo originale: %.2f €, Penale (%.0f%%): %.2f €, Rimborso: %.2f € (annullamento con %d giorni di anticipo).",
                    originalPrice, penaltyPercentage, penaltyAmount, refundAmount, daysUntilTravel
                );
            }

            OperationResponse response = OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage(message)
                    .setOldPrice(originalPrice)
                    .setPenalty(penaltyAmount)
                    .setTotal(refundAmount)
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
     * Calcola la percentuale di penale per l'annullamento in base ai giorni rimanenti.
     *
     * @param daysUntilTravel Numero di giorni tra oggi e la data di viaggio
     * @return Percentuale di penale (0-100)
     */
    private double calculateCancellationPenalty(long daysUntilTravel) {
        if (daysUntilTravel > 7) {
            return 0.0;   // Nessuna penale se più di 7 giorni
        } else if (daysUntilTravel >= 4) {
            return 10.0;  // 10% di penale tra 4-7 giorni
        } else if (daysUntilTravel >= 2) {
            return 25.0;  // 25% di penale tra 2-3 giorni
        } else if (daysUntilTravel == 1) {
            return 50.0;  // 50% di penale se 1 giorno
        } else {
            return 100.0; // 100% di penale (nessun rimborso) se stesso giorno
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
                if (t.hasTravelDate() && ( (!t.getStatus().equalsIgnoreCase("Annullato") && !t.getStatus().equalsIgnoreCase("Scaduto")))) {
                    long travelEpoch = t.getTravelDate().getSeconds();
                    if (travelEpoch < now) {
                        // Aggiorna lo stato a "Scaduto"
                        Ticket updated = t.toBuilder().setStatus("Scaduto").build();
                        toUpdate.add(updated);
                        t = updated;
                    }
                }
                if (!passengerName.isEmpty()) {
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
            ListTicketsResponse response = ListTicketsResponse.newBuilder().addAllTickets(filteredTickets).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Errore durante la lista biglietti: " + e.getMessage())
                            .asRuntimeException()
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
     * PATTERN STRATEGY: Utilizza la strategia appropriata per il calcolo del prezzo.
     */
    @Override
    public void getTicketPrice(GetTicketPriceRequest request, StreamObserver<GetTicketPriceResponse> responseObserver) {
        try {
            // PATTERN STRATEGY: Usa il tipo utente dalla richiesta per selezionare la strategia corretta
            String userType = request.getUserType();
            if (userType == null || userType.isEmpty()) {
                userType = "standard"; // Default fallback
            }

            System.out.println("[STRATEGY PATTERN] Calcolo prezzo per tipo utente: " + userType);

            // PATTERN STRATEGY: Imposta la strategia appropriata per il tipo di utente
            priceCalculator.setStrategyByUserType(userType);

            // PATTERN STRATEGY: Delega il calcolo alla strategia corretta per tipo utente
            double price = priceCalculator.calculateTicketPrice(
                request.getDepartureStation(),
                request.getArrivalStation(),
                request.getServiceClass(),
                request.getTravelDate(),
                !request.getPromoCode().trim().isEmpty() ? request.getPromoCode().toUpperCase() : "",
                request.getTrainType(),
                userType // Passa il tipo utente
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
            if (ticketId.isEmpty()) {
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
                !request.getPassengerName().isEmpty() &&
                !request.getDepartureStation().isEmpty() &&
                !request.getArrivalStation().isEmpty() &&
                !request.getServiceClass().isEmpty() &&
                request.hasTravelDate();
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
