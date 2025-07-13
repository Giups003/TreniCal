package it.unical.trenical.server;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.promotion.*;
import java.util.List;

/**
 * Servizio gRPC per la gestione delle promozioni del sistema TreniCal.
 * Gestisce operazioni CRUD sulle promozioni e validazione per l'applicazione degli sconti.
 */
public class PromotionServiceImpl extends PromotionServiceGrpc.PromotionServiceImplBase {

    /** Istanza singleton del DataStore per accesso ai dati */
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * Restituisce la lista completa di tutte le promozioni disponibili.
     *
     * @param request richiesta vuota (Empty)
     * @param responseObserver observer per inviare la lista delle promozioni
     */
    @Override
    public void listPromotions(Empty request, StreamObserver<PromotionList> responseObserver) {
        List<Promotion> allPromotions = dataStore.getAllPromotions();

        PromotionList.Builder builder = PromotionList.newBuilder();
        for (Promotion promotion : allPromotions) {
            builder.addPromotions(promotion);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * Aggiunge una nuova promozione al sistema.
     * Se l'ID è nullo o già esistente, viene generato automaticamente un nuovo ID univoco.
     *
     * @param request richiesta contenente i dati della promozione da aggiungere
     * @param responseObserver observer per inviare la risposta dell'operazione
     */
    @Override
    public void addPromotion(AddPromotionRequest request, StreamObserver<PromotionOperationResponse> responseObserver) {
        Promotion promotion = request.getPromotion();

        // Validazione e generazione ID univoco se necessario
        List<Promotion> existingPromotions = dataStore.getAllPromotions();
        int promotionId = promotion.getId();
        final int currentId = promotionId;

        if (promotionId <= 0 || existingPromotions.stream().anyMatch(existing -> existing.getId() == currentId)) {
            promotionId = dataStore.generateNextPromotionId();
            promotion = promotion.toBuilder().setId(promotionId).build();
        }

        // Salvataggio della promozione
        dataStore.addPromotion(promotion);

        responseObserver.onNext(
            PromotionOperationResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Promozione aggiunta con successo")
                .build()
        );
        responseObserver.onCompleted();
    }

    /**
     * Rimuove una promozione dal sistema dato il suo ID.
     *
     * @param request richiesta contenente l'ID della promozione da eliminare
     * @param responseObserver observer per inviare la risposta dell'operazione
     */
    @Override
    public void deletePromotion(DeletePromotionRequest request, StreamObserver<PromotionOperationResponse> responseObserver) {
        int promotionId = request.getId();

        dataStore.deletePromotion(promotionId);

        responseObserver.onNext(
            PromotionOperationResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Promozione rimossa con successo")
                .build()
        );
        responseObserver.onCompleted();
    }
}