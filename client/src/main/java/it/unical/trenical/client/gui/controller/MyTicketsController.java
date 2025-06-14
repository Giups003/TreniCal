package it.unical.trenical.client.gui.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.gui.util.AlertUtils;
import it.unical.trenical.client.session.UserSession;
import it.unical.trenical.grpc.common.Ticket;
import it.unical.trenical.grpc.ticket.*;
import it.unical.trenical.grpc.train.TrainServiceGrpc;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;


/**
 * Controller per la schermata di visualizzazione biglietti acquistati.
 */
public class MyTicketsController {
    @FXML private TableView<TicketViewModel> ticketsTable;
    @FXML private TableColumn<TicketViewModel, String> colTrain;
    @FXML private TableColumn<TicketViewModel, String> colDeparture;
    @FXML private TableColumn<TicketViewModel, String> colDepartureTime;
    @FXML private TableColumn<TicketViewModel, String> colArrival;
    @FXML private TableColumn<TicketViewModel, String> colClass;
    @FXML private TableColumn<TicketViewModel, String> colSeat;
    @FXML private TableColumn<TicketViewModel, String> colStatus;
    @FXML private TableColumn<TicketViewModel, String> colPurchaseDate;
    @FXML private Button clearAllButton;
    @FXML private Button toggleViewButton;

    private boolean showAllTickets = true;
    private final ObservableList<TicketViewModel> tickets = FXCollections.observableArrayList();
    private TicketServiceGrpc.TicketServiceBlockingStub ticketService;
    private TrainServiceGrpc.TrainServiceBlockingStub trainService;
    private ManagedChannel channel;

    private static final double PENALTY_PERCENTAGE = 0.10; // 10% penale annullamento

    @FXML
    public void initialize() {
        colTrain.setCellValueFactory(data -> data.getValue().trainProperty());
        colDeparture.setCellValueFactory(data -> data.getValue().departureProperty());
        colDepartureTime.setCellValueFactory(data -> data.getValue().departureDateTimeProperty());
        colArrival.setCellValueFactory(data -> data.getValue().arrivalProperty());
        colClass.setCellValueFactory(data -> data.getValue().serviceClassProperty());
        colSeat.setCellValueFactory(data -> data.getValue().seatProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colPurchaseDate.setCellValueFactory(data -> data.getValue().purchaseDateProperty());
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

        // Imposta la visibilità dei pulsanti per l'amministratore
        boolean isAdmin = UserSession.isAdmin();
        if (clearAllButton != null) {
            clearAllButton.setVisible(isAdmin);
        }
        if (toggleViewButton != null) {
            toggleViewButton.setVisible(isAdmin);
            toggleViewButton.setText("Mostra solo i miei biglietti");
        }
    }

    private void loadTicketsFromServer() {
        loadTicketsFromServer(false);
    }

    /**
     * Carica i biglietti dal server.
     * @param onlyPersonal Se true, anche l'admin vedrà solo i propri biglietti personali
     */
    private void loadTicketsFromServer(boolean onlyPersonal) {
        try {
            String username = UserSession.getUsername();
            if (username == null || username.isEmpty()) {
                AlertUtils.showError("Errore", "Utente non loggato. Effettua il login.");
                return;
            }
            ListTicketsRequest.Builder reqBuilder = ListTicketsRequest.newBuilder();
            // Se admin e non richiede solo i propri biglietti personali, carica tutti i biglietti
            if (!UserSession.isAdmin() || onlyPersonal) {
                reqBuilder.setPassengerName(username);
            }
            ListTicketsResponse resp = ticketService.listTickets(reqBuilder.build());
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
        // Non permettere la modifica se il biglietto è annullato o scaduto
        String status = selected.statusProperty().get();
        if (status.equalsIgnoreCase("Annullato") || status.equalsIgnoreCase("Scaduto")) {
            AlertUtils.showError("Non modificabile", "Non puoi modificare un biglietto annullato o scaduto.");
            return;
        }
        // Passa i dati del biglietto selezionato a SceneManager
        SceneManager.getInstance().showModifyTicketDialog(
            selected.getTicketId(),
            selected.departureProperty().get(),
            selected.arrivalProperty().get(),
            java.time.LocalDate.parse(selected.dateProperty().get()),
            selected.departureTimeProperty().get(),
            selected.serviceClassProperty().get(),
            selected.getTrainId(), // Passa il trainId del biglietto selezionato
            () -> loadTicketsFromServer()
        );
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
        // Recupera il prezzo del biglietto (se disponibile)
        double price = selected.getPrice();
        double penalty = price * PENALTY_PERCENTAGE;
        double refund = price - penalty;
        String msg;
        if (price > 0.0) {
            msg = String.format("Il prezzo pagato per il biglietto è di %.2f €.\nVerrà applicata una penale del %.0f%% (%.2f €).\nCredito rimborsato: %.2f €.\n\nVuoi procedere con l'annullamento?", price, PENALTY_PERCENTAGE*100, penalty, refund);
        } else {
            msg = "Vuoi davvero annullare il biglietto selezionato?";
        }
        boolean conferma = AlertUtils.showConfirm("Conferma annullamento", msg);
        if (!conferma) return;
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
        SceneManager.getInstance().showDashboard();
    }

    /**
     * Gestisce la richiesta di svuotamento della lista dei biglietti.
     */
    @FXML
    private void onClearAll() {
        // Solo admin può svuotare la lista
        if (!UserSession.isAdmin()) {
            AlertUtils.showError("Permesso negato", "Solo un amministratore può svuotare tutti i biglietti.");
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
     * Gestisce il toggle per visualizzare solo i biglietti personali o tutti i biglietti.
     */
    @FXML
    private void onToggleView() {
        showAllTickets = !showAllTickets;
        toggleViewButton.setText(showAllTickets ? "Mostra solo i miei biglietti" : "Mostra tutti i biglietti");
        loadTicketsFromServer(!showAllTickets);
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
        private final StringProperty serviceClass;
        private final StringProperty seat;
        private final StringProperty status;
        private final String ticketId;
        private final StringProperty departureDateTime;
        private final StringProperty purchaseDate;
        private final double price;
        private final int trainId;

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

            // Data di acquisto: usa quella fornita dal server se disponibile
            String purchaseDateStr = "";
            if (ticket.hasPurchaseDate()) {
                Instant purchaseInstant = Instant.ofEpochSecond(ticket.getPurchaseDate().getSeconds());
                LocalDateTime purchaseLdt = LocalDateTime.ofInstant(purchaseInstant, ZoneId.systemDefault());
                purchaseDateStr = purchaseLdt.toString();
            }
            this.purchaseDate = new SimpleStringProperty(purchaseDateStr);
            this.price = ticket.getPrice();
            this.trainId = ticket.getTrainId();
        }

        public StringProperty trainProperty() { return train; }
        public StringProperty departureProperty() { return departure; }
        public StringProperty departureTimeProperty() { return departureTime; }
        public StringProperty arrivalProperty() { return arrival; }
        public StringProperty dateProperty() { return date; }
        public StringProperty serviceClassProperty() { return serviceClass; }
        public StringProperty seatProperty() { return seat; }
        public StringProperty statusProperty() { return status; }
        public String getTicketId() { return ticketId; }
        public StringProperty departureDateTimeProperty() { return departureDateTime; }
        public StringProperty purchaseDateProperty() { return purchaseDate; }
        public double getPrice() { return price; }
        public int getTrainId() { return trainId; }
    }
}
