package it.unical.trenical.server.strategy;

import com.google.protobuf.Timestamp;
import it.unical.trenical.server.DataStore;
import it.unical.trenical.grpc.promotion.Promotion;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * PATTERN STRATEGY - STRATEGIA CONCRETA PER CLIENTI CORPORATE
 * Implementa la logica di calcolo prezzo per clienti aziendali.

 * Caratteristiche:
 * - Sconti graduali basati sul volume (più viaggi = più sconto)
 * - Tariffe preferenziali negli orari business (7-9, 17-19)
 * - Fatturazione semplificata con IVA inclusa
 * - Gestione promozioni aziendali dedicate
 */
public class CorporateCustomerPricingStrategy implements PriceCalculationStrategy {

    private static final double CORPORATE_BASE_DISCOUNT = 0.10; // 10% sconto base aziendale
    private static final double BUSINESS_HOURS_DISCOUNT = 0.05; // 5% extra negli orari business
    private static final double VOLUME_DISCOUNT_THRESHOLD = 100.0; // Soglia per sconto volume
    private static final double VOLUME_DISCOUNT = 0.08; // 8% sconto volume aggiuntivo
    private static final double MIN_PRICE = 4.0; // Prezzo minimo corporate

    // Delega il calcolo base alla strategia standard
    private final StandardPriceCalculationStrategy standardStrategy;

    private String userType = "standard";

    public CorporateCustomerPricingStrategy() {
        this.standardStrategy = new StandardPriceCalculationStrategy(null, null);
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
    @Override
    public double calculateTicketPrice(String departureStation, String arrivalStation,
                                     String serviceClass, Timestamp travelDate,
                                     String promoCode, String trainType) {
        // Usa il tipo utente impostato
        String type = userType != null ? userType : "standard";
        double standardPrice = standardStrategy.calculateTicketPrice(
            departureStation, arrivalStation, serviceClass, travelDate, promoCode, trainType
        );
        double corporatePrice = "corporate".equalsIgnoreCase(type) ? standardPrice * (1.0 - CORPORATE_BASE_DISCOUNT) : standardPrice;
        if ("corporate".equalsIgnoreCase(type) && isBusinessHours(travelDate)) {
            corporatePrice *= (1.0 - BUSINESS_HOURS_DISCOUNT);
            System.out.println("[CORPORATE STRATEGY] Sconto orario business applicato");
        }
        if ("corporate".equalsIgnoreCase(type) && standardPrice >= VOLUME_DISCOUNT_THRESHOLD) {
            corporatePrice *= (1.0 - VOLUME_DISCOUNT);
            System.out.println("[CORPORATE STRATEGY] Sconto volume aziendale applicato");
        }
        if (promoCode != null && !promoCode.trim().isEmpty()) {
            if (isCorporateExclusiveCode(promoCode) && !"corporate".equalsIgnoreCase(type)) {
                System.out.println("[CORPORATE STRATEGY] Codice business inserito da utente non corporate: nessuno sconto applicato");
                return Math.round(standardPrice * 100.0) / 100.0;
            }
            corporatePrice = applyCorporatePromotion(corporatePrice, promoCode,
                                                   departureStation, arrivalStation,
                                                   serviceClass, travelDate, trainType);
        }
        double finalPrice = Math.max(corporatePrice, MIN_PRICE);
        System.out.println("[CORPORATE STRATEGY] Prezzo standard: " + standardPrice +
                          ", Prezzo corporate finale: " + finalPrice +
                          ", Risparmio aziendale: " + (standardPrice - finalPrice) + "€");
        return Math.round(finalPrice * 100.0) / 100.0;
    }

    @Override
    public boolean isValidPromoCode(String promoCode) {
        String type = userType != null ? userType.toLowerCase() : "standard";

        if (!type.equals("corporate")) {
            System.out.println("[CORPORATE STRATEGY] Codice '" + promoCode + "' rifiutato: utente non è corporate (tipo: " + type + ")");
            return false;
        }

        // Controlla se il codice promo è valido per utenti corporate dal DataStore
        return isPromoValidForUserType(promoCode, "corporate");
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
            System.err.println("[CORPORATE PROMO VALIDATION ERROR] " + e.getMessage());
        }

        return false;
    }

    /**
     * Applica promozioni specifiche per clienti aziendali
     */
    private double applyCorporatePromotion(double currentPrice, String promoCode,
                                         String departureStation, String arrivalStation,
                                         String serviceClass, Timestamp travelDate, String trainType) {

        // Controlla promozioni aziendali nel sistema
        try {
            DataStore dataStore = DataStore.getInstance();

            for (Promotion promo : dataStore.getAllPromotions()) {
                if (promo.getName().equalsIgnoreCase(promoCode.trim())) {

                    // Verifica se è una promo corporate o generica applicabile
                    if (isCorporateApplicablePromo(promo, departureStation, arrivalStation,
                                                  serviceClass, travelDate, trainType)) {

                        double discount = promo.getDiscountPercent() / 100.0;

                        // Bonus del 3% per clienti corporate su promozioni generiche
                        if (!isCorporateSpecificPromo(promo)) {
                            discount += 0.03; // 3% bonus corporate
                            System.out.println("[CORPORATE STRATEGY] Bonus corporate del 3% applicato");
                        }

                        double discountedPrice = currentPrice * (1.0 - discount);
                        System.out.println("[CORPORATE STRATEGY] Promozione aziendale applicata: " +
                                          promoCode + " (" + (discount * 100) + "%)");

                        return discountedPrice;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[CORPORATE PROMO ERROR] " + e.getMessage());
        }

        // Fallback: codici corporate legacy
        if (isCorporateExclusiveCode(promoCode)) {
            double legacyDiscount = getCorporateLegacyDiscount(promoCode);
            if (legacyDiscount > 0) {
                double discountedPrice = currentPrice * (1.0 - legacyDiscount);
                System.out.println("[CORPORATE STRATEGY] Codice corporate legacy applicato: " +
                                  promoCode + " (" + (legacyDiscount * 100) + "%)");
                return discountedPrice;
            }
        }

        return currentPrice;
    }

    /**
     * Controlla se l'orario di viaggio è negli orari business
     */
    private boolean isBusinessHours(Timestamp travelDate) {
        if (travelDate == null) return false;

        try {
            LocalTime travelTime = Instant.ofEpochSecond(travelDate.getSeconds())
                                         .atZone(ZoneId.systemDefault())
                                         .toLocalTime();

            // Orari business: 7:00-9:00 e 17:00-19:00
            return (travelTime.isAfter(LocalTime.of(7, 0)) && travelTime.isBefore(LocalTime.of(9, 0))) ||
                   (travelTime.isAfter(LocalTime.of(17, 0)) && travelTime.isBefore(LocalTime.of(19, 0)));

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se una promozione è applicabile per clienti corporate
     */
    private boolean isCorporateApplicablePromo(Promotion promo, String departureStation,
                                             String arrivalStation, String serviceClass,
                                             Timestamp travelDate, String trainType) {

        String routeName = departureStation + "-" + arrivalStation;

        // Verifica criteri base
        boolean routeOk = promo.getRouteNamesList().isEmpty() ||
                         promo.getRouteNamesList().contains(routeName);

        boolean classOk = promo.getServiceClassesList().isEmpty() ||
                         promo.getServiceClassesList().contains(serviceClass);

        boolean typeOk = promo.getTrainType().isEmpty() ||
                        promo.getTrainType().equalsIgnoreCase(trainType);

        // Verifica validità temporale
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
     * Controlla se una promozione è specifica per corporate
     */
    private boolean isCorporateSpecificPromo(Promotion promo) {
        String name = promo.getName().toLowerCase();
        String description = promo.getDescription().toLowerCase();

        return name.contains("corporate") || name.contains("business") || name.contains("azien") ||
               description.contains("corporate") || description.contains("business") || description.contains("azien");
    }

    /**
     * Controlla se un codice è esclusivo corporate
     */
    private boolean isCorporateExclusiveCode(String promoCode) {
        if (promoCode == null) return false;

        String code = promoCode.toUpperCase();
        return code.startsWith("CORP") ||
               code.startsWith("BIZ") ||
               code.startsWith("BUSINESS") ||
               code.startsWith("COMPANY") ||
               code.equals("ENTERPRISE");
    }

    /**
     * Ottiene sconto per codici corporate legacy
     */
    private double getCorporateLegacyDiscount(String promoCode) {
        String code = promoCode.toUpperCase();

        return switch (code) {
            case "CORP2024", "BUSINESS" -> 0.18; // 18% sconto
            case "ENTERPRISE" -> 0.22; // 22% sconto enterprise
            case "COMPANY50" -> 0.15; // 15% sconto
            default -> {
                if (code.startsWith("BIZ")) {
                    yield 0.12; // 12% sconto generico business
                }
                yield 0.0;
            }
        };
    }

    @Override
    public String getStrategyName() {
        return "Corporate Customer Pricing Strategy";
    }
}
