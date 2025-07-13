package it.unical.trenical.server.strategy;

import com.google.protobuf.Timestamp;
import it.unical.trenical.server.DataStore;
import it.unical.trenical.grpc.promotion.Promotion;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN STRATEGY - STRATEGIA CONCRETA PER CLIENTI STANDARD
 * Implementa la logica di calcolo prezzo per clienti standard.
 *
 * Caratteristiche:
 * - Prezzo base calcolato su distanza e classe
 * - Gestione codici promo tramite database delle promozioni
 * - Sconti dinamici basati su promozioni attive
 */
public class StandardPriceCalculationStrategy implements PriceCalculationStrategy {

    private static final double BASE_PRICE_PER_KM = 0.15;
    private static final double FIRST_CLASS_MULTIPLIER = 1.8;
    private static final double PREMIUM_TRAIN_MULTIPLIER = 1.5;

    // Database delle distanze (condiviso)
    private final Map<String, Map<String, Integer>> distanceMap;

    // Database legacy dei codici promo (manteniamo per compatibilità)
    private final Map<String, Double> legacyPromoCodeDiscounts;

    private String userType = "standard";

    public StandardPriceCalculationStrategy(Map<String, Map<String, Integer>> distanceMap,
                                           Map<String, Double> promoCodeDiscounts) {
        this.distanceMap = distanceMap != null ? distanceMap : new HashMap<>();
        this.legacyPromoCodeDiscounts = promoCodeDiscounts != null ? promoCodeDiscounts : new HashMap<>();

        // Inizializza dati se necessario
        if (this.distanceMap.isEmpty()) {
            initializeDistanceMap();
        }
        if (this.legacyPromoCodeDiscounts.isEmpty()) {
            initializeLegacyPromoCodes();
        }
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public double calculateTicketPrice(String departureStation, String arrivalStation,
                                     String serviceClass, Timestamp travelDate,
                                     String promoCode, String trainType) {

        // 1. Calcola prezzo base
        double basePrice = calculateBasePrice(departureStation, arrivalStation, serviceClass, trainType);

        // 2. Applica sconto promozionale se presente
        double finalPrice = applyPromotionalDiscount(basePrice, promoCode, departureStation,
                                                    arrivalStation, serviceClass, travelDate, trainType);

        System.out.println("[STANDARD STRATEGY] Prezzo base: " + basePrice +
                          ", Prezzo finale: " + finalPrice +
                          ", Promo applicata: " + (promoCode != null ? promoCode : "nessuna"));

        return Math.max(finalPrice, 5.0); // Prezzo minimo 5€
    }

    /**
     * Calcola il prezzo base senza sconti promozionali
     */
    private double calculateBasePrice(String departureStation, String arrivalStation,
                                    String serviceClass, String trainType) {

        // Calcola distanza
        int distance = getDistance(departureStation, arrivalStation);
        if (distance == 0) {
            distance = 100; // Distanza default se non trovata
        }

        // Prezzo base
        double price = distance * BASE_PRICE_PER_KM;

        // Moltiplicatore per classe
        if ("Prima Classe".equalsIgnoreCase(serviceClass)) {
            price *= FIRST_CLASS_MULTIPLIER;
        }

        // Moltiplicatore per tipo treno premium
        if (trainType != null && isPremiumTrain(trainType)) {
            price *= PREMIUM_TRAIN_MULTIPLIER;
        }

        return Math.round(price * 100.0) / 100.0; // Arrotonda a 2 decimali
    }

    /**
     * Applica sconti promozionali usando il sistema delle promozioni del DataStore
     */
    private double applyPromotionalDiscount(double basePrice, String promoCode,
                                          String departureStation, String arrivalStation,
                                          String serviceClass, Timestamp travelDate, String trainType) {

        if (promoCode == null || promoCode.trim().isEmpty()) {
            return basePrice;
        }

        // Prima controlla nel nuovo sistema delle promozioni (priorità alta)
        double discountFromNewSystem = getDiscountFromPromotionSystem(promoCode, departureStation,
                                                                     arrivalStation, serviceClass,
                                                                     travelDate, trainType);

        if (discountFromNewSystem > 0) {
            double discountedPrice = basePrice * (1.0 - discountFromNewSystem);
            System.out.println("[PROMO] Sconto applicato dal sistema promozioni: " +
                              (discountFromNewSystem * 100) + "%");
            return discountedPrice;
        }

        // Fallback: controlla nel sistema legacy
        Double legacyDiscount = legacyPromoCodeDiscounts.get(promoCode.toUpperCase());
        if (legacyDiscount != null && legacyDiscount > 0) {
            double discountedPrice = basePrice * (1.0 - legacyDiscount);
            System.out.println("[PROMO] Sconto applicato dal sistema legacy: " +
                              (legacyDiscount * 100) + "%");
            return discountedPrice;
        }

        System.out.println("[PROMO] Codice '" + promoCode + "' non valido o scaduto");
        return basePrice; // Nessuno sconto applicabile
    }

    /**
     * Ottiene sconto dal nuovo sistema delle promozioni
     */
    private double getDiscountFromPromotionSystem(String promoCode, String departureStation,
                                                String arrivalStation, String serviceClass,
                                                Timestamp travelDate, String trainType) {
        try {
            DataStore dataStore = DataStore.getInstance();

            // Converte parametri
            String routeName = departureStation + "-" + arrivalStation;
            LocalDate travelLocalDate = null;
            if (travelDate != null) {
                travelLocalDate = Instant.ofEpochSecond(travelDate.getSeconds())
                                        .atZone(ZoneId.systemDefault()).toLocalDate();
            }

            // Cerca promozione per codice specifico
            for (Promotion promo : dataStore.getAllPromotions()) {
                if (promo.getName().equalsIgnoreCase(promoCode.trim())) {

                    // Verifica applicabilità
                    boolean routeOk = promo.getRouteNamesList().isEmpty() ||
                                     promo.getRouteNamesList().contains(routeName);

                    boolean classOk = promo.getServiceClassesList().isEmpty() ||
                                     promo.getServiceClassesList().contains(serviceClass);

                    boolean typeOk = promo.getTrainType().isEmpty() ||
                                    promo.getTrainType().equalsIgnoreCase(trainType);

                    boolean dateOk = true;
                    if (travelLocalDate != null) {
                        if (promo.hasValidFrom()) {
                            LocalDate validFrom = Instant.ofEpochSecond(promo.getValidFrom().getSeconds())
                                                        .atZone(ZoneId.systemDefault()).toLocalDate();
                            dateOk = !travelLocalDate.isBefore(validFrom);
                        }
                        if (dateOk && promo.hasValidTo()) {
                            LocalDate validTo = Instant.ofEpochSecond(promo.getValidTo().getSeconds())
                                                      .atZone(ZoneId.systemDefault()).toLocalDate();
                            dateOk = !travelLocalDate.isAfter(validTo);
                        }
                    }

                    if (routeOk && classOk && typeOk && dateOk) {
                        return promo.getDiscountPercent() / 100.0; // Converte percentuale in decimale
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[PROMO ERROR] Errore nel sistema promozioni: " + e.getMessage());
        }

        return 0.0; // Nessuno sconto trovato
    }

    @Override
    public boolean isValidPromoCode(String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return false;
        }

        String code = promoCode.toUpperCase().trim();
        String type = userType != null ? userType.toLowerCase() : "standard";

        // Verifica nel database delle promozioni per determinare i requisiti
        try {
            DataStore dataStore = DataStore.getInstance();
            for (Promotion promo : dataStore.getAllPromotions()) {
                if (promo.getName().equalsIgnoreCase(code)) {

                    // VALIDAZIONE BASATA SUI DATI DELLA PROMOZIONE:

                    // 1. NUOVO: Controllo esplicito sui tipi di utenti ammessi
                    if (!promo.getUserTypesList().isEmpty() && !promo.getUserTypesList().contains(type)) {
                        System.out.println("[STANDARD STRATEGY] Promozione '" + code + "' rifiutata: tipo utente '" + type + "' non in lista ammessi: " + promo.getUserTypesList());
                        return false;
                    }

                    // 2. Se la promozione è riservata ai membri loyalty (programma fedeltà)
                    if (promo.getOnlyForLoyaltyMembers()) {
                        // Per ora assumiamo che gli utenti VIP siano automaticamente nel programma fedeltà
                        boolean isLoyaltyMember = type.equals("vip");
                        if (!isLoyaltyMember) {
                            System.out.println("[STANDARD STRATEGY] Promozione loyalty '" + code + "' rifiutata: utente non è membro fedeltà");
                            return false;
                        }
                    }

                    // 3. Se la promozione ha restrizioni specifiche sui tipi di treno business
                    String trainTypeRestriction = promo.getTrainType();
                    if (trainTypeRestriction != null && !trainTypeRestriction.isEmpty()) {
                        if (isBusinessTrainType(trainTypeRestriction) && type.equals("standard")) {
                            System.out.println("[STANDARD STRATEGY] Promozione business '" + code + "' (treni: " + trainTypeRestriction + ") rifiutata per utente standard");
                            return false;
                        }
                    }

                    System.out.println("[STANDARD STRATEGY] Promozione '" + code + "' validata per utente " + type);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[STANDARD STRATEGY] Errore verifica promozioni: " + e.getMessage());
        }

        // Verifica nei codici legacy (solo per compatibilità e codici generali)
        boolean isLegacyValid = legacyPromoCodeDiscounts.containsKey(code);

        if (isLegacyValid) {
            System.out.println("[STANDARD STRATEGY] Codice legacy validato per utente " + type + ": " + code);
        } else {
            System.out.println("[STANDARD STRATEGY] Codice '" + code + "' non valido o non trovato");
        }

        return isLegacyValid;
    }

    /**
     * Determina se un tipo di treno è considerato "business" (premium)
     */
    private boolean isBusinessTrainType(String trainType) {
        if (trainType == null || trainType.isEmpty()) return false;

        String lowerType = trainType.toLowerCase();
        return lowerType.contains("frecciarossa") ||
               lowerType.contains("frecciargento") ||
               lowerType.contains("italo") ||
               lowerType.contains("premium") ||
               lowerType.contains("business") ||
               lowerType.contains("executive");
    }

    /**
     * Calcola la distanza tra due stazioni
     */
    private int getDistance(String from, String to) {
        // Normalizza nomi stazioni
        from = normalizeStationName(from);
        to = normalizeStationName(to);

        if (from.equals(to)) {
            return 0;
        }

        // Cerca distanza diretta
        Map<String, Integer> fromDistances = distanceMap.get(from);
        if (fromDistances != null && fromDistances.containsKey(to)) {
            return fromDistances.get(to);
        }

        // Cerca distanza inversa
        Map<String, Integer> toDistances = distanceMap.get(to);
        if (toDistances != null && toDistances.containsKey(from)) {
            return toDistances.get(from);
        }

        // Stima basata sui nomi (fallback)
        return estimateDistance(from, to);
    }

    private String normalizeStationName(String stationName) {
        if (stationName == null) return "";

        return stationName.trim()
                         .replace(" Centrale", "")
                         .replace(" Termini", "")
                         .replace(" SMN", "");
    }

    private int estimateDistance(String from, String to) {
        // Stima semplice basata su posizione geografica approssimativa
        Map<String, Integer> coordinates = Map.of(
            "Roma", 0,
            "Milano", 570,
            "Napoli", -220,
            "Firenze", 270,
            "Bologna", 370,
            "Torino", 670,
            "Venezia", 530,
            "Bari", -400
        );

        Integer fromCoord = coordinates.get(from);
        Integer toCoord = coordinates.get(to);

        if (fromCoord != null && toCoord != null) {
            return Math.abs(fromCoord - toCoord);
        }

        return 200; // Distanza media di default
    }

    private boolean isPremiumTrain(String trainType) {
        if (trainType == null) return false;

        String type = trainType.toLowerCase();
        return type.contains("frecciarossa") ||
               type.contains("italo") ||
               type.contains("frecciargento");
    }

    private void initializeDistanceMap() {
        // Carica dinamicamente le stazioni dal DataStore e calcola le distanze
        try {
            DataStore dataStore = DataStore.getInstance();
            var stations = dataStore.getAllStations();

            // Per ogni stazione, calcola la distanza verso tutte le altre
            for (var fromStation : stations) {
                Map<String, Integer> distances = new HashMap<>();
                String fromName = normalizeStationName(fromStation.getName());

                for (var toStation : stations) {
                    if (fromStation.getId() != toStation.getId()) {
                        String toName = normalizeStationName(toStation.getName());

                        // Calcola distanza usando coordinate geografiche
                        double distance = calculateHaversineDistance(
                            fromStation.getLatitude(), fromStation.getLongitude(),
                            toStation.getLatitude(), toStation.getLongitude()
                        );

                        // Converte da km a intero arrotondato
                        distances.put(toName, (int) Math.round(distance));
                    }
                }

                distanceMap.put(fromName, distances);
            }

            System.out.println("[DISTANCE MAP] Mappa distanze inizializzata con " +
                             distanceMap.size() + " stazioni dal database");

        } catch (Exception e) {
            System.err.println("[DISTANCE MAP] Errore nel caricamento stazioni: " + e.getMessage());
            // Fallback ai valori hardcodati in caso di errore
            initializeHardcodedDistances();
        }
    }

    /**
     * Calcola la distanza tra due punti geografici usando la formula di Haversine
     * @param lat1 Latitudine punto 1
     * @param lon1 Longitudine punto 1
     * @param lat2 Latitudine punto 2
     * @param lon2 Longitudine punto 2
     * @return Distanza in chilometri
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Raggio della Terra in km

        // Converte gradi in radianti
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Differenze
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Formula di Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distanza in km
    }

    /**
     * Metodo di fallback con distanze hardcodated per compatibilità
     */
    private void initializeHardcodedDistances() {
        System.out.println("[DISTANCE MAP] Usando distanze hardcodated come fallback");

        // Roma
        Map<String, Integer> romaDistances = new HashMap<>();
        romaDistances.put("Milano", 570);
        romaDistances.put("Napoli", 220);
        romaDistances.put("Firenze", 270);
        romaDistances.put("Bologna", 370);
        romaDistances.put("Torino", 670);
        romaDistances.put("Venezia", 530);
        romaDistances.put("Bari", 400);
        distanceMap.put("Roma", romaDistances);

        // Milano
        Map<String, Integer> milanoDistances = new HashMap<>();
        milanoDistances.put("Roma", 570);
        milanoDistances.put("Torino", 140);
        milanoDistances.put("Bologna", 210);
        milanoDistances.put("Firenze", 300);
        milanoDistances.put("Venezia", 270);
        milanoDistances.put("Napoli", 790);
        distanceMap.put("Milano", milanoDistances);

        // Napoli
        Map<String, Integer> napoliDistances = new HashMap<>();
        napoliDistances.put("Roma", 220);
        napoliDistances.put("Milano", 790);
        napoliDistances.put("Bari", 260);
        napoliDistances.put("Firenze", 490);
        distanceMap.put("Napoli", napoliDistances);
    }

    private void initializeLegacyPromoCodes() {
        legacyPromoCodeDiscounts.put("ESTATE2025", 0.15);
        legacyPromoCodeDiscounts.put("WEEKEND", 0.10);
        legacyPromoCodeDiscounts.put("STUDENT", 0.20);
        legacyPromoCodeDiscounts.put("FAMILY", 0.12);
    }
}
