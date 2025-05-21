package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller per la schermata di acquisto biglietto.
 */
public class BuyTicketController {
    @FXML private TextField trainField;
    @FXML private ComboBox<String> classBox;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private RadioButton creditCardRadio;
    @FXML private RadioButton digitalWalletRadio;

    /**
     * Gestisce l'acquisto biglietto.
     */
    @FXML
    private void onBuy() {
        // Da implementare: chiamata service gRPC per acquisto biglietto
        System.out.println("Acquisto biglietto avviato");
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }
}