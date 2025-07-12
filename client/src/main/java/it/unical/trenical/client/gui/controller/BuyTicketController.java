package it.unical.trenical.client.gui.controller;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.gui.SelectedTrainService;
import it.unical.trenical.client.gui.util.AlertUtils;
import it.unical.trenical.client.gui.util.AutoCompleteUtil;
import it.unical.trenical.client.session.UserSession;
import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.promotion.Promotion;
import it.unical.trenical.grpc.promotion.PromotionList;
import it.unical.trenical.grpc.promotion.PromotionServiceGrpc;
import it.unical.trenical.grpc.ticket.*;
import it.unical.trenical.grpc.train.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller per la schermata di acquisto biglietto.
 * Gestisce la selezione di treni, validazione codici promozionali e processo di acquisto.
 */
public class BuyTicketController {

    // Campi UI
    @FXML private TextField trainField, departureStationField, arrivalStationField, promoCodeField;
    @FXML private ComboBox<String> classBox, timeBox;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private RadioButton creditCardRadio, digitalWalletRadio;
    @FXML private Label priceLabel, seatsAvailableLabel, promoValidationLabel;
    @FXML private Button validatePromoButton, buyButton;
    @FXML private DatePicker datePicker;

    // Servizi gRPC
    private TrainServiceGrpc.TrainServiceBlockingStub trainService;
    private TicketServiceGrpc.TicketServiceBlockingStub ticketService;
    private PromotionServiceGrpc.PromotionServiceBlockingStub promotionService;

    // Stato interno
    private final Map<String, Train> trainNameToTrain = new HashMap<>();
    private Train selectedTrain = null;
    private int seatsSpinnerMax = 10;
    private boolean promoValid = false;
    private double promoPrice = 0.0;

    @FXML
    public void initialize() {
        // Inizializza servizi gRPC
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        trainService = TrainServiceGrpc.newBlockingStub(channel);
        ticketService = TicketServiceGrpc.newBlockingStub(channel);
        promotionService = PromotionServiceGrpc.newBlockingStub(channel);

        // Configura UI
        classBox.getItems().addAll("Prima Classe", "Seconda Classe");
        classBox.setValue("Seconda Classe");
        seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Configura gruppo radio button per pagamento
        ToggleGroup paymentGroup = new ToggleGroup();
        creditCardRadio.setToggleGroup(paymentGroup);
        digitalWalletRadio.setToggleGroup(paymentGroup);
        creditCardRadio.setSelected(true);

        // Setup autocompletamento
        AutoCompleteUtil.setupAutoComplete(trainField, this::fetchTrainSuggestions);
        setupTrainFieldListener();
        AutoCompleteUtil.setupAutoComplete(departureStationField, this::fetchStationSuggestions);
        AutoCompleteUtil.setupAutoComplete(arrivalStationField, this::fetchStationSuggestions);

        // Listeners per aggiornamenti automatici
        classBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        seatsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        trainField.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedTrain = trainNameToTrain.get(newVal);
            buyButton.setDisable(selectedTrain == null);
            updateSeatsSpinnerMax();
            updatePrice();
        });

        departureStationField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateAvailableTimes();
            updateTrainByStations();
            updateSeatsSpinnerMax();
        });
        arrivalStationField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateAvailableTimes();
            updateTrainByStations();
            updateSeatsSpinnerMax();
        });

        if (datePicker != null) {
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateAvailableTimes();
                updateTrainByStations();
                updateSeatsSpinnerMax();
                updatePrice();
            });
        }
        if (timeBox != null) {
            timeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateTrainByStations();
                updateSeatsSpinnerMax();
                updatePrice();
            });
        }

        // Impedisci selezione di date passate
        if (datePicker != null) {
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(empty || item.isBefore(LocalDate.now()));
                }
            });
        }

        // Carica dati iniziali
        loadSelectedTrain();
        loadSelectedDateTime();
        setupTooltips();
        validatePromoButton.setOnAction(e -> onValidatePromo());
        updateSeatsSpinnerMax();
        updatePrice();
    }

    private void setupTooltips() {
        trainField.setTooltip(new Tooltip("Il treno è selezionato automaticamente in base a partenza e arrivo"));
        classBox.setTooltip(new Tooltip("Seleziona la classe del biglietto"));
        seatsSpinner.setTooltip(new Tooltip("Seleziona il numero di posti da acquistare"));
        promoCodeField.setTooltip(new Tooltip("Inserisci un codice promozionale se disponibile"));
        validatePromoButton.setTooltip(new Tooltip("Verifica la validità del codice promozionale"));
        creditCardRadio.setTooltip(new Tooltip("Paga con carta di credito"));
        digitalWalletRadio.setTooltip(new Tooltip("Paga con portafoglio digitale"));
        buyButton.setTooltip(new Tooltip("Procedi all'acquisto del biglietto"));
    }

    private void loadSelectedDateTime() {
        SelectedTrainService service = SelectedTrainService.getInstance();

        LocalDate searchDate = service.getSelectedDate();
        if (searchDate != null && datePicker != null) {
            datePicker.setValue(searchDate);
        } else if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }

        LocalTime searchTime = service.getSelectedTime();
        if (searchTime != null && timeBox != null) {
            updateAvailableTimes();
            String timeStr = searchTime.toString();
            if (!timeBox.getItems().contains(timeStr)) {
                timeBox.getItems().add(timeStr);
            }
            timeBox.setValue(timeStr);
        }
    }

    private void updateAvailableTimes() {
        timeBox.getItems().clear();
        String dep = departureStationField.getText();
        String arr = arrivalStationField.getText();
        LocalDate date = datePicker.getValue();
        if (dep == null || dep.isBlank() || arr == null || arr.isBlank() || date == null) return;
        try {
            // Recupera tutti i treni per la tratta e la data selezionata
            SearchTrainRequest req = SearchTrainRequest.newBuilder()
                    .setDepartureStation(dep)
                    .setArrivalStation(arr)
                    .setDate(Timestamp.newBuilder().setSeconds(date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()).build())
                    .build();
            TrainResponse resp = trainService.searchTrains(req);
            List<String> allTimes = new ArrayList<>();
            Map<String, Train> timeToTrain = new HashMap<>();
            for (Train train : resp.getTrainsList()) {
                if (!train.hasDepartureTime()) continue;
                Instant instant = Instant.ofEpochSecond(train.getDepartureTime().getSeconds());
                LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                if (date.equals(LocalDate.now())) {
                    // Se la data selezionata è oggi, mostra solo treni con orario >= ora attuale (solo ore e minuti)
                    LocalTime now = LocalTime.now();
                    if (ldt.toLocalTime().isBefore(now) && ldt.toLocalDate().isEqual(LocalDate.now())) continue;
                }
                LocalTime time = ldt.toLocalTime();
                String timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"));
                if (!allTimes.contains(timeStr)) {
                    allTimes.add(timeStr);
                    timeToTrain.put(timeStr, train);
                }
            }
            Collections.sort(allTimes);
            timeBox.getItems().addAll(allTimes);
            // Seleziona il primo orario valido
            if (!allTimes.isEmpty()) {
                timeBox.setValue(allTimes.get(0));
                // Aggiorna il treno selezionato in base all'orario
                selectedTrain = timeToTrain.get(allTimes.get(0));
                trainField.setText(selectedTrain != null ? selectedTrain.getName() : "");
            } else {
                selectedTrain = null;
                trainField.setText("");
            }
        } catch (Exception e) {
            // fallback: orari fissi
            for (int h = 6; h <= 22; h++) timeBox.getItems().add(String.format("%02d:00", h));
            timeBox.setValue("06:00");
            selectedTrain = null;
            trainField.setText("");
        }
    }

    /**
     * Seleziona il treno in base a stazioni, data e orario (se disponibile)
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
            // Match perfetto su stazione e orario
            Train found = resp.getTrainsList().stream()
                    .filter(tr -> tr.getDepartureStation().equalsIgnoreCase(partenza)
                            && tr.getArrivalStation().equalsIgnoreCase(arrivo)
                            && tr.hasDepartureTime()
                            && localTime.equals(Instant.ofEpochSecond(tr.getDepartureTime().getSeconds())
                            .atZone(ZoneId.systemDefault()).toLocalTime()))
                    .findFirst()
                    .orElse(null);
            if (found != null) {
                trainField.setText(found.getName());
                selectedTrain = found;
            } else {
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
        LocalDate date = datePicker != null ? datePicker.getValue() : null;
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

            // Se c'è un codice promo validato, mostra il prezzo promo
            if (promoValid && promoPrice > 0.0) {
                double total = promoPrice * seats;
                priceLabel.setText("Prezzo: " + String.format("%.2f", total) + " € (promo)");
                return;
            }

            // Ottieni il tipo utente dal client
            String userType = it.unical.trenical.client.session.UserManager.getCustomerType(username);
            if (userType == null || userType.isEmpty()) {
                userType = "standard";
            }

            LocalTime localTime = LocalTime.parse(time);
            LocalDateTime ldt = LocalDateTime.of(date, localTime);
            Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();

            // USA IL NUOVO SERVIZIO GetTicketPrice con il tipo utente
            GetTicketPriceRequest req = GetTicketPriceRequest.newBuilder()
                    .setDepartureStation(partenza)
                    .setArrivalStation(arrivo)
                    .setServiceClass(classBox.getValue())
                    .setTravelDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).build())
                    .setPromoCode("") // Non applicare promo se non validato
                    .setTrainType(selectedTrain != null ? selectedTrain.getName() : "")
                    .setUserType(userType) // PASSA IL TIPO UTENTE
                    .build();

            GetTicketPriceResponse resp = ticketService.getTicketPrice(req);
            double total = resp.getPrice() * seats;

            // Mostra il prezzo con indicazione del tipo utente se non è standard
            String priceText = "Prezzo: " + String.format("%.2f", total) + " €";
            if (!"standard".equalsIgnoreCase(userType)) {
                priceText += " (" + userType + ")";
            }
            priceLabel.setText(priceText);
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
            LocalDate date = datePicker != null ? datePicker.getValue() : null;
            String time = timeBox != null ? timeBox.getValue() : null;
            TrainDetailsRequest.Builder reqBuilder = TrainDetailsRequest.newBuilder().setTrainId(trainId);
            if (date != null && time != null && !time.isBlank()) {
                LocalTime localTime = LocalTime.parse(time);
                LocalDateTime ldt = LocalDateTime.of(date, localTime);
                // Conversione coerente con il server: ZoneId.systemDefault()
                Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
                reqBuilder.setDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).build());
            } else if (date != null) {
                LocalDateTime ldt = date.atStartOfDay();
                Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
                reqBuilder.setDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).build());
            }
            TrainDetailsResponse resp = trainService.getTrainDetails(reqBuilder.build());
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

    // --- Promo section ---

    private void loadPromoCodesFromServer() {
        try {
            PromotionList promoList = promotionService.listPromotions(Empty.getDefaultInstance());
            promoList.getPromotionsList();
        } catch (Exception e) {
            // In caso di errore lascia la lista vuota
        }
    }

    @FXML
    private void onValidatePromo() {
        String promoCode = promoCodeField.getText();
        if (promoCode == null || promoCode.isBlank()) {
            promoValidationLabel.setText("Inserisci un codice promozione");
            promoValidationLabel.setStyle("-fx-text-fill: red;");
            promoValid = false;
            promoPrice = 0.0;
            updatePrice();
            return;
        }

        String partenza = departureStationField.getText();
        String arrivo = arrivalStationField.getText();
        LocalDate date = datePicker.getValue();
        String time = timeBox.getValue();

        if (partenza.isBlank() || arrivo.isBlank() || date == null || time == null) {
            promoValidationLabel.setText("Compila tutti i campi per validare il codice");
            promoValidationLabel.setStyle("-fx-text-fill: red;");
            promoValid = false;
            promoPrice = 0.0;
            updatePrice();
            return;
        }

        try {
            LocalTime localTime = LocalTime.parse(time);
            LocalDateTime ldt = LocalDateTime.of(date, localTime);
            Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();

            String username = UserSession.getUsername();
            if (username == null || username.isEmpty()) {
                promoValidationLabel.setText("Errore: utente non loggato");
                promoValidationLabel.setStyle("-fx-text-fill: red;");
                promoValid = false;
                promoPrice = 0.0;
                updatePrice();
                return;
            }

            // Prima calcola il prezzo senza codice promo per confronto
            PurchaseTicketRequest reqWithoutPromo = PurchaseTicketRequest.newBuilder()
                    .setTrainId(selectedTrain.getId())
                    .setDepartureStation(partenza)
                    .setArrivalStation(arrivo)
                    .setServiceClass(classBox.getValue())
                    .setSeats(1)
                    .setPassengerName(username)
                    .setTravelDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).build())
                    .setPromoCode("") // Nessun codice promo
                    .setPaymentMethod("")
                    .setTrainType(selectedTrain != null ? selectedTrain.getName() : "")
                    .build();

            PurchaseTicketResponse respWithoutPromo = ticketService.purchaseTicket(reqWithoutPromo);
            double priceWithoutPromo = respWithoutPromo.getPrice();

            // Poi calcola il prezzo con il codice promo
            PurchaseTicketRequest reqWithPromo = PurchaseTicketRequest.newBuilder()
                    .setTrainId(selectedTrain.getId())
                    .setDepartureStation(partenza)
                    .setArrivalStation(arrivo)
                    .setServiceClass(classBox.getValue())
                    .setSeats(1)
                    .setPassengerName(username)
                    .setTravelDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).build())
                    .setPromoCode(promoCode)
                    .setPaymentMethod("")
                    .setTrainType(selectedTrain != null ? selectedTrain.getName() : "")
                    .build();

            PurchaseTicketResponse respWithPromo = ticketService.purchaseTicket(reqWithPromo);

            if (!respWithPromo.getSuccess()) {
                // Se il server restituisce errore, il codice non è valido
                promoValidationLabel.setText(respWithPromo.getMessage());
                promoValidationLabel.setStyle("-fx-text-fill: red;");
                promoValid = false;
                promoPrice = 0.0;
            } else {
                // Controlla se il prezzo è effettivamente cambiato
                double priceWithPromo = respWithPromo.getPrice();

                if (Math.abs(priceWithPromo - priceWithoutPromo) > 0.01) {
                    // Il prezzo è cambiato, il codice promo è stato applicato
                    double discountPerTicket = priceWithoutPromo - priceWithPromo;
                    int totalSeats = seatsSpinner.getValue();
                    double totalDiscount = discountPerTicket * totalSeats;

                    // Trova la percentuale effettiva della promozione dal server
                    double actualDiscountPercent = getActualPromotionPercent(promoCode);

                    // Mostra sconto totale e percentuale corretta
                    if (actualDiscountPercent > 0) {
                        promoValidationLabel.setText(String.format("Codice valido! Sconto totale: %.2f€ (%.0f%%) per %d bigliett%s",
                                totalDiscount, actualDiscountPercent, totalSeats, totalSeats > 1 ? "i" : "o"));
                    } else {
                        promoValidationLabel.setText(String.format("Codice valido! Sconto totale: %.2f€ per %d bigliett%s",
                                totalDiscount, totalSeats, totalSeats > 1 ? "i" : "o"));
                    }
                    promoValidationLabel.setStyle("-fx-text-fill: green;");
                    promoValid = true;
                    promoPrice = priceWithPromo;
                } else {
                    // Il prezzo non è cambiato, il codice non è stato applicato
                    promoValidationLabel.setText("Codice non valido o non applicabile");
                    promoValidationLabel.setStyle("-fx-text-fill: red;");
                    promoValid = false;
                    promoPrice = 0.0;
                }
            }
            updatePrice();
        } catch (Exception e) {
            promoValidationLabel.setText("Errore nella validazione del codice");
            promoValidationLabel.setStyle("-fx-text-fill: red;");
            promoValid = false;
            promoPrice = 0.0;
            updatePrice();
        }
    }

    /**
     * Ottiene la percentuale di sconto effettiva della promozione dal server.
     */
    private double getActualPromotionPercent(String promoCode) {
        try {
            PromotionList promoList = promotionService.listPromotions(Empty.getDefaultInstance());
            for (Promotion promo : promoList.getPromotionsList()) {
                if (promo.getName().equalsIgnoreCase(promoCode)) {
                    return promo.getDiscountPercent();
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel recupero percentuale promozione: " + e.getMessage());
        }
        return 0.0;
    }

    // --- Suggerimenti e listeners ---

    private List<String> fetchTrainSuggestions(String query) {
        trainNameToTrain.clear();
        try {
            SearchTrainRequest nameReq = SearchTrainRequest.newBuilder()
                    .setDepartureStation("")
                    .setArrivalStation("")
                    .setDate(Timestamp.getDefaultInstance())
                    .build();
            TrainResponse nameResp = trainService.searchTrains(nameReq);
            List<Train> trainsByName = nameResp.getTrainsList().stream()
                    .filter(t -> t.getName().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            Map<Integer, Train> idToTrain = new LinkedHashMap<>();
            for (Train t : trainsByName) idToTrain.put(t.getId(), t);
            for (Train t : idToTrain.values()) trainNameToTrain.put(t.getName(), t);
            return idToTrain.values().stream().map(Train::getName).toList();
        } catch (Exception e) {
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

    private List<String> fetchStationSuggestions(String query) {
        try {
            SearchStationRequest request = SearchStationRequest.newBuilder().setQuery(query).setLimit(10).build();
            SearchStationResponse response = trainService.searchStations(request);
            return response.getStationsList().stream().map(it.unical.trenical.grpc.common.Station::getName).toList(); // lambda -> method reference
        } catch (Exception e) {
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

    private void loadSelectedTrain() {
        Train selected = SelectedTrainService.getInstance().getSelectedTrain();
        if (selected != null) {
            departureStationField.setText(selected.getDepartureStation());
            arrivalStationField.setText(selected.getArrivalStation());
            trainField.setText(selected.getName());
            trainField.requestLayout();
            trainNameToTrain.put(selected.getName(), selected);
            selectedTrain = selected;
            updateSeatsSpinnerMax();
            updatePrice();
        }
    }

    private void setupTrainFieldListener() {
        trainField.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedTrain = trainNameToTrain.get(newVal);
            buyButton.setDisable(selectedTrain == null);
            updateSeatsSpinnerMax();
        });
    }

    @FXML
    private void onBuy() {
        buyButton.setDisable(true);
        String partenza = departureStationField.getText();
        String arrivo = arrivalStationField.getText();
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
        LocalDate date = datePicker != null ? datePicker.getValue() : null;
        String time = timeBox != null ? timeBox.getValue() : null;
        if (date == null || time == null || time.isBlank()) {
            AlertUtils.showError("Errore", "Seleziona data e orario di partenza.");
            buyButton.setDisable(false);
            return;
        }
        // BLOCCO ACQUISTO SE L'ORARIO È NEL PASSATO
        try {
            LocalTime localTime = LocalTime.parse(time);
            if (date.equals(LocalDate.now()) && localTime.isBefore(LocalTime.now().minusMinutes(1))) {
                AlertUtils.showError("Errore", "Non puoi acquistare un biglietto per un treno già partito.");
                buyButton.setDisable(false);
                return;
            }
            LocalDateTime ldt = LocalDateTime.of(date, localTime);
            // Corretto: uso il fuso orario locale per il timestamp
            long epochSecond = ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
            PurchaseTicketRequest.Builder builder = PurchaseTicketRequest.newBuilder()
                    .setTrainId(selectedTrain.getId())
                    .setPassengerName(username)
                    .setDepartureStation(departureStationField.getText())
                    .setArrivalStation(arrivalStationField.getText())
                    .setTravelDate(Timestamp.newBuilder().setSeconds(epochSecond).build())
                    .setServiceClass(classBox.getValue())
                    .setPaymentMethod(paymentMethod)
                    .setSeats(seatsRequested);

            // Applica promo solo se valida
            if (promoValid && promoCodeField.getText() != null && !promoCodeField.getText().isBlank()) {
                builder.setPromoCode(promoCodeField.getText());
            }

            PurchaseTicketResponse response = ticketService.purchaseTicket(builder.build());
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

    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }
}
