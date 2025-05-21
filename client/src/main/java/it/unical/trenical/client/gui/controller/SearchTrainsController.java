package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.model.TrainSearchResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller per la schermata di ricerca treni.
 */
public class SearchTrainsController {
    @FXML private TextField departureField;
    @FXML private TextField arrivalField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> trainTypeBox;
    @FXML private ComboBox<String> classBox;
    @FXML private TableView<TrainSearchResult> resultsTable;
    @FXML private TableColumn<TrainSearchResult, String> colTrain;
    @FXML private TableColumn<TrainSearchResult, String> colDeparture;
    @FXML private TableColumn<TrainSearchResult, String> colArrival;
    @FXML private TableColumn<TrainSearchResult, String> colTime;
    @FXML private TableColumn<TrainSearchResult, Integer> colSeats;

    private final ObservableList<TrainSearchResult> searchResults = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Inizializza colonne tabella
        colTrain.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTrainName()));
        colDeparture.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDepartureStation()));
        colArrival.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getArrivalStation()));
        colTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTime().toString()));
        colSeats.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getAvailableSeats()).asObject());
        resultsTable.setItems(searchResults);

        // Popola le comboBox (mock)
        trainTypeBox.setItems(FXCollections.observableArrayList("Regionale", "Intercity", "Frecciarossa"));
        classBox.setItems(FXCollections.observableArrayList("Prima", "Seconda"));
    }

    /**
     * Gestisce la ricerca dei treni.
     */
    @FXML
    private void onSearch() {
        String partenza = departureField.getText();
        String arrivo = arrivalField.getText();
        LocalDate data = datePicker.getValue();

        // Validazione input base
        if (partenza == null || partenza.isBlank() || arrivo == null || arrivo.isBlank() || data == null) {
            showAlert("Errore", "Compila tutti i campi obbligatori (partenza, arrivo, data).");
            return;
        }

        // Per ora: mock di risultati
        searchResults.setAll(mockTrainSearch(partenza, arrivo, data));
    }

    /**
     * Mock: restituisce una lista di risultati fittizi per la ricerca treni.
     */
    private List<TrainSearchResult> mockTrainSearch(String partenza, String arrivo, LocalDate data) {
        return List.of(
                new TrainSearchResult("Regionale 123", partenza, arrivo, data, LocalTime.of(9, 30), 35),
                new TrainSearchResult("Intercity 456", partenza, arrivo, data, LocalTime.of(11, 15), 18),
                new TrainSearchResult("Frecciarossa 789", partenza, arrivo, data, LocalTime.of(14, 45), 5)
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }
}