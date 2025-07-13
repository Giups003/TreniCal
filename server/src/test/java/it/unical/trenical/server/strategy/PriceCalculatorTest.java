package it.unical.trenical.server.strategy;

import com.google.protobuf.Timestamp;
import it.unical.trenical.server.DataStore;
import it.unical.trenical.grpc.promotion.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite completa per il Pattern Strategy nel sistema di calcolo prezzi.
 * Verifica il corretto funzionamento delle strategie, gestione codici promo e applicazione sconti.
 */
class PriceCalculatorTest {

    private PriceCalculator priceCalculator;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        // Setup del DataStore per i test
        dataStore = DataStore.getInstance();
        setupTestPromotions();

        // Inizializza il price calculator con strategia standard
        priceCalculator = new PriceCalculator();
    }

    @Test
    @DisplayName("Test strategia Standard - calcolo prezzo base")
    void testStandardStrategyBasicPricing() {
        priceCalculator.setStrategyByType("standard");

        Timestamp travelDate = createTravelDate(7); // 7 giorni nel futuro

        double price = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        assertTrue(price > 0, "Il prezzo dovrebbe essere maggiore di 0");
        assertTrue(price >= 5.0, "Il prezzo minimo dovrebbe essere 5€");

        System.out.println("Prezzo standard Roma-Milano: " + String.format("%.2f", price) + "€");
    }

    @Test
    @DisplayName("Test strategia Standard - applicazione codice promo valido")
    void testStandardStrategyWithValidPromoCode() {
        priceCalculator.setStrategyByType("standard");

        Timestamp travelDate = createTravelDate(7);

        // Prezzo senza promo
        double priceWithoutPromo = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        // Prezzo con promo
        double priceWithPromo = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "ESTATE2024", "Frecciarossa"
        );

        assertTrue(priceWithPromo < priceWithoutPromo,
                  "Il prezzo con promo dovrebbe essere minore del prezzo base");

        double expectedDiscount = priceWithoutPromo * 0.15; // 15% di sconto
        double actualDiscount = priceWithoutPromo - priceWithPromo;

        assertEquals(expectedDiscount, actualDiscount, 5.0,
                    "Lo sconto applicato dovrebbe essere circa il 15%");

        System.out.println("Sconto applicato: " + String.format("%.2f", actualDiscount) + "€");
    }

    @Test
    @DisplayName("Test strategia VIP - sconto base del 15%")
    void testVIPStrategyBaseDiscount() {
        // Calcola prezzo standard per confronto
        priceCalculator.setStrategyByType("standard");
        double standardPrice = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", createTravelDate(7), "", "Frecciarossa"
        );

        // Calcola prezzo VIP
        priceCalculator.setStrategyByType("vip");
        double vipPrice = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", createTravelDate(7), "", "Frecciarossa"
        );

        assertTrue(vipPrice < standardPrice, "Il prezzo VIP dovrebbe essere minore del prezzo standard");

        double expectedVipPrice = standardPrice * 0.85; // 15% di sconto VIP
        assertEquals(expectedVipPrice, vipPrice, 5.0,
                    "Il prezzo VIP dovrebbe avere uno sconto base del 15%");

        System.out.println("Prezzo standard: " + String.format("%.2f", standardPrice) + "€");
        System.out.println("Prezzo VIP: " + String.format("%.2f", vipPrice) + "€");
    }

    @Test
    @DisplayName("Test strategia Corporate - sconto base del 10%")
    void testCorporateStrategyBaseDiscount() {
        // Calcola prezzo standard per confronto
        priceCalculator.setStrategyByType("standard");
        double standardPrice = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Prima Classe", createTravelDate(7), "", "Frecciarossa"
        );

        // Calcola prezzo Corporate
        priceCalculator.setStrategyByType("corporate");
        double corporatePrice = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Prima Classe", createTravelDate(7), "", "Frecciarossa"
        );

        assertTrue(corporatePrice < standardPrice,
                  "Il prezzo Corporate dovrebbe essere minore del prezzo standard");

        System.out.println("Prezzo standard: " + String.format("%.2f", standardPrice) + "€");
        System.out.println("Prezzo corporate: " + String.format("%.2f", corporatePrice) + "€");
    }

    @Test
    @DisplayName("Test validazione codici promo per diverse strategie")
    void testPromoCodeValidationAcrossStrategies() {
        // Test strategia Standard
        priceCalculator.setStrategyByType("standard");
        assertTrue(priceCalculator.isValidPromoCode("ESTATE2024", "standard"),
                  "ESTATE2024 dovrebbe essere valido per clienti standard");
        assertFalse(priceCalculator.isValidPromoCode("INVALIDCODE", "standard"),
                   "INVALIDCODE non dovrebbe essere valido");

        // Test strategia VIP
        priceCalculator.setStrategyByType("vip");
        assertTrue(priceCalculator.isValidPromoCode("ESTATE2024", "vip"),
                  "ESTATE2024 dovrebbe essere valido per clienti VIP");
        assertTrue(priceCalculator.isValidPromoCode("VIP2024", "vip"),
                  "VIP2024 dovrebbe essere valido per clienti VIP");

        // Test strategia Corporate
        priceCalculator.setStrategyByType("corporate");
        assertTrue(priceCalculator.isValidPromoCode("CORP2024", "corporate"),
                  "CORP2024 dovrebbe essere valido per clienti corporate");
    }

    @Test
    @DisplayName("Test confronto prezzi tra tutte le strategie")
    void testAllStrategiesComparison() {
        Timestamp travelDate = createTravelDate(7);

        var comparison = priceCalculator.calculatePriceWithAllStrategies(
                "Roma", "Milano", "Prima Classe", travelDate, "ESTATE2024", "Frecciarossa"
        );

        assertEquals(3, comparison.size(), "Dovrebbero esserci 3 strategie");

        assertTrue(comparison.containsKey("Standard Customer"),
                  "Dovrebbe contenere la strategia Standard Customer");
        assertTrue(comparison.containsKey("VIP Customer"),
                  "Dovrebbe contenere la strategia VIP Customer");
        assertTrue(comparison.containsKey("Corporate Customer"),
                  "Dovrebbe contenere la strategia Corporate Customer");

        double standardPrice = comparison.get("Standard Customer");
        double vipPrice = comparison.get("VIP Customer");
        double corporatePrice = comparison.get("Corporate Customer");

        assertTrue(vipPrice <= standardPrice,
                  "Il prezzo VIP dovrebbe essere minore o uguale al prezzo standard");
        assertTrue(corporatePrice <= standardPrice,
                  "Il prezzo Corporate dovrebbe essere minore o uguale al prezzo standard");

        System.out.println("Confronto prezzi con ESTATE2024:");
        comparison.forEach((strategy, price) ->
            System.out.println(strategy + ": " + String.format("%.2f", price) + "€")
        );
    }

    @Test
    @DisplayName("Test cambio strategia a runtime")
    void testRuntimeStrategyChange() {
        // Inizia con strategia standard
        assertEquals("StandardPriceCalculationStrategy",
                    priceCalculator.getCurrentStrategyName());

        // Cambia a VIP
        priceCalculator.setStrategyByType("vip");
        assertEquals("VIPCustomerPricingStrategy",
                    priceCalculator.getCurrentStrategyName());

        // Cambia a Corporate
        priceCalculator.setStrategyByType("corporate");
        assertEquals("CorporateCustomerPricingStrategy",
                    priceCalculator.getCurrentStrategyName());

        // Torna a Standard
        priceCalculator.setStrategyByType("standard");
        assertEquals("StandardPriceCalculationStrategy",
                    priceCalculator.getCurrentStrategyName());
    }

    @Test
    @DisplayName("Test gestione codici promo vuoti o null")
    void testEmptyOrNullPromoCodes() {
        priceCalculator.setStrategyByType("standard");
        Timestamp travelDate = createTravelDate(7);

        double priceWithEmptyPromo = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        double priceWithNullPromo = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, null, "Frecciarossa"
        );

        assertEquals(priceWithEmptyPromo, priceWithNullPromo, 0.01,
                    "Prezzo con promo vuoto e null dovrebbe essere uguale");

        assertFalse(priceCalculator.isValidPromoCode("", "standard"),
                   "Codice promo vuoto non dovrebbe essere valido");
        assertFalse(priceCalculator.isValidPromoCode(null, "standard"),
                   "Codice promo null non dovrebbe essere valido");
    }

    @Test
    @DisplayName("Test differenze tariffarie tra classi di servizio")
    void testServiceClassPriceDifferences() {
        priceCalculator.setStrategyByType("standard");
        Timestamp travelDate = createTravelDate(7);

        double secondClassPrice = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        double firstClassPrice = priceCalculator.calculateTicketPrice(
                "Roma", "Milano", "Prima Classe", travelDate, "", "Frecciarossa"
        );

        assertTrue(firstClassPrice > secondClassPrice,
                  "Prima Classe dovrebbe costare più di Seconda Classe");

        // Verifica che la differenza sia ragionevole (circa 80% in più)
        double expectedFirstClassPrice = secondClassPrice * 1.8;
        assertEquals(expectedFirstClassPrice, firstClassPrice, 5.0,
                    "Prima Classe dovrebbe costare circa l'80% in più");

        System.out.println("Seconda Classe: " + String.format("%.2f", secondClassPrice) + "€");
        System.out.println("Prima Classe: " + String.format("%.2f", firstClassPrice) + "€");
    }

    // Metodi di utilità per i test

    private void setupTestPromotions() {
        // Promozione generale
        Promotion generalPromo = Promotion.newBuilder()
                .setId(1)
                .setName("ESTATE2024")
                .setDescription("Sconto estivo del 15%")
                .setDiscountPercent(15.0)
                .setValidFrom(createTimestamp(-30))
                .setValidTo(createTimestamp(30))
                .setOnlyForLoyaltyMembers(false)
                .build();

        // Promozione VIP esclusiva
        Promotion vipPromo = Promotion.newBuilder()
                .setId(2)
                .setName("VIP2024")
                .setDescription("Sconto VIP esclusivo del 25%")
                .setDiscountPercent(25.0)
                .setValidFrom(createTimestamp(-10))
                .setValidTo(createTimestamp(60))
                .setOnlyForLoyaltyMembers(true)
                .build();

        // Promozione Corporate
        Promotion corpPromo = Promotion.newBuilder()
                .setId(3)
                .setName("CORP2024")
                .setDescription("Sconto aziendale del 18%")
                .setDiscountPercent(18.0)
                .setValidFrom(createTimestamp(-15))
                .setValidTo(createTimestamp(45))
                .setOnlyForLoyaltyMembers(false)
                .build();

        dataStore.addPromotion(generalPromo);
        dataStore.addPromotion(vipPromo);
        dataStore.addPromotion(corpPromo);
    }

    private Timestamp createTravelDate(int daysFromNow) {
        return createTimestamp(daysFromNow);
    }

    private Timestamp createTimestamp(int daysFromNow) {
        return Timestamp.newBuilder()
                .setSeconds(LocalDate.now().plusDays(daysFromNow)
                           .atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                .build();
    }
}
