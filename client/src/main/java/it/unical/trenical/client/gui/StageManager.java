package it.unical.trenical.client.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility centralizzata per la gestione delle schermate nell'applicazione TreniCal.
 * Permette di cambiare schermata caricando nuovi FXML nello stage principale.
 */
public class StageManager {

    private final Stage primaryStage;

    public StageManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Carica e mostra una nuova schermata FXML.
     * @param fxmlName nome del file FXML (es: "dashboard.fxml")
     */
    public void showView(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/it/unical/trenical/client/gui/" + fxmlName)
            ));
            Parent root = loader.load();

            // Passa lo StageManager al controller, se implementa la nostra interfaccia
            Object controller = loader.getController();
            if (controller instanceof StageManagerAware) {
                ((StageManagerAware) controller).setStageManager(this);
            }

            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Puoi mostrare un dialogo di errore qui
        }
    }
}