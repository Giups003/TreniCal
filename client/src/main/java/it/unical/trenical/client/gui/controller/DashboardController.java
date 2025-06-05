package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller della schermata dashboard.
 * Gestisce la navigazione verso le altre schermate tramite SceneManager.
 */
public class DashboardController {

    @FXML
    private Button adminPromotionsButton;
    @FXML
    private Label adminLabel;

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
    private void onLogout(ActionEvent event) {
        UserSession.setUsername(null);
        UserSession.setAdmin(false);
        SceneManager.getInstance().showLogin();
    }

    @FXML
    private void onAdminPromotions(ActionEvent event) {
        SceneManager.getInstance().switchTo("src/main/java/it/unical/trenical/client/gui/view/promotions_admin.fxml");
    }
}
