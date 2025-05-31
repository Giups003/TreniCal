package it.unical.trenical.server.data;

import com.google.protobuf.util.JsonFormat;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.common.Station;
import com.google.protobuf.Timestamp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataStore {
    // Salva i dati solo nella cartella data all'interno di server
    private static final String DATA_DIR = "C:/Users/Giuseppe/Documents/TreniCal/TreniCal/server/data";
    private static final String STATIONS_FILE = DATA_DIR + "/stations.json";
    private static final String TRAINS_FILE = DATA_DIR + "/trains.json";
    private static final String TICKETS_FILE = DATA_DIR + "/tickets.json";

    private static DataStore instance;
    private List<Station> stations = new ArrayList<>();
    private List<Train> trains = new ArrayList<>();
    private List<Ticket> tickets = new ArrayList<>();

    private DataStore() {
        // Crea la directory dei dati
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        createJsonFilesIfNotExist();
        loadData();
        populateAllExampleDataIfEmpty();
    }

    private void createJsonFilesIfNotExist() {
        createEmptyJsonIfNotExist(STATIONS_FILE);
        createEmptyJsonIfNotExist(TRAINS_FILE);
        createEmptyJsonIfNotExist(TICKETS_FILE);
    }

    private void createEmptyJsonIfNotExist(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                Files.writeString(Paths.get(filename), "[]");
            } catch (IOException e) {
                System.err.println("Impossibile creare il file " + filename + ": " + e.getMessage());
            }
        }
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private void loadData() {
        // Prima di caricare, verifica che i file siano validi (array JSON o vuoti)
        resetFileIfMalformed(STATIONS_FILE);
        resetFileIfMalformed(TRAINS_FILE);
        resetFileIfMalformed(TICKETS_FILE);

        // Se i file sono già popolati correttamente (non vuoti e validi), non caricare dati di esempio
        boolean stationsOk = isFilePopulated(STATIONS_FILE);
        boolean trainsOk = isFilePopulated(TRAINS_FILE);
        boolean ticketsOk = isFilePopulated(TICKETS_FILE);

        try {
            stations = loadStationsFromFile(STATIONS_FILE);
        } catch (Exception e) {
            System.out.println("Impossibile caricare le stazioni: " + e.getMessage());
            stations = new ArrayList<>();
        }

        try {
            trains = loadTrainsFromFile(TRAINS_FILE);
        } catch (Exception e) {
            System.out.println("Impossibile caricare i treni: " + e.getMessage());
            trains = new ArrayList<>();
        }

        try {
            tickets = loadTicketsFromFile(TICKETS_FILE);
        } catch (Exception e) {
            System.out.println("Impossibile caricare i biglietti: " + e.getMessage());
            tickets = new ArrayList<>();
        }

        // Popola dati di esempio solo se i file sono vuoti o non validi
        if (!stationsOk) populateExampleStations();
        if (!trainsOk) populateExampleTrains();
        if (!ticketsOk) populateExampleTickets();
    }

    private boolean isFilePopulated(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) return false;
            String content = Files.readString(Paths.get(filename)).trim();
            // Considera popolato se contiene almeno un oggetto (es: [{"..."}])
            return content.startsWith("[") && content.endsWith("]") && content.length() > 2;
        } catch (Exception e) {
            return false;
        }
    }

    // Se il file non è un array JSON valido, lo resetta a []
    private void resetFileIfMalformed(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) return;
            String content = Files.readString(Paths.get(filename)).trim();
            // Deve iniziare con [ e finire con ]
            if (!(content.startsWith("[") && content.endsWith("]"))) {
                Files.writeString(Paths.get(filename), "[]");
            }
        } catch (Exception e) {
            try {
                Files.writeString(Paths.get(filename), "[]");
            } catch (IOException ignored) {
            }
        }
    }

    private List<Station> loadStationsFromFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) {
            return new ArrayList<>();
        }
        List<Station> result = new ArrayList<>();
        // Usa una semplice regex per dividere gli oggetti JSON (più robusto per JSON standard)
        json = json.substring(1, json.length() - 1); // rimuove [ ]
        if (json.trim().isEmpty()) return result;
        String[] objects = json.split("},\\s*\\{");
        for (int i = 0; i < objects.length; i++) {
            String obj = objects[i];
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";
            try {
                Station.Builder builder = Station.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(obj, builder);
                result.add(builder.build());
            } catch (Exception e) {
                System.out.println("Errore parsing stazione: " + obj + " - " + e.getMessage());
            }
        }
        return result;
    }

    private List<Train> loadTrainsFromFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) {
            return new ArrayList<>();
        }
        List<Train> result = new ArrayList<>();
        json = json.substring(1, json.length() - 1); // rimuove [ ]
        if (json.trim().isEmpty()) return result;
        String[] objects = json.split("},\\s*\\{");
        for (int i = 0; i < objects.length; i++) {
            String obj = objects[i];
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";
            try {
                Train.Builder builder = Train.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(obj, builder);
                result.add(builder.build());
            } catch (Exception e) {
                System.out.println("Errore parsing treno: " + obj + " - " + e.getMessage());
            }
        }
        return result;
    }

    private List<Ticket> loadTicketsFromFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) {
            return new ArrayList<>();
        }
        List<Ticket> result = new ArrayList<>();
        json = json.substring(1, json.length() - 1); // rimuove [ ]
        if (json.trim().isEmpty()) return result;
        String[] objects = json.split("},\\s*\\{");
        for (int i = 0; i < objects.length; i++) {
            String obj = objects[i];
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";
            try {
                Ticket.Builder builder = Ticket.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(obj, builder);
                result.add(builder.build());
            } catch (Exception e) {
                System.out.println("Errore parsing biglietto: " + obj + " - " + e.getMessage());
            }
        }
        return result;
    }

    private void saveToFile(String filename, List<?> objects) throws IOException {
        File file = new File(filename);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        StringBuilder jsonArray = new StringBuilder("[");
        boolean first = true;
        for (Object obj : objects) {
            if (!first) {
                jsonArray.append(",");
            }
            String json = JsonFormat.printer().print((com.google.protobuf.MessageOrBuilder) obj);
            jsonArray.append(json);
            first = false;
        }
        jsonArray.append("]");
        Files.writeString(Paths.get(filename), jsonArray.toString());
    }

    public void saveData() {
        try {
            saveToFile(STATIONS_FILE, stations);
            saveToFile(TRAINS_FILE, trains);
            saveToFile(TICKETS_FILE, tickets);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dei dati: " + e.getMessage());
        }
    }

    public List<Station> getAllStations() {
        return stations;
    }

    public Station getStationById(int id) {
        return stations.stream()
                .filter(station -> station.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Station> searchStations(String query, int limit) {
        if (query == null || query.isEmpty()) {
            return stations.stream().limit(limit).collect(Collectors.toList());
        }
        String lowerQuery = query.toLowerCase();
        return stations.stream()
                .filter(station -> station.getName().toLowerCase().contains(lowerQuery) ||
                        station.getCity().toLowerCase().contains(lowerQuery))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Train> getAllTrains() {
        return trains;
    }

    public Train getTrainById(int id) {
        return trains.stream()
                .filter(train -> train.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Train> searchTrains(String departureStation, String arrivalStation, String date, int limit) {
        return trains.stream()
                .filter(train ->
                                (departureStation == null || departureStation.isEmpty() ||
                                        train.getDepartureStation().toLowerCase().contains(departureStation.toLowerCase())) &&
                                        (arrivalStation == null || arrivalStation.isEmpty() ||
                                                train.getArrivalStation().toLowerCase().contains(arrivalStation.toLowerCase()))
                        // Aggiungere filtro per data
                )
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Ticket> getAllTickets() {
        return tickets;
    }

    public Ticket getTicketById(String id) {
        return tickets.stream()
                .filter(ticket -> ticket.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void saveTicket(Ticket ticket) {
        boolean exists = tickets.stream()
                .anyMatch(t -> t.getId().equals(ticket.getId()));
        if (!exists) {
            tickets.add(ticket);
        } else {
            for (int i = 0; i < tickets.size(); i++) {
                if (tickets.get(i).getId().equals(ticket.getId())) {
                    tickets.set(i, ticket);
                    break;
                }
            }
        }
        saveData();
    }

    public void deleteTicket(String id) {
        tickets.removeIf(ticket -> ticket.getId().equals(id));
        saveData();
    }

    public void addStation(Station station) {
        boolean exists = stations.stream().anyMatch(s -> s.getId() == station.getId());
        if (!exists) {
            stations.add(station);
            saveData();
        }
    }

    public void addTrain(Train train) {
        boolean exists = trains.stream().anyMatch(t -> t.getId() == train.getId());
        if (!exists) {
            trains.add(train);
            saveData();
        }
    }

    public void addTicket(Ticket ticket) {
        boolean exists = tickets.stream().anyMatch(t -> t.getId().equals(ticket.getId()));
        if (!exists) {
            tickets.add(ticket);
            saveData();
        }
    }

    public void populateExampleStations() {
        stations.clear();
        addStation(Station.newBuilder().setId(1).setName("Roma Termini").setCity("Roma").build());
        addStation(Station.newBuilder().setId(2).setName("Milano Centrale").setCity("Milano").build());
        addStation(Station.newBuilder().setId(3).setName("Napoli Centrale").setCity("Napoli").build());
    }

    public void populateExampleTrains() {
        trains.clear();
        addTrain(Train.newBuilder()
                .setId(1)
                .setDepartureStation("Roma Termini")
                .setArrivalStation("Milano Centrale")
                .setDepartureTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .setArrivalTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 3600 * 3).build())
                .build());
        addTrain(Train.newBuilder()
                .setId(2)
                .setDepartureStation("Milano Centrale")
                .setArrivalStation("Napoli Centrale")
                .setDepartureTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 3600 * 4).build())
                .setArrivalTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() + 3600 * 7).build())
                .build());
    }

    public void populateExampleTickets() {
        tickets.clear();
        addTicket(Ticket.newBuilder()
                .setId("TICKET-1")
                .setTrainId(1)
                .setPassengerName("Mario Rossi")
                .setSeat("12A")
                .build());
        addTicket(Ticket.newBuilder()
                .setId("TICKET-2")
                .setTrainId(2)
                .setPassengerName("Giulia Bianchi")
                .setSeat("8B")
                .build());
    }

    public void populateAllExampleDataIfEmpty() {
        // Non popolare se i file sono già popolati (evita sovrascrittura manuale)
        if ((stations == null || stations.isEmpty()) && !isFilePopulated(STATIONS_FILE)) {
            populateExampleStations();
        }
        if ((trains == null || trains.isEmpty()) && !isFilePopulated(TRAINS_FILE)) {
            populateExampleTrains();
        }
        if ((tickets == null || tickets.isEmpty()) && !isFilePopulated(TICKETS_FILE)) {
            populateExampleTickets();
        }
    }
}
