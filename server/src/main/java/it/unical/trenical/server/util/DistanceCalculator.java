package it.unical.trenical.server.util;

import it.unical.trenical.grpc.common.Station;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Utility class per calcoli geografici e di distanza tra stazioni.
 * Implementa il pattern Strategy per diversi algoritmi di calcolo distanza.
 */
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calcola la distanza in chilometri tra due stazioni usando la formula di Haversine.
     * @param station1 Prima stazione
     * @param station2 Seconda stazione
     * @return distanza in chilometri, -1 se una delle stazioni è null
     */
    public static double calculateDistance(Station station1, Station station2) {
        if (station1 == null || station2 == null) {
            return -1;
        }

        return calculateHaversineDistance(
            station1.getLatitude(), station1.getLongitude(),
            station2.getLatitude(), station2. getLongitude()
        );
    }

    /**
     * Formula di Haversine per calcolare la distanza tra due punti geografici.
     * @param lat1 Latitudine del primo punto
     * @param lon1 Longitudine del primo punto
     * @param lat2 Latitudine del secondo punto
     * @param lon2 Longitudine del secondo punto
     * @return distanza in chilometri
     */
    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Converti gradi in radianti
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

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calcola la distanza totale di una tratta passando per più stazioni.
     * @param stations Lista delle stazioni in ordine
     * @return distanza totale in chilometri, -1 se errore
     */
    public static double calculateRouteDistance(List<Station> stations) {
        if (stations == null || stations.size() < 2) return -1;

        double totalDistance = 0;
        for (int i = 0; i < stations.size() - 1; i++) {
            double segmentDistance = calculateDistance(stations.get(i), stations.get(i + 1));
            if (segmentDistance < 0) return -1; // Errore nel calcolo
            totalDistance += segmentDistance;
        }

        return totalDistance;
    }

    /**
     * Trova le stazioni più vicine a una stazione data.
     * @param referenceStation Stazione di riferimento
     * @param allStations Tutte le stazioni disponibili
     * @param maxResults Numero massimo di risultati
     * @return Lista di stazioni ordinate per distanza crescente
     */
    public static List<StationDistance> findNearestStations(Station referenceStation, List<Station> allStations, int maxResults) {
        if (referenceStation == null || allStations == null) return new ArrayList<>();

        return allStations.stream()
                .filter(station -> station.getId() != referenceStation.getId()) // Escludi la stazione stessa
                .map(station -> new StationDistance(
                    station,
                    calculateDistance(referenceStation, station)
                ))
                .filter(sd -> sd.distance >= 0) // Escludi errori di calcolo
                .sorted((a, b) -> Double.compare(a.distance, b.distance))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Calcola il prezzo di viaggio basato sulla distanza.
     * @param distance Distanza in chilometri
     * @param pricePerKm Prezzo per chilometro
     * @param basePrice Prezzo base minimo
     * @return prezzo calcolato
     */
    public static double calculatePriceByDistance(double distance, double pricePerKm, double basePrice) {
        if (distance <= 0) return basePrice;
        return Math.max(basePrice, distance * pricePerKm);
    }

    /**
     * Stima il tempo di viaggio basato sulla distanza.
     * @param distance Distanza in chilometri
     * @param averageSpeedKmh Velocità media in km/h
     * @return tempo in minuti
     */
    public static int estimateTravelTime(double distance, double averageSpeedKmh) {
        if (distance <= 0 || averageSpeedKmh <= 0) return 0;
        return (int) Math.ceil((distance / averageSpeedKmh) * 60); // Converti ore in minuti
    }

    /**
     * Classe helper per rappresentare una stazione con la sua distanza.
     */
    public static class StationDistance {
        public final Station station;
        public final double distance;

        public StationDistance(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }

        public Station getStation() { return station; }
        public double getDistance() { return distance; }

        @Override
        public String toString() {
            return String.format("%s (%.2f km)", station.getName(), distance);
        }
    }
}
