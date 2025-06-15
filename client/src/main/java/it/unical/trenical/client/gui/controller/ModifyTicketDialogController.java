package it.unical.trenical.client.gui.controller;

import com.google.protobuf.Timestamp;
import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.train.*;
import it.unical.trenical.grpc.ticket.ModifyTicketRequest;
import it.unical.trenical.grpc.ticket.OperationResponse;
import it.unical.trenical.grpc.ticket.TicketServiceGrpc;
import it.unical.trenical.grpc.ticket.GetTicketPriceRequest;
import it.unical.trenical.grpc.ticket.GetTicketPriceResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Controller per la finestra di modifica biglietto.
 */
public class ModifyTicketDialogController {
    @FXML private TextField departureField;
    @FXML private TextField arrivalField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeBox;
    @FXML private ComboBox<String> classBox;
    @FXML private Label priceLabel;
    @FXML private Button confirmButton;
    @FXML private Label seatsAvailableLabel;

    private ManagedChannel channel;
    private TrainServiceGrpc.TrainServiceBlockingStub trainService;
    private TicketServiceGrpc.TicketServiceBlockingStub ticketService;
    private String ticketId;
    private Runnable onSuccess;
    private Integer selectedTrainId = null;
    private Integer originalTrainId = null;

    private final Map<String, Integer> timeToTrainId = new HashMap<>();
    private final Map<String, it.unical.trenical.grpc.common.Train> timeToTrain = new HashMap<>();
    private it.unical.trenical.grpc.common.Train selectedTrain = null;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private double oldPrice = 0.0;
    private double newPrice = 0.0;
    private double surcharge = 0.0;

    @FXML
    public void initialize() {
        // Inizializzazione del canale gRPC e dei servizi
        channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        trainService = TrainServiceGrpc.newBlockingStub(channel);
        ticketService = TicketServiceGrpc.newBlockingStub(channel);

        // Configurazione componenti UI
        setupUI();

        // Gestione listener eventi
        setupListeners();

        // Aggiorna i posti disponibili ogni volta che la finestra riceve focus
        if (priceLabel != null && priceLabel.getScene() != null && priceLabel.getScene().getWindow() != null) {
            priceLabel.getScene().getWindow().focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) updateSeatsAvailable();
            });
        }
    }

    private void setupUI() {
        classBox.getItems().addAll("Prima Classe", "Seconda Classe");
        classBox.setValue("Seconda Classe");
        datePicker.setValue(LocalDate.now());

        // Limita il DatePicker alle date future
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });

        // I campi di partenza e arrivo non sono modificabili
        departureField.setEditable(false);
        arrivalField.setEditable(false);
        departureField.setFocusTraversable(false);
        arrivalField.setFocusTraversable(false);

        // Inizialmente il pulsante di conferma è disabilitato
        confirmButton.setDisable(true);
    }

    private void setupListeners() {
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateAvailableTimes();
            updateSeatsAvailable();
        });
        classBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        timeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateSelectedTrain();
            updateSeatsAvailable();
            updatePrice();
        });
    }

    private void updateAvailableTimes() {
        timeBox.getItems().clear();
        timeToTrain.clear();
        seatsAvailableLabel.setText("Disponibili: -");
        seatsAvailableLabel.setVisible(true);
        String dep = departureField.getText();
        String arr = arrivalField.getText();
        LocalDate date = datePicker.getValue();
        if (dep == null || dep.isBlank() || arr == null || arr.isBlank() || date == null) return;
        try {
            SearchTrainRequest req = SearchTrainRequest.newBuilder()
                    .setDepartureStation(dep)
                    .setArrivalStation(arr)
                    .setDate(Timestamp.newBuilder().setSeconds(date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()).build())
                    .build();
            TrainResponse resp = trainService.searchTrains(req);
            List<String> allTimes = new ArrayList<>();
            Map<String, it.unical.trenical.grpc.common.Train> localTimeToTrain = new HashMap<>();
            LocalTime now = LocalTime.now();
            boolean isToday = date.equals(LocalDate.now());
            for (Train train : resp.getTrainsList()) {
                if (!train.hasDepartureTime()) continue;
                Instant instant = Instant.ofEpochSecond(train.getDepartureTime().getSeconds());
                LocalDateTime ldt = LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
                LocalTime time = ldt.toLocalTime();
                if (isToday && time.isBefore(now)) continue;
                String timeStr = time.format(TIME_FORMATTER);
                if (!allTimes.contains(timeStr)) {
                    allTimes.add(timeStr);
                    localTimeToTrain.put(timeStr, train);
                }
            }
            Collections.sort(allTimes);
            timeBox.getItems().addAll(allTimes);
            timeToTrain.clear();
            timeToTrain.putAll(localTimeToTrain);
            if (!allTimes.isEmpty()) {
                timeBox.setValue(allTimes.get(0));
            }
            updateSelectedTrain();
            updateSeatsAvailable();
        } catch (Exception e) {
            for (int h = 6; h <= 22; h++) timeBox.getItems().add(String.format("%02d:00", h));
            timeBox.setValue("06:00");
            seatsAvailableLabel.setText("Disponibili: -");
            seatsAvailableLabel.setVisible(true);
            selectedTrain = null;
            confirmButton.setDisable(true);
        }
    }

    private void updateSelectedTrain() {
        String time = getTime();
        if (time != null && !time.isEmpty() && timeToTrain.containsKey(time)) {
            selectedTrain = timeToTrain.get(time);
            confirmButton.setDisable(false);
        } else {
            selectedTrain = null;
            confirmButton.setDisable(true);
        }
    }

    private void updateSeatsAvailable() {
        if (selectedTrain == null) {
            seatsAvailableLabel.setText("Disponibili: -");
            seatsAvailableLabel.setVisible(true);
            confirmButton.setDisable(true);
            return;
        }
        try {
            int trainId = selectedTrain.getId();
            // Usa l'orario reale del treno selezionato per la richiesta!
            LocalDateTime travelDateTime;
            if (selectedTrain.hasDepartureTime()) {
                Instant instant = Instant.ofEpochSecond(selectedTrain.getDepartureTime().getSeconds());
                travelDateTime = LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
            } else {
                // fallback: usa la data/orario selezionati dall'utente
                LocalDate date = getDate();
                String time = getTime();
                if (date == null || time == null || time.isBlank()) {
                    seatsAvailableLabel.setText("Disponibili: -");
                    seatsAvailableLabel.setVisible(true);
                    confirmButton.setDisable(true);
                    return;
                }
                LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
                travelDateTime = LocalDateTime.of(date, localTime);
            }
            Instant instant = travelDateTime.atZone(DEFAULT_ZONE).toInstant();
            TrainDetailsRequest req = TrainDetailsRequest.newBuilder()
                    .setTrainId(trainId)
                    .setDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).build())
                    .build();
            TrainDetailsResponse resp = trainService.getTrainDetails(req);
            int available = resp.getSeatsAvailable();
            if (available < 1) available = 1;
            seatsAvailableLabel.setText("Disponibili: " + available);
            seatsAvailableLabel.setVisible(true);
            confirmButton.setDisable(available < 1);
        } catch (Exception e) {
            seatsAvailableLabel.setText("Disponibili: -");
            seatsAvailableLabel.setVisible(true);
            confirmButton.setDisable(true);
        }
    }

    private void updateTimeComboBox(List<String> times) {
        timeBox.getItems().addAll(times);
        if (!times.isEmpty()) {
            timeBox.setValue(times.get(0));
            confirmButton.setDisable(false);
        }
        updateSelectedTrainId();
    }

    private void updateSelectedTrainId() {
        String time = getTime();
        if (time != null && !time.isEmpty() && timeToTrainId.containsKey(time)) {
            selectedTrainId = timeToTrainId.get(time);
            confirmButton.setDisable(false);
        } else {
            selectedTrainId = originalTrainId;
            confirmButton.setDisable(selectedTrainId == null);
        }
    }

    private void updatePrice() {
        String dep = getDepartureStation();
        String arr = getArrivalStation();
        LocalDate date = getDate();
        String time = getTime();
        String serviceClass = getServiceClass();
        if (dep == null || dep.isEmpty() || arr == null || arr.isEmpty() || date == null ||
                time == null || time.isEmpty() || serviceClass == null || serviceClass.isEmpty()) {
            priceLabel.setText("Prezzo: -");
            return;
        }
        updateSelectedTrain();
        if (selectedTrain == null) {
            priceLabel.setText("Prezzo: -");
            return;
        }
        try {
            LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
            LocalDateTime ldt = LocalDateTime.of(date, localTime);
            ZonedDateTime zdt = ldt.atZone(DEFAULT_ZONE);
            Timestamp travelTimestamp = Timestamp.newBuilder()
                    .setSeconds(zdt.toEpochSecond())
                    .setNanos(0)
                    .build();
            // Calcola nuovo prezzo
            GetTicketPriceRequest req = GetTicketPriceRequest.newBuilder()
                    .setDepartureStation(dep)
                    .setArrivalStation(arr)
                    .setTravelDate(travelTimestamp)
                    .setServiceClass(serviceClass)
                    .setPromoCode("")
                    .build();
            GetTicketPriceResponse resp = ticketService.getTicketPrice(req);
            newPrice = resp.getPrice();
            // Calcola sovrapprezzo
            surcharge = Math.max(0, newPrice - oldPrice);
            priceLabel.setText("Sovrapprezzo: " + String.format("%.2f", surcharge) + " €");
        } catch (Exception e) {
            priceLabel.setText("Prezzo: errore");
        }
    }

    // Getters per i valori UI
    public String getDepartureStation() { return departureField.getText(); }
    public String getArrivalStation() { return arrivalField.getText(); }
    public LocalDate getDate() { return datePicker.getValue(); }
    public String getTime() { return timeBox.getValue(); }
    public String getServiceClass() { return classBox.getValue(); }

    /**
     * Imposta i campi iniziali del form
     */
    public void setFields(String dep, String arr, LocalDate date, String time, String serviceClass, Integer trainId) {
        // Imposta i campi base
        departureField.setText(dep != null ? dep : "");
        arrivalField.setText(arr != null ? arr : "");
        if (date != null && !date.isBefore(LocalDate.now())) {
            datePicker.setValue(date);
        } else {
            datePicker.setValue(LocalDate.now());
        }
        if (serviceClass != null && !serviceClass.isEmpty()) {
            classBox.setValue(serviceClass);
        } else {
            classBox.setValue("Seconda Classe");
        }
        originalTrainId = (trainId != null && trainId > 0) ? trainId : null;
        // Aggiorna gli orari disponibili
        updateAvailableTimes();
        // Tenta di ripristinare l'orario originale
        if (time != null && !time.trim().isEmpty() && !timeBox.getItems().isEmpty()) {
            String formattedTime = formatTime(time);
            if (formattedTime != null) {
                for (String t : timeBox.getItems()) {
                    if (t.equals(formattedTime)) {
                        timeBox.setValue(t);
                        break;
                    }
                }
            }
        }
        updateSelectedTrain();
        updateSeatsAvailable();
        // Recupera il prezzo originale del biglietto dal server
        fetchOriginalTicketPrice();
    }

    private void fetchOriginalTicketPrice() {
        if (ticketId == null || ticketId.isEmpty()) return;
        try {
            it.unical.trenical.grpc.ticket.GetTicketRequest req = it.unical.trenical.grpc.ticket.GetTicketRequest.newBuilder()
                .setTicketId(ticketId)
                .build();
            it.unical.trenical.grpc.ticket.GetTicketResponse resp = ticketService.getTicket(req);
            if (resp.hasTicket()) {
                oldPrice = resp.getTicket().getPrice();
                updatePrice();
            }
        } catch (Exception e) {
            // In caso di errore lascia oldPrice a 0
        }
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void onConfirmClicked() {
        if (ticketId == null || ticketId.isEmpty()) {
            showError("ID biglietto mancante");
            return;
        }

        String dep = getDepartureStation();
        String arr = getArrivalStation();
        LocalDate date = getDate();
        String time = getTime();
        String serviceClass = getServiceClass();

        if (dep == null || dep.isEmpty() || arr == null || arr.isEmpty() || date == null ||
                time == null || time.isEmpty() || serviceClass == null || serviceClass.isEmpty()) {
            showError("Compila tutti i campi obbligatori.");
            return;
        }

        try {
            // Parsing dell'orario
            LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);

            // Creazione della data completa
            LocalDateTime ldt = LocalDateTime.of(date, localTime);
            ZonedDateTime zdt = ldt.atZone(DEFAULT_ZONE);

            // Verifica trainId
            if (selectedTrainId == null || selectedTrainId <= 0) {
                updateSelectedTrainId();
                if (selectedTrainId == null || selectedTrainId <= 0) {
                    showError("Nessun treno selezionato. Scegli un orario valido.");
                    return;
                }
            }

            // Creazione timestamp
            Timestamp travelDate = Timestamp.newBuilder()
                    .setSeconds(zdt.toEpochSecond())
                    .setNanos(0)
                    .build();

            Timestamp travelTime = Timestamp.newBuilder()
                    .setSeconds(localTime.toSecondOfDay())
                    .setNanos(0)
                    .build();

            Timestamp modificationDate = Timestamp.newBuilder()
                    .setSeconds(Instant.now().getEpochSecond())
                    .setNanos(0)
                    .build();

            // Creazione richiesta
            ModifyTicketRequest req = ModifyTicketRequest.newBuilder()
                    .setTicketId(ticketId)
                    .setNewTravelDate(travelDate)
                    .setNewTravelTime(travelTime)
                    .setModificationDate(modificationDate)
                    .setNewServiceClass(serviceClass)
                    .setNewDepartureStation(dep)
                    .setNewArrivalStation(arr)
                    .setTrainId(selectedTrainId)
                    .build();

            // Invio richiesta
            OperationResponse resp = ticketService.modifyTicket(req);

            // Gestione risposta
            if (resp.getSuccess()) {
                showConfirmation("Modifica effettuata con successo! Sovrapprezzo da pagare: " + String.format("%.2f", surcharge) + " €");
                if (onSuccess != null) onSuccess.run();
                closeDialog();
            } else {
                showError(resp.getMessage());
            }

        } catch (Exception e) {
            showError("Errore durante la modifica: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelClicked() {
        closeDialog();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void showConfirmation(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void closeDialog() {
        try {
            // Chiudi il canale gRPC
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown();
                if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            }

            // Chiudi la finestra
            if (priceLabel != null && priceLabel.getScene() != null && priceLabel.getScene().getWindow() != null) {
                priceLabel.getScene().getWindow().hide();
            }
        } catch (Exception e) {
            // Ignora errori di chiusura
        }
    }

    /**
     * Formatta un orario in formato HH:mm
     */
    private String formatTime(String time) {
        try {
            if (time.matches("\\d{2}:\\d{2}")) {
                return time;
            } else if (time.matches("\\d{1,2}:\\d{2}")) {
                // Gestisce anche orari tipo 9:05
                LocalTime parsedTime = LocalTime.parse(time);
                return parsedTime.format(TIME_FORMATTER);
            } else {
                LocalTime parsedTime = LocalTime.parse(time);
                return parsedTime.format(TIME_FORMATTER);
            }
        } catch (Exception e) {
            return null;
        }
    }
}

