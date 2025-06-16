package it.unical.trenical.server;

import org.junit.jupiter.api.*;
import java.util.List;
import it.unical.trenical.grpc.common.Station;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = DataStore.getInstance();
    }

    @Test
    void testStationAddAndUpdate() {
        int id = dataStore.generateNextStationId();
        Station s = Station.newBuilder().setId(id).setName("TestStation").setCity("TestCity").build();
        dataStore.addStation(s);
        Station loaded = dataStore.getStationById(id);
        assertNotNull(loaded);
        assertEquals("TestStation", loaded.getName());
        Station updated = Station.newBuilder(loaded).setName("UpdatedStation").build();
        dataStore.updateStation(updated);
        Station loaded2 = dataStore.getStationById(id);
        assertEquals("UpdatedStation", loaded2.getName());
    }

    @Test
    void testGetAllStations_NotEmpty() {
        var stations = dataStore.getAllStations();
        assertNotNull(stations);
        assertFalse(stations.isEmpty(), "Il database delle stazioni non dovrebbe essere vuoto");
    }

    @Test
    void testGetAllTrains_NotEmpty() {
        var trains = dataStore.getAllTrains();
        assertNotNull(trains);
        assertFalse(trains.isEmpty(), "Il database dei treni non dovrebbe essere vuoto");
    }

    @Test
    void testGetAllPromotions() {
        var promos = dataStore.getAllPromotions();
        assertNotNull(promos);
        // Può essere vuoto, ma il metodo deve funzionare
    }

    @Test
    void testGetAllTickets() {
        var tickets = dataStore.getAllTickets();
        assertNotNull(tickets);
        // Può essere vuoto, ma il metodo deve funzionare
    }

    @Test
    void testFindBestPromotion() {
        var stations = dataStore.getAllStations();
        if (stations.size() >= 2) {
            var dep = stations.get(0).getName();
            var arr = stations.get(1).getName();
            var promos = dataStore.getAllPromotions();
            var today = java.time.LocalDate.now();
            var promo = dataStore.findBestPromotion(dep+"-"+arr, "Economy", today, "");
            // Non è detto che esista, ma il metodo deve funzionare
        }
    }

    @Test
    void testGetStationById() {
        var stations = dataStore.getAllStations();
        if (!stations.isEmpty()) {
            var id = stations.get(0).getId();
            var s = dataStore.getStationById(id);
            assertNotNull(s);
        }
    }

    @Test
    void testGetTrainById() {
        var trains = dataStore.getAllTrains();
        if (!trains.isEmpty()) {
            var id = trains.get(0).getId();
            var t = dataStore.getTrainById(id);
            assertNotNull(t);
        }
    }

    @Test
    void testGetTicketById() {
        var tickets = dataStore.getAllTickets();
        if (!tickets.isEmpty()) {
            var id = tickets.get(0).getId();
            var t = dataStore.getTicketById(id);
            assertNotNull(t);
        }
    }

    @Test
    void testGetPromotionById() {
        var promos = dataStore.getAllPromotions();
        if (!promos.isEmpty()) {
            var id = promos.get(0).getId();
            var p = dataStore.getPromotionById(id);
            assertNotNull(p);
        }
    }

    @Test
    void testExportImportBackup() {
        String backup = dataStore.exportAllData();
        assertNotNull(backup);
        assertFalse(backup.isEmpty());
        // Import test: non deve lanciare eccezioni
        assertDoesNotThrow(() -> dataStore.importAllData(backup));
    }

    @Test
    void testThreadSafety() throws Exception {
        Runnable r = () -> {
            var st = dataStore.getAllStations();
            var tr = dataStore.getAllTrains();
            var tk = dataStore.getAllTickets();
            var pr = dataStore.getAllPromotions();
            assertNotNull(st); assertNotNull(tr); assertNotNull(tk); assertNotNull(pr);
        };
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start(); t2.start(); t1.join(); t2.join();
    }

    @Test
    void testTicketAddAndGet() {
        var ticket = it.unical.trenical.grpc.common.Ticket.newBuilder()
                .setId("T1").setTrainId(1).setPassengerName("Mario").build();
        dataStore.addTicket(ticket);
        var loaded = dataStore.getTicketById("T1");
        assertNotNull(loaded);
        assertEquals("Mario", loaded.getPassengerName());
    }

    @Test
    void testPromotionAddAndGet() {
        var promo = it.unical.trenical.grpc.promotion.Promotion.newBuilder()
                .setId(1).setName("PROMO10").setDiscountPercent(10).build();
        dataStore.addPromotion(promo);
        var promos = dataStore.getAllPromotions();
        assertTrue(promos.stream().anyMatch(p -> p.getName().equals("PROMO10")));
    }
}
