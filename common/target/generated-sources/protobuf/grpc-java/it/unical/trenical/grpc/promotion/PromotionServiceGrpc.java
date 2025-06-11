package it.unical.trenical.grpc.promotion;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Servizio promozioni
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: promotion_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class PromotionServiceGrpc {

  private PromotionServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "trenical.promotion.PromotionService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      it.unical.trenical.grpc.promotion.PromotionList> getListPromotionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListPromotions",
      requestType = com.google.protobuf.Empty.class,
      responseType = it.unical.trenical.grpc.promotion.PromotionList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      it.unical.trenical.grpc.promotion.PromotionList> getListPromotionsMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, it.unical.trenical.grpc.promotion.PromotionList> getListPromotionsMethod;
    if ((getListPromotionsMethod = PromotionServiceGrpc.getListPromotionsMethod) == null) {
      synchronized (PromotionServiceGrpc.class) {
        if ((getListPromotionsMethod = PromotionServiceGrpc.getListPromotionsMethod) == null) {
          PromotionServiceGrpc.getListPromotionsMethod = getListPromotionsMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, it.unical.trenical.grpc.promotion.PromotionList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListPromotions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.promotion.PromotionList.getDefaultInstance()))
              .setSchemaDescriptor(new PromotionServiceMethodDescriptorSupplier("ListPromotions"))
              .build();
        }
      }
    }
    return getListPromotionsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.promotion.AddPromotionRequest,
      it.unical.trenical.grpc.promotion.PromotionOperationResponse> getAddPromotionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddPromotion",
      requestType = it.unical.trenical.grpc.promotion.AddPromotionRequest.class,
      responseType = it.unical.trenical.grpc.promotion.PromotionOperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.promotion.AddPromotionRequest,
      it.unical.trenical.grpc.promotion.PromotionOperationResponse> getAddPromotionMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.promotion.AddPromotionRequest, it.unical.trenical.grpc.promotion.PromotionOperationResponse> getAddPromotionMethod;
    if ((getAddPromotionMethod = PromotionServiceGrpc.getAddPromotionMethod) == null) {
      synchronized (PromotionServiceGrpc.class) {
        if ((getAddPromotionMethod = PromotionServiceGrpc.getAddPromotionMethod) == null) {
          PromotionServiceGrpc.getAddPromotionMethod = getAddPromotionMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.promotion.AddPromotionRequest, it.unical.trenical.grpc.promotion.PromotionOperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddPromotion"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.promotion.AddPromotionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.promotion.PromotionOperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PromotionServiceMethodDescriptorSupplier("AddPromotion"))
              .build();
        }
      }
    }
    return getAddPromotionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<it.unical.trenical.grpc.promotion.DeletePromotionRequest,
      it.unical.trenical.grpc.promotion.PromotionOperationResponse> getDeletePromotionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeletePromotion",
      requestType = it.unical.trenical.grpc.promotion.DeletePromotionRequest.class,
      responseType = it.unical.trenical.grpc.promotion.PromotionOperationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<it.unical.trenical.grpc.promotion.DeletePromotionRequest,
      it.unical.trenical.grpc.promotion.PromotionOperationResponse> getDeletePromotionMethod() {
    io.grpc.MethodDescriptor<it.unical.trenical.grpc.promotion.DeletePromotionRequest, it.unical.trenical.grpc.promotion.PromotionOperationResponse> getDeletePromotionMethod;
    if ((getDeletePromotionMethod = PromotionServiceGrpc.getDeletePromotionMethod) == null) {
      synchronized (PromotionServiceGrpc.class) {
        if ((getDeletePromotionMethod = PromotionServiceGrpc.getDeletePromotionMethod) == null) {
          PromotionServiceGrpc.getDeletePromotionMethod = getDeletePromotionMethod =
              io.grpc.MethodDescriptor.<it.unical.trenical.grpc.promotion.DeletePromotionRequest, it.unical.trenical.grpc.promotion.PromotionOperationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeletePromotion"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.promotion.DeletePromotionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  it.unical.trenical.grpc.promotion.PromotionOperationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PromotionServiceMethodDescriptorSupplier("DeletePromotion"))
              .build();
        }
      }
    }
    return getDeletePromotionMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PromotionServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PromotionServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PromotionServiceStub>() {
        @java.lang.Override
        public PromotionServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PromotionServiceStub(channel, callOptions);
        }
      };
    return PromotionServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PromotionServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PromotionServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PromotionServiceBlockingStub>() {
        @java.lang.Override
        public PromotionServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PromotionServiceBlockingStub(channel, callOptions);
        }
      };
    return PromotionServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PromotionServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PromotionServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PromotionServiceFutureStub>() {
        @java.lang.Override
        public PromotionServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PromotionServiceFutureStub(channel, callOptions);
        }
      };
    return PromotionServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Servizio promozioni
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void listPromotions(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListPromotionsMethod(), responseObserver);
    }

    /**
     */
    default void addPromotion(it.unical.trenical.grpc.promotion.AddPromotionRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionOperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAddPromotionMethod(), responseObserver);
    }

    /**
     * <pre>
     *rpc UpdatePromotion(AddPromotionRequest) returns (PromotionOperationResponse);
     * </pre>
     */
    default void deletePromotion(it.unical.trenical.grpc.promotion.DeletePromotionRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionOperationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeletePromotionMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service PromotionService.
   * <pre>
   * Servizio promozioni
   * </pre>
   */
  public static abstract class PromotionServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return PromotionServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service PromotionService.
   * <pre>
   * Servizio promozioni
   * </pre>
   */
  public static final class PromotionServiceStub
      extends io.grpc.stub.AbstractAsyncStub<PromotionServiceStub> {
    private PromotionServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PromotionServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PromotionServiceStub(channel, callOptions);
    }

    /**
     */
    public void listPromotions(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListPromotionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addPromotion(it.unical.trenical.grpc.promotion.AddPromotionRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionOperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAddPromotionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *rpc UpdatePromotion(AddPromotionRequest) returns (PromotionOperationResponse);
     * </pre>
     */
    public void deletePromotion(it.unical.trenical.grpc.promotion.DeletePromotionRequest request,
        io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionOperationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeletePromotionMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service PromotionService.
   * <pre>
   * Servizio promozioni
   * </pre>
   */
  public static final class PromotionServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<PromotionServiceBlockingStub> {
    private PromotionServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PromotionServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PromotionServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public it.unical.trenical.grpc.promotion.PromotionList listPromotions(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListPromotionsMethod(), getCallOptions(), request);
    }

    /**
     */
    public it.unical.trenical.grpc.promotion.PromotionOperationResponse addPromotion(it.unical.trenical.grpc.promotion.AddPromotionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAddPromotionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *rpc UpdatePromotion(AddPromotionRequest) returns (PromotionOperationResponse);
     * </pre>
     */
    public it.unical.trenical.grpc.promotion.PromotionOperationResponse deletePromotion(it.unical.trenical.grpc.promotion.DeletePromotionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeletePromotionMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service PromotionService.
   * <pre>
   * Servizio promozioni
   * </pre>
   */
  public static final class PromotionServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<PromotionServiceFutureStub> {
    private PromotionServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PromotionServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PromotionServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.promotion.PromotionList> listPromotions(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListPromotionsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.promotion.PromotionOperationResponse> addPromotion(
        it.unical.trenical.grpc.promotion.AddPromotionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAddPromotionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *rpc UpdatePromotion(AddPromotionRequest) returns (PromotionOperationResponse);
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<it.unical.trenical.grpc.promotion.PromotionOperationResponse> deletePromotion(
        it.unical.trenical.grpc.promotion.DeletePromotionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeletePromotionMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_PROMOTIONS = 0;
  private static final int METHODID_ADD_PROMOTION = 1;
  private static final int METHODID_DELETE_PROMOTION = 2;

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
        case METHODID_LIST_PROMOTIONS:
          serviceImpl.listPromotions((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionList>) responseObserver);
          break;
        case METHODID_ADD_PROMOTION:
          serviceImpl.addPromotion((it.unical.trenical.grpc.promotion.AddPromotionRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionOperationResponse>) responseObserver);
          break;
        case METHODID_DELETE_PROMOTION:
          serviceImpl.deletePromotion((it.unical.trenical.grpc.promotion.DeletePromotionRequest) request,
              (io.grpc.stub.StreamObserver<it.unical.trenical.grpc.promotion.PromotionOperationResponse>) responseObserver);
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
          getListPromotionsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              it.unical.trenical.grpc.promotion.PromotionList>(
                service, METHODID_LIST_PROMOTIONS)))
        .addMethod(
          getAddPromotionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.promotion.AddPromotionRequest,
              it.unical.trenical.grpc.promotion.PromotionOperationResponse>(
                service, METHODID_ADD_PROMOTION)))
        .addMethod(
          getDeletePromotionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              it.unical.trenical.grpc.promotion.DeletePromotionRequest,
              it.unical.trenical.grpc.promotion.PromotionOperationResponse>(
                service, METHODID_DELETE_PROMOTION)))
        .build();
  }

  private static abstract class PromotionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PromotionServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return it.unical.trenical.grpc.promotion.PromotionServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PromotionService");
    }
  }

  private static final class PromotionServiceFileDescriptorSupplier
      extends PromotionServiceBaseDescriptorSupplier {
    PromotionServiceFileDescriptorSupplier() {}
  }

  private static final class PromotionServiceMethodDescriptorSupplier
      extends PromotionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    PromotionServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (PromotionServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PromotionServiceFileDescriptorSupplier())
              .addMethod(getListPromotionsMethod())
              .addMethod(getAddPromotionMethod())
              .addMethod(getDeletePromotionMethod())
              .build();
        }
      }
    }
    return result;
  }
}
