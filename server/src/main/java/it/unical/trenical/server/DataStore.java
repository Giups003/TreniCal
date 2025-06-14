package it.unical.trenical.server;

import com.google.protobuf.util.JsonFormat;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.common.Station;
import it.unical.trenical.grpc.train.Route;
import it.unical.trenical.grpc.promotion.Promotion;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataStore {
    private static final String DATA_DIR = "C:/Users/Giuseppe/Documents/TreniCal/TreniCal/server/data";
    private static final String STATIONS_FILE = DATA_DIR + "/stations.json";
    private static final String TRAINS_FILE = DATA_DIR + "/trains.json";
    private static final String TICKETS_FILE = DATA_DIR + "/tickets.json";
    private static final String ROUTES_FILE = DATA_DIR + "/routes.json";
    private static final String PROMOTIONS_FILE = DATA_DIR + "/promotions.json";

    private static DataStore instance;
    private List<Station> stations = new ArrayList<>();
    private List<Train> trains = new ArrayList<>();
    private List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());
    private List<Route> routes = new ArrayList<>();
    private List<Promotion> promotions = new ArrayList<>();
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
        createEmptyJsonIfNotExist(PROMOTIONS_FILE);
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
        resetFileIfMalformed(PROMOTIONS_FILE);

        try {
            stations = loadStationsFromFile(STATIONS_FILE);
        } catch (Exception e) {
            System.out.println("Impossibile caricare le stazioni: " + e.getMessage());
            stations = new ArrayList<>();
        }

        // Pulisci i treni prima di rigenerarli
        trains.clear();
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

        try {
            promotions = loadPromotionsFromFile(PROMOTIONS_FILE);
        } catch (Exception e) {
            System.out.println("Impossibile caricare le promozioni: " + e.getMessage());
            promotions = new ArrayList<>();
        }

        generateTrainsForWeeks(2); // 2 settimane, treni ogni 2 ore
        saveData();

        for (Train t : trains) {
            trainSeatsAvailable.putIfAbsent(t.getId(), DEFAULT_SEATS_PER_TRAIN);
        }
    }

    /**
     * Genera treni per tutte le tratte, per un certo numero di settimane e corse ogni ora tra 6:00 e 22:00
     * @param weeks numero di settimane
     */
    private void generateTrainsForWeeks(int weeks) {
        if (routes == null || routes.isEmpty()) return;
        int maxId = trains.stream().mapToInt(Train::getId).max().orElse(0);
        var today = java.time.LocalDate.now();
        var rand = new java.util.Random();
        int firstHour = 7;
        int lastHour = 19;
        int step = 2;
        Set<String> uniqueTrainKeys = new HashSet<>();
        for (Route route : routes) {
            for (int w = 0; w < weeks; w++) {
                for (int d = 0; d < 7; d++) {
                    var date = today.plusDays(w * 7 + d);
                    for (int hour = firstHour; hour <= lastHour; hour += step) {
                        var departure = date.atTime(hour, 0);
                        var arrival = departure.plusMinutes(60 + rand.nextInt(60));
                        String routeKey = route.getName() + "_" +
                                getStationById(route.getDepartureStationId()).getName() + "_" +
                                getStationById(route.getArrivalStationId()).getName();
                        String key = routeKey + "_" + date + "_" + hour;
                        if (!uniqueTrainKeys.add(key)) continue;

                        Train train = Train.newBuilder()
                                .setId(++maxId)
                                .setName(route.getName())
                                .setDepartureStation(getStationById(route.getDepartureStationId()).getName())
                                .setArrivalStation(getStationById(route.getArrivalStationId()).getName())
                                .setDepartureTime(com.google.protobuf.Timestamp.newBuilder()
                                        .setSeconds(departure.toEpochSecond(java.time.ZoneOffset.UTC))
                                        .build())
                                .setArrivalTime(com.google.protobuf.Timestamp.newBuilder()
                                        .setSeconds(arrival.toEpochSecond(java.time.ZoneOffset.UTC))
                                        .build())
                                .build();
                        trains.add(train);
                    }
                }
            }
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
        List<Station> result = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return result;
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) return result;
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            try {
                Station.Builder builder = Station.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(obj.toString(), builder);
                result.add(builder.build());
            } catch (Exception e) {
                System.out.println("Errore parsing stazione: " + obj + " - " + e.getMessage());
            }
        }
        return result;
    }

    private List<Train> loadTrainsFromFile(String filename) throws IOException {
        List<Train> result = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return result;
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) return result;
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            try {
                Train.Builder builder = Train.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(obj.toString(), builder);
                result.add(builder.build());
            } catch (Exception e) {
                System.out.println("Errore parsing treno: " + obj + " - " + e.getMessage());
            }
        }
        return result;
    }

    private List<Ticket> loadTicketsFromFile(String filename) throws IOException {
        List<Ticket> result = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return result;
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) return result;
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            try {
                Ticket.Builder builder = Ticket.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(obj.toString(), builder);
                result.add(builder.build());
            } catch (Exception e) {
                System.out.println("Errore parsing biglietto: " + obj + " - " + e.getMessage());
            }
        }
        return result;
    }

    private List<Route> loadRoutesFromFile(String filename) throws IOException {
        List<Route> result = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return result;
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) return result;
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            try {
                Route.Builder builder = Route.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(obj.toString(), builder);
                result.add(builder.build());
            } catch (Exception e) {
                System.out.println("Errore parsing tratta: " + obj + " - " + e.getMessage());
            }
        }
        return result;
    }

    private List<Promotion> loadPromotionsFromFile(String filename) throws IOException {
        List<Promotion> result = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return result;
        String json = Files.readString(Paths.get(filename)).trim();
        if (json.equals("[]") || json.isEmpty()) return result;
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            try {
                Promotion.Builder builder = Promotion.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(obj.toString(), builder);
                result.add(builder.build());
            } catch (Exception e) {
                System.out.println("Errore parsing promozione: " + obj + " - " + e.getMessage());
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
                String json = JsonFormat.printer().print(r);
                jsonArray.append(json);
            } else if (obj instanceof Promotion p) {
                String json = JsonFormat.printer().print(p);
                jsonArray.append(json);
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
            saveToFile(PROMOTIONS_FILE, promotions);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dei dati: " + e.getMessage());
        }
    }

    // --- THREAD SAFETY SU TUTTE LE LISTE ---
    // RIMOSSI METODI DUPLICATI E NON USATI
    // --- METODI DI UPDATE (corretti, senza validazioni inutili) ---
    public synchronized void updateStation(Station updated) {
        if (updated == null || updated.getId() <= 0) return;
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).getId() == updated.getId()) {
                stations.set(i, updated);
                saveData();
                return;
            }
        }
    }
    public synchronized void updateTrain(Train updated) {
        if (updated == null || updated.getId() <= 0) return;
        for (int i = 0; i < trains.size(); i++) {
            if (trains.get(i).getId() == updated.getId()) {
                trains.set(i, updated);
                saveData();
                return;
            }
        }
    }
    public synchronized void updateTicket(Ticket updated) {
        if (updated == null || updated.getId() == null) return;
        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).getId().equals(updated.getId())) {
                tickets.set(i, updated);
                saveData();
                return;
            }
        }
    }
    public synchronized void updateRoute(Route updated) {
        if (updated == null || updated.getId() <= 0) return;
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getId() == updated.getId()) {
                routes.set(i, updated);
                saveData();
                return;
            }
        }
    }
    public synchronized void updatePromotion(Promotion updated) {
        if (updated == null || updated.getId() <= 0) return;
        for (int i = 0; i < promotions.size(); i++) {
            if (promotions.get(i).getId() == updated.getId()) {
                promotions.set(i, updated);
                saveData();
                return;
            }
        }
    }
    // --- GESTIONE ID CENTRALIZZATA ---
    private synchronized int generateNextId(List<?> list, java.util.function.ToIntFunction<Object> idGetter) {
        return list.stream().mapToInt(idGetter).max().orElse(0) + 1;
    }
    public synchronized int generateNextStationId() { return generateNextId(stations, s -> ((Station)s).getId()); }
    public synchronized int generateNextTrainId() { return generateNextId(trains, t -> ((Train)t).getId()); }
    public synchronized int generateNextRouteId() { return generateNextId(routes, r -> ((Route)r).getId()); }
    public synchronized int generateNextPromotionId() { return generateNextId(promotions, p -> ((Promotion)p).getId()); }
    // --- BACKUP/RESTORE ---
    /**
     * Esporta tutti i dati in una stringa JSON.
     */
    public synchronized String exportAllData() {
        JSONObject obj = new JSONObject();
        obj.put("stations", stations);
        obj.put("trains", trains);
        obj.put("tickets", tickets);
        obj.put("routes", routes);
        obj.put("promotions", promotions);
        return obj.toString();
    }
    /**
     * Importa tutti i dati da una stringa JSON.
     */
    public synchronized void importAllData(String json) {
        // Placeholder: implementazione da completare
    }
    // --- LAZY LOADING E CACHE (ESEMPIO SU PROMOTIONS) ---
    private boolean promotionsLoaded = false;
    public synchronized List<Promotion> getPromotionsLazy() {
        if (!promotionsLoaded) {
            try { promotions = loadPromotionsFromFile(PROMOTIONS_FILE); promotionsLoaded = true; }
            catch (Exception e) { promotions = new ArrayList<>(); }
        }
        return new ArrayList<>(promotions);
    }

    // --- METODI PUBBLICI DI ACCESSO E GESTIONE ---
    public synchronized List<Station> getAllStations() {
        return new ArrayList<>(stations);
    }
    public synchronized Station getStationById(int id) {
        return stations.stream().filter(station -> station.getId() == id).findFirst().orElse(null);
    }
    public synchronized void addStation(Station station) {
        if (station == null || station.getId() <= 0) return;
        boolean exists = stations.stream().anyMatch(s -> s.getId() == station.getId());
        if (!exists) {
            stations.add(station);
            saveData();
        }
    }
    public synchronized void deleteStation(int id) {
        stations.removeIf(s -> s.getId() == id);
        saveData();
    }
    public synchronized List<Station> searchStations(String query, int limit) {
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

    public synchronized List<Train> getAllTrains() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return generateTrainsForDay(null, null, today);
    }
    public synchronized Train getTrainById(int id) {
        java.time.LocalDate today = java.time.LocalDate.now();
        return generateTrainsForDay(null, null, today).stream()
                .filter(train -> train.getId() == id)
                .findFirst()
                .orElse(null);
    }
    public synchronized void addTrain(Train train) {
        if (train == null || train.getId() <= 0) return;
        boolean exists = trains.stream().anyMatch(t -> t.getId() == train.getId());
        if (!exists) {
            trains.add(train);
            saveData();
        }
    }
    public synchronized void deleteTrain(int id) {
        trains.removeIf(t -> t.getId() == id);
        saveData();
    }

    public synchronized List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets);
    }
    public synchronized Ticket getTicketById(String id) {
        return tickets.stream().filter(ticket -> ticket.getId().equals(id)).findFirst().orElse(null);
    }
    public synchronized void addTicket(Ticket ticket) {
        if (ticket == null || ticket.getId() == null) return;
        boolean exists = tickets.stream().anyMatch(t -> t.getId().equals(ticket.getId()));
        if (!exists) {
            tickets.add(ticket);
            saveData();
        }
    }
    public synchronized void deleteTicket(String id) {
        Ticket toRemove = null;
        for (Ticket t : tickets) {
            if (t.getId().equals(id)) {
                toRemove = t;
                break;
            }
        }
        if (toRemove != null) {
            tickets.remove(toRemove);
            int trainId = toRemove.getTrainId();
            int seats = 1;
            try { seats = Integer.parseInt(toRemove.getSeat()); } catch (Exception ignored) {}
            incrementSeat(trainId, seats); // Usa incrementSeat per ripristinare i posti
            saveData();
        }
    }

    public synchronized List<Route> getAllRoutes() {
        return new ArrayList<>(routes);
    }
    public synchronized Route getRouteById(int id) {
        return routes.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }
    public synchronized void addRoute(Route route) {
        if (route == null || route.getId() <= 0) return;
        boolean exists = routes.stream().anyMatch(r -> r.getId() == route.getId());
        if (!exists) {
            routes.add(route);
            saveData();
        }
    }
    public synchronized void deleteRoute(int id) {
        routes.removeIf(r -> r.getId() == id);
        saveData();
    }

    public synchronized List<Promotion> getAllPromotions() {
        return new ArrayList<>(promotions);
    }
    public synchronized Promotion getPromotionById(int id) {
        return promotions.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }
    public synchronized void addPromotion(Promotion promotion) {
        if (promotion == null || promotion.getId() <= 0) return;
        boolean exists = promotions.stream().anyMatch(p -> p.getId() == promotion.getId());
        if (!exists) {
            promotions.add(promotion);
            saveData();
        }
    }
    public synchronized void deletePromotion(int id) {
        promotions.removeIf(p -> p.getId() == id);
        saveData();
    }

    // --- GENERAZIONE DINAMICA TRENI E RICERCA ---
    public synchronized List<Train> generateTrainsForDay(String departureStation, String arrivalStation, java.time.LocalDate date) {
        List<Train> result = new ArrayList<>();
        for (Train t : trains) {
            boolean matchDep = (departureStation == null || departureStation.isEmpty() || t.getDepartureStation().toLowerCase().contains(departureStation.toLowerCase()));
            boolean matchArr = (arrivalStation == null || arrivalStation.isEmpty() || t.getArrivalStation().toLowerCase().contains(arrivalStation.toLowerCase()));
            java.time.LocalDate depDate = java.time.Instant.ofEpochSecond(t.getDepartureTime().getSeconds())
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            if (matchDep && matchArr && depDate.equals(date)) {
                result.add(t);
            }
        }
        return result;
    }
    public synchronized List<Train> searchTrains(String departureStation, String arrivalStation, String date, String trainType, int limit) {
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
        // Filtro per tipologia se specificata e diversa da null/vuota
        if (trainType != null && !trainType.isEmpty() && !trainType.equalsIgnoreCase("Tutti")) {
            found = found.stream()
                    .filter(t -> t.getName().toLowerCase().contains(trainType.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (limit > 0 && found.size() > limit) {
            return found.subList(0, limit);
        }
        return found;
    }

    // --- GESTIONE POSTI DISPONIBILI ---
    public synchronized int getAvailableSeats(int trainId) {
        // Se il treno non è presente nella mappa, inizializza con il valore di default
        return trainSeatsAvailable.computeIfAbsent(trainId, k -> DEFAULT_SEATS_PER_TRAIN);
    }
    public synchronized boolean decrementSeat(int trainId, int seats) {
        int available = getAvailableSeats(trainId);
        if (available >= seats) {
            trainSeatsAvailable.put(trainId, available - seats);
            return true;
        }
        return false;
    }
    public synchronized void incrementSeat(int trainId, int seats) {
        // Usa solo per cancellazione/annullamento biglietto
        int available = getAvailableSeats(trainId);
        trainSeatsAvailable.put(trainId, available + seats);
    }

    // --- PROMOZIONI ---
    public synchronized Promotion findBestPromotion(String routeName, String serviceClass, java.time.LocalDate travelDate) {
        // Implementazione da aggiornare se serve, ora non più usata direttamente
        return null;
    }

    public synchronized void clearAllTickets() {
        tickets.clear();
        // Reset posti disponibili per ogni treno
        for (Train t : trains) {
            trainSeatsAvailable.put(t.getId(), DEFAULT_SEATS_PER_TRAIN);
        }
        saveData();
    }
}
