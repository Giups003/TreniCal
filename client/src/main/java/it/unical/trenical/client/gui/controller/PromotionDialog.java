package it.unical.trenical.client.gui.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import java.time.LocalDate;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.scene.control.cell.CheckBoxListCell;

/**
 * Classe di utilità per la creazione del dialogo di gestione promozioni.
 * Fornisce un'interfaccia per aggiungere e modificare promozioni con tutti i campi necessari.
 */
public class PromotionDialog {

    /**
     * Mostra il dialogo per creare o modificare una promozione.
     * @param existing Promozione esistente da modificare (null per nuova promozione)
     * @param routes Lista delle tratte disponibili
     * @param classes Lista delle classi di servizio
     * @param trainTypes Lista dei tipi di treno
     * @param userTypes Lista dei tipi di utente
     * @return PromotionViewModel con i dati inseriti/modificati, null se cancellato
     */
    public static PromotionsAdminController.PromotionViewModel showDialog(PromotionsAdminController.PromotionViewModel existing,
                                                                          java.util.List<String> routes,
                                                                          java.util.List<String> classes,
                                                                          java.util.List<String> trainTypes,
                                                                          java.util.List<String> userTypes) {
        // --- Configurazione dialogo ---
        Dialog<PromotionsAdminController.PromotionViewModel> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Aggiungi Promozione" : "Modifica Promozione");
        dialog.setHeaderText(existing == null ? "Inserisci i dati della nuova promozione" : "Modifica i dati della promozione");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // --- Layout principale ---
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // --- Campi base ---
        TextField nameField = new TextField();
        nameField.setPromptText("Nome");
        TextField descField = new TextField();
        descField.setPromptText("Descrizione");

        // Spinner per sconto (solo numeri 1-100, con frecce)
        Spinner<Integer> discountSpinner = new Spinner<>(1, 100, existing != null ? (int)existing.discountPercentProperty().get() : 1);
        discountSpinner.setEditable(true); // Permetti inserimento manuale

        // Limita l'input solo a numeri tra 1 e 100
        TextFormatter<Integer> discountFormatter = new TextFormatter<>(c -> {
            if (c.getControlNewText().matches("([1-9][0-9]?|100)?")) {
                return c;
            } else {
                return null;
            }
        });
        discountSpinner.getEditor().setTextFormatter(discountFormatter);
        // Aggiorna il valore dello spinner quando si digita manualmente
        discountFormatter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                discountSpinner.getValueFactory().setValue(newVal);
            }
        });

        // --- Liste di selezione ---
        ListView<String> routeListView = new ListView<>(FXCollections.observableArrayList(routes));
        routeListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        routeListView.setPrefHeight(100);

        // Bottone per selezionare tutte le tratte
        Button selectAllRoutesButton = new Button("Seleziona tutti i treni");
        selectAllRoutesButton.setOnAction(e -> {
            routeListView.getSelectionModel().clearSelection();
            for (int i = 0; i < routeListView.getItems().size(); i++) {
                routeListView.getSelectionModel().select(i);
            }
        });

        // AGGIUNTA OPZIONE "Tutte le classi" e "Tutte le tipologie"
        ListView<String> classListView = new ListView<>(FXCollections.observableArrayList(classes));
        classListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        classListView.setPrefHeight(80);

        // Bottone per selezionare tutte le classi
        Button selectAllClassesButton = new Button("Seleziona tutte le classi");
        selectAllClassesButton.setOnAction(e -> {
            classListView.getSelectionModel().clearSelection();
            for (int i = 0; i < classListView.getItems().size(); i++) {
                classListView.getSelectionModel().select(i);
            }
        });

        // --- DatePicker e CheckBox ---
        DatePicker fromPicker = new DatePicker();
        DatePicker toPicker = new DatePicker();
        CheckBox fidelityCheckBox = new CheckBox("Solo per membri FedeltàTreno");

        // --- ListView per i tipi treno (senza opzione "Tutte le tipologie") ---
        ListView<String> trainTypeListView = new ListView<>(FXCollections.observableArrayList(trainTypes));
        trainTypeListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        trainTypeListView.setPrefHeight(80);

        // Bottone per selezionare tutte le tipologie
        Button selectAllTrainTypesButton = new Button("Seleziona tutte le tipologie");
        selectAllTrainTypesButton.setOnAction(e -> {
            trainTypeListView.getSelectionModel().clearSelection();
            for (int i = 0; i < trainTypeListView.getItems().size(); i++) {
                trainTypeListView.getSelectionModel().select(i);
            }
        });

        // --- ListView per i tipi utente ---
        ListView<String> userTypeListView = new ListView<>(FXCollections.observableArrayList(userTypes));
        userTypeListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userTypeListView.setPrefHeight(80);

        // Bottone per selezionare tutti i tipi
        Button selectAllUserTypesButton = new Button("Seleziona tutti i tipi");
        selectAllUserTypesButton.setOnAction(e -> {
            userTypeListView.getSelectionModel().clearSelection();
            for (int i = 0; i < userTypeListView.getItems().size(); i++) {
                userTypeListView.getSelectionModel().select(i);
            }
        });

        // --- Valorizzazione campi esistenti ---
        if (existing != null) {
            nameField.setText(existing.nameProperty().get());
            descField.setText(existing.descriptionProperty().get());
            discountSpinner.getValueFactory().setValue((int)existing.discountPercentProperty().get());

            // Seleziona le tratte già associate
            if (existing != null && existing.routeNameProperty().get() != null && !existing.routeNameProperty().get().isEmpty()) {
                java.util.List<String> selectedRoutes = Arrays.asList(existing.routeNameProperty().get().split(","));
                for (int i = 0; i < routes.size(); i++) {
                    for (String sel : selectedRoutes) {
                        if (routes.get(i).trim().equalsIgnoreCase(sel.trim())) {
                            routeListView.getSelectionModel().select(i);
                        }
                    }
                }
                if (!selectedRoutes.isEmpty()) {
                    routeListView.scrollTo(routes.indexOf(selectedRoutes.get(0).trim()));
                }
            }

            // Seleziona le classi già associate
            if (existing.serviceClassProperty().get() != null && !existing.serviceClassProperty().get().isEmpty()) {
                java.util.List<String> selectedClasses = Arrays.asList(existing.serviceClassProperty().get().split(","));
                for (int i = 0; i < classes.size(); i++) {
                    if (selectedClasses.contains(classes.get(i))) {
                        classListView.getSelectionModel().select(i);
                    }
                }
                if (!selectedClasses.isEmpty()) {
                    classListView.scrollTo(classes.indexOf(selectedClasses.get(0)));
                }
            }

            // Seleziona i tipi treno già associati
            if (existing.trainTypeProperty().get() != null && !existing.trainTypeProperty().get().isEmpty()) {
                java.util.List<String> selectedTypes = Arrays.asList(existing.trainTypeProperty().get().split(","));
                for (int i = 0; i < trainTypes.size(); i++) {
                    if (selectedTypes.contains(trainTypes.get(i))) {
                        trainTypeListView.getSelectionModel().select(i);
                    }
                }
                if (!selectedTypes.isEmpty()) {
                    trainTypeListView.scrollTo(trainTypes.indexOf(selectedTypes.get(0)));
                }
            }

            // Seleziona i tipi utente già associati
            if (existing.userTypesProperty().get() != null && !existing.userTypesProperty().get().isEmpty()) {
                java.util.List<String> selectedUserTypes = Arrays.asList(existing.userTypesProperty().get().split(","));
                for (int i = 0; i < userTypes.size(); i++) {
                    if (selectedUserTypes.contains(userTypes.get(i))) {
                        userTypeListView.getSelectionModel().select(i);
                    }
                }
                if (!selectedUserTypes.isEmpty()) {
                    userTypeListView.scrollTo(userTypes.indexOf(selectedUserTypes.get(0)));
                }
            }

            if (!existing.validFromProperty().get().isEmpty()) fromPicker.setValue(LocalDate.parse(existing.validFromProperty().get()));
            if (!existing.validToProperty().get().isEmpty()) toPicker.setValue(LocalDate.parse(existing.validToProperty().get()));
            fidelityCheckBox.setSelected(existing.fidelityOnlyProperty().get());
        }

        // --- Aggiunta dei componenti al layout ---
        grid.add(new Label("Nome:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Descrizione:"), 0, 1); grid.add(descField, 1, 1);
        grid.add(new Label("Sconto (%):"), 0, 2); grid.add(discountSpinner, 1, 2);
        grid.add(new Label("Tratte:"), 0, 3); grid.add(routeListView, 1, 3);
        grid.add(selectAllRoutesButton, 2, 3); // aggiungi il bottone accanto alla lista
        grid.add(new Label("Classe:"), 0, 4); grid.add(classListView, 1, 4); grid.add(selectAllClassesButton, 2, 4);
        grid.add(new Label("Valida dal:"), 0, 5); grid.add(fromPicker, 1, 5);
        grid.add(new Label("Valida al:"), 0, 6); grid.add(toPicker, 1, 6);
        grid.add(fidelityCheckBox, 1, 7);
        grid.add(new Label("Tipo treno:"), 0, 8); grid.add(trainTypeListView, 1, 8); grid.add(selectAllTrainTypesButton, 2, 8);
        grid.add(new Label("Tipo utente:"), 0, 9); grid.add(userTypeListView, 1, 9); grid.add(selectAllUserTypesButton, 2, 9);

        // --- Gestione abilitazione pulsante OK ---
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        okButton.setDisable(nameField.getText().trim().isEmpty());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> okButton.setDisable(newVal.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);
        // Quando si preme OK, crea il ViewModel con la logica "tutte"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                // Raccogli i dati solo se servono per il ViewModel
                String name = nameField.getText();
                String desc = descField.getText();
                double discount = discountSpinner.getValue();
                java.util.List<String> selectedRoutes = new ArrayList<>(routeListView.getSelectionModel().getSelectedItems());
                java.util.List<String> selectedClasses = new ArrayList<>(classListView.getSelectionModel().getSelectedItems());
                java.util.List<String> selectedTypes = new ArrayList<>(trainTypeListView.getSelectionModel().getSelectedItems());
                java.util.List<String> selectedUserTypes = new ArrayList<>(userTypeListView.getSelectionModel().getSelectedItems());
                String trainType = "";
                if (!selectedTypes.isEmpty() && selectedTypes.contains("Tutte le tipologie")) {
                    trainType = "";
                } else if (!selectedTypes.isEmpty()) {
                    trainType = String.join(",", selectedTypes);
                }
                String from = fromPicker.getValue() != null ? fromPicker.getValue().toString() : "";
                String to = toPicker.getValue() != null ? toPicker.getValue().toString() : "";
                boolean fidelity = fidelityCheckBox.isSelected();
                String userTypesStr = String.join(",", selectedUserTypes);
                // Crea e restituisce il PromotionViewModel
                return new PromotionsAdminController.PromotionViewModel(
                        0,
                        name,
                        desc,
                        discount,
                        String.join(",", selectedRoutes),
                        String.join(",", selectedClasses),
                        from,
                        to,
                        fidelity,
                        trainType,
                        userTypesStr
                );
            }
            return null;
        });

        // --- Gestione selezione con CheckBox ---
        routeListView.setCellFactory(CheckBoxListCell.forListView(item ->
                new SimpleBooleanProperty(routeListView.getSelectionModel().getSelectedItems().contains(item)) {
            @Override
            public void set(boolean selected) {
                if (selected) {
                    routeListView.getSelectionModel().select(item);
                } else {
                    routeListView.getSelectionModel().clearSelection(routeListView.getItems().indexOf(item));
                }
            }
        }));
        classListView.setCellFactory(CheckBoxListCell.forListView(item ->
                new SimpleBooleanProperty(classListView.getSelectionModel().getSelectedItems().contains(item)) {
            @Override
            public void set(boolean selected) {
                if (selected) {
                    classListView.getSelectionModel().select(item);
                } else {
                    classListView.getSelectionModel().clearSelection(classListView.getItems().indexOf(item));
                }
            }
        }));
        trainTypeListView.setCellFactory(CheckBoxListCell.forListView(item ->
                new SimpleBooleanProperty(trainTypeListView.getSelectionModel().getSelectedItems().contains(item)) {
            @Override
            public void set(boolean selected) {
                if (selected) {
                    trainTypeListView.getSelectionModel().select(item);
                } else {
                    trainTypeListView.getSelectionModel().clearSelection(trainTypeListView.getItems().indexOf(item));
                }
            }
        }));
        userTypeListView.setCellFactory(CheckBoxListCell.forListView(item ->
                new SimpleBooleanProperty(userTypeListView.getSelectionModel().getSelectedItems().contains(item)) {
            @Override
            public void set(boolean selected) {
                if (selected) {
                    userTypeListView.getSelectionModel().select(item);
                } else {
                    userTypeListView.getSelectionModel().clearSelection(userTypeListView.getItems().indexOf(item));
                }
            }
        }));

        return dialog.showAndWait().orElse(null);
    }
}
