package it.unical.trenical.client.gui.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import it.unical.trenical.client.gui.SceneManager;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import it.unical.trenical.grpc.promotion.*;
import com.google.protobuf.Timestamp;
import it.unical.trenical.grpc.train.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PromotionsAdminController {
    @FXML private TableView<PromotionViewModel> promotionsTable;
    @FXML private TableColumn<PromotionViewModel, Integer> colId;
    @FXML private TableColumn<PromotionViewModel, String> colName;
    @FXML private TableColumn<PromotionViewModel, String> colDescription;
    @FXML private TableColumn<PromotionViewModel, Double> colDiscount;
    @FXML private TableColumn<PromotionViewModel, String> colRoute;
    @FXML private TableColumn<PromotionViewModel, String> colClass;
    @FXML private TableColumn<PromotionViewModel, String> colFrom;
    @FXML private TableColumn<PromotionViewModel, String> colTo;
    @FXML private TableColumn<PromotionViewModel, Boolean> colFidelity;
    @FXML private TableColumn<PromotionViewModel, String> colTrainType;

    private final ObservableList<PromotionViewModel> promotions = FXCollections.observableArrayList();
    private PromotionServiceGrpc.PromotionServiceBlockingStub promotionStub;

    public PromotionsAdminController() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        promotionStub = PromotionServiceGrpc.newBlockingStub(channel);
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        colDiscount.setCellValueFactory(data -> data.getValue().discountPercentProperty().asObject());
        colRoute.setCellValueFactory(data -> data.getValue().routeNameProperty());
        colClass.setCellValueFactory(data -> data.getValue().serviceClassProperty());
        colFrom.setCellValueFactory(data -> data.getValue().validFromProperty());
        colTo.setCellValueFactory(data -> data.getValue().validToProperty());
        colFidelity.setCellValueFactory(data -> data.getValue().fidelityOnlyProperty().asObject());
        colTrainType.setCellValueFactory(data -> data.getValue().trainTypeProperty());
        promotionsTable.setItems(promotions);
        // Listener doppio click per modifica rapida
        promotionsTable.setRowFactory(tv -> {
            TableRow<PromotionViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    promotionsTable.getSelectionModel().select(row.getIndex());
                    onEditPromotion();
                }
            });
            return row;
        });
        loadPromotions();
    }

    private void loadPromotions() {
        promotions.clear();
        try {
            PromotionList list = promotionStub.listPromotions(com.google.protobuf.Empty.getDefaultInstance());
            for (Promotion p : list.getPromotionsList()) {
                promotions.add(protoToViewModel(p));
            }
        } catch (Exception e) {
            showError("Errore nel caricamento promozioni: " + e.getMessage());
        }
    }

    @FXML
    private void onAddPromotion() {
        java.util.List<String> routes = getRoutesList();
        java.util.List<String> classes = getClassesList();
        java.util.List<String> trainTypes = getTrainTypesList();
        PromotionViewModel newPromo = PromotionDialog.showDialog(null, routes, classes, trainTypes);
        if (newPromo != null) {
            try {
                Promotion proto = viewModelToProto(newPromo);
                AddPromotionRequest req = AddPromotionRequest.newBuilder().setPromotion(proto).build();
                PromotionOperationResponse resp = promotionStub.addPromotion(req);
                if (resp.getSuccess()) {
                    loadPromotions();
                } else {
                    showError("Errore: " + resp.getMessage());
                }
            } catch (Exception e) {
                showError("Errore nell'aggiunta: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onRemovePromotion() {
        PromotionViewModel selected = promotionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                DeletePromotionRequest req = DeletePromotionRequest.newBuilder().setId(selected.idProperty().get()).build();
                PromotionOperationResponse resp = promotionStub.deletePromotion(req);
                if (resp.getSuccess()) {
                    loadPromotions();
                } else {
                    showError("Errore: " + resp.getMessage());
                }
            } catch (Exception e) {
                showError("Errore nella rimozione: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onRefresh() {
        loadPromotions();
    }

    @FXML
    private void onBack() {
        SceneManager.getInstance().switchTo(SceneManager.DASHBOARD);
    }

    @FXML
    private void onEditPromotion() {
        PromotionViewModel selected = promotionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            java.util.List<String> routes = getRoutesList();
            java.util.List<String> classes = getClassesList();
            java.util.List<String> trainTypes = getTrainTypesList();
            PromotionViewModel edited = PromotionDialog.showDialog(selected, routes, classes, trainTypes);
            if (edited != null) {
                try {
                    // Prima rimuovi la promozione esistente
                    DeletePromotionRequest delReq = DeletePromotionRequest.newBuilder().setId(selected.idProperty().get()).build();
                    PromotionOperationResponse delResp = promotionStub.deletePromotion(delReq);
                    if (!delResp.getSuccess()) {
                        showError("Errore nella rimozione: " + delResp.getMessage());
                        return;
                    }
                    // Poi aggiungi la promozione modificata
                    Promotion proto = viewModelToProto(edited);
                    AddPromotionRequest addReq = AddPromotionRequest.newBuilder().setPromotion(proto).build();
                    PromotionOperationResponse addResp = promotionStub.addPromotion(addReq);
                    if (addResp.getSuccess()) {
                        loadPromotions();
                    } else {
                        showError("Errore nell'aggiunta: " + addResp.getMessage());
                    }
                } catch (Exception e) {
                    showError("Errore nella modifica: " + e.getMessage());
                }
            }
        }
    }

    // --- Metodi per recuperare le liste reali ---
    private java.util.List<String> getRoutesList() {
        java.util.List<String> routes = new java.util.ArrayList<>();
        try {
            // Usa il servizio dei treni per recuperare le tratte
            TrainServiceGrpc.TrainServiceBlockingStub trainStub =
                TrainServiceGrpc.newBlockingStub(
                    ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build()
                );
            // Recupera tutte le tratte tramite gRPC
            ListRoutesRequest req = ListRoutesRequest.newBuilder().build();
            ListRoutesResponse resp = trainStub.listRoutes(req);
            for (it.unical.trenical.grpc.train.Route r : resp.getRoutesList()) {
                routes.add(r.getName());
            }
        } catch (Exception e) {
            routes.add("Nessuna tratta trovata");
        }
        return routes;
    }

    private List<String> getClassesList() {
        return Arrays.asList("Prima Classe", "Seconda Classe");
    }

    private List<String> getTrainTypesList() {
        // Puoi popolare dinamicamente se hai i tipi treno, qui statico
        return Arrays.asList("Frecciarossa", "Intercity", "Regionale", "Italo");
    }

    private String protocolStringListToString(List<String> list) {
        return (list != null && !list.isEmpty()) ? String.join(",", list) : "";
    }

    private List<String> stringToProtocolStringList(String value) {
        return (value != null && !value.isEmpty()) ? Arrays.asList(value.split(",")) : new ArrayList<>();
    }

    private PromotionViewModel protoToViewModel(Promotion p) {
        return new PromotionViewModel(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getDiscountPercent(),
                protocolStringListToString(p.getRouteNamesList()),
                protocolStringListToString(p.getServiceClassesList()),
                p.hasValidFrom() ? toLocalDate(p.getValidFrom()).toString() : "",
                p.hasValidTo() ? toLocalDate(p.getValidTo()).toString() : "",
                p.getOnlyForLoyaltyMembers(),
                p.getTrainType()
        );
    }

    private Promotion viewModelToProto(PromotionViewModel vm) {
        Promotion.Builder b = Promotion.newBuilder()
                .setId(vm.idProperty().get())
                .setName(vm.nameProperty().get())
                .setDescription(vm.descriptionProperty().get())
                .setDiscountPercent(vm.discountPercentProperty().get())
                .addAllRouteNames(stringToProtocolStringList(vm.routeNameProperty().get()))
                .addAllServiceClasses(stringToProtocolStringList(vm.serviceClassProperty().get()))
                .setOnlyForLoyaltyMembers(vm.fidelityOnlyProperty().get())
                .setTrainType(vm.trainTypeProperty().get() == null ? "" : vm.trainTypeProperty().get());
        if (!vm.validFromProperty().get().isEmpty()) b.setValidFrom(toProtoTimestamp(LocalDate.parse(vm.validFromProperty().get())));
        if (!vm.validToProperty().get().isEmpty()) b.setValidTo(toProtoTimestamp(LocalDate.parse(vm.validToProperty().get())));
        return b.build();
    }

    private LocalDate toLocalDate(Timestamp ts) {
        return java.time.Instant.ofEpochSecond(ts.getSeconds()).atZone(java.time.ZoneOffset.UTC).toLocalDate();
    }

    private Timestamp toProtoTimestamp(LocalDate date) {
        return Timestamp.newBuilder().setSeconds(date.atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond()).build();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    // ViewModel per la tabella
    public static class PromotionViewModel {
        private final javafx.beans.property.IntegerProperty id;
        private final javafx.beans.property.StringProperty name;
        private final javafx.beans.property.StringProperty description;
        private final javafx.beans.property.DoubleProperty discountPercent;
        private final javafx.beans.property.StringProperty routeName;
        private final javafx.beans.property.StringProperty serviceClass;
        private final javafx.beans.property.StringProperty validFrom;
        private final javafx.beans.property.StringProperty validTo;
        private final javafx.beans.property.BooleanProperty fidelityOnly;
        private final javafx.beans.property.StringProperty trainType;

        public PromotionViewModel(int id, String name, String description, double discountPercent, String routeName, String serviceClass, String validFrom, String validTo, boolean fidelityOnly, String trainType) {
            this.id = new javafx.beans.property.SimpleIntegerProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.description = new javafx.beans.property.SimpleStringProperty(description);
            this.discountPercent = new javafx.beans.property.SimpleDoubleProperty(discountPercent);
            this.routeName = new javafx.beans.property.SimpleStringProperty(routeName);
            this.serviceClass = new javafx.beans.property.SimpleStringProperty(serviceClass);
            this.validFrom = new javafx.beans.property.SimpleStringProperty(validFrom);
            this.validTo = new javafx.beans.property.SimpleStringProperty(validTo);
            this.fidelityOnly = new javafx.beans.property.SimpleBooleanProperty(fidelityOnly);
            this.trainType = new javafx.beans.property.SimpleStringProperty(trainType);
        }
        public IntegerProperty idProperty() { return id; }
        public StringProperty nameProperty() { return name; }
        public StringProperty descriptionProperty() { return description; }
        public DoubleProperty discountPercentProperty() { return discountPercent; }
        public StringProperty routeNameProperty() { return routeName; }
        public StringProperty serviceClassProperty() { return serviceClass; }
        public StringProperty validFromProperty() { return validFrom; }
        public StringProperty validToProperty() { return validTo; }
        public BooleanProperty fidelityOnlyProperty() { return fidelityOnly; }
        public StringProperty trainTypeProperty() { return trainType; }
    }
}
