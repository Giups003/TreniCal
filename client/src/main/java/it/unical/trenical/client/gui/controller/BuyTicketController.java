package it.unical.trenical.client.gui.controller;

import com.google.protobuf.Timestamp;
import com.google.protobuf.Empty;
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
import it.unical.trenical.grpc.promotion.PromotionServiceGrpc;
import it.unical.trenical.grpc.promotion.PromotionList;
import it.unical.trenical.grpc.promotion.Promotion;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.*;
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
    @FXML
    private TextField promoCodeField;
    @FXML
    private Button validatePromoButton;
    @FXML
    private Button buyButton;
    @FXML
    private Label promoValidationLabel;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> timeBox;

    // Servizio per l'accesso ai dati dei treni
    private TrainServiceGrpc.TrainServiceBlockingStub trainService;
    // Servizio per l'accesso ai dati dei biglietti
    private TicketServiceGrpc.TicketServiceBlockingStub ticketService;
    // Servizio per l'accesso ai dati delle promozioni
    private PromotionServiceGrpc.PromotionServiceBlockingStub promotionService;

    // Mappa nome treno → oggetto Train (o ID)
    private Map<String, Train> trainNameToTrain = new HashMap<>();
    private Train selectedTrain = null; // Treno effettivamente selezionato
    private int seatsSpinnerMax = 10; // Valore massimo attuale dei posti selezionabili
    private boolean promoValid = false; // Indica se il codice promo è valido
    private double promoPrice = 0.0; // Prezzo con promo applicata, se valida
    private Set<String> promoCodesFromServer = new HashSet<>(); // Codici promo caricati dal server

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
        promotionService = PromotionServiceGrpc.newBlockingStub(channel);
        // Carica i codici promozionali dal server
        loadPromoCodesFromServer();
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
        setupTrainFieldListener();
        // Aggiungi suggerimenti per stazioni
        AutoCompleteUtil.setupAutoComplete(departureStationField, this::fetchStationSuggestions);
        AutoCompleteUtil.setupAutoComplete(arrivalStationField, this::fetchStationSuggestions);

        // Aggiorna il prezzo ogni volta che cambiano classe, posti o treno
        classBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        seatsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        trainField.textProperty().addListener((obs, oldVal, newVal) -> updatePrice());

        // Listener per aggiornare il treno in base a partenza e arrivo
        departureStationField.textProperty().addListener((obs, oldVal, newVal) -> updateTrainByStations());
        arrivalStationField.textProperty().addListener((obs, oldVal, newVal) -> updateTrainByStations());
        if (datePicker != null) {
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateTrainByStations());
        }
        if (timeBox != null) {
            timeBox.valueProperty().addListener((obs, oldVal, newVal) -> updateTrainByStations());
        }

        // Listener per aggiornare orari disponibili quando cambia la data o le stazioni
        if (datePicker != null) {
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTimes());
        }
        if (departureStationField != null) {
            departureStationField.textProperty().addListener((obs, oldVal, newVal) -> updateAvailableTimes());
        }
        if (arrivalStationField != null) {
            arrivalStationField.textProperty().addListener((obs, oldVal, newVal) -> updateAvailableTimes());
        }
        // Listener per aggiornare il prezzo anche su cambio orario/data
        if (timeBox != null) {
            timeBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        }
        if (datePicker != null) {
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        }

        // Carica il treno selezionato, se presente
        loadSelectedTrain();
        // Esegui ricerca automatica se i campi sono già valorizzati
        if (departureStationField.getText() != null && !departureStationField.getText().isBlank() &&
            arrivalStationField.getText() != null && !arrivalStationField.getText().isBlank() &&
            datePicker.getValue() != null) {
            updateAvailableTimes();
            updateTrainByStations();
        }
        // Imposta la data e l'orario uguali a quelli selezionati in ricerca treni, se presenti
        LocalDate searchDate = it.unical.trenical.client.gui.SelectedTrainService.getInstance().getSelectedDate();
        LocalTime searchTime = it.unical.trenical.client.gui.SelectedTrainService.getInstance().getSelectedTime();
        if (searchDate != null && datePicker != null) {
            datePicker.setValue(searchDate);
        } else if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }
        if (searchTime != null && timeBox != null) {
            updateAvailableTimes();
            String timeStr = searchTime.toString();
            if (!timeBox.getItems().contains(timeStr)) {
                timeBox.getItems().add(timeStr);
            }
            timeBox.setValue(timeStr);
        }
        // Impedisci selezione di date antecedenti a oggi
        if (datePicker != null) {
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(java.time.LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(empty || item.isBefore(java.time.LocalDate.now()));
                }
            });
        }
        updateSeatsSpinnerMax();
        updatePrice();

        if (trainField.getTooltip() == null)
            trainField.setTooltip(new Tooltip("Il treno è selezionato automaticamente in base a partenza e arrivo"));
        if (classBox.getTooltip() == null)
            classBox.setTooltip(new Tooltip("Seleziona la classe del biglietto"));
        if (seatsSpinner.getTooltip() == null)
            seatsSpinner.setTooltip(new Tooltip("Seleziona il numero di posti da acquistare"));
        if (promoCodeField.getTooltip() == null)
            promoCodeField.setTooltip(new Tooltip("Inserisci un codice promozionale se disponibile"));
        if (validatePromoButton.getTooltip() == null)
            validatePromoButton.setTooltip(new Tooltip("Verifica la validità del codice promozionale"));
        if (creditCardRadio.getTooltip() == null)
            creditCardRadio.setTooltip(new Tooltip("Paga con carta di credito"));
        if (digitalWalletRadio.getTooltip() == null)
            digitalWalletRadio.setTooltip(new Tooltip("Paga con portafoglio digitale"));
        if (buyButton.getTooltip() == null)
            buyButton.setTooltip(new Tooltip("Procedi all'acquisto del biglietto"));
    }

    private void updateAvailableTimes() {
        timeBox.getItems().clear();
        String dep = departureStationField.getText();
        String arr = arrivalStationField.getText();
        var date = datePicker.getValue();
        if (dep == null || dep.isBlank() || arr == null || arr.isBlank() || date == null) return;
        try {
            ScheduleRequest req = ScheduleRequest.newBuilder()
                    .setStation(dep)
                    .setDate(Timestamp.newBuilder().setSeconds(date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()).build())
                    .build();
            ScheduleResponse resp = trainService.getTrainSchedule(req);
            LocalDateTime now = LocalDateTime.now();
            List<String> availableTimes = resp.getDeparturesList().stream()
                    .filter(entry -> entry.getDestination().equalsIgnoreCase(arr))
                    .map(entry -> {
                        Instant instant = Instant.ofEpochSecond(entry.getTime().getSeconds());
                        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                        return ldt.toLocalTime().toString();
                    })
                    .distinct()
                    .sorted()
                    .toList();
            timeBox.getItems().addAll(availableTimes);
            if (!availableTimes.isEmpty()) {
                timeBox.setValue(availableTimes.get(0));
            }
        } catch (Exception e) {
            // fallback: orari fissi
            for (int h = 6; h <= 22; h++) timeBox.getItems().add(String.format("%02d:00", h));
            timeBox.setValue("06:00");
        }
    }

    private void updatePrice() {
        if (selectedTrain == null) {
            priceLabel.setText("Prezzo: -");
            return;
        }
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
        java.time.LocalDate date = datePicker != null ? datePicker.getValue() : null;
        String time = timeBox != null ? timeBox.getValue() : null;
        if (date == null || time == null || time.isBlank()) {
            priceLabel.setText("Prezzo: -");
            return;
        }
        try {
            String username = UserSession.getUsername();
            if (username == null || username.isEmpty()) {
                priceLabel.setText("Prezzo: -");
                return;
            }
            int seats = seatsSpinner.getValue();
            String promoCode = promoCodeField != null ? promoCodeField.getText() : "";
            double total;
            LocalTime localTime = LocalTime.parse(time);
            LocalDateTime ldt = LocalDateTime.of(date, localTime);
            if (promoValid && promoCode != null && !promoCode.isBlank()) {
                total = promoPrice * seats;
            } else {
                PurchaseTicketRequest req = PurchaseTicketRequest.newBuilder()
                        .setTrainId(selectedTrain.getId())
                        .setDepartureStation(partenza)
                        .setArrivalStation(arrivo)
                        .setServiceClass(classBox.getValue())
                        .setSeats(1)
                        .setPassengerName(username)
                        .setTravelDate(Timestamp.newBuilder().setSeconds(ldt.toEpochSecond(ZoneOffset.UTC)).build())
                        .setPromoCode("")
                        .build();
                PurchaseTicketResponse resp = ticketService.purchaseTicket(req);
                total = resp.getPrice() * seats;
            }
            priceLabel.setText("Prezzo: " + String.format("%.2f", total) + " €");
        } catch (Exception e) {
            priceLabel.setText("Prezzo: errore");
        }
    }

    private double getCurrentBasePrice() {
        // Simula richiesta senza promo
        try {
            if (selectedTrain == null) return 0.0;
            String partenza = departureStationField.getText();
            String arrivo = arrivalStationField.getText();
            String username = UserSession.getUsername();
            if (username == null || username.isEmpty()) return 0.0;
            PurchaseTicketRequest req = PurchaseTicketRequest.newBuilder()
                    .setTrainId(selectedTrain.getId())
                    .setDepartureStation(partenza)
                    .setArrivalStation(arrivo)
                    .setServiceClass(classBox.getValue())
                    .setSeats(1)
                    .setPassengerName(username)
                    .setTravelDate(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build())
                    .setPromoCode("")
                    .build();
            PurchaseTicketResponse resp = ticketService.purchaseTicket(req);
            return resp.getPrice();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getPromoPrice(String promoCode) {
        try {
            if (selectedTrain == null) return 0.0;
            String partenza = departureStationField.getText();
            String arrivo = arrivalStationField.getText();
            String username = UserSession.getUsername();
            if (username == null || username.isEmpty()) return 0.0;
            PurchaseTicketRequest req = PurchaseTicketRequest.newBuilder()
                    .setTrainId(selectedTrain.getId())
                    .setDepartureStation(partenza)
                    .setArrivalStation(arrivo)
                    .setServiceClass(classBox.getValue())
                    .setSeats(1)
                    .setPassengerName(username)
                    .setTravelDate(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build())
                    .setPromoCode(promoCode)
                    .build();
            PurchaseTicketResponse resp = ticketService.purchaseTicket(req);
            return resp.getPrice();
        } catch (Exception e) {
            return 0.0;
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
            TrainDetailsRequest req = TrainDetailsRequest.newBuilder().setTrainId(trainId).build();
            TrainDetailsResponse resp = trainService.getTrainDetails(req);
            int available = resp.getSeatsAvailable();
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
        Train selected = SelectedTrainService.getInstance().getSelectedTrain();
        if (selected != null) {
            // Precompila i campi stazione e treno
            departureStationField.setText(selected.getDepartureStation());
            arrivalStationField.setText(selected.getArrivalStation());
            trainField.setText(selected.getName());
            trainField.requestLayout();
            // Imposta selectedTrain e aggiorna la mappa DOPO aver settato i campi
            trainNameToTrain.put(selected.getName(), selected);
            selectedTrain = selected;
            updateSeatsSpinnerMax();
            updatePrice();
        }
    }

    /**
     * Gestisce l'acquisto biglietto.
     */
    @FXML
    private void onBuy() {
        buyButton.setDisable(true);
        String partenza = departureStationField.getText();
        String arrivo = arrivalStationField.getText();
        // Validazione avanzata dei campi
        if (trainField.getText().isEmpty()) {
            AlertUtils.showError("Errore", "Seleziona un treno");
            buyButton.setDisable(false);
            return;
        }
        if (selectedTrain == null) {
            AlertUtils.showError("Errore", "Seleziona un treno valido dalla lista.");
            buyButton.setDisable(false);
            return;
        }
        if (partenza == null || partenza.isBlank() || arrivo == null || arrivo.isBlank()) {
            AlertUtils.showError("Errore", "Compila sia la stazione di partenza che quella di arrivo.");
            buyButton.setDisable(false);
            return;
        }
        if (partenza.trim().equalsIgnoreCase(arrivo.trim())) {
            AlertUtils.showError("Errore", "Le stazioni di partenza e arrivo devono essere diverse.");
            buyButton.setDisable(false);
            return;
        }
        String username = UserSession.getUsername();
        if (username == null || username.isEmpty()) {
            AlertUtils.showError("Errore", "Utente non loggato. Effettua il login.");
            buyButton.setDisable(false);
            return;
        }
        String paymentMethod = creditCardRadio.isSelected() ?
                "Carta di Credito" : "Portafoglio Digitale";
        int seatsRequested = seatsSpinner.getValue();
        int maxSeats = seatsSpinnerMax;
        if (seatsRequested > maxSeats) {
            AlertUtils.showError("Errore", "Non ci sono abbastanza posti disponibili su questo treno.");
            buyButton.setDisable(false);
            return;
        }
        java.time.LocalDate date = datePicker != null ? datePicker.getValue() : null;
        String time = timeBox != null ? timeBox.getValue() : null;
        if (date == null || time == null || time.isBlank()) {
            AlertUtils.showError("Errore", "Seleziona data e orario di partenza.");
            buyButton.setDisable(false);
            return;
        }
        try {
            LocalTime localTime = LocalTime.parse(time);
            LocalDateTime ldt = LocalDateTime.of(date, localTime);
            PurchaseTicketRequest request = PurchaseTicketRequest.newBuilder()
                    .setTrainId(selectedTrain.getId())
                    .setPassengerName(username)
                    .setDepartureStation(departureStationField.getText())
                    .setArrivalStation(arrivalStationField.getText())
                    .setTravelDate(Timestamp.newBuilder().setSeconds(ldt.toEpochSecond(ZoneOffset.UTC)).build())
                    .setServiceClass(classBox.getValue())
                    .setPaymentMethod(paymentMethod)
                    .setSeats(seatsRequested)
                    .build();
            PurchaseTicketResponse response = ticketService.purchaseTicket(request);
            if (response.getSuccess()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Treno: ").append(selectedTrain.getName()).append("\n");
                sb.append("Da: ").append(partenza).append("  A: ").append(arrivo).append("\n");
                sb.append("Classe: ").append(classBox.getValue()).append("\n");
                sb.append("Posti: ").append(seatsRequested).append("\n");
                double totalPrice = 0.0;
                if (response.getTicketsCount() > 0) {
                    sb.append("Codici biglietti:\n");
                    for (var t : response.getTicketsList()) {
                        sb.append("- ").append(t.getId()).append("\n");
                        totalPrice += t.getPrice();
                    }
                } else {
                    sb.append("Codice biglietto: ").append(response.getTicketId()).append("\n");
                    totalPrice = response.getPrice() * seatsRequested;
                }
                sb.append("Prezzo totale: ").append(String.format("%.2f", totalPrice)).append(" €\n");
                AlertUtils.showInfo("Biglietti acquistati", sb.toString());
                SceneManager.getInstance().switchTo(SceneManager.MY_TICKETS);
            } else {
                AlertUtils.showError("Errore", response.getMessage());
            }
        } catch (Exception e) {
            AlertUtils.showError("Errore", "Impossibile completare l'acquisto. Riprova più tardi.");
        } finally {
            buyButton.setDisable(false);
        }
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }

    @FXML
    private void onValidatePromo() {
        String promoCode = promoCodeField.getText();
        if (promoCode == null || promoCode.isBlank()) {
            promoValidationLabel.setText("Inserisci un codice promo.");
            promoValid = false;
            promoPrice = 0.0;
            updatePrice();
            return;
        }
        double oldPrice = getCurrentBasePrice();
        double newPrice = getPromoPrice(promoCode);
        // Se la promo non cambia il prezzo o il prezzo promo è zero/negativo, la promo non è valida
        if (newPrice > 0 && newPrice < oldPrice) {
            promoValidationLabel.setText("Codice valido! Sconto applicato: " + String.format("%.2f", oldPrice - newPrice) + " €");
            promoValid = true;
            promoPrice = newPrice;
        } else {
            promoValidationLabel.setText("Codice non valido o non applicabile.");
            promoValid = false;
            promoPrice = 0.0;
        }
        updatePrice();
    }

    /**
     * Aggiorna il treno selezionato in base a partenza, arrivo, data e orario
     */
    private void updateTrainByStations() {
        String partenza = departureStationField.getText();
        String arrivo = arrivalStationField.getText();
        LocalDate date = (datePicker != null) ? datePicker.getValue() : null;
        String time = (timeBox != null) ? timeBox.getValue() : null;
        if (partenza == null || partenza.isBlank() || arrivo == null || arrivo.isBlank() || date == null || time == null || time.isBlank()) {
            trainField.setText("");
            selectedTrain = null;
            buyButton.setDisable(true);
            updateSeatsSpinnerMax();
            updatePrice();
            return;
        }
        try {
            LocalTime localTime = LocalTime.parse(time);
            LocalDateTime ldt = LocalDateTime.of(date, localTime);
            Timestamp ts = Timestamp.newBuilder().setSeconds(ldt.toEpochSecond(ZoneOffset.UTC)).build();
            SearchTrainRequest req = SearchTrainRequest.newBuilder()
                    .setDepartureStation(partenza)
                    .setArrivalStation(arrivo)
                    .setDate(ts)
                    .build();
            TrainResponse resp = trainService.searchTrains(req);
            // Se il treno precedentemente selezionato è ancora valido, mantienilo
            boolean found = false;
            if (selectedTrain != null && resp.getTrainsList().stream().anyMatch(t -> t.getId() == selectedTrain.getId())) {
                trainField.setText(selectedTrain.getName());
                found = true;
            } else if (!resp.getTrainsList().isEmpty()) {
                // Altrimenti seleziona il primo treno disponibile
                Train t = resp.getTrainsList().stream()
                    .filter(tr -> tr.getDepartureStation().equalsIgnoreCase(partenza)
                            && tr.getArrivalStation().equalsIgnoreCase(arrivo))
                    .findFirst()
                    .orElse(null);
                if (t != null) {
                    trainField.setText(t.getName());
                    selectedTrain = t;
                    found = true;
                }
            }
            if (!found) {
                trainField.setText("Nessun treno disponibile");
                selectedTrain = null;
            }
        } catch (Exception e) {
            trainField.setText("Nessun treno disponibile");
            selectedTrain = null;
        }
        buyButton.setDisable(selectedTrain == null);
        updateSeatsSpinnerMax();
        updatePrice();
    }

    // Listener per abilitare/disabilitare il pulsante quando si seleziona manualmente un treno suggerito
    private void setupTrainFieldListener() {
        trainField.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedTrain = trainNameToTrain.get(newVal);
            buyButton.setDisable(selectedTrain == null);
            updateSeatsSpinnerMax();
        });
    }

    private void loadPromoCodesFromServer() {
        try {
            PromotionList promoList = promotionService.listPromotions(Empty.getDefaultInstance());
            promoCodesFromServer.clear();
            for (Promotion p : promoList.getPromotionsList()) {
                promoCodesFromServer.add(p.getName());
            }
        } catch (Exception e) {
            // In caso di errore lascia la lista vuota

        }
    }
}
