package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.session.UserSession;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.grpc.notification.NotificationServiceGrpc;
import it.unical.trenical.grpc.notification.RegisterForTrainRequest;
import it.unical.trenical.grpc.notification.OperationResponse;
import it.unical.trenical.grpc.ticket.TicketServiceGrpc;
import it.unical.trenical.grpc.ticket.ListTicketsRequest;
import it.unical.trenical.grpc.ticket.ListTicketsResponse;
import it.unical.trenical.grpc.common.Ticket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller della schermata dashboard.
 * Responsabile della gestione della dashboard utente:
 * - Mostra/nasconde pulsanti e label in base allo stato dell'utente (login, admin, fedeltà, ecc.)
 * - Gestisce la navigazione tra le schermate tramite SceneManager
 */
public class DashboardController {

    @FXML
    private Label emailLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private Button joinLoyaltyButton;
    @FXML
    private Label loyaltyLabel;
    @FXML
    private Button searchTrainsButton;
    @FXML
    private Button buyTicketButton;
    @FXML
    private Button myTicketsButton;
    @FXML
    private Button notificationsButton;
    @FXML
    private Button adminPromotionsButton;
    @FXML
    private Label adminLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button adminPanelButton;
    @FXML
    private Button activateNotificationsButton;
    @FXML
    private CheckBox promotionsCheckBox;

    @FXML
    public void initialize() {
        if (adminPromotionsButton != null) {
            boolean isAdmin = UserSession.isAdmin();
            adminPromotionsButton.setVisible(isAdmin);
            adminPromotionsButton.setManaged(isAdmin);
        }
        if (adminLabel != null) {
            boolean isAdmin = UserSession.isAdmin();
            adminLabel.setVisible(isAdmin);
            adminLabel.setManaged(isAdmin);
        }
        if (adminPanelButton != null) {
            boolean isAdmin = UserSession.isAdmin();
            adminPanelButton.setVisible(isAdmin);
            adminPanelButton.setManaged(isAdmin);
        }
        if (loyaltyLabel != null && joinLoyaltyButton != null) {
            updateLoyaltyUI();
        }
        if (emailLabel != null) {
            updateEmailLabel();
        }
        // --- VISIBILITÀ LOGOUT ---
        if (logoutButton != null) {
            boolean isLogged = UserSession.getUsername() != null && !UserSession.getUsername().isEmpty();
            logoutButton.setVisible(isLogged);
            logoutButton.setManaged(isLogged);
        }
        updateLoginUI();
        // Mostra il pulsante login se non loggato
        if (loginButton != null) {
            boolean loggedIn = UserSession.getUsername() != null && !UserSession.getUsername().isEmpty();
            loginButton.setVisible(!loggedIn);
            loginButton.setManaged(!loggedIn);
        }
        if (searchTrainsButton != null) {
            searchTrainsButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                        if (newWin != null) {
                            javafx.application.Platform.runLater(this::updateStageMinSize);
                        }
                    });
                }
            });
        }
        updateStageMinSize();
    }

    private void updateLoyaltyUI() {
        boolean isLogged = UserSession.getUsername() != null && !UserSession.getUsername().isEmpty();
        boolean isLoyal = UserSession.isLoyaltyMember();
        if (!isLogged) {
            loyaltyLabel.setText("Effettua il login per aderire a FedeltàTreno");
            joinLoyaltyButton.setVisible(false);
            joinLoyaltyButton.setManaged(false);
        } else if (isLoyal) {
            loyaltyLabel.setText("Sei membro FedeltàTreno!");
            joinLoyaltyButton.setVisible(false);
            joinLoyaltyButton.setManaged(false);
        } else {
            loyaltyLabel.setText("Non sei ancora membro FedeltàTreno");
            joinLoyaltyButton.setVisible(true);
            joinLoyaltyButton.setManaged(true);
        }
        if (isLogged && isLoyal && promotionsCheckBox != null) {
            promotionsCheckBox.setVisible(true);
            promotionsCheckBox.setDisable(false);
            promotionsCheckBox.setText("Ricevi promozioni FedeltàTreno");
            // Recupera la preferenza dal server
            javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {
                @Override
                protected Boolean call() {
                    try {
                        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
                        NotificationServiceGrpc.NotificationServiceBlockingStub stub = NotificationServiceGrpc.newBlockingStub(channel);
                        var req = it.unical.trenical.grpc.notification.PromotionalPreferenceRequest.newBuilder()
                                .setUsername(UserSession.getUsername())
                                .build();
                        var resp = stub.getPromotionalPreference(req);
                        channel.shutdown();
                        return resp.getWantsPromotions();
                    } catch (Exception e) {
                        return true; // fallback: attivo
                    }
                }
            };
            task.setOnSucceeded(ev -> promotionsCheckBox.setSelected(task.getValue()));
            new Thread(task).start();
            promotionsCheckBox.setOnAction(e -> {
                boolean value = promotionsCheckBox.isSelected();
                javafx.concurrent.Task<Void> updateTask = new javafx.concurrent.Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
                            NotificationServiceGrpc.NotificationServiceBlockingStub stub = NotificationServiceGrpc.newBlockingStub(channel);
                            var req = it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest.newBuilder()
                                    .setUsername(UserSession.getUsername())
                                    .setWantsPromotions(value)
                                    .build();
                            stub.setPromotionalPreference(req);
                            channel.shutdown();
                        } catch (Exception ignored) {}
                        return null;
                    }
                };
                new Thread(updateTask).start();
            });
        } else if (promotionsCheckBox != null) {
            promotionsCheckBox.setVisible(false);
            promotionsCheckBox.setDisable(true);
        }
        updateStageMinSize();
    }

    private void updateEmailLabel() {
        String email = UserSession.getEmail();
        boolean show = email != null && !email.isBlank();
        emailLabel.setText(show ? ("Email: " + email) : "");
        emailLabel.setVisible(show);
        emailLabel.setManaged(show);
    }

    private void updateLoginUI() {
        boolean loggedIn = UserSession.getUsername() != null && !UserSession.getUsername().isEmpty();
        if (loginButton != null) {
            loginButton.setVisible(!loggedIn);
            loginButton.setManaged(!loggedIn);
        }
        searchTrainsButton.setDisable(!loggedIn);
        buyTicketButton.setDisable(!loggedIn);
        myTicketsButton.setDisable(!loggedIn);
        notificationsButton.setDisable(!loggedIn);
        joinLoyaltyButton.setDisable(!loggedIn);
        adminPromotionsButton.setDisable(!loggedIn || !UserSession.isAdmin());
        adminLabel.setVisible(UserSession.isAdmin());
        emailLabel.setVisible(loggedIn);
        emailLabel.setManaged(loggedIn);
        if (loggedIn) {
            emailLabel.setText("Utente: " + UserSession.getUsername() + " (" + UserSession.getEmail() + ")");
        } else {
            emailLabel.setText("Utente non loggato");
        }
        updateStageMinSize();
    }

    private void updateStageMinSize() {
        javafx.application.Platform.runLater(() -> {
            if (searchTrainsButton == null || searchTrainsButton.getScene() == null) return;
            javafx.stage.Window window = searchTrainsButton.getScene().getWindow();
            if (!(window instanceof javafx.stage.Stage)) return;
            javafx.stage.Stage stage = (javafx.stage.Stage) window;
            double minW = searchTrainsButton.getScene().getRoot().minWidth(0);
            double minH = searchTrainsButton.getScene().getRoot().minHeight(0);
            stage.setMinWidth(minW);
            stage.setMinHeight(minH);
        });
    }

    @FXML
    private void onSearchTrains(ActionEvent event) {
        SceneManager.getInstance().switchTo(SceneManager.SEARCH_TRAINS);
    }

    @FXML
    private void onBuyTicket(ActionEvent event) {
        SceneManager.getInstance().switchTo(SceneManager.BUY_TICKET);
    }

    @FXML
    private void onMyTickets(ActionEvent event) {
        if (UserSession.getUsername() == null || UserSession.getUsername().isEmpty()) {
            // Mostra errore e non naviga
            Alert alert = new Alert(Alert.AlertType.ERROR, "Devi effettuare il login per visualizzare i tuoi biglietti.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        SceneManager.getInstance().switchTo(SceneManager.MY_TICKETS);
    }

    @FXML
    private void onLogin() {
        SceneManager.getInstance().showLogin();
    }

    @FXML
    private void onLogout(ActionEvent event) {
        UserSession.setUsername(null);
        UserSession.setAdmin(false);
        UserSession.setLoyaltyMember(false);
        UserSession.setEmail(null);
        UserSession.setUserId(-1);
        updateLoginUI();
        SceneManager.getInstance().showLogin();
        if (loyaltyLabel != null && joinLoyaltyButton != null) {
            updateLoyaltyUI();
        }
        if (emailLabel != null) {
            updateEmailLabel();
        }
        if (logoutButton != null) {
            logoutButton.setVisible(false);
            logoutButton.setManaged(false);
        }
        updateStageMinSize();
    }

    @FXML
    private void onAdminPromotions(ActionEvent event) {
        SceneManager.getInstance().showPromotionsAdmin();
    }

    @FXML
    private void onNotifications(ActionEvent event) {
        if (UserSession.getUsername() == null || UserSession.getUsername().isEmpty()) {
            // Mostra errore e non naviga
            Alert alert = new Alert(Alert.AlertType.ERROR, "Devi effettuare il login per visualizzare le notifiche.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        SceneManager.getInstance().showNotifications();
    }

    @FXML
    private void onJoinLoyalty() {
        if (UserSession.getUsername() == null || UserSession.getUsername().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Devi effettuare il login per aderire a FedeltàTreno.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        UserSession.setLoyaltyMember(true);
        updateLoyaltyUI();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ora sei membro FedeltàTreno!", ButtonType.OK);
        alert.showAndWait();
        updateStageMinSize();
    }

    @FXML
    private void onAdminPanel() {
        SceneManager.getInstance().showAdminPanel();
        updateStageMinSize();
    }

    @FXML
    private void onLogout() {
        // Chiamata diretta al logout del LoginController
        try {
            it.unical.trenical.client.gui.controller.LoginController loginController = new it.unical.trenical.client.gui.controller.LoginController();
            loginController.onLogout();
        } catch (Exception e) {
            // Fallback: reset manuale
            it.unical.trenical.client.session.UserSession.setUsername("");
            it.unical.trenical.client.session.UserSession.setEmail("");
            it.unical.trenical.client.session.UserSession.setAdmin(false);
            it.unical.trenical.client.gui.SceneManager.getInstance().switchTo(it.unical.trenical.client.gui.SceneManager.LOGIN);
        }
    }

    @FXML
    private void onActivateNotifications() {
        String username = UserSession.getUsername();
        if (username == null || username.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Devi effettuare il login per attivare le notifiche.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        // Recupera i biglietti dell'utente
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        TicketServiceGrpc.TicketServiceBlockingStub ticketStub = TicketServiceGrpc.newBlockingStub(channel);
        ListTicketsRequest req = ListTicketsRequest.newBuilder().setPassengerName(username).build();
        ListTicketsResponse resp = ticketStub.listTickets(req);
        List<Ticket> tickets = resp.getTicketsList();
        if (tickets.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Non hai biglietti attivi per nessun treno.", ButtonType.OK);
            alert.showAndWait();
            channel.shutdown();
            return;
        }
        // Dialog personalizzata con selezione multipla
        Dialog<List<Ticket>> dialog = new Dialog<>();
        dialog.setTitle("Attiva notifiche treno");
        dialog.setHeaderText("Seleziona uno o più treni per cui ricevere notifiche");
        ButtonType okButtonType = new ButtonType("Attiva notifiche", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        ObservableList<Ticket> observableTickets = FXCollections.observableArrayList(tickets);
        ListView<CheckBox> listView = new ListView<>();
        ObservableList<CheckBox> checkBoxes = FXCollections.observableArrayList();
        for (Ticket t : tickets) {
            String label = "Treno " + t.getTrainId() + " - " + t.getDepartureStation() + " → " + t.getArrivalStation();
            CheckBox cb = new CheckBox(label);
            cb.setUserData(t);
            checkBoxes.add(cb);
        }
        listView.setItems(checkBoxes);
        listView.setPrefHeight(Math.min(300, tickets.size() * 40 + 20));
        // Pulsante seleziona tutti
        Button selectAllBtn = new Button("Seleziona tutti");
        selectAllBtn.setOnAction(e -> checkBoxes.forEach(cb -> cb.setSelected(true)));
        VBox vbox = new VBox(10, selectAllBtn, listView);
        dialog.getDialogPane().setContent(vbox);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return checkBoxes.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> (Ticket) cb.getUserData())
                        .collect(Collectors.toList());
            }
            return null;
        });
        Optional<List<Ticket>> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            NotificationServiceGrpc.NotificationServiceBlockingStub notifStub = NotificationServiceGrpc.newBlockingStub(channel);
            int successCount = 0;
            int failCount = 0;
            StringBuilder failMsg = new StringBuilder();
            for (Ticket t : result.get()) {
                RegisterForTrainRequest regReq = RegisterForTrainRequest.newBuilder()
                        .setUsername(username)
                        .setTrainId(t.getTrainId())
                        .build();
                OperationResponse opResp = notifStub.registerForTrainUpdates(regReq);
                if (opResp.getSuccess()) {
                    successCount++;
                } else {
                    failCount++;
                    failMsg.append("\nTreno ").append(t.getTrainId()).append(": ").append(opResp.getMessage());
                }
            }
            if (successCount > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Notifiche attivate per " + successCount + " treni selezionati!" + (failCount > 0 ? "\nAlcuni errori:" + failMsg : ""), ButtonType.OK);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Errore: nessuna notifica attivata." + (failCount > 0 ? failMsg.toString() : ""), ButtonType.OK);
                alert.showAndWait();
            }
        }
        channel.shutdown();
    }
}
