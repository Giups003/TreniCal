package it.unical.trenical.client.gui.controller;

import it.unical.trenical.client.gui.SceneManager;
import it.unical.trenical.client.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller della schermata dashboard.
 * Gestisce la navigazione verso le altre schermate tramite SceneManager.
 */
public class DashboardController {

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
        SceneManager.getInstance().switchTo(SceneManager.MY_TICKETS);
    }

    @FXML
    private void onLogout(ActionEvent event) {
        UserSession.setUsername(null);
        SceneManager.getInstance().showLogin();
    }
}

