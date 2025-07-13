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
 * Controller per il pannello amministrativo che gestisce la visualizzazione degli utenti.
 * Mostra una tabella con tutti gli utenti registrati e le loro informazioni principali.
 */
public class AdminPanelController {

    // --- Campi UI ---
    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, String> usernameCol;
    @FXML private TableColumn<UserRow, String> emailCol;
    @FXML private TableColumn<UserRow, String> fidelityCol;
    @FXML private TableColumn<UserRow, String> ticketsCol;

    /**
     * Inizializza il controller configurando la tabella degli utenti e caricando i dati.
     */
    @FXML
    public void initialize() {
        // Configura le colonne della tabella con PropertyValueFactory
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        fidelityCol.setCellValueFactory(new PropertyValueFactory<>("fidelity"));
        ticketsCol.setCellValueFactory(new PropertyValueFactory<>("tickets"));

        // Carica i dati utenti
        loadUsers();
    }

    /**
     * Carica tutti gli utenti dal sistema e li visualizza nella tabella.
     */
    private void loadUsers() {
        List<UserManager.User> users = UserManager.getAllUsers();
        ObservableList<UserRow> data = FXCollections.observableArrayList();

        for (UserManager.User u : users) {
            String tickets = String.join(", ", u.tickets);
            data.add(new UserRow(u.username, u.email, u.fidelityMember ? "Sì" : "No", tickets));
        }

        usersTable.setItems(data);
    }

    /**
     * Gestisce il ritorno alla dashboard principale.
     */
    @FXML
    private void onBack() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }

    /**
     * Classe per rappresentare una riga della tabella utenti.
     * I metodi getter sono utilizzati da JavaFX tramite reflection (PropertyValueFactory).
     */
    public static class UserRow {

        // --- Campi dati ---
        private final String username;
        private final String email;
        private final String fidelity;
        private final String tickets;

        /**
         * Costruttore per creare una riga della tabella utenti.
         * @param username Nome utente
         * @param email Email dell'utente
         * @param fidelity Stato membership fedeltà ("Sì" o "No")
         * @param tickets Lista biglietti separati da virgola
         */
        public UserRow(String username, String email, String fidelity, String tickets) {
            this.username = username;
            this.email = email;
            this.fidelity = fidelity;
            this.tickets = tickets;
        }

        // --- Getter utilizzati da JavaFX PropertyValueFactory tramite reflection ---

        /**
         * @return Il nome utente
         */
        public String getUsername() {
            return username;
        }

        /**
         * @return L'email dell'utente
         */
        public String getEmail() {
            return email;
        }

        /**
         * @return Lo stato della membership fedeltà
         */
        public String getFidelity() {
            return fidelity;
        }

        /**
         * @return La lista dei biglietti dell'utente
         */
        public String getTickets() {
            return tickets;
        }
    }
}
