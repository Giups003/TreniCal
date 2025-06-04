package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.client.gui.util.AlertUtils;
import it.unical.trenical.client.session.UserSession;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.grpc.ticket.TicketServiceGrpc;
import it.unical.trenical.grpc.ticket.ListTicketsRequest;
import it.unical.trenical.grpc.ticket.ListTicketsResponse;

/**
 * Controller per la schermata di visualizzazione biglietti acquistati.
 */
public class MyTicketsController {
    @FXML private TableView<TicketViewModel> ticketsTable;
    @FXML private TableColumn<TicketViewModel, String> colTrain;
    @FXML private TableColumn<TicketViewModel, String> colDate;
    @FXML private TableColumn<TicketViewModel, String> colTime;
    @FXML private TableColumn<TicketViewModel, String> colClass;
    @FXML private TableColumn<TicketViewModel, String> colSeat;
    @FXML private TableColumn<TicketViewModel, String> colStatus;

    private final ObservableList<TicketViewModel> tickets = FXCollections.observableArrayList();
    private TicketServiceGrpc.TicketServiceBlockingStub ticketService;

    @FXML
    public void initialize() {
        colTrain.setCellValueFactory(data -> data.getValue().trainProperty());
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());
        colTime.setCellValueFactory(data -> data.getValue().timeProperty());
        colClass.setCellValueFactory(data -> data.getValue().serviceClassProperty());
        colSeat.setCellValueFactory(data -> data.getValue().seatProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        ticketsTable.setItems(tickets);

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        ticketService = TicketServiceGrpc.newBlockingStub(channel);
        loadTicketsFromServer();
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
        try {
            // Carica la finestra di dialogo per la modifica
            FXMLLoader loader = new FXMLLoader(new java.io.File("src/main/java/it/unical/trenical/client/gui/view/modify_ticket_dialog.fxml").toURI().toURL());
            DialogPane dialogPane = loader.load();
            ModifyTicketDialogController dialogController = loader.getController();
            // Precompila i campi con i dati attuali del biglietto
            dialogController.setFields(
                selected.trainProperty().get(),
                selected.seatProperty().get(),
                java.time.LocalDate.parse(selected.dateProperty().get()),
                selected.serviceClassProperty().get()
            );
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifica Biglietto");
            dialog.setResizable(true);
            // Aggiorna il prezzo quando cambiano i campi
            dialogController.getClassBox().valueProperty().addListener((obs, oldVal, newVal) -> {
                updateDialogPrice(dialogController);
            });
            dialogController.getDatePicker().valueProperty().addListener((obs, oldVal, newVal) -> {
                updateDialogPrice(dialogController);
            });
            dialogController.getDepartureStationField().textProperty().addListener((obs, oldVal, newVal) -> {
                updateDialogPrice(dialogController);
            });
            dialogController.getArrivalStationField().textProperty().addListener((obs, oldVal, newVal) -> {
                updateDialogPrice(dialogController);
            });
            // Calcola subito il prezzo iniziale
            updateDialogPrice(dialogController);
            // Mostra la dialog e gestisci la conferma
            dialog.setResultConverter(button -> button);
            ButtonType conferma = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(conferma, ButtonType.CANCEL);
            dialog.showAndWait().ifPresent(result -> {
                if (result == conferma) {
                    // Invia richiesta di modifica al server
                    try {
                        var reqBuilder = it.unical.trenical.grpc.ticket.ModifyTicketRequest.newBuilder()
                                .setTicketId(selected.getTicketId())
                                .setNewServiceClass(dialogController.getServiceClass())
                                .setNewDepartureStation(dialogController.getDepartureStation())
                                .setNewArrivalStation(dialogController.getArrivalStation());
                        java.time.LocalDate date = dialogController.getDate();
                        if (date != null) {
                            reqBuilder.setNewDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(date.atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond()).build());
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
        } catch (Exception e) {
            AlertUtils.showError("Errore", "Impossibile aprire la finestra di modifica: " + e.getMessage());
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
        tickets.remove(selected);
        refreshTicketsTable();
    }

    /**
     * Torna alla dashboard.
     */
    @FXML
    private void onBack() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }

    /**
     * --- Utility per aggiornare il prezzo nella dialog di modifica ---
     */
    private void updateDialogPrice(ModifyTicketDialogController dialogController) {
        try {
            // Simula una richiesta di acquisto per ottenere il prezzo aggiornato
            String username = UserSession.getUsername();
            if (username == null || username.isEmpty()) {
                dialogController.setPrice("Prezzo: -");
                return;
            }
            // Per la modifica, usiamo lo stesso metodo di calcolo prezzo dell'acquisto
            var req = it.unical.trenical.grpc.ticket.PurchaseTicketRequest.newBuilder()
                    .setTrainId(1) // Dummy, il server userà le stazioni
                    .setDepartureStation(dialogController.getDepartureStation())
                    .setArrivalStation(dialogController.getArrivalStation())
                    .setServiceClass(dialogController.getServiceClass())
                    .setSeats(1)
                    .setPassengerName(username)
                    .setTravelDate(com.google.protobuf.Timestamp.newBuilder().setSeconds(dialogController.getDate().atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond()).build())
                    .build();
            var resp = ticketService.purchaseTicket(req);
            dialogController.setPrice("Prezzo: " + String.format("%.2f", resp.getPrice()) + " €");
        } catch (Exception e) {
            dialogController.setPrice("Prezzo: errore");
        }
    }

    /**
     * ViewModel per la visualizzazione dei biglietti nella tabella.
     */
    public static class TicketViewModel {
        private final StringProperty train;
        private final StringProperty date;
        private final StringProperty time;
        private final StringProperty serviceClass;
        private final StringProperty seat;
        private final StringProperty status;
        private final String ticketId;

        public TicketViewModel(Ticket ticket) {
            this.train = new SimpleStringProperty("Treno " + ticket.getTrainId());
            String dateStr = "";
            String timeStr = "";
            if (ticket.hasTravelDate()) {
                Instant instant = Instant.ofEpochSecond(ticket.getTravelDate().getSeconds());
                LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                dateStr = ldt.toLocalDate().toString();
                timeStr = ldt.toLocalTime().toString();
            }
            this.date = new SimpleStringProperty(dateStr);
            this.time = new SimpleStringProperty(timeStr);
            this.serviceClass = new SimpleStringProperty(ticket.getServiceClass());
            this.seat = new SimpleStringProperty(ticket.getSeat());
            this.status = new SimpleStringProperty(ticket.getStatus());
            this.ticketId = ticket.getId();
        }

        public StringProperty trainProperty() { return train; }
        public StringProperty dateProperty() { return date; }
        public StringProperty timeProperty() { return time; }
        public StringProperty serviceClassProperty() { return serviceClass; }
        public StringProperty seatProperty() { return seat; }
        public StringProperty statusProperty() { return status; }
        public String getTicketId() { return ticketId; }
    }
}
