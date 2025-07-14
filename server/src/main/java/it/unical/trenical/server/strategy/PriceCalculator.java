package it.unical.trenical.server.strategy;

import com.google.protobuf.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN STRATEGY - CONTESTO
 * Classe per il calcolo del prezzo dei biglietti ferroviari.
 * Implementa il pattern Strategy delegando il calcolo del prezzo a diverse strategie intercambiabili.
 *
 * Il pattern Strategy permette di:
 * - Cambiare algoritmo di calcolo a runtime
 * - Aggiungere nuove strategie senza modificare il codice esistente
 * - Separare la logica di business (calcolo prezzo) dal contesto
 */
public class PriceCalculator {

    // PATTERN STRATEGY: Riferimento alla strategia corrente
    private PriceCalculationStrategy strategy;

    // Database condiviso per tutte le strategie
    private final Map<String, Map<String, Integer>> distanceMap;
    private final Map<String, Double> promoCodeDiscounts;

    /**
     * Costruttore con strategia di default (Standard).
     * PATTERN STRATEGY: Inizializza con una strategia predefinita.
     */
    public PriceCalculator() {
        this(null);
    }

    /**
     * Costruttore che accetta una strategia specifica.
     * PATTERN STRATEGY: Permette l'iniezione della strategia.
     */
    public PriceCalculator(PriceCalculationStrategy strategy) {
        // Inizializza i database condivisi
        this.distanceMap = new HashMap<>();
        this.promoCodeDiscounts = new HashMap<>();
        initializeDistanceMap();
        initializePromoCodeDiscounts();

        // PATTERN STRATEGY: Imposta la strategia (default se null)
        if (strategy == null) {
            this.strategy = new StandardPriceCalculationStrategy(distanceMap, promoCodeDiscounts);
        } else {
            this.strategy = strategy;
        }
    }

    /**
     * PATTERN STRATEGY - METODO PRINCIPALE
     * Delega il calcolo del prezzo alla strategia corrente.
     * Questo è il punto chiave del pattern: il contesto delega all'algoritmo.
     */
    public double calculateTicketPrice(
            String departureStation,
            String arrivalStation,
            String serviceClass,
            Timestamp travelDate,
            String promoCode,
            String trainType
    ) {
        // PATTERN STRATEGY: Delega il calcolo alla strategia corrente
        return strategy.calculateTicketPrice(
            departureStation, arrivalStation, serviceClass,
            travelDate, promoCode, trainType
        );
    }

    /**
     * PATTERN STRATEGY - METODO PRINCIPALE CON TIPO UTENTE
     * Seleziona automaticamente la strategia corretta in base al tipo utente e delega il calcolo.
     */
    public double calculateTicketPrice(
            String departureStation,
            String arrivalStation,
            String serviceClass,
            Timestamp travelDate,
            String promoCode,
            String trainType,
            String userType // Nuovo parametro per il tipo utente
    ) {
        // Seleziona strategia in base al tipo utente
        setStrategyByUserType(userType);

        // PATTERN STRATEGY: Delega il calcolo alla strategia corrente
        return strategy.calculateTicketPrice(
            departureStation, arrivalStation, serviceClass,
            travelDate, promoCode, trainType
        );
    }

    /**
     * PATTERN STRATEGY - CAMBIO STRATEGIA A RUNTIME
     * Permette di cambiare la strategia di calcolo durante l'esecuzione.
     * Questo dimostra la flessibilità del pattern Strategy.
     */
    public void setStrategy(PriceCalculationStrategy strategy) {
        if (strategy != null) {
            this.strategy = strategy;
            System.out.println("[STRATEGY PATTERN] Strategia cambiata a: " +
                             strategy.getClass().getSimpleName());
        }
    }

    /**
     * PATTERN STRATEGY - FACTORY METHOD AGGIORNATO
     * Crea e imposta una strategia basata su un tipo di cliente realistico.
     * Ora riflette i veri casi d'uso del business.
     */
    public void setStrategyByType(String strategyType) {
        PriceCalculationStrategy newStrategy;

        switch (strategyType.toLowerCase()) {
            case "standard":
                newStrategy = new StandardPriceCalculationStrategy(distanceMap, promoCodeDiscounts);
                break;
            case "vip":
            case "premium":
                VIPCustomerPricingStrategy vipStrategy = new VIPCustomerPricingStrategy(distanceMap, promoCodeDiscounts);
                vipStrategy.setUserType("vip");
                newStrategy = vipStrategy;
                break;
            case "corporate":
            case "business":
                CorporateCustomerPricingStrategy corporateStrategy = new CorporateCustomerPricingStrategy();
                corporateStrategy.setUserType("corporate");
                newStrategy = corporateStrategy;
                break;
            default:
                System.out.println("[STRATEGY PATTERN] Tipo strategia non riconosciuto: " + strategyType +
                                  ". Uso strategia standard.");
                newStrategy = new StandardPriceCalculationStrategy(distanceMap, promoCodeDiscounts);
        }

        this.strategy = newStrategy;
        System.out.println("[STRATEGY PATTERN] Strategia cambiata a: " + getCurrentStrategyName() +
                          " per tipo cliente: " + strategyType);
    }

    /**
     * PATTERN STRATEGY - SELEZIONE STRATEGIA PER TIPO UTENTE
     * Seleziona automaticamente la strategia corretta in base al tipo di utente.
     */
    public void setStrategyByUserType(String userType) {
        if (userType == null || userType.isEmpty()) {
            userType = "standard";
        }

        switch (userType.toLowerCase()) {
            case "corporate":
                CorporateCustomerPricingStrategy corpStrategy = new CorporateCustomerPricingStrategy();
                corpStrategy.setUserType(userType);
                this.strategy = corpStrategy;
                break;
            case "vip":
                VIPCustomerPricingStrategy vipStrategy = new VIPCustomerPricingStrategy(distanceMap, promoCodeDiscounts);
                vipStrategy.setUserType(userType);
                this.strategy = vipStrategy;
                break;
            case "standard":
            default:
                StandardPriceCalculationStrategy stdStrategy = new StandardPriceCalculationStrategy(distanceMap, promoCodeDiscounts);
                stdStrategy.setUserType(userType);
                this.strategy = stdStrategy;
                break;
        }

        System.out.println("[STRATEGY PATTERN] Strategia selezionata per utente " + userType + ": " + strategy.getStrategyName());
    }

    /**
     * PATTERN STRATEGY - VALIDAZIONE CODICE PROMO CON TIPO UTENTE
     * Valida i codici promo in base al tipo utente e alla strategia corrente.
     */
    public boolean isValidPromoCode(String promoCode, String userType) {
        if (strategy instanceof StandardPriceCalculationStrategy) {
            ((StandardPriceCalculationStrategy) strategy).setUserType(userType);
        } else if (strategy instanceof CorporateCustomerPricingStrategy) {
            ((CorporateCustomerPricingStrategy) strategy).setUserType(userType);
        } else if (strategy instanceof VIPCustomerPricingStrategy) {
            ((VIPCustomerPricingStrategy) strategy).setUserType(userType);
        }

        // Poi seleziona strategia in base al tipo utente
        setStrategyByUserType(userType);

        // PATTERN STRATEGY: Delega la validazione alla strategia corrente
        return strategy.isValidPromoCode(promoCode);
    }

    /**
     * PATTERN STRATEGY - METODO DI UTILITÀ
     * Ottiene il nome della strategia corrente per debugging/logging.
     */
    public String getCurrentStrategyName() {
        return strategy.getClass().getSimpleName();
    }


    /**
     * PATTERN STRATEGY - METODO DI COMPARAZIONE AGGIORNATO
     * Calcola il prezzo con tutte le strategie realistiche per confronto.
     */
    public Map<String, Double> calculatePriceWithAllStrategies(
            String departureStation,
            String arrivalStation,
            String serviceClass,
            Timestamp travelDate,
            String promoCode,
            String trainType
    ) {
        Map<String, Double> results = new HashMap<>();

        // Salva la strategia corrente
        PriceCalculationStrategy currentStrategy = this.strategy;

        // Testa tutte le strategie realistiche
        PriceCalculationStrategy[] strategies = {
            new StandardPriceCalculationStrategy(distanceMap, promoCodeDiscounts),
            new VIPCustomerPricingStrategy(),
            new CorporateCustomerPricingStrategy()
        };

        String[] strategyNames = {"Standard Customer", "VIP Customer", "Corporate Customer"};

        for (int i = 0; i < strategies.length; i++) {
            this.strategy = strategies[i];
            double price = calculateTicketPrice(departureStation, arrivalStation,
                                              serviceClass, travelDate, promoCode, trainType);
            results.put(strategyNames[i], price);
        }

        // Ripristina la strategia originale
        this.strategy = currentStrategy;

        return results;
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
        napoliDistances.put("Reggio Calabria", 410);
        napoliDistances.put("Firenze", 490);
        distanceMap.put("Napoli", napoliDistances);

        // Firenze
        Map<String, Integer> firenzeDistances = new HashMap<>();
        firenzeDistances.put("Roma", 270);
        firenzeDistances.put("Milano", 300);
        firenzeDistances.put("Bologna", 100);
        firenzeDistances.put("Napoli", 490);
        firenzeDistances.put("Venezia", 260);
        distanceMap.put("Firenze", firenzeDistances);

        // Bologna
        Map<String, Integer> bolognaDistances = new HashMap<>();
        bolognaDistances.put("Roma", 370);
        bolognaDistances.put("Milano", 210);
        bolognaDistances.put("Firenze", 100);
        bolognaDistances.put("Venezia", 150);
        bolognaDistances.put("Torino", 350);
        distanceMap.put("Bologna", bolognaDistances);

        // Torino
        Map<String, Integer> torinoDistances = new HashMap<>();
        torinoDistances.put("Milano", 140);
        torinoDistances.put("Roma", 670);
        torinoDistances.put("Bologna", 350);
        torinoDistances.put("Firenze", 450);
        distanceMap.put("Torino", torinoDistances);

        // Venezia
        Map<String, Integer> veneziaDistances = new HashMap<>();
        veneziaDistances.put("Milano", 270);
        veneziaDistances.put("Roma", 530);
        veneziaDistances.put("Bologna", 150);
        veneziaDistances.put("Firenze", 260);
        distanceMap.put("Venezia", veneziaDistances);

        // Bari
        Map<String, Integer> bariDistances = new HashMap<>();
        bariDistances.put("Roma", 400);
        bariDistances.put("Napoli", 260);
        bariDistances.put("Milano", 900);
        distanceMap.put("Bari", bariDistances);
    }

    /**
     * Inizializza il database dei codici promozionali (legacy).
     */
    private void initializePromoCodeDiscounts() {
        // Codici promozionali standard - SOLO per utenti generali
        promoCodeDiscounts.put("ESTATE2024", 0.15);
        promoCodeDiscounts.put("WEEKEND", 0.10);
        promoCodeDiscounts.put("STUDENT", 0.20);
        promoCodeDiscounts.put("FAMILY", 0.12);
        promoCodeDiscounts.put("SENIOR", 0.18);

        // Codici temporanei/stagionali generali
        promoCodeDiscounts.put("NATALE2024", 0.14);
        promoCodeDiscounts.put("PASQUA", 0.16);
        promoCodeDiscounts.put("BLACKFRIDAY", 0.35);
    }
}
