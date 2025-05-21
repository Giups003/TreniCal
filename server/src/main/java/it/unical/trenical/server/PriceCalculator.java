package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe per il calcolo del prezzo dei biglietti ferroviari.
 * Implementa una logica modulare per calcolare i prezzi in base a vari fattori:
 * - Distanza tra stazioni
 * - Classe di servizio
 * - Giorno della settimana
 * - Anticipo di prenotazione
 * - Codici promozionali
 */
public class PriceCalculator {

    // Costanti per il calcolo del prezzo
    private static final double BASE_PRICE_PER_KM = 0.15; // Prezzo base per km
    private static final double MIN_PRICE = 5.0; // Prezzo minimo per qualsiasi biglietto

    // Moltiplicatori per classe di servizio
    private static final double ECONOMY_MULTIPLIER = 1.0;
    private static final double STANDARD_MULTIPLIER = 1.3;
    private static final double FIRST_CLASS_MULTIPLIER = 1.8;
    private static final double BUSINESS_MULTIPLIER = 2.5;

    // Moltiplicatori per giorno della settimana (weekend più costoso)
    private static final double WEEKDAY_MULTIPLIER = 1.0;
    private static final double WEEKEND_MULTIPLIER = 1.2;

    // Sconti per prenotazione anticipata
    private static final double ADVANCE_30_DAYS_DISCOUNT = 0.8; // 20% di sconto
    private static final double ADVANCE_15_DAYS_DISCOUNT = 0.9; // 10% di sconto
    private static final double ADVANCE_7_DAYS_DISCOUNT = 0.95; // 5% di sconto

    // Database mock delle distanze tra stazioni (in km)
    private final Map<String, Map<String, Integer>> distanceMap;

    // Database mock dei codici promozionali
    private final Map<String, Double> promoCodeDiscounts;

    /**
     * Costruttore che inizializza i database mock.
     */
    public PriceCalculator() {
        // Inizializza il database delle distanze
        distanceMap = new HashMap<>();
        initializeDistanceMap();

        // Inizializza il database dei codici promozionali
        promoCodeDiscounts = new HashMap<>();
        initializePromoCodeDiscounts();
    }

    /**
     * Calcola il prezzo del biglietto in base ai parametri forniti.
     *
     * @param departureStation Stazione di partenza
     * @param arrivalStation Stazione di arrivo
     * @param serviceClass Classe di servizio (Economy, Standard, Prima Classe, Business)
     * @param travelDate Data di viaggio (formato DD-MM-YYYY)
     * @param promoCode Codice promozionale (opzionale)
     * @return Prezzo calcolato del biglietto
     */
    public double calculateTicketPrice(
            String departureStation,
            String arrivalStation,
            String serviceClass,
            Timestamp travelDate,
            String promoCode
    ) {
        LocalDateTime travelDateTime = toLocalDateTime(travelDate);

        int distance = getDistance(departureStation, arrivalStation);
        double classMultiplier = getServiceClassMultiplier(serviceClass);
        double weekdayMultiplier = getWeekdayMultiplier(travelDateTime);
        double advanceBookingDiscount = getAdvanceBookingDiscount(travelDateTime);

        // Prezzo base = distanza * prezzo al km
        double basePrice = distance * BASE_PRICE_PER_KM;

        // Applicare i moltiplicatori e sconti
        double finalPrice = basePrice * classMultiplier * weekdayMultiplier * advanceBookingDiscount;

        // Sconto da codice promozionale
        if (promoCode != null && isValidPromoCode(promoCode)) {
            double promoDiscount = promoCodeDiscounts.getOrDefault(promoCode, 1.0);
            finalPrice *= promoDiscount;
        }

        // Assicurarsi che il prezzo non sia mai inferiore al prezzo minimo
        return Math.max(finalPrice, MIN_PRICE);
    }


    // Convertire Timestamp in LocalDateTime
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Ottiene la distanza tra due stazioni.
     * Se la distanza non è presente nel database, calcola una stima.
     * 
     * @param departure Stazione di partenza
     * @param arrival Stazione di arrivo
     * @return Distanza in km
     */
    private int getDistance(String departure, String arrival) {
        // Controlla se la distanza è presente nel database
        if (distanceMap.containsKey(departure) && distanceMap.get(departure).containsKey(arrival)) {
            return distanceMap.get(departure).get(arrival);
        }

        // Controlla se la distanza inversa è presente (A->B = B->A)
        if (distanceMap.containsKey(arrival) && distanceMap.get(arrival).containsKey(departure)) {
            return distanceMap.get(arrival).get(departure);
        }

        // Se non presente, calcola una distanza stimata (mock)
        return 100; // Distanza predefinita di 100 km
    }

    /**
     * Ottiene il moltiplicatore per la classe di servizio.
     * 
     * @param serviceClass Classe di servizio
     * @return Moltiplicatore
     */
    private double getServiceClassMultiplier(String serviceClass) {
        if (serviceClass == null) {
            return ECONOMY_MULTIPLIER;
        }

        switch (serviceClass.toLowerCase()) {
            case "prima classe":
                return FIRST_CLASS_MULTIPLIER;
            case "business":
                return BUSINESS_MULTIPLIER;
            case "standard":
                return STANDARD_MULTIPLIER;
            case "economy":
            default:
                return ECONOMY_MULTIPLIER;
        }
    }

    /**
     * Ottiene il moltiplicatore per il giorno della settimana.
     *
     * @param travelDate Data di viaggio
     * @return Moltiplicatore
     */
    private double getWeekdayMultiplier(LocalDateTime travelDate) {
        int dayOfWeek = travelDate.getDayOfWeek().getValue(); // 1 = Lunedì, ... 7 = Domenica
        return (dayOfWeek == 6 || dayOfWeek == 7) ? WEEKEND_MULTIPLIER : WEEKDAY_MULTIPLIER;
    }

    /**
     * Ottiene lo sconto per prenotazione anticipata.
     *
     * @param travelDate Data di viaggio
     * @return Fattore di sconto (1.0 = nessuno sconto)
     */
    private double getAdvanceBookingDiscount(LocalDateTime travelDate) {
        LocalDateTime today = LocalDateTime.now();

        long daysBetween = ChronoUnit.DAYS.between(today, travelDate);
        if (daysBetween >= 30) {
            return ADVANCE_30_DAYS_DISCOUNT;
        } else if (daysBetween >= 15) {
            return ADVANCE_15_DAYS_DISCOUNT;
        } else if (daysBetween >= 7) {
            return ADVANCE_7_DAYS_DISCOUNT;
        } else {
            return 1.0; // Nessuno sconto
        }
    }

    /**
     * Verifica se un codice promozionale è valido.
     * 
     * @param promoCode Codice promozionale
     * @return true se valido, false altrimenti
     */
    public boolean isValidPromoCode(String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return false;
        }

        return promoCodeDiscounts.containsKey(promoCode.toUpperCase());
    }

    /**
     * Ottiene il fattore di sconto per un codice promozionale.
     * 
     * @param promoCode Codice promozionale
     * @return Fattore di sconto (1.0 = nessuno sconto, 0.8 = 20% di sconto)
     */
    private double getPromoCodeDiscount(String promoCode) {
        if (!isValidPromoCode(promoCode)) {
            return 1.0;
        }

        return promoCodeDiscounts.get(promoCode.toUpperCase());
    }

    /**
     * Inizializza il database mock delle distanze tra stazioni.
     */
    private void initializeDistanceMap() {
        // Roma
        Map<String, Integer> romaDistances = new HashMap<>();
        romaDistances.put("Milano", 570);
        romaDistances.put("Napoli", 220);
        romaDistances.put("Firenze", 270);
        romaDistances.put("Bologna", 370);
        romaDistances.put("Torino", 670);
        distanceMap.put("Roma", romaDistances);

        // Milano
        Map<String, Integer> milanoDistances = new HashMap<>();
        milanoDistances.put("Torino", 140);
        milanoDistances.put("Bologna", 210);
        milanoDistances.put("Firenze", 300);
        milanoDistances.put("Venezia", 270);
        distanceMap.put("Milano", milanoDistances);

        // Napoli
        Map<String, Integer> napoliDistances = new HashMap<>();
        napoliDistances.put("Bari", 260);
        napoliDistances.put("Reggio Calabria", 410);
        distanceMap.put("Napoli", napoliDistances);

        // Altre stazioni possono essere aggiunte secondo necessità
    }

    /**
     * Inizializza il database mock dei codici promozionali.
     */
    private void initializePromoCodeDiscounts() {
        promoCodeDiscounts.put("PROMO10", 0.9);  // 10% di sconto
        promoCodeDiscounts.put("PROMO20", 0.8);  // 20% di sconto
        promoCodeDiscounts.put("STUDENT", 0.7);  // 30% di sconto per studenti
        promoCodeDiscounts.put("FAMILY", 0.75);  // 25% di sconto per famiglie
        promoCodeDiscounts.put("WELCOME", 0.85); // 15% di sconto per nuovi utenti
    }
}
