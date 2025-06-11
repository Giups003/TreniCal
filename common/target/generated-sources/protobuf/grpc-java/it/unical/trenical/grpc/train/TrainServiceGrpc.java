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
  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.SearchStationRequest,
      it.unical.trenical.grpc.train.SearchStationResponse> getSearchStationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchStations",
      requestType = it.unical.trenical.grpc.train.SearchStationRequest.class,
      responseType = it.unical.trenical.grpc.train.SearchStationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.SearchStationRequest,
      it.unical.trenical.grpc.train.SearchStationResponse> getSearchStationsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.SearchStationRequest, it.unical.trenical.grpc.train.SearchStationResponse> getSearchStationsMethod;
    if ((getSearchStationsMethod = TrainServiceGrpc.getSearchStationsMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getSearchStationsMethod = TrainServiceGrpc.getSearchStationsMethod) == null) {
          TrainServiceGrpc.getSearchStationsMethod = getSearchStationsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.SearchStationRequest, it.unical.trenical.grpc.train.SearchStationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchStations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.SearchStationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.SearchStationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("SearchStations"))
              .build();
        }
      }
    }
    return getSearchStationsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainRequest,
      it.unical.trenical.grpc.train.TrainResponse> getGetTrainsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTrains",
      requestType = it.unical.trenical.grpc.train.TrainRequest.class,
      responseType = it.unical.trenical.grpc.train.TrainResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainRequest,
      it.unical.trenical.grpc.train.TrainResponse> getGetTrainsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainRequest, it.unical.trenical.grpc.train.TrainResponse> getGetTrainsMethod;
    if ((getGetTrainsMethod = TrainServiceGrpc.getGetTrainsMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getGetTrainsMethod = TrainServiceGrpc.getGetTrainsMethod) == null) {
          TrainServiceGrpc.getGetTrainsMethod = getGetTrainsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.TrainRequest, it.unical.trenical.grpc.train.TrainResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTrains"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("GetTrains"))
              .build();
        }
      }
    }
    return getGetTrainsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.SearchTrainRequest,
      it.unical.trenical.grpc.train.TrainResponse> getSearchTrainsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchTrains",
      requestType = it.unical.trenical.grpc.train.SearchTrainRequest.class,
      responseType = it.unical.trenical.grpc.train.TrainResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.SearchTrainRequest,
      it.unical.trenical.grpc.train.TrainResponse> getSearchTrainsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.SearchTrainRequest, it.unical.trenical.grpc.train.TrainResponse> getSearchTrainsMethod;
    if ((getSearchTrainsMethod = TrainServiceGrpc.getSearchTrainsMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getSearchTrainsMethod = TrainServiceGrpc.getSearchTrainsMethod) == null) {
          TrainServiceGrpc.getSearchTrainsMethod = getSearchTrainsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.SearchTrainRequest, it.unical.trenical.grpc.train.TrainResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTrains"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.SearchTrainRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("SearchTrains"))
              .build();
        }
      }
    }
    return getSearchTrainsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainDetailsRequest,
      it.unical.trenical.grpc.train.TrainDetailsResponse> getGetTrainDetailsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTrainDetails",
      requestType = it.unical.trenical.grpc.train.TrainDetailsRequest.class,
      responseType = it.unical.trenical.grpc.train.TrainDetailsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainDetailsRequest,
      it.unical.trenical.grpc.train.TrainDetailsResponse> getGetTrainDetailsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.TrainDetailsRequest, it.unical.trenical.grpc.train.TrainDetailsResponse> getGetTrainDetailsMethod;
    if ((getGetTrainDetailsMethod = TrainServiceGrpc.getGetTrainDetailsMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getGetTrainDetailsMethod = TrainServiceGrpc.getGetTrainDetailsMethod) == null) {
          TrainServiceGrpc.getGetTrainDetailsMethod = getGetTrainDetailsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.TrainDetailsRequest, it.unical.trenical.grpc.train.TrainDetailsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTrainDetails"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainDetailsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.TrainDetailsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("GetTrainDetails"))
              .build();
        }
      }
    }
    return getGetTrainDetailsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.ScheduleRequest,
      it.unical.trenical.grpc.train.ScheduleResponse> getGetTrainScheduleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTrainSchedule",
      requestType = it.unical.trenical.grpc.train.ScheduleRequest.class,
      responseType = it.unical.trenical.grpc.train.ScheduleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.ScheduleRequest,
      it.unical.trenical.grpc.train.ScheduleResponse> getGetTrainScheduleMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.ScheduleRequest, it.unical.trenical.grpc.train.ScheduleResponse> getGetTrainScheduleMethod;
    if ((getGetTrainScheduleMethod = TrainServiceGrpc.getGetTrainScheduleMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getGetTrainScheduleMethod = TrainServiceGrpc.getGetTrainScheduleMethod) == null) {
          TrainServiceGrpc.getGetTrainScheduleMethod = getGetTrainScheduleMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.ScheduleRequest, it.unical.trenical.grpc.train.ScheduleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTrainSchedule"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.ScheduleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.ScheduleResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("GetTrainSchedule"))
              .build();
        }
      }
    }
    return getGetTrainScheduleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.GetTrainStopsRequest,
      it.unical.trenical.grpc.train.GetTrainStopsResponse> getGetTrainStopsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTrainStops",
      requestType = it.unical.trenical.grpc.train.GetTrainStopsRequest.class,
      responseType = it.unical.trenical.grpc.train.GetTrainStopsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.GetTrainStopsRequest,
      it.unical.trenical.grpc.train.GetTrainStopsResponse> getGetTrainStopsMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.GetTrainStopsRequest, it.unical.trenical.grpc.train.GetTrainStopsResponse> getGetTrainStopsMethod;
    if ((getGetTrainStopsMethod = TrainServiceGrpc.getGetTrainStopsMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getGetTrainStopsMethod = TrainServiceGrpc.getGetTrainStopsMethod) == null) {
          TrainServiceGrpc.getGetTrainStopsMethod = getGetTrainStopsMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.GetTrainStopsRequest, it.unical.trenical.grpc.train.GetTrainStopsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTrainStops"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.GetTrainStopsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.GetTrainStopsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("GetTrainStops"))
              .build();
        }
      }
    }
    return getGetTrainStopsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.ListRoutesRequest,
      it.unical.trenical.grpc.train.ListRoutesResponse> getListRoutesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListRoutes",
      requestType = it.unical.trenical.grpc.train.ListRoutesRequest.class,
      responseType = it.unical.trenical.grpc.train.ListRoutesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.ListRoutesRequest,
      it.unical.trenical.grpc.train.ListRoutesResponse> getListRoutesMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.train.ListRoutesRequest, it.unical.trenical.grpc.train.ListRoutesResponse> getListRoutesMethod;
    if ((getListRoutesMethod = TrainServiceGrpc.getListRoutesMethod) == null) {
      synchronized (TrainServiceGrpc.class) {
        if ((getListRoutesMethod = TrainServiceGrpc.getListRoutesMethod) == null) {
          TrainServiceGrpc.getListRoutesMethod = getListRoutesMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.train.ListRoutesRequest, it.unical.trenical.grpc.train.ListRoutesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListRoutes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.ListRoutesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.train.ListRoutesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TrainServiceMethodDescriptorSupplier("ListRoutes"))
              .build();
        }
      }
    }
    return getListRoutesMethod;
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
    default void searchStations(it.unical.trenical.grpc.train.SearchStationRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.SearchStationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchStationsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottiene informazioni sui treni
     * </pre>
     */
    default void getTrains(it.unical.trenical.grpc.train.TrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTrainsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Cerca treni in base a criteri specifici
     * </pre>
     */
    default void searchTrains(it.unical.trenical.grpc.train.SearchTrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTrainsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottiene dettagli di un treno specifico
     * </pre>
     */
    default void getTrainDetails(it.unical.trenical.grpc.train.TrainDetailsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainDetailsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTrainDetailsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottiene gli orari dei treni per una specifica tratta
     * </pre>
     */
    default void getTrainSchedule(it.unical.trenical.grpc.train.ScheduleRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.ScheduleResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTrainScheduleMethod(), responseObserver);
    }

    /**
     * <pre>
     * Ottieni le fermate di un treno specifico
     * </pre>
     */
    default void getTrainStops(it.unical.trenical.grpc.train.GetTrainStopsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.GetTrainStopsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTrainStopsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Recupera tutte le tratte
     * </pre>
     */
    default void listRoutes(it.unical.trenical.grpc.train.ListRoutesRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.ListRoutesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListRoutesMethod(), responseObserver);
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
    public void searchStations(it.unical.trenical.grpc.train.SearchStationRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.SearchStationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchStationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottiene informazioni sui treni
     * </pre>
     */
    public void getTrains(it.unical.trenical.grpc.train.TrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTrainsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Cerca treni in base a criteri specifici
     * </pre>
     */
    public void searchTrains(it.unical.trenical.grpc.train.SearchTrainRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTrainsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottiene dettagli di un treno specifico
     * </pre>
     */
    public void getTrainDetails(it.unical.trenical.grpc.train.TrainDetailsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainDetailsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTrainDetailsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottiene gli orari dei treni per una specifica tratta
     * </pre>
     */
    public void getTrainSchedule(it.unical.trenical.grpc.train.ScheduleRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.ScheduleResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTrainScheduleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Ottieni le fermate di un treno specifico
     * </pre>
     */
    public void getTrainStops(it.unical.trenical.grpc.train.GetTrainStopsRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.GetTrainStopsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTrainStopsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Recupera tutte le tratte
     * </pre>
     */
    public void listRoutes(it.unical.trenical.grpc.train.ListRoutesRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.ListRoutesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListRoutesMethod(), getCallOptions()), request, responseObserver);
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
    public it.unical.trenical.grpc.train.SearchStationResponse searchStations(it.unical.trenical.grpc.train.SearchStationRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchStationsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottiene informazioni sui treni
     * </pre>
     */
    public it.unical.trenical.grpc.train.TrainResponse getTrains(it.unical.trenical.grpc.train.TrainRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTrainsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Cerca treni in base a criteri specifici
     * </pre>
     */
    public it.unical.trenical.grpc.train.TrainResponse searchTrains(it.unical.trenical.grpc.train.SearchTrainRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTrainsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottiene dettagli di un treno specifico
     * </pre>
     */
    public it.unical.trenical.grpc.train.TrainDetailsResponse getTrainDetails(it.unical.trenical.grpc.train.TrainDetailsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTrainDetailsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottiene gli orari dei treni per una specifica tratta
     * </pre>
     */
    public it.unical.trenical.grpc.train.ScheduleResponse getTrainSchedule(it.unical.trenical.grpc.train.ScheduleRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTrainScheduleMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Ottieni le fermate di un treno specifico
     * </pre>
     */
    public it.unical.trenical.grpc.train.GetTrainStopsResponse getTrainStops(it.unical.trenical.grpc.train.GetTrainStopsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTrainStopsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Recupera tutte le tratte
     * </pre>
     */
    public it.unical.trenical.grpc.train.ListRoutesResponse listRoutes(it.unical.trenical.grpc.train.ListRoutesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListRoutesMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.SearchStationResponse> searchStations(
        it.unical.trenical.grpc.train.SearchStationRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchStationsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottiene informazioni sui treni
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.TrainResponse> getTrains(
        it.unical.trenical.grpc.train.TrainRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTrainsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Cerca treni in base a criteri specifici
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.TrainResponse> searchTrains(
        it.unical.trenical.grpc.train.SearchTrainRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTrainsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottiene dettagli di un treno specifico
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.TrainDetailsResponse> getTrainDetails(
        it.unical.trenical.grpc.train.TrainDetailsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTrainDetailsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottiene gli orari dei treni per una specifica tratta
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.ScheduleResponse> getTrainSchedule(
        it.unical.trenical.grpc.train.ScheduleRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTrainScheduleMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Ottieni le fermate di un treno specifico
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.GetTrainStopsResponse> getTrainStops(
        it.unical.trenical.grpc.train.GetTrainStopsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTrainStopsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Recupera tutte le tratte
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.train.ListRoutesResponse> listRoutes(
        it.unical.trenical.grpc.train.ListRoutesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListRoutesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEARCH_STATIONS = 0;
  private static final int METHODID_GET_TRAINS = 1;
  private static final int METHODID_SEARCH_TRAINS = 2;
  private static final int METHODID_GET_TRAIN_DETAILS = 3;
  private static final int METHODID_GET_TRAIN_SCHEDULE = 4;
  private static final int METHODID_GET_TRAIN_STOPS = 5;
  private static final int METHODID_LIST_ROUTES = 6;

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
        case METHODID_SEARCH_STATIONS:
          serviceImpl.searchStations((it.unical.trenical.grpc.train.SearchStationRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.SearchStationResponse>) responseObserver);
          break;
        case METHODID_GET_TRAINS:
          serviceImpl.getTrains((it.unical.trenical.grpc.train.TrainRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainResponse>) responseObserver);
          break;
        case METHODID_SEARCH_TRAINS:
          serviceImpl.searchTrains((it.unical.trenical.grpc.train.SearchTrainRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainResponse>) responseObserver);
          break;
        case METHODID_GET_TRAIN_DETAILS:
          serviceImpl.getTrainDetails((it.unical.trenical.grpc.train.TrainDetailsRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.TrainDetailsResponse>) responseObserver);
          break;
        case METHODID_GET_TRAIN_SCHEDULE:
          serviceImpl.getTrainSchedule((it.unical.trenical.grpc.train.ScheduleRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.ScheduleResponse>) responseObserver);
          break;
        case METHODID_GET_TRAIN_STOPS:
          serviceImpl.getTrainStops((it.unical.trenical.grpc.train.GetTrainStopsRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.GetTrainStopsResponse>) responseObserver);
          break;
        case METHODID_LIST_ROUTES:
          serviceImpl.listRoutes((it.unical.trenical.grpc.train.ListRoutesRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.train.ListRoutesResponse>) responseObserver);
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
          getSearchStationsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.SearchStationRequest,
              it.unical.trenical.grpc.train.SearchStationResponse>(
                service, METHODID_SEARCH_STATIONS)))
        .addMethod(
          getGetTrainsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.TrainRequest,
              it.unical.trenical.grpc.train.TrainResponse>(
                service, METHODID_GET_TRAINS)))
        .addMethod(
          getSearchTrainsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.SearchTrainRequest,
              it.unical.trenical.grpc.train.TrainResponse>(
                service, METHODID_SEARCH_TRAINS)))
        .addMethod(
          getGetTrainDetailsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.TrainDetailsRequest,
              it.unical.trenical.grpc.train.TrainDetailsResponse>(
                service, METHODID_GET_TRAIN_DETAILS)))
        .addMethod(
          getGetTrainScheduleMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.ScheduleRequest,
              it.unical.trenical.grpc.train.ScheduleResponse>(
                service, METHODID_GET_TRAIN_SCHEDULE)))
        .addMethod(
          getGetTrainStopsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.GetTrainStopsRequest,
              it.unical.trenical.grpc.train.GetTrainStopsResponse>(
                service, METHODID_GET_TRAIN_STOPS)))
        .addMethod(
          getListRoutesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.train.ListRoutesRequest,
              it.unical.trenical.grpc.train.ListRoutesResponse>(
                service, METHODID_LIST_ROUTES)))
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
              .addMethod(getSearchStationsMethod())
              .addMethod(getGetTrainsMethod())
              .addMethod(getSearchTrainsMethod())
              .addMethod(getGetTrainDetailsMethod())
              .addMethod(getGetTrainScheduleMethod())
              .addMethod(getGetTrainStopsMethod())
              .addMethod(getListRoutesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
