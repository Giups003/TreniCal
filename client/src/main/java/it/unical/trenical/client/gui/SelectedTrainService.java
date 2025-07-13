package it.unical.trenical.client.gui;

import it.unical.trenical.grpc.common.Train;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Servizio per mantenere i dati del treno selezionato durante la navigazione.
 * Implementa il pattern Singleton per condividere informazioni tra schermate.
 */
public class SelectedTrainService {

    // --- Istanza Singleton ---
    private static SelectedTrainService instance;

    // --- Dati del treno selezionato ---
    private Train selectedTrain;
    private LocalDate selectedDate;
    private LocalTime selectedTime;

    /**
     * Costruttore privato per implementare il pattern Singleton.
     */
    private SelectedTrainService() {
        // Inizializzazione privata
    }

    /**
     * Ottiene l'istanza singleton del servizio.
     * @return L'istanza unica del servizio
     */
    public static synchronized SelectedTrainService getInstance() {
        if (instance == null) {
            instance = new SelectedTrainService();
        }
        return instance;
    }

    /**
     * Imposta il treno selezionato.
     * @param train Il treno selezionato dall'utente
     */
    public void setSelectedTrain(Train train) {
        this.selectedTrain = train;
    }

    /**
     * Ottiene il treno attualmente selezionato.
     * @return Il treno selezionato o null se nessun treno è stato selezionato
     */
    public Train getSelectedTrain() {
        return selectedTrain;
    }

    /**
     * Imposta la data selezionata per il viaggio.
     * @param date La data selezionata
     */
    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
    }

    /**
     * Ottiene la data selezionata per il viaggio.
     * @return La data selezionata o null se non impostata
     */
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * Imposta l'orario selezionato per il viaggio.
     * @param time L'orario selezionato
     */
    public void setSelectedTime(LocalTime time) {
        this.selectedTime = time;
    }

    /**
     * Ottiene l'orario selezionato per il viaggio.
     * @return L'orario selezionato o null se non impostato
     */
    public LocalTime getSelectedTime() {
        return selectedTime;
    }
}