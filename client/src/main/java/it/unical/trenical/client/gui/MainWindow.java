package it.unical.trenical.client.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point dell'applicazione, inizializza il SceneManager e carica la dashboard.
 */
public class MainWindow extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneManager.getInstance().setPrimaryStage(primaryStage);
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
        primaryStage.setTitle("TreniCal - Gestione Treni e Biglietti");
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}