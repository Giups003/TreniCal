package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.session.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller per il pannello admin che mostra tutti gli utenti.
 * Visualizza username, email, stato della membership fedeltà e biglietti.
 */
public class AdminPanelController {
    @FXML
    private TableView<UserRow> usersTable;
    @FXML
    private TableColumn<UserRow, String> usernameCol;
    @FXML
    private TableColumn<UserRow, String> emailCol;
    @FXML
    private TableColumn<UserRow, String> fidelityCol;
    @FXML
    private TableColumn<UserRow, String> ticketsCol;

    @FXML
    public void initialize() {
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        fidelityCol.setCellValueFactory(new PropertyValueFactory<>("fidelity"));
        ticketsCol.setCellValueFactory(new PropertyValueFactory<>("tickets"));
        loadUsers();
    }

    private void loadUsers() {
        List<UserManager.User> users = UserManager.getAllUsers();
        ObservableList<UserRow> data = FXCollections.observableArrayList();
        for (UserManager.User u : users) {
            String tickets = String.join(", ", u.tickets);
            data.add(new UserRow(u.username, u.email, u.fidelityMember ? "Sì" : "No", tickets));
        }
        usersTable.setItems(data);
    }

    @FXML
    private void onBack() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }

    public static class UserRow {
        private final String username;
        private final String email;
        private final String fidelity;
        private final String tickets;

        public UserRow(String username, String email, String fidelity, String tickets) {
            this.username = username;
            this.email = email;
            this.fidelity = fidelity;
            this.tickets = tickets;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getFidelity() {
            return fidelity;
        }

        public String getTickets() {
            return tickets;
        }
    }
}



