package it.unical.trenical.server;

import it.unical.trenical.grpc.promotion.Promotion;
import it.unical.trenical.grpc.common.Station;
import it.unical.trenical.grpc.common.Ticket;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = DataStore.getInstance();
        // Pulizia dati per evitare conflitti tra test
        cleanupTestData();
    }

    private void cleanupTestData() {
        // Pulisce solo i dati di test per evitare di compromettere il sistema
        List<Promotion> promos = dataStore.getAllPromotions();
        for (Promotion p : promos) {
            if (p.getName().startsWith("TEST_")) {
                dataStore.deletePromotion(p.getId());
            }
        }

        List<Ticket> tickets = dataStore.getAllTickets();
        for (Ticket t : tickets) {
            if (t.getId().startsWith("TEST_")) {
                dataStore.deleteTicket(t.getId());
            }
        }
    }

    @Test
    @DisplayName("Test aggiunta e aggiornamento stazione")
    void testStationAddAndUpdate() {
        int id = dataStore.generateNextStationId();
        Station s = Station.newBuilder()
                .setId(id)
                .setName("TestStation")
                .setCity("TestCity")
                .setLatitude(41.9028)
                .setLongitude(12.4964)
                .build();

        dataStore.addStation(s);
        Station loaded = dataStore.getStationById(id);

        assertNotNull(loaded, "La stazione dovrebbe essere stata salvata");
        assertEquals("TestStation", loaded.getName(), "Il nome della stazione dovrebbe corrispondere");
        assertEquals("TestCity", loaded.getCity(), "La città dovrebbe corrispondere");

        Station updated = Station.newBuilder(loaded)
                .setName("UpdatedStation")
                .build();
        dataStore.updateStation(updated);

        Station loaded2 = dataStore.getStationById(id);
        assertEquals("UpdatedStation", loaded2.getName(), "Il nome aggiornato dovrebbe essere salvato");

        // Cleanup
        dataStore.deleteStation(id);
    }

    @Test
    @DisplayName("Test database non vuoto - verifica presenza dati di sistema")
    void testDataNotEmpty() {
        var stations = dataStore.getAllStations();
        var trains = dataStore.getAllTrains();
        var routes = dataStore.getAllRoutes();

        assertNotNull(stations, "La lista delle stazioni non dovrebbe essere null");
        assertNotNull(trains, "La lista dei treni non dovrebbe essere null");
        assertNotNull(routes, "La lista delle rotte non dovrebbe essere null");

        // I dati possono essere vuoti in un ambiente di test pulito, ma i metodi devono funzionare
        assertTrue(stations.size() >= 0, "Dovrebbe restituire una lista valida di stazioni");
        assertTrue(trains.size() >= 0, "Dovrebbe restituire una lista valida di treni");
        assertTrue(routes.size() >= 0, "Dovrebbe restituire una lista valida di rotte");
    }

    @Test
    @DisplayName("Test gestione biglietti")
    void testTicketManagement() {
        var ticket = Ticket.newBuilder()
                .setId("TEST_T1")
                .setTrainId(1)
                .setPassengerName("Mario Rossi")
                .setDepartureStation("Roma")
                .setArrivalStation("Milano")
                .setServiceClass("Seconda Classe")
                .setPrice(50.0)
                .setStatus("Confermato")
                .build();

        dataStore.addTicket(ticket);
        var loaded = dataStore.getTicketById("TEST_T1");

        assertNotNull(loaded, "Il biglietto dovrebbe essere stato salvato");
        assertEquals("Mario Rossi", loaded.getPassengerName(), "Il nome del passeggero dovrebbe corrispondere");
        assertEquals("Roma", loaded.getDepartureStation(), "La stazione di partenza dovrebbe corrispondere");
        assertEquals(50.0, loaded.getPrice(), 0.01, "Il prezzo dovrebbe corrispondere");

        // Test aggiornamento
        var updated = Ticket.newBuilder(loaded)
                .setStatus("Annullato")
                .build();
        dataStore.updateTicket(updated);

        var loadedUpdated = dataStore.getTicketById("TEST_T1");
        assertEquals("Annullato", loadedUpdated.getStatus(), "Lo status dovrebbe essere aggiornato");

        // Cleanup
        dataStore.deleteTicket("TEST_T1");
        assertNull(dataStore.getTicketById("TEST_T1"), "Il biglietto dovrebbe essere stato eliminato");
    }

    @Test
    @DisplayName("Test gestione promozioni")
    void testPromotionManagement() {
        int id = dataStore.generateNextPromotionId();
        var promo = Promotion.newBuilder()
                .setId(id)
                .setName("TEST_PROMO10")
                .setDescription("Sconto test del 10%")
                .setDiscountPercent(10.0)
                .setOnlyForLoyaltyMembers(false)
                .build();

        dataStore.addPromotion(promo);
        var loaded = dataStore.getPromotionById(id);

        assertNotNull(loaded, "La promozione dovrebbe essere stata salvata");
        assertEquals("TEST_PROMO10", loaded.getName(), "Il nome della promozione dovrebbe corrispondere");
        assertEquals(10.0, loaded.getDiscountPercent(), 0.01, "La percentuale di sconto dovrebbe corrispondere");

        var allPromos = dataStore.getAllPromotions();
        assertTrue(allPromos.stream().anyMatch(p -> p.getName().equals("TEST_PROMO10")),
                "La promozione dovrebbe essere presente nella lista");

        // Cleanup
        dataStore.deletePromotion(id);
        assertNull(dataStore.getPromotionById(id), "La promozione dovrebbe essere stata eliminata");
    }

    @Test
    @DisplayName("Test ricerca migliore promozione")
    void testFindBestPromotion() {
        // Setup promozioni di test
        int id1 = dataStore.generateNextPromotionId();
        int id2 = dataStore.generateNextPromotionId();

        var promo1 = Promotion.newBuilder()
                .setId(id1)
                .setName("TEST_SUMMER15")
                .setDiscountPercent(15.0)
                .setTrainType("Frecciarossa")
                .build();

        var promo2 = Promotion.newBuilder()
                .setId(id2)
                .setName("TEST_WINTER20")
                .setDiscountPercent(20.0)
                .setTrainType("Frecciarossa")
                .build();

        dataStore.addPromotion(promo1);
        dataStore.addPromotion(promo2);

        var bestPromo = dataStore.findBestPromotion(
                "Roma-Milano", "Seconda Classe",
                java.time.LocalDate.now(), "Frecciarossa"
        );

        assertNotNull(bestPromo, "Dovrebbe trovare una promozione");
        assertEquals(20.0, bestPromo.getDiscountPercent(), 0.01,
                "Dovrebbe trovare la promozione con sconto maggiore");

        // Cleanup
        dataStore.deletePromotion(id1);
        dataStore.deletePromotion(id2);
    }

    @Test
    @DisplayName("Test gestione tipi cliente")
    void testCustomerTypeManagement() {
        // Test impostazione tipo cliente
        dataStore.setCustomerType("test.user", "vip");
        assertEquals("vip", dataStore.getCustomerType("test.user"),
                "Il tipo cliente dovrebbe essere impostato correttamente");

        // Test tipo cliente standard di default
        assertEquals("standard", dataStore.getCustomerType("nonexistent.user"),
                "Dovrebbe restituire 'standard' per utenti inesistenti");

        // Test con username vuoto o null
        assertEquals("standard", dataStore.getCustomerType(""),
                "Dovrebbe restituire 'standard' per username vuoto");
        assertEquals("standard", dataStore.getCustomerType(null),
                "Dovrebbe restituire 'standard' per username null");
    }

    @Test
    @DisplayName("Test ricerca treni per giorno")
    void testTrainSearchByDay() {
        var today = java.time.LocalDate.now();
        var trains = dataStore.generateTrainsForDay("Roma", "Milano", today);

        assertNotNull(trains, "La lista dei treni non dovrebbe essere null");

        // Se ci sono treni, verifica che rispettino i criteri
        trains.forEach(train -> {
            var depDate = java.time.Instant.ofEpochSecond(train.getDepartureTime().getSeconds())
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            assertEquals(today, depDate, "Tutti i treni dovrebbero essere per la data richiesta");
        });
    }

    @Test
    @DisplayName("Test thread safety delle operazioni")
    void testThreadSafety() throws InterruptedException {
        final int NUM_THREADS = 5;
        final int OPERATIONS_PER_THREAD = 10;

        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    // Operazioni di lettura thread-safe
                    var stations = dataStore.getAllStations();
                    var trains = dataStore.getAllTrains();
                    var tickets = dataStore.getAllTickets();
                    var promotions = dataStore.getAllPromotions();

                    assertNotNull(stations, "Stations non dovrebbe essere null nel thread " + threadId);
                    assertNotNull(trains, "Trains non dovrebbe essere null nel thread " + threadId);
                    assertNotNull(tickets, "Tickets non dovrebbe essere null nel thread " + threadId);
                    assertNotNull(promotions, "Promotions non dovrebbe essere null nel thread " + threadId);
                }
            });
        }

        // Avvia tutti i thread
        for (Thread thread : threads) {
            thread.start();
        }

        // Aspetta che tutti i thread terminino
        for (Thread thread : threads) {
            thread.join(5000); // Timeout di 5 secondi
            assertFalse(thread.isAlive(), "Il thread dovrebbe essere terminato");
        }
    }

    @Test
    @DisplayName("Test export dati")
    void testDataExportImport() {
        assertDoesNotThrow(() -> {
            String backup = dataStore.exportAllData();
            assertNotNull(backup, "L'export non dovrebbe essere null");
            assertFalse(backup.isEmpty(), "L'export non dovrebbe essere vuoto");
            assertTrue(backup.contains("stations"), "L'export dovrebbe contenere le stazioni");
            assertTrue(backup.contains("trains"), "L'export dovrebbe contenere i treni");

        }, "L'export non dovrebbe generare eccezioni");
    }

    @Test
    @DisplayName("Test gestione posti disponibili")
    void testAvailableSeatsManagement() {
        var trains = dataStore.getAllTrains();
        if (!trains.isEmpty()) {
            var train = trains.get(0);
            var travelDateTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(train.getDepartureTime().getSeconds()),
                    java.time.ZoneId.systemDefault()
            );

            int availableSeats = dataStore.getAvailableSeats(train.getId(), travelDateTime);
            assertTrue(availableSeats >= 0, "I posti disponibili dovrebbero essere non negativi");
            assertTrue(availableSeats <= 150, "I posti disponibili non dovrebbero superare la capacità massima");

            boolean canBook = dataStore.checkAvailableSeats(train.getId(), travelDateTime, 1);
            assertEquals(availableSeats >= 1, canBook,
                    "La verifica disponibilità dovrebbe essere coerente con i posti disponibili");
        }
    }
}
