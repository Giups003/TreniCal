package it.unical.trenical.client.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Gestisce il cambio centralizzato delle schermate principali dell'applicazione.
 * Singleton accessibile da tutti i controller.
 */
public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

    // Percorsi FXML delle schermate
    public static final String DASHBOARD = "src/main/java/it/unical/trenical/client/gui/view/dashboard.fxml";
    public static final String SEARCH_TRAINS = "src/main/java/it/unical/trenical/client/gui/view/search_trains.fxml";
    public static final String BUY_TICKET = "src/main/java/it/unical/trenical/client/gui/view/buy_ticket.fxml";
    public static final String MY_TICKETS = "src/main/java/it/unical/trenical/client/gui/view/my_tickets.fxml";
    public static final String LOGIN = "src/main/java/it/unical/trenical/client/gui/view/login.fxml";

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        // Mostra la schermata di login solo se non c'è un utente salvato
        String savedUsername = java.util.prefs.Preferences.userNodeForPackage(it.unical.trenical.client.gui.controller.LoginController.class)
                .get("trenical_username", "");
        if (savedUsername == null || savedUsername.isEmpty()) {
            showLogin();
        } else {
            switchTo(DASHBOARD);
        }
    }

    /**
     * Carica e mostra una schermata FXML.
     * @param fxmlPath percorso della risorsa FXML
     */
    public void switchTo(String fxmlPath) {
        if (primaryStage == null) {
            System.err.println("ERRORE: primaryStage non è stato inizializzato");
            return;
        }

        try {
            File file = new File(fxmlPath);

            if (!file.exists()) {
                System.err.println("ERRORE: Impossibile trovare il file FXML: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(file.toURI().toURL());
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERRORE durante il caricamento della schermata: " + e.getMessage());
            // Puoi mostrare un alert di errore qui se vuoi
        }
    }
    /**
     * Mostra la schermata di acquisto biglietti.
     */
    public void showTicketPurchaseView() {
        switchTo(BUY_TICKET);
    }

    /**
     * Mostra la schermata di login.
     */
    public void showLogin() {
        switchTo(LOGIN);
    }
}

