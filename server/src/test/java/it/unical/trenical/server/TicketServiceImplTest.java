package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.ticket.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketServiceImplTest {

    private TicketServiceImpl service;
    private PriceCalculator priceCalculator;
    private StreamObserver<PurchaseTicketResponse> purchaseResponseObserver;
    private StreamObserver<OperationResponse> operationResponseObserver;

    @BeforeEach
    public void setup() {
        // Mock del calcolatore di prezzi
        priceCalculator = mock(PriceCalculator.class);
        when(priceCalculator.calculateTicketPrice(anyString(), anyString(), anyString(), any(Timestamp.class), anyString()))
                .thenReturn(100.0); // Prezzo fisso per i test

        // Crea il servizio con il mock del calcolatore
        service = new TicketServiceImpl(priceCalculator);

        // Mock degli observer per le risposte
        purchaseResponseObserver = mock(StreamObserver.class);
        operationResponseObserver = mock(StreamObserver.class);
    }

    @Test
    public void testPurchaseTicket_Success() {
        // Crea una richiesta di acquisto valida
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1234)
                .setPassengerName("Mario Rossi")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setTravelDate(Timestamp.newBuilder().setSeconds(1684502400).build()) // 2025-05-20
                .setServiceClass("Economy")
                .build();

        // Esegui il metodo
        service.purchaseTicket(request, purchaseResponseObserver);

        // Cattura la risposta
        ArgumentCaptor<PurchaseTicketResponse> responseCaptor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(responseCaptor.capture());
        verify(purchaseResponseObserver).onCompleted();

        // Verifica i dettagli della risposta
        PurchaseTicketResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertNotNull(response.getTicketId());
        assertEquals(100.0, response.getPrice());
        assertEquals("Biglietto acquistato con successo!", response.getMessage());

        // Verifica che il calcolatore di prezzi sia stato chiamato con i parametri corretti
        verify(priceCalculator).calculateTicketPrice(
                eq("Roma"),
                eq("Milano"),
                eq("Economy"),
                eq(Timestamp.newBuilder().setSeconds(1684502400).build()), // 2025-05-20
                eq("")
        );
    }

    @Test
    public void testPurchaseTicket_WithPromoCode() {
        // Crea una richiesta di acquisto con codice promozionale
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1234)
                .setPassengerName("Mario Rossi")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setTravelDate(Timestamp.newBuilder().setSeconds(1684502400).build()) // 2025-05-20
                .setServiceClass("Economy")
                .setPromoCode("PROMO10")
                .build();

        // Esegui il metodo
        service.purchaseTicket(request, purchaseResponseObserver);

        // Verifica che il calcolatore di prezzi sia stato chiamato con il codice promozionale
        verify(priceCalculator).calculateTicketPrice(
                eq("Roma"),
                eq("Milano"),
                eq("Economy"),
                eq(Timestamp.newBuilder().setSeconds(1684502400).build()), // 2025-05-20
                eq("PROMO10")
        );
    }

    @Test
    public void testPurchaseTicket_InvalidRequest() {
        // Crea una richiesta invalida (senza ID treno)
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setPassengerName("Mario Rossi")
                .build();

        // Esegui il metodo
        service.purchaseTicket(request, purchaseResponseObserver);

        // Cattura la risposta
        ArgumentCaptor<PurchaseTicketResponse> responseCaptor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(responseCaptor.capture());
        verify(purchaseResponseObserver).onCompleted();

        // Verifica che la risposta indichi un errore
        PurchaseTicketResponse response = responseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertTrue(response.getMessage().contains("non valida"));

        // Verifica che il calcolatore di prezzi non sia stato chiamato
        verify(priceCalculator, never()).calculateTicketPrice(anyString(), anyString(), anyString(), any(Timestamp.class), anyString());
    }

    @Test
    public void testModifyTicket_Success() {
        // Prima acquista un biglietto per avere un ID valido
        PurchaseTicketRequest purchaseRequest = PurchaseTicketRequest.newBuilder()
                .setTrainId(1234)
                .setPassengerName("Mario Rossi")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setTravelDate(Timestamp.newBuilder().setSeconds(1684502400).build()) // 2025-05-20
                .setServiceClass("Economy")
                .build();

        service.purchaseTicket(purchaseRequest, purchaseResponseObserver);

        // Cattura la risposta dell'acquisto per ottenere l'ID del biglietto
        ArgumentCaptor<PurchaseTicketResponse> purchaseResponseCaptor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(purchaseResponseCaptor.capture());

        String ticketId = purchaseResponseCaptor.getValue().getTicketId();

        // Crea una richiesta di modifica
        ModifyTicketRequest modifyRequest = ModifyTicketRequest.newBuilder()
                .setTicketId(ticketId)
                .setNewDate(Timestamp.newBuilder().setSeconds(1684588800).build()) // 2025-06-01
                .setNewServiceClass("Prima Classe")
                .build();

        // Configura il mock del calcolatore per la nuova classe di servizio
        when(priceCalculator.calculateTicketPrice(anyString(), anyString(), eq("Prima Classe"), any(Timestamp.class), anyString()))
                .thenReturn(150.0); // Prezzo più alto per la prima classe

        // Esegui il metodo di modifica
        service.modifyTicket(modifyRequest, operationResponseObserver);

        // Cattura la risposta
        ArgumentCaptor<OperationResponse> operationResponseCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(operationResponseCaptor.capture());
        verify(operationResponseObserver).onCompleted();

        // Verifica i dettagli della risposta
        OperationResponse response = operationResponseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals("Biglietto modificato con successo!", response.getMessage());
    }

    @Test
    public void testModifyTicket_InvalidTicketId() {
        // Crea una richiesta di modifica con ID non valido
        ModifyTicketRequest request = ModifyTicketRequest.newBuilder()
                .setTicketId("non-esistente")
                .setNewDate(Timestamp.newBuilder().setSeconds(1684588800).build()) // 2025-06-01
                .build();

        // Esegui il metodo
        service.modifyTicket(request, operationResponseObserver);

        // Cattura la risposta
        ArgumentCaptor<OperationResponse> responseCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(responseCaptor.capture());
        verify(operationResponseObserver).onCompleted();

        // Verifica che la risposta indichi un errore
        OperationResponse response = responseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertEquals("Biglietto non trovato!", response.getMessage());
    }

    @Test
    public void testModifyTicket_NoFieldsToModify() {
        // Prima acquista un biglietto per avere un ID valido
        PurchaseTicketRequest purchaseRequest = PurchaseTicketRequest.newBuilder()
                .setTrainId(1234)
                .setPassengerName("Mario Rossi")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setTravelDate(Timestamp.newBuilder().setSeconds(1684502400).build()) // 2025-05-20
                .setServiceClass("Economy")
                .build();

        service.purchaseTicket(purchaseRequest, purchaseResponseObserver);

        // Cattura la risposta dell'acquisto per ottenere l'ID del biglietto
        ArgumentCaptor<PurchaseTicketResponse> purchaseResponseCaptor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(purchaseResponseCaptor.capture());

        String ticketId = purchaseResponseCaptor.getValue().getTicketId();

        // Crea una richiesta di modifica senza campi da modificare
        ModifyTicketRequest modifyRequest = ModifyTicketRequest.newBuilder()
                .setTicketId(ticketId)
                .build();

        // Esegui il metodo di modifica
        service.modifyTicket(modifyRequest, operationResponseObserver);

        // Cattura la risposta
        ArgumentCaptor<OperationResponse> operationResponseCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(operationResponseCaptor.capture());
        verify(operationResponseObserver).onCompleted();

        // Verifica che la risposta indichi un errore
        OperationResponse response = operationResponseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertEquals("Nessun campo da modificare specificato!", response.getMessage());
    }
}
