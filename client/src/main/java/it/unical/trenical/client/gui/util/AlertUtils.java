package it.unical.trenical.client.gui.util;

import javafx.scene.control.Alert;

/**
 * Utility per la gestione di finestre di dialogo e alert.
 * Fornisce metodi semplificati per mostrare messaggi informativi, errori e conferme.
 */
public class AlertUtils {

    /**
     * Mostra un messaggio informativo all'utente.
     * @param title Il titolo della finestra di dialogo
     * @param message Il messaggio da visualizzare
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra un messaggio di errore all'utente.
     * @param title Il titolo della finestra di dialogo
     * @param message Il messaggio di errore da visualizzare
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra una finestra di conferma e restituisce la risposta dell'utente.
     * @param title Il titolo della finestra di dialogo
     * @param message Il messaggio di conferma da visualizzare
     * @return true se l'utente ha premuto OK, false altrimenti
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
    }
}
