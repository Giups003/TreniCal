package it.unical.trenical.server;

import com.google.protobuf.util.JsonFormat;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.common.Station;
import com.google.protobuf.Timestamp;
import it.unical.trenical.server.Route;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DataStore {
    private static final String DATA_DIR = "C:/Users/Giuseppe/Documents/TreniCal/TreniCal/server/data";
    private static final String STATIONS_FILE = DATA_DIR + "/stations.json";
    private static final String TRAINS_FILE = DATA_DIR + "/trains.json";
    private static final String TICKETS_FILE = DATA_DIR + "/tickets.json";
    private static final String ROUTES_FILE = DATA_DIR + "/routes.json";

    private static DataStore instance;
    private List<Station> stations = new ArrayList<>();
    private List<Train> trains = new ArrayList<>();
    private List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());
    private List<Route> routes = new ArrayList<>();
    private final Map<Integer, Integer> trainSeatsAvailable = new ConcurrentHashMap<>();
    private static final int DEFAULT_SEATS_PER_TRAIN = 50;

    private DataStore() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                System.err.println("Impossibile creare la cartella dati: " + DATA_DIR);
            }
        }
        createJsonFilesIfNotExist();
        loadData();
    }

    private void createJsonFilesIfNotExist() {
        createEmptyJsonIfNotExist(STATIONS_FILE);
        createEmptyJsonIfNotExist(TRAINS_FILE);
        createEmptyJsonIfNotExist(TICKETS_FILE);
        createEmptyJsonIfNotExist(ROUTES_FILE);
    }

    private void createEmptyJsonIfNotExist(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        System.err.println("Impossibile creare la cartella: " + dir.getAbsolutePath());
                    }
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
        resetFileIfMalformed(STATIONS_FILE);
        resetFileIfMalformed(TRAINS_FILE);
        resetFileIfMalformed(TICKETS_FILE);
        resetFileIfMalformed(ROUTES_FILE);

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

        // Inizializza i posti disponibili per ogni treno
        for (Train t : trains) {
            trainSeatsAvailable.putIfAbsent(t.getId(), DEFAULT_SEATS_PER_TRAIN);
        }

        try {
            tickets = loadTicketsFromFile(TICKETS_FILE);
        } catch (Exception e) {
            System.out.println("Impossibile caricare i biglietti: " + e.getMessage());
            tickets = new ArrayList<>();
        }

        try {
            routes = loadRoutesFromFile(ROUTES_FILE);
        } catch (Exception e) {
            System.out.println("Impossibile caricare le tratte: " + e.getMessage());
            routes = new ArrayList<>();
        }
    }

    private void resetFileIfMalformed(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) return;
            String content = Files.readString(Paths.get(filename)).trim();
            if (!(content.startsWith("[") && content.endsWith("]"))) {
                Files.writeString(Paths.get(filename), "[]");
            }
        } catch (Exception e) {
            try {
                Files.writeString(Paths.get(filename), "[]");
            } catch (IOException ignored) {}
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
        json = json.substring(1, json.length() - 1);
        if (json.trim().isEmpty()) return result;
        String[] objects = json.split("},\\s*\\{");
        for (String obj : objects) {
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
        json = json.substring(1, json.length() - 1);
        if (json.trim().isEmpty()) return result;
        String[] objects = json.split("},\\s*\\{");
        for (String obj : objects) {
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
        json = json.substring(1, json.length() - 1);
        if (json.trim().isEmpty()) return result;
        String[] objects = json.split("},\\s*\\{");
        for (String obj : objects) {
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

    private List<Route> loadRoutesFromFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) {
            return new ArrayList<>();
        }
        List<Route> result = new ArrayList<>();
        // Rimuovi parentesi quadre iniziali/finali
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1).trim();
        }
        if (json.isEmpty()) return result;
        // Split robusto sugli oggetti JSON
        String[] objects = json.split("(?<=\\}),\\s*(?=\\{)");
        for (String obj : objects) {
            obj = obj.trim();
            try {
                // Parsing manuale senza org.json né Gson
                // Rimuovi eventuali virgole finali
                if (obj.endsWith(",")) obj = obj.substring(0, obj.length() - 1);
                // Rimuovi parentesi graffe
                if (obj.startsWith("{")) obj = obj.substring(1);
                if (obj.endsWith("}")) obj = obj.substring(0, obj.length() - 1);
                String[] fields = obj.split(",\\s*\"?");
                Route r = new Route();
                for (String field : fields) {
                    String[] kv = field.split(":", 2);
                    if (kv.length != 2) continue;
                    String key = kv[0].replaceAll("[\"{}]", "").trim();
                    String value = kv[1].replaceAll("[\"{}]", "").trim();
                    switch (key) {
                        case "id": r.id = Integer.parseInt(value); break;
                        case "name": r.name = value; break;
                        case "departureStationId": r.departureStationId = Integer.parseInt(value); break;
                        case "arrivalStationId": r.arrivalStationId = Integer.parseInt(value); break;
                        case "departureTime": r.departureTime = value; break;
                        case "arrivalTime": r.arrivalTime = value; break;
                    }
                }
                result.add(r);
            } catch (Exception e) {
                System.out.println("Errore parsing tratta: " + obj + " - " + e.getMessage());
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
            if (obj instanceof Route r) {
                jsonArray.append("{\"id\":" + r.id + ",\"name\":\"" + r.name + "\",\"departureStationId\":" + r.departureStationId + ",\"arrivalStationId\":" + r.arrivalStationId + ",\"departureTime\":\"" + r.departureTime + "\",\"arrivalTime\":\"" + r.arrivalTime + "\"}");
            } else {
                String json = JsonFormat.printer().print((com.google.protobuf.MessageOrBuilder) obj);
                jsonArray.append(json);
            }
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
            saveToFile(ROUTES_FILE, routes);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dei dati: " + e.getMessage());
        }
    }

    public List<Station> getAllStations() { return stations; }
    public Station getStationById(int id) {
        return stations.stream().filter(station -> station.getId() == id).findFirst().orElse(null);
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
        // Genera i treni dinamicamente dalle tratte per la data odierna
        java.time.LocalDate today = java.time.LocalDate.now();
        return generateTrainsForDay(null, null, today);
    }

    public Train getTrainById(int id) {
        java.time.LocalDate today = java.time.LocalDate.now();
        return generateTrainsForDay(null, null, today).stream()
                .filter(train -> train.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Ticket> getAllTickets() { return tickets; }
    public Ticket getTicketById(String id) {
        return tickets.stream().filter(ticket -> ticket.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Route> getAllRoutes() { return routes; }
    public Route getRouteById(int id) {
        return routes.stream().filter(r -> r.id == id).findFirst().orElse(null);
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
        synchronized (tickets) {
            boolean exists = tickets.stream().anyMatch(t -> t.getId().equals(ticket.getId()));
            if (!exists) {
                tickets.add(ticket);
                saveData();
            }
        }
    }
    public void addRoute(Route route) {
        boolean exists = routes.stream().anyMatch(r -> r.id == route.id);
        if (!exists) {
            routes.add(route);
            saveData();
        }
    }
    public void deleteTicket(String id) {
        Ticket toRemove = null;
        synchronized (tickets) {
            for (Ticket t : tickets) {
                if (t.getId().equals(id)) {
                    toRemove = t;
                    break;
                }
            }
            if (toRemove != null) {
                tickets.remove(toRemove);
                // Ripristina i posti disponibili per il treno associato
                int trainId = toRemove.getTrainId();
                int seats = 1;
                try { seats = Integer.parseInt(toRemove.getSeat()); } catch (Exception ignored) {}
                trainSeatsAvailable.put(trainId, trainSeatsAvailable.getOrDefault(trainId, DEFAULT_SEATS_PER_TRAIN) + seats);
                saveData();
            }
        }
    }
    public void deleteRoute(int id) {
        routes.removeIf(r -> r.id == id);
        saveData();
    }

    public List<Train> generateTrainsForDay(String departureStation, String arrivalStation, java.time.LocalDate date) {
        List<Train> result = new ArrayList<>();
        for (Route r : routes) {
            Station dep = getStationById(r.departureStationId);
            Station arr = getStationById(r.arrivalStationId);
            if (dep == null || arr == null) continue;
            if ((departureStation == null || departureStation.isEmpty() || dep.getName().toLowerCase().contains(departureStation.toLowerCase())) &&
                (arrivalStation == null || arrivalStation.isEmpty() || arr.getName().toLowerCase().contains(arrivalStation.toLowerCase()))) {
                java.time.LocalTime depTime = java.time.LocalTime.parse(r.departureTime);
                java.time.LocalTime arrTime = java.time.LocalTime.parse(r.arrivalTime);
                java.time.LocalDateTime depDateTime = java.time.LocalDateTime.of(date, depTime);
                java.time.LocalDateTime arrDateTime = java.time.LocalDateTime.of(date, arrTime);
                Train t = Train.newBuilder()
                    .setId(r.id)
                    .setName(r.name)
                    .setDepartureStation(dep.getName())
                    .setArrivalStation(arr.getName())
                    .setDepartureTime(Timestamp.newBuilder().setSeconds(depDateTime.toEpochSecond(java.time.ZoneOffset.UTC)).build())
                    .setArrivalTime(Timestamp.newBuilder().setSeconds(arrDateTime.toEpochSecond(java.time.ZoneOffset.UTC)).build())
                    .build();
                result.add(t);
            }
        }
        return result;
    }

    // Cerca treni in base a partenza, arrivo, data e limite
    public List<Train> searchTrains(String departureStation, String arrivalStation, String date, int limit) {
        java.time.LocalDate searchDate;
        if (date == null || date.isEmpty()) {
            searchDate = java.time.LocalDate.now();
        } else {
            try {
                searchDate = java.time.LocalDate.parse(date);
            } catch (Exception e) {
                searchDate = java.time.LocalDate.now();
            }
        }
        List<Train> found = generateTrainsForDay(departureStation, arrivalStation, searchDate);
        if (limit > 0 && found.size() > limit) {
            return found.subList(0, limit);
        }
        return found;
    }

    // Ottieni i posti disponibili per un treno
    public int getAvailableSeats(int trainId) {
        return trainSeatsAvailable.getOrDefault(trainId, DEFAULT_SEATS_PER_TRAIN);
    }

    // Decrementa i posti disponibili in modo thread-safe, restituisce true se riuscito
    public synchronized boolean decrementSeat(int trainId, int seats) {
        int available = trainSeatsAvailable.getOrDefault(trainId, DEFAULT_SEATS_PER_TRAIN);
        if (available >= seats) {
            trainSeatsAvailable.put(trainId, available - seats);
            return true;
        }
        return false;
    }

    // Incrementa i posti disponibili in modo thread-safe
    public synchronized void incrementSeat(int trainId, int seats) {
        int available = trainSeatsAvailable.getOrDefault(trainId, DEFAULT_SEATS_PER_TRAIN);
        trainSeatsAvailable.put(trainId, available + seats);
    }
}
