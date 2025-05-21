package it.unical.trenical.grpc.train;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Servizio per la gestione dei treni
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: train_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class TrainServiceGrpc {

  private TrainServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "trenical.train.TrainService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest,
      it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> getGetTrainsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTrains",
      requestType = it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest.class,
      responseType = it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest,
      it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> getGetTrainsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest, it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> getGetTrainsMethod;
    if ((getGetTrainsMethod = TrainServiceGrpc.getGetTrainsMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getGetTrainsMethod = TrainServiceGrpc.getGetTrainsMethod) == null) {
          TrainServiceGrpc.getGetTrainsMethod = getGetTrainsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest, it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTrains"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("GetTrains"))
              .build();
        }
      }
    }
    return getGetTrainsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest,
      it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> getSearchTrainsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchTrains",
      requestType = it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest.class,
      responseType = it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest,
      it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> getSearchTrainsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest, it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> getSearchTrainsMethod;
    if ((getSearchTrainsMethod = TrainServiceGrpc.getSearchTrainsMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getSearchTrainsMethod = TrainServiceGrpc.getSearchTrainsMethod) == null) {
          TrainServiceGrpc.getSearchTrainsMethod = getSearchTrainsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest, it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTrains"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("SearchTrains"))
              .build();
        }
      }
    }
    return getSearchTrainsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest,
      it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse> getGetTrainDetailsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTrainDetails",
      requestType = it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest.class,
      responseType = it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest,
      it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse> getGetTrainDetailsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest, it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse> getGetTrainDetailsMethod;
    if ((getGetTrainDetailsMethod = TrainServiceGrpc.getGetTrainDetailsMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getGetTrainDetailsMethod = TrainServiceGrpc.getGetTrainDetailsMethod) == null) {
          TrainServiceGrpc.getGetTrainDetailsMethod = getGetTrainDetailsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest, it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTrainDetails"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("GetTrainDetails"))
              .build();
        }
      }
    }
    return getGetTrainDetailsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest,
      it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse> getGetTrainScheduleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTrainSchedule",
      requestType = it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest.class,
      responseType = it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest,
      it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse> getGetTrainScheduleMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest, it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse> getGetTrainScheduleMethod;
    if ((getGetTrainScheduleMethod = TrainServiceGrpc.getGetTrainScheduleMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getGetTrainScheduleMethod = TrainServiceGrpc.getGetTrainScheduleMethod) == null) {
          TrainServiceGrpc.getGetTrainScheduleMethod = getGetTrainScheduleMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest, it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTrainSchedule"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("GetTrainSchedule"))
              .build();
        }
      }
    }
    return getGetTrainScheduleMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TrainServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TrainServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TrainServiceStub>() {
        @java.lang.Override
        public TrainServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TrainServiceStub(channel, callOptions);
        }
      };
    return TrainServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TrainServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TrainServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TrainServiceBlockingStub>() {
        @java.lang.Override
        public TrainServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TrainServiceBlockingStub(channel, callOptions);
        }
      };
    return TrainServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TrainServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TrainServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TrainServiceFutureStub>() {
        @java.lang.Override
        public TrainServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TrainServiceFutureStub(channel, callOptions);
        }
      };
    return TrainServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Servizio per la gestione dei treni
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Ottiene informazioni sui treni
     * </pre>
     */
    default void getTrains(it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTrainsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Cerca treni in base a criteri specifici
     * </pre>
     */
    default void searchTrains(it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTrainsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottiene dettagli di un treno specifico
     * </pre>
     */
    default void getTrainDetails(it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTrainDetailsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottiene gli orari dei treni per una specifica tratta
     * </pre>
     */
    default void getTrainSchedule(it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTrainScheduleMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service TrainService.
   * <pre>
   * Servizio per la gestione dei treni
   * </pre>
   */
  public static abstract class TrainServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return TrainServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service TrainService.
   * <pre>
   * Servizio per la gestione dei treni
   * </pre>
   */
  public static final class TrainServiceStub
      extends io.grpc.stub.AbstractAsyncStub<TrainServiceStub> {
    private TrainServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TrainServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TrainServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Ottiene informazioni sui treni
     * </pre>
     */
    public void getTrains(it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTrainsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Cerca treni in base a criteri specifici
     * </pre>
     */
    public void searchTrains(it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTrainsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottiene dettagli di un treno specifico
     * </pre>
     */
    public void getTrainDetails(it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTrainDetailsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottiene gli orari dei treni per una specifica tratta
     * </pre>
     */
    public void getTrainSchedule(it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTrainScheduleMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service TrainService.
   * <pre>
   * Servizio per la gestione dei treni
   * </pre>
   */
  public static final class TrainServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<TrainServiceBlockingStub> {
    private TrainServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TrainServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TrainServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Ottiene informazioni sui treni
     * </pre>
     */
    public it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse getTrains(it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTrainsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Cerca treni in base a criteri specifici
     * </pre>
     */
    public it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse searchTrains(it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTrainsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottiene dettagli di un treno specifico
     * </pre>
     */
    public it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse getTrainDetails(it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTrainDetailsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottiene gli orari dei treni per una specifica tratta
     * </pre>
     */
    public it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse getTrainSchedule(it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTrainScheduleMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service TrainService.
   * <pre>
   * Servizio per la gestione dei treni
   * </pre>
   */
  public static final class TrainServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<TrainServiceFutureStub> {
    private TrainServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TrainServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TrainServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Ottiene informazioni sui treni
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> getTrains(
        it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTrainsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Cerca treni in base a criteri specifici
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse> searchTrains(
        it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTrainsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottiene dettagli di un treno specifico
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse> getTrainDetails(
        it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTrainDetailsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottiene gli orari dei treni per una specifica tratta
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse> getTrainSchedule(
        it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTrainScheduleMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_TRAINS = 0;
  private static final int METHODID_SEARCH_TRAINS = 1;
  private static final int METHODID_GET_TRAIN_DETAILS = 2;
  private static final int METHODID_GET_TRAIN_SCHEDULE = 3;

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
        case METHODID_GET_TRAINS:
          serviceImpl.getTrains((it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse>) responseObserver);
          break;
        case METHODID_SEARCH_TRAINS:
          serviceImpl.searchTrains((it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse>) responseObserver);
          break;
        case METHODID_GET_TRAIN_DETAILS:
          serviceImpl.getTrainDetails((it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse>) responseObserver);
          break;
        case METHODID_GET_TRAIN_SCHEDULE:
          serviceImpl.getTrainSchedule((it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse>) responseObserver);
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
          getGetTrainsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.TrainServiceProto.TrainRequest,
              it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse>(
                service, METHODID_GET_TRAINS)))
        .addMethod(
          getSearchTrainsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.TrainServiceProto.SearchTrainRequest,
              it.unical.trenical.grpc.train.TrainServiceProto.TrainResponse>(
                service, METHODID_SEARCH_TRAINS)))
        .addMethod(
          getGetTrainDetailsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsRequest,
              it.unical.trenical.grpc.train.TrainServiceProto.TrainDetailsResponse>(
                service, METHODID_GET_TRAIN_DETAILS)))
        .addMethod(
          getGetTrainScheduleMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.TrainServiceProto.ScheduleRequest,
              it.unical.trenical.grpc.train.TrainServiceProto.ScheduleResponse>(
                service, METHODID_GET_TRAIN_SCHEDULE)))
        .build();
  }

  private static abstract class TrainServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TrainServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return it.unical.trenical.grpc.train.TrainServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TrainService");
    }
  }

  private static final class TrainServiceFileDescriptorSupplier
      extends TrainServiceBaseDescriptorSupplier {
    TrainServiceFileDescriptorSupplier() {}
  }

  private static final class TrainServiceMethodDescriptorSupplier
      extends TrainServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    TrainServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (TrainServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TrainServiceFileDescriptorSupplier())
              .addMethod(getGetTrainsMethod())
              .addMethod(getSearchTrainsMethod())
              .addMethod(getGetTrainDetailsMethod())
              .addMethod(getGetTrainScheduleMethod())
              .build();
        }
      }
    }
    return result;
  }
}
