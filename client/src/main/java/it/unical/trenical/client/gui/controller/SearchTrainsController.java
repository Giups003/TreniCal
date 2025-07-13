package it.unical.trenical.client.gui.controller;

import com.google.protobuf.Timestamp;
import it.unical.trenical.client.TrainClient;
import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.gui.SelectedTrainService;
import it.unical.trenical.client.model.TrainSearchResult;
import it.unical.trenical.grpc.common.Train;
import it.unical.trenical.grpc.train.*;
import it.unical.trenical.client.gui.util.AlertUtils;
import it.unical.trenical.client.gui.util.AutoCompleteUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.scene.control.DateCell;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller per la schermata di ricerca treni.
 * Gestisce la ricerca dei treni disponibili e la selezione per l'acquisto.
 */
public class SearchTrainsController {
    // --- Campi UI ---
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
    @FXML private TableColumn<TrainSearchResult, String> colDate;
    @FXML private TableColumn<TrainSearchResult, Integer> colTrainId;

    // --- Stato interno ---
    private final ObservableList<TrainSearchResult> searchResults = FXCollections.observableArrayList();
    private TrainClient trainClient;

    /**
     * Inizializza il controller configurando tabella, autocompletamento e validazioni.
     */
    @FXML
    public void initialize() {
        // Inizializza client
        trainClient = new TrainClient("localhost", 9090);

        // Configura tabella risultati
        setupTable();

        // Configura autocompletamento
        setupAutoComplete();

        // Configura valori di default
        setupDefaults();
    }

    private void setupTable() {
        // Imposta il datePicker con la data odierna e limita la selezione a oggi o date future
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Configura le colonne della tabella
        colTrain.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTrainName()));
        colDeparture.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartureStation()));
        colArrival.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArrivalStation()));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        colSeats.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAvailableSeats()).asObject());
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        colTrainId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTrainId()).asObject());

        // Configura il doppio click sulla riga per selezionare un treno
        resultsTable.setRowFactory(tv -> {
            TableRow<TrainSearchResult> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    TrainSearchResult result = row.getItem();
                    handleTrainSelection(result);
                }
            });
            return row;
        });
        // Configura la gestione della selezione nella tabella
        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                handleTrainSelection(newSelection);
            }
        });

        // Assegna i risultati alla tabella
        resultsTable.setItems(searchResults);
    }

    private void setupAutoComplete() {
        // Uso la utility centralizzata per l'autocompletamento delle stazioni
        AutoCompleteUtil.setupAutoComplete(departureField, this::fetchStationSuggestions);
        AutoCompleteUtil.setupAutoComplete(arrivalField, this::fetchStationSuggestions);
    }

    private void setupDefaults() {
        // Inizializza i combobox con i valori predefiniti
        trainTypeBox.setItems(FXCollections.observableArrayList("Tutti", "Regionale", "Intercity", "Frecciarossa"));
        trainTypeBox.getSelectionModel().selectFirst();

        classBox.setItems(FXCollections.observableArrayList("Tutte", "Economy", "Business", "Executive"));
        classBox.getSelectionModel().selectFirst();
    }

    private void handleTrainSelection(TrainSearchResult result) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma selezione treno");
        alert.setHeaderText("Vuoi davvero selezionare questo treno?");
        alert.setContentText("Treno: " + result.getTrainName() + "\nPartenza: " + result.getDepartureStation() + "\nArrivo: " + result.getArrivalStation() + "\nOrario: " + result.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        ButtonType conferma = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType annulla = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(conferma, annulla);
        alert.showAndWait().ifPresent(response -> {
            if (response == conferma) {
                try {
                    // Usa la data e l'orario del risultato selezionato
                    LocalDateTime dateTime = LocalDateTime.of(result.getDate(), result.getTime());
                    TrainDetailsResponse responseDetails = trainClient.getTrainDetails(result.getTrainId(), dateTime);

                    // Gestisci la risposta
                    if (responseDetails != null) {
                        // Estrai le informazioni dal treno
                        Train trainDetails = responseDetails.getTrain();
                        boolean isAvailable = responseDetails.getAvailable();
                        int seatsAvailable = responseDetails.getSeatsAvailable();

                        // Se il treno è disponibile, si procede con la visualizzazione dei dettagli
                        if (isAvailable && seatsAvailable > 0) {
                            // Salva i dettagli del treno selezionato in un servizio condiviso
                            SelectedTrainService.getInstance().setSelectedTrain(trainDetails);
                            // Salva anche la data e l'orario selezionati
                            SelectedTrainService.getInstance().setSelectedDate(result.getDate());
                            SelectedTrainService.getInstance().setSelectedTime(result.getTime());
                            // Passa alla schermata di acquisto biglietto
                            SceneManager.getInstance().showTicketPurchaseView();
                        } else {
                            // Mostra un messaggio se il treno non è disponibile
                            AlertUtils.showError("Treno non disponibile",
                                    "Ci dispiace, questo treno non è più disponibile per la prenotazione o non ha posti liberi.");
                        }
                    } else {
                        AlertUtils.showError("Errore", "Impossibile recuperare i dettagli del treno: risposta nulla");
                    }
                } catch (Exception e) {
                    AlertUtils.showError("Errore", "Impossibile recuperare i dettagli del treno: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Gestisce la ricerca dei treni.
     */
    @FXML
    private void onSearch() {

        String partenza = departureField.getText();
        String arrivo = arrivalField.getText();
        LocalDate data = datePicker.getValue();
        String tipologia = trainTypeBox.getValue(); // Legge la tipologia selezionata

        // Validazione input base
        if (partenza == null || partenza.isBlank() || arrivo == null || arrivo.isBlank() || data == null) {
            AlertUtils.showError("Errore", "Compila tutti i campi obbligatori (partenza, arrivo, data).");
            return;
        }
        try {
            // Converte la data di partenza in Timestamp per gRPC
            LocalDateTime dateTime = data.atStartOfDay();
            Instant instant = dateTime.toInstant(ZoneOffset.UTC);
            Timestamp dateTimestamp = Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();

            // Imposta timeFrom e timeTo come null per cercare tutti i treni del giorno
            Timestamp timeFromTimestamp = null;
            Timestamp timeToTimestamp = null;

            List<Train> trains = trainClient.searchTrains(
                    partenza,
                    arrivo,
                    dateTimestamp,
                    timeFromTimestamp,
                    timeToTimestamp,
                    tipologia
            );

            // Converte i risultati e aggiorna la tabella
            searchResults.clear();
            TrainSearchResult firstResult = null;
            for (Train train : trains) {
                TrainSearchResult result = convertToTrainSearchResult(train);
                // Filtro: mostra solo treni futuri se la data è oggi
                if (!data.isEqual(LocalDate.now()) || (result.getTime() != null && result.getTime().isAfter(LocalTime.now()))) {
                    searchResults.add(result);
                    if (firstResult == null) firstResult = result;
                }
            }
            resultsTable.setItems(searchResults);
            resultsTable.refresh();
            // NON selezionare nessun treno automaticamente dopo la ricerca
            resultsTable.getSelectionModel().clearSelection();
            // La selezione avviene solo manualmente tramite click sulla tabella
            if (searchResults.isEmpty()) {
                AlertUtils.showError("Nessun risultato",
                        "Non ci sono treni disponibili per questa tratta nella data selezionata.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Errore", "Si è verificato un errore durante la ricerca: " + e.getMessage());
        }

    }

    /**
     * Converte un oggetto Train gRPC in un oggetto TrainSearchResult per la visualizzazione.
     */
    private TrainSearchResult convertToTrainSearchResult(Train train) {
        int trainId = train.getId();
        String trainName = train.getName();
        String departureStation = train.getDepartureStation();
        String arrivalStation = train.getArrivalStation();
        LocalDate date;
        if (train.hasDepartureTime()) {
            date = Instant.ofEpochSecond(train.getDepartureTime().getSeconds())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            date = datePicker.getValue();
        }
        LocalTime departureTime = convertTimestampToLocalTime(train.getDepartureTime());
        int availableSeats = 0;
        try {
            // Richiama sempre il server con la data e l'orario esatti del treno
            if (date != null && departureTime != null) {
                LocalDateTime dateTime = LocalDateTime.of(date, departureTime);
                TrainDetailsResponse details = trainClient.getTrainDetails(trainId, dateTime);
                if (details != null) {
                    availableSeats = details.getSeatsAvailable();
                } else {
                    availableSeats = 0;
                }
            }
        } catch (Exception e) {
            // In caso di errore, mostra 0 posti disponibili
            availableSeats = 0;
        }
        return new TrainSearchResult(
                trainId,
                trainName,
                departureStation,
                arrivalStation,
                date,
                departureTime,
                availableSeats);
    }

    /**
     * Recupera i suggerimenti per le stazioni.
     *
     * @param query La query di ricerca
     * @return Lista di stazioni che corrispondono alla query
     */
    private List<String> fetchStationSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of(); // Lista vuota se la query è vuota
        }

        try {
            SearchStationRequest request = SearchStationRequest.newBuilder()
                    .setQuery(query)
                    .setLimit(10)
                    .build();
            SearchStationResponse response = trainClient.searchStations(request);
            return response.getStationsList().stream()
                    .map(station -> station.getName())
                    .collect(Collectors.toList());
        } catch (Exception e) {

            // Fallback per test/demo
            List<String> mockStations = List.of(
                    "Roma Termini", "Roma Tiburtina", "Roma Ostiense",
                    "Milano Centrale", "Milano Porta Garibaldi",
                    "Napoli Centrale", "Napoli Afragola",
                    "Firenze SMN", "Firenze Campo di Marte",
                    "Bologna Centrale",
                    "Torino Porta Nuova", "Torino Porta Susa",
                    "Venezia Santa Lucia", "Venezia Mestre",
                    "Genova Piazza Principe", "Genova Brignole",
                    "Bari Centrale"
            );
            String queryLower = query.toLowerCase();
            return mockStations.stream()
                    .filter(station -> station.toLowerCase().contains(queryLower))
                    .limit(10)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Converte un Timestamp gRPC in LocalTime.
     */
    private LocalTime convertTimestampToLocalTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBackToDashboard() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }
}
