package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.notification.*;
import it.unical.trenical.grpc.common.Ticket;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementazione definitiva del servizio gRPC per la gestione delle notifiche treni.
 * Fornisce funzionalità per:
 * - Registrazione e annullamento notifiche su treni specifici
 * - Invio notifiche di stato, ritardo, cancellazione, cambio binario e promozioni
 * - Gestione notifiche lette/non lette
 * - Recupero notifiche per utente
 * Tutte le notifiche sono persistite in memoria (thread-safe).
 */
public class NotificationServiceImpl extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final DataStore dataStore;
    // Mappa username -> lista di ID treni a cui l'utente è registrato
    private final Map<String, Set<Integer>> userTrainSubscriptions = new ConcurrentHashMap<>();
    // Mappa ID treno -> lista di username registrati
    private final Map<Integer, Set<String>> trainSubscribers = new ConcurrentHashMap<>();
    // Lista di notifiche (thread-safe)
    private final List<Notification> notifications = Collections.synchronizedList(new ArrayList<>());
    // Lista di utenti "FedeltàTreno"
    private final Set<String> loyaltyUsers = ConcurrentHashMap.newKeySet();
    // Mappa username -> preferenza ricezione promozioni
    private final Map<String, Boolean> loyaltyPromotionsPreferences = new ConcurrentHashMap<>();

    /**
     * Costruttore: inizializza il servizio e alcuni utenti fedeltà di test.
     */
    public NotificationServiceImpl() {
        this.dataStore = DataStore.getInstance();
        loyaltyUsers.add("admin");
        loyaltyUsers.add("user");
        // Default: tutti gli utenti fedeltà ricevono promozioni
        loyaltyPromotionsPreferences.put("admin", true);
        loyaltyPromotionsPreferences.put("user", true);
    }

    /**
     * Registra un utente per ricevere aggiornamenti su un treno specifico.
     * Genera una notifica di conferma.
     */
    @Override
    public void registerForTrainUpdates(RegisterForTrainRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            String username = request.getUsername();
            int trainId = request.getTrainId();
            if (username == null || username.isEmpty()) {
                sendOperationResponse(false, "Username non valido", responseObserver);
                return;
            }
            if (trainId <= 0) {
                sendOperationResponse(false, "ID treno non valido", responseObserver);
                return;
            }
            userTrainSubscriptions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(trainId);
            trainSubscribers.computeIfAbsent(trainId, k -> ConcurrentHashMap.newKeySet()).add(username);
            createNotification(
                    username,
                    "Registrazione completata",
                    "Riceverai aggiornamenti in tempo reale per il treno " + trainId,
                    NotificationType.GENERAL,
                    trainId,
                    null
            );
            sendOperationResponse(true, "Registrazione completata con successo", responseObserver);
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Errore interno: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * Annulla la registrazione di un utente per gli aggiornamenti di un treno.
     */
    @Override
    public void unregisterFromTrainUpdates(UnregisterRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            String username = request.getUsername();
            int trainId = request.getTrainId();
            if (username == null || username.isEmpty()) {
                sendOperationResponse(false, "Username non valido", responseObserver);
                return;
            }
            if (trainId <= 0) {
                sendOperationResponse(false, "ID treno non valido", responseObserver);
                return;
            }
            Set<Integer> userTrains = userTrainSubscriptions.get(username);
            if (userTrains != null) {
                userTrains.remove(trainId);
            }
            Set<String> subscribers = trainSubscribers.get(trainId);
            if (subscribers != null) {
                subscribers.remove(username);
            }
            sendOperationResponse(true, "Registrazione annullata con successo", responseObserver);
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Errore interno: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * Restituisce tutte le notifiche per un utente, opzionalmente includendo quelle già lette.
     */
    @Override
    public void getNotifications(GetNotificationsRequest request, StreamObserver<NotificationList> responseObserver) {
        try {
            String username = request.getUsername();
            boolean includeRead = request.getIncludeRead();
            if (username == null || username.isEmpty()) {
                responseObserver.onNext(NotificationList.newBuilder().build());
                responseObserver.onCompleted();
                return;
            }
            List<Notification> userNotifications = notifications.stream()
                    .filter(n -> n.getUsername().equals(username))
                    .filter(n -> includeRead || !n.getRead())
                    .collect(Collectors.toList());
            NotificationList response = NotificationList.newBuilder()
                    .addAllNotifications(userNotifications)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Errore interno: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * Segna una notifica come letta dato il suo ID.
     */
    @Override
    public void markNotificationAsRead(MarkNotificationRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            String notificationId = request.getNotificationId();
            if (notificationId == null || notificationId.isEmpty()) {
                sendOperationResponse(false, "ID notifica non valido", responseObserver);
                return;
            }
            boolean found = false;
            synchronized (notifications) {
                Iterator<Notification> it = notifications.iterator();
                while (it.hasNext()) {
                    Notification notification = it.next();
                    if (notification.getId().equals(notificationId)) {
                        Notification updated = notification.toBuilder().setRead(true).build();
                        it.remove();
                        notifications.add(updated);
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                sendOperationResponse(true, "Notifica segnata come letta", responseObserver);
            } else {
                sendOperationResponse(false, "Notifica non trovata", responseObserver);
            }
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Errore interno: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * Restituisce lo stato attuale di un treno per una data specifica.
     */
    @Override
    public void getTrainStatus(GetTrainStatusRequest request, StreamObserver<TrainStatusResponse> responseObserver) {
        try {
            int trainId = request.getTrainId();
            Timestamp dateTs = request.getDate();
            java.time.LocalDateTime travelDateTime = null;
            if (dateTs != null && dateTs.getSeconds() != 0) {
                java.time.Instant instant = java.time.Instant.ofEpochSecond(dateTs.getSeconds());
                travelDateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
            }
            it.unical.trenical.grpc.common.Train train = null;
            if (trainId > 0 && travelDateTime != null) {
                train = dataStore.getTrainById(trainId, travelDateTime);
            }
            if (train == null) {
                responseObserver.onError(
                        Status.NOT_FOUND.withDescription("Treno non trovato per la data/orario richiesti").asRuntimeException()
                );
                return;
            }
            TrainStatusResponse response = TrainStatusResponse.newBuilder()
                    .setTrainId(trainId)
                    .setTrainName(train.getName())
                    .setStatus(TrainStatus.ON_TIME)
                    .setPlatform(1)
                    .setDelayMinutes(0)
                    .setMessage("Il treno è in orario")
                    .setLastUpdate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Errore interno: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * Aggiorna lo stato di un treno e invia notifiche agli utenti iscritti.
     * Elimina i messaggi che non contengono una descrizione della tipologia stato treno.
     * Genera notifiche solo se il messaggio è significativo.
     */
    @Override
    public void updateTrainStatus(UpdateTrainStatusRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            int trainId = request.getTrainId();
            TrainStatus status = request.getStatus();
            String message = request.getMessage();
            int platformChange = request.getPlatformChange();
            int delayMinutes = request.getDelayMinutes();
            Timestamp date = request.getDate();

            Set<String> subscribers = trainSubscribers.getOrDefault(trainId, Collections.emptySet());
            NotificationType type = NotificationType.TRAIN_STATUS;
            String title = "Aggiornamento treno " + trainId;
            String notifMsg = null;
            // Determina il tipo e il messaggio della notifica
            if (status == TrainStatus.DELAYED) {
                type = NotificationType.TRAIN_DELAY;
                notifMsg = (message != null && !message.isEmpty()) ? message : "Il treno è in ritardo di " + delayMinutes + " minuti.";
            } else if (status == TrainStatus.CANCELLED) {
                type = NotificationType.TRAIN_CANCELLATION;
                notifMsg = (message != null && !message.isEmpty()) ? message : "Il treno è stato cancellato.";
            } else if (platformChange > 0) {
                type = NotificationType.PLATFORM_CHANGE;
                notifMsg = (message != null && !message.isEmpty()) ? message : "Il treno partirà dal binario " + platformChange + ".";
            } else if (status == TrainStatus.ARRIVED) {
                notifMsg = (message != null && !message.isEmpty()) ? message : "Il treno è arrivato a destinazione.";
            } else if (status == TrainStatus.DEPARTED) {
                notifMsg = (message != null && !message.isEmpty()) ? message : "Il treno è partito.";
            } else if (status == TrainStatus.ON_TIME) {
                notifMsg = (message != null && !message.isEmpty()) ? message : "Il treno è in orario.";
            }
            // Elimina notifiche senza descrizione significativa
            if (notifMsg == null || notifMsg.trim().isEmpty()) {
                sendOperationResponse(false, "Messaggio di stato treno non valido: nessuna descrizione.", responseObserver);
                return;
            }
            for (String username : subscribers) {
                createNotification(
                        username,
                        title,
                        notifMsg,
                        type,
                        trainId,
                        null
                );
            }
            sendOperationResponse(true, "Stato treno aggiornato e notifiche inviate", responseObserver);
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Errore interno: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * Avvia un task periodico che aggiorna randomicamente lo stato di alcuni treni (per test/demo).
     */
    public void startRandomTrainStatusUpdater() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            Random random = new Random();
            @Override
            public void run() {
                try {
                    List<Integer> trainIds = new ArrayList<>(trainSubscribers.keySet());
                    if (trainIds.isEmpty()) return;
                    int trainId = trainIds.get(random.nextInt(trainIds.size()));
                    TrainStatus[] statuses = {TrainStatus.ON_TIME, TrainStatus.DELAYED, TrainStatus.CANCELLED, TrainStatus.ARRIVED, TrainStatus.DEPARTED};
                    TrainStatus status = statuses[random.nextInt(statuses.length)];
                    int delay = (status == TrainStatus.DELAYED) ? (5 + random.nextInt(30)) : 0;
                    int platform = (random.nextBoolean()) ? (1 + random.nextInt(10)) : 0;
                    String msg = null;
                    if (status == TrainStatus.DELAYED) msg = "Ritardo di " + delay + " minuti.";
                    else if (status == TrainStatus.CANCELLED) msg = "Treno cancellato.";
                    else if (platform > 0) msg = "Cambio binario: " + platform;
                    UpdateTrainStatusRequest req = UpdateTrainStatusRequest.newBuilder()
                            .setTrainId(trainId)
                            .setStatus(status)
                            .setDelayMinutes(delay)
                            .setPlatformChange(platform)
                            .setMessage(msg == null ? "" : msg)
                            .setDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                            .build();
                    updateTrainStatus(req, new StreamObserver<OperationResponse>() {
                        @Override public void onNext(OperationResponse value) {}
                        @Override public void onError(Throwable t) {}
                        @Override public void onCompleted() {}
                    });
                } catch (Exception ignored) {}
            }
        }, 10000, 20000); // ogni 20 secondi
    }

    /**
     * Invia notifiche promozionali a tutti gli utenti "FedeltàTreno".
     */
    @Override
    public void sendPromotionalNotifications(SendPromotionalRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            String title = request.getTitle();
            String message = request.getMessage();
            String promoCode = request.getPromoCode();
            if (title == null || title.isEmpty() || message == null || message.isEmpty()) {
                sendOperationResponse(false, "Titolo e messaggio sono obbligatori", responseObserver);
                return;
            }
            int count = 0;
            for (String username : loyaltyUsers) {
                if (Boolean.TRUE.equals(loyaltyPromotionsPreferences.getOrDefault(username, true))) {
                    createNotification(username, title, message, NotificationType.PROMOTION, 0, promoCode);
                    count++;
                }
            }
            sendOperationResponse(true, "Notifiche promozionali inviate a " + count + " utenti fedeltà", responseObserver);
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Errore interno: " + e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void getPromotionalPreference(PromotionalPreferenceRequest request, StreamObserver<PromotionalPreferenceResponse> responseObserver) {
        String username = request.getUsername();
        boolean wantsPromotions = loyaltyPromotionsPreferences.getOrDefault(username, true);
        PromotionalPreferenceResponse response = PromotionalPreferenceResponse.newBuilder()
                .setWantsPromotions(wantsPromotions)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void setPromotionalPreference(SetPromotionalPreferenceRequest request, StreamObserver<OperationResponse> responseObserver) {
        String username = request.getUsername();
        boolean wantsPromotions = request.getWantsPromotions();
        if (!loyaltyUsers.contains(username)) {
            sendOperationResponse(false, "Utente non membro FedeltàTreno", responseObserver);
            return;
        }
        loyaltyPromotionsPreferences.put(username, wantsPromotions);
        sendOperationResponse(true, "Preferenza aggiornata", responseObserver);
    }

    /**
     * Crea e aggiunge una nuova notifica alla lista.
     * @param username destinatario
     * @param title titolo notifica
     * @param message messaggio notifica
     * @param type tipo notifica
     * @param trainId id treno (0 se non applicabile)
     * @param promoCode codice promozionale (opzionale)
     */
    private void createNotification(String username, String title, String message, NotificationType type, int trainId, String promoCode) {
        String id = UUID.randomUUID().toString();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build();
        Notification.Builder builder = Notification.newBuilder()
                .setId(id)
                .setUsername(username)
                .setTitle(title)
                .setMessage(message)
                .setTimestamp(timestamp)
                .setType(type)
                .setRead(false)
                .setTrainId(trainId);
        if (promoCode != null && !promoCode.isEmpty()) {
            builder.setPromoCode(promoCode);
        }
        Notification notification = builder.build();
        notifications.add(notification);
        // Log della notifica creata
        System.out.println("[NOTIFICA CREATA] Utente: " + username + ", Titolo: " + title + ", Messaggio: " + message + ", Tipo: " + type + ", Treno: " + trainId + ", Data: " + timestamp.getSeconds());
    }

    /**
     * Invia una risposta operazione generica.
     */
    private void sendOperationResponse(boolean success, String message, StreamObserver<OperationResponse> responseObserver) {
        OperationResponse response = OperationResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}