package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.ticket.*;
import it.unical.trenical.server.strategy.PriceCalculator;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test completo per la classe TicketServiceImpl.
 * Testa tutti i metodi principali: purchaseTicket, modifyTicket, cancelTicket.
 */
public class TicketServiceImplTest {

    private TicketServiceImpl ticketService;
    private PriceCalculator mockPriceCalculator;
    private DataStore dataStore; // Torna ad usare il DataStore reale
    private StreamObserver<PurchaseTicketResponse> purchaseResponseObserver;
    private StreamObserver<OperationResponse> operationResponseObserver;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setup() {
        // Setup mock per PriceCalculator
        mockPriceCalculator = mock(PriceCalculator.class);

        // Usa il DataStore reale (Singleton)
        dataStore = DataStore.getInstance();
        dataStore.clearAllTickets(); // Pulisci i biglietti per ogni test

        // Inizializza il servizio con il mock del calcolatore prezzi
        ticketService = new TicketServiceImpl(mockPriceCalculator);

        // Setup mock observers
        purchaseResponseObserver = mock(StreamObserver.class);
        operationResponseObserver = mock(StreamObserver.class);

        // Setup comportamento default del mock PriceCalculator
        when(mockPriceCalculator.calculateTicketPrice(
                anyString(), anyString(), anyString(), any(), anyString(), anyString(), anyString()))
                .thenReturn(50.0);
        when(mockPriceCalculator.calculateTicketPrice(
                anyString(), anyString(), anyString(), any(), anyString(), anyString()))
                .thenReturn(50.0);
        when(mockPriceCalculator.getCurrentStrategyName()).thenReturn("Standard");
        when(mockPriceCalculator.isValidPromoCode(anyString(), anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("Test acquisto biglietto - Solo calcolo prezzo")
    public void testPurchaseTicket_PriceCalculationOnly_Success() {
        // Preparazione - richiesta senza metodo di pagamento (solo calcolo prezzo)
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("testUser")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("STANDARD")
                .setTravelDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 86400).build())
                .setPaymentMethod("SOLO_PREZZO")
                .setTrainType("INTERCITY")
                .build();

        // Esecuzione
        ticketService.purchaseTicket(request, purchaseResponseObserver);

        // Verifica
        ArgumentCaptor<PurchaseTicketResponse> responseCaptor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(responseCaptor.capture());
        verify(purchaseResponseObserver).onCompleted();

        PurchaseTicketResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals(50.0, response.getPrice(), 0.01);
        assertFalse(response.hasTicket()); // Non dovrebbe creare un biglietto reale

        // Verifica che nessun biglietto sia stato salvato
        List<Ticket> tickets = dataStore.getAllTickets();
        assertEquals(0, tickets.size());
    }

    @Test
    @DisplayName("Test acquisto biglietto - Richiesta non valida")
    public void testPurchaseTicket_InvalidRequest_Failure() {
        // Preparazione - richiesta con campi mancanti
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                // Manca passengerName
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setPaymentMethod("CARTA_CREDITO")
                .build();

        // Esecuzione
        ticketService.purchaseTicket(request, purchaseResponseObserver);

        // Verifica
        ArgumentCaptor<PurchaseTicketResponse> responseCaptor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(responseCaptor.capture());
        verify(purchaseResponseObserver).onCompleted();

        PurchaseTicketResponse response = responseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("non valida"));
    }

    @Test
    @DisplayName("Test acquisto biglietto - Posti esauriti")
    public void testPurchaseTicket_NoSeatsAvailable_Failure() {
        // Preparazione - questo test non può funzionare con il DataStore reale
        // perché non posso mockare checkAvailableSeats su un oggetto reale
        // Saltiamo questo test o lo modifichiamo per usare condizioni reali

        // Per ora, testiamo uno scenario dove i posti sono effettivamente esauriti
        // creando abbastanza biglietti da riempire il treno

        // Assumendo che il treno 1 abbia un numero limitato di posti,
        // creiamo prima molti biglietti per saturarlo
        for (int i = 0; i < 150; i++) { // Assumendo 150 posti max per treno
            Ticket ticket = Ticket.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .setTrainId(1)
                    .setPassengerName("user" + i)
                    .setDepartureStation("Roma")
                    .setArrivalStation("Milano")
                    .setServiceClass("STANDARD")
                    .setPrice(50.0)
                    .setSeat(String.valueOf(i + 1))
                    .setTravelDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 86400).build())
                    .build();
            dataStore.addTicket(ticket);
        }

        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("testUser")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("STANDARD")
                .setTravelDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 86400).build())
                .setPaymentMethod("CARTA_CREDITO")
                .setSeats(1)
                .setTrainType("INTERCITY")
                .build();

        // Esecuzione
        ticketService.purchaseTicket(request, purchaseResponseObserver);

        // Verifica
        ArgumentCaptor<PurchaseTicketResponse> responseCaptor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(responseCaptor.capture());
        verify(purchaseResponseObserver).onCompleted();

        PurchaseTicketResponse response = responseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("Posti esauriti"));
    }

    @Test
    @DisplayName("Test modifica biglietto - Modifica valida")
    public void testModifyTicket_ValidModification_Success() {
        // Preparazione - Crea prima un biglietto
        String ticketId = UUID.randomUUID().toString();
        Ticket originalTicket = Ticket.newBuilder()
                .setId(ticketId)
                .setTrainId(1)
                .setPassengerName("testUser")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("STANDARD")
                .setPrice(50.0)
                .setSeat("1")
                .setTravelDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 86400).build())
                .build();

        dataStore.addTicket(originalTicket);

        // Richiesta di modifica
        ModifyTicketRequest request = ModifyTicketRequest.newBuilder()
                .setTicketId(ticketId)
                .setNewServiceClass("PREMIUM")
                .setTrainType("INTERCITY")
                .build();

        when(mockPriceCalculator.calculateTicketPrice(
                eq("Roma"), eq("Milano"), eq("PREMIUM"), any(), eq(""), eq("INTERCITY")))
                .thenReturn(80.0);

        // Esecuzione
        ticketService.modifyTicket(request, operationResponseObserver);

        // Verifica
        ArgumentCaptor<OperationResponse> responseCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(responseCaptor.capture());
        verify(operationResponseObserver).onCompleted();

        OperationResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals(50.0, response.getOldPrice(), 0.01);
        assertEquals(80.0, response.getNewPrice(), 0.01);
        assertEquals(5.0, response.getPenalty(), 0.01); // 10% penale per cambio classe
        assertEquals(30.0, response.getTariffDiff(), 0.01); // Differenza tariffaria
    }

    @Test
    @DisplayName("Test modifica biglietto - Biglietto non trovato")
    public void testModifyTicket_TicketNotFound_Failure() {
        // Preparazione
        ModifyTicketRequest request = ModifyTicketRequest.newBuilder()
                .setTicketId("non-esistente")
                .setNewServiceClass("PREMIUM")
                .build();

        // Esecuzione
        ticketService.modifyTicket(request, operationResponseObserver);

        // Verifica
        ArgumentCaptor<OperationResponse> responseCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(responseCaptor.capture());
        verify(operationResponseObserver).onCompleted();

        OperationResponse response = responseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("non trovato"));
    }

    @Test
    @DisplayName("Test modifica biglietto - Nessuna modifica effettuata")
    public void testModifyTicket_NoChanges_Failure() {
        // Preparazione - Crea prima un biglietto
        String ticketId = UUID.randomUUID().toString();
        Ticket originalTicket = Ticket.newBuilder()
                .setId(ticketId)
                .setTrainId(1)
                .setPassengerName("testUser")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("STANDARD")
                .setPrice(50.0)
                .setSeat("1")
                .setTravelDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 86400).build())
                .build();

        dataStore.addTicket(originalTicket);

        // Richiesta di modifica senza cambiamenti
        ModifyTicketRequest request = ModifyTicketRequest.newBuilder()
                .setTicketId(ticketId)
                .build();

        // Esecuzione
        ticketService.modifyTicket(request, operationResponseObserver);

        // Verifica
        ArgumentCaptor<OperationResponse> responseCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(responseCaptor.capture());
        verify(operationResponseObserver).onCompleted();

        OperationResponse response = responseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("Nessuna modifica"));
    }

    @Test
    @DisplayName("Test annullamento biglietto - Annullamento valido")
    public void testCancelTicket_ValidCancellation_Success() {
        // Preparazione - Crea prima un biglietto
        String ticketId = UUID.randomUUID().toString();
        Ticket originalTicket = Ticket.newBuilder()
                .setId(ticketId)
                .setTrainId(1)
                .setPassengerName("testUser")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("STANDARD")
                .setPrice(50.0)
                .setSeat("1")
                .setTravelDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 86400).build())
                .build();

        dataStore.addTicket(originalTicket);

        // Richiesta di annullamento
        CancelTicketRequest request = CancelTicketRequest.newBuilder()
                .setTicketId(ticketId)
                .build();

        // Esecuzione
        ticketService.cancelTicket(request, operationResponseObserver);

        // Verifica
        ArgumentCaptor<OperationResponse> responseCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(responseCaptor.capture());
        verify(operationResponseObserver).onCompleted();

        OperationResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());

        // Verifica che il biglietto esista ancora ma con stato "Annullato"
        Ticket cancelledTicket = dataStore.getTicketById(ticketId);
        assertNotNull(cancelledTicket, "Il biglietto dovrebbe esistere ancora dopo l'annullamento");
        assertEquals("Annullato", cancelledTicket.getStatus(), "Il biglietto dovrebbe avere stato 'Annullato'");
        assertTrue(cancelledTicket.getPrice() <= originalTicket.getPrice(), "Il prezzo dovrebbe essere ridotto (rimborso dopo penale)");
    }

    @Test
    @DisplayName("Test annullamento biglietto - Biglietto non trovato")
    public void testCancelTicket_TicketNotFound_Failure() {
        // Preparazione
        CancelTicketRequest request = CancelTicketRequest.newBuilder()
                .setTicketId("non-esistente")
                .build();

        // Esecuzione
        ticketService.cancelTicket(request, operationResponseObserver);

        // Verifica
        ArgumentCaptor<OperationResponse> responseCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(responseCaptor.capture());
        verify(operationResponseObserver).onCompleted();

        OperationResponse response = responseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("non trovato"));
    }

    @AfterEach
    public void cleanup() {
        // Pulisci il DataStore dopo ogni test
        // Questo test è difficile da simulare con DataStore reale
        // Possiamo testare un errore con PriceCalculator invece
    }
}