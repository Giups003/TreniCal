package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.ticket.*;
import it.unical.trenical.grpc.promotion.Promotion;
import it.unical.trenical.server.strategy.PriceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test di integrazione per verificare che il pattern Strategy sia correttamente
 * integrato nel TicketServiceImpl e che i codici promo vengano gestiti correttamente
 * durante l'acquisto dei biglietti.
 */
class TicketServiceImplIntegrationTest {

    private TicketServiceImpl ticketService;
    private DataStore dataStore;
    private PriceCalculator mockPriceCalculator;

    @BeforeEach
    void setUp() {
        dataStore = DataStore.getInstance();
        setupTestData();

        // Usa un mock del PriceCalculator per controllare le chiamate
        mockPriceCalculator = mock(PriceCalculator.class);
        ticketService = new TicketServiceImpl(mockPriceCalculator);
    }

    @Test
    @DisplayName("Test acquisto biglietto con strategia automatica basata su tipo cliente")
    void testPurchaseTicketWithAutomaticStrategy() throws InterruptedException {
        // Setup: cliente standard nel DataStore
        dataStore.setCustomerType("mario.rossi", "standard");

        // Mock delle chiamate al PriceCalculator
        when(mockPriceCalculator.getCurrentStrategyName()).thenReturn("StandardPriceCalculationStrategy");
        when(mockPriceCalculator.isValidPromoCode("ESTATE2024", "standard")).thenReturn(true);
        when(mockPriceCalculator.calculateTicketPrice(any(), any(), any(), any(), any(), any()))
                .thenReturn(85.50);

        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("mario.rossi")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("Seconda Classe")
                .setTravelDate(createTimestamp(7))
                .setPromoCode("ESTATE2024")
                .setPaymentMethod("Carta di Credito")
                .setSeats(1)
                .setTrainType("Frecciarossa")
                .build();

        CountDownLatch latch = new CountDownLatch(1);
        PurchaseTicketResponse[] response = new PurchaseTicketResponse[1];

        StreamObserver<PurchaseTicketResponse> observer = new StreamObserver<>() {
            @Override
            public void onNext(PurchaseTicketResponse value) {
                response[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                fail("Non dovrebbe verificarsi un errore: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        ticketService.purchaseTicket(request, observer);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "La risposta dovrebbe arrivare entro 5 secondi");

        // Verifica che la strategia sia stata impostata correttamente
        verify(mockPriceCalculator).setStrategyByType("standard");

        // Verifica che il codice promo sia stato validato
        verify(mockPriceCalculator).isValidPromoCode("ESTATE2024", "standard");

        // Verifica che il prezzo sia stato calcolato con i parametri corretti
        verify(mockPriceCalculator).calculateTicketPrice(
                eq("Roma"), eq("Milano"), eq("Seconda Classe"),
                any(Timestamp.class), eq("ESTATE2024"), eq("Frecciarossa")
        );

        // Verifica la risposta
        assertNotNull(response[0], "La risposta non dovrebbe essere null");
        assertTrue(response[0].getSuccess(), "L'acquisto dovrebbe avere successo");
        assertEquals(85.50, response[0].getPrice(), 0.01, "Il prezzo dovrebbe corrispondere");
    }

    @Test
    @DisplayName("Test rifiuto acquisto con codice promo non valido")
    void testPurchaseTicketWithInvalidPromoCode() throws InterruptedException {
        dataStore.setCustomerType("mario.rossi", "standard");

        when(mockPriceCalculator.getCurrentStrategyName()).thenReturn("StandardPriceCalculationStrategy");
        when(mockPriceCalculator.isValidPromoCode("INVALIDCODE", "standard")).thenReturn(false);

        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("mario.rossi")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("Seconda Classe")
                .setTravelDate(createTimestamp(7))
                .setPromoCode("INVALIDCODE")
                .setPaymentMethod("Carta di Credito")
                .setSeats(1)
                .build();

        CountDownLatch latch = new CountDownLatch(1);
        PurchaseTicketResponse[] response = new PurchaseTicketResponse[1];

        StreamObserver<PurchaseTicketResponse> observer = new StreamObserver<>() {
            @Override
            public void onNext(PurchaseTicketResponse value) {
                response[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                fail("Non dovrebbe verificarsi un errore: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        ticketService.purchaseTicket(request, observer);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "La risposta dovrebbe arrivare entro 5 secondi");

        assertNotNull(response[0], "La risposta non dovrebbe essere null");
        assertFalse(response[0].getSuccess(), "L'acquisto dovrebbe fallire");
        assertTrue(response[0].getMessage().contains("non valido"),
                  "Il messaggio dovrebbe indicare che il codice non è valido");

        // Verifica che il calcolo del prezzo non sia stato fatto
        verify(mockPriceCalculator, never()).calculateTicketPrice(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test calcolo prezzo con strategia VIP")
    void testPriceCalculationWithVIPStrategy() throws InterruptedException {
        dataStore.setCustomerType("vip.customer", "vip");

        when(mockPriceCalculator.getCurrentStrategyName()).thenReturn("VIPCustomerPricingStrategy");
        when(mockPriceCalculator.calculateTicketPrice(any(), any(), any(), any(), any(), any()))
                .thenReturn(65.00); // Prezzo scontato VIP

        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("vip.customer")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("Prima Classe")
                .setTravelDate(createTimestamp(7))
                .setPaymentMethod("SOLO_PREZZO") // Solo calcolo prezzo
                .setSeats(1)
                .setTrainType("Frecciarossa")
                .build();

        CountDownLatch latch = new CountDownLatch(1);
        PurchaseTicketResponse[] response = new PurchaseTicketResponse[1];

        StreamObserver<PurchaseTicketResponse> observer = new StreamObserver<>() {
            @Override
            public void onNext(PurchaseTicketResponse value) {
                response[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                fail("Non dovrebbe verificarsi un errore: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        ticketService.purchaseTicket(request, observer);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "La risposta dovrebbe arrivare entro 5 secondi");

        // Verifica che sia stata impostata la strategia VIP
        verify(mockPriceCalculator).setStrategyByType("vip");

        assertNotNull(response[0], "La risposta non dovrebbe essere null");
        assertTrue(response[0].getSuccess(), "Il calcolo prezzo dovrebbe avere successo");
        assertEquals(65.00, response[0].getPrice(), 0.01, "Il prezzo VIP dovrebbe essere applicato");
        assertTrue(response[0].getMessage().contains("VIPCustomerPricingStrategy"),
                  "Il messaggio dovrebbe indicare la strategia VIP");
    }

    @Test
    @DisplayName("Test GetTicketPrice con strategia Corporate")
    void testGetTicketPriceWithCorporateStrategy() throws InterruptedException {
        // NOTA: GetTicketPriceRequest non ha passengerName, quindi testiamo diversamente
        // Impostiamo manualmente il tipo cliente nel DataStore
        dataStore.setCustomerType("corp.user", "corporate");

        when(mockPriceCalculator.getCurrentStrategyName()).thenReturn("CorporateCustomerPricingStrategy");
        when(mockPriceCalculator.calculateTicketPrice(
                eq("Roma"), eq("Milano"), eq("Prima Classe"),
                any(Timestamp.class), eq("CORP2024"), eq("Frecciarossa")))
                .thenReturn(75.00);

        GetTicketPriceRequest request = GetTicketPriceRequest.newBuilder()
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("Prima Classe")
                .setTravelDate(createTimestamp(7))
                .setPromoCode("CORP2024")
                .setTrainType("Frecciarossa")
                .build();

        CountDownLatch latch = new CountDownLatch(1);
        GetTicketPriceResponse[] response = new GetTicketPriceResponse[1];

        StreamObserver<GetTicketPriceResponse> observer = new StreamObserver<>() {
            @Override
            public void onNext(GetTicketPriceResponse value) {
                response[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                fail("Non dovrebbe verificarsi un errore: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        ticketService.getTicketPrice(request, observer);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "La risposta dovrebbe arrivare entro 5 secondi");

        // NOTA: GetTicketPrice senza username usa la strategia standard di default
        // Per testare la strategia corporate, dovremmo usare PurchaseTicket con SOLO_PREZZO
        assertNotNull(response[0], "La risposta non dovrebbe essere null");
        assertEquals(75.00, response[0].getPrice(), 0.01, "Il prezzo dovrebbe essere calcolato");

        // Verifica che il calcolo del prezzo sia stato chiamato
        verify(mockPriceCalculator).calculateTicketPrice(
                eq("Roma"), eq("Milano"), eq("Prima Classe"),
                any(Timestamp.class), eq("CORP2024"), eq("Frecciarossa")
        );
    }

    @Test
    @DisplayName("Test calcolo prezzo con tipo cliente specifico tramite PurchaseTicket")
    void testPriceCalculationWithSpecificCustomerType() throws InterruptedException {
        // Test più accurato per la strategia corporate usando PurchaseTicket con SOLO_PREZZO
        dataStore.setCustomerType("corp.user", "corporate");

        when(mockPriceCalculator.getCurrentStrategyName()).thenReturn("CorporateCustomerPricingStrategy");
        when(mockPriceCalculator.isValidPromoCode("CORP2024", "corporate")).thenReturn(true);
        when(mockPriceCalculator.calculateTicketPrice(any(), any(), any(), any(), any(), any()))
                .thenReturn(70.00);

        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("corp.user")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("Prima Classe")
                .setTravelDate(createTimestamp(7))
                .setPromoCode("CORP2024")
                .setPaymentMethod("SOLO_PREZZO") // Solo calcolo prezzo
                .setSeats(1)
                .setTrainType("Frecciarossa")
                .build();

        CountDownLatch latch = new CountDownLatch(1);
        PurchaseTicketResponse[] response = new PurchaseTicketResponse[1];

        StreamObserver<PurchaseTicketResponse> observer = new StreamObserver<>() {
            @Override
            public void onNext(PurchaseTicketResponse value) {
                response[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                fail("Non dovrebbe verificarsi un errore: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        ticketService.purchaseTicket(request, observer);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "La risposta dovrebbe arrivare entro 5 secondi");

        // Verifica che sia stata impostata la strategia Corporate
        verify(mockPriceCalculator).setStrategyByType("corporate");

        assertNotNull(response[0], "La risposta non dovrebbe essere null");
        assertTrue(response[0].getSuccess(), "Il calcolo prezzo dovrebbe avere successo");
        assertEquals(70.00, response[0].getPrice(), 0.01, "Il prezzo Corporate dovrebbe essere applicato");
        assertTrue(response[0].getMessage().contains("CorporateCustomerPricingStrategy"),
                  "Il messaggio dovrebbe indicare la strategia Corporate");
    }

    // Metodi di utilità

    private void setupTestData() {
        // Setup promozioni di test
        Promotion promo = Promotion.newBuilder()
                .setId(1)
                .setName("ESTATE2024")
                .setDescription("Sconto estivo del 15%")
                .setDiscountPercent(15.0)
                .setValidFrom(createTimestamp(-30))
                .setValidTo(createTimestamp(30))
                .setOnlyForLoyaltyMembers(false)
                .build();

        dataStore.addPromotion(promo);
    }

    private Timestamp createTimestamp(int daysFromNow) {
        return Timestamp.newBuilder()
                .setSeconds(LocalDate.now().plusDays(daysFromNow)
                           .atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                .build();
    }
}
