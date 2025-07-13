package it.unical.trenical.client.gui.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.gui.util.AlertUtils;
import it.unical.trenical.client.session.UserSession;
import it.unical.trenical.grpc.notification.*;
import it.unical.trenical.grpc.ticket.TicketServiceGrpc;
import it.unical.trenical.grpc.ticket.ListTicketsRequest;
import it.unical.trenical.grpc.common.Ticket;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

 /**
 * Controller per la gestione delle notifiche utente.
 * Gestisce la visualizzazione, aggiornamento automatico e notifiche di sistema.
 */
public class NotificationsController {

    // --- Campi UI ---
    @FXML private TableView<NotificationViewModel> notificationsTable;
    @FXML private TableColumn<NotificationViewModel, String> colTitle, colMessage, colTime, colType;
    @FXML private Label statusLabel;
    @FXML private CheckBox showReadCheckbox;

    // --- Servizi e stato ---
    private ManagedChannel channel;
    private NotificationServiceGrpc.NotificationServiceBlockingStub notificationService;
    private final ObservableList<NotificationViewModel> notifications = FXCollections.observableArrayList();
    private Timer autoRefreshTimer;
    private static final int REFRESH_MS = 30_000;
    private static final String PREFS_FILE = System.getProperty("user.home") + "/trenical_notifications.json";
    private TrayIcon trayIcon;

    /**
     * Inizializza il controller configurando servizi, tabella e timer automatico.
     */
    @FXML
    public void initialize() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        notificationService = NotificationServiceGrpc.newBlockingStub(channel);
        setupTable();
        showReadCheckbox.selectedProperty().addListener((obs, o, n) -> loadNotifications());
        loadNotifications();
        startAutoRefresh();
    }

    private void setupTable() {
        colTitle.setCellValueFactory(data -> data.getValue().titleProperty());
        colMessage.setCellValueFactory(data -> data.getValue().messageProperty());
        colTime.setCellValueFactory(data -> data.getValue().timeProperty());
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colType.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle(empty || item == null ? "" : "-fx-font-weight: bold;");
                if (!empty && item != null) {
                    setTextFill(switch (item) {
                        case "Ritardo" -> Color.ORANGE;
                        case "Cancellazione" -> Color.RED;
                        case "Cambio Binario" -> Color.BLUE;
                        case "Promozione" -> Color.GREEN;
                        default -> Color.BLACK;
                    });
                }
            }
        });
        notificationsTable.setItems(notifications);
        notificationsTable.setRowFactory(tv -> {
            TableRow<NotificationViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty())
                    markAsRead(row.getItem());
            });
            return row;
        });
    }

    private void showSystemNotification(String title, String message) {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            if (trayIcon == null) {
                Image image = Toolkit.getDefaultToolkit().createImage(new byte[0]);
                trayIcon = new TrayIcon(image, "TreniCal");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("TreniCal Notifiche");
                tray.add(trayIcon);
            }
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (Exception ignored) {}
    }

    private void loadNotifications() {
        String username = UserSession.getUsername();
        if (username == null || username.isEmpty()) {
            statusLabel.setText("Utente non loggato");
            return;
        }
        try {
            GetNotificationsRequest req = GetNotificationsRequest.newBuilder()
                    .setUsername(username)
                    .setIncludeRead(showReadCheckbox.isSelected())
                    .build();
            NotificationList response = notificationService.getNotifications(req);
            notifications.setAll(response.getNotificationsList().stream().map(NotificationViewModel::new).toList());
            var notRead = response.getNotificationsList().stream().filter(n -> !n.getRead()).toList();
            if (!notRead.isEmpty()) {
                showSystemNotification("Nuove notifiche TreniCal", notRead.stream()
                        .map(n -> n.getTitle() + ": " + n.getMessage()).reduce("", (a, b) -> a + "\n" + b));
            }
            statusLabel.setText("Notifiche aggiornate: " + notifications.size());
        } catch (Exception e) {
            statusLabel.setText("Errore: " + e.getMessage());
        }
    }

    private void markAsRead(NotificationViewModel notification) {
        if (notification.isRead()) return;
        try {
            var req = MarkNotificationRequest.newBuilder().setNotificationId(notification.getId()).build();
            var resp = notificationService.markNotificationAsRead(req);
            if (resp.getSuccess()) {
                notification.setRead(true);
                notificationsTable.refresh();
                if (!showReadCheckbox.isSelected()) notifications.remove(notification);
            } else AlertUtils.showError("Errore", resp.getMessage());
        } catch (Exception e) {
            AlertUtils.showError("Errore", "Impossibile segnare la notifica come letta: " + e.getMessage());
        }
    }

    /**
     * Segna tutte le notifiche come lette.
     */
    @FXML
    private void onMarkAllRead() {
        for (NotificationViewModel notification : FXCollections.observableArrayList(notifications)) {
            if (!notification.isRead()) {
                markAsRead(notification);
            }
        }
    }

    /**
     * Aggiorna manualmente le notifiche.
     */
    @FXML
    private void onRefresh() {
        loadNotifications();
    }
    @FXML private void onBack() { SceneManager.getInstance().showDashboard(); }

    private void startAutoRefresh() {
        if (autoRefreshTimer != null) autoRefreshTimer.cancel();
        autoRefreshTimer = new Timer(true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { Platform.runLater(NotificationsController.this::loadNotifications); }
        }, REFRESH_MS, REFRESH_MS);
    }

    public void shutdown() {
        if (autoRefreshTimer != null) autoRefreshTimer.cancel();
        if (channel != null && !channel.isShutdown()) {
            try { channel.shutdown().awaitTermination(5, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    // --- Preferenze notifiche ---

    @FXML
    private void onActivateNotifications() {
        String username = UserSession.getUsername();
        if (username == null || username.isEmpty()) {
            AlertUtils.showError("Errore", "Devi effettuare il login per gestire le notifiche.");
            return;
        }
        ManagedChannel tchannel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        try {
            var ticketStub = TicketServiceGrpc.newBlockingStub(tchannel);
            var req = ListTicketsRequest.newBuilder().setPassengerName(username).build();
            var resp = ticketStub.listTickets(req);
            List<Ticket> tickets = resp.getTicketsList();
            if (tickets.isEmpty()) {
                AlertUtils.showInfo("Info", "Non hai biglietti attivi per nessun treno.");
                return;
            }
            Set<Integer> subscribedTrainIds = loadSubscribedTrainIds();
            Dialog<List<Ticket>> dialog = new Dialog<>();
            dialog.setTitle("Notifiche treno");
            dialog.setHeaderText("Seleziona treni per notifiche");
            ButtonType okBtn = new ButtonType("Salva notifiche", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);
            ListView<CheckBox> listView = new ListView<>();
            ObservableList<CheckBox> boxes = FXCollections.observableArrayList();
            for (Ticket t : tickets) {
                CheckBox cb = new CheckBox("Treno " + t.getTrainId() + " - " + t.getDepartureStation() + " → " + t.getArrivalStation());
                cb.setUserData(t);
                cb.setSelected(subscribedTrainIds.contains(t.getTrainId()));
                boxes.add(cb);
            }
            listView.setItems(boxes);
            listView.setPrefHeight(Math.min(300, tickets.size() * 40 + 20));
            Button selectAllBtn = new Button("Seleziona tutti");
            selectAllBtn.setOnAction(e -> boxes.forEach(cb -> cb.setSelected(true)));
            dialog.getDialogPane().setContent(new VBox(10, selectAllBtn, listView));
            dialog.setResultConverter(dialogBtn -> dialogBtn == okBtn ? boxes.stream().filter(CheckBox::isSelected).map(cb -> (Ticket) cb.getUserData()).toList() : null);
            dialog.showAndWait().ifPresent(selectedTickets -> {
                Set<Integer> selIds = new HashSet<>();
                selectedTickets.forEach(t -> selIds.add(t.getTrainId()));
                updateSubscriptions(username, tickets, subscribedTrainIds, selIds);
                saveSubscribedTrainIds(selIds);
            });
        } catch (Exception e) {
            AlertUtils.showError("Errore", "Impossibile gestire notifiche: " + e.getMessage());
        } finally {
            if (tchannel != null && !tchannel.isShutdown()) {
                tchannel.shutdown();
            }
        }
    }

    private void updateSubscriptions(String username, List<Ticket> tickets, Set<Integer> wasSubscribed, Set<Integer> shouldBeSubscribed) {
        int success = 0, fail = 0;
        StringBuilder failMsg = new StringBuilder();
        for (Ticket t : tickets) {
            int id = t.getTrainId();
            boolean old = wasSubscribed.contains(id), now = shouldBeSubscribed.contains(id);
            try {
                if (!old && now) {
                    var resp = notificationService.registerForTrainUpdates(RegisterForTrainRequest.newBuilder().setUsername(username).setTrainId(id).build());
                    if (!resp.getSuccess()) { fail++; failMsg.append("\nTreno ").append(id).append(": ").append(resp.getMessage()); }
                    else success++;
                }
                else if (old && !now) {
                    var resp = notificationService.unregisterFromTrainUpdates(UnregisterRequest.newBuilder().setUsername(username).setTrainId(id).build());
                    if (!resp.getSuccess()) { fail++; failMsg.append("\nTreno ").append(id).append(": ").append(resp.getMessage()); }
                    else success++;
                }
            } catch (Exception e) { fail++; failMsg.append("\nTreno ").append(id).append(": ").append(e.getMessage()); }
        }
        if (success > 0) AlertUtils.showInfo("Preferenze aggiornate", (fail > 0 ? "Alcuni errori:" + failMsg : "Preferenze notifiche aggiornate!"));
        else if (fail > 0) AlertUtils.showError("Errore", "Errore: nessuna preferenza aggiornata." + failMsg);
    }

    private void saveSubscribedTrainIds(Set<Integer> trainIds) {
        try { Files.write(Paths.get(PREFS_FILE), new JSONArray(trainIds).toString().getBytes(StandardCharsets.UTF_8)); }
        catch (Exception ignored) {}
    }
    private Set<Integer> loadSubscribedTrainIds() {
        try {
            Path path = Paths.get(PREFS_FILE);
            if (Files.exists(path)) {
                JSONArray arr = new JSONArray(Files.readString(path, StandardCharsets.UTF_8));
                Set<Integer> result = new HashSet<>();
                for (int i = 0; i < arr.length(); i++) result.add(arr.getInt(i));
                return result;
            }
        } catch (Exception ignored) {}
        return new HashSet<>();
    }

    // --- ViewModel ---
    public static class NotificationViewModel {
        private final SimpleStringProperty title, message, time, type;
        private final String id;
        private boolean read;
        public NotificationViewModel(Notification n) {
            this.title = new SimpleStringProperty(n.getTitle());
            this.message = new SimpleStringProperty(n.getMessage());
            LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(n.getTimestamp().getSeconds()), ZoneId.systemDefault());
            this.time = new SimpleStringProperty(ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            this.type = new SimpleStringProperty(switch (n.getType()) {
                case TRAIN_DELAY -> "Ritardo";
                case TRAIN_CANCELLATION -> "Cancellazione";
                case PLATFORM_CHANGE -> "Cambio Binario";
                case PROMOTION -> "Promozione";
                case TRAIN_STATUS -> "Stato Treno";
                default -> "Generale";
            });
            this.id = n.getId();
            this.read = n.getRead();
        }
        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty messageProperty() { return message; }
        public SimpleStringProperty timeProperty() { return time; }
        public SimpleStringProperty typeProperty() { return type; }
        public String getId() { return id; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }
}
