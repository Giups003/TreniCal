package it.unical.trenical.server;

import com.google.protobuf.Timestamp;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import it.unical.trenical.grpc.promotion.*;
import java.time.LocalDate;
import java.util.List;

public class PromotionServiceImpl extends PromotionServiceGrpc.PromotionServiceImplBase {
    private final DataStore dataStore = DataStore.getInstance();

    @Override
    public void listPromotions(Empty request, StreamObserver<PromotionList> responseObserver) {
        List<Promotion> all = dataStore.getAllPromotions();
        PromotionList.Builder builder = PromotionList.newBuilder();
        for (Promotion p : all) {
            builder.addPromotions(p);
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void addPromotion(AddPromotionRequest request, StreamObserver<PromotionOperationResponse> responseObserver) {
        Promotion proto = request.getPromotion();
        // Se l'ID è nullo o duplicato, genera un nuovo ID
        List<Promotion> allPromos = dataStore.getAllPromotions();
        int id = proto.getId();
        final int currentId = id;
        if (id <= 0 || allPromos.stream().anyMatch(existing -> existing.getId() == currentId)) {
            id = dataStore.generateNextPromotionId();
            proto = proto.toBuilder().setId(id).build();
        }
        dataStore.addPromotion(proto);
        responseObserver.onNext(PromotionOperationResponse.newBuilder().setSuccess(true).setMessage("Promozione aggiunta").build());
        responseObserver.onCompleted();
    }

    @Override
    public void deletePromotion(DeletePromotionRequest request, StreamObserver<PromotionOperationResponse> responseObserver) {
        int id = request.getId();
        dataStore.deletePromotion(id);
        responseObserver.onNext(PromotionOperationResponse.newBuilder().setSuccess(true).setMessage("Promozione rimossa").build());
        responseObserver.onCompleted();
    }

    // Utility per promozioni: sconto e validazione
    private boolean isApplicable(Promotion promo, String routeName, String serviceClass, java.time.LocalDate travelDate, boolean isLoyaltyMember, String trainType) {
        boolean routeOk = (promo.getRouteNamesList().isEmpty() || promo.getRouteNamesList().contains(routeName));
        boolean classOk = (promo.getServiceClassesList().isEmpty() || promo.getServiceClassesList().contains(serviceClass));
        boolean fromOk = (!promo.hasValidFrom() || !travelDate.isBefore(java.time.Instant.ofEpochSecond(promo.getValidFrom().getSeconds()).atZone(java.time.ZoneOffset.UTC).toLocalDate()));
        boolean toOk = (!promo.hasValidTo() || !travelDate.isAfter(java.time.Instant.ofEpochSecond(promo.getValidTo().getSeconds()).atZone(java.time.ZoneOffset.UTC).toLocalDate()));
        boolean loyaltyOk = (!promo.getOnlyForLoyaltyMembers() || isLoyaltyMember);
        boolean typeOk = (promo.getTrainType().isEmpty() || promo.getTrainType().equalsIgnoreCase(trainType));
        return routeOk && classOk && fromOk && toOk && loyaltyOk && typeOk;
    }
    private double applyDiscount(Promotion promo, double basePrice) {
        return basePrice * (1.0 - (promo.getDiscountPercent() / 100.0));
    }
    private Promotion findBestPromotion(Iterable<Promotion> promotions, String routeName, String serviceClass, java.time.LocalDate travelDate, boolean isLoyaltyMember, String trainType) {
        Promotion best = null;
        for (Promotion p : promotions) {
            if (isApplicable(p, routeName, serviceClass, travelDate, isLoyaltyMember, trainType)) {
                if (best == null || p.getDiscountPercent() > best.getDiscountPercent()) {
                    best = p;
                }
            }
        }
        return best;
    }
}
