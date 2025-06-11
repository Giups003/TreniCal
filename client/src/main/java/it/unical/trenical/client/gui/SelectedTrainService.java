package it.unical.trenical.client.gui;

import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.train.Stop;
import java.util.List;

/**
 * Servizio che mantiene il riferimento al treno selezionato durante la navigazione tra schermate.
 * Implementa il pattern Singleton per garantire un unico punto di accesso.
 */
public class SelectedTrainService {

    private static SelectedTrainService instance;
    private Train selectedTrain;
    private List<Stop> stops;  // Aggiungi questo campo
    private java.time.LocalDate selectedDate;
    private java.time.LocalTime selectedTime;

    // Costruttore privato per il pattern Singleton
    private SelectedTrainService() {
        // Inizializzazione privata

    }

    /**
     * Ottiene l'istanza singleton del servizio.
     *
     * @return L'istanza del servizio
     */
    public static synchronized SelectedTrainService getInstance() {
        if (instance == null) {
            instance = new SelectedTrainService();
        }
        return instance;
    }

    /**
     * Imposta il treno selezionato.
     *
     * @param train Il treno selezionato dall'utente
     */
    public void setSelectedTrain(Train train) {
        this.selectedTrain = train;
    }

    /**
     * Ottiene il treno attualmente selezionato.
     *
     * @return Il treno selezionato o null se nessun treno è stato selezionato
     */
    public Train getSelectedTrain() {
        return selectedTrain;
    }

    /**
     * Imposta le fermate del treno selezionato.
     *
     * @param stops Lista delle fermate del treno
     */
    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    /**
     * Ottiene le fermate del treno selezionato.
     *
     * @return Lista delle fermate o null se non sono state impostate
     */
    public List<Stop> getStops() {
        return stops;
    }

    /**
     * Cancella il treno selezionato e le sue fermate.
     */
    public void clearSelectedTrain() {
        this.selectedTrain = null;
        this.stops = null;
    }

    /**
     * Imposta la data selezionata.
     */
    public void setSelectedDate(java.time.LocalDate date) {
        this.selectedDate = date;
    }
    /**
     * Restituisce la data selezionata.
     */
    public java.time.LocalDate getSelectedDate() {
        return selectedDate;
    }
    /**
     * Imposta l'orario selezionato.
     */
    public void setSelectedTime(java.time.LocalTime time) {
        this.selectedTime = time;
    }
    /**
     * Restituisce l'orario selezionato.
     */
    public java.time.LocalTime getSelectedTime() {
        return selectedTime;
    }
}