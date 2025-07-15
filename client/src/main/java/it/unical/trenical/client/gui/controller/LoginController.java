package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.gui.util.AlertUtils;
import it.unical.trenical.client.session.UserSession;
import it.unical.trenical.client.session.UserManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.prefs.Preferences;

/**
 * Controller per la gestione della schermata di login.
 * Permette l'accesso come utente normale o come admin.
 * Salva username ed email nelle preferenze locali.
 */
public class LoginController {

    // Costanti per le preferenze
    private static final String PREF_KEY = "trenical_username";
    private static final String PREF_EMAIL_KEY = "trenical_email";
    private static final String PREF_REMEMBER_KEY = "trenical_remember_me";

    // Campi UI
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private CheckBox rememberMeCheckBox;

    // Stato interno
    private boolean loginInProgress = false;

    /**
     * Inizializza i campi username ed email con i valori salvati nelle preferenze.
     */
    @FXML
    public void initialize() {
        loadSavedPreferences();
        setupDynamicUI();
        setupEnterKeyHandlers();
    }

    private void loadSavedPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        String savedUsername = prefs.get(PREF_KEY, "");
        String savedEmail = prefs.get(PREF_EMAIL_KEY, "");
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER_KEY, false);

        if (!savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
        }
        if (!savedEmail.isEmpty()) {
            emailField.setText(savedEmail);
        }
        if (rememberMeCheckBox != null) {
            rememberMeCheckBox.setSelected(rememberMe);
        }
    }

    private void setupDynamicUI() {
        // Aggiorna UI in base al tipo di utente (admin vs normale)
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if ("admin".equalsIgnoreCase(newVal.trim())) {
                emailField.setPromptText("Email (opzionale per admin)");
                passwordField.setPromptText("Password (obbligatoria)");
                if (rememberMeCheckBox != null) rememberMeCheckBox.setVisible(false);
            } else {
                emailField.setPromptText("Email (obbligatoria)");
                passwordField.setPromptText("Password (obbligatoria, richiesta insieme all'email)");
                if (rememberMeCheckBox != null) rememberMeCheckBox.setVisible(true);
            }
        });
    }

    private void setupEnterKeyHandlers() {
        // Permetti login con tasto Invio su tutti i campi
        usernameField.setOnAction(e -> onLogin());
        passwordField.setOnAction(e -> onLogin());
        emailField.setOnAction(e -> onLogin());
    }

    /**
     * Gestisce il tentativo di login.
     * Valida i dati inseriti, distingue tra login admin e utente normale,
     * salva le preferenze e aggiorna la sessione utente.
     */
    @FXML
    private void onLogin() {
        if (loginInProgress) return;
        loginInProgress = true;
        try {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            boolean rememberMe = rememberMeCheckBox != null && rememberMeCheckBox.isSelected();
            if (username == null || username.isBlank()) {
                AlertUtils.showError("Errore", "Il nome utente è obbligatorio.");
                return;
            }
            // Controllo validità email per utenti normali (non admin)
            if (!"admin".equalsIgnoreCase(username.trim())) {
                if (email == null || email.isBlank()) {
                    AlertUtils.showError("Errore", "L'email è obbligatoria.");
                    return;
                }
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    AlertUtils.showError("Errore", "Inserisci un indirizzo email valido.");
                    return;
                }
            }
            if ("admin".equalsIgnoreCase(username.trim())) {
                // Admin: password obbligatoria
                if (password == null || password.isBlank()) {
                    AlertUtils.showError("Errore", "La password è obbligatoria per l'admin.");
                    return;
                }
                if (!"admin".equals(password)) {
                    AlertUtils.showError("Accesso negato", "Password admin errata.");
                    return;
                }
                UserSession.setAdmin(true);
                UserSession.setLoyaltyMember(false); // Admin non è mai utente fedeltà
                UserSession.clearTickets();
                AlertUtils.showInfo("Accesso riuscito", "Login admin effettuato con successo.");
            } else {
                // Utente normale: login solo se registrato
                if (!UserManager.userExists(username)) {
                    AlertUtils.showError("Errore", "Utente non registrato. Premi 'Registrati' per creare un account.");
                    return;
                }
                if (!UserManager.validateLogin(username, password)) {
                    AlertUtils.showError("Errore", "Password errata.");
                    return;
                }
                UserSession.setAdmin(false);
                // Carica stato fedeltà e biglietti
                boolean fidelity = UserManager.isFidelityMember(username);
                UserSession.setLoyaltyMember(fidelity);
                UserSession.setTickets(UserManager.getTickets(username));
                AlertUtils.showInfo("Accesso riuscito", "Login utente effettuato con successo.");
                email = UserManager.getEmail(username);
            }
            // Salva il nome, email e password nelle preferenze
            Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
            if (rememberMe) {
                prefs.put(PREF_KEY, username);
                prefs.put(PREF_EMAIL_KEY, email);
                prefs.put("trenical_password", password);
                prefs.putBoolean(PREF_REMEMBER_KEY, true);
            } else {
                prefs.remove(PREF_KEY);
                prefs.remove(PREF_EMAIL_KEY);
                prefs.remove("trenical_password");
                prefs.putBoolean(PREF_REMEMBER_KEY, false);
            }
            UserSession.setUsername(username);
            UserSession.setEmail(email);
            SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
        } finally {
            loginInProgress = false;
        }
    }

    @FXML
    private void onRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        if (username == null || username.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            AlertUtils.showError("Errore", "Tutti i campi sono obbligatori per la registrazione.");
            return;
        }
        if (UserManager.userExists(username)) {
            AlertUtils.showError("Errore", "Nome utente già registrato. Scegli un altro nome o effettua il login.");
            return;
        }
        boolean ok = UserManager.registerUser(username, email, password);
        if (ok) {
            AlertUtils.showInfo("Registrazione riuscita", "Account creato con successo. Ora puoi accedere.");
            // Salva credenziali
            Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
            prefs.put(PREF_KEY, username);
            prefs.put(PREF_EMAIL_KEY, email);
            prefs.put("trenical_password", password);
        } else {
            AlertUtils.showError("Errore", "Registrazione fallita. Riprova.");
        }
    }

    /**
     * Pulisce le preferenze salvate per il "ricorda accesso".
     * Chiamato dal DashboardController durante il logout.
     */
    public static void clearSavedPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        prefs.remove(PREF_KEY);
        prefs.remove(PREF_EMAIL_KEY);
        prefs.remove("trenical_password");
        prefs.putBoolean(PREF_REMEMBER_KEY, false);
    }
}
