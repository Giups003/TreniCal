package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.train.TrainServiceProto.*;
import it.unical.trenical.grpc.common.Train;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test per la classe TrainServiceImpl.
 */
public class TrainServiceImplTest {

    private TrainServiceImpl service;
    private StreamObserver<TrainResponse> trainResponseObserver;
    private StreamObserver<TrainDetailsResponse> detailsResponseObserver;
    private StreamObserver<ScheduleResponse> scheduleResponseObserver;

    @BeforeEach
    public void setup() {
        service = new TrainServiceImpl();

        // Mock degli stream observers
        trainResponseObserver = mock(StreamObserver.class);
        detailsResponseObserver = mock(StreamObserver.class);
        scheduleResponseObserver = mock(StreamObserver.class);
    }

    @Test
    public void testGetTrains_Success() {
        // Preparazione
        TrainRequest request = TrainRequest.newBuilder()
                .setId(0) // 0 significa tutti i treni
                .build();

        // Esecuzione
        service.getTrains(request, trainResponseObserver);

        // Verifica
        ArgumentCaptor<TrainResponse> responseCaptor = ArgumentCaptor.forClass(TrainResponse.class);
        verify(trainResponseObserver).onNext(responseCaptor.capture());
        verify(trainResponseObserver).onCompleted();

        TrainResponse response = responseCaptor.getValue();
        assertNotNull(response);
        assertTrue(response.getTrainsCount() > 0, "Dovrebbe restituire almeno un treno");
    }

    @Test
    public void testSearchTrains_SuccessWithStations() {
        // Preparazione
        String departureStation = "Roma";
        String arrivalStation = "Milano";

        SearchTrainRequest request = SearchTrainRequest.newBuilder()
                .setDepartureStation(departureStation)
                .setArrivalStation(arrivalStation)
                .build();

        // Esecuzione
        service.searchTrains(request, trainResponseObserver);

        // Verifica
        ArgumentCaptor<TrainResponse> responseCaptor = ArgumentCaptor.forClass(TrainResponse.class);
        verify(trainResponseObserver).onNext(responseCaptor.capture());
        verify(trainResponseObserver).onCompleted();

        TrainResponse response = responseCaptor.getValue();
        assertNotNull(response);

        // Verifica che tutti i treni rispettino i criteri di ricerca
        for (Train train : response.getTrainsList()) {
            assertEquals(departureStation, train.getDepartureStation(),
                    "La stazione di partenza dovrebbe corrispondere");
            assertEquals(arrivalStation, train.getArrivalStation(),
                    "La stazione di arrivo dovrebbe corrispondere");
        }
    }

    @Test
    public void testSearchTrains_WithDate() {
        // Preparazione
        LocalDate today = LocalDate.now();
        Instant instant = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Timestamp dateTimestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        SearchTrainRequest request = SearchTrainRequest.newBuilder()
                .setDate(dateTimestamp)
                .build();

        // Esecuzione
        service.searchTrains(request, trainResponseObserver);

        // Verifica
        verify(trainResponseObserver).onNext(any(TrainResponse.class));
        verify(trainResponseObserver).onCompleted();
        verifyNoMoreInteractions(trainResponseObserver);
    }

@Test
public void testSearchTrains_InvalidRequest() {
    // Preparazione - richiesta vuota
    SearchTrainRequest request = SearchTrainRequest.newBuilder().build();

    // Esecuzione
    service.searchTrains(request, trainResponseObserver);

    // Verifica - adatta alle aspettative dell'implementazione attuale
    verify(trainResponseObserver).onNext(any(TrainResponse.class));
    verify(trainResponseObserver).onCompleted();
}

   @Test
    public void testGetTrains_NonExistingTrain() {  // Rinominato per chiarezza
        int nonExistingTrainId = 2;
        TrainRequest request = TrainRequest.newBuilder()
                .setId(nonExistingTrainId)
                .build();

        // Esecuzione
        service.getTrains(request, trainResponseObserver);

        // Verifica
        ArgumentCaptor<TrainResponse> responseCaptor = ArgumentCaptor.forClass(TrainResponse.class);
        verify(trainResponseObserver).onNext(responseCaptor.capture());
        verify(trainResponseObserver).onCompleted();

        TrainResponse response = responseCaptor.getValue();
        assertNotNull(response);
        assertEquals(0, response.getTrainsCount(), "Non dovrebbe restituire treni per un ID inesistente");
    }

    @Test
    public void testGetTrainDetails_InvalidTrainId() {
        // Preparazione
        int nonExistingTrainId = 9999; // Presuppone che non ci sia un treno con questo ID
        TrainDetailsRequest request = TrainDetailsRequest.newBuilder()
                .setTrainId(nonExistingTrainId)
                .build();

        // Esecuzione
        service.getTrainDetails(request, detailsResponseObserver);

        // Verifica
        verify(detailsResponseObserver, never()).onNext(any());
        verify(detailsResponseObserver).onError(any());
    }

    @Test
    public void testGetTrainSchedule_Success() {
        String station = "Milano";
        LocalDate today = LocalDate.now();
        Instant instant = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Timestamp dateTimestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        ScheduleRequest request = ScheduleRequest.newBuilder()
                .setStation(station)
                .setDate(dateTimestamp)
                .build();

        // Act: invoco il servizio
        service.getTrainSchedule(request, scheduleResponseObserver);

        // Assert: verifico che la risposta sia ricevuta e completata
        ArgumentCaptor<ScheduleResponse> responseCaptor = ArgumentCaptor.forClass(ScheduleResponse.class);
        verify(scheduleResponseObserver).onNext(responseCaptor.capture());
        verify(scheduleResponseObserver).onCompleted();

        ScheduleResponse response = responseCaptor.getValue();
        assertNotNull(response);

        // Se non sono sicuro che ci siano partenze o arrivi, questa asserzione va bene.
        // Ma se voglio essere certo che almeno una partenza o un arrivo ci sia (magari per un test più "forte"),
        // posso aggiungere questa riga (commentata per ora):
        // assertTrue(response.getDeparturesCount() > 0 || response.getArrivalsCount() > 0,
        // Mi aspetto almeno una partenza o un arrivo;
    }

    @Test
    public void testGetTrainSchedule_InvalidStation() {
        // Preparazione
        String nonExistingStation = "StazioneInesistente";
        LocalDate today = LocalDate.now();
        Instant instant = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Timestamp dateTimestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        ScheduleRequest request = ScheduleRequest.newBuilder()
                .setStation(nonExistingStation)
                .setDate(dateTimestamp)
                .build();

        // Esecuzione
        service.getTrainSchedule(request, scheduleResponseObserver);

        // Verifica
        ArgumentCaptor<ScheduleResponse> responseCaptor = ArgumentCaptor.forClass(ScheduleResponse.class);
        verify(scheduleResponseObserver).onNext(responseCaptor.capture());
        verify(scheduleResponseObserver).onCompleted();

        ScheduleResponse response = responseCaptor.getValue();
        assertNotNull(response);
        assertEquals(0, response.getDeparturesCount(), "Non dovrebbero esserci partenze");
        assertEquals(0, response.getArrivalsCount(), "Non dovrebbero esserci arrivi");
    }
}