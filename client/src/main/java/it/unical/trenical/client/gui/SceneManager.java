package it.unical.trenical.client.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Gestisce il cambio centralizzato delle schermate principali dell'applicazione.
 * Singleton accessibile da tutti i controller.
 */
public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

    // Percorsi FXML delle schermate
    public static final String DASHBOARD = "/it/unical/trenical/client/gui/dashboard.fxml";
    public static final String SEARCH_TRAINS = "/it/unical/trenical/client/gui/search_trains.fxml";
    public static final String BUY_TICKET = "/it/unical/trenical/client/gui/buy_ticket.fxml";
    public static final String MY_TICKETS = "/it/unical/trenical/client/gui/my_tickets.fxml";

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Carica e mostra una schermata FXML.
     * @param fxmlPath percorso della risorsa FXML
     */
    public void switchTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            // Puoi mostrare un alert di errore qui se vuoi
        }
    }
}