package it.unical.trenical.grpc.notification;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Servizio per la gestione delle notifiche
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: notification_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class NotificationServiceGrpc {

  private NotificationServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "trenical.notification.NotificationService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.RegisterForTrainRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getRegisterForTrainUpdatesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RegisterForTrainUpdates",
      requestType = it.unical.trenical.grpc.notification.RegisterForTrainRequest.class,
      responseType = it.unical.trenical.grpc.notification.OperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.RegisterForTrainRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getRegisterForTrainUpdatesMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.RegisterForTrainRequest, it.unical.trenical.grpc.notification.OperationResponse> getRegisterForTrainUpdatesMethod;
    if ((getRegisterForTrainUpdatesMethod = NotificationServiceGrpc.getRegisterForTrainUpdatesMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getRegisterForTrainUpdatesMethod = NotificationServiceGrpc.getRegisterForTrainUpdatesMethod) == null) {
          NotificationServiceGrpc.getRegisterForTrainUpdatesMethod = getRegisterForTrainUpdatesMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.RegisterForTrainRequest, it.unical.trenical.grpc.notification.OperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RegisterForTrainUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.RegisterForTrainRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.OperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("RegisterForTrainUpdates"))
              .build();
        }
      }
    }
    return getRegisterForTrainUpdatesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.UnregisterRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getUnregisterFromTrainUpdatesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UnregisterFromTrainUpdates",
      requestType = it.unical.trenical.grpc.notification.UnregisterRequest.class,
      responseType = it.unical.trenical.grpc.notification.OperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.UnregisterRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getUnregisterFromTrainUpdatesMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.UnregisterRequest, it.unical.trenical.grpc.notification.OperationResponse> getUnregisterFromTrainUpdatesMethod;
    if ((getUnregisterFromTrainUpdatesMethod = NotificationServiceGrpc.getUnregisterFromTrainUpdatesMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getUnregisterFromTrainUpdatesMethod = NotificationServiceGrpc.getUnregisterFromTrainUpdatesMethod) == null) {
          NotificationServiceGrpc.getUnregisterFromTrainUpdatesMethod = getUnregisterFromTrainUpdatesMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.UnregisterRequest, it.unical.trenical.grpc.notification.OperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UnregisterFromTrainUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.UnregisterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.OperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("UnregisterFromTrainUpdates"))
              .build();
        }
      }
    }
    return getUnregisterFromTrainUpdatesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.GetNotificationsRequest,
      it.unical.trenical.grpc.notification.NotificationList> getGetNotificationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNotifications",
      requestType = it.unical.trenical.grpc.notification.GetNotificationsRequest.class,
      responseType = it.unical.trenical.grpc.notification.NotificationList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.GetNotificationsRequest,
      it.unical.trenical.grpc.notification.NotificationList> getGetNotificationsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.GetNotificationsRequest, it.unical.trenical.grpc.notification.NotificationList> getGetNotificationsMethod;
    if ((getGetNotificationsMethod = NotificationServiceGrpc.getGetNotificationsMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getGetNotificationsMethod = NotificationServiceGrpc.getGetNotificationsMethod) == null) {
          NotificationServiceGrpc.getGetNotificationsMethod = getGetNotificationsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.GetNotificationsRequest, it.unical.trenical.grpc.notification.NotificationList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNotifications"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.GetNotificationsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.NotificationList.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("GetNotifications"))
              .build();
        }
      }
    }
    return getGetNotificationsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.MarkNotificationRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getMarkNotificationAsReadMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MarkNotificationAsRead",
      requestType = it.unical.trenical.grpc.notification.MarkNotificationRequest.class,
      responseType = it.unical.trenical.grpc.notification.OperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.MarkNotificationRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getMarkNotificationAsReadMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.MarkNotificationRequest, it.unical.trenical.grpc.notification.OperationResponse> getMarkNotificationAsReadMethod;
    if ((getMarkNotificationAsReadMethod = NotificationServiceGrpc.getMarkNotificationAsReadMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getMarkNotificationAsReadMethod = NotificationServiceGrpc.getMarkNotificationAsReadMethod) == null) {
          NotificationServiceGrpc.getMarkNotificationAsReadMethod = getMarkNotificationAsReadMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.MarkNotificationRequest, it.unical.trenical.grpc.notification.OperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MarkNotificationAsRead"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.MarkNotificationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.OperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("MarkNotificationAsRead"))
              .build();
        }
      }
    }
    return getMarkNotificationAsReadMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.GetTrainStatusRequest,
      it.unical.trenical.grpc.notification.TrainStatusResponse> getGetTrainStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTrainStatus",
      requestType = it.unical.trenical.grpc.notification.GetTrainStatusRequest.class,
      responseType = it.unical.trenical.grpc.notification.TrainStatusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.GetTrainStatusRequest,
      it.unical.trenical.grpc.notification.TrainStatusResponse> getGetTrainStatusMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.GetTrainStatusRequest, it.unical.trenical.grpc.notification.TrainStatusResponse> getGetTrainStatusMethod;
    if ((getGetTrainStatusMethod = NotificationServiceGrpc.getGetTrainStatusMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getGetTrainStatusMethod = NotificationServiceGrpc.getGetTrainStatusMethod) == null) {
          NotificationServiceGrpc.getGetTrainStatusMethod = getGetTrainStatusMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.GetTrainStatusRequest, it.unical.trenical.grpc.notification.TrainStatusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTrainStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.GetTrainStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.TrainStatusResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("GetTrainStatus"))
              .build();
        }
      }
    }
    return getGetTrainStatusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.UpdateTrainStatusRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getUpdateTrainStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateTrainStatus",
      requestType = it.unical.trenical.grpc.notification.UpdateTrainStatusRequest.class,
      responseType = it.unical.trenical.grpc.notification.OperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.UpdateTrainStatusRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getUpdateTrainStatusMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.UpdateTrainStatusRequest, it.unical.trenical.grpc.notification.OperationResponse> getUpdateTrainStatusMethod;
    if ((getUpdateTrainStatusMethod = NotificationServiceGrpc.getUpdateTrainStatusMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getUpdateTrainStatusMethod = NotificationServiceGrpc.getUpdateTrainStatusMethod) == null) {
          NotificationServiceGrpc.getUpdateTrainStatusMethod = getUpdateTrainStatusMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.UpdateTrainStatusRequest, it.unical.trenical.grpc.notification.OperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateTrainStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.UpdateTrainStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.OperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("UpdateTrainStatus"))
              .build();
        }
      }
    }
    return getUpdateTrainStatusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.SendPromotionalRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getSendPromotionalNotificationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendPromotionalNotifications",
      requestType = it.unical.trenical.grpc.notification.SendPromotionalRequest.class,
      responseType = it.unical.trenical.grpc.notification.OperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.SendPromotionalRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getSendPromotionalNotificationsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.SendPromotionalRequest, it.unical.trenical.grpc.notification.OperationResponse> getSendPromotionalNotificationsMethod;
    if ((getSendPromotionalNotificationsMethod = NotificationServiceGrpc.getSendPromotionalNotificationsMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getSendPromotionalNotificationsMethod = NotificationServiceGrpc.getSendPromotionalNotificationsMethod) == null) {
          NotificationServiceGrpc.getSendPromotionalNotificationsMethod = getSendPromotionalNotificationsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.SendPromotionalRequest, it.unical.trenical.grpc.notification.OperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendPromotionalNotifications"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.SendPromotionalRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.OperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("SendPromotionalNotifications"))
              .build();
        }
      }
    }
    return getSendPromotionalNotificationsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.PromotionalPreferenceRequest,
      it.unical.trenical.grpc.notification.PromotionalPreferenceResponse> getGetPromotionalPreferenceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPromotionalPreference",
      requestType = it.unical.trenical.grpc.notification.PromotionalPreferenceRequest.class,
      responseType = it.unical.trenical.grpc.notification.PromotionalPreferenceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.PromotionalPreferenceRequest,
      it.unical.trenical.grpc.notification.PromotionalPreferenceResponse> getGetPromotionalPreferenceMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.PromotionalPreferenceRequest, it.unical.trenical.grpc.notification.PromotionalPreferenceResponse> getGetPromotionalPreferenceMethod;
    if ((getGetPromotionalPreferenceMethod = NotificationServiceGrpc.getGetPromotionalPreferenceMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getGetPromotionalPreferenceMethod = NotificationServiceGrpc.getGetPromotionalPreferenceMethod) == null) {
          NotificationServiceGrpc.getGetPromotionalPreferenceMethod = getGetPromotionalPreferenceMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.PromotionalPreferenceRequest, it.unical.trenical.grpc.notification.PromotionalPreferenceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPromotionalPreference"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.PromotionalPreferenceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.PromotionalPreferenceResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("GetPromotionalPreference"))
              .build();
        }
      }
    }
    return getGetPromotionalPreferenceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getSetPromotionalPreferenceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetPromotionalPreference",
      requestType = it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest.class,
      responseType = it.unical.trenical.grpc.notification.OperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest,
      it.unical.trenical.grpc.notification.OperationResponse> getSetPromotionalPreferenceMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest, it.unical.trenical.grpc.notification.OperationResponse> getSetPromotionalPreferenceMethod;
    if ((getSetPromotionalPreferenceMethod = NotificationServiceGrpc.getSetPromotionalPreferenceMethod) == null) {
      synchronized (NotificationServiceGrpc.class) {
        if ((getSetPromotionalPreferenceMethod = NotificationServiceGrpc.getSetPromotionalPreferenceMethod) == null) {
          NotificationServiceGrpc.getSetPromotionalPreferenceMethod = getSetPromotionalPreferenceMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest, it.unical.trenical.grpc.notification.OperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetPromotionalPreference"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.notification.OperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NotificationServiceMethodDescriptorSupplier("SetPromotionalPreference"))
              .build();
        }
      }
    }
    return getSetPromotionalPreferenceMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NotificationServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationServiceStub>() {
        @java.lang.Override
        public NotificationServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationServiceStub(channel, callOptions);
        }
      };
    return NotificationServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NotificationServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationServiceBlockingStub>() {
        @java.lang.Override
        public NotificationServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationServiceBlockingStub(channel, callOptions);
        }
      };
    return NotificationServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NotificationServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NotificationServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NotificationServiceFutureStub>() {
        @java.lang.Override
        public NotificationServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NotificationServiceFutureStub(channel, callOptions);
        }
      };
    return NotificationServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Servizio per la gestione delle notifiche
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Registra un cliente per ricevere aggiornamenti in tempo reale su un treno specifico
     * </pre>
     */
    default void registerForTrainUpdates(it.unical.trenical.grpc.notification.RegisterForTrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRegisterForTrainUpdatesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Annulla la registrazione per gli aggiornamenti di un treno
     * </pre>
     */
    default void unregisterFromTrainUpdates(it.unical.trenical.grpc.notification.UnregisterRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUnregisterFromTrainUpdatesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottiene tutte le notifiche per un utente
     * </pre>
     */
    default void getNotifications(it.unical.trenical.grpc.notification.GetNotificationsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.NotificationList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetNotificationsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Segna una notifica come letta
     * </pre>
     */
    default void markNotificationAsRead(it.unical.trenical.grpc.notification.MarkNotificationRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMarkNotificationAsReadMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottiene lo stato attuale di un treno
     * </pre>
     */
    default void getTrainStatus(it.unical.trenical.grpc.notification.GetTrainStatusRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.TrainStatusResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTrainStatusMethod(), responseObserver);
    }

    /**
     * <pre>
     * Aggiorna lo stato di un treno (solo per admin)
     * </pre>
     */
    default void updateTrainStatus(it.unical.trenical.grpc.notification.UpdateTrainStatusRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateTrainStatusMethod(), responseObserver);
    }

    /**
     * <pre>
     * Invia notifiche promozionali ai clienti "FedeltàTreno" (solo per admin)
     * </pre>
     */
    default void sendPromotionalNotifications(it.unical.trenical.grpc.notification.SendPromotionalRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendPromotionalNotificationsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottiene la preferenza di ricezione promozioni per un utente
     * </pre>
     */
    default void getPromotionalPreference(it.unical.trenical.grpc.notification.PromotionalPreferenceRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.PromotionalPreferenceResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPromotionalPreferenceMethod(), responseObserver);
    }

    /**
     * <pre>
     * Aggiorna la preferenza di ricezione promozioni per un utente
     * </pre>
     */
    default void setPromotionalPreference(it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetPromotionalPreferenceMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service NotificationService.
   * <pre>
   * Servizio per la gestione delle notifiche
   * </pre>
   */
  public static abstract class NotificationServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return NotificationServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service NotificationService.
   * <pre>
   * Servizio per la gestione delle notifiche
   * </pre>
   */
  public static final class NotificationServiceStub
      extends io.grpc.stub.AbstractAsyncStub<NotificationServiceStub> {
    private NotificationServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NotificationServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Registra un cliente per ricevere aggiornamenti in tempo reale su un treno specifico
     * </pre>
     */
    public void registerForTrainUpdates(it.unical.trenical.grpc.notification.RegisterForTrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRegisterForTrainUpdatesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Annulla la registrazione per gli aggiornamenti di un treno
     * </pre>
     */
    public void unregisterFromTrainUpdates(it.unical.trenical.grpc.notification.UnregisterRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUnregisterFromTrainUpdatesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottiene tutte le notifiche per un utente
     * </pre>
     */
    public void getNotifications(it.unical.trenical.grpc.notification.GetNotificationsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.NotificationList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetNotificationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Segna una notifica come letta
     * </pre>
     */
    public void markNotificationAsRead(it.unical.trenical.grpc.notification.MarkNotificationRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMarkNotificationAsReadMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottiene lo stato attuale di un treno
     * </pre>
     */
    public void getTrainStatus(it.unical.trenical.grpc.notification.GetTrainStatusRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.TrainStatusResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTrainStatusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Aggiorna lo stato di un treno (solo per admin)
     * </pre>
     */
    public void updateTrainStatus(it.unical.trenical.grpc.notification.UpdateTrainStatusRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateTrainStatusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Invia notifiche promozionali ai clienti "FedeltàTreno" (solo per admin)
     * </pre>
     */
    public void sendPromotionalNotifications(it.unical.trenical.grpc.notification.SendPromotionalRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendPromotionalNotificationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottiene la preferenza di ricezione promozioni per un utente
     * </pre>
     */
    public void getPromotionalPreference(it.unical.trenical.grpc.notification.PromotionalPreferenceRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.PromotionalPreferenceResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPromotionalPreferenceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Aggiorna la preferenza di ricezione promozioni per un utente
     * </pre>
     */
    public void setPromotionalPreference(it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetPromotionalPreferenceMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service NotificationService.
   * <pre>
   * Servizio per la gestione delle notifiche
   * </pre>
   */
  public static final class NotificationServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<NotificationServiceBlockingStub> {
    private NotificationServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NotificationServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Registra un cliente per ricevere aggiornamenti in tempo reale su un treno specifico
     * </pre>
     */
    public it.unical.trenical.grpc.notification.OperationResponse registerForTrainUpdates(it.unical.trenical.grpc.notification.RegisterForTrainRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRegisterForTrainUpdatesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Annulla la registrazione per gli aggiornamenti di un treno
     * </pre>
     */
    public it.unical.trenical.grpc.notification.OperationResponse unregisterFromTrainUpdates(it.unical.trenical.grpc.notification.UnregisterRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUnregisterFromTrainUpdatesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottiene tutte le notifiche per un utente
     * </pre>
     */
    public it.unical.trenical.grpc.notification.NotificationList getNotifications(it.unical.trenical.grpc.notification.GetNotificationsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetNotificationsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Segna una notifica come letta
     * </pre>
     */
    public it.unical.trenical.grpc.notification.OperationResponse markNotificationAsRead(it.unical.trenical.grpc.notification.MarkNotificationRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMarkNotificationAsReadMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottiene lo stato attuale di un treno
     * </pre>
     */
    public it.unical.trenical.grpc.notification.TrainStatusResponse getTrainStatus(it.unical.trenical.grpc.notification.GetTrainStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTrainStatusMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Aggiorna lo stato di un treno (solo per admin)
     * </pre>
     */
    public it.unical.trenical.grpc.notification.OperationResponse updateTrainStatus(it.unical.trenical.grpc.notification.UpdateTrainStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateTrainStatusMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Invia notifiche promozionali ai clienti "FedeltàTreno" (solo per admin)
     * </pre>
     */
    public it.unical.trenical.grpc.notification.OperationResponse sendPromotionalNotifications(it.unical.trenical.grpc.notification.SendPromotionalRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendPromotionalNotificationsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottiene la preferenza di ricezione promozioni per un utente
     * </pre>
     */
    public it.unical.trenical.grpc.notification.PromotionalPreferenceResponse getPromotionalPreference(it.unical.trenical.grpc.notification.PromotionalPreferenceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPromotionalPreferenceMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Aggiorna la preferenza di ricezione promozioni per un utente
     * </pre>
     */
    public it.unical.trenical.grpc.notification.OperationResponse setPromotionalPreference(it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetPromotionalPreferenceMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service NotificationService.
   * <pre>
   * Servizio per la gestione delle notifiche
   * </pre>
   */
  public static final class NotificationServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<NotificationServiceFutureStub> {
    private NotificationServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NotificationServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NotificationServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Registra un cliente per ricevere aggiornamenti in tempo reale su un treno specifico
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.OperationResponse> registerForTrainUpdates(
        it.unical.trenical.grpc.notification.RegisterForTrainRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRegisterForTrainUpdatesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Annulla la registrazione per gli aggiornamenti di un treno
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.OperationResponse> unregisterFromTrainUpdates(
        it.unical.trenical.grpc.notification.UnregisterRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUnregisterFromTrainUpdatesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottiene tutte le notifiche per un utente
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.NotificationList> getNotifications(
        it.unical.trenical.grpc.notification.GetNotificationsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetNotificationsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Segna una notifica come letta
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.OperationResponse> markNotificationAsRead(
        it.unical.trenical.grpc.notification.MarkNotificationRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMarkNotificationAsReadMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottiene lo stato attuale di un treno
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.TrainStatusResponse> getTrainStatus(
        it.unical.trenical.grpc.notification.GetTrainStatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTrainStatusMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Aggiorna lo stato di un treno (solo per admin)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.OperationResponse> updateTrainStatus(
        it.unical.trenical.grpc.notification.UpdateTrainStatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateTrainStatusMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Invia notifiche promozionali ai clienti "FedeltàTreno" (solo per admin)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.OperationResponse> sendPromotionalNotifications(
        it.unical.trenical.grpc.notification.SendPromotionalRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendPromotionalNotificationsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottiene la preferenza di ricezione promozioni per un utente
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.PromotionalPreferenceResponse> getPromotionalPreference(
        it.unical.trenical.grpc.notification.PromotionalPreferenceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPromotionalPreferenceMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Aggiorna la preferenza di ricezione promozioni per un utente
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.notification.OperationResponse> setPromotionalPreference(
        it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetPromotionalPreferenceMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REGISTER_FOR_TRAIN_UPDATES = 0;
  private static final int METHODID_UNREGISTER_FROM_TRAIN_UPDATES = 1;
  private static final int METHODID_GET_NOTIFICATIONS = 2;
  private static final int METHODID_MARK_NOTIFICATION_AS_READ = 3;
  private static final int METHODID_GET_TRAIN_STATUS = 4;
  private static final int METHODID_UPDATE_TRAIN_STATUS = 5;
  private static final int METHODID_SEND_PROMOTIONAL_NOTIFICATIONS = 6;
  private static final int METHODID_GET_PROMOTIONAL_PREFERENCE = 7;
  private static final int METHODID_SET_PROMOTIONAL_PREFERENCE = 8;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REGISTER_FOR_TRAIN_UPDATES:
          serviceImpl.registerForTrainUpdates((it.unical.trenical.grpc.notification.RegisterForTrainRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse>) responseObserver);
          break;
        case METHODID_UNREGISTER_FROM_TRAIN_UPDATES:
          serviceImpl.unregisterFromTrainUpdates((it.unical.trenical.grpc.notification.UnregisterRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse>) responseObserver);
          break;
        case METHODID_GET_NOTIFICATIONS:
          serviceImpl.getNotifications((it.unical.trenical.grpc.notification.GetNotificationsRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.NotificationList>) responseObserver);
          break;
        case METHODID_MARK_NOTIFICATION_AS_READ:
          serviceImpl.markNotificationAsRead((it.unical.trenical.grpc.notification.MarkNotificationRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse>) responseObserver);
          break;
        case METHODID_GET_TRAIN_STATUS:
          serviceImpl.getTrainStatus((it.unical.trenical.grpc.notification.GetTrainStatusRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.TrainStatusResponse>) responseObserver);
          break;
        case METHODID_UPDATE_TRAIN_STATUS:
          serviceImpl.updateTrainStatus((it.unical.trenical.grpc.notification.UpdateTrainStatusRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse>) responseObserver);
          break;
        case METHODID_SEND_PROMOTIONAL_NOTIFICATIONS:
          serviceImpl.sendPromotionalNotifications((it.unical.trenical.grpc.notification.SendPromotionalRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse>) responseObserver);
          break;
        case METHODID_GET_PROMOTIONAL_PREFERENCE:
          serviceImpl.getPromotionalPreference((it.unical.trenical.grpc.notification.PromotionalPreferenceRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.PromotionalPreferenceResponse>) responseObserver);
          break;
        case METHODID_SET_PROMOTIONAL_PREFERENCE:
          serviceImpl.setPromotionalPreference((it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.notification.OperationResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRegisterForTrainUpdatesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.RegisterForTrainRequest,
              it.unical.trenical.grpc.notification.OperationResponse>(
                service, METHODID_REGISTER_FOR_TRAIN_UPDATES)))
        .addMethod(
          getUnregisterFromTrainUpdatesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.UnregisterRequest,
              it.unical.trenical.grpc.notification.OperationResponse>(
                service, METHODID_UNREGISTER_FROM_TRAIN_UPDATES)))
        .addMethod(
          getGetNotificationsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.GetNotificationsRequest,
              it.unical.trenical.grpc.notification.NotificationList>(
                service, METHODID_GET_NOTIFICATIONS)))
        .addMethod(
          getMarkNotificationAsReadMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.MarkNotificationRequest,
              it.unical.trenical.grpc.notification.OperationResponse>(
                service, METHODID_MARK_NOTIFICATION_AS_READ)))
        .addMethod(
          getGetTrainStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.GetTrainStatusRequest,
              it.unical.trenical.grpc.notification.TrainStatusResponse>(
                service, METHODID_GET_TRAIN_STATUS)))
        .addMethod(
          getUpdateTrainStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.UpdateTrainStatusRequest,
              it.unical.trenical.grpc.notification.OperationResponse>(
                service, METHODID_UPDATE_TRAIN_STATUS)))
        .addMethod(
          getSendPromotionalNotificationsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.SendPromotionalRequest,
              it.unical.trenical.grpc.notification.OperationResponse>(
                service, METHODID_SEND_PROMOTIONAL_NOTIFICATIONS)))
        .addMethod(
          getGetPromotionalPreferenceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.PromotionalPreferenceRequest,
              it.unical.trenical.grpc.notification.PromotionalPreferenceResponse>(
                service, METHODID_GET_PROMOTIONAL_PREFERENCE)))
        .addMethod(
          getSetPromotionalPreferenceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.notification.SetPromotionalPreferenceRequest,
              it.unical.trenical.grpc.notification.OperationResponse>(
                service, METHODID_SET_PROMOTIONAL_PREFERENCE)))
        .build();
  }

  private static abstract class NotificationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NotificationServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return it.unical.trenical.grpc.notification.NotificationServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NotificationService");
    }
  }

  private static final class NotificationServiceFileDescriptorSupplier
      extends NotificationServiceBaseDescriptorSupplier {
    NotificationServiceFileDescriptorSupplier() {}
  }

  private static final class NotificationServiceMethodDescriptorSupplier
      extends NotificationServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    NotificationServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (NotificationServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NotificationServiceFileDescriptorSupplier())
              .addMethod(getRegisterForTrainUpdatesMethod())
              .addMethod(getUnregisterFromTrainUpdatesMethod())
              .addMethod(getGetNotificationsMethod())
              .addMethod(getMarkNotificationAsReadMethod())
              .addMethod(getGetTrainStatusMethod())
              .addMethod(getUpdateTrainStatusMethod())
              .addMethod(getSendPromotionalNotificationsMethod())
              .addMethod(getGetPromotionalPreferenceMethod())
              .addMethod(getSetPromotionalPreferenceMethod())
              .build();
        }
      }
    }
    return result;
  }
}
