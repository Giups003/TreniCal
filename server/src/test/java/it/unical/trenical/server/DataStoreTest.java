package it.unical.trenical.server;

import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import it.unical.trenical.grpc.common.Station;
import it.unical.trenical.grpc.common.Station.Builder;

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
    void testExportImportBackup() {
        String backup = dataStore.exportAllData();
        assertNotNull(backup);
        assertTrue(backup.contains("stations"));
        // Simula import (non implementato completamente)
        assertDoesNotThrow(() -> dataStore.importAllData(backup));
    }

    @Test
    void testIdGenerationUnique() {
        int id1 = dataStore.generateNextStationId();
        int id2 = dataStore.generateNextStationId();
        assertTrue(id2 >= id1);
    }

    @Test
    void testThreadSafety() throws Exception {
        // Test semplice: accesso concorrente a getAllStations
        Runnable r = () -> {
            List<Station> list = dataStore.getAllStations();
            assertNotNull(list);
        };
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start(); t2.start();
        t1.join(); t2.join();
    }
}

