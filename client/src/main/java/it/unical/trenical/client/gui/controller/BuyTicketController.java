package it.unical.trenical.client.gui.controller;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.gui.SelectedTrainService;
import it.unical.trenical.client.gui.util.AlertUtils;
import it.unical.trenical.client.gui.util.AutoCompleteUtil;
import it.unical.trenical.client.session.UserSession;
import it.unical.trenical.grpc.common.Train;

import it.unical.trenical.grpc.ticket.PurchaseTicketRequest;
import it.unical.trenical.grpc.ticket.PurchaseTicketResponse;
import it.unical.trenical.grpc.ticket.TicketServiceGrpc;
import it.unical.trenical.grpc.train.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;

/**
 * Controller per la schermata di acquisto biglietto.
 */
public class BuyTicketController {
    @FXML
    private TextField trainField;
    @FXML
    private ComboBox<String> classBox;
    @FXML
    private Spinner<Integer> seatsSpinner;
    @FXML
    private RadioButton creditCardRadio;
    @FXML
    private RadioButton digitalWalletRadio;
    @FXML
    private TextField departureStationField;
    @FXML
    private TextField arrivalStationField;
    @FXML
    private Label priceLabel;
    @FXML
    private Label seatsAvailableLabel;

    // Servizio per l'accesso ai dati dei treni
    private TrainServiceGrpc.TrainServiceBlockingStub trainService;
    // Servizio per l'accesso ai dati dei biglietti
    private TicketServiceGrpc.TicketServiceBlockingStub ticketService;

    // Mappa nome treno → oggetto Train (o ID)
    private Map<String, Train> trainNameToTrain = new HashMap<>();
    private Train selectedTrain = null; // Treno effettivamente selezionato
    private int seatsSpinnerMax = 10; // Valore massimo attuale dei posti selezionabili

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
        AutoCompleteUtil.setupAutoComplete(trainField, this::fetchTrainSuggestions);
        // Listener: quando il campo cambia e corrisponde a un nome in mappa, aggiorna selectedTrain!
        trainField.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedTrain = trainNameToTrain.get(newVal);
            updateSeatsSpinnerMax();
        });
        // Aggiungi suggerimenti per stazioni
        AutoCompleteUtil.setupAutoComplete(departureStationField, this::fetchStationSuggestions);
        AutoCompleteUtil.setupAutoComplete(arrivalStationField, this::fetchStationSuggestions);

        // Aggiorna il prezzo ogni volta che cambiano classe, posti o treno
        classBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        seatsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        trainField.textProperty().addListener((obs, oldVal, newVal) -> updatePrice());

        // Carica il treno selezionato, se presente
        loadSelectedTrain();

        updateSeatsSpinnerMax();
        updatePrice();
    }

    private void updatePrice() {
        if (selectedTrain == null) {
            priceLabel.setText("Prezzo: -");
            return;
        }
        // Validazione avanzata dei campi
        String partenza = departureStationField.getText();
        String arrivo = arrivalStationField.getText();
        if (partenza == null || partenza.isBlank() || arrivo == null || arrivo.isBlank()) {
            priceLabel.setText("Prezzo: -");
            return;
        }
        if (partenza.trim().equalsIgnoreCase(arrivo.trim())) {
            priceLabel.setText("Errore: stazioni uguali");
            return;
        }
        try {
            String username = UserSession.getUsername();
            if (username == null || username.isEmpty()) {
                priceLabel.setText("Prezzo: -");
                return;
            }
            int seats = seatsSpinner.getValue();
            // Chiamata al server per calcolare il prezzo unitario
            PurchaseTicketRequest req = PurchaseTicketRequest.newBuilder()
                    .setTrainId(selectedTrain.getId())
                    .setDepartureStation(partenza)
                    .setArrivalStation(arrivo)
                    .setServiceClass(classBox.getValue())
                    .setSeats(1)
                    .setPassengerName(username)
                    .setTravelDate(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build())
                    .build();
            PurchaseTicketResponse resp = ticketService.purchaseTicket(req);
            double total = resp.getPrice() * seats;
            priceLabel.setText("Prezzo: " + String.format("%.2f", total) + " €");
        } catch (Exception e) {
            priceLabel.setText("Prezzo: errore");
        }
    }

    private void updateSeatsSpinnerMax() {
        if (selectedTrain == null) {
            seatsSpinnerMax = 10;
            seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, seatsSpinnerMax, 1));
            if (seatsAvailableLabel != null) seatsAvailableLabel.setText("Disponibili: -");
            return;
        }
        try {
            int trainId = selectedTrain.getId();
            TrainRequest req = TrainRequest.newBuilder().setId(trainId).build();
            TrainResponse resp = trainService.getTrains(req);
            int available = 10; // default fallback
            // Prova a leggere il campo "posti disponibili" dal nome del treno (workaround)
            if (!resp.getTrainsList().isEmpty()) {
                Train t = resp.getTrainsList().get(0);
                String name = t.getName();
                if (name != null && name.contains("posti:")) {
                    try {
                        String[] parts = name.split("posti:");
                        available = Integer.parseInt(parts[1].replaceAll("\\D", ""));
                    } catch (Exception ignored) {}
                }
            }
            if (available < 1) available = 1;
            seatsSpinnerMax = available;
            seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, seatsSpinnerMax, 1));
            if (seatsAvailableLabel != null) seatsAvailableLabel.setText("Disponibili: " + seatsSpinnerMax);
        } catch (Exception e) {
            seatsSpinnerMax = 10;
            seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, seatsSpinnerMax, 1));
            if (seatsAvailableLabel != null) seatsAvailableLabel.setText("Disponibili: -");
        }
    }

    /**
     * Recupera i suggerimenti per i treni in base all'input dell'utente.
     * Mostra i nomi dei treni e associa internamente il nome selezionato all'oggetto Train (per ottenere l'ID).
     * Se la query è numerica, cerca anche direttamente per ID.
     * Usa la mappa trainNameToTrain per associare il nome visualizzato al Train gRPC.
     */
    private List<String> fetchTrainSuggestions(String query) {
        trainNameToTrain.clear();
        try {
            // Prepara le liste risultati
            List<Train> trainsByName = new ArrayList<>();
            List<Train> trainsById = new ArrayList<>();
            boolean isNumeric = query.matches("\\d+");

            // --- Ricerca per nome (filtra lato client perché il backend non ha campo nome diretto) ---
            SearchTrainRequest nameReq = SearchTrainRequest.newBuilder()
                    .setDepartureStation("") // Vuoti per ricerca generica
                    .setArrivalStation("")
                    .setDate(Timestamp.getDefaultInstance())
                    .build();
            TrainResponse nameResp = trainService.searchTrains(nameReq);
            trainsByName = nameResp.getTrainsList().stream()
                    .filter(t -> t.getName().toLowerCase().contains(query.toLowerCase()))
                    .toList();

            // --- Ricerca per ID se la query è numerica ---
            if (isNumeric) {
                try {
                    int id = Integer.parseInt(query);
                    TrainRequest idReq = TrainRequest.newBuilder().setId(id).build();
                    TrainResponse idResp = trainService.getTrains(idReq);
                    trainsById = idResp.getTrainsList();
                } catch (NumberFormatException ignored) {
                }
            }

            // --- Unisci risultati rimuovendo duplicati (per ID) ---
            Map<Integer, Train> idToTrain = new LinkedHashMap<>();
            for (Train t : trainsByName) idToTrain.put(t.getId(), t);
            for (Train t : trainsById) idToTrain.putIfAbsent(t.getId(), t);

            // --- Popola la mappa nome→Train per la selezione successiva ---
            for (Train t : idToTrain.values()) trainNameToTrain.put(t.getName(), t);

            // --- Restituisci solo i nomi per i suggerimenti ---
            return idToTrain.values().stream().map(Train::getName).toList();
        } catch (Exception e) {
            // Fallback DEMO: suggerimenti statici se il server non risponde
            List<Train> trains = Arrays.asList(
                    Train.newBuilder().setId(1).setName("Frecciarossa 1000").build(),
                    Train.newBuilder().setId(2).setName("Italo").build(),
                    Train.newBuilder().setId(3).setName("Intercity").build()
            );
            trainNameToTrain.clear();
            return trains.stream()
                    .filter(train -> train.getName().toLowerCase().contains(query.toLowerCase()))
                    .peek(train -> trainNameToTrain.put(train.getName(), train))
                    .map(Train::getName)
                    .toList();
        }
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
            // Precompila i campi stazione
            departureStationField.setText(selectedTrain.getDepartureStation());
            arrivalStationField.setText(selectedTrain.getArrivalStation());
        }
    }

    /**
     * Gestisce l'acquisto biglietto.
     */
    @FXML
    private void onBuy() {
        // Validazione avanzata dei campi
        String partenza = departureStationField.getText();
        String arrivo = arrivalStationField.getText();
        if (trainField.getText().isEmpty()) {
            AlertUtils.showError("Errore", "Seleziona un treno");
            return;
        }
        if (selectedTrain == null) {
            AlertUtils.showError("Errore", "Seleziona un treno valido dalla lista.");
            return;
        }
        if (partenza == null || partenza.isBlank() || arrivo == null || arrivo.isBlank()) {
            AlertUtils.showError("Errore", "Compila sia la stazione di partenza che quella di arrivo.");
            return;
        }
        if (partenza.trim().equalsIgnoreCase(arrivo.trim())) {
            AlertUtils.showError("Errore", "Le stazioni di partenza e arrivo devono essere diverse.");
            return;
        }

        // Verifica che l'utente sia loggato
        String username = UserSession.getUsername();
        if (username == null || username.isEmpty()) {
            AlertUtils.showError("Errore", "Utente non loggato. Effettua il login.");
            return;
        }

        // Determina il metodo di pagamento selezionato
        String paymentMethod = creditCardRadio.isSelected() ?
                "Carta di Credito" : "Portafoglio Digitale";

        int seatsRequested = seatsSpinner.getValue();
        int maxSeats = seatsSpinnerMax;
        if (seatsRequested > maxSeats) {
            AlertUtils.showError("Errore", "Non ci sono abbastanza posti disponibili su questo treno.");
            return;
        }

        // Costruisci la richiesta per l'acquisto biglietto
        PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                .setTrainId(selectedTrain.getId())
                .setPassengerName(username)
                .setDepartureStation(departureStationField.getText())
                .setArrivalStation(arrivalStationField.getText())
                .setTravelDate(Timestamp.newBuilder()
                        .setSeconds(System.currentTimeMillis() / 1000)
                        .build())
                .setServiceClass(classBox.getValue())
                .setPaymentMethod(paymentMethod)
                .setSeats(seatsSpinner.getValue())
                .build();

        try {
            PurchaseTicketResponse response = ticketService.purchaseTicket(request);
            if (response.getSuccess()) {
                // Mostra riepilogo biglietto acquistato
                StringBuilder sb = new StringBuilder();
                sb.append("Treno: ").append(selectedTrain.getName()).append("\n");
                sb.append("Da: ").append(partenza).append("  A: ").append(arrivo).append("\n");
                sb.append("Classe: ").append(classBox.getValue()).append("\n");
                sb.append("Posti: ").append(seatsSpinner.getValue()).append("\n");
                sb.append("Prezzo totale: ").append(String.format("%.2f", response.getPrice() * seatsSpinner.getValue())).append(" €\n");
                sb.append("Codice biglietto: ").append(response.getTicketId());
                AlertUtils.showInfo("Biglietto acquistato", sb.toString());
                SceneManager.getInstance().switchTo(SceneManager.MY_TICKETS);
            } else {
                AlertUtils.showError("Errore", response.getMessage());
            }
        } catch (Exception e) {
            AlertUtils.showError("Errore", "Impossibile completare l'acquisto. Riprova più tardi.");
        }
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }
}
