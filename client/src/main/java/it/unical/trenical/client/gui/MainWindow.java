package it.unical.trenical.client.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Finestra principale dell'applicazione TreniCal.
 * Puoi estendere questa classe per aggiungere nuove funzionalità alla GUI.
 */
public class MainWindow extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TreniCal - Gestione Treni e Biglietti");
        Label label = new Label("Benvenuto in TreniCal!");
        Scene scene = new Scene(label, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}