package it.unical.trenical.client.gui.controller;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.gui.util.AlertUtils;
import it.unical.trenical.client.session.UserSession;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.ticket.ClearAllTicketsRequest;
import it.unical.trenical.grpc.ticket.ListTicketsRequest;
import it.unical.trenical.grpc.ticket.ListTicketsResponse;
import it.unical.trenical.grpc.ticket.TicketServiceGrpc;
import it.unical.trenical.grpc.train.TrainServiceGrpc;
import it.unical.trenical.grpc.train.ScheduleRequest;
import it.unical.trenical.grpc.train.ScheduleResponse;
import it.unical.trenical.grpc.train.ScheduleEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller per la schermata di visualizzazione biglietti acquistati.
 */
public class MyTicketsController {
    @FXML private TableView<TicketViewModel> ticketsTable;
    @FXML private TableColumn<TicketViewModel, String> colTrain;
    @FXML private TableColumn<TicketViewModel, String> colDeparture;
    @FXML private TableColumn<TicketViewModel, String> colDepartureTime;
    @FXML private TableColumn<TicketViewModel, String> colArrival;
    @FXML private TableColumn<TicketViewModel, String> colDate;
    @FXML private TableColumn<TicketViewModel, String> colTime;
    @FXML private TableColumn<TicketViewModel, String> colClass;
    @FXML private TableColumn<TicketViewModel, String> colSeat;
    @FXML private TableColumn<TicketViewModel, String> colStatus;
    @FXML private Button clearAllButton;

    private final ObservableList<TicketViewModel> tickets = FXCollections.observableArrayList();
    private TicketServiceGrpc.TicketServiceBlockingStub ticketService;
    private TrainServiceGrpc.TrainServiceBlockingStub trainService;
    private ManagedChannel channel;

    @FXML
    public void initialize() {
        String username = UserSession.getUsername();
        if (username == null || username.isEmpty()) {
            AlertUtils.showError("Errore", "Utente non loggato. Effettua il login.");
            SceneManager.getInstance().showLogin();
            // Nasconde la finestra corrente se presente
            if (ticketsTable != null && ticketsTable.getScene() != null && ticketsTable.getScene().getWindow() != null) {
                ticketsTable.getScene().getWindow().hide();
            }
            // Blocca la visualizzazione della schermata
            return;
        }
        colTrain.setCellValueFactory(data -> data.getValue().trainProperty());
        colDeparture.setCellValueFactory(data -> data.getValue().departureProperty());
        colDepartureTime.setCellValueFactory(data -> data.getValue().departureDateTimeProperty());
        colArrival.setCellValueFactory(data -> data.getValue().arrivalProperty());
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());
        colTime.setCellValueFactory(data -> data.getValue().timeProperty());
        colClass.setCellValueFactory(data -> data.getValue().serviceClassProperty());
        colSeat.setCellValueFactory(data -> data.getValue().seatProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        ticketsTable.setItems(tickets);

        // --- Doppio click per modifica biglietto ---
        ticketsTable.setRowFactory(tv -> {
            TableRow<TicketViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ticketsTable.getSelectionModel().select(row.getIndex());
                    onModify();
                }
            });
            return row;
        });

        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        ticketService = TicketServiceGrpc.newBlockingStub(channel);
        trainService = TrainServiceGrpc.newBlockingStub(channel);
        loadTicketsFromServer();
        if (clearAllButton != null) {
            clearAllButton.setVisible(UserSession.isAdmin());
        }
    }

    private void loadTicketsFromServer() {
        try {
            String username = UserSession.getUsername();
            if (username == null || username.isEmpty()) {
                AlertUtils.showError("Errore", "Utente non loggato. Effettua il login.");
                return;
            }
            ListTicketsRequest req = ListTicketsRequest.newBuilder()
                .setPassengerName(username)
                .build();
            ListTicketsResponse resp = ticketService.listTickets(req);
            tickets.clear();
            for (Ticket t : resp.getTicketsList()) {
                tickets.add(new TicketViewModel(t));
            }
            refreshTicketsTable();
        } catch (Exception e) {
            AlertUtils.showError("Errore", "Impossibile caricare i biglietti dal server.");
        }
    }

    /**
     * Aggiorna la tabella con i biglietti acquistati.
     */
    public void refreshTicketsTable() {
        ticketsTable.refresh();
    }

    /**
     * Aggiunge un biglietto alla lista in memoria e aggiorna la tabella.
     */
    public void addTicket(Ticket ticket) {
        tickets.add(new TicketViewModel(ticket));
        refreshTicketsTable();
    }

    /**
     * Gestisce la modifica di un biglietto selezionato.
     */
    @FXML
    private void onModify() {
        TicketViewModel selected = ticketsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Errore", "Seleziona un biglietto da modificare.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifica Biglietto");
        dialog.setHeaderText(null);
        dialog.initOwner(ticketsTable.getScene().getWindow());
        DatePicker datePicker = new DatePicker(java.time.LocalDate.parse(selected.dateProperty().get()));
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(java.time.LocalDate.now()));
            }
        });
        ComboBox<String> classBox = new ComboBox<>();
        classBox.getItems().addAll("Economy", "Prima Classe");
        classBox.setValue(selected.serviceClassProperty().get());
        ComboBox<String> timeBox = new ComboBox<>();
        // --- Carica solo orari disponibili e futuri ---
        Runnable updateTimeBox = () -> {
            timeBox.getItems().clear();
            List<String> availableTimes = getAvailableTimes(
                selected.departureProperty().get(),
                selected.arrivalProperty().get(),
                datePicker.getValue()
            );
            timeBox.getItems().addAll(availableTimes);
            // Se l'orario attuale è ancora valido, selezionalo, altrimenti seleziona il primo disponibile
            String currentTime = selected.departureTimeProperty().get();
            if (currentTime != null && availableTimes.contains(currentTime)) {
                timeBox.setValue(currentTime);
            } else if (!availableTimes.isEmpty()) {
                timeBox.setValue(availableTimes.get(0));
            }
        };
        datePicker.valueProperty().addListener((obs, o, n) -> updateTimeBox.run());
        updateTimeBox.run();
        Label priceLabel = new Label("Prezzo: -");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Data:"), datePicker);
        grid.addRow(1, new Label("Orario:"), timeBox);
        grid.addRow(2, new Label("Classe:"), classBox);
        grid.addRow(3, priceLabel);
        dialog.getDialogPane().setContent(grid);
        ButtonType conferma = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(conferma, ButtonType.CANCEL);
        Runnable updatePrice = () -> updateDialogPrice(selected, datePicker, timeBox, classBox, priceLabel);
        classBox.valueProperty().addListener((obs, o, n) -> updatePrice.run());
        datePicker.valueProperty().addListener((obs, o, n) -> updatePrice.run());
        timeBox.valueProperty().addListener((obs, o, n) -> updatePrice.run());
        updatePrice.run();
        dialog.setResultConverter(button -> button);
        dialog.showAndWait().ifPresent(result -> {
            if (result == conferma) {
                try {
                    var reqBuilder = it.unical.trenical.grpc.ticket.ModifyTicketRequest.newBuilder()
                            .setTicketId(selected.getTicketId())
                            .setNewServiceClass(classBox.getValue());
                    java.time.LocalDate date = datePicker.getValue();
                    String time = timeBox.getValue();
                    if (date != null) {
                        java.time.LocalTime localTime = java.time.LocalTime.parse(time);
                        java.time.LocalDateTime ldt = java.time.LocalDateTime.of(date, localTime);
                        reqBuilder.setNewDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(ldt.toEpochSecond(java.time.ZoneOffset.UTC)).build());
                        reqBuilder.setNewTravelTime(com.google.protobuf.Timestamp.newBuilder().setSeconds(localTime.toSecondOfDay()).build());
                    }
                    var resp = ticketService.modifyTicket(reqBuilder.build());
                    if (resp.getSuccess()) {
                        AlertUtils.showInfo("Successo", "Biglietto modificato con successo!");
                        loadTicketsFromServer();
                    } else {
                        AlertUtils.showError("Errore", resp.getMessage());
                    }
                } catch (Exception ex) {
                    AlertUtils.showError("Errore", "Impossibile modificare il biglietto: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * Restituisce la lista degli orari disponibili per la tratta e la data selezionata, solo futuri.
     */
    private List<String> getAvailableTimes(String departure, String arrival, java.time.LocalDate date) {
        try {
            // Chiamata gRPC al servizio treni per ottenere le partenze dalla stazione di partenza
            ScheduleRequest req = ScheduleRequest.newBuilder()
                    .setStation(departure)
                    .setDate(Timestamp.newBuilder().setSeconds(date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()).build())
                    .build();
            ScheduleResponse resp = trainService.getTrainSchedule(req);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            return resp.getDeparturesList().stream()
                    // Filtra solo le partenze verso la stazione di arrivo desiderata
                    .filter(entry -> entry.getDestination().equalsIgnoreCase(arrival))
                    // Solo orari futuri
                    .filter(entry -> {
                        Instant instant = Instant.ofEpochSecond(entry.getTime().getSeconds());
                        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                        return ldt.isAfter(now);
                    })
                    .map(entry -> {
                        Instant instant = Instant.ofEpochSecond(entry.getTime().getSeconds());
                        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                        return ldt.toLocalTime().toString();
                    })
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // In caso di errore, fallback a lista vuota
            return Collections.emptyList();
        }
    }

    /**
     * Aggiorna dinamicamente il prezzo nella dialog di modifica biglietto.
     */
    private void updateDialogPrice(TicketViewModel selected, DatePicker datePicker, ComboBox<String> timeBox, ComboBox<String> classBox, Label priceLabel) {
        try {
            java.time.LocalDate date = datePicker.getValue();
            String time = timeBox.getValue();
            java.time.LocalTime localTime = java.time.LocalTime.parse(time);
            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(date, localTime);
            var req = it.unical.trenical.grpc.ticket.PurchaseTicketRequest.newBuilder()
                    .setTrainId(1)
                    .setDepartureStation(selected.departureProperty().get())
                    .setArrivalStation(selected.arrivalProperty().get())
                    .setServiceClass(classBox.getValue())
                    .setSeats(1)
                    .setPassengerName(UserSession.getUsername())
                    .setTravelDate(Timestamp.newBuilder().setSeconds(ldt.toEpochSecond(java.time.ZoneOffset.UTC)).build())
                    .setTravelTime(Timestamp.newBuilder().setSeconds(localTime.toSecondOfDay()).build())
                    .build();
            var resp = ticketService.purchaseTicket(req);
            priceLabel.setText("Prezzo: " + String.format("%.2f", resp.getPrice()) + " €");
        } catch (Exception e) {
            priceLabel.setText("Prezzo: errore");
        }
    }

    /**
     * Gestisce l'annullamento di un biglietto selezionato.
     */
    @FXML
    private void onCancel() {
        TicketViewModel selected = ticketsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Errore", "Seleziona un biglietto da annullare.");
            return;
        }
        // Chiamata gRPC per annullare il biglietto dal server
        try {
            var req = it.unical.trenical.grpc.ticket.CancelTicketRequest.newBuilder()
                    .setTicketId(selected.getTicketId())
                    .build();
            var resp = ticketService.cancelTicket(req);
            if (resp.getSuccess()) {
                AlertUtils.showInfo("Successo", "Biglietto annullato con successo.");
                loadTicketsFromServer();
            } else {
                AlertUtils.showError("Errore", resp.getMessage());
            }
        } catch (Exception e) {
            AlertUtils.showError("Errore", "Impossibile annullare il biglietto: " + e.getMessage());
        }
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBack() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }

    /**
     * Gestisce la richiesta di svuotamento della lista dei biglietti.
     */
    @FXML
    private void onClearAll() {
        // Solo admin può svuotare la lista
        if (!UserSession.isAdmin()) {
            AlertUtils.showError("Permesso negato", "Solo un amministratore può svuotere tutti i biglietti.");
            return;
        }
        if (AlertUtils.showConfirm("Conferma", "Sei sicuro di voler eliminare tutti i biglietti?")) {
            try {
                ticketService.clearAllTickets(ClearAllTicketsRequest.newBuilder().build());
                loadTicketsFromServer();
                AlertUtils.showInfo("Successo", "Tutti i biglietti sono stati eliminati.");
            } catch (Exception e) {
                AlertUtils.showError("Errore", "Impossibile svuotare la lista: " + e.getMessage());
            }
        }
    }

    /**
     * ViewModel per la visualizzazione dei biglietti nella tabella.
     */
    public static class TicketViewModel {
        private final StringProperty train;
        private final StringProperty departure;
        private final StringProperty departureTime;
        private final StringProperty arrival;
        private final StringProperty date;
        private final StringProperty time;
        private final StringProperty serviceClass;
        private final StringProperty seat;
        private final StringProperty status;
        private final String ticketId;
        private final StringProperty departureDateTime;

        public TicketViewModel(Ticket ticket) {
            this.train = new SimpleStringProperty("Treno " + ticket.getTrainId());
            this.departure = new SimpleStringProperty(ticket.getDepartureStation());
            this.arrival = new SimpleStringProperty(ticket.getArrivalStation());
            String dateStr = "";
            String timeStr = "";
            String departureDateTimeStr = "";
            if (ticket.hasTravelDate()) {
                Instant instant = Instant.ofEpochSecond(ticket.getTravelDate().getSeconds());
                LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                dateStr = ldt.toLocalDate().toString();
                timeStr = ldt.toLocalTime().toString();
                departureDateTimeStr = ldt.toString();
            }
            this.date = new SimpleStringProperty(dateStr);
            this.time = new SimpleStringProperty(timeStr);
            this.serviceClass = new SimpleStringProperty(ticket.getServiceClass());
            this.seat = new SimpleStringProperty(ticket.getSeat());
            // Stato: "Annullato" se il biglietto non esiste più sul server, altrimenti "Attivo"
            String statusValue = "Attivo";
            if (ticket.getStatus() != null && !ticket.getStatus().isEmpty()) {
                statusValue = ticket.getStatus();
            }
            this.status = new SimpleStringProperty(statusValue);
            this.ticketId = ticket.getId();
            this.departureDateTime = new SimpleStringProperty(departureDateTimeStr);
            this.departureTime = new SimpleStringProperty(timeStr);
        }

        public StringProperty trainProperty() { return train; }
        public StringProperty departureProperty() { return departure; }
        public StringProperty departureTimeProperty() { return departureTime; }
        public StringProperty arrivalProperty() { return arrival; }
        public StringProperty dateProperty() { return date; }
        public StringProperty timeProperty() { return time; }
        public StringProperty serviceClassProperty() { return serviceClass; }
        public StringProperty seatProperty() { return seat; }
        public StringProperty statusProperty() { return status; }
        public String getTicketId() { return ticketId; }
        public StringProperty departureDateTimeProperty() { return departureDateTime; }
    }
}
