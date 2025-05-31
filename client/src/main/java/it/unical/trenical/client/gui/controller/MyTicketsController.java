package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.grpc.common.Ticket;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    @FXML
    public void initialize() {
        colTrain.setCellValueFactory(data -> data.getValue().trainProperty());
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());
        colTime.setCellValueFactory(data -> data.getValue().timeProperty());
        colClass.setCellValueFactory(data -> data.getValue().serviceClassProperty());
        colSeat.setCellValueFactory(data -> data.getValue().seatProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        ticketsTable.setItems(tickets);
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
            showAlert("Seleziona un biglietto da modificare.");
            return;
        }
        // Da implementare: logica di modifica
        showAlert("Funzionalità di modifica non ancora implementata.");
    }

    /**
     * Gestisce l'annullamento di un biglietto selezionato.
     */
    @FXML
    private void onCancel() {
        TicketViewModel selected = ticketsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleziona un biglietto da annullare.");
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

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
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
        }

        public StringProperty trainProperty() { return train; }
        public StringProperty dateProperty() { return date; }
        public StringProperty timeProperty() { return time; }
        public StringProperty serviceClassProperty() { return serviceClass; }
        public StringProperty seatProperty() { return seat; }
        public StringProperty statusProperty() { return status; }
    }
}
