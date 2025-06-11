package it.unical.trenical.client.gui.util;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;

import java.util.List;
import java.util.function.Function;

public class AutoCompleteUtil {

    public static void setupAutoComplete(TextField field, Function<String, List<String>> fetchSuggestions) {
        ListView<String> suggestionsList = new ListView<>();
        suggestionsList.setPrefHeight(180);
        suggestionsList.setMaxHeight(220);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                popup.hide();
                Platform.runLater(() -> {
                    popup.getContent().clear();
                    suggestionsList.getItems().clear();
                });
                return;
            }

            List<String> suggestions;
            try {
                suggestions = fetchSuggestions.apply(newVal);
            } catch (Exception e) {
                popup.hide();
                Platform.runLater(() -> {
                    popup.getContent().clear();
                    suggestionsList.getItems().clear();
                });
                return;
            }

            if (suggestions == null || suggestions.isEmpty()) {
                popup.hide();
                Platform.runLater(() -> {
                    popup.getContent().clear();
                    suggestionsList.getItems().clear();
                });
                return;
            }

            suggestionsList.getItems().setAll(suggestions);

            Platform.runLater(() -> {
                Bounds bounds = field.localToScreen(field.getBoundsInLocal());
                if (bounds != null) {
                    if (!popup.getContent().contains(suggestionsList)) {
                        popup.getContent().clear();
                        popup.getContent().add(suggestionsList);
                    }
                    popup.show(field, bounds.getMinX(), bounds.getMaxY());
                }
            });
        });

        setupEventHandlers(field, popup, suggestionsList);
    }

    private static void safeAttachWindowListeners(javafx.scene.Scene scene, Popup popup) {
        Runnable attach = new Runnable() {
            @Override
            public void run() {
                if (scene.getWindow() != null) {
                    scene.getWindow().addEventFilter(javafx.stage.WindowEvent.WINDOW_HIDDEN, e -> {
                        popup.hide();
                        Platform.runLater(() -> popup.getContent().clear());
                    });
                    scene.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
                        popup.hide();
                        Platform.runLater(() -> popup.getContent().clear());
                    });
                    scene.widthProperty().addListener((o, ov, nv) -> {
                        popup.hide();
                        Platform.runLater(() -> popup.getContent().clear());
                    });
                    scene.heightProperty().addListener((o, ov, nv) -> {
                        popup.hide();
                        Platform.runLater(() -> popup.getContent().clear());
                    });
                } else {
                    Platform.runLater(this);
                }
            }
        };
        Platform.runLater(attach);
    }

    private static void setupEventHandlers(TextField field, Popup popup, ListView<String> suggestionsList) {
        field.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null)
                safeAttachWindowListeners(newScene, popup);
        });

        suggestionsList.setOnMouseClicked(e -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                field.setText(selected);
                popup.hide();
                Platform.runLater(() -> {
                    popup.getContent().clear();
                    suggestionsList.getItems().clear();
                });
                field.fireEvent(new javafx.event.ActionEvent());
                field.getParent().requestFocus();
            }
        });

        suggestionsList.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> {
                    popup.hide();
                    Platform.runLater(() -> {
                        popup.getContent().clear();
                        suggestionsList.getItems().clear();
                    });
                }
                case UP -> {
                    if (suggestionsList.getSelectionModel().getSelectedIndex() == 0) {
                        field.requestFocus();
                    }
                }
                case ENTER -> {
                    String selected = suggestionsList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        field.setText(selected);
                        popup.hide();
                        Platform.runLater(() -> {
                            popup.getContent().clear();
                            suggestionsList.getItems().clear();
                        });
                        field.fireEvent(new javafx.event.ActionEvent());
                        field.getParent().requestFocus();
                    }
                }
            }
        });

        field.setOnKeyPressed(e -> {
            if (popup.isShowing()) {
                switch (e.getCode()) {
                    case DOWN -> suggestionsList.requestFocus();
                    case ESCAPE, ENTER -> {
                        popup.hide();
                        Platform.runLater(() -> {
                            popup.getContent().clear();
                            suggestionsList.getItems().clear();
                        });
                    }
                }
            }
        });

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && popup.isShowing()) {
                Platform.runLater(() -> {
                    if (!suggestionsList.isFocused()) {
                        popup.hide();
                        popup.getContent().clear();
                        suggestionsList.getItems().clear();
                    }
                });
            }
        });
    }
}