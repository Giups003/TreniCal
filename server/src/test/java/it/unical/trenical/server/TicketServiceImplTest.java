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

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setup() {
        priceCalculator = mock(PriceCalculator.class);
        // Mock con 6 parametri (firma aggiornata)
        when(priceCalculator.calculateTicketPrice(anyString(), anyString(), anyString(), any(Timestamp.class), anyString(), anyString()))
                .thenReturn(100.0);
        service = new TicketServiceImpl(priceCalculator);
        purchaseResponseObserver = mock(StreamObserver.class);
        operationResponseObserver = mock(StreamObserver.class);
    }

    @Test
    public void acquistoBigliettoSuccesso() {
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("Mario")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setTravelDate(Timestamp.newBuilder().setSeconds(1700000000).build())
                .setServiceClass("Economy")
                .setTrainType("Frecciarossa")
                .build();
        service.purchaseTicket(request, purchaseResponseObserver);
        ArgumentCaptor<PurchaseTicketResponse> captor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(captor.capture());
        assertTrue(captor.getValue().getSuccess());
        verify(priceCalculator).calculateTicketPrice(eq("Roma"), eq("Milano"), eq("Economy"), any(Timestamp.class), eq(""), eq("Frecciarossa"));
    }

    @Test
    public void modificaBigliettoSuccesso() {
        // Acquisto biglietto
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("Mario")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setTravelDate(Timestamp.newBuilder().setSeconds(1700000000).build())
                .setServiceClass("Economy")
                .setTrainType("Frecciarossa")
                .build();
        service.purchaseTicket(request, purchaseResponseObserver);
        ArgumentCaptor<PurchaseTicketResponse> captor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(captor.capture());
        String ticketId = captor.getValue().getTicketId();
        // Modifica biglietto
        ModifyTicketRequest modReq = ModifyTicketRequest.newBuilder()
                .setTicketId(ticketId)
                .setNewServiceClass("Prima Classe")
                .setTrainType("Frecciarossa")
                .build();
        when(priceCalculator.calculateTicketPrice(anyString(), anyString(), eq("Prima Classe"), any(Timestamp.class), anyString(), anyString())).thenReturn(150.0);
        service.modifyTicket(modReq, operationResponseObserver);
        ArgumentCaptor<OperationResponse> opCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(opCaptor.capture());
        assertTrue(opCaptor.getValue().getSuccess());
    }

    @Test
    public void acquistoBigliettoPromoCode() {
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(1)
                .setPassengerName("Mario")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setTravelDate(Timestamp.newBuilder().setSeconds(1700000000).build())
                .setServiceClass("Economy")
                .setPromoCode("PROMO10")
                .setTrainType("Frecciarossa")
                .build();
        service.purchaseTicket(request, purchaseResponseObserver);
        verify(priceCalculator).calculateTicketPrice(eq("Roma"), eq("Milano"), eq("Economy"), any(Timestamp.class), eq("PROMO10"), eq("Frecciarossa"));
    }

    @Test
    public void modificaBigliettoNonTrovato() {
        ModifyTicketRequest modReq = ModifyTicketRequest.newBuilder()
                .setTicketId("fake")
                .setNewServiceClass("Prima Classe")
                .setTrainType("Frecciarossa")
                .build();
        service.modifyTicket(modReq, operationResponseObserver);
        ArgumentCaptor<OperationResponse> opCaptor = ArgumentCaptor.forClass(OperationResponse.class);
        verify(operationResponseObserver).onNext(opCaptor.capture());
        assertFalse(opCaptor.getValue().getSuccess());
    }

    @Test
    public void acquistoBigliettoRichiestaNonValida() {
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setPassengerName("")
                .build();
        service.purchaseTicket(request, purchaseResponseObserver);
        ArgumentCaptor<PurchaseTicketResponse> captor = ArgumentCaptor.forClass(PurchaseTicketResponse.class);
        verify(purchaseResponseObserver).onNext(captor.capture());
        assertFalse(captor.getValue().getSuccess());
    }
}
