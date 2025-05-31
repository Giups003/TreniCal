package it.unical.trenical.client.gui.controller;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.gui.SelectedTrainService;
import it.unical.trenical.grpc.common.Train;

import it.unical.trenical.grpc.ticket.PurchaseTicketRequest;
import it.unical.trenical.grpc.ticket.PurchaseTicketResponse;
import it.unical.trenical.grpc.ticket.TicketServiceGrpc;
import it.unical.trenical.grpc.train.SearchStationRequest;
import it.unical.trenical.grpc.train.SearchStationResponse;
import it.unical.trenical.grpc.train.TrainServiceGrpc;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.List;

/**
 * Controller per la schermata di acquisto biglietto.
 */
public class BuyTicketController {
    @FXML private TextField trainField;
    @FXML private ComboBox<String> classBox;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private RadioButton creditCardRadio;
    @FXML private RadioButton digitalWalletRadio;
    @FXML private TextField departureStationField;
    @FXML private TextField arrivalStationField;

    // Servizio per l'accesso ai dati dei treni
    private TrainServiceGrpc.TrainServiceBlockingStub trainService;
    // Servizio per l'accesso ai dati dei biglietti
    private TicketServiceGrpc.TicketServiceBlockingStub ticketService;

    /**
     * Inizializza il controller.
     */
    @FXML
    public void initialize() {
        // Crea il canale gRPC per comunicare con il server
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        trainService = TrainServiceGrpc.newBlockingStub(channel);
        ticketService = TicketServiceGrpc.newBlockingStub(channel);
        // Inizializza la combo box per la classe
        classBox.getItems().addAll("Prima Classe", "Seconda Classe");
        classBox.setValue("Seconda Classe"); // Valore predefinito

        // Configura lo spinner per i posti
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        seatsSpinner.setValueFactory(valueFactory);

        // Crea un gruppo di toggle per i metodi di pagamento
        ToggleGroup paymentGroup = new ToggleGroup();
        creditCardRadio.setToggleGroup(paymentGroup);
        digitalWalletRadio.setToggleGroup(paymentGroup);
        creditCardRadio.setSelected(true); // Seleziona come predefinito

        // Implementa la lista dei suggerimenti per il campo treno
        setupTrainSuggestions();

        // Aggiungi suggerimenti per stazioni
        setupStationAutoComplete(departureStationField);
        setupStationAutoComplete(arrivalStationField);

        // Carica il treno selezionato, se presente
        loadSelectedTrain();
    }

    /**
     * Configura il sistema di suggerimenti per il campo treno.
     */
    private void setupTrainSuggestions() {
        ListView<String> suggestionsList = new ListView<>();
        suggestionsList.setPrefHeight(180);
        suggestionsList.setMaxHeight(220);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(suggestionsList);

        trainField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                popup.hide();
            } else {
                List<String> suggestions = fetchTrainSuggestions(newVal);
                suggestionsList.getItems().setAll(suggestions);
                if (!suggestions.isEmpty()) {
                    if (!popup.isShowing()) {
                        Bounds bounds = trainField.localToScreen(trainField.getBoundsInLocal());
                        popup.show(trainField, bounds.getMinX(), bounds.getMaxY());
                    }
                } else {
                    popup.hide();
                }
            }
        });

        suggestionsList.setOnMouseClicked(e -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                trainField.setText(selected);
                popup.hide();
            }
        });

        suggestionsList.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                String selected = suggestionsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    trainField.setText(selected);
                    popup.hide();
                }
            }
        });

        trainField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                popup.hide();
            }
        });
    }

    /**
     * Configura il sistema di suggerimenti per il campo stazione.
     */
    private void setupStationAutoComplete(TextField stationField) {
        ListView<String> suggestionsList = new ListView<>();
        suggestionsList.setPrefHeight(180);
        suggestionsList.setMaxHeight(220);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(suggestionsList);

        stationField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                popup.hide();
            } else {
                List<String> suggestions = fetchStationSuggestions(newVal);
                suggestionsList.getItems().setAll(suggestions);
                if (!suggestions.isEmpty()) {
                    if (!popup.isShowing()) {
                        Bounds bounds = stationField.localToScreen(stationField.getBoundsInLocal());
                        popup.show(stationField, bounds.getMinX(), bounds.getMaxY());
                    }
                } else {
                    popup.hide();
                }
            }
        });

        suggestionsList.setOnMouseClicked(e -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                stationField.setText(selected);
                popup.hide();
            }
        });

        suggestionsList.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                String selected = suggestionsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    stationField.setText(selected);
                    popup.hide();
                }
            }
        });

        stationField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                popup.hide();
            }
        });
    }

    /**
     * Recupera i suggerimenti per i treni in base all'input dell'utente.
     *
     * @param query Il testo inserito dall'utente
     * @return Una lista di nomi di treni che corrispondono alla query
     */
    private List<String> fetchTrainSuggestions(String query) {
        // Implementazione temporanea con dati di esempio
        List<String> allTrains = Arrays.asList(
            "Frecciarossa 1000", "Frecciabianca", "Frecciargento",
            "Italo", "Intercity", "Regionale", "Eurocity"
        );

        // Filtra i risultati in base alla query
        return allTrains.stream()
                .filter(train -> train.toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Recupera i suggerimenti per le stazioni in base all'input dell'utente.
     *
     * @param query Il testo inserito dall'utente
     * @return Una lista di nomi di stazioni che corrispondono alla query
     */
    private List<String> fetchStationSuggestions(String query) {
        try {
            SearchStationRequest request = SearchStationRequest.newBuilder()
                    .setQuery(query)
                    .setLimit(10)
                    .build();
            SearchStationResponse response = trainService.searchStations(request);
            return response.getStationsList().stream()
                    .map(station -> station.getName())
                    .toList();
        } catch (Exception e) {
            // fallback demo
            List<String> mockStations = Arrays.asList(
                "Roma Termini", "Milano Centrale", "Napoli Centrale", "Firenze SMN", "Bologna Centrale"
            );
            String queryLower = query.toLowerCase();
            return mockStations.stream()
                    .filter(station -> station.toLowerCase().contains(queryLower))
                    .limit(10)
                    .toList();
        }
    }

    /**
     * Carica il treno selezionato dalla schermata di ricerca, se presente.
     */
    private void loadSelectedTrain() {
        Train selectedTrain = SelectedTrainService.getInstance().getSelectedTrain();
        if (selectedTrain != null) {
            trainField.setText(selectedTrain.getName());
            // Puoi anche preselezionare altri campi se necessario
        }
    }

    /**
     * Gestisce l'acquisto biglietto.
     */
    @FXML
    private void onBuy() {
        // Verifica che tutti i campi siano completi
        if (trainField.getText().isEmpty()) {
            showAlert("Errore", "Seleziona un treno");
            return;
        }

        // Determina il metodo di pagamento selezionato
        String paymentMethod = creditCardRadio.isSelected() ?
                "Carta di Credito" : "Portafoglio Digitale";

        // Costruisci la richiesta per l'acquisto biglietto
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(Integer.parseInt(trainField.getText()))
                .setPassengerName("Nome Passeggero") // Sostituisci con il nome reale
                .setDepartureStation("Stazione partenza") // Sostituisci con valore reale
                .setArrivalStation("Stazione arrivo") // Sostituisci con valore reale
                .setTravelDate( Timestamp.newBuilder()
                        .setSeconds(System.currentTimeMillis() / 1000)
                        .build())
                .setServiceClass(classBox.getValue())
                .setPaymentMethod(paymentMethod)
                .setSeats(seatsSpinner.getValue())
                .build();

        try {
            PurchaseTicketResponse response = ticketService.purchaseTicket(request);
            if (response.getSuccess()) {
                showAlert("Successo", "Biglietto acquistato con successo!");
                SceneManager.getInstance().switchTo(SceneManager.MY_TICKETS);
            } else {
                showAlert("Errore", response.getMessage());
            }
        } catch (Exception e) {
            showAlert("Errore", "Impossibile completare l'acquisto. Riprova più tardi.");
        }
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }

    /**
     * Mostra un alert informativo.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
