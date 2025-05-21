package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller per la schermata di visualizzazione biglietti acquistati.
 */
public class MyTicketsController {
    @FXML private TableView<?> ticketsTable;
    @FXML private TableColumn<?, ?> colTrain;
    @FXML private TableColumn<?, ?> colDate;
    @FXML private TableColumn<?, ?> colTime;
    @FXML private TableColumn<?, ?> colClass;
    @FXML private TableColumn<?, ?> colSeats;
    @FXML private TableColumn<?, ?> colStatus;

    /**
     * Gestisce la modifica di un biglietto selezionato.
     */
    @FXML
    private void onModify() {
        // Da implementare: modifica biglietto
        System.out.println("Modifica biglietto");
    }

    /**
     * Gestisce l'annullamento di un biglietto selezionato.
     */
    @FXML
    private void onCancel() {
        // Da implementare: annulla biglietto
        System.out.println("Annulla biglietto");
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBack() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }
}