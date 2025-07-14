package it.unical.trenical.server.strategy;

import com.google.protobuf.Timestamp;

/**
 * PATTERN STRATEGY - INTERFACCIA STRATEGIA
 * Definisce il contratto comune per tutte le strategie di calcolo prezzo.
 *
 * Ogni strategia concreta implementa questo contratto con il proprio algoritmo specifico.
 * Questo permette al contesto (PriceCalculator) di utilizzare qualsiasi strategia
 * in modo intercambiabile senza conoscere i dettagli implementativi.
 */
public interface PriceCalculationStrategy {

    /**
     * Calcola il prezzo del biglietto secondo la strategia specifica.
     *
     * @param departureStation Stazione di partenza
     * @param arrivalStation Stazione di arrivo
     * @param serviceClass Classe di servizio (Prima/Seconda Classe)
     * @param travelDate Data di viaggio
     * @param promoCode Codice promozionale (può essere null o vuoto)
     * @param trainType Tipo di treno
     * @return Prezzo calcolato secondo la strategia
     */
    double calculateTicketPrice(
        String departureStation,
        String arrivalStation,
        String serviceClass,
        Timestamp travelDate,
        String promoCode,
        String trainType
    );

    /**
     * Valida se un codice promozionale è valido per questa strategia.
     *
     * @param promoCode Codice promozionale da validare
     * @return true se il codice è valido, false altrimenti
     */
    boolean isValidPromoCode(String promoCode);

    /**
     * Ottiene il nome descrittivo della strategia per logging/debugging.
     *
     * @return Nome della strategia
     */
    default String getStrategyName() {
        return this.getClass().getSimpleName();
    }
}
