package it.unical.trenical.client.gui;

import it.unical.trenical.client.gui.controller.LoginController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * Gestisce il cambio centralizzato delle schermate principali dell'applicazione.
 * Singleton accessibile da tutti i controller.
 */
public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

    // Percorsi FXML delle schermate
    public static final String DASHBOARD = "src/main/java/it/unical/trenical/client/gui/view/dashboard.fxml";
    public static final String SEARCH_TRAINS = "src/main/java/it/unical/trenical/client/gui/view/search_trains.fxml";
    public static final String BUY_TICKET = "src/main/java/it/unical/trenical/client/gui/view/buy_ticket.fxml";
    public static final String MY_TICKETS = "src/main/java/it/unical/trenical/client/gui/view/my_tickets.fxml";
    public static final String LOGIN = "src/main/java/it/unical/trenical/client/gui/view/login.fxml";
    public static final String ADMIN_PANEL = "src/main/java/it/unical/trenical/client/gui/view/admin_panel.fxml";
    public static final String ADMIN_PROMOTIONS = "src/main/java/it/unical/trenical/client/gui/view/promotions_admin.fxml";
    public static final String MODIFY_TICKET_DIALOG = "src/main/java/it/unical/trenical/client/gui/view/modify_ticket_dialog.fxml";
    public static final String NOTIFICATIONS = "src/main/java/it/unical/trenical/client/gui/view/notifications.fxml";

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        // Se l'utente ha scelto "ricorda accesso" e c'è un username salvato, vai direttamente alla dashboard
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        String savedUsername = prefs.get("trenical_username", "");
        boolean rememberMe = prefs.getBoolean("trenical_remember_me", false);
        if (rememberMe && savedUsername != null && !savedUsername.isEmpty()) {
            it.unical.trenical.client.session.UserSession.setUsername(savedUsername);
            switchTo(DASHBOARD);
        } else {
            showLogin();
        }
    }

    /**
     * Carica e mostra una schermata FXML.
     *
     * @param fxmlPath percorso della risorsa FXML
     */
    public void switchTo(String fxmlPath) {
        if (primaryStage == null) {
            System.err.println("ERRORE: primaryStage non è stato inizializzato");
            return;
        }

        try {
            File file = new File(fxmlPath);

            if (!file.exists()) {
                System.err.println("ERRORE: Impossibile trovare il file FXML: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(file.toURI().toURL());
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            // Imposta dimensioni minime della finestra in base alla scena
            primaryStage.setMinWidth(scene.getRoot().minWidth(0));
            primaryStage.setMinHeight(scene.getRoot().minHeight(0));
            // Centra la finestra ogni volta che si cambia schermata
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERRORE durante il caricamento della schermata: " + e.getMessage());
        }
    }

    /**
     * Mostra la schermata della dashboard.
     */
    public void showDashboard() {
        switchTo(DASHBOARD);
    }
    /**
     * Mostra la schermata di acquisto biglietti.
     */
    public void showTicketPurchaseView() {
        switchTo(BUY_TICKET);
    }

    /**
     * Mostra la schermata di login.
     */
    public void showLogin() {
        switchTo(LOGIN);
    }

    /**
     * Mostra la schermata admin panel.
     */
    public void showAdminPanel() {
        switchTo(ADMIN_PANEL);
    }

    /**
     * Mostra la schermata admin promotions.
     */
    public void showPromotionsAdmin() {
        switchTo(ADMIN_PROMOTIONS);
    }

    /**
     * Mostra la dialog di modifica biglietto con i dati precompilati.
     */
    public void showModifyTicketDialog(String ticketId, String departure, String arrival, java.time.LocalDate date, String time, String serviceClass, int trainId, Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(new File(MODIFY_TICKET_DIALOG).toURI().toURL());
            Parent root = loader.load();
            it.unical.trenical.client.gui.controller.ModifyTicketDialogController controller = loader.getController();
            controller.setTicketId(ticketId);
            controller.setFields(departure, arrival, date, time, serviceClass, trainId);
            controller.setOnSuccess(onSuccess);
            Stage stage = new Stage();
            stage.setTitle("Modifica Biglietto");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERRORE durante l'apertura della dialog di modifica biglietto: " + e.getMessage());
        }
    }

    /**
     * Mostra la schermata di modifica biglietto (vecchio metodo, deprecato).
     */
    @Deprecated
    public void showModifyTicketDialog() {
        switchTo(MODIFY_TICKET_DIALOG);
    }

    /**
     * Mostra la schermata delle notifiche.
     */
    public void showNotifications() {
        switchTo(NOTIFICATIONS);
    }
}
