package it.unical.trenical.server.strategy;

import com.google.protobuf.Timestamp;
import it.unical.trenical.server.DataStore;
import it.unical.trenical.grpc.promotion.Promotion;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * PATTERN STRATEGY - STRATEGIA CONCRETA PER CLIENTI VIP
 * Implementa la logica di calcolo prezzo per clienti VIP/Premium.
 *
 * Caratteristiche:
 * - Sconto base del 15% su tutti i biglietti
 * - Sconti promozionali maggiorati (bonus del 5%)
 * - Accesso a promozioni esclusive VIP
 * - Upgrade gratuito di classe in determinati casi
 */
public class VIPCustomerPricingStrategy implements PriceCalculationStrategy {

    private static final double VIP_BASE_DISCOUNT = 0.15; // 15% di sconto base
    private static final double VIP_PROMO_BONUS = 0.05;   // 5% bonus sui codici promo
    private static final double MIN_PRICE = 3.0;          // Prezzo minimo per VIP

    // Delega il calcolo base alla strategia standard
    private final StandardPriceCalculationStrategy standardStrategy;

    private String userType = "vip";

    public VIPCustomerPricingStrategy() {
        this.standardStrategy = new StandardPriceCalculationStrategy(null, null);
    }

    public VIPCustomerPricingStrategy(java.util.Map<String, java.util.Map<String, Integer>> distanceMap,
                                     java.util.Map<String, Double> promoCodeDiscounts) {
        this.standardStrategy = new StandardPriceCalculationStrategy(distanceMap, promoCodeDiscounts);
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public double calculateTicketPrice(String departureStation, String arrivalStation,
                                     String serviceClass, Timestamp travelDate,
                                     String promoCode, String trainType) {

        // 1. Calcola il prezzo con la strategia standard
        double standardPrice = standardStrategy.calculateTicketPrice(
            departureStation, arrivalStation, serviceClass, travelDate, promoCode, trainType
        );

        // 2. Applica sconto VIP base
        double vipPrice = standardPrice * (1.0 - VIP_BASE_DISCOUNT);

        // 3. Applica bonus aggiuntivo se c'è un codice promo valido
        if (promoCode != null && !promoCode.trim().isEmpty() && isValidPromoCode(promoCode)) {

            // Controlla se è una promozione VIP esclusiva
            if (isVIPExclusivePromo(promoCode, departureStation, arrivalStation,
                                   serviceClass, travelDate, trainType)) {
                // Promo VIP: sconto aggiuntivo del 10%
                vipPrice *= 0.90;
                System.out.println("[VIP STRATEGY] Promozione VIP esclusiva applicata: " + promoCode);
            } else {
                // Promo normale: bonus del 5%
                vipPrice *= (1.0 - VIP_PROMO_BONUS);
                System.out.println("[VIP STRATEGY] Bonus VIP del 5% applicato al codice promo: " + promoCode);
            }
        }

        // 4. Upgrade gratuito di classe se conveniente
        if ("Seconda Classe".equalsIgnoreCase(serviceClass) && shouldUpgradeToFirstClass(vipPrice)) {
            System.out.println("[VIP STRATEGY] Upgrade gratuito a Prima Classe per cliente VIP");
            // Non aumentiamo il prezzo, è un upgrade gratuito
        }

        double finalPrice = Math.max(vipPrice, MIN_PRICE);

        System.out.println("[VIP STRATEGY] Prezzo standard: " + standardPrice +
                          ", Prezzo VIP finale: " + finalPrice +
                          ", Risparmio: " + (standardPrice - finalPrice) + "€");

        return Math.round(finalPrice * 100.0) / 100.0;
    }

    @Override
    public boolean isValidPromoCode(String promoCode) {
        String type = userType != null ? userType.toLowerCase() : "standard";

        if (!type.equals("vip")) {
            System.out.println("[VIP STRATEGY] Codice '" + promoCode + "' rifiutato: utente non è VIP (tipo: " + type + ")");
            return false;
        }

        // Controlla se il codice promo è valido per utenti VIP dal DataStore
        return isPromoValidForUserType(promoCode, "vip");
    }

    /**
     * Controlla se una promozione è valida per un tipo di utente specifico
     * utilizzando il campo userTypes dal file promotions.json
     */
    private boolean isPromoValidForUserType(String promoCode, String userType) {
        if (promoCode == null || promoCode.trim().isEmpty()) return false;

        try {
            DataStore dataStore = DataStore.getInstance();

            for (Promotion promo : dataStore.getAllPromotions()) {
                if (promo.getName().equalsIgnoreCase(promoCode.trim())) {
                    // Controlla se il tipo utente è nella lista userTypes
                    return promo.getUserTypesList().contains(userType);
                }
            }
        } catch (Exception e) {
            System.err.println("[VIP PROMO VALIDATION ERROR] " + e.getMessage());
        }

        return false;
    }

    /**
     * Controlla se una promozione è esclusiva per clienti VIP
     */
    private boolean isVIPExclusivePromo(String promoCode, String departureStation, String arrivalStation,
                                       String serviceClass, Timestamp travelDate, String trainType) {
        try {
            DataStore dataStore = DataStore.getInstance();

            for (Promotion promo : dataStore.getAllPromotions()) {
                if (promo.getName().equalsIgnoreCase(promoCode.trim())) {
                    // Controlla se è solo per loyalty members (interpretiamo come VIP)
                    if (promo.getOnlyForLoyaltyMembers()) {
                        return isPromotionApplicable(promo, departureStation, arrivalStation,
                                                   serviceClass, travelDate, trainType);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[VIP PROMO ERROR] " + e.getMessage());
        }

        return false;
    }

    /**
     * Verifica se una promozione è applicabile ai parametri del viaggio
     */
    private boolean isPromotionApplicable(Promotion promo, String departureStation, String arrivalStation,
                                        String serviceClass, Timestamp travelDate, String trainType) {

        String routeName = departureStation + "-" + arrivalStation;

        // Controlla tratta
        boolean routeOk = promo.getRouteNamesList().isEmpty() ||
                         promo.getRouteNamesList().contains(routeName);

        // Controlla classe
        boolean classOk = promo.getServiceClassesList().isEmpty() ||
                         promo.getServiceClassesList().contains(serviceClass);

        // Controlla tipo treno
        boolean typeOk = promo.getTrainType().isEmpty() ||
                        promo.getTrainType().equalsIgnoreCase(trainType);

        // Controlla validità temporale
        boolean dateOk = true;
        if (travelDate != null) {
            LocalDate travelLocalDate = Instant.ofEpochSecond(travelDate.getSeconds())
                                               .atZone(ZoneId.systemDefault()).toLocalDate();

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

        return routeOk && classOk && typeOk && dateOk;
    }

    /**
     * Controlla se un codice è esclusivo VIP (legacy)
     */
    private boolean isVIPExclusiveCode(String promoCode) {
        if (promoCode == null) return false;

        String code = promoCode.toUpperCase();
        return code.startsWith("VIP") ||
               code.startsWith("PREMIUM") ||
               code.equals("PLATINUM") ||
               code.equals("GOLD");
    }

    /**
     * Determina se offrire upgrade gratuito a Prima Classe
     */
    private boolean shouldUpgradeToFirstClass(double currentPrice) {
        // Upgrade gratuito se il prezzo è superiore a 50€
        // (significa viaggio lungo dove l'upgrade ha valore)
        return currentPrice > 50.0;
    }

    @Override
    public String getStrategyName() {
        return "VIP Customer Pricing Strategy";
    }
}
