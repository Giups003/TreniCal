package it.unical.trenical.server.strategy;

import com.google.protobuf.Timestamp;
import it.unical.trenical.server.DataStore;
import it.unical.trenical.grpc.promotion.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test specifici per la strategia Standard che gestisce clienti standard.
 */
class StandardPriceCalculationStrategyTest {

    private StandardPriceCalculationStrategy strategy;
    private Map<String, Map<String, Integer>> distanceMap;
    private Map<String, Double> promoCodeDiscounts;

    @BeforeEach
    void setUp() {
        distanceMap = new HashMap<>();
        promoCodeDiscounts = new HashMap<>();
        setupTestData();

        strategy = new StandardPriceCalculationStrategy(distanceMap, promoCodeDiscounts);
        setupPromotionsInDataStore();
    }

    @Test
    @DisplayName("Test calcolo prezzo base senza promozioni")
    void testBasePriceCalculation() {
        Timestamp travelDate = createTimestamp(7);

        double price = strategy.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        assertTrue(price > 0, "Il prezzo dovrebbe essere maggiore di 0");
        assertTrue(price >= 5.0, "Il prezzo minimo dovrebbe essere 5€");

        // Verifica che il calcolo sia basato sulla distanza
        double expectedBasePrice = 570 * 0.15 * 1.5; // distanza * prezzo_per_km * moltiplicatore_treno_premium
        assertEquals(expectedBasePrice, price, 1.0, "Il prezzo dovrebbe essere calcolato correttamente");
    }

    @Test
    @DisplayName("Test differenza di prezzo tra classi")
    void testClassPriceDifference() {
        Timestamp travelDate = createTimestamp(7);

        double secondClassPrice = strategy.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        double firstClassPrice = strategy.calculateTicketPrice(
                "Roma", "Milano", "Prima Classe", travelDate, "", "Frecciarossa"
        );

        assertTrue(firstClassPrice > secondClassPrice,
                  "Prima Classe dovrebbe costare più di Seconda Classe");

        double expectedRatio = 1.8; // Moltiplicatore Prima Classe
        assertEquals(expectedRatio, firstClassPrice / secondClassPrice, 0.1,
                    "Il rapporto di prezzo dovrebbe essere circa 1.8");
    }

    @Test
    @DisplayName("Test applicazione sconto dal sistema promozioni")
    void testPromotionSystemDiscount() {
        Timestamp travelDate = createTimestamp(7);

        double priceWithoutPromo = strategy.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        double priceWithPromo = strategy.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "ESTATE2024", "Frecciarossa"
        );

        assertTrue(priceWithPromo < priceWithoutPromo,
                  "Il prezzo con promo dovrebbe essere minore");

        double discountPercent = (priceWithoutPromo - priceWithPromo) / priceWithoutPromo;
        assertEquals(0.15, discountPercent, 0.01,
                    "Lo sconto dovrebbe essere del 15%");
    }

    @Test
    @DisplayName("Test fallback a codici legacy")
    void testLegacyPromoCodeFallback() {
        Timestamp travelDate = createTimestamp(7);

        double priceWithoutPromo = strategy.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        // Usa un codice legacy che non è nel sistema promozioni
        double priceWithLegacyPromo = strategy.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "WEEKEND", "Frecciarossa"
        );

        assertTrue(priceWithLegacyPromo < priceWithoutPromo,
                  "Il prezzo con promo legacy dovrebbe essere minore");

        double discountPercent = (priceWithoutPromo - priceWithLegacyPromo) / priceWithoutPromo;
        assertEquals(0.10, discountPercent, 0.01,
                    "Lo sconto legacy WEEKEND dovrebbe essere del 10%");
    }

    @Test
    @DisplayName("Test validazione codici promo")
    void testPromoCodeValidation() {
        // Codici dal sistema promozioni
        assertTrue(strategy.isValidPromoCode("ESTATE2024"),
                  "ESTATE2024 dovrebbe essere valido");

        // Codici legacy
        assertTrue(strategy.isValidPromoCode("WEEKEND"),
                  "WEEKEND dovrebbe essere valido (legacy)");
        assertTrue(strategy.isValidPromoCode("STUDENT"),
                  "STUDENT dovrebbe essere valido (legacy)");

        // Codici non validi
        assertFalse(strategy.isValidPromoCode("INVALIDCODE"),
                   "INVALIDCODE non dovrebbe essere valido");
        assertFalse(strategy.isValidPromoCode(""),
                   "Codice vuoto non dovrebbe essere valido");
        assertFalse(strategy.isValidPromoCode(null),
                   "Codice null non dovrebbe essere valido");
    }

    @Test
    @DisplayName("Test calcolo prezzo con distanze sconosciute")
    void testUnknownRouteHandling() {
        Timestamp travelDate = createTimestamp(7);

        double price = strategy.calculateTicketPrice(
                "StazioneInesistente1", "StazioneInesistente2",
                "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        assertTrue(price > 0, "Dovrebbe calcolare un prezzo anche per rotte sconosciute");

        // Dovrebbe usare la distanza di fallback (200 km)
        double expectedPrice = 200 * 0.15 * 1.5; // distanza_fallback * prezzo_per_km * moltiplicatore_premium
        assertEquals(expectedPrice, price, 1.0, "Dovrebbe usare la distanza di fallback");
    }

    @Test
    @DisplayName("Test gestione treni non premium")
    void testNonPremiumTrainPricing() {
        Timestamp travelDate = createTimestamp(7);

        double premiumPrice = strategy.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Frecciarossa"
        );

        double regularPrice = strategy.calculateTicketPrice(
                "Roma", "Milano", "Seconda Classe", travelDate, "", "Regionale"
        );

        assertTrue(premiumPrice > regularPrice,
                  "I treni premium dovrebbero costare di più");

        double expectedRatio = 1.5; // Moltiplicatore treni premium
        assertEquals(expectedRatio, premiumPrice / regularPrice, 0.1,
                    "Il rapporto dovrebbe essere circa 1.5");
    }

    // Metodi di utilità

    private void setupTestData() {
        // Setup mappa distanze
        Map<String, Integer> romaDistances = new HashMap<>();
        romaDistances.put("Milano", 570);
        romaDistances.put("Napoli", 220);
        romaDistances.put("Firenze", 270);
        distanceMap.put("Roma", romaDistances);

        Map<String, Integer> milanoDistances = new HashMap<>();
        milanoDistances.put("Roma", 570);
        milanoDistances.put("Torino", 140);
        distanceMap.put("Milano", milanoDistances);

        // Setup codici promo legacy
        promoCodeDiscounts.put("ESTATE2024", 0.15);
        promoCodeDiscounts.put("WEEKEND", 0.10);
        promoCodeDiscounts.put("STUDENT", 0.20);
        promoCodeDiscounts.put("FAMILY", 0.12);
    }

    private void setupPromotionsInDataStore() {
        DataStore dataStore = DataStore.getInstance();

        Promotion promo = Promotion.newBuilder()
                .setId(1)
                .setName("ESTATE2024")
                .setDescription("Sconto estivo del 15%")
                .setDiscountPercent(15.0)
                .setValidFrom(createTimestamp(-30))
                .setValidTo(createTimestamp(30))
                .setOnlyForLoyaltyMembers(false)
                .build();

        dataStore.addPromotion(promo);
    }

    private Timestamp createTimestamp(int daysFromNow) {
        return Timestamp.newBuilder()
                .setSeconds(LocalDate.now().plusDays(daysFromNow)
                           .atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                .build();
    }
}
